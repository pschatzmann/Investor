package ch.pschatzmann.stocks.ta4j.indicator;

import org.ta4j.core.Indicator;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.num.Num;

import ch.pschatzmann.stocks.Context;

/**
 * Splits an indicator into a training and test set: The training set can be
 * accessed by setting head to true; head = false gives the test data set
 * 
 * @author pschatzmann
 *
 */
public class SplitIndicator implements IIndicator<Num>, Name{
	private static final long serialVersionUID = 1L;
	private TimeSeries timeSeries; // (String name, List<Bar> bars)
	private int start;
	private Indicator<Num> indicator;
	private String name;

	public SplitIndicator(Indicator<Num> indicator, double rate, boolean head) {
		this(indicator, (int) (indicator.getTimeSeries().getBarCount() * rate), head);
	}

	public SplitIndicator(Indicator<Num> indicator, int pos, boolean head) {
		this.indicator = indicator;
		timeSeries = getTimeSeries(indicator, pos, head);
		if (indicator instanceof Name) {
			this.name = ((Name)indicator).getName();
		} else {
			this.name = this.getClass().getSimpleName();
		}
	}
	
	protected TimeSeries getTimeSeries(Indicator<Num> indicator, int pos, boolean head) {
		TimeSeries ts = indicator.getTimeSeries();
		int end;
		if (head) {
			start = 0;
			end = Math.min(pos-1, ts.getBarCount()-1);
		} else {
			end = ts.getBarCount() - 1;
			start = Math.min(Math.max(pos, 0), end);
		}
		return IndicatorUtils.getTimeSeries(ts,start, end);
	}

	@Override
	public Num getValue(int index) {
		return indicator.getValue(index + start);
	}

	@Override
	public TimeSeries getTimeSeries() {
		return timeSeries;
	}

	@Override
	public Num numOf(Number number) {
		return Context.number(number);
	}

	@Override
	public String getName() {
		return name;
	}

}
