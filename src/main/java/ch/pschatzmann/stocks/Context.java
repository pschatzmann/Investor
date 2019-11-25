package ch.pschatzmann.stocks;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.Indicator;
import org.ta4j.core.BarSeries;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.pschatzmann.dates.DateRange;
import ch.pschatzmann.dates.StandardDateRangeSource;
import ch.pschatzmann.stocks.accounting.kpi.KPI;
import ch.pschatzmann.stocks.cache.CombinedCache;
import ch.pschatzmann.stocks.cache.ICache;
import ch.pschatzmann.stocks.cache.JcsCache;
import ch.pschatzmann.stocks.cache.RedisCache;
import ch.pschatzmann.stocks.errors.DateException;
import ch.pschatzmann.stocks.errors.UserException;
import ch.pschatzmann.stocks.input.DefaultReader;
import ch.pschatzmann.stocks.input.IReader;
import ch.pschatzmann.stocks.input.IReaderEx;
import ch.pschatzmann.stocks.parameters.ParameterValue;
import ch.pschatzmann.stocks.parameters.State;
import ch.pschatzmann.stocks.strategy.optimization.InputParameterName;
import ch.pschatzmann.stocks.utils.FileUtils;

/**
 * Common functionality to read and import-export stock information.
 * 
 * @author pschatzmann
 *
 */

