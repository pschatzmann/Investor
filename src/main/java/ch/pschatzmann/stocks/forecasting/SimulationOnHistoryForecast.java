package ch.pschatzmann.stocks.forecasting;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.ta4j.core.Indicator;
import org.ta4j.core.num.Num;

import ch.pschatzmann.dates.CalendarUtils;
import ch.pschatzmann.stocks.IStockData;
import ch.pschatzmann.stocks.IStockRecord;
import ch.pschatzmann.stocks.StockData;
import ch.pschatzmann.stocks.StockRecord;
import ch.pschatzmann.stocks.accounting.HistoricValue;
import ch.pschatzmann.stocks.accounting.IHistoricValue;
import ch.pschatzmann.stocks.integration.HistoricValues;
import ch.pschatzmann.stocks.ta4j.indicator.IndicatorFromData;
import ch.pschatzmann.stocks.ta4j.indicator.IndicatorUtils;

/**
 * We use the reference time series with the indicated starting date to
 * calculate the historic price differences from the starting date and apply
 * them to the future.
 * 
 * @author pschatzmann
 *
 */

public class SimulationOnHistoryForecast implements Serializable, IForecast  {
	private static final long serialVersionUID = 1L;
	private Date referenceDate = null;
	private HistoricValues values;
	private HistoricValues refValues;
	private String name = "SimulationOnHistory";

	/**
	 * We use the forecast data as reference with a random date
	 */
	public SimulationOnHistoryForecast(Indicator<Num> indicator) {
		this.values = new HistoricValues(indicator, IndicatorUtils.getName(indicator));
		this.refValues = values;
	}

	/**
	 * We use the forecast data as reference with the indicated date
	 */
	public SimulationOnHistoryForecast(Indicator<Num> indicator, Date date) {
		this.values = new HistoricValues(indicator, IndicatorUtils.getName(indicator));
		this.refValues = values;
		this.referenceDate = date;
	}

	/**
	 * We use the indicated referenceValues (e.g an Index) starting from the start
	 * Date
	 * 
	 * @param referenceValues
	 * @param referenceStartDate
	 */
	public SimulationOnHistoryForecast(Indicator<Num> indicator,  Date referenceStartDate, Indicator<Num> reference) {
		this.values = new HistoricValues(indicator, IndicatorUtils.getName(indicator));
		this.refValues = new HistoricValues(reference, IndicatorUtils.getName(indicator));
		this.referenceDate = referenceStartDate;
	
	}

	/* (non-Javadoc)
	 * @see ch.pschatzmann.stocks.forecasting.IForecast#forecast(ch.pschatzmann.stocks.IStockData, int)
	 */
	@Override
	public HistoricValues forecast(int numberOfForecasts) throws Exception {
		// use random date which is able to give the indicate number of forecasts
		if (referenceDate == null) {
			Random r = new Random();
			int randomPos = r.nextInt(refValues.size() - numberOfForecasts);
			referenceDate = refValues.list().get(randomPos).getDate();
		}

		List<IHistoricValue> result = new ArrayList();
		// add existing dates
		for (IHistoricValue sr : values.list()) {
			result.add(sr);
		}

		// add simulated future
		IHistoricValue lastRefValue = null;
		IHistoricValue forecastValue = (IHistoricValue) values.list().get(values.size() - 1);
		Date lastDate = forecastValue.getDate();
		for (IHistoricValue v : refValues.list()) {
			if (lastRefValue == null) {
				lastRefValue = v;
			} else {
				if (v.getDate().after(referenceDate)) {
					HistoricValue temp = getNewRecord(lastRefValue, forecastValue, v);
					
					lastDate = CalendarUtils.nextWorkDay(lastDate);
					temp.setDate(lastDate);
					result.add(temp);
					forecastValue = temp;

					if (numberOfForecasts-- == 0) {
						break;
					}
				}
			}
			lastRefValue = v;
		}
		return HistoricValues.create(result, this.getName());
	}

	protected HistoricValue getNewRecord( IHistoricValue lastRefValue, IHistoricValue forecastValue,
			IHistoricValue v) {
		// calculate factors
		double value = v.getValue() / lastRefValue.getValue();
		return new HistoricValue((Date)null, value);
	}

	@Override
	public HistoricValues forecast(Date endDate) throws Exception {
		Date nextDate = getStartDay();
		int count = 0;
		while (nextDate.before(endDate)) {
			count++;
			nextDate = CalendarUtils.nextWorkDay(nextDate);
		}
		return forecast(count);
	}
	
	protected Date getStartDay() {
		Date start = new Date();;
		if (values!=null){
			List<Date> dates = values.getDates();
			start = dates.get(dates.size()-1);
		}
		if (CalendarUtils.isHoiday(start)) {
			start = CalendarUtils.nextWorkDay(start);
		}
		return start;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
