package ch.pschatzmann.stocks.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.pschatzmann.dates.DateRange;
import ch.pschatzmann.stocks.Context;
import ch.pschatzmann.stocks.IStockID;
import ch.pschatzmann.stocks.StockData;
import ch.pschatzmann.stocks.StockID;
import ch.pschatzmann.stocks.input.MarketArchiveHttpReader;
import ch.pschatzmann.stocks.integration.StockDataArray;
import ch.pschatzmann.stocks.strategy.TradingStrategyFactory;
import ch.pschatzmann.stocks.strategy.selection.Restartable;
import ch.pschatzmann.stocks.strategy.selection.SelectionResult;
import ch.pschatzmann.stocks.strategy.selection.SelectionState;
import ch.pschatzmann.stocks.utils.Calculations;

public class TestUtils {
	private static final Logger LOG = LoggerFactory.getLogger(TestUtils.class);

	@BeforeClass 
	public static void setup() throws Exception{
		System.out.println("*** "+TestUtils.class.getSimpleName()+" ***");
	}	


	@Test
	public void testAmount() throws Exception {
		LOG.info("{}",Calculations.toLong(1.1));
		LOG.info("{}",Calculations.toLong(1.9));
		LOG.info("{}",Calculations.toLong(1.99));
	}
	
	@Test
	public void testArray() {
		StockData stockdata = new StockData(new StockID("AAPL", "NASDAQ"), new MarketArchiveHttpReader());
		StockDataArray arr = new StockDataArray(stockdata);
	}
	
	
	@Test
	public void testDateRanges() {
		List<DateRange> periods = Context.getDateRanges(Context.date("2015-01-01"), Context.date("2016-01-01"));
		for (DateRange dr : periods) {
			LOG.info("{}",dr);
		}
		Assert.assertEquals(Context.date("2016-01-01"), periods.get(1).getStart());		
		Assert.assertEquals(2, periods.size());
	}

	@Test
	public void testDateRanges1() {
		List<DateRange> periods = Context.getDateRanges(Context.date("2015-01-01"), Context.date("2016-01-01"),new Date());
		for (DateRange dr : periods) {
			LOG.info("{}",dr);
		}
		Assert.assertEquals(Context.date("2016-01-01"), periods.get(1).getStart());		
		Assert.assertEquals(3, periods.size());
	}
	
	@Test
	public void testRestartalbe() throws IOException {
		Restartable restartable = new Restartable("test",1);
		HashMap data = new HashMap();
		data.put("topN", Arrays.asList("a","b","c"));
		data.put("processed", Arrays.asList(new SelectionState(null),new SelectionState(null)));
		restartable.save(data);
		
		Map<String, Object> map = restartable.load();
		Collection c1 = (Collection<SelectionState>) map.get("topN");
		Collection c2 =  (Collection<IStockID>) map.get("processed");
		
		Assert.assertEquals(3, c1.size());
		Assert.assertEquals(2, c2.size());
		
	}
	
	@Test
	public void testSaveLoad() throws Exception {
		Collection<SelectionState> selectionStates = new ArrayList();
		SelectionState s = new SelectionState(new StockID("AA","BB"));
		s.setStrategyName("test");
		selectionStates.add(s);
		SelectionResult result = new SelectionResult(selectionStates);
		Assert.assertEquals(1, result.getStocks().size());
		File file = new File("test.json");
		result.save(file);
		
		result = new SelectionResult();
		result.load(file);
		file.delete();
		Assert.assertEquals(1, result.getStocks().size());

	}

	@Test
	public void testStrategyDescription() throws Exception {
		String str = TradingStrategyFactory.getStrategyDesciption("BuyAndHoldStrategy");
		LOG.info(str);
		Assert.assertNotNull(str);
	}

}
