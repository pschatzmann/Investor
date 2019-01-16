package ch.pschatzmann.stocks.test;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.pschatzmann.stocks.Context;
import ch.pschatzmann.stocks.IStockData;
import ch.pschatzmann.stocks.StockData;
import ch.pschatzmann.stocks.StockID;
import ch.pschatzmann.stocks.cache.ICache;
import ch.pschatzmann.stocks.cache.JcsCache;
import ch.pschatzmann.stocks.cache.RedisCache;
import ch.pschatzmann.stocks.input.DefaultReader;

public class TestCache {
	private static final Logger LOG = LoggerFactory.getLogger(TestCache.class);
	private static ICache old;
	private long time;
	StockID id = new StockID("AAPL");

	@BeforeClass
	public static void setup() {
		System.out.println("*** "+TestCache.class.getSimpleName()+" ***");
		old = Context.getCache();
	}
	
	@AfterClass 
	public static void end() {
		Context.setCache(old);
	}
	
	@Test
	public void testRedis() {
		ICache c = new RedisCache();
		
		c.remove(id + "/" + StockData.getSimpleClassName(Context.getDefaultReader()));
		Context.setCache(c);

		startTimer();
		IStockData sd = Context.getStockData(id);
		LOG.info("{}", sd.getHistory().size());
		sd = Context.getStockData(id, new DefaultReader());
		long time = runTime();
		Assert.assertTrue(sd.getHistory().size()>100);

		startTimer();	
		sd = Context.getStockData(id, new DefaultReader());
		long timeCached = runTime();
		
		Assert.assertTrue(sd.getHistory().size()>100);
		Assert.assertTrue(timeCached < time);

	}
	
	
	@Test
	public void testJCS() {
		ICache c = new JcsCache();
		c.remove(id + "/" + StockData.getSimpleClassName(Context.getDefaultReader()));
		Context.setCache(c);

		startTimer();
		IStockData sd = Context.getStockData(id);
		LOG.info("{}", sd.getHistory().size());
		sd = Context.getStockData(id, new DefaultReader());
		long time = runTime();
		Assert.assertTrue(sd.getHistory().size()>100);

		startTimer();	
		sd = Context.getStockData(id, new DefaultReader());
		long timeCached = runTime();
		
		Assert.assertTrue(sd.getHistory().size()>100);
		Assert.assertTrue(timeCached < time);
		
	}

	private void startTimer() {
		time = System.currentTimeMillis();		
	}
	
	private long runTime() {
		return System.currentTimeMillis() - this.time;
	}


}
