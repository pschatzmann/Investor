package ch.pschatzmann.stocks.strategy;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.Strategy;

import ch.pschatzmann.dates.DateRange;
import ch.pschatzmann.stocks.Context;
import ch.pschatzmann.stocks.IStockData;
import ch.pschatzmann.stocks.parameters.State;
import ch.pschatzmann.stocks.strategy.optimization.IOptimizableTradingStrategy;
import ch.pschatzmann.stocks.strategy.optimization.IOptimizer;
import ch.pschatzmann.stocks.strategy.optimization.InputParameterName;

/**
 * Performs an optimization for the indicated period. Alternatively we can run
 * the optimization on the indicated schedule intervals
 * 
 * @author pschatzmann
 *
 */
public class OptimizedStrategy implements IOptimizableTradingStrategy, ITradeEvent {
	private static Logger LOG = LoggerFactory.getLogger(OptimizedStrategy.class);
	
	public enum Schedule {
		MONTH, WEEK, DAY, YEAR, ONCE
	}

	private IOptimizableTradingStrategy strategy;
	private IOptimizer optimizer;
	private Schedule schedule = Schedule.ONCE;
	private String lastYear = "";
	private String lastMonth = "";
	private String lastDay = "";
	private Object lastWeek = "";
	private static int numberOfMonths = 12;
	private DateRange optimizationPeriod;

	/**
	 * Constructor for scheduled OptimizedStrategy
	 * 
	 * @param strategy
	 * @param optimizer
	 * @param schedule
	 */
	public OptimizedStrategy(IOptimizableTradingStrategy strategy, IOptimizer optimizer, Schedule schedule) {
		this.strategy = strategy;
		this.schedule = schedule;
		this.optimizer = optimizer;
		if (schedule==Schedule.ONCE) {
			optimize();
		}
	}

	/**
	 * Constructor for OptimizedStrategy which uses data of the indicated
	 * optimizationPeriod
	 * 
	 * @param strategy
	 * @param optimizer
	 * @param optimizationPeriod
	 */
	public OptimizedStrategy(IOptimizableTradingStrategy strategy, IOptimizer optimizer) {
		this(strategy, optimizer, Schedule.ONCE);
	}

	public OptimizedStrategy(IOptimizableTradingStrategy strategy, IOptimizer optimizer, DateRange period) {
		this.strategy = strategy;
		this.schedule = Schedule.ONCE;
		this.optimizer = optimizer;
		this.optimizationPeriod = period;
		optimize();		
	}

	@Override
	public Strategy getStrategy() {
		return strategy.getStrategy();
	}

	@Override
	public IStockData getStockData() {
		return strategy.getStockData();
	}

	@Override
	public String getName() {
		return strategy.getName();
	}

	@Override
	public String getDescription() {
		return strategy.getDescription();
	}


	@Override
	public State getParameters() {
		return strategy.getParameters();
	}

	@Override
	public void setParameters(State state) {
		strategy.setParameters(state);
	}

	@Override
	public List<InputParameterName> getParameterOptimizationSequence() {
		return strategy.getParameterOptimizationSequence();
	}

	@Override
	public void reset() {
		strategy.reset();
	}

	@Override
	public void beforeTrade(Date date) {
		if (schedule != Schedule.ONCE) {
			if (schedule == Schedule.WEEK && weekChange(date) || schedule == Schedule.DAY && dayChange(date)
					|| schedule == Schedule.MONTH && monthChange(date)
					|| schedule == Schedule.YEAR && yearChange(date)) {

				Date endDate = Context.getDateWithOffsetDays(date, -1);
				this.optimizationPeriod = new DateRange(Context.getDateWithOffsetMonths(endDate, -numberOfMonths),
						endDate);
				optimize();
			}
		}
	}

	private void optimize() {
		optimizer.optimize(strategy, optimizationPeriod);
	}


	private boolean yearChange(Date date) {
		String dateString = Context.format(date).substring(0, 4);
		boolean result = !lastYear.equals(dateString);
		lastYear = dateString;
		return result;
	}

	private boolean monthChange(Date date) {
		String dateString = Context.format(date).substring(0, 7);
		boolean result = !lastMonth.equals(dateString);
		lastMonth = dateString;
		return result;
	}

	private boolean dayChange(Date date) {
		String dateString = Context.format(date);
		boolean result = !lastDay.equals(dateString);
		lastDay = dateString;
		return result;
	}

	private boolean weekChange(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		String weekString = "" + c.get(Calendar.WEEK_OF_YEAR);
		boolean result = !lastWeek.equals(weekString);
		lastWeek = weekString;
		return result;
	}

	@Override
	public void resetHistory() {
		this.strategy.resetHistory();
		
	}

}
