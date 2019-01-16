package ch.pschatzmann.stocks.ta4j.indicator;

import org.ta4j.core.BaseTimeSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.TimeSeries;

public class IndicatorUtils {

	public static TimeSeries getTimeSeries(TimeSeries ts, int start, int end) {
		return new BaseTimeSeries(ts.getName(), ts.getBarData().subList(start, end));
	}
	
	public static String getName(Indicator indicator) {
		return (indicator instanceof Name) ? ((Name)indicator).getName() : indicator.getClass().getSimpleName();
	}
}
