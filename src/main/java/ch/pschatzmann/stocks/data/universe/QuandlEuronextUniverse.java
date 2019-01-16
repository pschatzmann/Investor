package ch.pschatzmann.stocks.data.universe;

import ch.pschatzmann.stocks.input.IReader;
import ch.pschatzmann.stocks.input.QuandlEuronextReader;

/**
 * Provides the stocks which are available via the Quandl WIKI database
 * 
 * @author pschatzmann
 *
 */

public class QuandlEuronextUniverse extends QuandlCommonUniverse {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private IReader reader = new QuandlEuronextReader();

	public QuandlEuronextUniverse() {
		super("EURONEXT",new QuandlEuronextReader());
	}

}
