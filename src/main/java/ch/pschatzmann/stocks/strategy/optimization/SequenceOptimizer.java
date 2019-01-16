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
 * We try all combinations of all parameter values in order to find the optimum. This optimizer
 * assumes that the different parameters are independent on each other.
 * 
 * @author pschatzmann
 *
 */

public class SequenceOptimizer implements IOptimizer, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(SequenceOptimizer.class);
	private State maxResult = null;
	private KPI optimizationParameter;
	private IFitness fitness;

	public SequenceOptimizer(IFitness fitness, KPI optimizationParameter) throws IOException {
		this.optimizationParameter = optimizationParameter;
		this.fitness = fitness;
	}

	@Override
	public State optimize(IOptimizableTradingStrategy ts, DateRange optimizationPeriod) {
		for (InputParameterName parameter : ts.getParameterOptimizationSequence()) {
			ParameterValue p1 = ts.getParameters().input().getParameterValue(parameter);		
			double value = fitness.getFitness(ts, optimizationPeriod).getResult().getDouble(optimizationParameter);
			Number state = p1.getValue();
			
			for (double d1 = p1.getRange().getMin().doubleValue(); d1<= p1.getRange().getMax().doubleValue(); d1+= this.getStep(p1)) {
				p1.setValue(d1);
				determineMaxResult(ts,optimizationPeriod);
			}
			
			double value2 = maxResult.getResult().getDouble(optimizationParameter);
			if (value2<=value){
				// roll back
				LOG.info("roll back");
				p1.setValue(state);
			} else {
				p1.setValue(state);
				ts.setParameters(maxResult);
			}
			
		}
		
		return maxResult;
	}	

	protected synchronized void determineMaxResult(IOptimizableTradingStrategy ts, DateRange optimizationPeriod) {
		ts.reset();
		State fitnessState = fitness.getFitness(ts,optimizationPeriod);
		double value = fitnessState.result().getDouble(optimizationParameter);
		LOG.info("{}",fitnessState);
		if (maxResult == null ||value  > maxResult.result().getDouble(optimizationParameter)) {
			LOG.info("*** New value -> "+value);
			maxResult = fitnessState;
			LOG.info("New maximum -> "+maxResult.result().getDouble(optimizationParameter));
		}
	}
	
	protected ParameterValue getParameterValueForIndex(IOptimizableTradingStrategy ts ,int index) {
		ParameterValue result;
		Parameters p = ts.getParameters().input();
		if (index < p.names().size()) {
			result = (ParameterValue) p.values().get(index);
		} else {
			result = new ParameterValue(0.0,0.0,0.0,0);
		}
		return result;
	}

	/**
	 * Determines the increment in which we increase the values
	 * E.g if we have 2 decimals we perform the iteration in steps of 0.01
	 * @param s
	 * @return
	 */
	protected double getStep(ParameterValue s) {
		return 1.0 / Math.pow(10, s.decimals());
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
