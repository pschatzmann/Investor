package ch.pschatzmann.stocks.ta4j.indicator;

import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

/**
 * Returns the volume directly from the tick
 * 
 * @author pschatzmann
 *
 */
public class VolumeIndicator extends CachedIndicator<Num> implements IIndicator<Num> {
	private static final long serialVersionUID = 1L;
	private BarSeries series;

	public VolumeIndicator(BarSeries series) {
		super(series);
		this.series = series;
	}

	@Override
	protected Num calculate(int index) {
		return series.getBar(index).getVolume();
	}
}