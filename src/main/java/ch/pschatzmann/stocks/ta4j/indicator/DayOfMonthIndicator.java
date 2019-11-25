package ch.pschatzmann.stocks.ta4j.indicator;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.ta4j.core.Indicator;
import org.ta4j.core.BarSeries;
import org.ta4j.core.num.Num;

import ch.pschatzmann.stocks.Context;

/**
 * Returns the Day of the month from 1 to 31
 * 
 * @author pschatzmann
 *
 */
public class DayOfMonthIndicator implements Indicator<Num>, Name, IIndicator<Num> {
	private static final long serialVersionUID = 1L;
	private Indicator indicator;

	public DayOfMonthIndicator(Indicator indicator) {
		this.indicator = indicator;
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public Num getValue(int index) {
		Instant inst = getBarSeries().getBar(index).getBeginTime().toInstant();
		LocalDateTime ldt = LocalDateTime.ofInstant(inst, ZoneId.systemDefault());
		return numOf(ldt.getDayOfMonth());
	}

	@Override
	public BarSeries getBarSeries() {
		return indicator.getBarSeries();
	}

	@Override
	public Num numOf(Number number) {
		return Context.number(number);
	}

}
