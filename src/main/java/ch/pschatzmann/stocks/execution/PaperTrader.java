package ch.pschatzmann.stocks.execution;

import java.io.Serializable;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.pschatzmann.stocks.IStockData;
import ch.pschatzmann.stocks.IStockRecord;
import ch.pschatzmann.stocks.accounting.Account;
import ch.pschatzmann.stocks.accounting.IAccount;
import ch.pschatzmann.stocks.accounting.IBasicAccount;
import ch.pschatzmann.stocks.accounting.Transaction;
import ch.pschatzmann.stocks.accounting.Transaction.Status;
import ch.pschatzmann.stocks.errors.TradingException;
import ch.pschatzmann.stocks.execution.price.ClosePrice;
import ch.pschatzmann.stocks.execution.price.IPriceLogic;

/**
 * Simulate stock order processing. We support market, limit and stop orders.
 * The fees are modeled as simple fees per trade.
 * 
 * @author pschatzmann
 *
 */

public class PaperTrader implements Serializable, ITrader {
	private static final long serialVersionUID = 1L;
	private static Logger LOG = LoggerFactory.getLogger(PaperTrader.class);
	private IAccount account;
	private ITradingDelayModel delay = new NoDelay();
	private IPriceLogic priceLogic = new ClosePrice();
	private IOrderCreator orderCreator = null;
	
	/**
	 * Setup a Trading simulation for an account using the indicated fees.
	 * 
	 * @param account
	 * @param feesPerTrade
	 */
	public PaperTrader(IAccount account, ITradingDelayModel delay) {
		this.account = account;
		this.delay = delay;
		this.orderCreator = new OrderCreator(account);
	}

	/**
	 * Setup a Trading simulation for an account using the indicated fees.
	 * 
	 * @param account
	 * @param feesPerTrade
	 */
	public PaperTrader(IAccount account) {
		this.account = account;
		this.orderCreator = new OrderCreator(account);
	}

	/**
	 * We also support IBasicAccount: In this case the system automatically wraps
	 * it with an Account in order to have the necessary evaluation functionality
	 * available.
	 * 
	 * @param account
	 */
	
	public PaperTrader(IBasicAccount account) {
		this.account = new Account(account);
		this.orderCreator = new OrderCreator(this.account);
	}