public class Context implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(Context.class);
	private static String dateFormatString = "yyyy-MM-dd";
	private static DateFormat dateFormat = new SimpleDateFormat(dateFormatString);
	private static StockID cashID = new StockID("Cash", "Account");
	private static NumberFormat numberFormat;
	private static StandardDateRangeSource standardDateRangeSource = new StandardDateRangeSource();
	private static IReaderEx defaultReader = new DefaultReader();
	private static long id = new Date().getTime();
	private static Date defaultStartDate = date("1970-01-01");
	private static boolean cachingActive = false;
	private static ICache cache = setCache();
	private static Function<Number, Num> function = DoubleNum::valueOf;
	private static Properties properties=null;


	static {
		numberFormat = NumberFormat.getInstance();
		numberFormat.setMaximumFractionDigits(4);
		numberFormat.setGroupingUsed(false);
		numberFormat.setRoundingMode(RoundingMode.HALF_EVEN);
	}

	/**
	 * Returns the stock from the default reader (market archive)
	 * 
	 * @param symbol
	 * @param exchange
	 * @return
	 */
	public static synchronized IStockData getStockData(String symbol, String exchange) {
		return getStockData(new StockID(symbol, exchange), getDefaultReader());
	}

	
	/**
	 * Returns the stock from the default reader (market archive)
	 * 
	 * @param symbol
	 * @return
	 */
	public static synchronized IStockData getStockData(String symbol) {
		return getStockData(new StockID(symbol, ""), getDefaultReader());
	}
	
	
	/**
	 * Returns the stock from the default reader (market archive)
	 * 
	 * @param id
	 * @return
	 */
	public static synchronized IStockData getStockData(IStockID id) {
		return getStockData(id, getDefaultReader());
	}

	/**
	 * Returns the stock from the indicated reader 
	 * 
	 * @param id
	 * @param reader
	 * @return
	 */
	public static synchronized IStockData getStockData(IStockID id, IReader reader) {
		if (reader == null) {
			reader = defaultReader;
		}
		return new StockData(id,reader);
	}

	/**
	 * Load the stock data via the cache
	 * 
	 * @param id
	 * @return
	 */

	public static IStockData getStockData(String id, IReader r) {
		return getStockData(new StockID(id, ""), r);
	}


	/**
	 * Converts the string to a date
	 * @param str
	 * @return
	 */
	
	public synchronized static Date date(String str) {
		try {
			return str == null ? null : dateFormat.parse(str);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Converts the ZonedDateTime to a date 
	 * @param date
	 * @return
	 */
	public synchronized static Date date(ZonedDateTime date) {
		try {
			return Date.from(date.toInstant());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Converts the date to a standardized date (w/o time information)
	 * @param date
	 * @return
	 */
	public synchronized static Date date(Date date) {
		Date result = new Date(date.getTime());
		result.setHours(0);
		result.setMinutes(0);
		result.setSeconds(0);
		return result;
	}

	/**
	 * Formats the date as yyyy-MM-dd
	 * @param date
	 * @return
	 */
	public static String format(Date date) {
		return date == null ? "" : getDateFormat().format(date);
	}

	/**
	 * Formats a Num object
	 * @param d
	 * @return
	 */
	public synchronized static String format(Num d) {
		return numberFormat.format(d.doubleValue());
	}

	/**
	 * Formats a number 
	 * @param d
	 * @return
	 */
	public synchronized static String format(Number d) {
		return numberFormat.format(d);
	}

	/**
	 * The stock ID for cash
	 * @return
	 */
	public static StockID cashID() {
		return cashID;
	}

	/**
	 * Provides the state of the stock as map
	 * 
	 * @param id
	 * @param state
	 * @return
	 */
	public static Map<String, Object> getMap(IStockID id, State state) {
		Map<String, Object> result = new HashMap();
		result.put("ticker", id.getTicker());
		result.put("exchange", id.getExchange());
		for (Entry<InputParameterName, ParameterValue<Number>> e : state.getInput().getParameters().entrySet()) {
			result.put(e.getKey().name(), e.getValue().getValue());
		}

		for (Entry<KPI, ParameterValue<Number>> e : state.getResult().getParameters().entrySet()) {
			result.put(e.getKey().name(), e.getValue().getValue());
		}
		return result;
	}

	/**
	 * Returns a closed date format
	 * 
	 * @return
	 */
	public static SimpleDateFormat getDateFormat() {
		return (SimpleDateFormat) dateFormat.clone();
	}

	/**
	 * Returns the supported periods
	 * 
	 * @return
	 * @throws DateException
	 */
	public static List<DateRange> getDates() throws DateException {
		return standardDateRangeSource.getDates();
	}

	/**
	 * Determines the date starting from today adding the indicated number of days
	 * 
	 * @param offsetDays
	 * @return
	 */
	public static Date getDateWithOffsetDays(Date date, int offsetDays) {
		Calendar c = Calendar.getInstance();
		c.setTime(date); // Now use today date.
		c.add(Calendar.DATE, offsetDays); // Adding 5 days
		return c.getTime();
	}

	/**
	 * Determines the date starting from today adding the indicated number of months
	 * 
	 * @param offsetMonts
	 * @return
	 */
	public static Date getDateWithOffsetMonths(Date date, int offsetMonts) {
		Calendar c = Calendar.getInstance();
		c.setTime(date); // Now use today date.
		c.add(Calendar.MONTH, offsetMonts); // Adding 5 months
		return c.getTime();
	}

	/**
	 * Determines the date by adding the number of years
	 * @param date
	 * @param years
	 * @return
	 */
	public static Date getDateWithOffsetYears(Date date, int years) {
		return getDateWithOffsetMonths(date, years*12);
	}

	/**
	 * Determines the year for the indicated date
	 * 
	 * @param data
	 * @return
	 */
	public static int getYear(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		return c.get(Calendar.YEAR);
	}

	/**
	 * Activate or deactivate caching
	 * 
	 * @param active
	 */
	public static void setCacheActive(boolean active) {
		cachingActive = active;
	}

	/**
	 * Checks if caching is active
	 * 
	 * @return
	 */
	public static boolean isCacheActive() {
		return cachingActive;
	}

	/**
	 * Resets the cache
	 */
	public static void resetCache() {
		StockData.resetCache();
	}


	/**
	 * Returns the default stock reader
	 * 
	 * @return
	 */
	public static IReaderEx getDefaultReader() {
		return defaultReader;
	}

	/**
	 * Defines the default stock reader
	 * 
	 * @param defaultReader
	 */
	public static void setDefaultReader(IReaderEx defaultReader) {
		Context.defaultReader = defaultReader;
	}

	/**
	 * Checks if a string is null or empty
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isEmpty(String str) {
		return str == null || str.isEmpty();
	}

	/**
	 * Converts a collection of object to a list of maps
	 * 
	 * @param c
	 * @param dateFormat
	 * @return
	 */
	public static List<Map> toListOfMap(Collection c, String dateFormat) {
		List<Map> result = new ArrayList();
		ObjectMapper objectMapper = new ObjectMapper();
		if (!isEmpty(dateFormat)) {
			objectMapper.setDateFormat(new SimpleDateFormat(dateFormat));
		}
		for (Object obj : c) {
			if (obj instanceof Collection) {
				Map record = new TreeMap();
				List values = new ArrayList((Collection)obj);
				for (int j=0;j<values.size();j++) {
					record.put("["+(j+1)+"]",String.valueOf(values.get(j)));
				}
				result.add(record);
			} else {				
				Map<String, Object> map = objectMapper.convertValue(obj, Map.class);
				result.add(map);
			}
		}
		return result;
	}
	
	/**
	 * Converts a collection of object to a list of maps. Dates are converted to a String
	 * 
	 * @param c
	 * @return
	 */
	public static List<Map> toListOfMap(Collection c) {
		return toListOfMap(c,  dateFormatString);
	}


	/**
	 * Determines a value from a Indicator.
	 * 
	 * @param ind
	 * @param index
	 * @return
	 */
	public static Num getValue(Indicator<Num> ind, int index) {
		try {
			return ind.getValue(index);
		} catch (Exception ex) {
			return Context.number(Double.NaN);
		}
	}

	/**
	 * Converts a decimal to a Number
	 * 
	 * @param volume
	 * @return
	 */
	public static Number toDouble(Num volume) {
		return volume == null ? Double.NaN : volume.doubleValue();
	}

	/**
	 * Determines a the value of a system property or environment property
	 * 
	 * @param name
	 * @return
	 */
	public static String getProperty(String name) {
		// java properties
		String str = System.getProperty(name);
		if (str == null) {
			// system environment
			str = System.getenv(name);
		}
		// get value from investor.properties 
		if (properties!=null) {
			str = properties.getProperty(name);
			
		}
		
		// re-load properties
		if (str == null) {
			try {
				// investor.properties from file
				properties = FileUtils.getProperties(new File("investor.properties"));
				if (properties != null) {
					str = properties.getProperty(name);
				}
			} catch (Exception e) {
				LOG.debug("Could not load properties",e);
			}
			
			// investor.properties from classpath
			if (str==null) {
				InputStream is=null; 
				try {
					properties = new Properties();
					is = Context.class.getClassLoader().getResourceAsStream("investor.properties"); 
					if (is!=null) {
						properties.load(is);
						str = properties.getProperty(name);
					}
				} catch (Exception e) {
					LOG.error("Could not load properties",e);
				} finally {
					if (is!=null) {
						try {
							is.close();
						} catch (IOException e) {
						}
					}
				}
			}
		}
		
		
		if (str==null) {
			LOG.info("Please define the property: "+name);
		}
		return str;
	}
	
	/**
	 * Determines the value of a mandatory property
	 * @param name
	 * @return
	 */
	public static String getPropertyMandatory(String name) {
		String str = getProperty(name);
		if (str==null) {
			LOG.error("Please define the property: "+name);
		}
		return str;
	}

	/**
	 * Determines a the value of a system property or environment property. If
	 * nothing is defined we use the default value
	 * 
	 * @param name
	 * @param defaultValue
	 * @return
	 */
	public static String getProperty(String name, String defaultValue) {
		String str = Context.getProperty(name);
		return str == null ? defaultValue : str;
	}

	/**
	 * Returns the head of a list
	 * 
	 * @param c
	 * @param len
	 * @return
	 */
	public static List head(Collection c, int len) {
		return new ArrayList(c).subList(0, Math.min(len, c.size()));
	}

	/**
	 * Returns the tail of a list
	 * 
	 * @param c
	 * @param len
	 * @return
	 */
	public static List tail(Collection c, int len) {
		return new ArrayList(c).subList(c.size() - len - 1, c.size() - 1);
	}

	/**
	 * Converts an object to a string
	 * 
	 * @param obj
	 * @return
	 */
	public static String toString(Object obj) {
		String result = "";
		if (obj != null) {
			if (obj instanceof Date) {
				result = format((Date) obj);
			} else if (obj instanceof Num) {
				result = format((Num) obj);
			} else if (obj instanceof Number) {
				result = format((Number) obj);
			} else {
				result = obj.toString();
			}
		}
		return result;
	}

	/**
	 * Provides date ranges which are defined by the indicated start dates
	 * 
	 * @param dates
	 * @return
	 */
	public static List<DateRange> getDateRanges(Date... dates) {
		List<DateRange> result = new ArrayList();
		for (int i = 0; i < dates.length; ++i) {
			Date end = i < dates.length - 1 ? Context.getDateWithOffsetDays(dates[i + 1], -1) : new Date();
			result.add(new DateRange(dates[i], end));
		}
		return result;
	}
	
	/**
	 * Returns the date range for the indicated start and end date
	 * @param start
	 * @param end
	 * @return
	 */
	public static DateRange getDateRange(Date start, Date end) {
		return new DateRange(start, end);
	}
	
	/**
	 * Returns the date range from the indicated date range until now
	 * @param start
	 * @return
	 */
	public static DateRange getDateRange(Date start) {
		return new DateRange(start, new Date());
	}


	/**
	 * Provides date ranges which are defined by the indicated start dates
	 * 
	 * @param dates
	 * @return
	 */
	public static List<DateRange> getDateRanges(String... dates) {
		List<DateRange> result = new ArrayList();
		for (int i = 0; i < dates.length; ++i) {
			Date end = i < dates.length - 1 ? Context.getDateWithOffsetDays(Context.date(dates[i + 1]), -1)
					: new Date();
			result.add(new DateRange(Context.date(dates[i]), end));
		}
		return result;
	}

	/**
	 * Returns an ID of the JVM. Currently we use the time
	 * 
	 * @return
	 */
	public static long getID() {
		return id;
	}

	/**
	 * Returns the default start date for reading the stock history
	 * 
	 * @return
	 */
	public static Date getDefaultStartDate() {
		return defaultStartDate;
	}

	/**
	 * Defines the default start date for reading the stock history
	 * 
	 * @param start
	 */
	public static void setDefaultStartDate(Date start) {
		defaultStartDate = start;
	}

	/**
	 * Defines the default start date for reading the stock history
	 * 
	 * @param start
	 */
	public static void setDefaultStartDate(String start) {
		defaultStartDate = date(start);
	}

	/**
	 * Returns the Cache implementation for the stock history. We use JcsCache as
	 * default.
	 * 
	 * @return
	 */
	public static ICache getCache() {
		return cache;
	}

	/**
	 * Defines the default cache implementation for the stock history.
	 * 
	 * @param cache
	 */
	public static void setCache(ICache cache) {
		Context.cache = cache;
	}
	
	/**
	 * Seths the defaut cache
	 * @return
	 */
	public static ICache setCache() {
		for (Class<? extends ICache> cacheClass : Arrays.asList(CombinedCache.class, RedisCache.class, JcsCache.class)) {
			try {
				ICache cache = cacheClass.newInstance();
				cache.put("test", new ArrayList());
				setCacheActive(true);
				LOG.info("The cache is {}",cacheClass.getSimpleName());
				return cache;
			} catch(Exception ex) {				
			}
		}
		setCacheActive(false);
		return null;
		
	}

	/**
	 * Ta4J is supporting multiple number implementations. We decided to use doubles 
	 * 
	 * @param value
	 * @return
	 */
	public static Num number(Number value) {
		return function.apply(value);
	}
	
	/**
	 * Defines the default ta4j number implementation
	 * @param numFunction
	 */
	public static void setNumberImplementation(java.util.function.Function<Number,Num> numFunction) {
		function = numFunction;
	}
	
	/**
	 * Returns the default ta4j number implementation
	 * @return
	 */
	public static java.util.function.Function<Number,Num> getNumberImplementation() {
		return function;
	}
	

	/**
	 * Splits the data into a training and a test dataset
	 * @param sd
	 * @param trainingFactor
	 * @return
	 */
	public static List<IStockData> split(IStockData sd, double trainingFactor){
		int pos = (int) (sd.size() * trainingFactor);
		List<IStockRecord> history = sd.getHistory();
		List<IStockRecord> training = history.subList(0, pos);
		List<IStockRecord> test = history.subList(pos, history.size()-1);
		return Arrays.asList(new StockData(sd.getStockID(),training),new StockData(sd.getStockID(),test));
	}
	
	/**
	 * Determines the dates from the indicator
	 * @param indicator
	 * @return
	 */
	public static List<Date> getDates(Indicator indicator){
		return getDates(indicator.getBarSeries());
	}
	
	/**
	 * Determines the dates from the TimeSeries
	 * @param ts
	 * @return
	 */
	public static List<Date> getDates(BarSeries ts){
		return ts.getBarData().stream()
			.map(bar -> toDate(bar.getBeginTime().toInstant()))
			.collect(Collectors.toList());
	}

	
	/**
	 * Converts an instant to a date
	 * @param instant
	 * @return
	 */
	public static Date toDate(Instant instant) {
		return Date.from(instant);
	}
	
	
}
