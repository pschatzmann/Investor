package ch.pschatzmann.stocks.input;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import ch.pschatzmann.dates.DateRange;
import ch.pschatzmann.stocks.IStockRecord;
import ch.pschatzmann.stocks.IStockTarget;
import ch.pschatzmann.stocks.StockData;
import ch.pschatzmann.stocks.StockRecord;

/**
 * Reads the stock data from multiple sources. First it gets it from the primary
 * source defined in the stock id. For the missing entries we try the indicated
 * secondary readers.
 * 
 * @author pschatzmann
 *
 */

public class CombinedReader implements IReader, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<IReaderEx> secondaryList;
	private IReader reader;

	public CombinedReader(IReader primary, IReaderEx reader) {
		this.secondaryList = Arrays.asList(reader);
		this.reader = primary;
	}

	public CombinedReader(IReader primary, List<IReaderEx> secondaryList) {
		this.secondaryList = secondaryList;
		this.reader = primary;
	}

	public CombinedReader(List<IReaderEx> alternativeReaderList) {
		this.secondaryList = alternativeReaderList;
		this.reader = null;
	}

	@Override
	public int read(IStockTarget sd) {
		StockData sd1 = new StockData(sd.getStockID());
		int count = reader == null ? 0 : reader.read(sd1);
		if (count > 0) {
			DateRange period = sd.getDateRange();
			for (IReaderEx r : this.secondaryList) {
				StockData sd2 = new StockData(sd.getStockID());
				int count1 = r.read(sd2, period.getEnd());
				if (count1 > 0) {
					count += count1;
					sd1.getHistory().addAll(sd2.getHistory());
					int index = 0;
					for (IStockRecord sr : sd1.getHistory()) {
						sd.addRecord(sr);
						if (sd instanceof StockRecord) {
							((StockRecord)sd).setIndex(index);
						}
						index++;
					}
					break;

				}
			}
		} else {
			for (IReaderEx r : this.secondaryList) {
				int count1 = r.read(sd);
				if (count > 0) {
					count += count1;
					break;
				}
			}
		}
		return count;
	}
}
