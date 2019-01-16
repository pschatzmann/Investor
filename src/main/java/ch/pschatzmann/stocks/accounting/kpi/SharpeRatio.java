package ch.pschatzmann.stocks.accounting.kpi;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import ch.pschatzmann.stocks.accounting.HistoricValueComparator;
import ch.pschatzmann.stocks.accounting.IHistoricValue;
import ch.pschatzmann.stocks.utils.Calculations;

/**
 * The Sharpe Ratio is a measure for calculating risk-adjusted return, and this
 * ratio has become the industry standard for such calculations. It was
 * developed by Nobel laureate William F. Sharpe. The Sharpe ratio is the
 * average return earned in excess of the risk-free rate per unit of volatility
 * or total risk.
 * 
 * @author pschatzmann
 *
 */

public class SharpeRatio implements IKPICollector, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<IHistoricValue> history;
	private double riskFreeReturnPerDay = 0.0;
	private int tradingDays;

	/**
	 * 
	 * @param historyList
	 * @param riskFreeReturnInPercent
	 */
	public SharpeRatio(List<IHistoricValue> historyList, double riskFreeReturnInPercent) {
		this(historyList, riskFreeReturnInPercent, 252);
	}

	public SharpeRatio(List<IHistoricValue> historyValues, double riskFreeReturnAsPercent, int tradingDays) {
		Collections.sort(historyValues, new HistoricValueComparator());
		Calculations.removeLeadingZeros(historyValues);

		this.history = historyValues;
		this.tradingDays = tradingDays;
		this.riskFreeReturnPerDay = (riskFreeReturnAsPercent / 100) / tradingDays;
	}


	public double getValue() {
		List<IHistoricValue> returns = getRiskExcessDailyReturns();
		return Math.sqrt(tradingDays) * Calculations.avg(returns) / Calculations.stddev(returns);
	}

	private List<IHistoricValue> getRiskExcessDailyReturns() {
		return Calculations.getReturns(history, -riskFreeReturnPerDay);
	}

	@Override
	public void collectKPIValues(Collection<KPIValue> result) {
		result.add(new KPIValue(KPI.SharpeRatio, "Sharp Ratio", getValue()));
		
	}
}
