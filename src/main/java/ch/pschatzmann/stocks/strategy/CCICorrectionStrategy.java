package ch.pschatzmann.stocks.strategy;
/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2016 Marc de Verdelhan & respective authors (see AUTHORS)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import java.util.Arrays;
import java.util.List;

import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.CCIIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.trading.rules.OverIndicatorRule;
import org.ta4j.core.trading.rules.UnderIndicatorRule;

import ch.pschatzmann.stocks.Context;
import ch.pschatzmann.stocks.IStockData;
import ch.pschatzmann.stocks.strategy.optimization.InputParameterName;

/**
 * CCI Correction Strategy
 * <p>
 * 
 * see http://stockcharts.com/school/doku.php?id=chart_school:trading_strategies:cci_correction
 */
public class CCICorrectionStrategy extends CommonTradingStrategy {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public CCICorrectionStrategy(IStockData stockData) {
		super(stockData);

		getParameters().input().setValue(InputParameterName.ShortCCIPeriod, 5, 1, 100, 0);
		getParameters().input().setValue(InputParameterName.LongCCIPeriod, 200, 100, 300, 0);
		getParameters().input().setValue(InputParameterName.Signal, 100, 90, 110, 0);

	}

	/**
	 * @param series
	 *            a time series
	 * @return a CCI correction strategy
	 */
	@Override
	public Strategy buildStrategy(BarSeries series) {
		if (series == null) {
			throw new IllegalArgumentException("Series cannot be null");
		}

		CCIIndicator shortCci = new CCIIndicator(series, getInteger(InputParameterName.ShortCCIPeriod));
		CCIIndicator longCci = new CCIIndicator(series, getInteger(InputParameterName.LongCCIPeriod));
		Num plus100 = Context.number(100.0);
		Num minus100 = Context.number(-getInteger(InputParameterName.Signal));

		Rule entryRule = new OverIndicatorRule(longCci, plus100) // Bull trend
				.and(new UnderIndicatorRule(shortCci, minus100)); // Signal

		Rule exitRule = new UnderIndicatorRule(longCci, minus100) // Bear trend
				.and(new OverIndicatorRule(shortCci, plus100)); // Signal

		Strategy strategy = new NamedStrategy(entryRule, exitRule, "CCICorrectionStrategy");
		strategy.setUnstablePeriod(getInteger(InputParameterName.ShortCCIPeriod));
		return strategy;
	}

	@Override
	public List<InputParameterName> getParameterOptimizationSequence() {
		return Arrays.asList(InputParameterName.ShortCCIPeriod, InputParameterName.LongCCIPeriod,
				InputParameterName.Signal);
	}

}
