package ch.pschatzmann.analysis.criteria;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.ta4j.core.TimeSeries;
import org.ta4j.core.Trade;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.analysis.criteria.AbstractAnalysisCriterion;
import org.ta4j.core.num.Num;

import ch.pschatzmann.stocks.Context;
import ch.pschatzmann.stocks.accounting.HistoricValue;
import ch.pschatzmann.stocks.accounting.IHistoricValue;
import ch.pschatzmann.stocks.utils.Calculations;

/**
 * Calculates the absolute realized returns and unrealized profits for open
 * trades
 * 
 * @author pschatzmann
 *
 */
public class SharpeRatioCriterion extends AbstractAnalysisCriterion implements Serializable  {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private double riskFreeReturnPerDay;
	private List<IHistoricValue> returns = new ArrayList();
	private int tradingDays = 252;

	public SharpeRatioCriterion(double riskFreeReturnAsPercent, int tradingDays) {
		this.tradingDays = tradingDays;
		this.riskFreeReturnPerDay = (riskFreeReturnAsPercent / 100) / tradingDays;
	}

	@Override
	public Num calculate(TimeSeries series, TradingRecord tradingRecord) {
		for (Trade trade : tradingRecord.getTrades()) {
			ZonedDateTime date = series.getBar(trade.getEntry().getIndex()).getEndTime();
			returns.add(new HistoricValue(date, calculateProfit(series, trade) - riskFreeReturnPerDay));
		}
		return Context.number(Math.sqrt(tradingDays) * Calculations.avg(returns) / Calculations.stddev(returns));
	}

	@Override
	public Num calculate(TimeSeries series, Trade trade) {
		return Context.number(calculateProfit(series, trade));
	}

	@Override
	public boolean betterThan(Num criterionValue1, Num criterionValue2) {
		return criterionValue1.isGreaterThan(criterionValue2);
	}

	/**
	 * Calculates the profit of a trade (Buy and sell).
	 * 
	 * @param series
	 *            a time series
	 * @param trade
	 *            a trade
	 * @return the profit of the trade
	 */
	private double calculateProfit(TimeSeries series, Trade trade) {
		Num profit = Context.number(0.0);
		if (trade.isClosed()) {
			Num exitClosePrice = series.getBar(trade.getExit().getIndex()).getClosePrice();
			Num entryClosePrice = series.getBar(trade.getEntry().getIndex()).getClosePrice();
			profit = exitClosePrice.minus(entryClosePrice).dividedBy(entryClosePrice);
		} else {
			Num currentPrice = series.getLastBar().getClosePrice();
			Num entryClosePrice = series.getBar(trade.getEntry().getIndex()).getClosePrice();
			profit = entryClosePrice.minus(currentPrice).dividedBy(entryClosePrice);
		}
		return profit.doubleValue();
	}
}