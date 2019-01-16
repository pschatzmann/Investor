package ch.pschatzmann.stocks;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.TimeSeries;

import ch.pschatzmann.dates.DateRange;
import ch.pschatzmann.stocks.accounting.HistoricValue;
import ch.pschatzmann.stocks.accounting.IHistoricValue;
import ch.pschatzmann.stocks.input.IReader;
import ch.pschatzmann.stocks.integration.StockTimeSeries;
import ch.pschatzmann.stocks.utils.CSVWriter;

/**
 * Price history of an individual stock
 * 
 * @author pschatzmann
 *
 */

public class StockData implements Serializable, IStockTarget, IStockData, IResettable {
	public enum DateMatching {
		Exact, Prior, Next
	};

	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(StockData.class);
	private List<IStockRecord> history = null;
	private IStockID id;
	private transient Comparator<IStockRecord> dateComparator;
	private transient DateFormat df;
	private transient TimeSeries timeSeries;
	private IReader reader = null;

	public StockData() {
	}

	public StockData(IStockID id) {
		this();
		this.id = id;
		this.reader = Context.getDefaultReader();
	}

	public StockData(IStockID id, IReader reader) {
		this();
		this.id = id;
		this.reader = reader;
	}

	public StockData(StockData source) {
		this.id = source.id;
		this.reader = source.reader;
		this.history = source.history;
		this.timeSeries = source.timeSeries;
	}

	public StockData(IStockID id, List<IStockRecord>history) {
		this.id = id;
		this.history = history;
	}

	
	@Override
	public String getTicker() {
		return id == null ? null : id.getTicker();
	}

	@Override
	public String getExchange() {
		return id == null ? null : id.getExchange();
	}

	@Override
	public IStockID getStockID() {
		return this.id;
	}

	public void setStockID(IStockID stockID) {
		this.id = stockID;
	}

	public void setStockID(String stockID) {
		this.id = StockID.parse(stockID);
	}

	@Override
	public synchronized List<IStockRecord> getHistory() {
		if (history == null) {
			LOG.debug("getHistory " + this);
			IReader r = this.getReader();

			String key = this.getStockID() + "/" + getSimpleClassName(r);
			getHistoryFromCache(key);

			if (history == null) {
				history = new ArrayList();
				if (r != null) {
					int count = r.read(this);
					LOG.info("Reading stock data for {} -> {} records", this.getStockID(), count);
					putHistoryToCache(key);
				}
			}
		}
		return history;
	}

	public static String getSimpleClassName(IReader r) {
		return r == null ? "" : r.getClass().getSimpleName();
	}

	private void putHistoryToCache(String key) {
		if (Context.isCacheActive()) {
			Context.getCache().put(key, history);
		}
	}

	private void getHistoryFromCache(String key) {
		if (Context.isCacheActive()) {
			try {
				history = Context.getCache().get(key);
				if (history!=null) {
					LOG.info(this.getStockID()+" loaded from cache");
				}
			} catch(Exception ex) {
				LOG.warn("Could not load from cache for "+key,ex);
				try {
					Context.getCache().remove(key);
				} catch(Exception ex1) {}
			}
		}
	}

	public List<IStockRecord> getHistory(DateRange range) {
		return getHistory(range.getStart(), range.getEnd());
	}

	@Override
	public List<IStockRecord> getHistory(Date start) {
		return getHistory(start, new Date());
	}

	public List<IStockRecord> getHistory(Date start, Date end) {
		List<IStockRecord> result = new ArrayList();
		if (end == null) {
			end = new Date();
		}

		for (IStockRecord r : getHistory()) {
			if ((start != null && !(r.getDate().before(start)) || r.getDate().after(end))) {
				result.add(r);
			}
		}
		return result;
	}

	@Override
	public IStockRecord getValue(Date date) {
		return getValue(date, DateMatching.Next);
	}

	@Override
	public IStockRecord getValue(Date date, DateMatching dateMatching) {
		Date normalizedDate = Context.date(date);
		List<IStockRecord> result = getHistory();
		int pos = this.getPos(normalizedDate, result, dateMatching);
		IStockRecord rec = result.isEmpty() ? null : result.get(pos);
		if (rec != null && dateMatching.equals(DateMatching.Exact) && !Context.date(rec.getDate()).equals(normalizedDate)) {
			rec = null;
		}
		return rec;
	}

	@Override
	public DateRange getDateRange() {
		List<IStockRecord> history = getHistory();
		if (history.isEmpty()) {
			return new DateRange(new Date(), new Date());
		}
		return new DateRange(history.get(0).getDate(), history.get(history.size() - 1).getDate());
	}

	/**
	 * Returns the latest value
	 * 
	 * @return
	 */
	@Override
	public IStockRecord getValue() {
		List<IStockRecord> history = getHistory();
		if (history.isEmpty()) {
			return null;
		}
		return history.get(history.size() - 1);
	}

