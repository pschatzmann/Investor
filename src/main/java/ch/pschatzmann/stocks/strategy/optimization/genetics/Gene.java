package ch.pschatzmann.stocks.strategy.optimization.genetics;

import java.io.Serializable;

import ch.pschatzmann.stocks.utils.Range;

public class Gene implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String name;
	double value;
	Range<Number> range;
	
	Gene(String name, Range<Number> range, double value) {
		this.name = name;
		this.range = range;
		this.value = value;
	}

	Gene(String name, Range<Number> range) {
		this.range = range;
		this.name = name;
		this.value = random(range.getMin().doubleValue(), range.getMax().doubleValue());
	}


	public Range<Number> getRange() {
		return this.range;
	}
	
	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public static double random(double min, double max) {
		double r = Math.random();
		if (r < 0.5) {
			return ((1 - Math.random()) * (max - min) + min);
		}
		return (Math.random() * (max - min) + min);
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(value);
		return sb.toString();
	}

	public String getName() {
		return this.name;
	}

}
