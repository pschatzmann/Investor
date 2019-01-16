package ch.pschatzmann.stocks.strategy.optimization.genetics;

import java.io.Serializable;

import ch.pschatzmann.stocks.strategy.optimization.GeneticOptimizer;
import ch.pschatzmann.stocks.utils.Range;

public class Population implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Individual[] individuals;
	GeneticOptimizer optimizer;

	/*
	 * Constructors
	 */
	// Create a population
	public Population(int populationSize, String[] names, Range[] geneTemplate, boolean initialise,
			GeneticOptimizer optimizer) {
		this.optimizer = optimizer;
		individuals = new Individual[populationSize];
		// Initialise population
		if (initialise) {
			// Loop and create individuals
			for (int i = 0; i < size(); i++) {
				Individual newIndividual = new Individual(geneTemplate.length);
				newIndividual.generateIndividual(names, geneTemplate);
				saveIndividual(i, newIndividual);
			}
		}
	}

	/* Getters */
	public Individual getIndividual(int index) {
		return individuals[index];
	}

	// Get population size
	public int size() {
		return individuals.length;
	}

	// Save individual
	public void saveIndividual(int index, Individual indiv) {
		individuals[index] = indiv;
	}

	public Individual getFittest() {
		Double max = -10000.0;
		Individual result = null;
		for (int j = 0; j < size(); j++) {
			Individual i = getIndividual(j);
			if (i != null) {
				double value = optimizer.getFitness(i);
				if (value > max) {
					result = i;
					max = value;
				}
			}
		}
		return result;
	}
}