	/**
	 * We also support IBasicAccount: In this case the system automatically wraps
	 * it with an Account in order to have the necessary evaluation functionality
	 * available.
	 * 
	 * @param account
	 * @param delay
	 */
	public PaperTrader(IBasicAccount account, ITradingDelayModel delay) {
		this.account = new Account(account);
		this.setDelay(delay);
		this.orderCreator = new OrderCreator(this.account);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see stocks.accounting.execution.ITrader#execute()
	 */
	@Override
	public synchronized void execute() {
		// execute orders
		this.account.getAccount().getTransactions().stream().sorted().filter(l -> l.getStatus() == Status.Planned && !l.isCashTransfer()).forEach(l -> execute(l));
	}

	/**
	 * Execute an individual order line
	 * 
	 * @param l
	 * @throws TradingException 
	 */
	protected synchronized void execute(Transaction l)  {
		//LOG.info("execute "+l);
		if (!l.isCashTransfer()) {
			IStockData sd = this.account.getStockData(l.getStockID());
			Date start = new Date(l.getDate().getTime() + delay.getDelayInMs());
			for (IStockRecord sr : sd.getHistory()) {
				if (this.account.getDateRange().isValid(start)
				&& (sr.getDate().getTime()>=start.getTime())) {
					if (!l.isFilled()) {
						try {
						switch (l.getRequestedPriceType()) {
							case Market:
								double price = priceLogic.getPrice(sr, l.getQuantity() > 0.0).doubleValue();
								submitOrder(l, sr, price);
								break;
							case Limit:
								executeLimitOrder(l, sr);
								break;
							case Stop:
								executeStopOrder(l, sr);
								break;
							}
						} catch(Exception ex) {
							throw new RuntimeException(ex);
						}

						if (l.getQuantity() == 0l) {
							l.cancel("Cancelled because quantity was 0");
						}
					}
					if (l.isFilled()) {
						return;
					}
				}
			}
		}
	}

	/**
	 * A buy–stop order is typically used to limit a loss (or to protect an existing
	 * profit) on a short sale.[12] A buy-stop price is always above the current
	 * market price. For example, if an investor sells a stock short—hoping for the
	 * stock price to go down so they can return the borrowed shares at a lower
	 * price (i.e., covering)—the investor may use a buy stop order to protect
	 * against losses if the price goes too high. It can also be used to advantage
	 * in a declining market when you want to enter a long position close to the
	 * bottom after turnaround.
	 * 
	 * A sell–stop order is an instruction to sell at the best available price after
	 * the price goes below the stop price. A sell–stop price is always below the
	 * current market price. For example, if an investor holds a stock currently
	 * valued at $50 and is worried that the value may drop, he/she can place a
	 * sell–stop order at $40. If the share price drops to $40, the broker sells the
	 * stock at the next available price. This can limit the investor's losses or
	 * lock in some of the investor's profits (if the stop price is at or above the
	 * purchase price).
	 * 
	 * @param l
	 * @param sr
	 * @throws TradingException 
	 */
	protected void executeStopOrder(Transaction orderLine, IStockRecord sr) throws TradingException {
		if (orderLine.isBuy()) {
			if (orderLine.getRequestedPrice() <= sr.getClosing().doubleValue()) {
				double price = priceLogic.getPrice(sr, true).doubleValue();
				submitOrder(orderLine, sr, price);
				return;
			}
		} else {
			if (orderLine.getRequestedPrice() >= sr.getClosing().doubleValue()) {
				double price = priceLogic.getPrice(sr, false).doubleValue();
				submitOrder(orderLine, sr, price);
				return;
			}
		}
	}

	/**
	 * A limit order is an order to buy or sell a stock at a specific price or
	 * better. A buy limit order can only be executed at the limit price or lower,
	 * and a sell limit order can only be executed at the limit price or higher. A
	 * limit order is not guaranteed to execute.
	 * 
	 * @param orderLine
	 * @param sr
	 * @throws TradingException 
	 */
	protected void executeLimitOrder(Transaction orderLine, IStockRecord sr) throws TradingException {
		if (orderLine.isBuy()) {
			if (orderLine.getRequestedPrice() >= sr.getClosing().doubleValue()) {
				double price = priceLogic.getPrice(sr, true).doubleValue();
				submitOrder(orderLine, sr, price);
				return;
			}
		} else {
			if (orderLine.getRequestedPrice() <= sr.getClosing().doubleValue()) {
				double price = priceLogic.getPrice(sr, false).doubleValue();
				submitOrder(orderLine, sr, price);
				return;
			}
		}
	}

	/**
	 * Submit the order to the Trader
	 * @param orderLine
	 * @param sr
	 * @param price
	 * @throws TradingException 
	 */
	protected void submitOrder(Transaction orderLine, IStockRecord sr, double price) throws TradingException {
		//orderLine.fill(sr.getDate(), orderLine.getQuantity(), price, account);
		this.orderCreator.submitOrder(orderLine, sr, price);
	}
	
	
	/**
	 * Determines the actually defined price determination logic
	 * 
	 * @return
	 */
	@Override
	public IPriceLogic getPrice() {
		return priceLogic;
	}

	/**
	 * Defines the price determination logic
	 */
	public void setPrice(IPriceLogic price) {
		this.priceLogic = price;
	}

	@Override
	public ITradingDelayModel getDelay() {
		return delay;
	}

	public void setDelay(ITradingDelayModel delay) {
		this.delay = delay;
	}

	@Override
	public IAccount getAccount() {
		return this.account;
	}

	public IOrderCreator getOrderCreator() {
		return orderCreator;
	}

	public void setOrderCreator(IOrderCreator orderCreator) {
		this.orderCreator = orderCreator;
	}
	
	@Override
	public String toString() {
		return "PaperTrader for "+this.getAccount();
	}
	
}
