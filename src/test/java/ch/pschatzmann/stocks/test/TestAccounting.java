package ch.pschatzmann.stocks.test;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.pschatzmann.stocks.Context;
import ch.pschatzmann.stocks.StockID;
import ch.pschatzmann.stocks.accounting.Account;
import ch.pschatzmann.stocks.accounting.BasicAccount;
import ch.pschatzmann.stocks.accounting.Portfolio;
import ch.pschatzmann.stocks.accounting.PortfolioStockInfo;
import ch.pschatzmann.stocks.accounting.Transaction;
import ch.pschatzmann.stocks.errors.SystemException;
import ch.pschatzmann.stocks.execution.ITrader;
import ch.pschatzmann.stocks.execution.PaperTrader;
import ch.pschatzmann.stocks.execution.fees.PerTradeFees;
import ch.pschatzmann.stocks.input.IReader;
import ch.pschatzmann.stocks.input.MarketArchiveHttpReader;

/**
 * Test cases around the stock accounting and automatic trading simulation
 * 
 * @author pschatzmann
 *
 */
public class TestAccounting {
	Double delta = 0.9;

	@BeforeClass
	public static void setup() throws Exception {
		System.out.println("*** "+TestAccounting.class.getSimpleName()+" ***");
		Context.setDefaultReader(new MarketArchiveHttpReader());
	}

	@Test
	public void testSetup() throws SystemException {
		// seup account with an initial value of 10'000
		BasicAccount account = new BasicAccount("Simulation", "USD", 10000.00, Context.date("2000-01-01"), new PerTradeFees(10.0));
		Account ta = new Account(account);
		// set up trading execution with costs of 10 usd per trade
		ITrader pt = new PaperTrader(account);
		pt.execute();

		// check the result
		Assert.assertEquals(10000.00, ta.getCash(), delta);
		Assert.assertEquals(10000.00, ta.getTotalValue(), delta);
		Assert.assertEquals(10000.00, ta.getTotalPurchasedValue(), delta);
		Assert.assertEquals(0.00, ta.getActualValue(), delta);
		Assert.assertEquals(0.00, ta.getPurchasedValue(), delta);

		Portfolio portfolio = ta.getPortfolio(Context.date("2000-01-01"));

		Assert.assertEquals(Context.date("2000-01-01"), portfolio.getDate());
		Assert.assertEquals(10000.00, portfolio.getImpactOnCash(), delta);
		Assert.assertEquals(0.00, portfolio.getActualValue(), delta);
		Assert.assertEquals(0.00, portfolio.getPurchasedValue(), delta);
		Assert.assertEquals(0, portfolio.getFees(), delta);
		Assert.assertEquals(0.00, portfolio.getTotalProfit(), delta);
		Assert.assertEquals(0.0, portfolio.getUnrealizedGains(), delta);

	}

	@Test
	public void testBuy() throws Exception {
		BasicAccount account = new BasicAccount("Simulation", "USD", 10000.00, Context.date("2000-01-01"), new PerTradeFees(10.0));
		Account ta = new Account(account);
		StockID apple = new StockID("TEST-AAPL", "NASDAQ");

		account.addTransaction(new Transaction(Context.date("2010-01-01"), apple, 100));

		// set up trading execution with costs of 10 usd per trade
		ITrader pt = new PaperTrader(account);
		pt.execute();

		// check the result
		Portfolio portfolio = ta.getPortfolio(Context.date("2010-01-04"));
		PortfolioStockInfo line = portfolio.getInfo(apple);

		ta.getTransactions().forEach(t -> System.out.println(t));
		
		Assert.assertEquals(0, line.getUnrealizedGains(), delta);
		Assert.assertEquals(-(27.406532287597656 * 100.00) - 10.00, line.getImpactOnCash(), delta);
		Assert.assertEquals(27.406532287597656, line.getPurchasedAveragePrice(), delta);
		Assert.assertEquals(100, line.getQuantity(), delta);
		Assert.assertEquals(10.00, line.getFees(), delta);

		Assert.assertEquals(Context.date("2010-01-04"), portfolio.getDate());
		Assert.assertEquals(2740, portfolio.getActualValue(), delta);
		Assert.assertEquals(27.406532287597656 * 100.00, portfolio.getPurchasedValue(), delta);
		Assert.assertEquals(10.0, portfolio.getFees(), delta);
		Assert.assertEquals(-10.00, portfolio.getTotalProfit(), delta);
		Assert.assertEquals(0.0, portfolio.getUnrealizedGains(), delta);
		Assert.assertEquals(0.0, portfolio.getRealizedGains(), delta);

		Assert.assertEquals(10000.00 - (27.406532287597656 * 100.00) - 10.00, ta.getCash(), delta);

		Assert.assertEquals(ta.getTotalProfit(), ta.getRealizedGain() + ta.getUnrealizedGain() - ta.getTotalFees(),
				delta);

	}

