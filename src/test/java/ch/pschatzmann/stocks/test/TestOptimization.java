package ch.pschatzmann.stocks.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.pschatzmann.dates.DateRange;
import ch.pschatzmann.stocks.Context;
import ch.pschatzmann.stocks.IStockData;
import ch.pschatzmann.stocks.StockData;
import ch.pschatzmann.stocks.StockID;
import ch.pschatzmann.stocks.accounting.Account;
import ch.pschatzmann.stocks.accounting.IAccount;
import ch.pschatzmann.stocks.accounting.kpi.KPI;
import ch.pschatzmann.stocks.data.index.Nasdaq100Index;
import ch.pschatzmann.stocks.data.universe.ListUniverse;
import ch.pschatzmann.stocks.data.universe.MarketUniverse;
import ch.pschatzmann.stocks.execution.ITrader;
import ch.pschatzmann.stocks.execution.PaperTrader;
import ch.pschatzmann.stocks.execution.fees.PerTradeFees;
import ch.pschatzmann.stocks.input.MarketArchiveHttpReader;
import ch.pschatzmann.stocks.parameters.State;
import ch.pschatzmann.stocks.strategy.GlobalExtremaStrategy;
import ch.pschatzmann.stocks.strategy.optimization.BinarySearchOptimizer;
import ch.pschatzmann.stocks.strategy.optimization.BruteForceOptimizer;
import ch.pschatzmann.stocks.strategy.optimization.Fitness;
import ch.pschatzmann.stocks.strategy.optimization.GeneticOptimizer;
import ch.pschatzmann.stocks.strategy.optimization.IOptimizableTradingStrategy;
import ch.pschatzmann.stocks.strategy.optimization.IOptimizer;
import ch.pschatzmann.stocks.strategy.optimization.PermutatedBinarySearchOptimizer;
import ch.pschatzmann.stocks.strategy.optimization.SequenceOptimizer;
import ch.pschatzmann.stocks.strategy.optimization.SimulatedAnnealingOptimizer;
import ch.pschatzmann.stocks.strategy.optimization.SimulatedFitness;

public class TestOptimization {
	private static final Logger LOG = LoggerFactory.getLogger(TestOptimization.class);
	private static List<DateRange> periods = Context.getDateRanges("2015-01-01", "2017-01-01");
	private static final int NUM = 5;
	private static double baseline;

	@BeforeClass
	public static void setup() throws Exception {
		System.out.println("*** " + TestOptimization.class.getSimpleName() + " ***");
		Context.setDefaultReader(new MarketArchiveHttpReader());

		IAccount account = getAccount();
		IStockData sd = Context.getStockData("AAPL", "NASDAQ");
		IOptimizableTradingStrategy strategy = new GlobalExtremaStrategy(sd);
		ITrader trader = new PaperTrader(account);
		State state = new Fitness(trader).getFitness(strategy, periods.get(0));
		baseline = (Double) state.result().getValue(KPI.AbsoluteReturn);

	}

	private static IAccount getAccount() {
		Account account = new Account("Simulation", "USD", 100000.00, periods.get(0).getStart(),
				new PerTradeFees(10.0));
		account.setCloseDate(periods.get(0).getEnd());
		return account;
	}


	@Test
	@Ignore
	public void testNasdaqSequenceOptimizer() throws Exception {
		IAccount account = getAccount();

		SequenceOptimizer fc = new SequenceOptimizer(new SimulatedFitness(account), KPI.AbsoluteReturn);
		LOG.info("Preparing StockData");

		ListUniverse u = new ListUniverse(new MarketUniverse("NASDAQ").list());
		u.retainAll(new Nasdaq100Index().listID().subList(0, NUM));

		Collection<State> result = u.stream().map(id -> Context.getStockData(id))
				.map(s -> fc.optimize(new GlobalExtremaStrategy(s), periods.get(0)))
				.filter(r -> r.result().getDouble(KPI.AbsoluteReturn) > 1.0)
				.collect(Collectors.toCollection(ArrayList::new));

		LOG.info(result.toString());
		LOG.info("*** END ***");

	}

	@Test
	@Ignore
	public void testNasdaqBinarySearchOptimizer() throws Exception {
		IAccount account = getAccount();

		BinarySearchOptimizer fc = new BinarySearchOptimizer(new SimulatedFitness(account), KPI.AbsoluteReturn);
		LOG.info("Preparing StockData");

		ListUniverse u = new ListUniverse(new MarketUniverse("NASDAQ").list());
		u.retainAll(new Nasdaq100Index().listID().subList(0, NUM));

		Collection<State> result = u.stream().map(id -> Context.getStockData(id).filterDates(periods.get(0)))
				.map(s -> fc.optimize(new GlobalExtremaStrategy(s), periods.get(0)))
				.filter(r -> r.result().getDouble(KPI.AbsoluteReturn) > 1.0)
				.collect(Collectors.toCollection(ArrayList::new));

		LOG.info(result.toString());
		LOG.info("*** END ***");

	}

