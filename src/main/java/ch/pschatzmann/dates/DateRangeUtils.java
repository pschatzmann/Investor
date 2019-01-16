package ch.pschatzmann.dates;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import ch.pschatzmann.stocks.errors.DateException;

public class DateRangeUtils implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static MonthlyDateRangeSource mds = new MonthlyDateRangeSource();

	/**
	 * Returns a collection of dates up to the end date starting at the inidicated
	 * number of days from the end date
	 *
	 * @param endDate
	 *            End date
	 * @param days
	 *            number of days in the result collection
	 * @return Collection of dates
	 */
	public static Collection<Date> getDates(Date endDate, int days) {
		Collection<Date> result = new ArrayList<>();
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		c.set(Calendar.HOUR, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		c.add(Calendar.DATE, -days);

		for (int j = 0; j < days; j++) {
			c.add(Calendar.DAY_OF_YEAR, 1);
			result.add(c.getTime());
		}
		return result;
	}

	public static DateRange getDateRange(List<DateRange> dates, Date date, int offset) {
		for (int i = 0; i < dates.size(); i++) {
			if (dates.get(i).isValid(date)) {
				int pos = i + offset;
				if (pos < 0) {
					return null;
				}
				return dates.get(pos);
			}
		}
		return null;
	}

	public static DateRange getDateRange(Date date, int offset) throws DateException {
		return getDateRange(getMonthlyDateRanges(), date, offset);
	}

	/**
	 * Returns a date range for each month up to today starting from 2007-01-01
	 * @return
	 * @throws DateException
	 */
	public static List<DateRange> getMonthlyDateRanges() throws DateException {
		return mds.getDates();
	}



}
