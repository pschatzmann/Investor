package ch.pschatzmann.stocks.ta4j.indicator;

import java.util.Date;
import java.util.List;

import org.ta4j.core.Indicator;
import org.ta4j.core.num.Num;

import ch.pschatzmann.stocks.integration.HistoricValues;

/**
 * Interface for Indicators which provide an easy access to the values and dates
 * 
 * @author pschatzmann
 *
 * @param <E>
 */
public interface IIndicator<E> extends Indicator<E> {

	default HistoricValues toHistoricValues() {
		return HistoricValues.create((Indicator<Num>) this);
	}

	default List<Double> values() {
		return toHistoricValues().getValues();
	}

	default List<Date> dates() {
		return toHistoricValues().getDates();
	}

}
