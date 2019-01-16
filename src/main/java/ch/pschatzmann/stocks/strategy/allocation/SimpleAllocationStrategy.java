package ch.pschatzmann.stocks.strategy.allocation;

import java.io.Serializable;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.pschatzmann.stocks.IStockData;
import ch.pschatzmann.stocks.IStockID;
import ch.pschatzmann.stocks.IStockRecord;
import ch.pschatzmann.stocks.accounting.IAccount;
import ch.pschatzmann.stocks.execution.ITrader;
import ch.pschatzmann.stocks.execution.fees.IFeesModel;
import ch.pschatzmann.stocks.strategy.ITradingStrategy;
import ch.pschatzmann.stocks.utils.Calculations;

/**
 * We buy as much stocks as possible with the available cash just keeping a
 * miniumum reserve. A buy signal is not executed if there is not enough
 * cash
 * 
 * @author pschatzmann
 *
 */

public class SimpleAllocationStrategy implements IAllocationStrategy, Serializable {
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(SimpleAllocationStrategy.class);
	private double cashReserve = 0;
	private ITrader trader;

	public SimpleAllocationStrategy(ITrader trader) {
		this.trader = trader;
	}

	@Override
	public Long onBuy(IAccount account, IStockRecord sr, ITradingStrategy strategy) {
		// determine the buy quantity
		long result = 0;
		Long currentQuantity = getCurrentQuantity(account, sr);
		if (currentQuantity==0) {
			double currentCash = account.getCash(sr.getDate());
			Number price = this.getPrice(sr.getStockID(), sr.getDate(), true);
			result = getBuyQty(currentCash, account.getAccount().getFeesModel(), price.doubleValue());
		}
		LOG.debug("buy signal  -> " + result);
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
		Long qty =  account.getQuantity(sr.getStockID());
		return qty;
	}


	protected long getBuyQty(double value, IFeesModel fees, double price) {
		long result;
		double quantity = value / price;
		double extimatedFees = fees.getFeesPerTrade(quantity, value);
		result = Calculations.toLong((value  - extimatedFees) / price);
		result = Math.max(0L, result);
		return result;
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
		Date date = getDateWithDelay(inputDate);
		IStockData sd = this.trader.getAccount().getStockData(id);
		IStockRecord srAfterDelay = sd.getValue(date);		
		return this.trader.getPrice().getPrice(srAfterDelay, enter);
	}

	@Override
	public void onEndOfDate(IAccount account, Date date) {
	}
}
