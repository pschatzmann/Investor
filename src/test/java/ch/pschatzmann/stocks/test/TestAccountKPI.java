package ch.pschatzmann.stocks.test;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.pschatzmann.stocks.Context;
import ch.pschatzmann.stocks.StockID;
import ch.pschatzmann.stocks.accounting.Account;
import ch.pschatzmann.stocks.accounting.BasicAccount;
import ch.pschatzmann.stocks.accounting.Transaction;
import ch.pschatzmann.stocks.accounting.kpi.KPIValue;
import ch.pschatzmann.stocks.errors.SystemException;
import ch.pschatzmann.stocks.execution.ITrader;
import ch.pschatzmann.stocks.execution.PaperTrader;
import ch.pschatzmann.stocks.execution.fees.PerTradeFees;
import ch.pschatzmann.stocks.execution.price.RandomPrice;
import ch.pschatzmann.stocks.input.MarketArchiveHttpReader;
import ch.pschatzmann.stocks.input.QuandlWIKIReader;

public class TestAccountKPI {
	private static final Logger LOG = LoggerFactory.getLogger(TestAccountKPI.class);
	private Double delta = 0.001;

	@BeforeClass 
	public static void setup() throws SystemException{
		System.out.println("*** "+TestAccountKPI.class.getSimpleName()+" ***");
		Context.setDefaultReader(new MarketArchiveHttpReader());
	}	

	@Test
	public void testKPIApple() {
		BasicAccount account = new BasicAccount("Simulation","USD", 10000.00, Context.date("2015-01-01"),new PerTradeFees(10.0));	
		Account  ta = new Account(account);
		account.setCloseDate(Context.date("2016-04-04"));
		StockID apple = new StockID("AAPL","NASDAQ");
	
		account.addTransaction(new Transaction(Context.date("2015-02-01"), apple, 100));
		
		ITrader pt = new PaperTrader(account);
		pt.execute();
		
		// calculate the kpis
		List<KPIValue> result = ta.getKPIValues();
		for (KPIValue v : result) {
			LOG.info("*** "+v+" "+v.getKpi()); 
		}
				
	}
	
	
	@Test
	public void testKPIAppleRandom() {
		BasicAccount account = new BasicAccount("Simulation","USD", 10000.00, Context.date("2015-01-01"),new PerTradeFees(10.0));	
		Account  ta = new Account(account);
		
		account.setCloseDate(Context.date("2016-04-04"));
		StockID apple = new StockID("AAPL","NASDAQ");
	
		account.addTransaction(new Transaction(Context.date("2015-02-01"), apple, 100));
		
		PaperTrader pt = new PaperTrader(account);
		pt.setPrice(new RandomPrice());

		pt.execute();
		
		// calculate the kpis
		List<KPIValue> result = ta.getKPIValues();
		for (KPIValue v : result) {
			LOG.info("*** "+v+" "+v.getKpi()); 
		}
		
	}
	
	
	@Test
	public void testKPIAppleQuandl() {
		BasicAccount account = new BasicAccount("Simulation","USD", 10000.00, Context.date("2015-01-01"),new PerTradeFees(10.0));	
		Account  ta = new Account(account);
		
		account.setCloseDate(Context.date("2016-04-04"));
		StockID apple = new StockID("AAPL","NASDAQ");
		ta.putReader(apple, new QuandlWIKIReader());
	
		account.addTransaction(new Transaction(Context.date("2015-02-01"), apple, 100));
		
		ITrader pt = new PaperTrader(account);
		pt.execute();
		
		// calculate the kpis
		List<KPIValue> result = ta.getKPIValues();
		for (KPIValue v : result) {
			LOG.info("*** "+v+" "+v.getKpi()); 
		}
	}
	
}
