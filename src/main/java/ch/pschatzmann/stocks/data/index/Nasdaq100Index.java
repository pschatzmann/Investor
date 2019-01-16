package ch.pschatzmann.stocks.data.index;

import ch.pschatzmann.stocks.IStockID;
import ch.pschatzmann.stocks.StockID;
import ch.pschatzmann.stocks.errors.UniverseException;

/**
 * Nasdaq 100 Index
 * 
 * @author pschatzmann
 *
 */
public class Nasdaq100Index extends IndexFromSlickChartsCommon {

	public Nasdaq100Index() throws UniverseException {
		super("https://www.slickcharts.com/nasdaq100");
	}

	@Override
	public IStockID getStockID() {
		return new StockID("^NDX", "");
	}

}
