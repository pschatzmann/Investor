package ch.pschatzmann.stocks.test;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.pschatzmann.dates.DateRange;
import ch.pschatzmann.stocks.Context;
import ch.pschatzmann.stocks.accounting.Account;
import ch.pschatzmann.stocks.accounting.IAccount;
import ch.pschatzmann.stocks.accounting.ManagedAccount;
import ch.pschatzmann.stocks.accounting.Transaction;
import ch.pschatzmann.stocks.accounting.kpi.KPI;
import ch.pschatzmann.stocks.data.universe.EdgarUniverse;
import ch.pschatzmann.stocks.execution.PaperTrader;
import ch.pschatzmann.stocks.execution.ScheduledExecutor;
import ch.pschatzmann.stocks.execution.StrategyExecutor;
import ch.pschatzmann.stocks.execution.fees.PerTradeFees;
import ch.pschatzmann.stocks.input.MarketArchiveHttpReader;
import ch.pschatzmann.stocks.strategy.TradingStrategyFactory;
import ch.pschatzmann.stocks.strategy.allocation.DistributedAllocationStrategy;
import ch.pschatzmann.stocks.strategy.selection.SelectionResult;
import ch.pschatzmann.stocks.strategy.selection.StockSelector;
import ch.pschatzmann.stocks.strategy.selection.StrategySelector;

public class TestExecution {
	@BeforeClass
	public static void setup() throws Exception {
		System.out.println("*** "+TestExecution.class.getSimpleName()+" ***");
		Context.setDefaultReader(new MarketArchiveHttpReader());
	}
	
	@Test
	public void testScheduledExecution() throws Exception {
		List<DateRange> periods = Context.getDateRanges("2016-01-01","2017-01-01");
		IAccount account = new ManagedAccount("AccountTimeBasedExecutorSimulation-1", "USD", 100000.00, periods.get(0).getStart(), new PerTradeFees(6.95));
		List<String> strategies = TradingStrategyFactory.list();
		PaperTrader trader = new PaperTrader(account);
		DistributedAllocationStrategy allocationStrategy = new DistributedAllocationStrategy(trader);
		StrategyExecutor executor = new StrategyExecutor(trader, allocationStrategy);
		EdgarUniverse portfolioUniverse =  new EdgarUniverse(2016, Arrays.asList("NetIncomeLoss"), true);                               
		StrategySelector strategySelector = new StrategySelector(account, strategies, periods.get(0), KPI.AbsoluteReturn);
		StockSelector stockSelector = new StockSelector(strategySelector);
		SelectionResult result = stockSelector.getSelection(10, portfolioUniverse.list(10), Context.getDefaultReader());
		executor.setStrategies(result.getStrategies());

		ScheduledExecutor scheduledExecutor = new ScheduledExecutor(executor);
		scheduledExecutor.schedule("* * * * * ?"); 
		
		Thread.sleep(8000);

		scheduledExecutor.stop();
		
		Assert.assertTrue(scheduledExecutor.getJobExecutionCount()>=1);
	}
	

	@Test
	public void testScheduledExecutionNow() throws Exception {
		List<DateRange> periods = Context.getDateRanges("2016-01-01","2017-01-01");
		IAccount account = new Account("test", "USD", 10000.00, periods.get(0).getStart(), new PerTradeFees(6.95));
		List<String> strategies = TradingStrategyFactory.list();
		PaperTrader trader = new PaperTrader(account);
		DistributedAllocationStrategy allocationStrategy = new DistributedAllocationStrategy(trader);
		StrategyExecutor executor = new StrategyExecutor(trader, allocationStrategy);
		EdgarUniverse portfolioUniverse =  new EdgarUniverse(2016, Arrays.asList("NetIncomeLoss"), true);                               
		StrategySelector strategySelector = new StrategySelector(account, strategies, periods.get(0), KPI.AbsoluteReturn);
		StockSelector stockSelector = new StockSelector(strategySelector);
		SelectionResult result = stockSelector.getSelection(10, portfolioUniverse.list(10), Context.getDefaultReader());
		executor.setStrategies(result.getStrategies());

		ScheduledExecutor scheduledExecutor = new ScheduledExecutor(executor);
		scheduledExecutor.reset();
		// simulate trade for today
		List<Transaction> t = scheduledExecutor.trade();
		System.out.println("transactions: "+t);
		
	}

}
