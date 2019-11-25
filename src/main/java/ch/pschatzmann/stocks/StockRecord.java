package ch.pschatzmann.stocks;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.ta4j.core.Bar;

import com.fasterxml.jackson.annotation.JsonIgnore;



/**
 * End of day information of a stock
 * 
 * @author pschatzmann
 *
 */

public class StockRecord implements IStockRecord, Serializable  {
	private static final long serialVersionUID = 1L;
	private int index;
	private Date date;
	private Number low;
	private Number high;
	private Number open;
	private Number closing;
	private Number adjustmentFactor;
	private Number volume;
	private DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
	private transient IStockID id;
	
	public StockRecord() {
	}
	
	public StockRecord(IStockRecord sr) {
		this.id = sr.getStockID();
		this.date = sr.getDate();
		this.open = sr.getOpen();
		this.low = sr.getLow();
		this.high = sr.getHigh();
		this.closing = sr.getClosing();
		this.volume = sr.getVolume();
	}

	public StockRecord(Bar tick, IStockID parent, int index) {
		this.date = Date.from(tick.getEndTime().toInstant());
		this.open = Context.toDouble(tick.getOpenPrice());
		this.low = Context.toDouble(tick.getLowPrice());
		this.high = Context.toDouble(tick.getHighPrice());
		this.closing = Context.toDouble(tick.getClosePrice());
		this.volume = Context.toDouble(tick.getVolume());
		this.index = index;
		this.id = parent;
	}


	@Override
	public Date getDate() {
		return date;
	}
	
	public void setDate(Date date) {
		this.date = Context.date(Context.format(date));
	}
	@Override
	public Number getLow() {
		return low;
	}
	public void setLow(Number min) {
		this.low = min;
	}
	@Override
	public Number getHigh() {
		return high;
	}
	public void setHigh(Number max) {
		this.high = max;
	}
	@Override
	public Number getClosing() {
		return closing;
	}
	public void setClosing(Number closing) {
		this.closing = closing;
	}
	
	@Override
	public Number getOpen() {
		return open;
	}

	public void setOpen(Number open) {
		this.open = open;
	}

	@Override
	public Number getVolume() {
		return volume;
	}

	public void setVolume(Number volume) {
		this.volume = volume;
	}

	@Override
	public Number getAdjustmentFactor() {
		return adjustmentFactor;
	}

	public void setAdjustmentFactor(Number adjustmentFactor) {
		this.adjustmentFactor = adjustmentFactor;
	}

	@Override
	@JsonIgnore
	public boolean isValid() {
		return this.date!=null && this.closing !=null && Double.isFinite(closing.doubleValue());
	}
	

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(this.getStockID());
		sb.append(" ");
		sb.append(df.format(this.getDate()));
		sb.append(": ");
		sb.append(this.getClosing());
		return sb.toString();
	}

	@JsonIgnore
	public String getHeader() {
		StringBuffer sb = new StringBuffer();
		sb.append("date");
		sb.append(",");
		sb.append("volume");
		sb.append(",");
		sb.append("open");
		sb.append(",");
		sb.append("closing");
		sb.append(",");
		sb.append("low");
		sb.append(",");
		sb.append("high");
		return sb.toString();
	}
	
	@JsonIgnore
	public String getData() {
		StringBuffer sb = new StringBuffer();
		sb.append(df.format(this.getDate()));
		sb.append(",");
		sb.append(this.getVolume());
		sb.append(",");
		sb.append(Context.format(this.getOpen()));
		sb.append(",");
		sb.append(Context.format(this.getClosing()));
		sb.append(",");
		sb.append(Context.format(this.getLow()));
		sb.append(",");
		sb.append(Context.format(this.getHigh()));
		return sb.toString();
	}


	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	@Override
	public int compareTo(IStockRecord o) {
		return this.getDate().compareTo(o.getDate());
	}

	@JsonIgnore 
	@Override
	public IStockID getStockID() {
		return this.id;
	}


	@JsonIgnore 
	@Override
	public void setStockID(IStockID sd) {
		this.id = sd;
	}
	
}
