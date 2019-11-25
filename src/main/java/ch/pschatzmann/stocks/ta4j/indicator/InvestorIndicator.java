package ch.pschatzmann.stocks.ta4j.indicator;

import org.ta4j.core.Indicator;
import org.ta4j.core.BarSeries;
import org.ta4j.core.num.Num;

/**
 * Prevents Null Pointer Exceptions in the indicator calculation. We return a
 * NAN if the value could not be calculated. We also provide the additional functionality
 * via IIndicator
 * 
 * @author pschatzmann
 *
 */
public class InvestorIndicator implements IIndicator<Num> {
	private static final long serialVersionUID = 1L;
	private Indicator<Num> indicator;

	public InvestorIndicator(Indicator<Num> indicator) {
		this.indicator = indicator;
	}

	@Override
	public Num getValue(int index) {
		try {
			return indicator.getValue(index);
		} catch (Exception ex) {
			return numOf(Double.NaN);
		}
	}

	@Override
	public BarSeries getBarSeries() {
		return indicator.getBarSeries();
	}

	@Override
	public Num numOf(Number number) {
		return indicator.numOf(number);
	}

}
