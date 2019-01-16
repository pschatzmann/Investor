package ch.pschatzmann.stocks.strategy;

import org.ta4j.core.Strategy;

import ch.pschatzmann.stocks.IStockData;
import ch.pschatzmann.stocks.parameters.State;

public interface ITradingStrategy {
	/**
	 * Returns the verdelhan Stategy
	 * @return
	 */
	Strategy getStrategy();

	/**
	 * Returns the history of the stock
	 * @return
	 */
	IStockData getStockData();

	/**
	 * Returns the name of the trading strategy
	 * @return
	 */
	String getName();

	/**
	 * Returns the description of the trading strategy
	 * @return
	 */
	String getDescription();

	/**
	 * Returns the evaluation pararamters
	 * @return
	 */
	State getParameters();

	/**
	 * Reset to trigger a recalculation
	 */
	void reset();

	void resetHistory();
}
