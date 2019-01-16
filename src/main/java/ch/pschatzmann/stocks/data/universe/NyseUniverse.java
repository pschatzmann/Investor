package ch.pschatzmann.stocks.data.universe;

import ch.pschatzmann.stocks.errors.UniverseException;

/**
 * List of symbols traded on the NYSE
 * 
 * @author pschatzmann
 *
 */
public class NyseUniverse extends NasdaqUniverse {

	public NyseUniverse() throws UniverseException {
		super("NYSE");
	}

}
