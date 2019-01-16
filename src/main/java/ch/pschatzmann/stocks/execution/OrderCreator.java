package ch.pschatzmann.stocks.execution;

import ch.pschatzmann.stocks.IStockRecord;
import ch.pschatzmann.stocks.accounting.IAccount;
import ch.pschatzmann.stocks.accounting.Transaction;

/**
 * Simulated order creation which is used by the paper trader. We execute and fill the order directly.
 * In real life this will need to be executed in 2 steps.
 * 
 * @author pschatzmann
 *
 */
public class OrderCreator implements IOrderCreator {
	private IAccount account;
	
	OrderCreator(IAccount account){
		this.account = account;
	}
	
	@Override
	public void submitOrder( Transaction orderLine, IStockRecord sr, double price) {
		orderLine.fill(sr.getDate(), orderLine.getQuantity(), price, account);
	}

}
