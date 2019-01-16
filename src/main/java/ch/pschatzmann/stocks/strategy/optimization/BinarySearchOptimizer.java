package ch.pschatzmann.stocks.strategy.optimization;

import java.io.Serializable;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.pschatzmann.dates.DateRange;
import ch.pschatzmann.stocks.accounting.kpi.KPI;
import ch.pschatzmann.stocks.parameters.ParameterValue;
import ch.pschatzmann.stocks.parameters.State;
import ch.pschatzmann.stocks.parameters.StateComparator;
import ch.pschatzmann.stocks.strategy.selection.TopNSet;

/**
 * We optimize a single parameter using a binary search. If the new result is
 * not better we keep the original parameter values.
 * 
 * 
 * @author pschatzmann
 *
 */

public class BinarySearchOptimizer implements IOptimizer, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(BinarySearchOptimizer.class);
	private KPI optimizationParameter;
	private IFitness fitness;

	public BinarySearchOptimizer(IFitness fitness, KPI targetParameterName) {
		this.optimizationParameter = targetParameterName;
		this.fitness = fitness;
	}

	@Override
	public State optimize(IOptimizableTradingStrategy ts, DateRange period) {
		return optimize(ts, period, ts.getParameterOptimizationSequence());
	}

	public State optimize(IOptimizableTradingStrategy ts, DateRange period, Collection<InputParameterName> parameters) {
		LOG.info("=> "+ts.getName()+" optimize with " + parameters);
		this.fitness.getTrader().getAccount().putStockData(ts.getStockData());

		TopNSet<State> bestValue = new TopNSet(1, new StateComparator(false, optimizationParameter));
		State initialState = fitness.getFitness(ts, period);
		bestValue.add(initialState);
		LOG.info(" *** Initial Parameter Combination -> "+ initialState.result().getDouble(optimizationParameter));

		for (InputParameterName key : parameters) {
			State optimized = optimize(ts, key, period);
			bestValue.add(optimized);
		}

		State result = bestValue.first();
		ts.getParameters().input().setParameters(result.getInput().getParameters());	
		LOG.info("=== Result Parameter Combination => "+ result.result().getDouble(optimizationParameter));
		return result;
	}

	protected State optimize(IOptimizableTradingStrategy ts, InputParameterName parameterToChange, DateRange period) {
		TopNSet<State> bestValue = new TopNSet(1, new StateComparator(false, optimizationParameter));

		State initialState = ts.getParameters().clone();
		double oldValue = fitness.getFitness(ts, period).getResult().getDouble(optimizationParameter);
		LOG.info(parameterToChange + ": " + initialState.getInput().getValue(parameterToChange) + " -> " + oldValue);
		bestValue.add(initialState);

				
		ParameterValue pv = ts.getParameters().input().getParameterValue(parameterToChange);
		pv.setValue(pv.getRange().getMin());
		State left = fitness.getFitness(ts, period);
		bestValue.add(left);
		
		pv.setValue(pv.getRange().getMax());
		State right = fitness.getFitness(ts, period);
		bestValue.add(left);

		pv.setValue((left.input().getDouble(parameterToChange) + right.input().getDouble(parameterToChange)) / 2.0);
		State middle = fitness.getFitness(ts, period);
		bestValue.add(left);

		double lastValue = middle.input().getDouble(parameterToChange);
		int count = 0;

		while (!equals(left.input().getDouble(parameterToChange), middle.input().getDouble(parameterToChange),
				pv.decimals())
				&& !equals(right.input().getDouble(parameterToChange), middle.input().getInteger(parameterToChange),
						pv.decimals())) {
			if (middle.result().getDouble(optimizationParameter) >= left.result().getDouble(optimizationParameter)) {
				left = middle;
			} else {
				right = middle;
			}
			pv.setValue((left.input().getDouble(parameterToChange) + right.input().getDouble(parameterToChange)) / 2.0);

			middle = fitness.getFitness(ts, period);
			bestValue.add(middle);

			LOG.info(" *** " + parameterToChange + ": " + middle.input().getDouble(parameterToChange) + " -> "
					+ middle.result().getDouble(optimizationParameter) + " "
					+ middle.result().getDouble(KPI.NumberOfTrades));

			
			// limit the search to 50 instances and step if there is no change any more
			// between the steps
			if (equals(middle.input().getDouble(parameterToChange), lastValue, pv.decimals()) || ++count > 50) {
				break;
			} else {
				lastValue = middle.input().getDouble(parameterToChange);
			}
		}
		
		State result = bestValue.first();
		LOG.info(" --> " + parameterToChange + ": " + result.input().getDouble(parameterToChange) + " -> "
				+ result.result().getDouble(optimizationParameter) + " "
				+ result.result().getDouble(KPI.NumberOfTrades));

		return result;
	}

	public static boolean equals(double a, double b, int precision) {
		return Math.abs(a - b) <= Math.pow(10, -precision);
	}

	@Override
	public IFitness getFitness() {
		return this.fitness;
	}

	@Override
	public KPI getOptimizationParameter() {
		return optimizationParameter;
	}

}
