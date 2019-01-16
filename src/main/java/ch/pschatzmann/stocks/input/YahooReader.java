package ch.pschatzmann.stocks.input;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.pschatzmann.dates.CalendarUtils;
import ch.pschatzmann.stocks.Context;
import ch.pschatzmann.stocks.IStockTarget;
import ch.pschatzmann.stocks.StockData.DateMatching;
import ch.pschatzmann.stocks.StockRecord;
import ch.pschatzmann.stocks.errors.SystemException;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;
import yahoofinance.quotes.stock.StockQuote;


/**
 * Reader which provides the stock data from Yahoo
 * 
 * @author pschatzmann
 *
 */

public class YahooReader implements IReaderEx, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(YahooReader.class);

	public YahooReader() {}
	
	@Override
	public int read(IStockTarget sd) {
		 return read(sd, Context.getDefaultStartDate());
	}

	@Override
	public int read(IStockTarget sd, Date fromDate) {
		LOG.info("read "+sd);
		int size = 0;
		try {
			String exchange = sd.getStockID().getExchange();
			if (exchange == null) {
				exchange = "";
			} else if (exchange.equals("HKG")) {
				exchange = "HKEX";
			} else if (exchange.equals("PCX")) {
				exchange = "AMEX";
			} else if (exchange.equals("ASE")) {
				exchange = "AMEX";
			} else if (exchange.equals("NASDAQ")){
				exchange = null;
			}
			
			Stock yahooStock = YahooFinance.get(sd.getStockID().getTicker());
			exchange = yahooStock.getStockExchange();

			if (!sd.getStockID().getExchange().equals(exchange)) {
				LOG.warn("The exchange does not match - we requested data from " + sd.getStockID().getExchange() + " but it was provided from " + exchange);
			}
			
			List<HistoricalQuote> history = getHistory(fromDate, yahooStock);

			size = history.size();
			sd.reset();
			int pos=0;
			for (HistoricalQuote l : history) {
				 processHistoricalQuotes(sd, l, pos++);
			}
			//processQuote(sd, yahooStock.getQuote());
		} catch (Exception ex) {
			size = 0;
			LOG.error(ex.getLocalizedMessage(), ex);
		}
		return size;

	}

	protected List<HistoricalQuote> getHistory(Date date, Stock yahooStock) throws IOException {
		List<HistoricalQuote> history = yahooStock.getHistory(CalendarUtils.toCalendar(date), Interval.DAILY);
		return history;
	}

	protected void processHistoricalQuotes(IStockTarget sd, HistoricalQuote l, int pos)
			throws SystemException {
		if (l.getOpen()==null && l.getClose()==null && l.getHigh()==null && l.getLow()==null) {
			return;
		}
		
		Date date = l.getDate().getTime();		
		double factor = 1.0;
		try {
			factor = l.getAdjClose().doubleValue() / l.getClose().doubleValue();
		} catch(Exception ex) {}

		StockRecord sr = new StockRecord();
		sr.setDate(date);
		sr.setStockID(sd.getStockID());
		sr.setAdjustmentFactor(factor);
		sr.setOpen(l.getOpen());
		sr.setClosing(l.getAdjClose());
		sr.setLow(l.getLow());
		sr.setHigh(l.getHigh());
		sr.setVolume(l.getVolume());
		sr.setIndex(pos);
		
		sd.addRecord(sr);
	}
	
	protected void processQuote(IStockTarget sd, StockQuote l) {
		boolean isNew = false;
		// ignore incomplete records
		if (l.getOpen()==null || l.getPrice()==null || l.getDayHigh()==null || l.getDayLow()==null) {
			return;
		}
		Date date = new Date();
		StockRecord sr = (StockRecord) sd.getValue(date,DateMatching.Exact);
		isNew = sr == null;
		if (sr!=null) {
			// check value
			if (sr.getClosing().doubleValue() != l.getPreviousClose().doubleValue()) {
				LOG.warn("The closing values do  not match "+sr.getClosing()+" vs "+l.getPreviousClose()+" for "+sr.getDate());
			}
		} else {
			sr = new StockRecord();
		}
		sr.setDate(date);
		sr.setStockID(sd.getStockID());
		sr.setAdjustmentFactor(1.0);
		sr.setOpen(l.getOpen());
		sr.setClosing(l.getPrice());
		sr.setLow(l.getDayLow());
		sr.setHigh(l.getDayHigh());
		sr.setVolume(l.getVolume());
		
		if (isNew) {
			sd.addRecord(sr);
		}		
	}

}
