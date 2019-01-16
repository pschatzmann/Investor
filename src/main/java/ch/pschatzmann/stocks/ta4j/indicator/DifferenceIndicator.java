package ch.pschatzmann.stocks.ta4j.indicator;

import org.ta4j.core.Indicator;
import org.ta4j.core.num.Num;

import ch.pschatzmann.stocks.Context;

public class DifferenceIndicator extends org.ta4j.core.indicators.helpers.DifferenceIndicator {
	private static final long serialVersionUID = 1L;

	public DifferenceIndicator(Indicator<org.ta4j.core.num.Num> first, Indicator<org.ta4j.core.num.Num> second) {
		super(first, second);
	}
	
    @Override
    protected Num calculate(int index) {
    	try {
    		return super.calculate(index);
    	} catch(Exception ex) {
    		return Context.number(Double.NaN);
    	}
    }


}
