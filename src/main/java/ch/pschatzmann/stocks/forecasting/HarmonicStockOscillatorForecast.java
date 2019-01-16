package ch.pschatzmann.stocks.forecasting;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.OptionalDouble;

import org.apache.commons.math3.analysis.function.HarmonicOscillator;
import org.apache.commons.math3.fitting.HarmonicCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.ta4j.core.Indicator;
import org.ta4j.core.num.Num;

import ch.pschatzmann.dates.CalendarUtils;
import ch.pschatzmann.stocks.IStockData;
import ch.pschatzmann.stocks.IStockID;
import ch.pschatzmann.stocks.IStockRecord;
import ch.pschatzmann.stocks.StockData;
import ch.pschatzmann.stocks.StockID;
import ch.pschatzmann.stocks.StockRecord;
import ch.pschatzmann.stocks.accounting.HistoricValue;
import ch.pschatzmann.stocks.accounting.IHistoricValue;
import ch.pschatzmann.stocks.integration.HistoricValues;
import ch.pschatzmann.stocks.ta4j.indicator.IndicatorFromData;
import ch.pschatzmann.stocks.ta4j.indicator.IndicatorUtils;

/**
 * We just generate a Sinusoid with the help of the Apache Commons Math HarmonicOscillator
 * 
 * @author pschatzmann
 *
 */
public class HarmonicStockOscillatorForecast extends BaseForecast {
	private static final long serialVersionUID = 1L;
	private double offset = 15;
	private double amplitude = 10.0;
	private double omega = 0.025;
	private double phase = 0;
	private boolean fit = true;
	private HarmonicOscillator harmonicOscillator;
	
	/**
	 * Default Constructor. 
	 */
	public HarmonicStockOscillatorForecast() {
		super();
		setName("HarmonicStockOscillatorForecast");
	}

	public HarmonicStockOscillatorForecast(HistoricValues values) {
		super(values);
		setName(values.getName()+"-HarmonicStockOscillatorForecast");
	}

	public HarmonicStockOscillatorForecast(Date startDay) {
		super(startDay);
		fit = false;
		setName("HarmonicStockOscillatorForecast");
	}

	/**
	 * Constructor with fixed parameters
	 * @param amplitude
	 * @param omega
	 * @param phase
	 */
	public HarmonicStockOscillatorForecast(Date startDay, double offset, double amplitude, double omega, double phase) {
		super(startDay);
		this.offset = offset;
		this.amplitude = amplitude;
		this.omega = omega;
		this.phase = phase;
		this.fit = false;
		setName("HarmonicStockOscillatorForecast");
	}

	/**
	 * Produce the forecast. The stockData might be null. If the stock data is defined and we 
	 * used the standard constructor, we are fitting the parameters based on the existing data
	 */
	@Override
	public HistoricValues forecast(int numberOfForecasts) throws Exception {
		// fit the current data and re-generate existing stock data based on the new model
		Integer time = 0;
		List<IHistoricValue> resultValues = new ArrayList();
				
		if (!this.getValues().isEmpty() && fit) {
			fit(this.getValues());
			harmonicOscillator = new HarmonicOscillator(amplitude, omega, phase);
			for (IHistoricValue hv: this.getValues().list()) {
				double value =  harmonicOscillator.value((time.doubleValue())) + offset;
				resultValues.add(new HistoricValue(hv.getDate(),value));
				time++;
			}
		} else {
			harmonicOscillator = new HarmonicOscillator(amplitude, omega, phase);
		}
			
		Date date = getStartOfForecastDay();
		// generate future data
		for (int j = 0; j < numberOfForecasts; j++) {
			double value =  harmonicOscillator.value((time.doubleValue() + j)) + offset;
			
			resultValues.add(new HistoricValue(date, value));
			date = CalendarUtils.nextWorkDay(date);
		}
		return HistoricValues.create(resultValues, this.getName());
	}


	/**
	 * Fits the data
	 * 
	 * @param sd
	 */
	protected void fit(HistoricValues hv) {
		final WeightedObservedPoints points = new WeightedObservedPoints();
		Integer day = 0;
		
		//double size = hv.size();
		OptionalDouble avg = hv.getValues().stream().mapToDouble(d -> d).average();
		this.offset = avg.getAsDouble();
		for (Double value : hv.getValues()) {
			double x = day.doubleValue();
			// the more recent the value the more weight it will have
			//double weight = x / size;
			points.add(x, value - offset);
			day++;
		}
		final HarmonicCurveFitter fitter = HarmonicCurveFitter.create();
		final double[] fitted = fitter.fit(points.toList());
		amplitude = fitted[0];
		omega = fitted[1];
		phase = fitted[2];
	}
	

	public boolean isFit() {
		return fit;
	}

	public void setFit(boolean fit) {
		this.fit = fit;
	}
	
	public HarmonicOscillator getHarmonicOscillator() {
		return harmonicOscillator;
	}

}
