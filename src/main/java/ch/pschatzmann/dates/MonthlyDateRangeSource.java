package ch.pschatzmann.dates;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ch.pschatzmann.stocks.Context;
import ch.pschatzmann.stocks.errors.DateException;

/**
 * Calculates a list of monthly date ranges which start on the first calendar day of the start date
 * and ends with the month period which covers the end date
 */


public class MonthlyDateRangeSource implements IDateRangeSource, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Date start; 
	private Date end;
	
	public MonthlyDateRangeSource() {
		this.start = Context.date("2007-01-01");
		this.end = new Date();
	}

	public MonthlyDateRangeSource(Date start) {
		this.start = start;
		this.end = new Date();
	}
	
	public MonthlyDateRangeSource(Date start, Date end) {
		this.start = start;
		this.end = end;
	}

	@Override
	public List<DateRange> getDates() throws DateException {
		List<DateRange> result = new ArrayList();
		
		Calendar startCalendar = Calendar.getInstance();
		startCalendar.setTime(start);
		startCalendar.set(Calendar.HOUR, 0);
		startCalendar.set(Calendar.MINUTE, 0);
		startCalendar.set(Calendar.SECOND, 0);
		startCalendar.set(Calendar.MILLISECOND, 0);
		startCalendar.set(Calendar.DAY_OF_MONTH, 1);
		
		Calendar nextStart = Calendar.getInstance();
		nextStart.setTime(startCalendar.getTime());
		nextStart.add(Calendar.MONTH, 1);
		Calendar endCalendar = Calendar.getInstance();
		endCalendar.setTime(nextStart.getTime());
		endCalendar.add(Calendar.DATE, -1);

		DateRange range = new DateRange(startCalendar.getTime(), endCalendar.getTime());
		
		while (range.getStart().before(end)) {
			result.add(range);
			startCalendar = nextStart;
			nextStart = Calendar.getInstance();
			nextStart.setTime(startCalendar.getTime());
			nextStart.add(Calendar.MONTH, 1);	
			endCalendar = Calendar.getInstance();
			endCalendar.setTime(nextStart.getTime());
			endCalendar.add(Calendar.DATE, -1);
			
			range = new DateRange(startCalendar.getTime(), endCalendar.getTime());
		}		
		return result;
	}
}
