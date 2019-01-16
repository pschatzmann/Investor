package ch.pschatzmann.stocks.data.universe;

import ch.pschatzmann.stocks.input.QuandlBSEReader;

/**
 * Provides all stocks from the Quandl BSE database
 * @author pschatzmann
 *
 */
public class QuandlBSEUniverse extends QuandlCommonUniverse {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public QuandlBSEUniverse() {
		super("BSE",new QuandlBSEReader());
	}
	

}

