package ch.pschatzmann.stocks.data.index;

import java.io.Serializable;

import ch.pschatzmann.stocks.StockID;

/**
 * Individual Record for Stock Index
 * 
 * @author pschatzmann
 *
 */
public class IndexRecord implements  Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private StockID id;
	private double weight;

	public IndexRecord(StockID stockID, Double weight) {
		this.id = stockID;
		this.weight = weight;
	}

	public IndexRecord(StockID stockID, String weight) {
		this.id = stockID;
		this.weight = Double.parseDouble(weight);
	}

	public StockID getId() {
		return id;
	}

	public void setId(StockID id) {
		this.id = id;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(id);
		sb.append(":");
		sb.append(""+weight);
		return sb.toString();
	}

}