	@Test
//	@Ignore
	public void testNasdaqSimmulatedAnnealingOptimizer() throws Exception {
		IAccount account = getAccount();

		SimulatedAnnealingOptimizer fc = new SimulatedAnnealingOptimizer(new SimulatedFitness(account),
				KPI.AbsoluteReturn);
		LOG.info("Preparing StockData");

		ListUniverse u = new ListUniverse(new MarketUniverse("NASDAQ").list());
		u.retainAll(new Nasdaq100Index().listID().subList(0, NUM));

		Collection<State> result = u.stream().map(id -> Context.getStockData(id))
				.map(s -> fc.optimize(new GlobalExtremaStrategy(s), periods.get(0)))
				.filter(r -> r.result().getDouble(KPI.AbsoluteReturn) > 1.0)
				.collect(Collectors.toCollection(ArrayList::new));

		LOG.info(result.toString());
		LOG.info("*** END ***");

	}

	/**
	 * BruteForceOptimizer 5 steps -> 68900.16923522949 BinarySearch
	 * 68900.16923522949 Simulated Annealing 68900.16923522949 Geneting 100
	 * 121441.66379547122
	 * 
	 * @throws IOException
	 */
	@Test
//	@Ignore
	public void testGeneticOptimizer() throws IOException {
		IAccount account = getAccount();
		GeneticOptimizer optimizer = new GeneticOptimizer(new SimulatedFitness(account), KPI.AbsoluteReturn);
		// optimizer.setMaxNumberOfSteps(5);
		IStockData stockdata = new StockData(new StockID("AAPL", "NASDAQ"), new MarketArchiveHttpReader());
		IOptimizableTradingStrategy strategy = new GlobalExtremaStrategy(stockdata);
		State state = optimizer.optimize(strategy, periods.get(0));
		LOG.info("test1Binary {} ", state);
		Assert.assertTrue(state.result().getValue(KPI.AbsoluteReturn).doubleValue() >= baseline);
	}

	@Test
	public void testBinarySearchOptimizer() throws IOException {
		IAccount account = getAccount();
		StockData stockdata = new StockData(new StockID("AAPL", "NASDAQ"), new MarketArchiveHttpReader());
		GlobalExtremaStrategy strategy = new GlobalExtremaStrategy(stockdata);
		BinarySearchOptimizer optimizer = new BinarySearchOptimizer(new SimulatedFitness(account), KPI.AbsoluteReturn);
		State result = optimizer.optimize(strategy, periods.get(0));

		LOG.info("Return: {}", result.result().getValue(KPI.AbsoluteReturn));
		LOG.info("baseline: {}", baseline);
		Assert.assertTrue(result.result().getValue(KPI.AbsoluteReturn).doubleValue() >= baseline);
	}

//	@Ignore
	@Test
	public void testSimmulatedAnnealingOptimizer() throws Exception {
		IAccount account = getAccount();

		SimulatedAnnealingOptimizer fc = new SimulatedAnnealingOptimizer(new SimulatedFitness(account), 50,
				KPI.AbsoluteReturn);
		IStockData s = Context.getStockData("AAPL", "NASDAQ");
		State result = fc.optimize(new GlobalExtremaStrategy(s), periods.get(0));
		LOG.info("test1NasdaqSimmulatedAnnealingOptimizer: {} ", Context.getMap(s.getStockID(), result));
		LOG.info("KPI.AbsoluteReturn: {}", result.getResult().getDouble(KPI.AbsoluteReturn));

		Assert.assertTrue(result.result().getValue(KPI.AbsoluteReturn).doubleValue() >= baseline);
	}

	@Test
	@Ignore
	public void testBruteForceOptimizer() throws Exception {
		IAccount account = getAccount();
		BruteForceOptimizer fc = new BruteForceOptimizer(new SimulatedFitness(account), KPI.AbsoluteReturn);
		IStockData s = Context.getStockData("AAPL", "NASDAQ");
		State state = fc.optimize(new GlobalExtremaStrategy(s), periods.get(0));
		LOG.info("test1Binary {} ", state);
		Assert.assertTrue(state.result().getValue(KPI.AbsoluteReturn).doubleValue() >= baseline);
	}

//	@Ignore
	@Test
	public void testPermutatedBinarySearchOptimizer() throws Exception {
		IAccount account = getAccount();
		IOptimizer fc = new PermutatedBinarySearchOptimizer(new SimulatedFitness(account), KPI.AbsoluteReturn);
		IStockData s = Context.getStockData("AAPL", "NASDAQ");
		State state = fc.optimize(new GlobalExtremaStrategy(s), periods.get(0));
		LOG.info("test1Binary {} ", state);
		Assert.assertTrue(state.result().getValue(KPI.AbsoluteReturn).doubleValue() >= baseline);
	}
	
	@Test
	@Ignore
	public void testSequenceOptimizer() throws Exception {
		IAccount account = getAccount();

		SequenceOptimizer fc = new SequenceOptimizer(new SimulatedFitness(account), KPI.AbsoluteReturn);
		IStockData s = Context.getStockData("AAPL", "NASDAQ");
		State state = fc.optimize(new GlobalExtremaStrategy(s), periods.get(0));
		LOG.info("test1Binary {} ", state);
		Assert.assertTrue(state.result().getValue(KPI.AbsoluteReturn).doubleValue() >= baseline);
	}


}
