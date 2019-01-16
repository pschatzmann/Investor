package ch.pschatzmann.stocks.strategy.allocation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.pschatzmann.stocks.Context;
import ch.pschatzmann.stocks.IStockData;
import ch.pschatzmann.stocks.IStockID;
import ch.pschatzmann.stocks.IStockRecord;
import ch.pschatzmann.stocks.accounting.IAccount;
import ch.pschatzmann.stocks.accounting.Transaction;
import ch.pschatzmann.stocks.execution.ITrader;
import ch.pschatzmann.stocks.execution.fees.IFeesModel;
import ch.pschatzmann.stocks.strategy.ITradingStrategy;
import ch.pschatzmann.stocks.utils.Calculations;

/**
 * We buy as much stocks as possible. At each trade we rebalance the stock
 * amounts by recalculating the targets using a IDistributor. Per default the
 * system uses an EvenDistributor which allocates the values evenly.
 * 
 * @author pschatzmann
 *
 */

public class DistributedAllocationStrategy implements IAllocationStrategy, Serializable {
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(DistributedAllocationStrategy.class);
	private ITrader trader;
	private IDistributor distributor = new EvenDistributor();
	private Map<ITradingStrategy, Action> plannedTransactions = new HashMap();

	enum Action {
		buy, sell
	}


	public DistributedAllocationStrategy(ITrader trader) {
		this.trader = trader;
	}

	public DistributedAllocationStrategy(ITrader trader, IDistributor distributor) {
		this.trader = trader;
		this.distributor = distributor;
	}

	@Override
	public Long onBuy(IAccount account, IStockRecord sr, ITradingStrategy strategy) {
		IStockID id = sr.getStockID();
		Long currentQty = getCurrentQuantity(account, id);

		if (currentQty == 0) {
			addPlannedTransaction(strategy, Action.buy);
			LOG.info(id + " buy signal");
		} else {
			LOG.info(id + " buy signal ignored because we have already stock");
		}
		// we delay the transaction
		return 0l;
	}

	@Override
	public Long onSell(IAccount account, IStockRecord sr, ITradingStrategy strategy) {
		// determine sell qty
		IStockID id = sr.getStockID();
		Long currentQty = getCurrentQuantity(account, id);

		if (currentQty > 0) {
			// buy other stock
			LOG.info(id + " sell signal-> with current stock " + currentQty);
			addPlannedTransaction(strategy, Action.sell);
		} else {
			LOG.info(id + " sell signal ignored because of no stock");
		}
		// we delay the transaction
		return 0l;
	}

	protected Long getCurrentQuantity(IAccount account, IStockID id) {
		return account.getQuantity(id);
	}

	protected void addPlannedTransaction(ITradingStrategy id, Action action) {
		this.plannedTransactions.put(id, action);
	}

	protected Date getDateWithDelay(Date date) {
		Date start = new Date(date.getTime() + this.trader.getDelay().getDelayInMs());
		return start;
	}

	protected double getPrice(IAccount acc, IStockID id, Date inputDate, boolean enter) {
		Date date = getDateWithDelay(inputDate);
		IStockData sd = acc.getStockData(id);
		IStockRecord srAfterDelay = sd.getValue(date);
		return this.trader.getPrice().getPrice(srAfterDelay, enter).doubleValue();
	}

	/**
	 * Process consolidated orders which contain buys and sells from the strategy
	 * and the Rebalancing
	 */
	@Override
	public void onEndOfDate(IAccount account, Date date) {
		Collection<Transaction> transactions = new ArrayList();
		if (!this.plannedTransactions.isEmpty()) {
			LOG.info("onEndOfDate:" + Context.format(date));
			updateDistributor(date);
			Collection<ITradingStrategy> strategies = getAllStrategies(account, date);
			for (ITradingStrategy strategy : strategies) {
				IStockID id = strategy.getStockData().getStockID();
				Action a = this.plannedTransactions.get(strategy);
				if (a == Action.sell) {
					// we sell all existing qty
					long qty = this.getCurrentQuantity(account, id);
					Transaction t = new Transaction(date, id, -qty);
					account.getAccount().addTransaction(t);
				} else {
					// we rebalance the amounts
					double totalValue = account.getTotalValue(date);
					double distributionFactor = distributor.getFactor(strategy, date);
					if (distributionFactor > 0.0) {
						addTransaction(account, date, transactions, id, a, totalValue, distributionFactor);
					} else {
						LOG.info(id+" not purchased because distribution factor is 0.0 (or negative)");
					}
				}
			}
			
			transactions.forEach(t -> account.getAccount().addTransaction(t));			
			plannedTransactions.clear();
		}
	}

	private void addTransaction(IAccount account, Date date, Collection<Transaction> transactions, IStockID id,
			Action a, double totalValue, double distributionFactor) {
		double targetValueOfStock = totalValue * distributionFactor;
		double actualValueOfStock = account.getActualValue(date, id);
		double buyAmount = targetValueOfStock - actualValueOfStock;
		boolean isBuy = buyAmount > 0;
		double price = this.getPrice(account, id, date, isBuy);
		long qty = getQty(buyAmount, account.getAccount().getFeesModel(), price, isBuy);

		Transaction t = new Transaction(date, id, qty);
		if (a != Action.buy) {
			t.setComment("rebalance of stocks");
			// we prevent buys of stock that lost value 
			if (isRebalanceBuyOnBuy(id, qty)) {
				LOG.info("No rebalencing buy on Buy for "+id);
				qty = 0;
			}
		}
						
		if (qty != 0) {
			transactions.add(t);
			LOG.info("{}",t);
		}
	}
	
	protected boolean isRebalanceBuyOnBuy(IStockID id, long qty) {
		if (this.plannedTransactions.size()==1) {
			if (plannedTransactions.containsValue(Action.buy)) {
				return qty>0L;
			}
		}
		return false;
	}
	

	protected long getQty(double value, IFeesModel fees, double price, boolean buy) {
		return buy ? this.getBuyQty(value, fees, price) : this.getSellQty(value, fees, price);
	}

	protected long getBuyQty(double value, IFeesModel fees, double price) {
		long result;
		double quantity = value / price;
		double extimatedFees = fees.getFeesPerTrade(quantity, value);
		result = Calculations.toLong((value - extimatedFees) / price);
		result = Math.max(0L, result);
		return result;
	}

	protected long getSellQty(double value, IFeesModel fees, double price) {
		long result;
		double quantity = value / price;
		double extimatedFees = fees.getFeesPerTrade(quantity, Math.abs(value));
		// To handle rounding issues we always round up (so we add 1)
		result = Calculations.toLong((value - extimatedFees) / price) + 1;
		// we make sure that the reszkt us negative
		result = Math.min(0L, result);
		return result;
	}

	protected void updateDistributor(Date date) {
		for (Entry<ITradingStrategy, Action> e : plannedTransactions.entrySet()) {
			switch (e.getValue()) {
			case sell:
				distributor.remove(e.getKey());
				break;
			case buy:
				distributor.add(e.getKey(), date);
				break;
			}
		}
	}

	protected Collection<ITradingStrategy> getAllStrategies(IAccount account, Date date) {
		return distributor.getAllStrategies();
	}

}
