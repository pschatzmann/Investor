package ch.pschatzmann.stocks.test;

import org.junit.Assert;
import org.junit.Test;

import ch.pschatzmann.stocks.data.index.DowJonesIndex;
import ch.pschatzmann.stocks.data.index.IIndex;
import ch.pschatzmann.stocks.data.index.Nasdaq100Index;
import ch.pschatzmann.stocks.data.index.SMI;
import ch.pschatzmann.stocks.data.index.SP500Index;

public class TestIndex {
	@Test
	public void testSMI() throws Exception {
		IIndex idx = new SMI();
		System.out.println(idx.list());
		Assert.assertFalse(idx.list().isEmpty());
	}

	@Test
	public void testNasdaq() throws Exception {
		IIndex idx = new Nasdaq100Index();
		System.out.println(idx.list());
		Assert.assertFalse(idx.list().isEmpty());
	}

	@Test
	public void testDowJonesIndex() throws Exception {
		IIndex idx = new DowJonesIndex();
		System.out.println(idx.list());
		Assert.assertFalse(idx.list().isEmpty());
	}

	@Test
	public void testDowJSP500Index() throws Exception {
		IIndex idx = new SP500Index();
		System.out.println(idx.list());
		Assert.assertFalse(idx.list().isEmpty());
	}

}
