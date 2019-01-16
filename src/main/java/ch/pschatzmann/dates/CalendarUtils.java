package ch.pschatzmann.dates;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class CalendarUtils implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Converts the date to a calendar
	 * 
	 * @param date
	 * @return
	 */
	public static Calendar toCalendar(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal;
	}
	
	/**
	 * Checks if the indicated calendar is a public holiday in the US
	 * @param c
	 * @return
	 */
	public static boolean isHoiday(Date date) {
		return isHoliday(toCalendar(date));
	}
	
	
	/**
	 * Checks if the indicated calendar is a public holiday in the US
	 * @param c
	 * @return
	 */
	public static boolean isHoliday(Calendar c) {
		int year = c.get(Calendar.YEAR);
		boolean result = isSameDay(c, NewYearsDayObserved(year))
		|| isSameDay(c,MartinLutherKingObserved(year))
		|| isSameDay(c,PresidentsDayObserved(year))
		|| isSameDay(c,MemorialDayObserved(year))
		|| isSameDay(c,IndependenceDayObserved(year))
		|| isSameDay(c,LaborDayObserved(year))
		|| isSameDay(c,ColumbusDayObserved(year))
		|| isSameDay(c,VeteransDayObserved(year))
		|| isSameDay(c,ThanksgivingObserved(year))
		|| isSameDay(c,ChristmasDayObserved(year));	
		return result;
	}
	
	/**
	 * Checks if the two calendars ore on the same date
	 * @param cal1
	 * @param cal2
	 * @return
	 */
	public static boolean isSameDay(Calendar cal1, Calendar cal2) {
	    if (cal1 == null || cal2 == null)
	        return false;
	    return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA)
	            && cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) 
	            && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
	}
	
	/**
	 * Returns the next working day after the indicated day
	 * @param date
	 * @return
	 */
	public static Date nextWorkDay(Date date) {
		for (int j=1; j<7; j++) {
			Date result = ch.pschatzmann.stocks.Context.getDateWithOffsetDays(date,j);
			Calendar myDate = Calendar.getInstance(); 
			myDate.setTime(result);
			if (isWeekDay(myDate) && !CalendarUtils.isHoliday(myDate)) {
				return result;
			}
		}
		return null;
	}
	
	/**
	 * Returns the prior working day after the indicated day
	 * @param date
	 * @return
	 */
	public static Date priorWorkDay(Date date) {
		for (int j=1; j<7; j++) {
			Date result = ch.pschatzmann.stocks.Context.getDateWithOffsetDays(date,-j);
			Calendar myDate = Calendar.getInstance(); 
			myDate.setTime(result);
			if (isWeekDay(myDate) && !CalendarUtils.isHoliday(myDate)) {
				return result;
			}
		}
		return null;
	}
	
	
	public static boolean isWeekDay(Calendar myDate) {
		int dow = myDate.get (Calendar.DAY_OF_WEEK);
		boolean isWeekday = ((dow >= Calendar.MONDAY) && (dow <= Calendar.FRIDAY));
		return isWeekday;		
	}
	
	public static java.util.Calendar NewYearsDayObserved(int nYear) {
		Calendar cal = new GregorianCalendar(nYear, Calendar.JANUARY, 1);
		switch (cal.get(Calendar.DAY_OF_WEEK)) {
		case Calendar.SATURDAY:
			return (new GregorianCalendar(--nYear, Calendar.DECEMBER, 31));
		case Calendar.SUNDAY:
			return (new GregorianCalendar(nYear, Calendar.JANUARY, 2));
		case Calendar.MONDAY:
		case Calendar.TUESDAY:
		case Calendar.WEDNESDAY:
		case Calendar.THURSDAY:
		case Calendar.FRIDAY:
		default:
			return cal;
		}
	}

	public static java.util.Calendar MartinLutherKingObserved(int nYear) {
		// Third Monday in January
		Calendar cal = new GregorianCalendar(nYear, Calendar.JANUARY, 1);
		switch (cal.get(Calendar.DAY_OF_WEEK)) {
		case Calendar.SUNDAY:
			return (new GregorianCalendar(nYear, Calendar.JANUARY, 16));
		case Calendar.MONDAY:
			return (new GregorianCalendar(nYear, Calendar.JANUARY, 15));
		case Calendar.TUESDAY:
			return (new GregorianCalendar(nYear, Calendar.JANUARY, 21));
		case Calendar.WEDNESDAY:
			return (new GregorianCalendar(nYear, Calendar.JANUARY, 20));
		case Calendar.THURSDAY:
			return (new GregorianCalendar(nYear, Calendar.JANUARY, 19));
		case Calendar.FRIDAY:
			return (new GregorianCalendar(nYear, Calendar.JANUARY, 18));
		default: // Saturday
			return (new GregorianCalendar(nYear, Calendar.JANUARY, 17));
		}
	}

	public static java.util.Calendar PresidentsDayObserved(int nYear) {
		// Third Monday in February
		Calendar cal = new GregorianCalendar(nYear, Calendar.FEBRUARY, 1);
		switch (cal.get(Calendar.DAY_OF_WEEK)) {
		case Calendar.SUNDAY:
			return (new GregorianCalendar(nYear, Calendar.FEBRUARY, 16));
		case Calendar.MONDAY:
			return (new GregorianCalendar(nYear, Calendar.FEBRUARY, 15));
		case Calendar.TUESDAY:
			return (new GregorianCalendar(nYear, Calendar.FEBRUARY, 21));
		case Calendar.WEDNESDAY:
			return (new GregorianCalendar(nYear, Calendar.FEBRUARY, 20));
		case Calendar.THURSDAY:
			return (new GregorianCalendar(nYear, Calendar.FEBRUARY, 19));
		case Calendar.FRIDAY:
			return (new GregorianCalendar(nYear, Calendar.FEBRUARY, 18));
		default: // Saturday
			return (new GregorianCalendar(nYear, Calendar.FEBRUARY, 17));
		}
	}

	public static java.util.Calendar MemorialDayObserved(int nYear) {
		// Last Monday in May
		Calendar cal = new GregorianCalendar(nYear, Calendar.MAY, 1);
		switch (cal.get(Calendar.DAY_OF_WEEK)) {
		case Calendar.SUNDAY:
			return (new GregorianCalendar(nYear, Calendar.MAY, 30));
		case Calendar.MONDAY:
			return (new GregorianCalendar(nYear, Calendar.MAY, 29));
		case Calendar.TUESDAY:
			return (new GregorianCalendar(nYear, Calendar.MAY, 28));
		case Calendar.WEDNESDAY:
			return (new GregorianCalendar(nYear, Calendar.MAY, 27));
		case Calendar.THURSDAY:
			return (new GregorianCalendar(nYear, Calendar.MAY, 26));
		case Calendar.FRIDAY:
			return (new GregorianCalendar(nYear, Calendar.MAY, 25));
		default: // Saturday
			return (new GregorianCalendar(nYear, Calendar.MAY, 31));
		}
	}

	public static java.util.Calendar IndependenceDayObserved(int nYear) {
		Calendar cal = new GregorianCalendar(nYear, Calendar.JULY, 4);
		switch (cal.get(Calendar.DAY_OF_WEEK)) {
		case Calendar.SATURDAY:
			return (new GregorianCalendar(nYear, Calendar.JULY, 3));
		case Calendar.SUNDAY:
			return (new GregorianCalendar(nYear, Calendar.JULY, 5));
		case Calendar.MONDAY:
		case Calendar.TUESDAY:
		case Calendar.WEDNESDAY:
		case Calendar.THURSDAY:
		case Calendar.FRIDAY:
		default:
			return cal;
		}
	}

	public static java.util.Calendar LaborDayObserved(int nYear) {
		// The first Monday in September
		Calendar cal = new GregorianCalendar(nYear, Calendar.SEPTEMBER, 1);
		switch (cal.get(Calendar.DAY_OF_WEEK)) {
		case Calendar.TUESDAY:
			return (new GregorianCalendar(nYear, Calendar.SEPTEMBER, 7));
		case Calendar.WEDNESDAY:
			return (new GregorianCalendar(nYear, Calendar.SEPTEMBER, 6));
		case Calendar.THURSDAY:
			return (new GregorianCalendar(nYear, Calendar.SEPTEMBER, 5));
		case Calendar.FRIDAY:
			return (new GregorianCalendar(nYear, Calendar.SEPTEMBER, 4));
		case Calendar.SATURDAY:
			return (new GregorianCalendar(nYear, Calendar.SEPTEMBER, 3));
		case Calendar.SUNDAY:
			return (new GregorianCalendar(nYear, Calendar.SEPTEMBER, 2));
		case Calendar.MONDAY:
		default:
			return cal;
		}
	}

	public static java.util.Calendar ColumbusDayObserved(int nYear) {
		// Second Monday in October
		Calendar cal = new GregorianCalendar(nYear, Calendar.OCTOBER, 1);
		switch (cal.get(Calendar.DAY_OF_WEEK)) {
		case Calendar.SUNDAY:
			return (new GregorianCalendar(nYear, Calendar.OCTOBER, 9));
		case Calendar.MONDAY:
			return (new GregorianCalendar(nYear, Calendar.OCTOBER, 8));
		case Calendar.TUESDAY:
			return (new GregorianCalendar(nYear, Calendar.OCTOBER, 14));
		case Calendar.WEDNESDAY:
			return (new GregorianCalendar(nYear, Calendar.OCTOBER, 13));
		case Calendar.THURSDAY:
			return (new GregorianCalendar(nYear, Calendar.OCTOBER, 12));
		case Calendar.FRIDAY:
			return (new GregorianCalendar(nYear, Calendar.OCTOBER, 11));
		default:
			return (new GregorianCalendar(nYear, Calendar.OCTOBER, 10));
		}

	}

	public static java.util.Calendar VeteransDayObserved(int nYear) {
		// November 11th
		Calendar cal = new GregorianCalendar(nYear, Calendar.NOVEMBER, 11);
		switch (cal.get(Calendar.DAY_OF_WEEK)) {
		case Calendar.SATURDAY:
			return (new GregorianCalendar(nYear, Calendar.NOVEMBER, 10));
		case Calendar.SUNDAY:
			return (new GregorianCalendar(nYear, Calendar.NOVEMBER, 12));
		case Calendar.MONDAY:
		case Calendar.TUESDAY:
		case Calendar.WEDNESDAY:
		case Calendar.THURSDAY:
		case Calendar.FRIDAY:
		default:
			return cal;
		}
	}

	public static java.util.Calendar ThanksgivingObserved(int nYear) {
		Calendar cal = new GregorianCalendar(nYear, Calendar.NOVEMBER, 1);
		switch (cal.get(Calendar.DAY_OF_WEEK)) {
		case Calendar.SUNDAY:
			return (new GregorianCalendar(nYear, Calendar.NOVEMBER, 26));
		case Calendar.MONDAY:
			return (new GregorianCalendar(nYear, Calendar.NOVEMBER, 25));
		case Calendar.TUESDAY:
			return (new GregorianCalendar(nYear, Calendar.NOVEMBER, 24));
		case Calendar.WEDNESDAY:
			return (new GregorianCalendar(nYear, Calendar.NOVEMBER, 23));
		case Calendar.THURSDAY:
			return (new GregorianCalendar(nYear, Calendar.NOVEMBER, 22));
		case Calendar.FRIDAY:
			return (new GregorianCalendar(nYear, Calendar.NOVEMBER, 28));
		default: // Saturday
			return (new GregorianCalendar(nYear, Calendar.NOVEMBER, 27));
		}
	}

	public static java.util.Calendar ChristmasDayObserved(int nYear) {
		Calendar cal = new GregorianCalendar(nYear, Calendar.DECEMBER, 25);
		switch (cal.get(Calendar.DAY_OF_WEEK)) {
		case Calendar.SATURDAY:
			return (new GregorianCalendar(nYear, Calendar.DECEMBER, 24));
		case Calendar.SUNDAY:
			return (new GregorianCalendar(nYear, Calendar.DECEMBER, 26));
		case Calendar.MONDAY:
		case Calendar.TUESDAY:
		case Calendar.WEDNESDAY:
		case Calendar.THURSDAY:
		case Calendar.FRIDAY:
		default:
			return cal;
		}
	}
	


}
