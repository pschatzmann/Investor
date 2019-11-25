package ch.pschatzmann.stocks.data.universe;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.pschatzmann.stocks.IStockID;
import ch.pschatzmann.stocks.StockID;
import ch.pschatzmann.stocks.errors.UniverseException;
import ch.pschatzmann.stocks.utils.FileUtils;

/**
 * Determines the trading symbols with the biggest parameter values for the
 * indicated year from the Edgar database for US stocks. We also support the
 * query over multiple years by indicating a multiplication factor per year
 *
 * @author pschatzmann
 *
 */

public class EdgarUniverse implements IUniverse {
	private static final Logger LOG = LoggerFactory.getLogger(EdgarUniverse.class);
	private List<IStockID> data = null;
	private YearInfo yearInfo;
	private String queryName = "edgarQuery.json";
	private List<String> parameters = Arrays.asList("NetIncomeLoss", "ProfitLoss", "OperatingIncomeLoss");
	private Boolean alternatives = true;
	private Boolean calculatePercentChange = false;
	private double minValue = 0;

	/**
	 * Determine the Universe as the sum of the NetIncomeLoss of the current year
	 *
	 * @throws IOException
	 */
	public EdgarUniverse() {
		setYearInfo(Calendar.getInstance().get(Calendar.YEAR) - 1);
	}

	/**
	 * Determine the Universe as the sum of the NetIncomeLoss
	 * 
	 * @param year
	 * @param count
	 * @throws IOException
	 */
	public EdgarUniverse(int year) {
		setYearInfo(year);
	}

	/**
	 * Determine the Universe as the sum of the indicated parameter values
	 * 
	 * @param year
	 * @param count
	 * @param parameters
	 * @throws IOException
	 */
	public EdgarUniverse(int year, List<String> parameters) {
		setYearInfo(year);
		this.parameters = parameters;
		this.alternatives = true;
	}

	/**
	 * 
	 * @param year
	 * @param count
	 * @param parameters
	 * @param alt
	 * @throws IOException
	 */
	public EdgarUniverse(int year, List<String> parameters, boolean alt) {
		setYearInfo(year);
		this.parameters = parameters;
		this.alternatives = alt;
	}

	/**
	 * 
	 * @param yearInfo
	 * @param count
	 * @param parameters
	 * @param alt
	 * @throws IOException
	 */
	public EdgarUniverse(int endYear, List<? extends Number> factors, List<String> parameters, boolean alt) {
		setYearInfo(endYear, factors);
		this.parameters = parameters;
		this.alternatives = alt;
	}

