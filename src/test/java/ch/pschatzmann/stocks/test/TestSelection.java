package ch.pschatzmann.stocks.test;

import java.io.File;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
import ch.pschatzmann.stocks.accounting.Account;
import ch.pschatzmann.stocks.accounting.IAccount;
import ch.pschatzmann.stocks.accounting.kpi.KPI;
import ch.pschatzmann.stocks.data.universe.MarketUniverse;
import ch.pschatzmann.stocks.data.universe.QuandlSixUniverse;
import ch.pschatzmann.stocks.execution.ITrader;
import ch.pschatzmann.stocks.execution.PaperTrader;
import ch.pschatzmann.stocks.execution.StrategyExecutor;
import ch.pschatzmann.stocks.execution.fees.PerTradeFees;
import ch.pschatzmann.stocks.execution.fees.PostFinanceFees;
import ch.pschatzmann.stocks.input.IReader;
import ch.pschatzmann.stocks.input.MarketArchiveHttpReader;
import ch.pschatzmann.stocks.input.QuandlSixReader;
import ch.pschatzmann.stocks.input.YahooReader;
import ch.pschatzmann.stocks.parameters.State;
import ch.pschatzmann.stocks.strategy.OptimizedStrategy;
import ch.pschatzmann.stocks.strategy.RSI2Strategy;
import ch.pschatzmann.stocks.strategy.TradingStrategyFactory;
import ch.pschatzmann.stocks.strategy.allocation.DistributedAllocationStrategy;
import ch.pschatzmann.stocks.strategy.allocation.IAllocationStrategy;
import ch.pschatzmann.stocks.strategy.optimization.BinarySearchOptimizer;
import ch.pschatzmann.stocks.strategy.optimization.Fitness;
import ch.pschatzmann.stocks.strategy.optimization.SimulatedFitness;
import ch.pschatzmann.stocks.strategy.selection.IStategySelector;
import ch.pschatzmann.stocks.strategy.selection.Restartable;
import ch.pschatzmann.stocks.strategy.selection.SelectionResult;
import ch.pschatzmann.stocks.strategy.selection.SelectionState;
import ch.pschatzmann.stocks.strategy.selection.StockSelector;
import ch.pschatzmann.stocks.strategy.selection.StrategySelector;
import ch.pschatzmann.stocks.strategy.selection.StrategySelectorOptimized;

public class TestSelection {
	private static final Logger LOG = LoggerFactory.getLogger(TestSelection.class);

	@BeforeClass
	public static void setup() throws Exception {
		System.out.println("*** "+TestSelection.class.getSimpleName()+" ***");
		Context.setDefaultReader(new MarketArchiveHttpReader());
	}

	@Test
	public void testSelect() throws Exception {
		List<DateRange> periods = Context.getDateRanges("2016-01-01","2017-01-01");
		IAccount account = new Account("Simulation", "USD", 100000.0, periods.get(0).getStart(), new PerTradeFees(100.0));
		List<IStockID> stocks = Context.head(new MarketUniverse("NASDAQ").stream().collect(Collectors.toList()),2);
		List<String> strategies = TradingStrategyFactory.list();
		Predicate<SelectionState> withReturn = a->a.result().getDouble(KPI.AbsoluteReturn)>0;
		Predicate<SelectionState> traded = a->a.result().getDouble(KPI.NumberOfTrades)>3;		
		IStategySelector strategySelector = new StrategySelector(account, strategies, periods.get(0), KPI.AbsoluteReturn, withReturn.and(traded));
		StockSelector stockSelector = new StockSelector(strategySelector, new Restartable("restart.ser",100));
		SelectionResult result = stockSelector.getSelection(5, stocks, new MarketArchiveHttpReader());
		LOG.info("{}",result.getStocks());
		result.getResult().forEach(l -> LOG.info(" Return=>"+l.getResult().getDouble(KPI.AbsoluteReturn)));;
		new File("restart.ser").delete();
		Assert.assertEquals(2, result.getResult().size());
	}
	
