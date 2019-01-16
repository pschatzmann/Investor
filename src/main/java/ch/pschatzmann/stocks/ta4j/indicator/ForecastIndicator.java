package ch.pschatzmann.stocks.ta4j.indicator;

import java.util.Date;

import org.ta4j.core.Indicator;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.num.Num;

import ch.pschatzmann.stocks.Context;
import ch.pschatzmann.stocks.forecasting.IForecast;
import ch.pschatzmann.stocks.integration.HistoricValues;

/**
 * We use IForecast as Indicator
 * 
 * @author pschatzmann
 *
 */

public class ForecastIndicator implements Indicator<Num>, IIndicator<Num>, Name {
	private static final long serialVersionUID = 1L;
	private Indicator<Num> indicator;
	private String name;
	
	public ForecastIndicator(IForecast forecast, Date endDate) throws Exception{
		HistoricValues hv = forecast.forecast(endDate);
		this.indicator = new IndicatorFromData(hv.getName(),hv.list());
	}

	public ForecastIndicator(IForecast forecast, int numberOfPeriods) throws Exception{
		HistoricValues hv = forecast.forecast(numberOfPeriods);
		indicator = new IndicatorFromData(hv.getName(),hv.list());
	}

	
	@Override
	public Num getValue(int index) {
		return indicator.getValue(index);
	}

	@Override
	public TimeSeries getTimeSeries() {
		return indicator.getTimeSeries();
	}

	@Override
	public Num numOf(Number value) {
		return Context.number(value);
	}

	@Override
	public String getName() {
		return ((Name)indicator).getName();
	}
}
