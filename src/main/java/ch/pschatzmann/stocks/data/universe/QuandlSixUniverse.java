package ch.pschatzmann.stocks.data.universe;

import ch.pschatzmann.stocks.input.QuandlSixReader;

/**
 * Six Data from Quandl
 * 
 * @author pschatzmann
 *
 */
public class QuandlSixUniverse extends QuandlCommonUniverse {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public QuandlSixUniverse() {
		super("SIX",new QuandlSixReader());
	}
	
	public QuandlSixUniverse(String regex) {
		super("SIX",new QuandlSixReader(),regex);	
	}

}
