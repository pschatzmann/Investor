package ch.pschatzmann.stocks.strategy.optimization.annealing;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Optimizer implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(Optimizer.class);
	private State state;
	private State optState;
	private double optResult = Double.MIN_VALUE;

	public Optimizer(State initState) {
		// get initial state and the related result
		state = initState;
		optResult = state.result();
		optState = (State) state.clone();
	}

	public State search(long iterations) {
		
		for (long i = 0; i < iterations; i++) {
			double temp = (1.0 - ((double) i / (double) iterations)) * 100.0;
			state.step();
			LOG.info("{}",state);
			double nextResult = state.result();
			if (accept(optResult,nextResult,temp)) {
				LOG.info("==>accept "+nextResult);
				optState = (State) state.clone();
				optResult = nextResult;
			} else {
				LOG.info("-->undo "+nextResult);
				state.undo();
			}
		}
		return optState;
	}

	boolean accept(double current, double proposal, double temperature) {
		double prob;
		if (proposal > current)
			return true;
		if (temperature == 0.0)
			return false;
		prob = Math.exp(-(proposal - current) / temperature);
		return Math.random() < prob;
	}

}