package ch.pschatzmann.stocks.strategy.optimization;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.pschatzmann.dates.DateRange;
import ch.pschatzmann.stocks.accounting.kpi.KPI;
import ch.pschatzmann.stocks.parameters.ParameterValue;
import ch.pschatzmann.stocks.parameters.State;
import ch.pschatzmann.stocks.strategy.optimization.genetics.Algorithm;
import ch.pschatzmann.stocks.strategy.optimization.genetics.Gene;
import ch.pschatzmann.stocks.strategy.optimization.genetics.Individual;
import ch.pschatzmann.stocks.strategy.optimization.genetics.Population;
import ch.pschatzmann.stocks.utils.Range;

/**
 * Optimizer which uses a genetic algorithm to optimize the parameters. 
 * https://en.wikipedia.org/wiki/Genetic_algorithm

 * 
 * @author pschatzmann
 *
 */
public class GeneticOptimizer implements IOptimizer, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(GeneticOptimizer.class);
	private KPI optimizationParameter;
	private IOptimizableTradingStrategy ts;
	private IFitness fitness;
	private int generations = 10;
	private int populationSize = 50;
	private DateRange optimizationPeriod;

	/**
	 * Default constructor
	 * @param fitness
	 * @param optimizationParameter
	 * @throws IOException
	 */
	
	public GeneticOptimizer(IFitness fitness, KPI optimizationParameter) throws IOException {
		this.optimizationParameter = optimizationParameter;
		this.fitness = fitness;
	}

	/**
	 * Starts the optimization. 
	 */
	@Override
	public State optimize(IOptimizableTradingStrategy ts, DateRange period) {	
		this.fitness.getTrader().getAccount().putStockData(ts.getStockData());
		this.optimizationPeriod = period;
		this.ts = ts;
		// setup genes from inputParameters
		Map<InputParameterName, ParameterValue<Number>> inputParameters = ts.getParameters().getInput().getParameters();
		Range[] geneTemplate = new Range[inputParameters.size()];
		String[] names = new String[inputParameters.size()];
		int j=0;
		for (Entry<InputParameterName, ParameterValue<Number>> e : inputParameters.entrySet()) {
			names[j] = e.getKey().name();
			geneTemplate[j] = e.getValue().getRange();
			j++;
 		}
		
		// setup Algorithm
		Algorithm algorithm = new Algorithm(names, geneTemplate, this);

		// Create an initial population
		Population myPop = new Population(this.getPopulationSize(), names, geneTemplate, true, this);
		
		// we use the default values on the first individual
		Individual first = myPop.getIndividual(0);
		for (int i=0; i<first.getGeneLength(); i++) {
			Gene gene = first.getGene(i);
			String name = gene.getName();
			gene.setValue(ts.getParameters().getInput().getDouble(InputParameterName.valueOf(name)));
		}
		
		// Evolve our population until we reach the indicated generation
		for (int generationCount = 0; generationCount < this.getGenerations(); generationCount++) {
			LOG.info("Generation: " + generationCount + " Fittest: " + myPop.getFittest());
			myPop = algorithm.evolvePopulation(myPop);
		}

		Individual fittest = myPop.getFittest();
		State result =  getState(fittest);
		ts.getParameters().input().setParameters(result.getInput().getParameters());	
		return result;
	}

	protected State getState(Individual indidual) {
		for (int j = 0; j < indidual.getGeneLength();j++) {
			Gene g = indidual.getGene(j);
			ts.getParameters().getInput().setValue(InputParameterName.valueOf(g.getName()), g.getValue());			
		}
		ts.reset();
		return fitness.getFitness(ts,optimizationPeriod);
	}
	
	/**
	 * Returns the fittness. This is used to determine the best indivudual of a population
	 * @param indidual
	 * @return
	 */
	public double getFitness(Individual indidual) {		
		Double fitness = indidual.getFitness();
		if (fitness==null) {
			fitness = this.getState(indidual).result().getDouble(optimizationParameter);
			indidual.setFitness(fitness);
		}
		return fitness;
	}

	/**
	 * Determines the number of generations
	 * @return
	 */
	public int getGenerations() {
		return generations;
	}

	/**
	 * Defines the number of generations
	 * @param generations
	 */
	public void setGenerations(int generations) {
		this.generations = generations;
	}

	public int getPopulationSize() {
		return populationSize;
	}

	public void setPopulationSize(int populationSize) {
		this.populationSize = populationSize;
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
