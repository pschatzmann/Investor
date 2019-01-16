package ch.pschatzmann.stocks.test;

import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.pschatzmann.dates.DateRange;
import ch.pschatzmann.stocks.Context;
import ch.pschatzmann.stocks.StockData;
import ch.pschatzmann.stocks.StockID;
import ch.pschatzmann.stocks.accounting.Account;
import ch.pschatzmann.stocks.accounting.IAccount;
import ch.pschatzmann.stocks.accounting.kpi.KPI;
import ch.pschatzmann.stocks.execution.PaperTrader;
import ch.pschatzmann.stocks.execution.StrategyExecutor;
import ch.pschatzmann.stocks.execution.fees.PerTradeFees;
import ch.pschatzmann.stocks.input.MarketArchiveHttpReader;
import ch.pschatzmann.stocks.strategy.RSI2Strategy;
import ch.pschatzmann.stocks.strategy.allocation.DistributedAllocationStrategy;
import ch.pschatzmann.stocks.strategy.allocation.Distributor;
import ch.pschatzmann.stocks.strategy.allocation.IDistributor;
import ch.pschatzmann.stocks.strategy.allocation.RandomDistributor;
import ch.pschatzmann.stocks.strategy.allocation.SharpeRatioDistributor;
import ch.pschatzmann.stocks.strategy.allocation.SharpeRatioOfStockDistributor;
import ch.pschatzmann.stocks.strategy.allocation.SimpleAllocationStrategy;
import ch.pschatzmann.stocks.strategy.allocation.SimpleDistributedAllocationStrategy;

public class TestDistribution {
	private static final Logger LOG = LoggerFactory.getLogger(TestDistribution.class);
	private int TRANSACTIONS = 2;
	private double RETURN = 70299.88;
	private static List<DateRange> periods;
	
	@BeforeClass
	public static void setup() throws Exception {
		System.out.println("*** "+TestDistribution.class.getSimpleName()+" ***");
		Context.setDefaultReader(new MarketArchiveHttpReader());
		periods = Context.getDateRanges("2015-01-01","2017-01-01");
	}
	
	
	@Test
	public void testSimpleAllocationStrategy() throws Exception {
		Account account = new Account("Simulation","USD", 100000.00, Context.date("2016-01-01"), new PerTradeFees(10.0));
		
		PaperTrader trader = new PaperTrader(account);
		SimpleAllocationStrategy allocationStrategy = new SimpleAllocationStrategy(trader);
		StockData apple = new StockData(new StockID("AAPL", "NASDAQ"), new MarketArchiveHttpReader());
		StrategyExecutor executor = new StrategyExecutor(trader, allocationStrategy);
		RSI2Strategy strategy = new RSI2Strategy(apple);
		executor.addStrategy(strategy);
		executor.run(periods.get(0));

		LOG.info("***SimpleAllocationStrategy");
		LOG.info("AbsoluteReturn:"+account.getKPIValue(KPI.AbsoluteReturn));
		LOG.info("{}",apple.getValue(new Date()));
		account.getTransactions().stream().sorted().forEach(t -> LOG.info("{}",t));
		
		Assert.assertEquals(RETURN,account.getKPIValue(KPI.AbsoluteReturn),1);
		Assert.assertEquals(0,strategy.getParameters().getResult().getDouble(KPI.AbsoluteReturn),1);		
		Assert.assertEquals(TRANSACTIONS, account.getTransactions().stream().count());
		
	}
	

	@Test
	public void testSimpleDistributedAllocationStrategy() throws Exception {
		Account account = new Account("Simulation","USD", 100000.00, Context.date("2016-01-01"), new PerTradeFees(10.0));

		PaperTrader trader = new PaperTrader(account);
		SimpleDistributedAllocationStrategy allocationStrategy = new SimpleDistributedAllocationStrategy(trader);
		StockData apple = new StockData(new StockID("AAPL", "NASDAQ"), new MarketArchiveHttpReader());
		StrategyExecutor executor = new StrategyExecutor(trader, allocationStrategy);
		RSI2Strategy strategy = new RSI2Strategy(apple);
		executor.addStrategy(strategy);
		executor.run(periods.get(0));
		
		LOG.info("***SimpleDistributedAllocationStrategy");
		LOG.info("{}",apple.getValue(new Date()));
		account.getTransactions().forEach(t -> LOG.info("{}",t));


		Assert.assertEquals(RETURN,account.getKPIValue(KPI.AbsoluteReturn),1);
		Assert.assertEquals(0,strategy.getParameters().getResult().getDouble(KPI.AbsoluteReturn),1);		
		Assert.assertEquals(TRANSACTIONS, account.getTransactions().stream().count());

	}


	
	@Test
	public void testDistributedAllocationStrategy() throws Exception {
		Account account = new Account("Simulation","USD", 100000.00, Context.date("2016-01-01"), new PerTradeFees(10.0));

		PaperTrader trader = new PaperTrader(account);
		DistributedAllocationStrategy allocationStrategy = new DistributedAllocationStrategy(trader);
		StockData apple = new StockData(new StockID("AAPL", "NASDAQ"), new MarketArchiveHttpReader());
		StrategyExecutor executor = new StrategyExecutor(trader, allocationStrategy);
		RSI2Strategy strategy = new RSI2Strategy(apple);
		executor.addStrategy(strategy);
		executor.run(periods.get(0));

		LOG.info("***DistributedAllocationStrategy");
		LOG.info("{}",apple.getValue(new Date()));
		account.getTransactions().forEach(t -> LOG.info("{}",t));

		Assert.assertEquals(70485.80,account.getKPIValue(KPI.AbsoluteReturn),1);
		Assert.assertEquals(0,strategy.getParameters().getResult().getDouble(KPI.AbsoluteReturn),1);		
		Assert.assertEquals(TRANSACTIONS, account.getTransactions().stream().count());


	}

