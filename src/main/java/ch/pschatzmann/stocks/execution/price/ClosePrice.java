package ch.pschatzmann.stocks.execution.price;

import java.io.Serializable;

import ch.pschatzmann.stocks.IStockRecord;

/**
 * We just use the closeing price
 * @author pschatzmann
 *
 */
public class ClosePrice implements IPriceLogic,Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public Number getPrice(IStockRecord tick, boolean enter) {
		return tick.getClosing();
	}
}