	@Test
	public void testBuyAndSell() throws Exception {
		BasicAccount account = new BasicAccount("Simulation","USD", 10000.00, Context.date("2010-01-01"),new PerTradeFees(10.0));	
		Account  ta = new Account(account);
		StockID apple = new StockID("TEST-AAPL", "NASDAQ");

		account.addTransaction(new Transaction(Context.date("2010-01-01"), apple, 100));
		account.addTransaction(new Transaction(Context.date("2010-10-01"), apple, -90));

		// set up trading execution with costs of 10 usd per trade
		ITrader pt = new PaperTrader(account);
		pt.execute();
		
		ta.getTransactions().forEach(t -> System.out.println(t));


		// check the result
		Portfolio portfolio = ta.getPortfolio(Context.date("2010-10-01"));
		Assert.assertEquals(Context.date("2010-10-01"), portfolio.getDate());

		PortfolioStockInfo line = portfolio.getInfo(apple);

		Assert.assertEquals(36.18, line.getCurrentPrice(), delta);
		Assert.assertEquals(10.0 * 36.18, line.getActualValue(), delta);
		Assert.assertEquals(274, line.getPurchasedValue(), delta);
		Assert.assertEquals(87.73, line.getUnrealizedGains(), delta);
		Assert.assertEquals(789.61, line.getRealizedGains(), delta);
		Assert.assertEquals(27.40, line.getPurchasedAveragePrice(), delta);
		Assert.assertEquals(10.0, line.getQuantity(), delta);
		Assert.assertEquals(20.00, line.getFees(), delta);
		Assert.assertEquals(495.55, line.getImpactOnCash(), delta);

		Assert.assertEquals(361.8, portfolio.getActualValue(), delta);
		Assert.assertEquals(274.0, portfolio.getPurchasedValue(), delta);
		Assert.assertEquals(20.0, portfolio.getFees(), delta);
		Assert.assertEquals(87.73, portfolio.getUnrealizedGains(), delta);
		Assert.assertEquals(789.61, portfolio.getRealizedGains(), delta);

		Assert.assertEquals(10495.55, ta.getCash(), delta);

		Assert.assertEquals(857.35, portfolio.getTotalProfit(), delta);

		Assert.assertEquals(ta.getTotalProfit(), ta.getRealizedGain() + ta.getUnrealizedGain() - ta.getTotalFees(),
				delta);

	}

