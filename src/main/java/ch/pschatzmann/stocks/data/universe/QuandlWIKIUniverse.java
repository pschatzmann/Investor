package ch.pschatzmann.stocks.data.universe;

import ch.pschatzmann.stocks.input.IReader;
import ch.pschatzmann.stocks.input.QuandlWIKIReader;

/**
 * Provides the stocks which are available via the Quandl WIKI database
 * 
 * @author pschatzmann
 *
 */

public class QuandlWIKIUniverse extends QuandlCommonUniverse {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private IReader reader = new QuandlWIKIReader();

	public QuandlWIKIUniverse() {
		super("WIKI", new QuandlWIKIReader() );
	}

}
