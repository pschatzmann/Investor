package ch.pschatzmann.stocks.forecasting;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.ta4j.core.Indicator;
import org.ta4j.core.num.Num;

import ch.pschatzmann.dates.CalendarUtils;
import ch.pschatzmann.stocks.accounting.HistoricValue;
import ch.pschatzmann.stocks.accounting.IHistoricValue;
import ch.pschatzmann.stocks.integration.HistoricValues;
import ch.pschatzmann.stocks.ta4j.indicator.IndicatorFromData;
import ch.pschatzmann.stocks.ta4j.indicator.IndicatorUtils;
import ch.pschatzmann.stocks.utils.Calculations;

/**
 * We use a simple regression on the current stock data
 * 
 * @author pschatzmann
 *
 */
public class SimpleRegressionForecast extends BaseForecast  {
	private static final long serialVersionUID = 1L;
	private SimpleRegression regression = new SimpleRegression();
		
	public SimpleRegressionForecast(HistoricValues values) {
		super(values);
		setName(values.getName()+"-SimpleRegressionForecast");
		int x = 0;
		for (IHistoricValue value : this.getValues().list()) {
			regression.addData(x++, value.getValue());
		}
	}

	@Override
	public HistoricValues forecast(int numberOfForecasts) throws Exception {
		int x = 0;
		List<IHistoricValue> result = new ArrayList();
		for (IHistoricValue value : this.getValues().list()) {
			result.add(new HistoricValue(value.getDate(), regression.predict(x)));
			x++;
		}
		Date date = Calculations.lastDate(this.getValues().list(), new Date());
		for (int j=0;j<numberOfForecasts;j++) {
			date = CalendarUtils.nextWorkDay(date);
			result.add(new HistoricValue(date, regression.predict(x)));
			x++;			
		}
		
		return HistoricValues.create(result, this.getName());
	}

	
	public SimpleRegression getSimpleRegression() {
		return regression;
	}

}
