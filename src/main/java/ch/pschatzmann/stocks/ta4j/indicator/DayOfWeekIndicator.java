package ch.pschatzmann.stocks.ta4j.indicator;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.ta4j.core.Indicator;
import org.ta4j.core.BarSeries;
import org.ta4j.core.num.Num;

import ch.pschatzmann.stocks.Context;

/**
 * Returns the Week Day from 1 to 7
 * 
 * @author pschatzmann
 *
 */
public class DayOfWeekIndicator implements Indicator<Num>, Name, IIndicator<Num> {
	private static final long serialVersionUID = 1L;
	private Indicator indicator;

	public DayOfWeekIndicator(Indicator indicator) {
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
		return numOf(ldt.getDayOfWeek().getValue());
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
