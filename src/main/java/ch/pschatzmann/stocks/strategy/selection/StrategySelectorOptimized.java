package ch.pschatzmann.stocks.strategy.selection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.pschatzmann.dates.DateRange;
import ch.pschatzmann.stocks.IStockData;
import ch.pschatzmann.stocks.accounting.IAccount;
import ch.pschatzmann.stocks.accounting.IBasicAccount;
import ch.pschatzmann.stocks.accounting.kpi.KPI;
import ch.pschatzmann.stocks.parameters.State;
import ch.pschatzmann.stocks.parameters.StateComparator;
import ch.pschatzmann.stocks.strategy.ITradingStrategy;
import ch.pschatzmann.stocks.strategy.OptimizedStrategy;
import ch.pschatzmann.stocks.strategy.TradingStrategyFactory;
import ch.pschatzmann.stocks.strategy.optimization.IOptimizableTradingStrategy;
import ch.pschatzmann.stocks.strategy.optimization.IOptimizer;
import ch.pschatzmann.stocks.strategy.optimization.SimulatedFitness;

/*
 *  We determine the best strategy for the indicated stock. We use the evaluation of the non optimized
 *  and the optimized versions of all indicated strategies. 
 * 
 */
public class StrategySelectorOptimized implements IStategySelector, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final static Logger LOG = LoggerFactory.getLogger(StrategySelector.class);
	private IBasicAccount account;
	private Collection<String> strategies;
	private DateRange optimizationPeriod;
	private DateRange evaluationPeriod;
	private IOptimizer optimizer;
	private Predicate<SelectionState> predicate = x -> true;

	public StrategySelectorOptimized(IAccount account, Collection<String> strategies, DateRange optimizationPeriod,
			DateRange evaluationPeriod, IOptimizer optimizer) {
		this(account, strategies, optimizationPeriod, evaluationPeriod, optimizer, null);
	}

	public StrategySelectorOptimized(IAccount account, Collection<String> strategies, DateRange optimizationPeriod,
			DateRange evaluationPeriod, IOptimizer optimizer, Predicate<SelectionState> predicate) {
		this.account = account;
		this.strategies = strategies;
		this.optimizationPeriod = optimizationPeriod;
		this.evaluationPeriod = evaluationPeriod;
		this.optimizer = optimizer;
		if (predicate!=null) {
			this.predicate = predicate;
		}
	}

	public Stream<SelectionState> evaluate(IStockData stockData) {
		List<SelectionState> result = new ArrayList();
		for (String strategyName : strategies) {
			try {
				ITradingStrategy strategy = TradingStrategyFactory.create(strategyName, stockData);
				if (stockData.getHistory().size() > 1) {
					StrategySelector.updateAccount(account,stockData);
					State state = new SimulatedFitness(account).getFitness(strategy, evaluationPeriod);
					SelectionState ss = new SelectionState(state, stockData.getStockID(), strategyName, false); 
					if (predicate.test(ss)) {
						result.add(ss);
					}

					if (optimizer != null && strategy instanceof IOptimizableTradingStrategy) {
						ITradingStrategy optimizedStrategy = new OptimizedStrategy(
								(IOptimizableTradingStrategy) strategy, optimizer, optimizationPeriod);
						State stateOptimized = new SimulatedFitness(account).getFitness(optimizedStrategy,
								evaluationPeriod);
						SelectionState ssOpt = new SelectionState(stateOptimized, stockData.getStockID(), strategyName, true);
						if (predicate.test(ssOpt)) {
							result.add(ssOpt);
						}
					}
				}
			} catch (Exception ex) {
				LOG.error("Error for " + stockData.getStockID() + "/" + strategyName + ": " + ex.getMessage());
			}
		}

		return result.stream();
	}

	@Override
	public SelectionState getMax(IStockData stockData) {
		Optional<SelectionState> result = evaluate(stockData).max(new StateComparator(this.getOptimizationParameter()));
		return result.isPresent() ? result.get() : new SelectionState(stockData.getStockID());
	}

	@Override
	public KPI getOptimizationParameter() {
		return this.optimizer.getOptimizationParameter();
	}

	protected Double getValue(State state) {
		return state.getResult().getDouble(this.getOptimizationParameter());
	}

}
