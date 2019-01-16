package ch.pschatzmann.stocks.strategy;

import java.util.ArrayList;
import java.util.List;

import org.ta4j.core.Indicator;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.HighestValueIndicator;
import org.ta4j.core.indicators.helpers.MultiplierIndicator;
import org.ta4j.core.trading.rules.OverIndicatorRule;

import ch.pschatzmann.stocks.IStockData;
import ch.pschatzmann.stocks.strategy.optimization.InputParameterName;
import ch.pschatzmann.stocks.ta4j.indicator.PriceHistoryIndicator;


/**
 * We buy after a stock was falling for n periods and we sell when the stock
 * falls below the bought price again or after we have a profit for n%
 * 
 * @author pschatzmann
 *
 */
public class BuyOnUpAfterDown extends CommonTradingStrategy  {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public BuyOnUpAfterDown(IStockData stockData) {
		super( stockData);	
		getParameters().input().setValue(InputParameterName.NumberOfTicks, 5, 1, 200, 0);
		getParameters().input().setValue(InputParameterName.LossLimitFactor, 0.9, .5, 1, 0);
		getParameters().input().setValue(InputParameterName.ProfitTakingFactor, 1.25, 1.1, 1.5, 0);
	}

	@Override
	public List<InputParameterName> getParameterOptimizationSequence() {
		return new ArrayList();
	}

	@Override
	public Strategy buildStrategy(TimeSeries timeSeries) {
		Indicator closePrice = new ClosePriceIndicator(timeSeries);
		PriceHistoryIndicator yesterday = new PriceHistoryIndicator(timeSeries,-1);
		Indicator movingAverage = new SMAIndicator(closePrice,this.getInteger(InputParameterName.NumberOfTicks));

		// we buy when stock is down (current price lower then moving average) and then up again
		Rule buyRule = new OverIndicatorRule(closePrice,yesterday).and(new OverIndicatorRule(movingAverage,closePrice));

		//Indicator lastBuyPriceIndicator = new PortfolioAveragePriceIndicator(timeSeries,this.getAccount(),this.getStockData().getStockID());
		//Indicator limitLossIndicator = new MultiplierIndicator(lastBuyPriceIndicator,getDecimal(InputParameterName.LossLimitFactor));
		//Indicator takeProfitIndicator = new MultiplierIndicator(lastBuyPriceIndicator,Decimal.valueOf(1.5));
		Indicator takeProfitIndicator2 = new HighestValueIndicator(closePrice,getInteger(InputParameterName.NumberOfTicks));
		Indicator limitLossIndicator2 = new MultiplierIndicator(takeProfitIndicator2,getDecimal(InputParameterName.ProfitTakingFactor).doubleValue());
		// we sell if we can take a profit or if we need to limit the loss
		//Rule sellRule = new OverIndicatorRule(closePrice,takeProfitIndicator).or(new UnderIndicatorRule(closePrice,limitLossIndicator));//.or(new UnderIndicatorRule(closePrice,limitLossIndicator2)));
		//Rule sellRule = new UnderIndicatorRule(closePrice,limitLossIndicator).or(new UnderIndicatorRule(closePrice,limitLossIndicator2));
		
		return new NamedStrategy(buyRule,null, this.getDescription());
	}


	 
}
