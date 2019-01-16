package ch.pschatzmann.stocks.test;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.pschatzmann.dates.DateRange;
import ch.pschatzmann.stocks.Context;
import ch.pschatzmann.stocks.StockData;
import ch.pschatzmann.stocks.StockID;
import ch.pschatzmann.stocks.accounting.IHistoricValue;
import ch.pschatzmann.stocks.accounting.kpi.DrawDown;
import ch.pschatzmann.stocks.accounting.kpi.DrawDownValue;
import ch.pschatzmann.stocks.accounting.kpi.Return;
import ch.pschatzmann.stocks.accounting.kpi.SharpeRatio;
import ch.pschatzmann.stocks.input.MarketArchiveHttpReader;
import ch.pschatzmann.stocks.utils.Calculations;

/**
 * Tests for KPI and formulas
 * 
 * @author pschatzmann
 *
 */

public class TestCalculation {
	private static final Logger LOG = LoggerFactory.getLogger(TestCalculation.class);
	private Double delta = 0.001;

	@BeforeClass
	public static void setup() throws Exception {
		System.out.println("*** "+TestCalculation.class.getSimpleName()+" ***");

	}

	@Test
	public void testCalculations() {
		StockData sd = stockData();
		List<IHistoricValue> prices = sd.toHistoryValuesClosingPrice().collect(Collectors.toList());

		double avg = Calculations.avg(prices);
		Assert.assertEquals(20.79, avg, delta);
		double sum = Calculations.sum(prices);
		Assert.assertEquals(194520.4692, sum, delta);
		double stddev = Calculations.stddev(prices);
		double stddev1 = Calculations.stddevFast(prices);
		Assert.assertEquals(stddev, stddev1, delta);

	}

	@Test
	public void testCalcAbsReturns() {
		StockData sd = stockData();
		List<IHistoricValue> prices = sd.toHistoryValuesClosingPrice().collect(Collectors.toList());
		List<IHistoricValue> returns = Calculations.getAbsoluteReturns(prices);

		double avg = Calculations.avg(returns);
		Assert.assertEquals(0.019, avg, delta);
		double sum = Calculations.sum(returns);
		Assert.assertEquals(177.9899, sum, delta);

	}

	@Test
	public void testCalcReturns() {
		StockData sd = stockData();
		List<IHistoricValue> prices = sd.toHistoryValuesClosingPrice().collect(Collectors.toList());
		Assert.assertEquals(9356, prices.size());
		List<IHistoricValue> returns = Calculations.getReturns(prices);
		Assert.assertEquals(9355, returns.size());

		double avg = Calculations.avg(returns);
		Assert.assertEquals(0.0010718447366749, avg, delta);
		double sum = Calculations.sum(returns);
		Assert.assertEquals(10.086, sum, delta);
	}

	@Test
	public void testKPIReturns() {
		StockData sd = stockData();
		List<IHistoricValue> prices = sd.toHistoryValuesClosingPrice().collect(Collectors.toList());
		Return r = new Return(prices);

		double percent = r.getPercent();
		double first = prices.get(0).getValue();
		double last = prices.get(prices.size() - 1).getValue();
		double tobe = (last - first) / first * 100;
		Assert.assertEquals(tobe, percent, delta);

		double percentPA = r.getPercentAnnulized();
		Assert.assertEquals(tobe * 252.0 / prices.size(), percentPA, delta);

		double abs = r.getAbsolute();
		Assert.assertEquals(177.9899, abs, delta);

	}

	@Test
	public void testKPIDrawDown() {
		StockData sd = stockData();
		List<IHistoricValue> prices = sd.toHistoryValuesClosingPrice().collect(Collectors.toList());
		DrawDown d = new DrawDown(prices);
		LOG.info("{}",d.getDrawDowns());

		DrawDownValue dv = d.getMaxDrowDown();
		Assert.assertNotNull(dv);
		Assert.assertEquals(81.8015, dv.getPercent(), delta);

		LOG.info("{}",dv);

		DrawDownValue dvShort = d.getShortesDrawDownPeriod();
		LOG.info("{}",dvShort);
		Assert.assertEquals(3.1477, dvShort.getPercent(), delta);
	}

	@Test
	public void testKPISharpRatio() {
		StockData sd = stockData();
		List<IHistoricValue> prices = sd.toHistoryValuesClosingPrice().collect(Collectors.toList());
		double sharpRatio = new SharpeRatio(prices, 0.0).getValue();
		LOG.info("sharpRatio: " + sharpRatio);
		Assert.assertEquals(0.588335, sharpRatio, delta);
	}

	/**
	 * Determine the AAPL stock data between 1980-12-12 - 2016-04-04
	 * 
	 * @return
	 */
	private StockData stockData() {
		StockData sd = (StockData) Context.getStockData(new StockID("AAPL", "NASDAQ"), new MarketArchiveHttpReader());
		sd.filterDates(new DateRange(Context.date("1980-12-12"), Context.date("2016-04-04")));
		return sd;
	}

}
