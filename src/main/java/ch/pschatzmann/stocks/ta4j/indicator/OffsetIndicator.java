package ch.pschatzmann.stocks.ta4j.indicator;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.ta4j.core.Bar;
import org.ta4j.core.Indicator;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

import ch.pschatzmann.dates.CalendarUtils;
import ch.pschatzmann.stocks.Context;
import ch.pschatzmann.stocks.integration.StockBar;
import ch.pschatzmann.stocks.integration.StockTimeSeries;

/**
 * Returns the indicator value for the future n (or past n) period
 * 
 * @author pschatzmann
 *
 */
public class OffsetIndicator extends CachedIndicator<Num> implements IIndicator<Num>, Name {
	private static final long serialVersionUID = 1L;
	private int periods;
	private Indicator<Num> indicator;
	private Num na;
	
	public OffsetIndicator(Indicator<Num> indicator, int periodsOffset) {
		this(indicator, periodsOffset, indicator.numOf(Double.NaN));
	}
	
	public OffsetIndicator(Indicator<Num> indicator, int periodsOffset, Num defaultNA) {
		super(setupTimeSeries(indicator.getTimeSeries(), periodsOffset));
		this.periods = periodsOffset;
		this.indicator = indicator;
		this.na = defaultNA;
	}

	protected static TimeSeries setupTimeSeries(TimeSeries timeSeries, int periodsOffset) {
		List<Bar> bars = new ArrayList(timeSeries.getBarData());
		Date date = periodsOffset<0 ? Context.date(bars.get(bars.size()-1).getEndTime()):Context.date(bars.get(0).getEndTime());
		if (periodsOffset<0) {
			for (int j=0;j<Math.abs(periodsOffset);j++) {
				 date = CalendarUtils.nextWorkDay(date);
				 bars.add(new StockBar(date));
			}
		} else {
			for (int j=0;j<Math.abs(periodsOffset);j++) {
				 date = CalendarUtils.priorWorkDay(date);
				 bars.add(0, new StockBar(date));
			}			
		}
		return new StockTimeSeries(bars);
	}


	@Override
	protected Num calculate(int index) {
		int pos = index+periods;
		if (pos>=0 && pos < this.indicator.getTimeSeries().getBarCount()) {
			return this.indicator.getValue(pos);				
		}
		return na;
	}
	
	public int getOffset() {
		return this.periods;
	}


	@Override
	public String getName() {
		StringBuffer sb = new StringBuffer();
		sb.append(IndicatorUtils.getName(indicator));
		if (periods>0) {
			sb.append("+");
		}
		sb.append(Integer.toString(periods));
		return sb.toString();
	}
	
	@Override
	public String toString() {
		return getName();
	}

}