	@Test
	public void testBuyAndBuy() throws Exception {
		BasicAccount account = new BasicAccount("Simulation","USD", 10000.00, Context.date("2010-01-01"),new PerTradeFees(10.0));	
		Account  ta = new Account(account);
		StockID apple = new StockID("TEST-AAPL", "NASDAQ");

		account.addTransaction(new Transaction(Context.date("2010-01-01"), apple, 100));
		account.addTransaction(new Transaction(Context.date("2010-10-01"), apple, 10));

		// set up trading execution with costs of 10 usd per trade
		ITrader pt = new PaperTrader(account);
		pt.execute();

		ta.getTransactions().forEach(t -> System.out.println(t));

		// check the result
		Portfolio portfolio = ta.getPortfolio(Context.date("2010-10-01"));
		Assert.assertEquals(Context.date("2010-10-01"), portfolio.getDate());

		PortfolioStockInfo line = portfolio.getInfo(apple);

		Assert.assertEquals(36.180, line.getCurrentPrice(), delta);
		Assert.assertEquals(110.0 * 36.180, line.getActualValue(), delta);
		Assert.assertEquals(3102, line.getPurchasedValue(), delta);
		Assert.assertEquals(877.3, line.getUnrealizedGains(), delta);
		Assert.assertEquals(0.0, line.getRealizedGains(), delta);
		Assert.assertEquals((100.0 * 28.313195 + 10.0 * 36.180) / 110.0, line.getPurchasedAveragePrice(), delta);
		Assert.assertEquals(110.0, line.getQuantity(), delta);
		Assert.assertEquals(20.00, line.getFees(), delta);
		Assert.assertEquals(-3122.45, line.getImpactOnCash(), delta);
		Assert.assertEquals(2L, line.getNumberOfTrades());

		Assert.assertEquals(36.180 * 110.00, portfolio.getActualValue(), delta);
		Assert.assertEquals(3102.4, portfolio.getPurchasedValue(), delta);
		Assert.assertEquals(20.0, portfolio.getFees(), delta);
		Assert.assertEquals(877.3, portfolio.getUnrealizedGains(), delta);
		Assert.assertEquals(0.0, portfolio.getRealizedGains(), delta);
		Assert.assertEquals(2L, portfolio.getNumberOfTrades(), delta);

		Assert.assertEquals(6877.54, ta.getCash(), delta);

		Assert.assertEquals(857.35, portfolio.getTotalProfit(), delta);

		Assert.assertEquals(ta.getTotalProfit(), ta.getRealizedGain() + ta.getUnrealizedGain() - ta.getTotalFees(),
				delta);

	}

	@Test
	public void testCount() {
		BasicAccount account = new BasicAccount("Simulation","USD", 10000.00, Context.date("2015-01-01"),new PerTradeFees(10.0));	
		Account  ta = new Account(account);
		StockID apple = new StockID("TEST-AAPL", "NASDAQ");

		account.addTransaction(new Transaction(Context.date("2010-01-01"), apple, 100));
		account.addTransaction(new Transaction(Context.date("2010-10-01"), apple, -90));
		account.addTransaction(new Transaction(Context.date("2010-12-01"), apple, -10));

		// set up trading execution with costs of 10 usd per trade
		ITrader pt = new PaperTrader(account);
		pt.execute();

		ta.getTransactions().forEach(t -> System.out.println("**"+t));

		
		Portfolio portfolio = ta.getPortfolio();
		PortfolioStockInfo line = portfolio.getInfo(apple);
		Assert.assertEquals(3L, portfolio.getNumberOfTrades());
		Assert.assertEquals(3L, line.getNumberOfTrades());

		Assert.assertEquals(ta.getTotalProfit(), ta.getRealizedGain() + ta.getUnrealizedGain() - ta.getTotalFees(),
				delta);

	}

	@Test
	public void testOrderBuyLimit() {
		BasicAccount account = new BasicAccount("Simulation","USD", 10000.00, Context.date("2015-01-01"),new PerTradeFees(10.0));	
		Account  ta = new Account(account);
		StockID apple = new StockID("TEST-AAPL", "NASDAQ");

		// limit buy -> the price must be lower as current price which is 121.48452758789062
		Transaction order = new Transaction(Context.date("2015-02-17"), apple,  1, 120.20, Transaction.Type.Limit);
		account.addTransaction(order);

		ITrader pt = new PaperTrader(account);
		pt.execute();
		
		ta.getTransactions().forEach(t -> System.out.println("**"+t));


		Assert.assertEquals(Context.date("2015-03-5"), Context.date(order.getDate()));
		Assert.assertEquals(120.13504028320312, order.getFilledPrice(), delta);

		Assert.assertEquals(ta.getTotalProfit(), ta.getRealizedGain() + ta.getUnrealizedGain() - ta.getTotalFees(),
				delta);

	}

