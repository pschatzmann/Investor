package ch.pschatzmann.stocks;

import java.util.Date;

import ch.pschatzmann.dates.DateRange;
import ch.pschatzmann.stocks.StockData.DateMatching;

/**
 * Interface to add stock data
 * @author pschatzmann
 *
 */
public interface IStockTarget {
	public IStockID getStockID();
	public DateRange getDateRange();
	public IStockRecord getValue();
	public IStockRecord getValue(Date date, DateMatching dateMatching);
	public void addRecord(IStockRecord stockRecord);
	public void reset();

}
