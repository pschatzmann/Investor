package ch.pschatzmann.stocks.accounting.kpi;

import java.io.Serializable;

import ch.pschatzmann.dates.DateRange;
import ch.pschatzmann.stocks.Context;

/**
 * Individual value for the DrawDown calculation
 * @author pschatzmann
 *
 */
public class DrawDownValue implements Comparable<DrawDownValue>,Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private DateRange range;
	double max = Double.MIN_VALUE;
	double min = Double.MAX_VALUE;
	int numberOfDays;
			
	public DrawDownValue(DrawDownValue d) {
		this.range = new DateRange(range.getStart(), range.getEnd());
		this.numberOfDays = d.numberOfDays;
		this.max = d.max;
		this.min = d.min;
	}

	public DrawDownValue() {
	}

	public Double getPercent() {
		return (max - min) / max * 100;		
	}
	
	public Double getValue() {
		return max - min;	
	}
	
	public int getNumberOfDays() {
		return numberOfDays;		
	}
	
	public void incNumberOfDays() {
		numberOfDays++;
	}
	
	public DateRange getPeriod() {
		return range;
	}

	public DateRange getRange() {
		return range;
	}

	public void setRange(DateRange range) {
		this.range = range;
	}

	public double getMax() {
		return max;
	}

	public void setMax(double max) {
		this.max = max;
	}

	public double getMin() {
		return min;
	}

	public void setMin(double min) {
		this.min = min;
	}
	
	public boolean isDatesDifferent() {
		return !this.getRange().getStart().equals(this.getRange().getEnd());
	}

	/**
	 * Sort by % value descening
	 * @param o
	 * @return
	 */	
	@Override
	public int compareTo(DrawDownValue o) {
		return o.getPercent().compareTo(this.getPercent());
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Drawdown: ");
		sb.append(Context.format(this.getPercent()));
		sb.append("% in ");
		sb.append(this.getNumberOfDays());
		sb.append(" days (");
		sb.append(this.getRange());
		sb.append(")");
		return sb.toString();
	}

	
}
