/**
 * Javascript strategy example
 * @param series
 * @returns {NamedStrategy}
 */

function execute(series) {
	var imports = new JavaImporter(
			Packages.ch.pschatzmann.stocks.strategy,
			Packages.org.ta4j.core,
			Packages.org.ta4j.core.num,
			Packages.org.ta4j.core.analysis,
			Packages.org.ta4j.core.analysis.criteria,
			Packages.org.ta4j.core.indicators,
			Packages.org.ta4j.core.trading.rules,
			Packages.org.ta4j.core.indicators.helpers);

	with (imports) {
		closePrices = new ClosePriceIndicator(series);
		// Getting the max price over the past week
		maxPrices = new HighPriceIndicator(series);
		weekMaxPrice = new HighestValueIndicator(maxPrices, 7);
		// Getting the min price over the past week
		minPrices = new LowPriceIndicator(series);
		weekMinPrice = new LowestValueIndicator(minPrices, 7);
		// Going long if the close price goes below the min price
		downWeek = new MultiplierIndicator(weekMinPrice, 1.004);
		buyingRule = new UnderIndicatorRule(closePrices, downWeek);
		// Going short if the close price goes above the max price
		upWeek = new MultiplierIndicator(weekMaxPrice, 0.996);
		sellingRule = new OverIndicatorRule(closePrices, upWeek);

		strategy = new NamedStrategy(buyingRule, sellingRule, "GlobalExtremaStrategyJS");
		return strategy;
	}
}