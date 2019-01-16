package ch.pschatzmann.stocks.strategy.allocation;

import java.io.Serializable;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.pschatzmann.stocks.IStockData;
import ch.pschatzmann.stocks.IStockID;
import ch.pschatzmann.stocks.IStockRecord;
import ch.pschatzmann.stocks.accounting.IAccount;
import ch.pschatzmann.stocks.execution.IExecutorAware;
import ch.pschatzmann.stocks.execution.ITrader;
import ch.pschatzmann.stocks.execution.StrategyExecutor;
import ch.pschatzmann.stocks.execution.fees.IFeesModel;
import ch.pschatzmann.stocks.strategy.ITradingStrategy;
import ch.pschatzmann.stocks.utils.Calculations;

/**
 * We try distribute the allocation of investments evenly. If we have n
 * investments, we limit the maximum investment of a single stock to 1/n of the
 * total value.
 * 
 * @author pschatzmann
 *
 */

public class SimpleDistributedAllocationStrategy implements IAllocationStrategy, IExecutorAware, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(SimpleDistributedAllocationStrategy.class);
	private double cashReserve=0;
	private double allocationFactor = 1.0;
	private ITrader trader;

	public SimpleDistributedAllocationStrategy(ITrader trader) {
		this.trader = trader;
	}

	@Override
	public Long onBuy(IAccount account, IStockRecord sr,ITradingStrategy strategy) {
		// determine the buy quantity
		long result = 0;
		Long qty = getCurrentQuantity(account, sr);
		if (qty == 0) {
			double amount = Math.min(account.getCash(), account.getTotalValue(sr.getDate()) * allocationFactor);
			Number price = this.getPrice(sr.getStockID(), sr.getDate(), true);
			result = getBuyQuantity(amount, account.getAccount().getFeesModel(), price.doubleValue());
			LOG.debug("buy signal  -> " + result);
		}
		return result;
	}

	@Override
	public Long onSell(IAccount account, IStockRecord sr, ITradingStrategy strategy) {
		// determine sell qty
		Long qty = getCurrentQuantity(account, sr);
		LOG.debug("sell signal  -> " + qty);
		return qty;
	}

	private Long getCurrentQuantity(IAccount account, IStockRecord sr) {
		return account.getQuantity(sr.getStockID());
	}

	protected long getBuyQuantity(double cash, IFeesModel fees, double price) {
		long result;
		double quantity = (cash) / price;
		double value = price * quantity;
		double extimatedFees = fees.getFeesPerTrade(quantity, value);
		result = Calculations.toLong((((cash - this.cashReserve) * this.allocationFactor) - extimatedFees) / price);
		result = Math.max(0L, result);
		return result;
	}

	@Override
	public void setExecutor(StrategyExecutor strategyExecutor) {
		this.allocationFactor = 1.0 / strategyExecutor.getTradingStrategies().size();
	}
	
	public double getCashReserve() {
		return cashReserve;
	}

	public void setCashReserve(double cashReserve) {
		this.cashReserve = cashReserve;
	}
	
	private Date getDateWithDelay(Date date) {
		Date start = new Date(date.getTime() + this.trader.getDelay().getDelayInMs());
		return start;
	}
	
	private Number getPrice(IStockID id, Date inputDate, boolean enter) {
		IStockData sd = trader.getAccount().getStockData(id);
		Date date = getDateWithDelay(inputDate);
		IStockRecord srAfterDelay = sd.getValue(date);		
		return this.trader.getPrice().getPrice(srAfterDelay, enter);
	}

	@Override
	public void onEndOfDate(IAccount account, Date date) {		
	}

}
