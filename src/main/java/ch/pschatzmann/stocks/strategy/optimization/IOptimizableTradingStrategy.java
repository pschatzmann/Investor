package ch.pschatzmann.stocks.strategy.optimization;

import java.util.List;

import ch.pschatzmann.stocks.parameters.State;
import ch.pschatzmann.stocks.strategy.ITradingStrategy;

public interface IOptimizableTradingStrategy extends ITradingStrategy {
	/**
	 * Determines the input and output parameters
	 * @return
	 */
	@Override
	State getParameters();
	/**
	 * Sets the input and output parameters
	 * @param state
	 */
	void setParameters(State state);
	/**
	 * Returns the sequence in which the parameters are optimized
	 * @return
	 */
	List<InputParameterName> getParameterOptimizationSequence();
	
	
	/**
	 * Trirggers strategy recalculation after e.g. parameters have been updated
	 */
	@Override
	public void reset();

	/**
	 * Triggers a strategy recalculation with new history data
	 */
	@Override
	public void resetHistory();


}
