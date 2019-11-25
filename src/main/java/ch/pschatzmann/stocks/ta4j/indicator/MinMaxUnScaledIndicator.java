package ch.pschatzmann.stocks.ta4j.indicator;

import java.util.Date;
import java.util.List;

import org.ta4j.core.num.Num;

import ch.pschatzmann.stocks.utils.IScaler;
import ch.pschatzmann.stocks.utils.MinMaxScaler;

/**
 * Indicator where the scaled values between 0.0 and 1.0 are un-scaled again. We
 * also provide some additional functionality to set the timeSeries-
 * 
 * @author pschatzmann
 *
 */

public class MinMaxUnScaledIndicator extends IndicatorFromData implements IIndicator<Num> {
	private static final long serialVersionUID = 1L;
	private IScaler scaler;

	public MinMaxUnScaledIndicator(String name, List<Date> dates, List<Number> values,double min, double max) {
		super(name,dates,values);
		MinMaxScaler scaler = new MinMaxScaler();
		scaler.setActualMax(max);
		scaler.setActualMin(min);
		this.scaler = scaler;
	}

	public MinMaxUnScaledIndicator(String name,  List<Date> dates, List<Number> values, MinMaxScaledIndicator ind) {
		super(name.replace("-MinMaxScaled", ""), dates, values);
		this.scaler = ind.getScaler();
	}

	public MinMaxUnScaledIndicator(String name,  List<Date> dates, List<Number> values, IScaler scaler) {
		super(name.replace("-MinMaxScaled", ""), dates, values);
		this.scaler = scaler;
	}

	public MinMaxUnScaledIndicator(MinMaxScaledIndicator ind) {
		super(ind.getName().replace("-MinMaxScaled",""),ind.getDates(),ind.getValues());
		this.scaler = ind.getScaler();
	}
	
	@Override
	public Num getValue(int index) {
		return numOf(denormalizeValue(super.getValue(index).doubleValue()));
	}

	public Double denormalizeValue(Double value) {
		return this.scaler.denormalizeValue(value);
	}

}
