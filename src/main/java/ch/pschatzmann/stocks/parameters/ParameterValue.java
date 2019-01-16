package ch.pschatzmann.stocks.parameters;

import java.io.Serializable;

import ch.pschatzmann.stocks.Context;
import ch.pschatzmann.stocks.utils.Calculations;
import ch.pschatzmann.stocks.utils.Range;

/**
 * Numeric parameter value
 * 
 * @author pschatzmann
 *
 * @param <T>
 */

public class ParameterValue<T extends Number> implements Cloneable, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private T value;
	private T defaultValue;
	private Range<T> range;
	private int decimals;

	public ParameterValue() {
	}
	
	public ParameterValue(T value, T from, T to, int decimals){
		this.value = value;
		this.defaultValue = value;
		this.range = new Range(from, to);
		this.decimals = decimals;
	}
	
	public Number getValue() {
		return value;
	}
	
	public Range<T> getRange() {
		return this.range;
	}
	
	public void setValue(T value) {
		this.value = (T)Calculations.round(value.doubleValue(), decimals);;
	}
	
	public void reset() {
		this.value = defaultValue;
	}
	
	public int decimals() {
		return this.decimals;
	}

	@Override
	public ParameterValue<T> clone() {
		ParameterValue result = new ParameterValue();
		result.value = value;
		result.defaultValue = defaultValue;
		result.decimals = decimals;
		if (range!=null) {
			result.range = new Range(range.getMin(), range.getMax());
		}
		return result;		
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(Context.format(value));
		if (range!=null) {
			sb.append(" ");
			sb.append(range);
			sb.append("/ ");
			sb.append(decimals);
			sb.append(" digits");
		}
		return sb.toString();
	}

	public void setRange(Range range) {
		this.range = range;		
	}
	
	public Number random() {
		return Calculations.round(this.range.random(), this.decimals);
	}
	
}
