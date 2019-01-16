package ch.pschatzmann.stocks.strategy;


import java.util.Arrays;
import java.util.List;

import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.trading.rules.CrossedDownIndicatorRule;
import org.ta4j.core.trading.rules.CrossedUpIndicatorRule;
import org.ta4j.core.trading.rules.OverIndicatorRule;
import org.ta4j.core.trading.rules.UnderIndicatorRule;

import ch.pschatzmann.stocks.Context;
import ch.pschatzmann.stocks.IStockData;
import ch.pschatzmann.stocks.strategy.optimization.InputParameterName;


/**
 * 2-Period RSI Strategy
 * 
 * see http://stockcharts.com/school/doku.php?id=chart_school:trading_strategies:rsi2
 */
public class RSI2Strategy extends CommonTradingStrategy  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public RSI2Strategy(IStockData stockData) {
		super( stockData);
		getParameters().input().setValue(InputParameterName.ShortSMAPeriod, 5, 2, 10, 0);
		getParameters().input().setValue(InputParameterName.LongSMAPeriod, 200, 100, 300, 0);
		getParameters().input().setValue(InputParameterName.RSIPeriod, 2, 1, 5, 0);
		getParameters().input().setValue(InputParameterName.EntryLimit,5, 2, 10, 0);
		getParameters().input().setValue(InputParameterName.ExitLimit, 95, 85, 98, 0);

	}

    /**
     * @param series a time series
     * @return a 2-period RSI strategy
     */
    @Override
    public Strategy buildStrategy(TimeSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        SMAIndicator shortSma = new SMAIndicator(closePrice, getInteger(InputParameterName.ShortSMAPeriod));
        SMAIndicator longSma = new SMAIndicator(closePrice, getInteger(InputParameterName.LongSMAPeriod));

        // We use a 2-period RSI indicator to identify buying
        // or selling opportunities within the bigger trend.
        RSIIndicator rsi = new RSIIndicator(closePrice, getInteger(InputParameterName.RSIPeriod));
        
        // Entry rule
        // The long-term trend is up when a security is above its 200-period SMA.
        Rule entryRule = new OverIndicatorRule(shortSma, longSma) // Trend
                .and(new CrossedDownIndicatorRule(rsi, Context.number(getDouble(InputParameterName.EntryLimit)))) // Signal 1
                .and(new OverIndicatorRule(shortSma, closePrice)); // Signal 2
        
        // Exit rule
        // The long-term trend is down when a security is below its 200-period SMA.
        Rule exitRule = new UnderIndicatorRule(shortSma, longSma) // Trend
                .and(new CrossedUpIndicatorRule(rsi, Context.number(getDouble(InputParameterName.ExitLimit)))) // Signal 1
                .and(new UnderIndicatorRule(shortSma, closePrice)); // Signal 2
        
        
        return new NamedStrategy(entryRule, exitRule,"RSI2Strategy");
    }

	@Override
	public List<InputParameterName> getParameterOptimizationSequence() {
		return Arrays.asList(InputParameterName.EntryLimit,InputParameterName.ExitLimit,InputParameterName.ShortSMAPeriod, InputParameterName.LongSMAPeriod,InputParameterName.RSIPeriod);
	}
    
}
