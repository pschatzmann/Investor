package ch.pschatzmann.stocks.test;

import java.util.Date;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;

import ch.pschatzmann.stocks.Context;
import ch.pschatzmann.stocks.IStockData;
import ch.pschatzmann.stocks.StockID;
import ch.pschatzmann.stocks.forecasting.ARIMAForecast;
import ch.pschatzmann.stocks.forecasting.SimulationOnHistoryForecast;
import ch.pschatzmann.stocks.forecasting.IForecast;
import ch.pschatzmann.stocks.input.MarketArchiveHttpReader;
import ch.pschatzmann.stocks.integration.HistoricValues;
import ch.pschatzmann.stocks.integration.StockTimeSeries;

public class TestForecast {
	@BeforeClass
	public static void setup() throws Exception {
		System.out.println("*** "+TestForecast.class.getSimpleName()+" ***");
		Context.setDefaultReader(new MarketArchiveHttpReader());
	}

	
	@Test
	public void ForecasterFromSimulationOnHistory() throws Exception {
		StockID apple = new StockID("AAPL", "NASDAQ");
		IStockData stockdata = Context.getStockData(apple);
		StockTimeSeries series = new StockTimeSeries(stockdata);
		Indicator<Num> cp = new ClosePriceIndicator(series);

		IForecast f = new SimulationOnHistoryForecast(cp, Context.date("2008-01-01"));
		HistoricValues result = f.forecast(20);
		
		result.stream().forEach(r -> System.out.println(r));

	}
	
	@Test
	public void testForecastARIMA() throws Exception {
		StockID apple = new StockID("AAPL", "NASDAQ");
		
		IStockData stockdata = Context.getStockData(apple);
		StockTimeSeries series = new StockTimeSeries(stockdata);
		Indicator<Num> ind = new ClosePriceIndicator(series);

		ARIMAForecast f = new ARIMAForecast(HistoricValues.create(ind));
		HistoricValues  result = f.forecast(new Date());
		
		result.stream().forEach(r -> System.out.println(r));

	}
	

}
