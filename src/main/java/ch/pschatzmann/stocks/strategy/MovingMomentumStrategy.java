package ch.pschatzmann.stocks.strategy;

import java.util.Arrays;
import java.util.List;

import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.StochasticOscillatorKIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.trading.rules.CrossedDownIndicatorRule;
import org.ta4j.core.trading.rules.CrossedUpIndicatorRule;
import org.ta4j.core.trading.rules.OverIndicatorRule;
import org.ta4j.core.trading.rules.UnderIndicatorRule;

import ch.pschatzmann.stocks.Context;
import ch.pschatzmann.stocks.IStockData;
import ch.pschatzmann.stocks.strategy.optimization.InputParameterName;


/**
 * Moving momentum strategy.
 * <p>
 * 
 * see http://stockcharts.com/help/doku.php?id=chart_school:trading_strategies:
 *      moving_momentum
 */
public class MovingMomentumStrategy extends CommonTradingStrategy  {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MovingMomentumStrategy(IStockData stockData) {
		super(stockData);
		
		getParameters().input().setValue(InputParameterName.ShortEMAPeriod, 9, 1, 20, 0);
		getParameters().input().setValue(InputParameterName.LongEMAPeriod, 26, 20, 40, 0);
		getParameters().input().setValue(InputParameterName.StochasticOscillatorKIndicator, 14, 7, 21, 0);
		getParameters().input().setValue(InputParameterName.SignalEMA, 18, 16, 20, 0);

		getParameters().input().setValue(InputParameterName.EntrySignal, 20, 10, 30, 0);
		getParameters().input().setValue(InputParameterName.ExitSignal, 80, 70, 90, 0);
	}

	/**
	 * @param series
	 *            a time series
	 * @return a moving momentum strategy
	 */
	@Override
	public Strategy buildStrategy(BarSeries series) {
		if (series == null) {
			throw new IllegalArgumentException("Series cannot be null");
		}

		ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

		// The bias is bullish when the shorter-moving average moves above the
		// longer moving average.
		// The bias is bearish when the shorter-moving average moves below the
		// longer moving average.
		EMAIndicator shortEma = new EMAIndicator(closePrice, getInteger(InputParameterName.ShortEMAPeriod));
		EMAIndicator longEma = new EMAIndicator(closePrice, getInteger(InputParameterName.LongEMAPeriod));

		StochasticOscillatorKIndicator stochasticOscillK = new StochasticOscillatorKIndicator(series, getInteger(InputParameterName.StochasticOscillatorKIndicator));

		MACDIndicator macd = new MACDIndicator(closePrice, getInteger(InputParameterName.ShortEMAPeriod), getInteger(InputParameterName.LongEMAPeriod));
		EMAIndicator emaMacd = new EMAIndicator(macd, getInteger(InputParameterName.SignalEMA));

		// Entry rule
		Rule entryRule = new OverIndicatorRule(shortEma, longEma) // Trend
				.and(new CrossedDownIndicatorRule(stochasticOscillK, Context.number(getDouble(InputParameterName.EntrySignal)))) 
				.and(new OverIndicatorRule(macd, emaMacd)); // Signal 2

		// Exit rule
		Rule exitRule = new UnderIndicatorRule(shortEma, longEma) // Trend
				.and(new CrossedUpIndicatorRule(stochasticOscillK, Context.number((getDouble(InputParameterName.ExitSignal)))) 
				.and(new UnderIndicatorRule(macd, emaMacd))); // Signal 2

		return new NamedStrategy(entryRule, exitRule, this.getName());
	}

	@Override
	public List<InputParameterName> getParameterOptimizationSequence() {
		return Arrays.asList(InputParameterName.EntrySignal,InputParameterName.EntrySignal,InputParameterName.ShortEMAPeriod, InputParameterName.LongEMAPeriod,InputParameterName.StochasticOscillatorKIndicator,InputParameterName.SignalEMA );
	}


}
