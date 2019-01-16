package ch.pschatzmann.stocks.strategy.allocation;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.pschatzmann.stocks.strategy.ITradingStrategy;

/**
 * Distributes the values evenly. The default weight is
 * 1.0.
 */

public class EvenDistributor implements IDistributor, Serializable {
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(EvenDistributor.class);
	private double defaultWeight = 1.0;
	private Map<ITradingStrategy, Double> actualStrategies = new HashMap();
	private Map<ITradingStrategy, Double> allStrategies = new HashMap();

	/**
	 * Standard constructor
	 */
	public EvenDistributor() {
	}

	/**
	 * Constructor whith a predefined default weight.
	 * @param defaultWeight
	 */
	public EvenDistributor(double defaultWeight) {
		this.defaultWeight = defaultWeight;
	}

	@Override
	public void add(ITradingStrategy id, Date date) {
		if (allStrategies.get(id)==null) {
			// add new strategy
			if (defaultWeight>0.0 ) {
				allStrategies.put(id, defaultWeight);
				actualStrategies.put(id, defaultWeight);
			}
		} else {
			// add the oringinal weight
			Double weight = allStrategies.get(id);
			actualStrategies.put(id, weight);
		}
	}

	/*
	 * The Strategy is removed and will not be considered in subsequent allocations
	 * 
	 * @see
	 * ch.pschatzmann.stocks.strategy.allocation.IDistributor#remove(ch.pschatzmann.
	 * stocks.IStockID)
	 */
	@Override
	public void remove(ITradingStrategy id) {
		actualStrategies.remove(id);
	}

	/*
	 * Returns the allocation factor for the Strategy. The sum of all factors is 1.0
	 * 
	 * @see ch.pschatzmann.stocks.strategy.allocation.IDistributor#getFactor(ch.
	 * pschatzmann.stocks.IStockID)
	 */
	@Override
	public double getFactor(ITradingStrategy id, Date date) {
		double result = 0;
		Double rate = actualStrategies.get(id);
		if (rate != null) {
			double total = actualStrategies.values().stream().mapToDouble(value -> value).sum();
			result =  actualStrategies.get(id) / total;
		} else {
			LOG.warn("No factor found for "+id);
		}
		return result;
	}

	protected double getDefaultValue() {
		return defaultWeight;
	}

	protected void setDefaultValue(double defaultValue) {
		this.defaultWeight = defaultValue;
	}

	/**
	 * Provides the collection of all strategies that were ever used
	 */
	@Override
	public Collection<ITradingStrategy> getAllStrategies() {
		return allStrategies.keySet();
	}
	
	/**
	 * Returns the currently defined distribution
	 * @return
	 */
	protected Map<ITradingStrategy, Double> getActualDistribution() {
		return actualStrategies;
	}

	public Map<ITradingStrategy, Double> getDistribution() {
		return allStrategies;
	}

	/**
	 * Get Distribution where the total of all factors is 1 
	 * @return
	 */
	public Map<ITradingStrategy, Double> getDistributionFactors() {
		Map<ITradingStrategy, Double> result = new HashMap();
		actualStrategies.entrySet().forEach(es -> result.put(es.getKey(), getFactor(es.getKey(),null)));		
		return result;
	}

}
