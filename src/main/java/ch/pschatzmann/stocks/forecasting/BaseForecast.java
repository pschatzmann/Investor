package ch.pschatzmann.stocks.forecasting;

import java.util.Date;
import java.util.List;

import org.ta4j.core.Indicator;
import org.ta4j.core.num.Num;

import ch.pschatzmann.dates.CalendarUtils;
import ch.pschatzmann.stocks.integration.HistoricValues;

/**
 * Common Forecast Functions
 * 
 * @author pschatzmann
 *
 */
public abstract class BaseForecast implements IForecast {
	private static final long serialVersionUID = 1L;
	private HistoricValues values = HistoricValues.create();
	private Date startDay;
    private String name = "";

	public BaseForecast(HistoricValues values) {
		this.values = values;
	}

	public BaseForecast(Date startDay) {
		this.startDay = startDay;
	}

	public BaseForecast() {
		this.startDay = new Date();
	}

	@Override
	public HistoricValues forecast(Date endDate) throws Exception {
		Date nextDate = getStartOfForecastDay();
		int count = 0;
		while (nextDate.before(endDate)) {
			count++;
			nextDate = CalendarUtils.nextWorkDay(nextDate);
		}
		return forecast(count);
	}
	

	/**
	 * Determines the last day of the history and gets the next working day
	 * 
	 * @param stockData
	 * @return
	 */
	protected Date getStartOfForecastDay() {
		Date result = this.startDay;
		if (!values.isEmpty() && startDay==null){
			List<Date> dates = values.getDates();
			result = dates.get(dates.size()-1);
			result = CalendarUtils.nextWorkDay(result);
		} 		
		if (result==null) {
			result = new Date();
		}
		if (CalendarUtils.isHoiday(result)) {
			result = CalendarUtils.nextWorkDay(result);
		}
		return result;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public HistoricValues getValues() {
		return values;
	}

	@Override
	public abstract HistoricValues forecast(int numberOfForecasts) throws Exception;

}
