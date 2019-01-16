package ch.pschatzmann.stocks.strategy;

import java.io.Serializable;

import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;

/**
 * eu.verdelhan.ta4j.Strategy which provides name. It used to have no name, so
 * we needed to implement this class
 * 
 * @author pschatzmann
 *
 */

public class NamedStrategy extends BaseStrategy implements Strategy, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NamedStrategy(Rule entryRule, Rule exitRule, String name) {
		super(name, entryRule, exitRule);
	}

}
