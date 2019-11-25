package ch.pschatzmann.stocks.test;

import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.pschatzmann.dates.DateRange;
import ch.pschatzmann.stocks.Context;
import ch.pschatzmann.stocks.IStockData;
import ch.pschatzmann.stocks.IStockRecord;
import ch.pschatzmann.stocks.StockData;
import ch.pschatzmann.stocks.StockID;
import ch.pschatzmann.stocks.data.universe.MarketUniverse;
import ch.pschatzmann.stocks.input.CombinedReader;
import ch.pschatzmann.stocks.input.IReader;
import ch.pschatzmann.stocks.input.MarketArchiveHttpReader;
import ch.pschatzmann.stocks.input.QuandlWIKIReader;
import ch.pschatzmann.stocks.input.YahooReader;

/**
 * Testing of class which provides the historic stock rates
 * 
 * @author pschatzmann
 *
 */

public class TestStocks {
	private static final Logger LOG = LoggerFactory.getLogger(TestStocks.class);

	@BeforeClass
	public static void setup() throws Exception {
		System.out.println("*** "+TestStocks.class.getSimpleName()+" ***");
		Context.setDefaultReader(new MarketArchiveHttpReader());
	}

	@Test
	public void testSingleStockHttp() throws Exception {
		LOG.info("testSingleStock");
		IStockData sd = Context.getStockData(new StockID("AAPL", "NASDAQ"),new MarketArchiveHttpReader());
		List<IStockRecord> result = sd.getHistory();
		LOG.info("size: "+result.size());
		Assert.assertFalse(result.isEmpty());
		Assert.assertNotNull(sd.getExchange());
		Assert.assertNotNull(sd.getTicker());

	}

	@Test
	public void testSingleStock() throws Exception {
		LOG.info("testSingleStock");
		StockData sd = new StockData(new StockID("AAPL", "NASDAQ"), new MarketArchiveHttpReader());
		List<IStockRecord> result = sd.getHistory();
		LOG.info("size: "+result.size());
		Assert.assertFalse(result.isEmpty());
		Assert.assertNotNull(sd.getExchange());
		Assert.assertNotNull(sd.getTicker());

	}
	

	@Test
	public void testSingleStockQuandlWIKI() throws Exception {
		LOG.info("testSingleStock");
		StockData sd = new StockData( new StockID("AAPL", "NASDAQ"),new QuandlWIKIReader());
		List<IStockRecord> result = sd.getHistory();
		LOG.info("size: "+result.size());
		Assert.assertFalse(result.isEmpty());
		Assert.assertNotNull(sd.getExchange());
		Assert.assertNotNull(sd.getTicker());
	}
	
	@Test
	public void testSingleStockYahooWOExchange() throws Exception {
		LOG.info("testSingleStock");
		StockData sd = new StockData(new StockID("AMZN", "NASDAQ"),new YahooReader());
		List<IStockRecord> result = sd.getHistory();
		LOG.info("size: "+result.size());
		Assert.assertFalse(result.isEmpty());
		Assert.assertNotNull(sd.getExchange());
		Assert.assertNotNull(sd.getTicker());
	}
	
	@Test
	public void testSingleStockYahoo() throws Exception {
		LOG.info("testSingleStock");
		StockData sd = new StockData(new StockID("AMZN", "NASDAQ"),new YahooReader());
		List<IStockRecord> result = sd.getHistory();
		LOG.info("size: "+result.size());
		Assert.assertFalse(result.isEmpty());
		Assert.assertNotNull(sd.getExchange());
		Assert.assertNotNull(sd.getTicker());
	}
	
	@Test
	public void testFilterDates() throws Exception {
		LOG.info("testFilterDates");
		StockData sd = new StockData(new StockID("AAPL", "NASDAQ"),new MarketArchiveHttpReader()).filterDates(new DateRange(2,Calendar.YEAR));
		List<IStockRecord> result = sd.getHistory();
		LOG.info("size: "+result.size());
		Assert.assertFalse(result.isEmpty());

	}

	@Test
	public void testCSV() throws Exception {
		LOG.info("testFilterDates");
		StockData sd = new StockData(new StockID("AAPL", "NASDAQ"),new MarketArchiveHttpReader());
		List<String> csv = sd.csvList();
		LOG.info("csv: "+csv);
		Assert.assertFalse(csv.isEmpty());

	}

	@Test
	public void testYears() throws Exception {
		LOG.info("testFilterDates");
		StockData sd = new StockData( new StockID("AAPL", "NASDAQ"),new MarketArchiveHttpReader());
		Collection<String> years = sd.getYears();
		LOG.info("{}",years);
		Assert.assertFalse(years.isEmpty());
		
	}
		
	@Test
	public void testReadNASDAQ() throws Exception {
		LOG.info("testFilterDates");
		LOG.info("{}",new MarketUniverse("NASDAQ",".*").stream().parallel()
				.map(id -> new StockData(id, new MarketArchiveHttpReader()))
		//		.map(sd -> sd.filterDates(new DateRange(1, Calendar.YEAR)))
				.count());
		
	}
	
	
	@Test
	public void testCombinedReader() throws Exception {
		LOG.info("testCombinedReader");
		IStockData sdMarketArchive = Context.getStockData(new StockID("AAPL", "NASDAQ"),new MarketArchiveHttpReader());

		IReader reader = new CombinedReader(new MarketArchiveHttpReader(), new YahooReader());
		IStockData sd = Context.getStockData(new StockID("AAPL", "NASDAQ"), reader);
		List<IStockRecord> result = sd.getHistory();
		LOG.info("size: {}",result.size());
		LOG.info("{}",result.get(result.size()-1));
		//result.stream().forEach(a -> LOG.info(a));
		Assert.assertFalse(result.isEmpty());
		Assert.assertNotNull(sd.getExchange());
		Assert.assertNotNull(sd.getTicker());
		Assert.assertTrue(sd.size() > sdMarketArchive.size());

	}
	
	
}
