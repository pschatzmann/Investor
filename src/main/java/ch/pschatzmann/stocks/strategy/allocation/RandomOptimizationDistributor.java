package ch.pschatzmann.stocks.strategy.allocation;

import java.util.Collection;

import ch.pschatzmann.dates.DateRange;
import ch.pschatzmann.stocks.accounting.Account;
import ch.pschatzmann.stocks.accounting.IAccount;
import ch.pschatzmann.stocks.accounting.kpi.KPI;
import ch.pschatzmann.stocks.execution.PaperTrader;
import ch.pschatzmann.stocks.execution.StrategyExecutor;
import ch.pschatzmann.stocks.strategy.ITradingStrategy;

/**
 * Finds the best weights by generating n random allocations for the defined
 * period and picks the best option.
 * 
 * @author pschatzmann
 *
 */

public class RandomOptimizationDistributor extends Distributor {
	private static final long serialVersionUID = 1L;
	private double maxKPIValue;
	/**
	 * Default constructor
	 * 
	 * @param period
	 * @param kpi
	 * @param maxCount
	 * @param strategies
	 * @param fees
	 */
	public RandomOptimizationDistributor(IAccount account, DateRange period, KPI kpi, int maxCount,
			Collection<ITradingStrategy> strategies) {
		if (strategies.size()>=1) {
			Account simulationAccount = new Account(account);
			maxKPIValue = -Double.MAX_VALUE;
			RandomDistributor maxDistributor = null;
			for (int j = 0; j < maxCount; j++) {
				PaperTrader trader = new PaperTrader(account);
				RandomDistributor distributor = new RandomDistributor();
				DistributedAllocationStrategy allocationStrategy = new DistributedAllocationStrategy(trader, distributor);
				StrategyExecutor executor = new StrategyExecutor(trader, allocationStrategy);
				strategies.forEach(s -> executor.addStrategy(s));
				executor.run(period);
	
				Double kpiValue = simulationAccount.getKPIValue(kpi);
				if (kpiValue > maxKPIValue) {
					maxKPIValue = kpiValue;
					maxDistributor = distributor;
				}
			}
			if (maxDistributor!=null){
				this.getDistribution().putAll(maxDistributor.getDistribution());
				this.getActualDistribution().putAll(maxDistributor.getDistribution());
			}
			account.reset();
		} 

	}
	
	/**
	 * We provide the KPI value so that we get the possibility to sort and optimize
	 * @return
	 */
	public Double getKPIValue() {
		return this.maxKPIValue;
	}

}
