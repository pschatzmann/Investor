package ch.pschatzmann.stocks.integration;

import java.time.ZonedDateTime;

import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.HighPriceIndicator;
import org.ta4j.core.indicators.helpers.LowPriceIndicator;
import org.ta4j.core.indicators.helpers.OpenPriceIndicator;
import org.ta4j.core.num.Num;

import ch.pschatzmann.stocks.Context;
import ch.pschatzmann.stocks.ta4j.indicator.VolumeIndicator;

/**
 * Exponentially smoothed time series
 * 
 * @author pschatzmann
 *
 */

public class StockTimeSeriesEMA extends BaseBarSeries {
	private static final long serialVersionUID = 1L;

	private StockTimeSeriesEMA(BarSeries series, int periods) {
		super(series.getName() + "-EMA-" + periods, Context.getNumberImplementation());

		if (periods > 0) {
			OpenPriceIndicator open = new OpenPriceIndicator(series);
			ClosePriceIndicator close = new ClosePriceIndicator(series);
			HighPriceIndicator max = new HighPriceIndicator(series);
			LowPriceIndicator min = new LowPriceIndicator(series);
			VolumeIndicator vol = new VolumeIndicator(series);
			EMAIndicator openEmea = new EMAIndicator(open, periods);
			EMAIndicator closeEmea = new EMAIndicator(close, periods);
			EMAIndicator minEmea = new EMAIndicator(min, periods);
			EMAIndicator maxEmea = new EMAIndicator(max, periods);
			EMAIndicator volEmea = new EMAIndicator(vol, periods);

			for (int j = 0; j < close.getBarSeries().getBarCount(); j++) {
				ZonedDateTime time = close.getBarSeries().getBar(j).getEndTime();
				Num value = closeEmea.getValue(j);
				if (!value.isNaN() && value.doubleValue() >= 0.0) {
					StockBar bar = new StockBar(time, openEmea.getValue(j), maxEmea.getValue(j), minEmea.getValue(j),
							closeEmea.getValue(j), volEmea.getValue(j));
					this.addBar(bar);
				}
			}
		} else {
			series.getBarData().forEach(bar -> this.addBar(bar));
		}

	}

	public static BarSeries create(BarSeries series, int periods) {
		return new StockTimeSeriesEMA(series, periods);
	}
	
	@Override
	public String toString() {
		return this.getName();
	}
}
