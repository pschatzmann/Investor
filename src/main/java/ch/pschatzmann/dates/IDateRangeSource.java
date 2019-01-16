package ch.pschatzmann.dates;

import java.util.List;

import ch.pschatzmann.stocks.errors.DateException;

/**
 * Source for a collection of date ranges
 * @author pschatzmann
 *
 */
public interface IDateRangeSource {
	public  List<DateRange> getDates() throws DateException;

}
