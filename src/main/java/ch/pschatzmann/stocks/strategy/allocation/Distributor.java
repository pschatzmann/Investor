package ch.pschatzmann.stocks.strategy.allocation;

import ch.pschatzmann.stocks.strategy.ITradingStrategy;

/**
 * Distributes the values based on a predefined weight. The default weight is
 * 1.0 which means that all entries which are (without defining a separate
 * weight) are evenly distributed.
 */

public class Distributor extends EvenDistributor {

	/**
	 * Standard constructor
	 */
	public Distributor() {
	}

	/**
	 * Constructor whith a predefined default weight.
	 * @param defaultWeight
	 */
	public Distributor(double defaultWeight) {
		super.setDefaultValue(defaultWeight);
	}

	/**
	 * Adds a new strategy with a predefined weight
	 * @param id
	 * @param weight
	 */
	public void add(ITradingStrategy id, double weight) {
		if (weight>=0.0) {
			super.getDistribution().put(id, weight);
			super.getActualDistribution().put(id, weight);
		}
	}

}
