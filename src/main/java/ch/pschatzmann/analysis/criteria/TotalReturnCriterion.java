package ch.pschatzmann.analysis.criteria;

import java.io.Serializable;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Trade;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.analysis.criteria.AbstractAnalysisCriterion;
import org.ta4j.core.num.Num;

import ch.pschatzmann.stocks.Context;

;

/**
 * Calculates the absolute realized returns and unrealized profits for open
 * trades
 * 
 * @author pschatzmann
 *
 */
public class TotalReturnCriterion extends AbstractAnalysisCriterion implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public Num calculate(BarSeries series, TradingRecord tradingRecord) {
		Num value = Context.number(0.0);
		for (Trade trade : tradingRecord.getTrades()) {
			value.plus(calculateProfit(series, trade));
		}
		return value;
	}

	@Override
	public Num calculate(BarSeries series, Trade trade) {
		return calculateProfit(series, trade);
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
	private Num calculateProfit(BarSeries series, Trade trade) {
		Num profit = Context.number(0.0);
		if (trade.isClosed()) {
			Num exitClosePrice = series.getBar(trade.getExit().getIndex()).getClosePrice();
			Num entryClosePrice = series.getBar(trade.getEntry().getIndex()).getClosePrice();
			profit = exitClosePrice.minus(entryClosePrice).multipliedBy(trade.getExit().getAmount());
		} else {
			Num currentPrice = series.getLastBar().getClosePrice();
			Num entryClosePrice = series.getBar(trade.getEntry().getIndex()).getClosePrice();
			profit = entryClosePrice.minus(currentPrice).multipliedBy(trade.getEntry().getAmount());
		}
		return profit;
	}
}