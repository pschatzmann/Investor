package ch.pschatzmann.stocks.strategy.optimization;

import java.io.Serializable;
import java.util.List;

import ch.pschatzmann.dates.DateRange;
import ch.pschatzmann.stocks.accounting.IAccount;
import ch.pschatzmann.stocks.accounting.kpi.KPI;
import ch.pschatzmann.stocks.accounting.kpi.KPIValue;
import ch.pschatzmann.stocks.execution.ITrader;
import ch.pschatzmann.stocks.execution.StrategyExecutor;
import ch.pschatzmann.stocks.parameters.Parameters;
import ch.pschatzmann.stocks.strategy.ITradingStrategy;
import ch.pschatzmann.stocks.strategy.allocation.IAllocationStrategy;
import ch.pschatzmann.stocks.strategy.allocation.SimpleAllocationStrategy;
import ch.pschatzmann.stocks.strategy.selection.SelectionState;

/**
 * The Fitness class is using the trader to execute a trading strategy
 * and calculate the KPIs.
 * 
 * The trading accound is updated by adding the resulting transactions
 * 
 * @author pschatzmann
 *
 */

public class Fitness implements IFitness, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ITrader trader;

	public Fitness(ITrader trader) {
		this.trader = trader;
	}
	
	@Override
	public ITrader getTrader() {
		return trader;
	}
	
	@Override
	public SelectionState getFitness(ITradingStrategy ts, DateRange period) {
		IAccount account =  trader.getAccount();
		ts.reset();
		account.getAccount().reset();
		IAllocationStrategy allocationStrategy = new SimpleAllocationStrategy(trader);

		StrategyExecutor executor = new StrategyExecutor(trader, allocationStrategy);
		executor.addStrategy(ts);
		executor.run(period);

		setResult(account, ts);
		return new SelectionState(ts.getParameters().clone(),ts.getStockData().getStockID(),ts.getName(),false);
	}

	private void setResult(IAccount account, ITradingStrategy ts) {
		if (ts != null) {
			List<KPIValue> result = account.getKPIValues();
			if (result != null) {
				for (KPIValue v : result) {
					Double dv = v.getDoubleValue();
					if (dv != null) {
						Parameters<KPI> p = ts.getParameters().result();
						if (p != null) {
							p.setValue(v.getKpi(), dv);
						}
					}
				}
			}
		}
	}
	
}
