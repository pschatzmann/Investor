package ch.pschatzmann.stocks.strategy;

import java.util.ArrayList;
import java.util.List;

import org.ta4j.core.Strategy;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.trading.rules.BooleanRule;

import ch.pschatzmann.stocks.IStockData;
import ch.pschatzmann.stocks.strategy.optimization.InputParameterName;


/**
 * We buy the title when the account is opened and hold the stock. This strategy
 * can be used as a baseline to compare the other strategies.
 * 
 * @author pschatzmann
 *
 */
public class BuyAndHoldStrategy extends CommonTradingStrategy  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public BuyAndHoldStrategy(IStockData stockData) {
		super(stockData);
	}

	@Override
	public List<InputParameterName> getParameterOptimizationSequence() {
		return new ArrayList();
	}

	@Override
	public Strategy buildStrategy(TimeSeries timeSeries) {
		return new NamedStrategy(new BooleanRule(true), new BooleanRule(false),"BuyAndHoldStrategy");
	}


}
