package ch.pschatzmann.dates;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ch.pschatzmann.stocks.Context;
import ch.pschatzmann.stocks.errors.DateException;

/**
 * Some insteresting date ranges
 * 
 * @author pschatzmann
 *
 */
public class StandardDateRangeSource implements IDateRangeSource, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Returns the supported default periods which are used to analyse the data
	 * 
	 * @return
	 * @throws DateException
	 */
	@Override
	public  List<DateRange> getDates() throws DateException {
		List<DateRange> dateRanges = new ArrayList();
		dateRanges.add(new DateRange("Crash 2007-2009", Context.date("2007-08-01"), Context.date("2009-02-01")));
		/*
		 * for (int day=1;day<=5;day++) { dateRanges.add(new DateRange(day+
		 * " day"+s(day),Common.getDate(-day),Common.getDate(0))); }
		 */
		for (int week = 1; week <= 4; week++) {
			dateRanges.add(new DateRange(week + " week" + s(week), getDate(-week * 7), getDate(0)));
		}
		for (int month = 1; month <= 11; month++) {
			dateRanges.add(new DateRange(month + " month" + s(month), getDateMonth(-month), getDate(0)));
		}
		for (int year = 1; year <= 10; year++) {
			dateRanges.add(new DateRange(year + " year" + s(year), getDateMonth(-year * 12), getDate(0)));
		}
		return dateRanges;
	}
	
	public static Date getDate(int offsetDays) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, offsetDays);
		return cal.getTime();
	}

	public static Date getDateMonth(int offsetMonths) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, offsetMonths);
		return cal.getTime();
	}
	
	private static String s(int j) {
		return j == 1 ? "" : "s";
	}

}
