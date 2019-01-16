package ch.pschatzmann.stocks.execution;

import ch.pschatzmann.stocks.IStockRecord;
import ch.pschatzmann.stocks.accounting.Transaction;
import ch.pschatzmann.stocks.errors.TradingException;

/**
 * Interface for the support of the creation and submission of new orders to a
 * trader
 * 
 * @author pschatzmann
 *
 */
public interface IOrderCreator {
	void submitOrder(Transaction orderLine, IStockRecord sr, double price) throws TradingException;

}