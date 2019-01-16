package ch.pschatzmann.stocks.execution;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.Bar;
import org.ta4j.core.Strategy;

import ch.pschatzmann.dates.DateRange;
import ch.pschatzmann.stocks.Context;
import ch.pschatzmann.stocks.IStockData;
import ch.pschatzmann.stocks.IStockRecord;
import ch.pschatzmann.stocks.StockRecord;
import ch.pschatzmann.stocks.accounting.IAccount;
import ch.pschatzmann.stocks.accounting.Transaction;
import ch.pschatzmann.stocks.errors.DateException;
import ch.pschatzmann.stocks.integration.StockBar;
import ch.pschatzmann.stocks.strategy.ITradeEvent;
import ch.pschatzmann.stocks.strategy.ITradingStrategy;
import ch.pschatzmann.stocks.strategy.allocation.DistributedAllocationStrategy;
import ch.pschatzmann.stocks.strategy.allocation.IAllocationStrategy;

/**
 * Translates a trading strategy into buy and sell orders. This version is based
 * on the buy and sell signals from ta4j
 * 
 * @author pschatzmann
 *
 */

public class StrategyExecutor implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(StrategyExecutor.class);
	private Map<IStockData, ITradingStrategy> strategies = Collections.synchronizedMap(new HashMap());
	private IAllocationStrategy allocationStrategy;
	private ITrader trader;
	private Date lastDate;
	private List<IStockData> discontinuedStocks = new ArrayList();
	private boolean isImmediateLiquidationOfDiscontinuedStocks = false;
	private boolean hasValidTicks = false;

	/**
	 * Simple Constructor
	 * 
	 * @param trader
	 */
	public StrategyExecutor(ITrader trader) {
		allocationStrategy = new DistributedAllocationStrategy(trader);
		this.trader = trader;
	}

	/**
	 * Default constructor
	 * 
	 * @param account
	 * @param allocationStrategy
	 * @param trader
	 */
	public StrategyExecutor(ITrader trader, IAllocationStrategy allocationStrategy) {
		this.allocationStrategy = allocationStrategy;
		this.trader = trader;
	}

	/**
	 * Adds a new trading strategy
	 * 
	 * @param strategy
	 * @param dateRange
	 */

	public void addStrategy(ITradingStrategy strategy) {
		strategies.put(strategy.getStockData(), strategy);
		this.trader.getAccount().putStockData(strategy.getStockData());
	}

	/**
	 * Adds multiple strategies
	 * 
	 * @param strategies
	 */
	public void addStrategy(Collection<ITradingStrategy> strategies) {
		strategies.stream().forEach(strategy -> addStrategy(strategy));
	}

	/**
	 * Adds multiple strategies
	 * 
	 * @param strategies
	 */
	public void addStrategy(ITradingStrategy ... strategies) {
		 addStrategy(Arrays.asList(strategies));
	}

	
	/**
	 * Defines the list of the actual allowed trading strategies. The existing old
	 * strategies are still evaluated to determine the final sell signal. Old
	 * strategies will however not get any buy signal
	 * 
	 * @param strategies
	 */
	public void setStrategies(Collection<ITradingStrategy>... listOfList) {
		Collection<ITradingStrategy> combinedList = new ArrayList();
		for (Collection<ITradingStrategy> list : listOfList) {
			combinedList.addAll(list);
		}
		setupStocksToLiquidate(combinedList);
		combinedList.stream().forEach(strategy -> addStrategy(strategy));
	}

	protected void setupStocksToLiquidate(Collection<ITradingStrategy> newStrategies) {
		List<IStockData> stocksToLiquidate = new ArrayList(this.strategies.keySet());
		List<IStockData> newStocks = strategies.entrySet().stream().map(s -> s.getValue().getStockData())
				.collect(Collectors.toList());
		stocksToLiquidate.removeAll(newStocks);
		// list of stocks that are not to be purchased
		discontinuedStocks = stocksToLiquidate;
	}

	/**
	 * Returns true if no strategies are available
	 * 
	 * @return
	 */
	public boolean isEmpty() {
		return this.strategies.isEmpty();
	}

	/**
	 * Execute the strategies for the indicated period
	 * 
	 * @param period
	 */
	public void run(DateRange period) {
		LOG.debug("run {}", period.toString());
		hasValidTicks = false;
		
		if (this.allocationStrategy instanceof IExecutorAware) {
			((IExecutorAware) allocationStrategy).setExecutor(this);
		}

		Set<TradingTick> records = new TreeSet();
		for (ITradingStrategy o : this.getTradingStrategies()) {
			int index = 0;
			// make sure that we have a strategy setup up
			o.getStrategy();
			for (IStockRecord r : o.getStockData().getHistory()) {
				try {
					if (r.isValid()) {
						LOG.debug("-> {}", r);
						records.add(new TradingTick(r.getDate(), o, r, index));
						index++;
					}
				} catch (Exception ex) {
					LOG.error("Error in the evaluation " + ex, ex);
				}
			}
		}

		try {
			run(records, period);
		} catch (Exception ex) {
			LOG.error("Error in the evaluation " + ex, ex);
		}
		
		if (!hasValidTicks) {
			LOG.error("No valid stock ticks were available - check your date range!");
		}

	}

	protected Date getMinDate() throws DateException {
		Date min = Context.getDates().get(0).getStart();
		for (DateRange dr : Context.getDates()) {
			if (dr.getStart().before(min)) {
				min = dr.getStart();
			}
		}
		return min;
	}

	protected Date getMaxDate() throws DateException {
		Date max = Context.getDates().get(0).getEnd();
		for (DateRange dr : Context.getDates()) {
			if (dr.getEnd().after(max)) {
				max = dr.getEnd();
			}
		}
		return max;
	}

	/**
	 * Execute the trades for the indicated stock
	 * 
	 * @param stockData
	 */

	protected void run(Set<TradingTick> records, DateRange period) {
		LOG.debug("run");
		for (TradingTick tt : records) {
			try {
				if (onDateChange(tt.date)) {
					allocationStrategy.onEndOfDate(this.getAccount(), tt.date);
					trader.execute();
				}
				runTradingTick(tt, period);
			} catch (Exception ex) {
				LOG.error(ex.getLocalizedMessage(), ex);
			}
		}
		allocationStrategy.onEndOfDate(this.getAccount(), this.lastDate);
		trader.execute();

	}

	protected boolean onDateChange(Date date) {
		boolean result = lastDate != null && !date.equals(lastDate);
		lastDate = date;
		return result;
	}

	protected void runTradingTick(TradingTick tt, DateRange period) {
		Date date = Date.from(tt.tick.getEndTime().toInstant());
		if (LOG.isDebugEnabled()) {
			LOG.debug(tt.tradingStrategy.getStrategy() + " index " + tt.index + " " + date);
		}
		if (period == null || period.isValid(date)) {
			// give optimizing strategies the chance to run an optization
			if (tt.tradingStrategy instanceof ITradeEvent) {
				((ITradeEvent) tt.tradingStrategy).beforeTrade(date);
			}
			executeTick(tt.tradingStrategy, tt.tradingStrategy.getStockData(), trader.getAccount(), tt.tick, tt.index);
			hasValidTicks = true;
		} else {
			LOG.debug("-> is not valid");
		}
	}

	protected void executeTick(ITradingStrategy tradingStrategy, IStockData stockData, IAccount account, Bar newTick,
			int index) {
		IStockRecord sr = new StockRecord(newTick, stockData.getStockID(), index);

		Strategy strategy = tradingStrategy.getStrategy();
		boolean shouldEnter = false;
		boolean shouldExit = false;
		try {
			if (!strategy.isUnstableAt(index)) {
				shouldEnter = strategy.shouldEnter(index);
				// we will not buy stocks which are discontinued
				if (shouldEnter) {
					if (isDiscontinued(stockData)) {
						shouldEnter = false;
					}
				}

				shouldExit = strategy.shouldExit(index);
				// we will liquidate it immediately if the flag is set
				if (this.isImmediateLiquidationOfDiscontinuedStocks && isDiscontinued(stockData)) {
					shouldExit = true;
				}
			}
		} catch (Exception ex) {
			LOG.error(ex.getLocalizedMessage(), ex);
		}

		if (shouldEnter) {
			LOG.debug(tradingStrategy + " " + index + " -> should enter");
			// Entering...
			Long qty = allocationStrategy.onBuy(account, sr, tradingStrategy);
			LOG.debug(tradingStrategy + " " + index + " -> enter with qty " + qty);
			if (qty != 0L) {
				account.getAccount().addTransaction(new Transaction(sr.getDate(), stockData.getStockID(), qty));
			}

		} else if (shouldExit) {
			LOG.debug(tradingStrategy + " " + index + " -> should exit");
			// Exiting...
			Long qty = allocationStrategy.onSell(account, sr, tradingStrategy);
			if (qty != 0L) {
				// sell full quantity
				account.getAccount().addTransaction(new Transaction(sr.getDate(), stockData.getStockID(), -qty));
			}
		} else {
			LOG.debug(tradingStrategy + " " + index + " -> no action");

		}
	}

	protected boolean isDiscontinued(IStockData stockData) {
		return this.discontinuedStocks.contains(stockData);
	}

	/**
	 * Return all trading strategies
	 * 
	 * @return
	 */
	public Collection<ITradingStrategy> getTradingStrategies() {
		return this.strategies.values();
	}

	/**
	 * Returns the account
	 * 
	 * @return
	 */

	public IAccount getAccount() {
		return this.trader.getAccount();
	}

	/**
	 * If true the system will not wait for the next sell signal but will sell
	 * immediatly
	 * 
	 * @return
	 */
	public boolean isImmediateLiquidationOfDiscontinuedStocks() {
		return isImmediateLiquidationOfDiscontinuedStocks;
	}

	/**
	 * Defines that discontinued stocks should be liquidated at the beginning of the
	 * execution
	 * 
	 * @param isImmediateLiquidationOfDiscontinuedStocks
	 */
	public void setImmediateLiquidationOfDiscontinuedStocks(boolean isImmediateLiquidationOfDiscontinuedStocks) {
		this.isImmediateLiquidationOfDiscontinuedStocks = isImmediateLiquidationOfDiscontinuedStocks;
	}

	/**
	 * Tick data sorted by dates
	 * 
	 * @author pschatzmann
	 *
	 */
	protected class TradingTick implements Comparable<TradingTick> {
		private Date date;
		private Bar tick;
		private ITradingStrategy tradingStrategy;
		private IStockRecord stockRecord;
		private int index;

		TradingTick(Date date, ITradingStrategy eo, IStockRecord stockRecord, int index) {
			this.date = date;
			this.tradingStrategy = eo;
			this.stockRecord = stockRecord;
			this.tick = new StockBar(stockRecord);
			this.index = index;
		}

		@Override
		public int compareTo(TradingTick o) {
			int result = date.compareTo(o.date);
			if (result == 0) {
				result = tradingStrategy.getStockData().getStockID()
						.compareTo(o.tradingStrategy.getStockData().getStockID());
			}
			return result;
		}

		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append(Context.format(date));
			sb.append(" ");
			sb.append(tradingStrategy.getStockData());
			sb.append(" ");
			sb.append(index);
			return sb.toString();
		}
	}

}
