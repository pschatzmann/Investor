package ch.pschatzmann.stocks.strategy.optimization.genetics;

import java.io.Serializable;

import ch.pschatzmann.stocks.strategy.optimization.GeneticOptimizer;
import ch.pschatzmann.stocks.utils.Range;

public class Algorithm implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/* GA parameters */
	private static final double uniformRate = 0.5;
	private static final double mutationRate = 0.015;
	private static final int tournamentSize = 5;
	private static final boolean elitism = true;
	private Range[] geneTemplate;
	private String names[];
	private GeneticOptimizer fitnessCalc;

	/* Public methods */
	public Algorithm(String names[], Range geneTemplate[], GeneticOptimizer optimizer) {
		this.geneTemplate = geneTemplate;
		this.fitnessCalc = optimizer;
		this.names = names;
	}

	// Evolve a population
	public Population evolvePopulation(Population pop) {
		Population newPopulation = new Population(pop.size(), names, this.geneTemplate, false, fitnessCalc);

		// Keep our best individual
		if (elitism) {
			newPopulation.saveIndividual(0, pop.getFittest());
		}

		// Crossover population
		int elitismOffset;
		if (elitism) {
			elitismOffset = 1;
		} else {
			elitismOffset = 0;
		}
		// Loop over the population size and create new individuals with
		// crossover
		for (int i = elitismOffset; i < pop.size(); i++) {
			Individual indiv1 = tournamentSelection(pop);
			Individual indiv2 = tournamentSelection(pop);
			Individual newIndiv = crossover(indiv1, indiv2);
			newPopulation.saveIndividual(i, newIndiv);
		}

		// Mutate population
		for (int i = elitismOffset; i < newPopulation.size(); i++) {
			mutate(newPopulation.getIndividual(i));
		}

		return newPopulation;
	}

	// Crossover individuals
	private Individual crossover(Individual indiv1, Individual indiv2) {
		Individual newSol = new Individual(indiv1.getGeneLength());
		// Loop through genes
		for (int i = 0; i < indiv1.size(); i++) {
			// Crossover
			if (Math.random() <= uniformRate) {
				newSol.setGene(i, indiv1.getGene(i));
			} else {
				newSol.setGene(i, indiv2.getGene(i));
			}
		}
		return newSol;
	}

	// Mutate an individual
	private static void mutate(Individual indiv) {
		// Loop through genes
		for (int i = 0; i < indiv.size(); i++) {
			if (Math.random() <= mutationRate) {
				// Create random gene
				Gene randomGene = indiv.getGene(i);
				Range r = randomGene.getRange();
				String name = randomGene.getName();
				Gene gene = new Gene(name, r);
				indiv.setGene(i, gene);
			}
		}
	}

	// Select individuals for crossover
	private Individual tournamentSelection(Population pop) {
		// Create a tournament population
		Population tournament = new Population(tournamentSize, names, geneTemplate, false, fitnessCalc);
		// For each place in the tournament get a random individual
		for (int i = 0; i < tournamentSize; i++) {
			int randomId = (int) (Math.random() * pop.size());
			tournament.saveIndividual(i, pop.getIndividual(randomId));
		}
		// Get the fittest
		Individual fittest = tournament.getFittest();
		return fittest;
	}

}
