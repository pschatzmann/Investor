package ch.pschatzmann.stocks.strategy;

import java.util.Date;
import java.util.List;

import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.TradingRecord;

import ch.pschatzmann.stocks.IStockData;
import ch.pschatzmann.stocks.parameters.State;


/**
 * Strategy which uses the ITradingAction to drive the strategy
 * 
 * @author pschatzmann
 *
 */
public class TradingActionStrategy implements Strategy, ITradingStrategy {
	private int unstablePeriod;
	private ITradingAction action;
	private List<Date> dates;
	private State state = new State();
	private IStockData stockData;

	// inline buy rule
	private Rule entryRule = new Rule() {
		@Override
		public boolean isSatisfied(int index, TradingRecord tradingRecord) {
			return action.get(dates.get(index)).equals(TradingAction.buy);
		}
	};

	// inline sell rule
	private Rule exitRule = new Rule() {
		@Override
		public boolean isSatisfied(int index, TradingRecord tradingRecord) {
			return action.get(dates.get(index)).equals(TradingAction.sell);
		}
	};

	/**
	 * Constructor
	 * 
	 * @param model
	 * @param in
	 * @param batchSize
	 * @param normalizer
	 */

	public TradingActionStrategy(IStockData stockData, List<Date> dates, ITradingAction action) {
		this.dates = dates;
		this.action = action;
		this.stockData = stockData;
	}

	@Override
	public Rule getEntryRule() {
		return entryRule;
	}

	@Override
	public Rule getExitRule() {
		return exitRule;
	}

	@Override
	public void setUnstablePeriod(int unstablePeriod) {
		this.unstablePeriod = unstablePeriod;
	}

	@Override
	public boolean isUnstableAt(int index) {
		return index < unstablePeriod;
	}

	@Override
	public Strategy and(Strategy strategy) {
		String andName = "and(" + this.getName() + "," + strategy.getName() + ")";
		int unstable = Math.max(unstablePeriod, strategy.getUnstablePeriod());
		return and(andName, strategy, unstable);
	}

	@Override
	public Strategy or(Strategy strategy) {
		String orName = "or(" + this.getName() + "," + strategy.getName() + ")";
		int unstable = Math.max(unstablePeriod, strategy.getUnstablePeriod());
		return or(orName, strategy, unstable);
	}

	@Override
	public Strategy opposite() {
		return new BaseStrategy("opposite(" + getName() + ")", exitRule, entryRule, unstablePeriod);
	}

	@Override
	public Strategy and(String name, Strategy strategy, int unstablePeriod) {
		return new BaseStrategy(name, entryRule.and(strategy.getEntryRule()), exitRule.and(strategy.getExitRule()),
				unstablePeriod);
	}

	@Override
	public Strategy or(String name, Strategy strategy, int unstablePeriod) {
		return new BaseStrategy(name, entryRule.or(strategy.getEntryRule()), exitRule.or(strategy.getExitRule()),
				unstablePeriod);
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public int getUnstablePeriod() {
		return unstablePeriod;
	}

	@Override
	public Strategy getStrategy() {
		return this;
	}

	@Override
	public IStockData getStockData() {
		return stockData;
	}

	@Override
	public String getDescription() {
		return this.getClass().getSimpleName();
	}

	@Override
	public State getParameters() {
		return this.state;
	}

	@Override
	public void reset() {
	}

	@Override
	public void resetHistory() {
	}

}
