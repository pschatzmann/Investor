package ch.pschatzmann.stocks.strategy.selection;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import ch.pschatzmann.stocks.Context;
import ch.pschatzmann.stocks.IStockData;
import ch.pschatzmann.stocks.IStockID;
import ch.pschatzmann.stocks.StockID;
import ch.pschatzmann.stocks.input.IReader;
import ch.pschatzmann.stocks.parameters.State;
import ch.pschatzmann.stocks.strategy.ITradingStrategy;
import ch.pschatzmann.stocks.strategy.TradingStrategyFactory;

/**
 * State object which privides the information for which stock and strategy.
 * 
 * @author pschatzmann
 *
 */
public class SelectionState extends State {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private StockID stockID;
	private String strategyName;
	private boolean isOptimized;

	public SelectionState() {
	}
	
	public SelectionState(IStockID id) {
		super();
		this.stockID = (StockID)id;
	}

	public SelectionState(State state, IStockID id, String strategy, boolean optimized) {
		super(state);
		this.setStockID(new StockID(id));
		this.setStrategyName(strategy);
		this.setOptimized(optimized);
	}

	public StockID getStockID() {
		return stockID;
	}

	public void setStockID(StockID stockID) {
		this.stockID = stockID;
	}

	public String getStrategyName() {
		return strategyName;
	}

	public void setStrategyName(String strategyName) {
		this.strategyName = strategyName;
	}

	public boolean isOptimized() {
		return isOptimized;
	}

	public void setOptimized(boolean isOptimized) {
		this.isOptimized = isOptimized;
	}

	@Override
	public Map<String, Object> getMap() {
		Map result = super.getMap();
		// add the additional information
		result.put("id", this.getStockID());
		result.put("strategy", this.getStrategyName());
		result.put("id", this.getStockID());
		result.put("optimized", this.isOptimized);
		return result;
	}

	/**
	 * Returns the trading strategy
	 * 
	 * @param reader
	 * @return
	 * @throws ClassNotFoundException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public ITradingStrategy getStrategy(IReader reader) {
		try {
			IStockData sd = Context.getStockData(this.stockID, reader);
			ITradingStrategy strategy = TradingStrategyFactory.create(strategyName, sd);
			// copy the optimized input paramteger form the state
			strategy.getParameters().setInput(this.getInput());
			return strategy;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		sb.append(this.getStockID());
		sb.append("/");
		sb.append(this.getStrategyName());
		sb.append("] ");
		sb.append(super.toString());
		return sb.toString();
	}
}
