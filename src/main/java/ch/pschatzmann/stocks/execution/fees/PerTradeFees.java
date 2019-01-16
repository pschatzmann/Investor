package ch.pschatzmann.stocks.execution.fees;

import java.io.Serializable;

/**
 * Simple class to represent fees per trade
 * 
 * @author pschatzmann
 *
 */
public class PerTradeFees implements Serializable, IFeesModel {
	private static final long serialVersionUID = 1L;
	private Double fees;
	
	public PerTradeFees() {}
	
	public PerTradeFees(Double fees){
		this.fees = fees;
	}

	@Override
	public double getFeesPerTrade(double qty, double value) {
		return fees;
	}

	public Double getFees() {
		return fees;
	}

	public void setFees(Double fees) {
		this.fees = fees;
	}

}
