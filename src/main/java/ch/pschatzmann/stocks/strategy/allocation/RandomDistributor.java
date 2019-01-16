package ch.pschatzmann.stocks.strategy.allocation;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import ch.pschatzmann.stocks.strategy.ITradingStrategy;

/**
 * Distributes the weights randomly. We make sure that we keep the defined factor even
 * if strategies are removed and then re-added
 * 
 * @author pschatzmann
 *
 */
public class RandomDistributor extends Distributor {
	private static final long serialVersionUID = 1L;
	private static Map<ITradingStrategy,Random> randomMap = new HashMap();

	public RandomDistributor(){
	}
		
	/**
	 * Defines the Distributor with predefined weights
	 * @param initialWeights
	 */
	public RandomDistributor(Map<ITradingStrategy, Double> initialWeights) {
		this.getDistribution().putAll(initialWeights);
	}

	@Override
	public void add(ITradingStrategy id, Date date) {
		// determine a random value if nothing has been predefined
		Double value = getDistribution().get(id);
		value = value != null ? value : getRandom(id).nextDouble();		
		super.add(id, value);		
	}

	
	private Random getRandom(ITradingStrategy id) {
		Random result = randomMap.get(id);
		if (result==null) {
			result = new Random();
			randomMap.put(id, result);
		}
		return result;
	}
	
	
}
