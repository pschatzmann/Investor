package ch.pschatzmann.stocks.integration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.ta4j.core.Indicator;
import org.ta4j.core.num.Num;

import ch.pschatzmann.stocks.Context;
import ch.pschatzmann.stocks.IStockData;
import ch.pschatzmann.stocks.accounting.HistoricValue;
import ch.pschatzmann.stocks.accounting.IHistoricValue;
import ch.pschatzmann.stocks.ta4j.indicator.IndicatorFromData;
import ch.pschatzmann.stocks.ta4j.indicator.IndicatorUtils;
import ch.pschatzmann.stocks.ta4j.indicator.Name;

/**
 * Named historic values. We can create them a collection of IHistoricValue or
 * from Indicators
 * 
 * @author pschatzmann
 *
 */
public class HistoricValues implements Serializable, Name {
	private static final long serialVersionUID = 1L;
	private List<IHistoricValue> values;
	private Map<Date, IHistoricValue> map;
	private List<Date> dates;
	private String name;

	/**
	 * Constructor for list of IHistoricValue
	 * 
	 * @param values
	 * @param name
	 */
	public HistoricValues(List<IHistoricValue> values, String name) {
		this.name = name;
		this.values = values;
	}


	/**
	 * Constructor for Indicator
	 * 
	 * @param indicator
	 * @param name
	 */

	public HistoricValues(Indicator<Num> indicator, String name) {
		this.name = name;
		this.values = IntStream.range(0, indicator.getBarSeries().getBarCount())
				.mapToObj(i -> new HistoricValue(Context.date(indicator.getBarSeries().getBar(i).getEndTime()), indicator.getValue(i).doubleValue()))
				.filter(hv -> hv.getDate()!=null)
				//.filter(hv -> !hv.getValue().isNaN() && !hv.getValue().isInfinite())
				.collect(Collectors.toList());
	}
	
	/**
	 * Constructor for Closing Prices for Stock Data
	 * 
	 * @param stockData
	 * @param name
	 */
	public HistoricValues(IStockData stockData, String name) {
		this.name = name;
		this.values = stockData.getHistory().stream()
				.map(rec -> new HistoricValue(rec.getDate(), rec.getClosing().doubleValue()))
				.collect(Collectors.toList());
	}

	public List<IHistoricValue> list() {
		return values;
	}
	
	public Stream<IHistoricValue> stream() {
		return values.stream();
	}

	public Date getDate(int i) {
		Date result = null;
		if (values != null && i < values.size()) {
			IHistoricValue hv = values.get(i);
			result = hv != null ? hv.getDate() : null;
		}
		return result;
	}
	
	public Double getValue(int i) {
		Double result = null;
		if (values != null && i < values.size()) {
			IHistoricValue hv = values.get(i);
			result = hv != null ? hv.getValue() : null;
		}
		return result;
	}

	public Set<Date> getDistinctDates() {
		return this.values.stream().map(r -> r.getDate()).collect(Collectors.toSet());
	}

	/**
	 * Determines the value at the indicated date. if nothing is found we return null
	 * @param date
	 * @return
	 */
	public Double getValue(Date date) {
		setupMap();
		IHistoricValue hv = map.get(Context.date(date));
		return hv == null ? null : hv.getValue();
	}
	
	/**
	 * We determine the historic value at the indicated date. If at the date no values exists we return the next available value
	 * @param date
	 * @return
	 */
	public IHistoricValue getNextValue(Date date) {
		Date searchDate = date;
		setupMap();
		IHistoricValue hv = map.get(Context.date(searchDate));
		if (hv==null) {
			int pos = Math.abs(Collections.binarySearch(dates, date));
			searchDate = dates.get(pos);
			hv = map.get(Context.date(searchDate));
		}
		return hv;		
	}
	
	/**
	 * We determine the historic value at the indicated date. If at the date no values exists we return the next available value.
	 * The date is a string if the format yyyy-MM-dd
	 * @param date
	 * @return
	 */
	public IHistoricValue getNextValue(String date) {
		return getNextValue(Context.date(date));
	}
	
	
	public boolean isValid(Date date) {
		Double v = this.getValue(date);
		return v!=null && !v.isNaN() && !v.isInfinite();
	}
	
	public Collection<Date> getInvalidDates(Collection<Date> dates){
		return dates.stream().filter(date -> !isValid(date)).collect(Collectors.toList());
	}

	private void setupMap() {
		if (map == null) {
			map = new TreeMap();
			this.values.forEach(v -> map.put(Context.date(v.getDate()), v));
			this.dates = new ArrayList(map.keySet());
		}
	}
	
	public void delete(Date date) {
		setupMap();
		IHistoricValue hv = map.get(Context.date(date));
		map.remove(Context.date(date), hv);
		if (hv!=null) {
			values.remove(hv);
		}
	}

	@Override
	public String getName() {
		return this.name;
	}

	/**
	 * Factory method from List<IHistoricValue> values
	 * 
	 * @param values
	 * @param name
	 * @return
	 */
	public static HistoricValues create(List<IHistoricValue> values, String name) {
		return new HistoricValues(values, name);
	}

	/**
	 * Factory method from Stream<IHistoricValue> values
	 * 
	 * @param values
	 * @param name
	 * @return
	 */
	public static HistoricValues create(Stream<IHistoricValue> values, String name) {
		return new HistoricValues(values.collect(Collectors.toList()), name);
	}

	/**
	 * Factory method for Indicator<Num>
	 * 
	 * @param indicator
	 * @param name
	 * @return
	 */
	public static HistoricValues create(Indicator<Num> indicator, String name) {
		return new HistoricValues(indicator, name);
	}

	/**
	 * Factory method for Indicator<Num> . The name will be determined from the
	 * indicator
	 * 
	 * @param indicator
	 * @return
	 */
	public static HistoricValues create(Indicator<Num> indicator) {
		return new HistoricValues(indicator, IndicatorUtils.getName(indicator));
	}
	
	/**
	 * Creates an empty HistoricValues
	 * @return
	 */
	public static HistoricValues create() {
		return new HistoricValues(new ArrayList(), "");
	}
	

	/**
	 * Factory method for Indicator<Num>
	 * 
	 * @param values
	 * @param name
	 * @return
	 */
	public static HistoricValues create(IStockData stockData, String name) {
		return new HistoricValues(stockData, name);
	}
	
	public static HistoricValues create(IStockData stockData) {
		return new HistoricValues(stockData, stockData.getStockID().toString());
	}
	
	public static HistoricValues create(List<Date> dates, List<Double> values, String name) {
		List<IHistoricValue> list = IntStream.range(0, values.size()).mapToObj(pos -> new HistoricValue(dates.get(pos),values.get(pos))).collect(Collectors.toList());
		return new HistoricValues(list, name);
	}

	public List<Double> getValues(){
		return list().stream().map(rec -> rec.getValue()).collect(Collectors.toList());
	}
	
	public Double[] getDoubleArray() {
		List<Double> values = this.getValues();
		return values.toArray(new Double[values.size()]);
	}

	public List<Date> getDates(){
		return list().stream().map(rec -> rec.getDate()).collect(Collectors.toList());
	}

	public int size() {
		return values.size();		
	}

	public boolean isEmpty() {
		return list().isEmpty();
	}
	
	public Indicator<Num> toIndicator() {
		return new IndicatorFromData(this);
	}
 

}
