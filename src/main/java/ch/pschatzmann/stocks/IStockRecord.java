package ch.pschatzmann.stocks;

import java.util.Date;

 /***
 * Interface for End of day information of a stock
 * 
 * @author pschatzmann
 *
 */

public interface IStockRecord extends Comparable<IStockRecord> {
	public Date getDate();
	public Number getClosing();
	public boolean isValid();
	public Number getVolume();
	public Number getOpen();
	public Number getHigh();
	public Number getLow();
	public IStockID getStockID();
	public void setStockID(IStockID sd);
	public Number getAdjustmentFactor();
	
}