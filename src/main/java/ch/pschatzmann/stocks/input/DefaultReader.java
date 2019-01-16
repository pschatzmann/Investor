package ch.pschatzmann.stocks.input;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.pschatzmann.stocks.Context;
import ch.pschatzmann.stocks.IStockTarget;
import ch.pschatzmann.stocks.StockData;

/**
 * Combined Reader which sequentially tries to find a valid reader
 * 
 * @author pschatzmann
 *
 */
public class DefaultReader implements IReaderEx {
	private static final Logger LOG = LoggerFactory.getLogger(DefaultReader.class);
	private Collection<IReaderEx> readers;

	/**
	 * Empty Constructor which uses the following readers: YahooReader(),
	 * QuandlSixReader(), AlphaVantageReader(), IEXReader(), QuandlWIKIReader()
	 */
	public DefaultReader() {
		readers = new ArrayList((Arrays.asList(new YahooReader(), new QuandlSixReader(), new AlphaVantageReader(), new TiingoReader(),
				new QuandlWIKIReader(), new IEXReader())));
	}

	/**
	 * Constructor
	 * 
	 * @param readers
	 */
	public DefaultReader(List<IReaderEx> readers) {
		this.readers = readers;
	}

	/**
	 * Constructor
	 * 
	 * @param readers
	 */
	public DefaultReader(IReaderEx... readers) {
		this.readers = Arrays.asList(readers);
	}

	@Override
	public int read(IStockTarget target, Date date) {		
		for (IReaderEx r : readers) {
			StockData sd = new StockData(target.getStockID(),(IReader) null);
			if (!sd.isDataLoaded()) {
				try {
					int len = r.read(sd, date);
					if (len > 5) {
						LOG.info("Using the reader " + r.getClass().getSimpleName() + " for " + sd.getStockID());
						sd.getHistory().stream().forEach(rec -> target.addRecord(rec));
						return len;
					}
				} catch(Exception ex) {}
			}
		}
		return 0;
	}
	
	/**
	 * Adds a reader 
	 * @param r
	 */
	public void addReader(IReaderEx r) {
		if (!readers.contains(r))
			readers.add(r);
	}
	
	/**
	 * Returns the currently defined list of readers
	 * @return
	 */
	public Collection<IReaderEx> getReaders() {
		return this.readers;
	}

	@Override
	public int read(IStockTarget sd) {
		return read(sd, Context.getDefaultStartDate());
	}
	

}
