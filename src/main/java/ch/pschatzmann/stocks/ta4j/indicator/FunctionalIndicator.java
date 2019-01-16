package ch.pschatzmann.stocks.ta4j.indicator;

import java.io.Serializable;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;
import java.util.function.ToDoubleBiFunction;
import java.util.function.ToDoubleFunction;

import org.ta4j.core.Indicator;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

/**
 * Compares the values of indicator 1 with indicator 2. If the 2 > 1 we return
 * 1. Otherwise we return 0. This will be used as input into some machine
 * learning algorithms.
 * 
 * @author pschatzmann
 *
 */
public class FunctionalIndicator extends CachedIndicator<Num> implements Serializable, IIndicator<Num>  {
	private static final long serialVersionUID = 1L;
	private Indicator<Num> i1,i2;
	private DoubleBinaryOperator twoIndicatorsFunction;
	private DoubleUnaryOperator oneIndicatorFunction;
	private Double defaultErrorValue = Double.NaN;
	
	/**
	 * Calculates the indicator from two indicators
	 * @param i1
	 * @param i2
	 * @param f
	 */
	public FunctionalIndicator(Indicator<Num> i1, Indicator<Num> i2, DoubleBinaryOperator f){
		super(i1);
		this.i1 = i1;
		this.i2 = i2;
		this.twoIndicatorsFunction = f;
	}

	/**
	 * Calculates the indicator from one indicator and the actual index position
	 * @param i1
	 * @param function
	 */
	public FunctionalIndicator(Indicator<Num> i1, DoubleUnaryOperator function) {
		this(i1,function, Double.NaN);
	}

	/**
	 * 
	 * @param i1
	 * @param function
	 * @param nan
	 */
	public FunctionalIndicator(Indicator<Num> i1, DoubleUnaryOperator function, Double nan){
		super((TimeSeries)i1.getTimeSeries());
		this.i1 = i1;
		this.i2 = null;
		this.oneIndicatorFunction = function;
		this.defaultErrorValue = nan;
	}
	

	@Override
	protected Num calculate(int index) {
		try {
			Double result;
			if (i1!=null && i2!=null) {
				result = twoIndicatorsFunction.applyAsDouble(value(i1, index), value(i2,index));
			} else {
				result = oneIndicatorFunction.applyAsDouble(value(i1, index));
			}
			return this.numOf(result);
		}catch(Exception ex) {
			return this.numOf(defaultErrorValue);			
		}
	}

	public Double getDefaultErrorValue() {
		return defaultErrorValue;
	}

	public void setDefaultErrorValue(Double defaultErrorValue) {
		this.defaultErrorValue = defaultErrorValue;
	}
	
	protected Double value(Indicator<Num> i, int pos) {
		Num num = i.getValue(pos);
		return num == null ? defaultErrorValue : num.doubleValue();
	}

}
