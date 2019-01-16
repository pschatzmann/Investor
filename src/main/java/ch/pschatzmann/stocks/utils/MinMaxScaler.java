package ch.pschatzmann.stocks.utils;

import java.util.Collection;

/**
 * Scales the values between a defined min and max
 * 
 * @author pschatzmann
 *
 */
public class MinMaxScaler implements IScaler  {
	private double actualMin = Double.MAX_VALUE;
	private double actualMax = -Double.MAX_VALUE;
	private double min = -1.0;
	private double max = +1.0;
	private double actualDiff=0.0;
	private double toBeDiff;
	private double fact;

	public MinMaxScaler(){
	}

	
	public MinMaxScaler(Double min, Double max){
		this.min = min;
		this.max = max;	
	}
	
	/* (non-Javadoc)
	 * @see ch.pschatzmann.stocks.ta4j.indicator.IScaler#setValues(java.util.Collection)
	 */
	@Override
	public void setValues(Collection<Double> values){		
		values.forEach(val -> updateMinMax(val));	
		this.actualDiff  = actualMax - actualMin;
		this.toBeDiff  = max - min;
		this.fact =  toBeDiff / actualDiff; //(MAX-MIN)/(max-min)
	}

	
	protected void updateMinMax(Double num) {
		actualMin = Math.min(actualMin, num.doubleValue());
		actualMax = Math.max(actualMax, num.doubleValue());
	}

	/* (non-Javadoc)
	 * @see ch.pschatzmann.stocks.ta4j.indicator.IScaler#normalizeValue(java.lang.Double)
	 */
	@Override
	public Double normalizeValue(Double value) {
		//  x  * fact + (MAX - (fact * max))
		return (value * fact + (max - (fact * actualMax)));
	}

	/* (non-Javadoc)
	 * @see ch.pschatzmann.stocks.ta4j.indicator.IScaler#denormalizeValue(java.lang.Double)
	 */
	@Override
	public Double denormalizeValue(Double value) {
		// (sx - (MAX - (fact * max)))/fact 
		return  (value - (max - (fact * actualMax)))/fact; 
	}


	public double getActualMin() {
		return actualMin;
	}


	public void setActualMin(double actualMin) {
		this.actualMin = actualMin;
	}


	public double getActualMax() {
		return actualMax;
	}


	public void setActualMax(double actualMax) {
		this.actualMax = actualMax;
	}


	public double getMin() {
		return min;
	}


	public void setMin(double min) {
		this.min = min;
	}


	public double getMax() {
		return max;
	}

	public void setMax(double max) {
		this.max = max;
	}
	
}
