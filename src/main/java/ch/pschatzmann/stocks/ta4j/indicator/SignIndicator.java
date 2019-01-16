package ch.pschatzmann.stocks.ta4j.indicator;

import org.ta4j.core.Indicator;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.num.Num;

import ch.pschatzmann.stocks.Context;

/**
 * Returns an integer that indicates the sign of a number.
 * 
 * Returns 1 if the indicator is > 0; 
 * Returns 0 if the indicator is = 0; 
 * Returns -1 if the indicator is < 0; 
 * 
 * @author pschatzmann
 *
 */

public class SignIndicator implements IIndicator<Num>, Category {
	private static final long serialVersionUID = 1L;
	private Indicator<Num> indicator;
	private boolean oneHotEncoded = false;

	
	public SignIndicator(Indicator<Num> indicator) {
		this.indicator = indicator;
	}

	@Override
	public Num getValue(int index) {
		double result = 0;
		if (indicator.getValue(index).doubleValue()>0) {
			result = 1.0;
		} else  if (indicator.getValue(index).doubleValue()<0) {
			result = -1.0;
		}
			
		return numOf(result);
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
	
	/**
	 * Translates the numerical value into positive, negative or neutral
	 * @param val
	 * @return
	 */
	public static String getLabel(double val) {
		if (val > 0.0) 
			return "positive";
		else if (val < 0.0) 
			return "negative";
		else 
			return "neutral";			
	}
	
	/**
	 * Translates the numerical value into bullish, bearish or neutral
	 * 
	 * @param val
	 * @return
	 */
	public static String getMarketLabel(double val) {
		if (val > 0.0) 
			return "bullish";
		else if (val < 0.0) 
			return "bearish";
		else 
			return "neutral";			
		
	}

}