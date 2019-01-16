package ch.pschatzmann.stocks.strategy;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.HighestValueIndicator;
import org.ta4j.core.indicators.helpers.LowestValueIndicator;
import org.ta4j.core.indicators.helpers.MaxPriceIndicator;
import org.ta4j.core.indicators.helpers.MinPriceIndicator;
import org.ta4j.core.indicators.helpers.MultiplierIndicator;
import org.ta4j.core.trading.rules.OverIndicatorRule;
import org.ta4j.core.trading.rules.UnderIndicatorRule;

import ch.pschatzmann.stocks.IStockData;
import ch.pschatzmann.stocks.strategy.optimization.InputParameterName;


/**
 * Strategies which compares current price to global extrema over a week. Going
 * long if the close price goes below the min price Going short if the close
 * price goes above the max price
 */
public class GlobalExtremaStrategy extends CommonTradingStrategy {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(GlobalExtremaStrategy.class);

	public GlobalExtremaStrategy(IStockData stockData) {
		super( stockData);
		getParameters().input().setValue(InputParameterName.NumberOfTicks, 5, 1, 500, 0);
		getParameters().input().setValue(InputParameterName.BuyFactor, 1.004, 1.001, 5.000, 3);
		getParameters().input().setValue(InputParameterName.SellFactor, 0.996, 0.005, 0.999, 3);
	}


	/**
	 * @param series
	 *            a time series
	 * @return a global extrema strategy
	 */
	@Override
	public Strategy buildStrategy(TimeSeries series) {

		ClosePriceIndicator closePrices = new ClosePriceIndicator(series);
		int tickCount = getParameters().input().getInteger(InputParameterName.NumberOfTicks);
		if (tickCount>=series.getMaximumBarCount()) {
			 tickCount = series.getBarCount()-1;
			 getParameters().input().setValue(InputParameterName.NumberOfTicks, tickCount);
		} else if (tickCount<0) {
			tickCount = 1;
			getParameters().input().setValue(InputParameterName.NumberOfTicks, tickCount);
		}

		// Getting the max price over the past week
		MaxPriceIndicator maxPrices = new MaxPriceIndicator(series);
		HighestValueIndicator weekMaxPrice = new HighestValueIndicator(maxPrices, tickCount);
		// Getting the min price over the past week
		MinPriceIndicator minPrices = new MinPriceIndicator(series);
		LowestValueIndicator weekMinPrice = new LowestValueIndicator(minPrices, tickCount);

		// Going long if the close price goes below the min price
		MultiplierIndicator downWeek = new MultiplierIndicator(weekMinPrice, getParameters().input().getDouble(InputParameterName.BuyFactor));
		Rule buyingRule = new UnderIndicatorRule(closePrices, downWeek);

		// Going short if the close price goes above the max price
		MultiplierIndicator upWeek = new MultiplierIndicator(weekMaxPrice, getParameters().input().getDouble(InputParameterName.SellFactor));
		Rule sellingRule = new OverIndicatorRule(closePrices, upWeek);

		return new NamedStrategy(buyingRule, sellingRule, "GlobalExtremaStrategy");
	}

	@Override
	public List<InputParameterName> getParameterOptimizationSequence() {
		return Arrays.asList(InputParameterName.NumberOfTicks,InputParameterName.SellFactor, InputParameterName.BuyFactor);
	}


}
