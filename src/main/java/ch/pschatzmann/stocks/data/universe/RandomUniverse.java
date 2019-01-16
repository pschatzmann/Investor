package ch.pschatzmann.stocks.data.universe;

import java.io.Serializable;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import ch.pschatzmann.stocks.IStockID;
import ch.pschatzmann.stocks.errors.UniverseException;

/**
 * Generates a random selection of stocks
 * 
 * @author pschatzmann
 *
 */
public class RandomUniverse implements IUniverse, Serializable {
	private static final long serialVersionUID = 1L;
	private List<IStockID> result;

	public RandomUniverse(IUniverse source, int size)  {
		List<IStockID> sourceList = source.list().stream().collect(Collectors.toList());
		Random rand = new Random();
		List<IStockID> wordList = rand.ints(size, 0, sourceList.size()).mapToObj(i -> sourceList.get(i))
				.collect(Collectors.toList());

		result = wordList;
	}


	@Override
	public List<IStockID> list() {
		return result;
	}

}
