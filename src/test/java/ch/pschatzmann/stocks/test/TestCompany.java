package ch.pschatzmann.stocks.test;

import java.io.IOException;
import java.net.MalformedURLException;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import ch.pschatzmann.stocks.company.Company;

public class TestCompany {


	@Test
	public void testAAPL() throws JsonParseException, JsonMappingException, MalformedURLException, IOException {
		Company c = new Company("AAPL");
		System.out.println(c.getIndustry());
		System.out.println(c.getSector());
		
		Assert.assertNotNull(c.getCompanyName());
		Assert.assertNotNull(c.getDescription());
		Assert.assertNotNull(c.getExchange());
		Assert.assertNotNull(c.getIndustry());
		Assert.assertNotNull(c.getSector());
		Assert.assertNotNull(c.getTicker());
		
	}

}
