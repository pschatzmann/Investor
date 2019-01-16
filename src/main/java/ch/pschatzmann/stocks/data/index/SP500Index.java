package ch.pschatzmann.stocks.data.index;

import ch.pschatzmann.stocks.IStockID;
import ch.pschatzmann.stocks.StockID;
import ch.pschatzmann.stocks.errors.UniverseException;

/**
 * S & P 500 Index
 * 
 * @author pschatzmann
 *
 */
public class SP500Index extends IndexFromSlickChartsCommon {

	public SP500Index() throws UniverseException {
		super("https://www.slickcharts.com/sp500");
	}

	@Override
	public IStockID getStockID() {
		return new StockID("^GSPC", "");
	}
}
