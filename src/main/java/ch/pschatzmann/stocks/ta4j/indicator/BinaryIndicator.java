package ch.pschatzmann.stocks.ta4j.indicator;

import org.ta4j.core.Indicator;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.num.Num;

import ch.pschatzmann.stocks.Context;

/**
 * Returns 1 if the indicator exeeds the indicated limit otherwise the result is
 * 0 or the indicated false value (e.g. -1)
 * 
 * @author pschatzmann
 *
 */

public class BinaryIndicator implements IIndicator<Num>, Category {
	private Indicator<Num> indicator;
	private double limit;
	private double falseValue = 0;
	private double trueValue = 1;
	private boolean oneHotEncoded = false;

	public BinaryIndicator(Indicator<Num> indicator, double limit) {
		this(indicator, limit, 0.0 , 1.0);
	}

	public BinaryIndicator(Indicator<Num> indicator, double limit, double falseValue) {
		this(indicator, limit, falseValue , 1.0);
	}

	public BinaryIndicator(Indicator<Num> indicator, double limit, double falseValue, double trueValue) {
		this.indicator = indicator;
		this.limit = limit;
		this.trueValue = trueValue;
		this.falseValue = falseValue;
	}

	@Override
	public Num getValue(int index) {
		return numOf(indicator.getValue(index).doubleValue()>limit ? this.trueValue : this.falseValue);
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
	public boolean isOneHotEncoded() {
		return oneHotEncoded;
	}

	public void setOneHotEncoded(boolean oneHotEncoded) {
		this.oneHotEncoded = oneHotEncoded;
	}

}