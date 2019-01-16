package ch.pschatzmann.stocks.strategy.selection;

import ch.pschatzmann.stocks.IStockData;
import ch.pschatzmann.stocks.accounting.kpi.KPI;

public interface IStategySelector {
	public SelectionState getMax(IStockData stockData);

	public KPI getOptimizationParameter();

}
