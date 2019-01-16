package ch.pschatzmann.stocks.data.index;

import ch.pschatzmann.stocks.IStockID;
import ch.pschatzmann.stocks.StockID;
import ch.pschatzmann.stocks.errors.UniverseException;

/**
 * Dow Jones Index
 * 
 * @author pschatzmann
 *
 */
public class DowJonesIndex extends IndexFromSlickChartsCommon {

	public DowJonesIndex() throws UniverseException {
		super("https://www.slickcharts.com/dowjones");
	}

	@Override
	public IStockID getStockID() {
		return new StockID("^DJI", "");
	}

}
