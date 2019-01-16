package ch.pschatzmann.stocks.ta4j.indicator;

import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;

import org.ta4j.core.Indicator;
import org.ta4j.core.num.Num;


/**
 * Compares the values of indicator 1 with indicator 2. If the 2 > 1 we return
 * 1. Otherwise we return 0. This will be used as input into some machine
 * learning algorithms.
 * 
 * @author pschatzmann
 *
 */
public class FunctionalCategoryIndicator extends FunctionalIndicator implements Category, IIndicator<Num> {
	private static final long serialVersionUID = 1L;
	private boolean isOneHotEncoded;


	public FunctionalCategoryIndicator(Indicator<Num> indicotro1, Indicator<Num> indicator2, DoubleBinaryOperator f) {
		super(indicotro1, indicator2, f);
	}
	
	public FunctionalCategoryIndicator(Indicator<Num> indicator,  DoubleUnaryOperator function) {
		super(indicator, function);
	}

	@Override
	public boolean isOneHotEncoded() {
		return this.isOneHotEncoded;
	}
	
	public void setOneHotEncoded(boolean isOneHotEncoded) {
		this.isOneHotEncoded = isOneHotEncoded;
	}


}
