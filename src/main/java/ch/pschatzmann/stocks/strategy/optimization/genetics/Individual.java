package ch.pschatzmann.stocks.strategy.optimization.genetics;

import java.io.Serializable;

import ch.pschatzmann.stocks.utils.Range;

public class Individual implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Gene[] genes;
	private int geneLength;
	private Double fitness = null;

	Individual(int geneLength) {
		this.genes = new Gene[geneLength];
		this.geneLength = geneLength;
	}

	// Create a random individual
	public void generateIndividual(String[] name, Range[] geneTemplate) {
		for (int i = 0; i < size(); i++) {
			Gene gene = new Gene(name[i],geneTemplate[i]);
			genes[i] = gene;
		}
	}

	public Gene getGene(int index) {
		return genes[index];
	}

	public void setGene(int index, Gene value) {
		genes[index] = value;
	}

	/* Public methods */
	public int size() {
		return genes.length;
	}


	public int getGeneLength() {
		return this.geneLength;
	}

	/**
	 * Returns the saved fitness. 
	 * @return
	 */
	public Double getFitness() {
		return fitness;
	}

	/**
	 * Saves the fitness: optional functionality if the calculation is expensive we store the result instead of 
	 * recalculating it on the fly
	 * @param fitness
	 */
	public void setFitness(Double fitness) {
		this.fitness = fitness;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < size(); i++) {
			sb.append(getGene(i));
			sb.append(", ");
		}
		sb.append(" -> "+this.getFitness());
		return sb.toString();
	}
}