	/**
	 * Returns the data as list of csv text records
	 * @return
	 */
	public List<String> csvList() {
		List<String> result = new ArrayList();
		List<IStockRecord> history = this.getHistory();
		for (int j = 0; j < history.size(); j++) {
			StockRecord rec = (StockRecord) history.get(j);
			if (j == 0) {
				result.add(rec.getHeader());
			}
			result.add(rec.getData());
		}
		return result;
	}
	
	/**
	 * Returns the data as csv String
	 * @return
	 */
	public String csv() {
		StringBuffer result = new StringBuffer();
		List<IStockRecord> history = this.getHistory();
		for (int j = 0; j < history.size(); j++) {
			StockRecord rec = (StockRecord) history.get(j);
			if (j == 0) {
				result.append(rec.getHeader());
				result.append(System.lineSeparator());
			}
			result.append(rec.getData());
			result.append(System.lineSeparator());
		}
		return result.toString();
		
	}

	public List<IStockRecord> getHistory(String year) {
		List<IStockRecord> result = new ArrayList();
		for (IStockRecord rec : getHistory()) {
			if (this.getYearDateFormat().format(rec.getDate()).equals(year)) {
				result.add(rec);
			}
		}
		return result;
	}

	public Collection<String> getYears() {
		Collection<String> result = new HashSet();
		for (IStockRecord rec : getHistory()) {
			result.add(this.getYearDateFormat().format(rec.getDate()));
		}
		return result;
	}

	public void setHistory(List<IStockRecord> history) {
		this.history = history;
	}

	@Override
	public void addRecord(IStockRecord stockRecord) {
		if (history==null ) {
			history = new ArrayList();
		}

		if (stockRecord.getStockID() == null) {
			stockRecord.setStockID((this.getStockID()));
		}

		history.add(stockRecord);
	}

	/**
	 * Resets the history to contain only values between the indicated dates
	 * 
	 * @param start
	 * @param end
	 * @return
	 */
	@Override
	public StockData filterDates(DateRange range) {
		LOG.debug("filterDates");
		StockData filteredCopy = new StockData(this);
		filteredCopy.setHistory(this.getHistory(range.getStart(), range.getEnd()));
		LOG.debug(this + ": " + this.getHistory().size());
		return filteredCopy;
	}

	private int getPos(Date d, List<IStockRecord> list, DateMatching dateMatching) {
		StockRecord sr = new StockRecord();
		sr.setDate(d);
		int pos = Collections.binarySearch(list, sr);
		if (pos > 0) {
		} else {
			pos = Math.abs(pos) - 1;
			if (dateMatching.equals(DateMatching.Prior)) {
				pos--;
			}
			if (pos >= list.size() - 1) {
				pos = list.size() - 1;
			}
			if (pos < 0) {
				pos = 0;
			}
		}
		return pos;
	}

	/**
	 * Returns a Date format which contains only the year as 4 digit string
	 * 
	 * @return
	 */
	public DateFormat getYearDateFormat() {
		if (df == null) {
			df = new SimpleDateFormat("yyyy");
		}
		return df;
	}

	/**
	 * Converts a List of IStockRecord to a list of HistoricValue
	 * 
	 * @param dateRange
	 * @return
	 */
	public Stream<IHistoricValue> toHistoryValuesClosingPrice() {
		return this.getHistory().stream().filter(p -> p.isValid())
				.map(hv -> (IHistoricValue) new HistoricValue(hv.getDate(), hv.getClosing().doubleValue())).sorted();
	}

	/**
	 * Converts a List of IStockRecord to a list of HistoricValue
	 * 
	 * @param dateRange
	 * @return
	 */
	public Stream<IHistoricValue> toHistoryValuesVolume() {
		return this.getHistory().stream()
				.map(hv -> (IHistoricValue) new HistoricValue(hv.getDate(), hv.getVolume().doubleValue())).sorted();
	}

	/**
	 * Removes the price history in order to free up the memory. The missing data is
	 * dynamically reloaded if it is needed.
	 */
	@Override
	public void reset() {
		LOG.info("reset "+this);
		history = null;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(this.getStockID());
		if (this.history != null) {
			sb.append(" size:");
			sb.append(this.history.size());
		}
		return sb.toString();
	}

	public boolean isDataLoaded() {
		return history != null;
	}

	@Override
	public int hashCode() {
		IStockID id = this.getStockID();
		return id == null ? 0 : id.hashCode();
	}

	public IReader getReader() {
		IReader r = null;
		if (r == null) {
			r = this.reader;
		}
		return r;
	}

	@Override
	public int size() {
		return this.getHistory().size();
	}

	@Override
	public boolean isEmpty() {
		return this.getHistory().isEmpty();
	}
	
	@Override
	public void writeCSV(OutputStream os) throws IOException {
		new CSVWriter().write(this,os);
	}

	public static void resetCache() {
		Context.getCache().clear();
	}
	
	public TimeSeries toTimeSeries() {
		return new StockTimeSeries(this);
	}

	public TimeSeries toTimeSeries(DateRange dateRange) {
		return new StockTimeSeries(this, dateRange);
	}
	
	public TimeSeries toTimeSeries(Date date) {
		return new StockTimeSeries(this, Context.getDateRanges(date).get(0));
	}


}
