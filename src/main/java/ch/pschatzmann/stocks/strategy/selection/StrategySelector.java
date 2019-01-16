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
import ch.pschatzmann.stocks.accounting.Account;
import ch.pschatzmann.stocks.accounting.IAccount;
import ch.pschatzmann.stocks.accounting.IBasicAccount;
import ch.pschatzmann.stocks.accounting.kpi.KPI;
import ch.pschatzmann.stocks.parameters.State;
import ch.pschatzmann.stocks.parameters.StateComparator;
import ch.pschatzmann.stocks.strategy.ITradingStrategy;
import ch.pschatzmann.stocks.strategy.TradingStrategyFactory;
import ch.pschatzmann.stocks.strategy.optimization.SimulatedFitness;

/*
 *  We determine the best strategy for the indicated stock. We use the evaluation of the non optimized
 *  versions only of all indicated strategies. 
 * 
 */
public class StrategySelector implements IStategySelector, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final static Logger LOG = LoggerFactory.getLogger(StrategySelectorOptimized.class);
	private IBasicAccount account;
	private Collection<String> strategies;
	private DateRange evaluationPeriod;
	private KPI target;
	private Predicate<SelectionState> predicate;

	/**
	 * No Arg Constructor to support serialization	
	 */
	public StrategySelector(){
		
	}
	
	public StrategySelector(IAccount account, Collection<String> strategies, DateRange optimizationPeriod,
			KPI target) {
		this(account, strategies, optimizationPeriod, target, null);
	}
	
	public StrategySelector(IAccount account, Collection<String> strategies, DateRange evaluationPeriod, KPI target, Predicate<SelectionState> predicate) {
		this.account = account;
		this.strategies = strategies;
		this.evaluationPeriod = evaluationPeriod;
		this.target = target;
		this.predicate = predicate;
	}

	public Stream<SelectionState> evaluate(IStockData stockData) {
		List<SelectionState> result = new ArrayList();
		for (String strategyName : strategies) {
			try {
				ITradingStrategy strategy = TradingStrategyFactory.create(strategyName, stockData);
				if (stockData.getHistory().size() > 1) {
					State state = new SimulatedFitness(account).getFitness(strategy, evaluationPeriod);
					StrategySelector.updateAccount(account,stockData);
					SelectionState resultState = new SelectionState(state, stockData.getStockID(), strategyName, false);
					LOG.info(this.getValue(resultState) +" <- "+resultState);
					if (predicate==null || predicate.test(resultState)) {
						result.add(resultState);
					} else {
						LOG.info("ignored because of predicate");
					}
				}
			} catch (Exception ex) {
				LOG.error("Error for " + stockData.getStockID() + "/" + strategyName + ": " + ex.getMessage(), ex);
			}
		}

		return result.stream();
	}

	protected static void updateAccount(IBasicAccount account2, IStockData stockData) {
		if (account2 instanceof Account) {
			((Account)account2).putStockData(stockData);
		}		
	}

	@Override
	public SelectionState getMax(IStockData stockData) {
		Optional<SelectionState> result = evaluate(stockData).max(new StateComparator(this.getOptimizationParameter()));
		return result.isPresent() ? result.get() : new SelectionState(stockData.getStockID());
	}

	@Override
	public KPI getOptimizationParameter() {
		return target;
	}

	protected Double getValue(State state) {
		return state.getResult().getDouble(this.getOptimizationParameter());
	}

}
