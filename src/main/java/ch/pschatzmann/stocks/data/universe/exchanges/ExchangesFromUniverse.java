package ch.pschatzmann.stocks.data.universe.exchanges;

import java.io.Serializable;
import java.util.Collection;
import java.util.TreeSet;
import java.util.stream.Collectors;

import ch.pschatzmann.stocks.data.universe.IUniverse;
import ch.pschatzmann.stocks.errors.UniverseException;

/**
 * Determines the Exchanges which are provided by the universe
 * @author pschatzmann
 *
 */
public class ExchangesFromUniverse implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	IUniverse u;
	
	public ExchangesFromUniverse(IUniverse u) {
		this.u = u;
	}
	
	public Collection<String> list() throws UniverseException {
		return new TreeSet(u.list().stream().map(id -> id.getExchange()).collect(Collectors.toSet()));
	} 
}
