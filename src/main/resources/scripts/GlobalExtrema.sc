/**
 * Scala strategy example
 * @param series
 * @returns {NamedStrategy}
 */
package scripts

import ch.pschatzmann.stocks.strategy._
import org.ta4j.core._
import org.ta4j.core.analysis._
import org.ta4j.core.analysis.criteria,._
import org.ta4j.core.indicators._;
import org.ta4j.core.trading.rules._;
import org.ta4j.core.indicators.helpers._;



def calculate(timeSeries : TimeSeries) : NamedStrategy = {
	
	val closePrices = new ClosePriceIndicator(timeSeries);
	// Getting the max price over the past week
	val maxPrices = new HighPriceIndicator(timeSeries);
	val weekMaxPrice = new HighestValueIndicator(maxPrices, 7);
	// Getting the min price over the past week
	val minPrices = new LowPriceIndicator(timeSeries);
	val weekMinPrice = new LowestValueIndicator(minPrices, 7);
	// Going long if the close price goes below the min price
	val downWeek = new MultiplierIndicator(weekMinPrice, 1.004);
	val buyingRule = new UnderIndicatorRule(closePrices, downWeek);
	// Going short if the close price goes above the max price
	val upWeek = new MultiplierIndicator(weekMaxPrice, 0.996);
	val sellingRule = new OverIndicatorRule(closePrices, upWeek);
	
	val strategy = new NamedStrategy(buyingRule, sellingRule, "GlobalExtremaStrategyJS");
	return strategy;
}

val strategy = calculate(timeSeries.asInstanceOf[TimeSeries]);