	@Test
	public void testSelectSIX() throws Exception {
		List<DateRange> periods = Context.getDateRanges("2016-01-01","2017-01-01");
		IAccount account = new Account("Simulation", "USD", 100000.0, periods.get(0).getStart(), new PerTradeFees(100.0));
		List<IStockID> stocks = Context.head(new QuandlSixUniverse("CH.*CHF").stream().collect(Collectors.toList()),10);
		List<String> strategies = TradingStrategyFactory.list();
		Predicate<SelectionState> withReturn = a->a.result().getDouble(KPI.AbsoluteReturn)>0;
		Predicate<SelectionState> traded = a->a.result().getDouble(KPI.NumberOfTrades)>3;		
		IStategySelector strategySelector = new StrategySelector(account, strategies, periods.get(0), KPI.AbsoluteReturn, withReturn.and(traded));
		StockSelector stockSelector = new StockSelector(strategySelector, new Restartable("restart.ser",100));
		SelectionResult result = stockSelector.getSelection(5, stocks, new QuandlSixReader());
		LOG.info("{}",result.getStocks());
		result.getResult().forEach(l -> LOG.info(" Return=>"+l.getResult().getDouble(KPI.AbsoluteReturn)));;
		new File("restart.ser").delete();
		Assert.assertEquals(5, result.getResult().size());
	}
	
	@Test
	public void testSelectYahoo() {
		IReader reader = new YahooReader();  //since jun 2017
		List<DateRange> periods = Context.getDateRanges("2010-01-01","2017-01-01");

		IAccount account = new Account("Simulation","USD", 100000.00, periods.get(0).getStart(), new PerTradeFees(100.0));
		StockData stockdata = new StockData(new StockID("NVDA", "NASDQ"), reader);
		RSI2Strategy strategy = new RSI2Strategy(stockdata);
		PaperTrader trader = new PaperTrader(account);
		BinarySearchOptimizer optimizer = new BinarySearchOptimizer(new SimulatedFitness(account),KPI.AbsoluteReturn);
		OptimizedStrategy optimizedStrategy = new OptimizedStrategy(strategy, optimizer, periods.get(0));                                             
		State state = new Fitness(trader).getFitness(optimizedStrategy, periods.get(1));

		System.out.println("Return: " + state.result().getValue(KPI.AbsoluteReturn));
	}
	
	@Test
	public void testSelectOptimized() throws Exception {
		List<DateRange> periods = Context.getDateRanges("2016-01-01","2017-01-01");
		IAccount account = new Account("Simulation", "USD", 100000.0, periods.get(0).getStart(), new PerTradeFees(100.0));
		List<IStockID> stocks = Context.head(new MarketUniverse("NASDAQ").stream().collect(Collectors.toList()),2);
		List<String> strategies = TradingStrategyFactory.list();
		BinarySearchOptimizer optimizer = new BinarySearchOptimizer(new SimulatedFitness(account), KPI.AbsoluteReturn);
		Predicate<SelectionState> withReturn = a->a.result().getDouble(KPI.AbsoluteReturn)>0;
		Predicate<SelectionState> traded = a->a.result().getDouble(KPI.NumberOfTrades)>3;		
		StrategySelectorOptimized strategySelector = new StrategySelectorOptimized(account, strategies, periods.get(0), periods.get(1), optimizer, withReturn.and(traded));
		StockSelector stockSelector = new StockSelector(strategySelector, new Restartable("restart.ser",100));
		SelectionResult result = stockSelector.getSelection(5, stocks, new MarketArchiveHttpReader());
		new File("restart.ser").delete();
		LOG.info("{}",result.getStocks());
		Assert.assertEquals(2, result.getResult().size());
		
	}
	
	
	@Test
	public void testEvaluate() throws Exception {
		List<DateRange> periods = Context.getDateRanges("2016-01-01","2017-01-01");

		SelectionResult result = new SelectionResult();
		result.load(new File("./src/test/resources/ch.json"));
		IAccount account = new Account("Simulation","USD", 1000000.00, periods.get(0).getStart(), new PostFinanceFees());
		ITrader trader = new PaperTrader(account);
		IAllocationStrategy allocationStrategy = new DistributedAllocationStrategy(trader);
		StrategyExecutor executor = new StrategyExecutor(trader, allocationStrategy);
		executor.addStrategy(result.getStrategies(new QuandlSixReader()));
		executor.run(periods.get(0));

		System.out.println(account.getKPIValues());
	}


	
}
