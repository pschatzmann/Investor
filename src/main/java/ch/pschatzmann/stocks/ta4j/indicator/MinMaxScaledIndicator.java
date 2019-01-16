package ch.pschatzmann.stocks.ta4j.indicator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.ta4j.core.Indicator;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.num.Num;

import ch.pschatzmann.stocks.Context;
import ch.pschatzmann.stocks.utils.IScaler;
import ch.pschatzmann.stocks.utils.MinMaxScaler;

/**
 * Indicator where the values are scaled between 0.0 and 1.0
 * 
 * @author pschatzmann
 *
 */

public class MinMaxScaledIndicator implements IIndicator<Num> , Name {
	private static final long serialVersionUID = 1L;
	private Indicator<Num> indicator;
	private String name = this.getClass().getSimpleName();
	private IScaler scaler;
	
	public MinMaxScaledIndicator(Indicator<Num> indicator) {
		this(indicator,-1.0, +1.0);
	}
	
	public MinMaxScaledIndicator(Indicator<Num> indicator, IScaler scaler) {
		this.indicator = indicator;
		this.scaler = scaler;
		this.name = IndicatorUtils.getName(indicator)+"-MinMaxScaled";
	}

	public MinMaxScaledIndicator(Indicator<Num> indicator, Double min, Double max) {
		this.indicator = indicator;
		this.scaler = new MinMaxScaler(min, max);
		this.name = IndicatorUtils.getName(indicator)+"-MinMaxScaled";
		List<Double> values = IntStream.range(0, indicator.getTimeSeries().getBarCount())
				.mapToObj(pos -> indicator.getValue(pos).doubleValue())
				.collect(Collectors.toList());	
		scaler.setValues(values);
	}

	@Override
	public Num getValue(int index) {
		return numOf(scaler.normalizeValue(indicator.getValue(index).doubleValue()));
	}

	@Override
	public TimeSeries getTimeSeries() {
		return this.indicator.getTimeSeries();
	}
	
	@Override
	public Num numOf(Number value) {
		return Context.number(value);
	}


	public Double normalizeValue(Double value) {
		return scaler.normalizeValue(value);
	}


	public Double denormalizeValue(Double value) {
		return scaler.denormalizeValue(value);
	}

	@Override
	public String getName() {
		return this.name;
	}
	
	public List<Date> getDates() {
		return Context.getDates(this);
	}
	
	public List<Number> getValues() {
		List<Number> result = new ArrayList();
		for (int j=0;j<indicator.getTimeSeries().getBarCount();j++) {
			result.add(this.getValue(j).doubleValue());
		}
		return result;
	}
	
	public IScaler getScaler() {
		return this.scaler;
	}

}
