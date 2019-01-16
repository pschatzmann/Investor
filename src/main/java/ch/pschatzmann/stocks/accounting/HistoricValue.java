package ch.pschatzmann.stocks.accounting;

import java.io.Serializable;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Date;

import ch.pschatzmann.stocks.Context;

/**
 * Record to document the evolution of a value over time
 * 
 * @author pschatzmann
 *
 */
public class HistoricValue implements IHistoricValue, Serializable, Comparable<IHistoricValue> {
	private static final long serialVersionUID = 1L;
	private Date date;
	private Double value;
	
	public HistoricValue() {}
	
	public  HistoricValue(ZonedDateTime date, Double v) {
		setDate(date);
		this.value = v;
	}
	public  HistoricValue(Date date, Double v) {
		this.date = date;
		this.value = v;
	}
	

	@Override
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public void setDate(ZonedDateTime date) {
		Instant instant = date.toInstant();
		// Convert Instant to Date.
		this.date = Date.from(instant);
	}
	/* (non-Javadoc)
	 * @see ch.pschatzmann.stocks.accounting.IHistoricValue#getValue()
	 */
	@Override
	public Double getValue() {
		return value;
	}
	public void setValue(Double value) {
		this.value = value;
	}

	@Override
	public int compareTo(IHistoricValue o) {
		return date.compareTo(o.getDate());
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(Context.format(date));
		sb.append(": ");
		sb.append(Context.format(value));
		return sb.toString();
	}
}
