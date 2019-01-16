package ch.pschatzmann.dates;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.pschatzmann.stocks.Context;

/**
 * Date range which is defined by a start and end date. The date range can have
 * a name
 * 
 * @author pschatzmann
 *
 */
public class DateRange implements Serializable {
	private static final Logger LOG = LoggerFactory.getLogger(DateRange.class);
	private static final long serialVersionUID = 1L;
	private static DateFormat df = new SimpleDateFormat("yyyyMMdd");
	private Date start;
	private Date end;
	private String name;

	public DateRange() {
	}

	public DateRange(Date start, Date end) {
		this.start = start;
		this.end = end;
	}

	public DateRange(String start, String end) {
		this.start = Context.date(start);
		this.end = Context.date(end);
	}

	public DateRange(String name, Date start, Date end) {
		this.start = start;
		this.end = end;
		this.name = name;
	}

	public DateRange(DateRange range) {
		this.start = range.start;
		this.end = range.end;
		this.name = toString(range.start, range.end);

	}

	public DateRange(Date endDate, int startOffset, int period) {
		Calendar endCal = Calendar.getInstance();
		endCal.setTime(endDate);
		Calendar startCal = Calendar.getInstance();
		startCal.setTime(endDate);
		startCal.add(period, -startOffset);
		this.start = startCal.getTime();
		this.end = endCal.getTime();
	}

	public DateRange(int startOffset, int period) {
		this(new Date(), startOffset, period);
	}

	public Date getStart() {
		return start;
	}

	public void setStart(Date start) {
		this.start = start;
	}
	
	public void setStart(ZonedDateTime start) {
		Instant instant = start.toInstant();
		this.start = Date.from(instant);
	}

	public Date getEnd() {
		return end;
	}

	public void setEnd(Date end) {
		this.end = end;
	}

	public void setEnd(ZonedDateTime end) {
		Instant instant = end.toInstant();
		this.end = Date.from(instant);
	}

	public static String format(Date d) {
		return df.format(d);
	}

	public boolean isValid(Date d) {
		if (start != null) {
			if (d.getTime() < start.getTime()) {
				return false;
			}
		}
		if (end != null) {
			if (d.getTime() > end.getTime()) {
				return false;
			}
		}
		return true;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	private String toString(Date start, Date end) {
		StringBuffer sb = new StringBuffer();
		if (start != null) {
			try {
				sb.append(df.format(start));
			} catch (Exception ex) {
				sb.append(start);
				LOG.error("Can not format date " + end);
			}
		}
		sb.append("-");
		if (end != null) {
			try {
				sb.append(df.format(end));
			} catch (Exception ex) {
				sb.append(end);
				LOG.error("Can not format date " + end);
			}
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		return name != null ? name : this.toString(this.getStart(), this.getEnd());
	}

	@Override
	public boolean equals(Object p1) {
		DateRange dr1 = (DateRange) p1;
		return this.toString().equals(dr1.toString());
	}

}