	/**
	 * Execute the webservice and fill the result list
	 * 
	 * @param year
	 * @param count
	 * @param queryName
	 * @param parameters
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 * @throws ClientProtocolException
	 * @throws UniverseException
	 */
	private void setupUniverse()
			throws IOException, UnsupportedEncodingException, ClientProtocolException, UniverseException {

		// for percent changes we need one more year in the past
		if (this.isCalculatePercentChange()) {
			// add one more history year
			this.yearInfo.putFactor(this.yearInfo.getMinYear() - 1, 0.0);
		}

		InputStream is = executeWebservice(yearInfo, 0, queryName, parameters, alternatives);
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));

		String line = null;
		List<List> values = new ArrayList();
		try {
			// read header line
			line = reader.readLine();
			if (line != null) {
				String[] headerFields = line.split(";");
				// process result records
				while ((line = reader.readLine()) != null) {
					String[] sa = line.split(";");
					if (!sa[0].equals("[OTHERS]")) {
						// in some exceptional cases we have 2 trading symbols. We just use the first!
						for (String id : sa[0].split(",")) {
							Double value = this.isCalculatePercentChange()
									? this.calculatePercentValue(headerFields, sa, yearInfo)
									: calculateValue(headerFields, sa, yearInfo);
							values.add(Arrays.asList(id, value));
						}
					}
				}
			}

			// sort by value
			Collections.sort(values, new Comparator<List>() {
				@Override
				public int compare(List o1, List o2) {
					return ((Double) o2.get(1)).compareTo((Double) o1.get(1));
				}
			});

			// provide only top n values
			data = new ArrayList();
			for (List rec : values) {
				StockID id = new StockID((String) rec.get(0), "");
				data.add(id);
			}

		} finally {
			try {
				is.close();
			} catch (IOException e) {
			}
		}
	}

	/**
	 * REST post to https://pschatzmann.ch/edgar/db/values
	 * 
	 * @param yearInfo
	 * @param count
	 * @param queryName
	 * @param parameters
	 * @param alt
	 * @return
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 * @throws ClientProtocolException
	 */
	private InputStream executeWebservice(YearInfo yearInfo, int count, String queryName, List<String> parameters,
			Boolean alt) throws IOException, UnsupportedEncodingException, ClientProtocolException {
		String jsonQuery = getQuery(yearInfo, count, queryName, parameters, alt);
		HttpClient client = HttpClientBuilder.create().build();
		HttpPost request = new HttpPost("http://pschatzmann.ch/edgar/db/values");
		StringEntity params = new StringEntity(jsonQuery);
		request.addHeader("content-type", "application/json");
		request.addHeader("Accept", "text/plain");
		request.setEntity(params);
		HttpResponse response = client.execute(request);
		HttpEntity entity = response.getEntity();

		InputStream is = entity.getContent();
		return is;
	}

	/**
	 * Calculate the total values over all years by applying a year specific factor
	 * 
	 * @param headerFields
	 * @param rec
	 * @param yearInfo
	 * @return
	 */
	private Double calculateValue(String[] headerFields, String[] rec, YearInfo yearInfo) {
		double value = 0;
		for (int j = 1; j < rec.length; j++) {
			Integer year = Integer.valueOf(headerFields[j]);
			double factor = yearInfo.getFactor(year);
			double yearValue = 0.0;
			try {
				yearValue = Double.valueOf(rec[j]);
			} catch (Exception ex) {
			}
			value += value + (factor * yearValue);
		}
		return value;
	}

	/**
	 * Calculate total of the yearly percent changes
	 * 
	 * @param headerFields
	 * @param rec
	 * @param yearInfo
	 * @return
	 */
	private Double calculatePercentValue(String[] headerFields, String[] rec, YearInfo yearInfo) {
		double value = 0;
		double totalValue = 0;
		for (int j = 1; j < rec.length - 1; j++) {
			Integer year = Integer.valueOf(headerFields[j + 1]);
			double factor = yearInfo.getFactor(year);
			double yearValue0 = 0.0;
			double yearValue1 = 0.0;
			double percentValue = 0.0;
			try {
				yearValue0 = Double.valueOf(rec[j]);
				yearValue1 = Double.valueOf(rec[j + 1]);
				totalValue += yearValue1;			
				if (yearValue0 != 0.0) {
					percentValue = (yearValue1 - yearValue0) / yearValue0;
				} else {
					percentValue = 0;
				}
			} catch (Exception ex) {
			}
			value += value + (factor * percentValue);
		}
		
		// we do not consider % changes if the value is below the indicated level
		if (totalValue < this.minValue*rec.length) {
			value = 0;
		}
		
		return value;
	}

	/**
	 * Determine the JSON query for the webservice
	 * 
	 * @param endYear
	 * @param count
	 * @param queryName
	 * @param parameters
	 * @return
	 * @throws IOException
	 */
	private String getQuery(YearInfo yearInfo, int count, String queryName, List<String> parameters, Boolean alt)
			throws IOException {
		String result = FileUtils.read("/edgar/" + queryName, Charset.defaultCharset());
		result = result.replace("%year", toString(yearInfo.years()));
		result = result.replace("%topN", "" + String.valueOf(count + 1));
		result = result.replace("%parameters", "" + toString(parameters));
		if (alt != null) {
			result = result.replace("%asAlternative", "" + alt);
		}
		return result;
	}

	public Boolean isCalculatePercentChange() {
		return calculatePercentChange;
	}

	public void setCalculatePercentChange(Boolean calculatePercentChange, double avgMinValuePerYear) {
		this.minValue = avgMinValuePerYear;
		this.calculatePercentChange = calculatePercentChange;
	}

	public void setCalculatePercentChange(Boolean calculatePercentChange) {
		this.minValue = 0;
		this.calculatePercentChange = calculatePercentChange;
	}

	
	public YearInfo getYearInfo() {
		return yearInfo;
	}

	public void setYearInfo(YearInfo yearInfo) {
		this.yearInfo = yearInfo;
	}

	public void setYearInfo(int startYear, List<? extends Number> factors) {
		this.yearInfo = new YearInfo(startYear, factors);
	}

	public void setYearInfo(int startYear) {
		this.yearInfo = new YearInfo(startYear, Arrays.asList(1.0));
	}

	public String getQueryName() {
		return queryName;
	}

	public void setQueryName(String queryName) {
		this.queryName = queryName;
	}

	public List<String> getParameters() {
		return parameters;
	}

	public void setParameters(List<String> parameters) {
		this.parameters = parameters;
	}

	private String toString(List<String> parameters) {
		StringBuffer sb = new StringBuffer();
		boolean first = true;
		for (String par : parameters) {
			if (!first) {
				sb.append(",");
			}
			sb.append("\"");
			sb.append(par);
			sb.append("\"");
			first = false;
		}
		return sb.toString();
	}

	@Override
	public List<IStockID> list()  {
		if (data == null) {
			try {
				setupUniverse();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return data;
	}

	/***
	 * Provides the first n records
	 * 
	 */
	public List<IStockID> list(int count)  {
		if (data == null) {
			try {
				setupUniverse();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		int max = Math.min(count, data.size());
		return data.subList(0, max);
	}

	/**
	 * Provides the list of the first n valid values
	 * 
	 * @param count
	 * @param validValues
	 * @return
	 * @throws UniverseException
	 */
	public List<IStockID> list(int count, List<IStockID> validValues)  {
		if (data == null) {
			try {
				setupUniverse();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		List<IStockID> result = new ArrayList(data);
		result.retainAll(validValues);
		int max = Math.min(count, result.size());
		return result.subList(0, max);
	}

	@Override
	public String toString() {
		try {
			return this.list().toString();
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	/**
	 * Logic to determine the number of years and the related factors.
	 * 
	 * @author pschatzmann
	 *
	 */
	private static class YearInfo {
		private Map<Integer, Double> map = new TreeMap();

		public YearInfo(int endYear, List<? extends Number> factors) {
			int year = endYear - factors.size();
			for (Number factor : factors) {
				year++;
				putFactor(year, factor.doubleValue());
			}
		}

		public Double getFactor(int year) {
			Double result = map.get(year);
			return result == null ? 1.0 : result;
		}

		public List<String> years() {
			return map.keySet().stream().map(r -> String.valueOf(r)).collect(Collectors.toList());
		}

		public void putFactor(int year, Double factor) {
			map.put(year, factor.doubleValue());
		}

		public int getMinYear() {
			return map.keySet().iterator().next();
		}

	}

}
