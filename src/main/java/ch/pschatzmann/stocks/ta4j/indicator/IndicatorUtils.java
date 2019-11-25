package ch.pschatzmann.stocks.ta4j.indicator;

import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.BarSeries;

public class IndicatorUtils {

	public static BarSeries getTimeSeries(BarSeries ts, int start, int end) {
		return new BaseBarSeries(ts.getName(), ts.getBarData().subList(start, end));
	}
	
	public static String getName(Indicator indicator) {
		return (indicator instanceof Name) ? ((Name)indicator).getName() : indicator.getClass().getSimpleName();
	}
}
