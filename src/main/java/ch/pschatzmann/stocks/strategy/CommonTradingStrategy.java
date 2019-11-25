package ch.pschatzmann.stocks.strategy;

import java.io.Serializable;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.Strategy;
import org.ta4j.core.BarSeries;
import org.ta4j.core.num.Num;

import ch.pschatzmann.stocks.Context;
import ch.pschatzmann.stocks.IResettable;
import ch.pschatzmann.stocks.IStockData;
import ch.pschatzmann.stocks.integration.StockTimeSeries;
import ch.pschatzmann.stocks.parameters.State;
import ch.pschatzmann.stocks.strategy.optimization.IOptimizableTradingStrategy;
import ch.pschatzmann.stocks.strategy.optimization.InputParameterName;

/**
 * Common functionality for trading strategies.
 * 
 * @author pschatzmann
 *
 */
public abstract class CommonTradingStrategy implements IOptimizableTradingStrategy, Serializable {
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(CommonTradingStrategy.class);
	private IStockData stockData;
	private State state = new State();
	private Strategy strategy = null;
	private BarSeries ts;
	private List<InputParameterName> parameterOptimizationSequence;

	/**
	 * Basic Constructor
	 * @param dateRange
	 * @param stockData
	 */
	public CommonTradingStrategy(IStockData stockData) {
		this.stockData = stockData; 
	}

	@Override
	public Strategy getStrategy() {
		if (strategy == null) {
			LOG.info("buildStrategy {}",this);
			strategy = buildStrategy(this.getBarSeries());
		}
		return strategy;
	}

	abstract public Strategy buildStrategy(BarSeries timeSeries) ;

	protected BarSeries getBarSeries() {
		if (ts==null) {
			ts = new StockTimeSeries(this.getStockData());
		}
		return ts;
	}
	
	/**
	 * Resets the strategy e.g. after the parameters have been updated
	 */
	@Override
	public void reset() {
		LOG.info("reset "+this);
		this.strategy = null;
		this.getParameters().result().clear();
	}
	
	@Override
	public void resetHistory(){
		LOG.info("resetHistory "+this);
		this.reset();
		this.ts = null;
		IStockData sd = this.getStockData();
		if (sd instanceof IResettable) {
			((IResettable) sd).reset();
		}		
	}

	@Override
	public IStockData getStockData() {
		return this.stockData;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(this.getStockData().getStockID());
		sb.append("/");
		sb.append(getName());
		return sb.toString();
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}
	
	@Override
	public State getParameters() {
		return this.state;
	}

	@Override
	public String getDescription() {
		String strategyName = this.getClass().getSimpleName() + ".txt";
		return TradingStrategyFactory.getStrategyDesciption(strategyName);
	}

	@Override
	public void setParameters(State s) {
		this.state = s;
	}

	protected Integer getInteger(InputParameterName name) {
		return getParameters().input().getInteger(name);
	}

	protected Double getDouble(InputParameterName name) {
		return getParameters().input().getDouble(name);
	}

	protected Num getDecimal(InputParameterName name) {
		return Context.number(getDouble(name));
	}
	
	@Override
	public List<InputParameterName> getParameterOptimizationSequence() {
		return parameterOptimizationSequence;
	}
	
	public void setParameterOptimizationSequence(List<InputParameterName> seq) {
		this.parameterOptimizationSequence = seq;
	}
	
	@Override
	public int hashCode(){
		return getStrategy().getClass().hashCode() + stockData.getStockID().hashCode();
	}
	
    @Override
    public boolean equals(Object obj) {
		boolean result = false;
		if (obj instanceof IOptimizableTradingStrategy) {
			IOptimizableTradingStrategy objs = (IOptimizableTradingStrategy) obj;
			result = this.getClass().equals(objs.getClass()) && this.getStockData().getStockID().equals(objs.getStockData().getStockID());
		}
		LOG.info(this+" equals "+obj +" -> "+result);
		return result;
	}

}
