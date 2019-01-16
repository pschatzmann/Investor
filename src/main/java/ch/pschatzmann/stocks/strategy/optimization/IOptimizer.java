package ch.pschatzmann.stocks.strategy.optimization;

import ch.pschatzmann.dates.DateRange;
import ch.pschatzmann.stocks.accounting.kpi.KPI;
import ch.pschatzmann.stocks.parameters.State;

public interface IOptimizer {
	public State optimize(IOptimizableTradingStrategy ts, DateRange optimizationPeriod);
	public IFitness getFitness();
	public KPI getOptimizationParameter();

}
