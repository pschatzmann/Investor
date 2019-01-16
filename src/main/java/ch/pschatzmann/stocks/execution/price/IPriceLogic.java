package ch.pschatzmann.stocks.execution.price;

import ch.pschatzmann.stocks.IStockRecord;

/**
 * Logic to determine the price at which the trade was closed
 * @author pschatzmann
 *
 */
public interface IPriceLogic {
	public Number getPrice(IStockRecord tick, boolean enter);
}
