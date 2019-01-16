package ch.pschatzmann.stocks.forecasting;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.ta4j.core.Indicator;
import org.ta4j.core.num.Num;

import ch.pschatzmann.dates.CalendarUtils;
import ch.pschatzmann.stocks.accounting.HistoricValue;
import ch.pschatzmann.stocks.accounting.IHistoricValue;
import ch.pschatzmann.stocks.integration.HistoricValues;
import ch.pschatzmann.stocks.ta4j.indicator.IndicatorFromData;

/**
 * Generates just a random walk with a random drift
 * 
 * @author pschatzmann
 *
 */
public class RandomForecast extends BaseForecast implements IForecast {
	private static final long serialVersionUID = 1L;
	private java.util.Random r = new java.util.Random();
	private double variance = r.nextDouble();
	private double start = 10.0 +  Math.random()* 91;
	private double mean = 0;
	private IDrift drift = new RandomDrift();

	public RandomForecast(Date startDate) {
		super(startDate);
		this.setName("RandomForecast");
	}

	public RandomForecast(Date startDate, Double startRate, Double variance, IDrift drift) {
		super(startDate);
		this.setName("RandomForecast");
		this.start = startRate;
		this.variance = variance;
		this.drift = drift;
	}

	@Override
	public HistoricValues forecast(int numberOfForecasts) throws Exception {
		Date date = this.getStartOfForecastDay();
		List<IHistoricValue> result = new ArrayList();
		double value = start;
		for (int j = 0; j < numberOfForecasts; j++) {
			result.add(new HistoricValue(date, value));
			value = value + noise() + drift();
			date = CalendarUtils.nextWorkDay(date);
		}
		return HistoricValues.create(result, this.getName());
	}
	
	public double noise() {
		return r.nextGaussian() * Math.sqrt(variance) + mean;
	}
	
	public double drift() {
		return this.drift == null ? 0.0 : drift.drift();
	}

}
