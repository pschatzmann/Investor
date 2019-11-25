package ch.pschatzmann.stocks.strategy;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.Strategy;
import org.ta4j.core.BarSeries;

import ch.pschatzmann.stocks.Context;
import ch.pschatzmann.stocks.IStockData;
import ch.pschatzmann.stocks.IStockID;
import ch.pschatzmann.stocks.StockData;
import ch.pschatzmann.stocks.data.universe.IUniverse;
import ch.pschatzmann.stocks.data.universe.RandomUniverse;
import ch.pschatzmann.stocks.errors.CommonException;
import ch.pschatzmann.stocks.errors.UniverseException;
import ch.pschatzmann.stocks.input.IReader;
import ch.pschatzmann.stocks.strategy.optimization.IOptimizableTradingStrategy;
import ch.pschatzmann.stocks.utils.FileUtils;

/**
 * Factory which lists and creates the available trading strategies
 * 
 * @author pschatzmann
 *
 */
public class TradingStrategyFactory {
	private static final Logger LOG = LoggerFactory.getLogger(TradingStrategyFactory.class);

	public enum StrategyEnum {
		CCICorrectionStrategy, GlobalExtremaStrategy, MovingMomentumStrategy, RSI2Strategy
	};

	/**
	 * Creates a single ITradingStrategy
	 * @param strategy
	 * @param stockData
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public static ITradingStrategy create(Class<ITradingStrategy> strategy,  IStockData stockData)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		try {
			Constructor[] ca = strategy.getDeclaredConstructors();
			if (ca.length != 1) {
				throw new IllegalArgumentException("We support only one construcutor!");
			}
			return (ITradingStrategy) ca[ca.length - 1].newInstance(stockData);
		} catch (IllegalArgumentException ex) {
			LOG.error("Illigal argument of construcotor for " + strategy);
			throw ex;
		}
	}

	/**
	 * 	 * Creates a single ITradingStrategy
	 *
	 * @param strategy
	 * @param stockData
	 * @return
	 */
	public static ITradingStrategy create(String strategy,  IStockData stockData) {
		try {
			Class<ITradingStrategy> c = (Class<ITradingStrategy>) Class
					.forName("ch.pschatzmann.stocks.strategy." + strategy);
			return create(c, stockData);
		} catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	/**
	 * 
	 * Creates a single ITradingStrategy
	 * 
	 * @param strategy
	 * @param stockData
	 * @return
	 */
	public static ITradingStrategy create(Strategy strategy, IStockData stockData) {
		return new CommonTradingStrategy(stockData) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public Strategy buildStrategy(BarSeries timeSeries) {
				return strategy;
			}			
		};		
	}

	/**
	 * Returns a detailed description for the strategy name
	 * @param strategy
	 * @return
	 */
	public static String getStrategyDesciption(String strategy) {
		URL url = TradingStrategyFactory.class.getResource("/definitions/" + strategy + ".txt");
		String result = "";
		try {
			result = FileUtils.read(url, Charset.defaultCharset());
		} catch (Exception ex) {
			 LOG.warn("Could not find description for "+strategy+": "+ex);
		}
		return result;
	}

	/**
	 * Provides a list of the supported strategy names
	 * @return
	 */	
	public static List<String> list() {
		try {
			List<String> result = new ArrayList();
			for (StrategyEnum cl : StrategyEnum.values()) {
				result.add(cl.name());
			}
			return result;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	/**
	 * Provides a list of strategies
	 * @param strategies
	 * @return
	 */
	public static List<String> list(StrategyEnum ... strategies) {
		try {
			List<String> result = new ArrayList();
			for (StrategyEnum cl : strategies) {
				result.add(cl.name());
			}
			return result;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Provides a list of strategies
	 * @param strategies
	 * @return
	 */
	public static List<String> list(String ... strategies) {
		try {
			List<String> result = new ArrayList();
			for (String cl : strategies) {
				result.add(StrategyEnum.valueOf(cl).name());
			}
			return result;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Provides a list of IOptimizableTradingStrategy
	 * @param stockData
	 * @return
	 * @throws CommonException
	 */

	public static List<IOptimizableTradingStrategy> listTradingStrategies(StockData stockData)
			throws CommonException {
		try {
			List<IOptimizableTradingStrategy> result = new ArrayList();
			for (StrategyEnum cl : StrategyEnum.values()) {
				result.add((IOptimizableTradingStrategy) create(cl.name(), stockData));
			}
			return result;
		} catch (Exception ex) {
			throw new CommonException(ex);
		}
	}
	
	/**
	 * Provides a list of randomly generated IOptimizableTradingStrategy 
	 * @param universe
	 * @param reader
	 * @param number
	 * @return
	 * @throws UniverseException
	 */
	public static List<ITradingStrategy> getRandomStrategies(IUniverse universe, IReader reader, int number) {
		return new RandomUniverse(universe, number).list().
			stream().
			map(id -> Context.getStockData(id, reader)).
			map(stockData -> create(getRandomStrategyName(), stockData)).
			collect(Collectors.toList());			  
	}

	/**
	 * Generates a list of a list of randomly generated IOptimizableTradingStrategy objects
	 * 
	 * @param universe
	 * @param reader
	 * @param number
	 * @param numberOfLists
	 * @return
	 * @throws UniverseException
	 */
	public static List<List<ITradingStrategy>> getRandomStrategiesList(IUniverse universe, IReader reader, int number, int numberOfLists) throws UniverseException{	
		return getRandomStrategiesStream(universe,reader,number,numberOfLists).collect(Collectors.toList());
	}
	
	/**
	 * Generates a stream of a list of randomly generated IOptimizableTradingStrategy objects
	 * 
	 * @param universe
	 * @param reader
	 * @param number
	 * @param numberOfLists
	 * @return
	 * @throws UniverseException
	 */
	public static Stream<List<ITradingStrategy>> getRandomStrategiesStream(IUniverse universe, IReader reader, int number, int numberOfLists) throws UniverseException{
		return IntStream.range(0, numberOfLists).mapToObj(i -> getRandomStrategies(universe,reader,number));  
	}
	
	/**
	 * Provides a random strategy name 
	 * @return
	 */
	public static String getRandomStrategyName()  {
		int randomNum = 0 + (int)(Math.random() * list().size()); 
		return list().get(randomNum);
	}

	
	/**
	 * Translates a list of stocks into a list of trading strategies
	 * 
	 * @param stocks
	 * @param reader
	 * @param strategy
	 * @return
	 */
	public static List<ITradingStrategy> getStrategies(Collection<IStockID> stocks, IReader reader, String strategy) {
		return stocks.stream().map(id -> Context.getStockData(id, reader))
				.map(stock -> TradingStrategyFactory.create(strategy, stock)).collect(Collectors.toList());

	}

}
