package ch.pschatzmann.stocks.strategy.optimization;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dakusui.combinatoradix.Permutator;

import ch.pschatzmann.dates.DateRange;
import ch.pschatzmann.stocks.accounting.kpi.KPI;
import ch.pschatzmann.stocks.parameters.State;
import ch.pschatzmann.stocks.utils.EnumeratorAdapter;

/**
 * We optimize a single parameter using a binary search. We try out all possible parameter
 * sequence combinations to handle the case that the parameters can be dependent on each other
 * 
 * @author pschatzmann
 *
 */

public class PermutatedBinarySearchOptimizer implements IOptimizer, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(PermutatedBinarySearchOptimizer.class);
	private KPI optimizationParameter;
	private IFitness fitness;

	public PermutatedBinarySearchOptimizer(IFitness fitness, KPI targetParameterName) {
		this.optimizationParameter = targetParameterName;
		this.fitness = fitness;
	}

	@Override
	public State optimize(IOptimizableTradingStrategy ts, DateRange period) {
		this.fitness.getTrader().getAccount().putStockData(ts.getStockData());

		List<InputParameterName> sequence = ts.getParameterOptimizationSequence();
		// comparator for targetParameterName
		Comparator<State> byState = (State o1, State o2) -> ((Double) o1.getResult().getDouble(optimizationParameter))
				.compareTo(o2.getResult().getDouble(optimizationParameter));
		// find maximum combination
		Optional<State> state = generateCombinations(sequence)
				.map(seq -> new BinarySearchOptimizer(fitness, optimizationParameter).optimize(ts, period, seq))
				.max(byState);

		State result = state.get();
		ts.getParameters().input().setParameters(result.getInput().getParameters());	
		return result;
	}

	protected KPI getTargetParameterName() {
		return optimizationParameter;
	}

	@Override
	public IFitness getFitness() {
		return this.fitness;
	}

	public Stream<List<InputParameterName>> generateCombinations(List<InputParameterName> from) {
		int size = from.size();
		return new EnumeratorAdapter<>(new Permutator<>(from, size)).stream();	
	}

	@Override
	public KPI getOptimizationParameter() {
		return optimizationParameter;
	}
}