	@Test
	public void testOrderBuyStop() {
		BasicAccount account = new BasicAccount("Simulation","USD", 10000.00, Context.date("2015-01-01"),new PerTradeFees(10.0));	
		Account  ta = new Account(account);
		StockID apple = new StockID("TEST-AAPL", "NASDAQ");

		// stop buy -> the price must be above the indicated price / at closing price of
		// 125.503506
		Transaction order = new Transaction(Context.date("2015-02-17"), apple,  1, 126.00, Transaction.Type.Stop);
		account.addTransaction(order);

		ITrader pt = new PaperTrader(account);
		pt.execute();
		
		ta.getTransactions().forEach(t -> System.out.println("**"+t));


		Assert.assertEquals(Context.date("2015-02-23"), Context.date(order.getDate()));
		Assert.assertEquals(126.39789581298828, order.getFilledPrice(), delta);

		Assert.assertEquals(ta.getTotalProfit(), ta.getRealizedGain() + ta.getUnrealizedGain() - ta.getTotalFees(),
				delta);

	}

	@Test
	public void testOrderSellLimit() {
		BasicAccount account = new BasicAccount("Simulation","USD", 10000.00, Context.date("2015-01-01"), new PerTradeFees(10.0));	
		Account  ta = new Account(account);
		StockID apple = new StockID("TEST-AAPL", "NASDAQ");
		IReader r = new MarketArchiveHttpReader();

		// we buy at closing price of 121.48452758789062
		account.addTransaction(new Transaction(Context.date("2015-02-17"), apple, 1));
		// limit sell -> the price must be higher
		Transaction order = new Transaction(Context.date("2015-02-17"), apple, -1, 126.00, Transaction.Type.Limit);
		account.addTransaction(order);

		ITrader pt = new PaperTrader(account);
		pt.execute();

		ta.getTransactions().forEach(t -> System.out.println("**"+t));

		Assert.assertTrue(order.isFilled());
		Assert.assertEquals(Context.date("2015-02-23"), Context.date(order.getDate()));
		Assert.assertEquals(126.39789581298828, order.getFilledPrice(), delta);

		Assert.assertEquals(ta.getTotalProfit(), ta.getRealizedGain() + ta.getUnrealizedGain() - ta.getTotalFees(),
				delta);


	}

	@Test
	public void testOrderSellStop() {
		BasicAccount account = new BasicAccount("Simulation","USD", 10000.00, Context.date("2015-01-01"), new PerTradeFees(10.0));	
		Account  ta = new Account(account);
		StockID apple = new StockID("TEST-AAPL", "NASDAQ");

		// we buy at closing price of 121.48452758789062
		account.addTransaction(new Transaction(Context.date("2015-02-17"), apple,  1));
		// stop sell -> the price must be lower
		Transaction order = new Transaction(Context.date("2015-02-20"), apple,  -1, 121.0, Transaction.Type.Stop);
		account.addTransaction(order);

		ITrader pt = new PaperTrader(account);
		pt.execute();
		
		ta.getTransactions().forEach(t -> System.out.println("**"+t));

		Assert.assertTrue(order.isFilled());
		Assert.assertEquals(Context.date("2015-03-05"), Context.date(order.getDate()));
		Assert.assertEquals(120.13504028320312, order.getFilledPrice(), delta);

		Assert.assertEquals(ta.getTotalProfit(), ta.getRealizedGain() + ta.getUnrealizedGain() - ta.getTotalFees(),
				delta);

	}


}
