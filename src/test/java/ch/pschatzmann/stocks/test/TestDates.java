package ch.pschatzmann.stocks.test;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.pschatzmann.dates.IDateRangeSource;
import ch.pschatzmann.dates.MonthlyDateRangeSource;
import ch.pschatzmann.dates.StandardDateRangeSource;
import ch.pschatzmann.stocks.Context;

public class TestDates {
	private static final Logger LOG = LoggerFactory.getLogger(TestDates.class);
	
	@BeforeClass 
	public static void setup() throws Exception{
		System.out.println("*** "+TestDates.class.getSimpleName()+" ***");
	}	
	
	@Test
	public void testStandardDates() throws Exception {
		IDateRangeSource dr = new StandardDateRangeSource();
		LOG.info("{}",dr.getDates());
		Assert.assertEquals(26, dr.getDates().size());

	}

	@Test
	public void testMonthlyDatesDates() throws Exception {
		IDateRangeSource dr = new MonthlyDateRangeSource(Context.date("2010-01-15"),  Context.date("2011-01-15"));
		LOG.info("{}",dr.getDates());
		Assert.assertEquals(13, dr.getDates().size());
	}

}
