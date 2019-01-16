package ch.pschatzmann.stocks.forecasting;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import org.ta4j.core.Indicator;
import org.ta4j.core.num.Num;

import com.workday.insights.timeseries.arima.Arima;
import com.workday.insights.timeseries.arima.struct.ArimaParams;
import com.workday.insights.timeseries.arima.struct.ForecastResult;

import ch.pschatzmann.dates.CalendarUtils;
import ch.pschatzmann.stocks.accounting.HistoricValue;
import ch.pschatzmann.stocks.accounting.IHistoricValue;
import ch.pschatzmann.stocks.integration.HistoricValues;
import ch.pschatzmann.stocks.ta4j.indicator.IndicatorFromData;
import ch.pschatzmann.stocks.ta4j.indicator.IndicatorUtils;

/**
 * Forecast using ARIMA
 * 
 * @author pschatzmann
 *
 */
public class ARIMAForecast implements IForecast {
	// Set ARIMA model parameters.
	private ArimaParams params;
	private HistoricValues values;
	private String name = "ARIMA";
	

	public ARIMAForecast(HistoricValues values) {
		this.params = new ArimaParams(3, 0, 3, 1, 1, 0, 0);
		this.values = values;
	}

	public ARIMAForecast(Indicator<Num> indicator, ArimaParams params) {
		this.params = params;
		this.values = new HistoricValues(indicator, IndicatorUtils.getName(indicator)+"-ForecasterARIMA");
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

	
	@Override
	public HistoricValues forecast(int numberOfForecasts) throws Exception {
		Double[] array = values.getValues().toArray(new Double[values.size()]);
		double[] unboxed = Stream.of(array).mapToDouble(Double::doubleValue).toArray();
		ForecastResult forcast = Arima.forecast_arima(unboxed, numberOfForecasts, params);
		double[] forecastedValues = forcast.getForecast();

		List<IHistoricValue> result = new ArrayList();
		// add existing dates
		for (IHistoricValue sr : values.list()) {
			result.add(sr);
		}

		// add future dates
		Date nextDate = getStartDay();
		for (int j = 0; j < numberOfForecasts; j++) {
			result.add(new HistoricValue(nextDate, forecastedValues[j]));
			nextDate = CalendarUtils.nextWorkDay(nextDate);
		}

		return HistoricValues.create(result,name);
	}

	public ArimaParams getParams() {
		return params;
	}

	public void setParams(ArimaParams params) {
		this.params = params;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


}
