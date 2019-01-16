package ch.pschatzmann.stocks.strategy.optimization;

import java.io.IOException;
import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.pschatzmann.dates.DateRange;
import ch.pschatzmann.stocks.accounting.kpi.KPI;
import ch.pschatzmann.stocks.parameters.ParameterValue;
import ch.pschatzmann.stocks.parameters.Parameters;
import ch.pschatzmann.stocks.parameters.State;

/**
 * We try all combinations of all parameter values in order to find the optimum.
 * This optimizer is working also if the different parameters are impacting each
 * other. If suffers from combinatorial explosion and takes very long if there
 * are more then 2 parameters to optimize. By default we increment the parameters
 * by the smallest defined digit.
 * 
 * In order to speed up the processing you can indicate the maximum number of steps.  
 * 
 * @author pschatzmann
 *
 */

public class BruteForceOptimizer implements IOptimizer, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(BruteForceOptimizer.class);
	private State maxResult = null;
	private KPI optimizationParameter;
	private IFitness fitness;
	private Integer maxNumberOfSteps;

	public BruteForceOptimizer(IFitness fitness, KPI optimizationParameter) throws IOException {
		this.optimizationParameter = optimizationParameter;
		this.fitness = fitness;
	}

	@Override
	public State optimize(IOptimizableTradingStrategy ts, DateRange period) {
		if (ts.getParameters().input().names().size() > 5) {
			throw new RuntimeException("Only 5 parameters are supported to be optimized");
		}
		this.fitness.getTrader().getAccount().putStockData(ts.getStockData());

		ParameterValue p1 = getParameterValueForIndex(ts, 0);
		ParameterValue p2 = getParameterValueForIndex(ts, 1);
		ParameterValue p3 = getParameterValueForIndex(ts, 2);
		ParameterValue p4 = getParameterValueForIndex(ts, 3);
		ParameterValue p5 = getParameterValueForIndex(ts, 4);

		// use standard parameters
		determineMaxResult(ts, period);

		for (double d1 = p1.getRange().getMin().doubleValue(); d1 <= p1.getRange().getMax().doubleValue(); d1 += this
				.getStep(p1)) {
			p1.setValue(d1);
			for (double d2 = p2.getRange().getMin().doubleValue(); d2 <= p2.getRange().getMax()
					.doubleValue(); d2 += this.getStep(p2)) {
				p2.setValue(d2);
				for (double d3 = p3.getRange().getMin().doubleValue(); d3 <= p3.getRange().getMax()
						.doubleValue(); d3 += this.getStep(p3)) {
					p3.setValue(d3);
					for (double d4 = p4.getRange().getMin().doubleValue(); d4 <= p4.getRange().getMax()
							.doubleValue(); d4 += this.getStep(p4)) {
						p4.setValue(d4);
						for (double d5 = p5.getRange().getMin().doubleValue(); d5 <= p5.getRange().getMax()
								.doubleValue(); d5 += this.getStep(p5)) {
							p4.setValue(d5);
							determineMaxResult(ts, period);
						}
					}
				}
			}
		}

		ts.getParameters().input().setParameters(maxResult.getInput().getParameters());	
		return maxResult;
	}

	protected void determineMaxResult(IOptimizableTradingStrategy ts, DateRange period) {
		ts.reset();
		State fitnessState = fitness.getFitness(ts, period);
		double value = fitnessState.result().getDouble(optimizationParameter);
		LOG.info("{}",fitnessState);
		if (maxResult == null || value > maxResult.result().getDouble(optimizationParameter)) {
			maxResult = fitnessState;
			LOG.info("*** New maximum -> " + maxResult.result().getDouble(optimizationParameter));
		}
	}

	protected ParameterValue getParameterValueForIndex(IOptimizableTradingStrategy ts, int index) {
		ParameterValue result;
		Parameters p = ts.getParameters().input();
		if (index < p.names().size()) {
			result = (ParameterValue) p.values().get(index);
		} else {
			result = new ParameterValue(0.0, 0.0, 0.0, 0);
		}
		return result;
	}

	/**
	 * Determines the increment in which we increase the values E.g if we have 2
	 * decimals we perform the iteration in steps of 0.01
	 * 
	 * @param s
	 * @return
	 */
	protected double getStep(ParameterValue s) {
		double result =  1.0 / Math.pow(10, s.decimals());
		if (this.getMaxNumberOfSteps()!=null) {
			result = Double.max(result, (s.getRange().getMax().doubleValue()-s.getRange().getMin().doubleValue())/maxNumberOfSteps.doubleValue());
		}		
		return result;
	}
	
	public Integer getMaxNumberOfSteps() {
		return maxNumberOfSteps;
	}

	/**
	 * In order to minimize the number of checks we can indicate the maximum number of steps for each parameter 
	 * @param maxNumberOfSteps
	 */
	public void setMaxNumberOfSteps(Integer maxNumberOfSteps) {
		this.maxNumberOfSteps = maxNumberOfSteps;
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
