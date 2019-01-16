package ch.pschatzmann.stocks.ta4j.indicator;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

/**
 * Calculates the percent difference to the prior period or to the base
 * indicator
 * 
 * @author pschatzmann
 *
 */
public class PercentChangeIndicator extends CachedIndicator<Num> implements IIndicator<Num> {
	private static final long serialVersionUID = 1L;
	private Indicator<Num> prices;
	private Indicator<Num> prices1;
	private Num na = null;

	public PercentChangeIndicator(Indicator<Num> prices) {
		this(prices, prices.numOf(Double.NaN));
	}

	public PercentChangeIndicator(Indicator<Num> originalValues, Indicator<Num> newValues) {
		super(newValues.getTimeSeries());
		this.prices = originalValues;
		this.prices1 = newValues;
		this.na = prices.numOf(Double.NaN);
	}

	public PercentChangeIndicator(Indicator<Num> prices, Num defaultNA) {
		super(prices.getTimeSeries());
		this.prices = prices;
		this.na = defaultNA;
	}

	@Override
	protected Num calculate(int index) {
		try {
			if (prices1 == null) {
				if (index >=1  && index <= prices.getTimeSeries().getBarCount()) {
					// calculate the difference to the prior period
					Double value0 = prices.getValue(index - 1).doubleValue();
					Double value = prices.getValue(index).doubleValue();
					return numOf(((value.doubleValue() - value0.doubleValue()) / value0.doubleValue()));
				}
			} else {
				// Calculate the difference to the base indicator
				Double value0 = prices.getValue(index).doubleValue();
				Double value = prices1.getValue(index).doubleValue();
				return numOf(((value.doubleValue() - value0.doubleValue()) / value0.doubleValue()));
			}
		} catch (Exception ex) {}
		
		return this.na;
	}

}
