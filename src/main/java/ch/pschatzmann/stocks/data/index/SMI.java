package ch.pschatzmann.stocks.data.index;

import ch.pschatzmann.stocks.IStockID;
import ch.pschatzmann.stocks.StockID;

/**
 * Swiss Market Index
 * 
 * @author pschatzmann
 *
 */
public class SMI extends IndexFromFile {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SMI() {
		super("/index/SMI.csv");
	}

	@Override
	public IStockID getStockID() {
		return new StockID("^SSMI","");
	}

}
