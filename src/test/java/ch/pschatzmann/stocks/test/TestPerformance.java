package ch.pschatzmann.stocks.test;

import java.util.Arrays;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import ch.pschatzmann.dates.DateRange;
import ch.pschatzmann.stocks.Context;
import ch.pschatzmann.stocks.accounting.Account;
import ch.pschatzmann.stocks.accounting.kpi.KPI;
import ch.pschatzmann.stocks.accounting.kpi.KPIValue;
import ch.pschatzmann.stocks.data.universe.EdgarUniverse;
import ch.pschatzmann.stocks.data.universe.ListUniverse;
import ch.pschatzmann.stocks.execution.PaperTrader;
import ch.pschatzmann.stocks.execution.StrategyExecutor;
import ch.pschatzmann.stocks.execution.fees.PerTradeFees;
import ch.pschatzmann.stocks.input.IReaderEx;
import ch.pschatzmann.stocks.input.MarketArchiveHttpReader;
import ch.pschatzmann.stocks.input.DefaultReader;
import ch.pschatzmann.stocks.input.YahooReader;
import ch.pschatzmann.stocks.strategy.TradingStrategyFactory;
import ch.pschatzmann.stocks.strategy.allocation.DistributedAllocationStrategy;
import ch.pschatzmann.stocks.strategy.selection.SelectionResult;
import ch.pschatzmann.stocks.strategy.selection.StockSelector;
import ch.pschatzmann.stocks.strategy.selection.StrategySelector;

public class TestPerformance {
	
	@BeforeClass
	public static void setup() throws Exception {
		System.out.println("*** "+TestPerformance.class.getSimpleName()+" ***");
		Context.setDefaultReader(new MarketArchiveHttpReader());
	}

	
	@Ignore
	@Test
	public void test() throws Exception {
		IReaderEx reader = new DefaultReader();
		Context.setDefaultReader(reader);
		List<DateRange> periods = Context.getDateRanges("2011-01-01", "2012-01-01", "2013-01-01", "2014-01-01",
				"2015-01-01", "2016-01-01", "2017-01-01");
		Account account = new Account("Simulation", "USD", 100000.00, periods.get(0).getStart(),
				new PerTradeFees(6.95));
		List<String> strategies = TradingStrategyFactory.list();
		PaperTrader trader = new PaperTrader(account);
		DistributedAllocationStrategy allocationStrategy = new DistributedAllocationStrategy(trader);
		StrategyExecutor executor = new StrategyExecutor(trader, allocationStrategy);

		for (int i = 1; i <= 5; i++) {
			int year = Context.getYear(periods.get(i - 1).getStart());
			ListUniverse portfolioUniverse = new ListUniverse();
			portfolioUniverse.add(new EdgarUniverse(year, Arrays.asList(0.2, 0.5, 0.8, 1.0),
					Arrays.asList("ResearchAndDevelopmentExpense"), true).list(10));
			portfolioUniverse.add(
					new EdgarUniverse(year, Arrays.asList(0.1, 0.3, 0.5, 1.0), Arrays.asList("NetIncomeLoss"), true)
							.list(10));
			EdgarUniverse percentUniverse = new EdgarUniverse(year, Arrays.asList(0.2, 0.5, 0.8, 1.0),
					Arrays.asList("NetIncomeLoss"), true);
			percentUniverse.setCalculatePercentChange(true);
			portfolioUniverse.add(percentUniverse.list(10));

			StrategySelector strategySelector = new StrategySelector(account, strategies, periods.get(i - 1),
					KPI.AbsoluteReturn);
			StockSelector stockSelector = new StockSelector(strategySelector);
			SelectionResult result = stockSelector.getSelection(30, portfolioUniverse.list(), reader);
			executor.setStrategies(result.getStrategies(reader));
			executor.run(periods.get(i));
		}
		account.setCloseDate(periods.get(5).getEnd());
		List<KPIValue> kpi = account.getKPIValues();
		System.out.println(kpi);

	}

	@Ignore
	@Test
	public void testException() throws Exception {
		IReaderEx reader = new YahooReader();
		Context.setDefaultReader(reader);
		List<DateRange> periods = Context.getDateRanges("2011-01-01", "2012-01-01", "2013-01-01", "2014-01-01",
				"2015-01-01", "2016-01-01", "2017-01-01");
		Account account = new Account("Simulation", "USD", 100000.00, periods.get(0).getStart(),
				new PerTradeFees(6.95));
		List<String> strategies = Arrays.asList("BuyAndHoldStrategy");
		PaperTrader trader = new PaperTrader(account);
		DistributedAllocationStrategy allocationStrategy = new DistributedAllocationStrategy(trader);
		StrategyExecutor executor = new StrategyExecutor(trader, allocationStrategy);
		int i = 5;
		int year = Context.getYear(periods.get(i - 1).getStart());
		ListUniverse portfolioUniverse = new ListUniverse();
		portfolioUniverse.add(new EdgarUniverse(year, Arrays.asList(0.2, 0.5, 0.8, 1.0), 
				Arrays.asList("ResearchAndDevelopmentExpense"), true).list(10));
		portfolioUniverse.add(
				new EdgarUniverse(year, Arrays.asList(0.1, 0.3, 0.5, 1.0), Arrays.asList("NetIncomeLoss"), true).list(19));
		EdgarUniverse percentUniverse = new EdgarUniverse(year, Arrays.asList(0.2, 0.5, 0.8, 1.0), 
				Arrays.asList("NetIncomeLoss"), true);
		percentUniverse.setCalculatePercentChange(true);
		portfolioUniverse.add(percentUniverse.list(10));
		StrategySelector strategySelector = new StrategySelector(account, strategies, periods.get(i - 1),
				KPI.AbsoluteReturn);
		StockSelector stockSelector = new StockSelector(strategySelector);
		SelectionResult result = stockSelector.getSelection(10, portfolioUniverse.list(), reader);
		executor.setStrategies(result.getStrategies(reader));
		executor.run(periods.get(i));
		account.setCloseDate(periods.get(5).getEnd());
		System.out.println(account.getKPIValues());
	}

}
