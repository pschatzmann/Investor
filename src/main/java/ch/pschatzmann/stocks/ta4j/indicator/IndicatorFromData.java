package ch.pschatzmann.stocks.ta4j.indicator;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.ta4j.core.Bar;
import org.ta4j.core.BaseTimeSeries;
import org.ta4j.core.BaseTimeSeries.SeriesBuilder;
import org.ta4j.core.Indicator;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.num.Num;

import ch.pschatzmann.dates.CalendarUtils;
import ch.pschatzmann.stocks.Context;
import ch.pschatzmann.stocks.accounting.HistoricValue;
import ch.pschatzmann.stocks.accounting.IHistoricValue;
import ch.pschatzmann.stocks.integration.HistoricValues;
import ch.pschatzmann.stocks.integration.StockBar;
import ch.pschatzmann.stocks.integration.StockTimeSeries;

/**
 * Indicator which is constructed from a list of dates and a list of values
 * 
 * @author pschatzmann
 *
 */
public class IndicatorFromData implements Name, IIndicator<Num> {
	private static final long serialVersionUID = 1L;
	private List<Number> values;
	private List<Date> dates;
	private TimeSeries timeSeries;
	private String name;

	public IndicatorFromData(String name, List<IHistoricValue> values) {
		this(name, dates(values), values(values));
	}

	public IndicatorFromData(HistoricValues values) {
		this(values.getName(), values.list());
	}

	public IndicatorFromData(String name, List<Date> dates, List<Number> values) {
		this.values = values;
		this.name = name;
		this.timeSeries = getTimeSeries(name, dates, values);
	}

	protected TimeSeries getTimeSeries(String name, List<Date> inDates, List<Number> values) {
		this.dates = new ArrayList(inDates);
		// if we have too many dates we allign them with the values by removing the leading dates
		while(this.dates.size()>values.size()) {
			this.dates.remove(0);
		}
		
		// if we have not enough dates we generate new dates
		if (!dates.isEmpty()) {
			Date date = this.dates.get(this.dates.size()-1);
			while(this.dates.size()<values.size()) {
				date = CalendarUtils.nextWorkDay(date);
				this.dates.add(date);
			}			
		}
		
		// we add bars for all dates
		List<Bar> bars = new ArrayList();
		for (int j=0;j<this.dates.size();j++) {
			 bars.add(new StockBar(this.dates.get(j),values.get(j).doubleValue()));
		}
		return new StockTimeSeries(name, bars);
	}

	@Override
	public Num getValue(int index) {
		return numOf(values.get(index));
	}

	@Override
	public TimeSeries getTimeSeries() {
		return timeSeries;
	}

	@Override
	public Num numOf(Number number) {
		return Context.number(number);
	}

	@Override
	public String getName() {
		return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Determines the value at the indicated data
	 * @param date
	 * @return
	 */
	public Double getValueForDate(Date date) {
		int idx = dates.indexOf(date);
		return (idx>=0 && idx<values.size()) ? values.get(idx).doubleValue(): Double.NaN;
	}
	
	/**
	 * Returns all dates
	 * @return
	 */
	public List<Date> getDates() {
		return this.dates;
	}
	
	protected static List<Date> dates(List<IHistoricValue> values){
		return values.stream().map(v ->v.getDate()).collect(Collectors.toList());
	}

	protected static List<Number> values(List<IHistoricValue> values){
		return values.stream().map(v ->v.getValue()).collect(Collectors.toList());
	}
	


}