	@Test
	public void testSharpRatioOfStockDistribution() throws Exception {
		List<DateRange> periods = Context.getDateRanges("2015-01-01","2016-01-01");
		IAccount account = new Account("Simulation","USD", 100000.00, 	periods.get(0).getStart(), new PerTradeFees(10.0));

		PaperTrader trader = new PaperTrader(account);
		StockData apple = new StockData(new StockID("AAPL", "NASDAQ"), new MarketArchiveHttpReader());
		StockData intel = new StockData(new StockID("INTC", "NASDAQ"), new MarketArchiveHttpReader());
		RSI2Strategy appleStrategy = new RSI2Strategy(apple);
		RSI2Strategy intelStrategy = new RSI2Strategy(intel);

		IDistributor distributor = new SharpeRatioOfStockDistributor(account,0.0);
		DistributedAllocationStrategy allocationStrategy = new DistributedAllocationStrategy(trader, distributor);

		StrategyExecutor executor = new StrategyExecutor(trader, allocationStrategy);
		executor.addStrategy(appleStrategy);
		executor.addStrategy(intelStrategy);
		executor.run(periods.get(0));
		
		System.out.println(account.getKPIValues());
		
		account.getTransactions().forEach(t -> System.out.println(t));
		
		Assert.assertTrue(account.getTransactions().stream().count()>1);
	}
	
	@Test
	public void testSharpRatioDistribution() throws Exception {
		List<DateRange> periods = Context.getDateRanges("2015-01-01","2016-01-01");
		IAccount account = new Account("Simulation","USD", 100000.00, 	periods.get(0).getStart(), new PerTradeFees(10.0));

		PaperTrader trader = new PaperTrader(account);
		StockData apple = new StockData(new StockID("AAPL", "NASDAQ"), new MarketArchiveHttpReader());
		StockData intel = new StockData(new StockID("INTC", "NASDAQ"), new MarketArchiveHttpReader());
		RSI2Strategy appleStrategy = new RSI2Strategy(apple);
		RSI2Strategy intelStrategy = new RSI2Strategy(intel);

		IDistributor distributor = new SharpeRatioDistributor(account,0.0);
		DistributedAllocationStrategy allocationStrategy = new DistributedAllocationStrategy(trader, distributor);

		StrategyExecutor executor = new StrategyExecutor(trader, allocationStrategy);
		executor.addStrategy(appleStrategy);
		executor.addStrategy(intelStrategy);
		executor.run(periods.get(0));
		
		System.out.println(account.getKPIValues());
		
		account.getTransactions().forEach(t -> System.out.println(t));
		
		Assert.assertTrue(account.getTransactions().stream().count()>1);
	}
	
	@Test
	public void testRndomDistribution() throws Exception {
		List<DateRange> periods = Context.getDateRanges("2015-01-01","2016-01-01");
		Account account = new Account("Simulation","USD", 100000.00, 	periods.get(0).getStart(), new PerTradeFees(10.0));

		PaperTrader trader = new PaperTrader(account);
		StockData apple = new StockData(new StockID("AAPL", "NASDAQ"), new MarketArchiveHttpReader());
		StockData intel = new StockData(new StockID("INTC", "NASDAQ"), new MarketArchiveHttpReader());
		RSI2Strategy appleStrategy = new RSI2Strategy(apple);
		RSI2Strategy intelStrategy = new RSI2Strategy(intel);

		IDistributor distributor = new RandomDistributor();
		DistributedAllocationStrategy allocationStrategy = new DistributedAllocationStrategy(trader, distributor);

		StrategyExecutor executor = new StrategyExecutor(trader, allocationStrategy);
		executor.addStrategy(appleStrategy);
		executor.addStrategy(intelStrategy);
		executor.run(periods.get(0));
		Assert.assertTrue(account.getKPIValue(KPI.SharpeRatio)>0.0);		
		
	}
	
	@Test
	public void testDistributor() {
		StockData apple = new StockData(new StockID("AAPL", "NASDAQ"), new MarketArchiveHttpReader());
		StockData intel = new StockData(new StockID("INTC", "NASDAQ"), new MarketArchiveHttpReader());
		RSI2Strategy appleStrategy = new RSI2Strategy(apple);
		RSI2Strategy intelStrategy = new RSI2Strategy(intel);
		Distributor distributor = new Distributor();
		distributor.add(appleStrategy, 0.75);
		distributor.add(intelStrategy, 0.25);
	}

}
