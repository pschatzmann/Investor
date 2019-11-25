package ch.pschatzmann.stocks.test;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.junit.BeforeClass;
import org.junit.Test;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsLowerIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsMiddleIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsUpperIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;

import ch.pschatzmann.stocks.Context;
import ch.pschatzmann.stocks.IStockData;
import ch.pschatzmann.stocks.StockID;
import ch.pschatzmann.stocks.input.MarketArchiveHttpReader;
import ch.pschatzmann.stocks.integration.StockTimeSeries;

public class TestStockIndicators {
	private static DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

	@BeforeClass
	public static void setup() throws Exception {
		System.out.println("*** "+TestStockIndicators.class.getSimpleName()+" ***");
		Context.setDefaultReader(new MarketArchiveHttpReader());
	}

	@Test
	public void test1() throws ParseException {

		StockID apple = new StockID("AAPL", "NASDAQ");
		IStockData stockdata = Context.getStockData(apple, new MarketArchiveHttpReader());
		ClosePriceIndicator closePrice = new ClosePriceIndicator(new StockTimeSeries(stockdata));
		// Bollinger bands
		StandardDeviationIndicator sd = new StandardDeviationIndicator(closePrice, 14);
		SMAIndicator shortSma = new SMAIndicator(closePrice, 8);
		BollingerBandsMiddleIndicator middleBBand = new BollingerBandsMiddleIndicator(shortSma);
		BollingerBandsLowerIndicator lowBBand = new BollingerBandsLowerIndicator(middleBBand, sd);
		BollingerBandsUpperIndicator upBBand = new BollingerBandsUpperIndicator(middleBBand, sd);

		for (int i = lowBBand.getBarSeries().getBeginIndex(); i < lowBBand.getBarSeries().getEndIndex(); i++) {
			System.out.println();
			System.out.print(Context.getValue(lowBBand, i)+" ");
			System.out.print(Context.getValue(middleBBand, i)+ " ");
			System.out.print(Context.getValue(upBBand, i));
		}
		System.out.println();
	}

	@Test
	public void test2() throws ParseException {
		StockID apple = new StockID("AAPL", "NASDAQ");
		IStockData stockdata = Context.getStockData(apple, new MarketArchiveHttpReader());
		ClosePriceIndicator closePrice = new ClosePriceIndicator(new StockTimeSeries(stockdata));

		// Bollinger bands
		StandardDeviationIndicator sd = new StandardDeviationIndicator(closePrice, 14);
		SMAIndicator shortSma = new SMAIndicator(closePrice, 8);
		BollingerBandsMiddleIndicator middleBBand = new BollingerBandsMiddleIndicator(shortSma);
		BollingerBandsLowerIndicator lowBBand = new BollingerBandsLowerIndicator(middleBBand, sd);
		BollingerBandsUpperIndicator upBBand = new BollingerBandsUpperIndicator(middleBBand, sd);


		for (int i = lowBBand.getBarSeries().getBeginIndex(); i < lowBBand.getBarSeries().getEndIndex(); i++) {
			System.out.println();
			System.out.print(Context.getValue(lowBBand, i)+" ");
			System.out.print(Context.getValue(middleBBand, i)+" ");
			System.out.print(Context.getValue(upBBand, i));
		}
		System.out.println();
	}
}
