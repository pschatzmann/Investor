package ch.pschatzmann.stocks.accounting.kpi;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import ch.pschatzmann.stocks.accounting.HistoricValueComparator;
import ch.pschatzmann.stocks.accounting.IHistoricValue;
import ch.pschatzmann.stocks.utils.Calculations;

/**
 * A return is the gain or loss of a security in a particular period. The return
 * consists of the income and the capital gains relative on an investment. It is
 * usually quoted as a percentage.
 * 
 * @author pschatzmann
 *
 */
public class Return implements IKPICollector, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<IHistoricValue> history;
	private double firstValue;
	private double lastValue;

	public Return(List<IHistoricValue> historyList) {
		Collections.sort(historyList, new HistoricValueComparator());
		//Calculations.removeLeadingZeros(valueHistory);
		this.history = historyList;
		if (!historyList.isEmpty()) {
			firstValue = historyList.get(0).getValue();
			lastValue = historyList.get(historyList.size() - 1).getValue();
		}
	}

	public double getPercent() {
		return (lastValue - firstValue) / firstValue * 100;
	}

	public double getPercentAnnulized() {
		return getPercentAnnulized(252);
	}

	public double getPercentAnnulized(int tradingDays) {
		return getPercent() / history.size() * tradingDays;
	}

	public double getAbsolute() {
		return lastValue - firstValue;
	}

	public double getStdDevOfPercent() {
		Collection<IHistoricValue> returns = Calculations.getReturns(history);
		return Calculations.stddev(returns);
	}

	public double getStdDevOfAbsolute() {
		Collection<IHistoricValue> returns = Calculations.getAbsoluteReturns(history);
		return Calculations.stddev(returns);
	}

	public double getAvgOfAboluteProfit() {
		Collection<IHistoricValue> returns = Calculations.getAbsoluteReturns(history);
		return Calculations.avg(returns);
	}

	@Override
	public void collectKPIValues(Collection<KPIValue> result) {
		result.add(new KPIValue(KPI.AbsoluteReturn, "Absolute Return", this.getAbsolute()));
		result.add(new KPIValue(KPI.AbsoluteReturnAveragePerDay, "Absolute Return Average per day", this.getAvgOfAboluteProfit()));
		result.add(new KPIValue(KPI.AbsoluteReturnStdDev, "Absolute Return StdDev", this.getStdDevOfAbsolute()));
		result.add(new KPIValue(KPI.ReturnPercent, "Return %", this.getPercent()));
		result.add(new KPIValue(KPI.ReturnPercentAnualized, "Return % per year", this.getPercentAnnulized()));
		result.add(new KPIValue(KPI.ReturnPercentStdDev, "Return % StdDev", this.getStdDevOfPercent()));
	}

}
