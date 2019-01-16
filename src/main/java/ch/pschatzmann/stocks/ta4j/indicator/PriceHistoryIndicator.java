package ch.pschatzmann.stocks.ta4j.indicator;

import java.io.Serializable;

import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

/**
 * Returns the close price for the future n (or past n) periods
 * 
 * @author pschatzmann
 *
 */
public class PriceHistoryIndicator extends CachedIndicator<Num> implements IIndicator<Num> {
	private static final long serialVersionUID = 1L;
	private int periods;

	public PriceHistoryIndicator(TimeSeries series, int periodsOffset) {
		super(series);
		this.periods = periodsOffset;
	}

	@Override
	protected Num calculate(int index) {
		return this.getTimeSeries().getBar(Math.max(0, index + periods)).getClosePrice();
	}

}
