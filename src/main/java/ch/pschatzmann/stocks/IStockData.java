package ch.pschatzmann.stocks;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

import org.ta4j.core.TimeSeries;

import ch.pschatzmann.dates.DateRange;
import ch.pschatzmann.stocks.StockData.DateMatching;

/**
 * Basic Interface for reading stock data
 * 
 * @author pschatzmann
 *
 */
public interface IStockData {
	/**
	 * Returns the tocker symbol and exchange
	 * 
	 * @return
	 */
	public IStockID getStockID();

	/**
	 * Provides all available historic rates
	 * 
	 * @return
	 */
	public List<IStockRecord> getHistory();

	/**
	 * 
	 * @param startDate
	 * @return
	 */
	public List<IStockRecord> getHistory(Date startDate);

	/**
	 * Returns the date record for the indicated date. This is e.g. used to
	 * determine the trading price
	 * 
	 * @param date
	 * @return
	 */
	public IStockRecord getValue(Date date);

	public IStockRecord getValue(Date date, DateMatching next);

	/**
	 * Returns true if there is no history
	 * 
	 * @return
	 */
	public boolean isEmpty();

	/**
	 * Returns the exchange
	 * 
	 * @return
	 */
	public String getExchange();

	/**
	 * Returns the ticker symbol
	 * 
	 * @return
	 */
	public String getTicker();

	/**
	 * Returns the number of history records
	 * 
	 * @return
	 */
	public int size();

	/**
	 * Provides a IStockData whith the history in the requested date range
	 * 
	 * @param range
	 * @return
	 */
	public IStockData filterDates(DateRange range);

	/**
	 * Writes the data as CSV to the output stream
	 * 
	 * @param os
	 * @throws IOException
	 */
	public void writeCSV(OutputStream os) throws IOException;

	/**
	 * Converts the StockData to a TimeSeries
	 * 
	 * @return
	 */

	public TimeSeries toTimeSeries();

	/**
	 * Converts the StockData to a TimeSeries
	 * 
	 * @param dateRange
	 * @return
	 */
	public TimeSeries toTimeSeries(DateRange dateRange);

	/**
	 * Converts the StockData to a TimeSeries
	 * @param startDate
	 * @return
	 */
	public TimeSeries toTimeSeries(Date startDate);
	
	/**
	 * Provides the data as CSV string
	 * @return
	 */
	public String csv();

}
