package ch.pschatzmann.stocks.ta4j.indicator;

import org.ta4j.core.Indicator;
import org.ta4j.core.BarSeries;
import org.ta4j.core.num.Num;

import ch.pschatzmann.stocks.Context;


/**
 * Determines the maximum value in period range. If the period is negative it
 * points to the past, if it is positive it is in the future.
 * 
 * @author pschatzmann
 *
 * @param <Decimal>
 */
public class MaxInPeriodIndicator implements IIndicator<Num>, Name {
	private static final long serialVersionUID = 1L;
	private Indicator<Num> indicator;
	private int period;
	private String name;

	public MaxInPeriodIndicator(Indicator i, int period) {
		this.indicator = i;
		this.period = period;
		this.name = "MaxInPeriodIndicator";
		if (i instanceof Name) {
			name = ((Name)i).getName()+"-MinMaxScaled";
		}
	}

	@Override
	public  Num getValue(int index) {
		int start = period < 0 ? index+period : period;
		int end = period < 0 ? index :   index + period;
		Double max = indicator.getValue(index).doubleValue();
		for (int j=start;j<end;j++) {
			try {
				max = Math.max(max,  indicator.getValue(j).doubleValue());
			} catch(Exception ex) {
				max = Double.NaN;
			}
		}
		
		return numOf(max);
	}

	@Override
	public BarSeries getBarSeries() {
		return indicator.getBarSeries();
	}

	@Override
	public Num numOf(Number number) {
		return Context.number(number);
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

}
