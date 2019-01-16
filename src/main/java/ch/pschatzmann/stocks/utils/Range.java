package ch.pschatzmann.stocks.utils;

import java.io.Serializable;
import java.util.Random;

import ch.pschatzmann.stocks.Context;

public class Range<N extends Number> implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Random rnd = new Random();
	private N min;
	private N max;

	public Range() {	
	}
	
	public Range(N min, N max) {
		this.min = min;
		this.max = max;
	}

	public N getMin() {
		return min;
	}

	public void setMin(N min) {
		this.min = min;
	}

	public N getMax() {
		return max;
	}

	public void setMax(N max) {
		this.max = max;
	}

	public Double random() {
		Random r = new Random();
		double randomValue = min.doubleValue() + (max.doubleValue() - min.doubleValue()) * r.nextDouble();
		return randomValue;
	}
	
	public Double avg() {
		return new Double((max.doubleValue() + min.doubleValue()) / 2.0);
	}
		
	public Double randomDiff() {
		return  new Double(random().doubleValue()-this.avg().doubleValue());
	}

	public Double randomDiff(double scale) {
		return (this.avg().doubleValue() - random().doubleValue()) * scale;
	}

	public Double diff() {
		return this.max.doubleValue() - this.min.doubleValue();
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		sb.append(Context.format(min));
		sb.append("-");
		sb.append(Context.format(max));
		sb.append("] ");
		return sb.toString();
	}

}
