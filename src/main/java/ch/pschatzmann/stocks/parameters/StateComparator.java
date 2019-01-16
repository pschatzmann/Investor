package ch.pschatzmann.stocks.parameters;

import java.io.Serializable;
import java.util.Comparator;

import ch.pschatzmann.stocks.accounting.kpi.KPI;

/**
 * Compare State objects based on the indicated parameter. The values are sorted
 * in a descending order because bigger values are better!
 * 
 * @author pschatzmann
 *
 */
public class StateComparator implements Comparator<State>, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private KPI kpi;
	private int factor = 1;

	/**
	 * Empty constructor for serialization
	 */
	public StateComparator(){		
	}
	
	/**
	 * Default Constructor
	 * @param kpi
	 */
	public StateComparator(KPI kpi) {
		this.kpi = kpi;
	}

	public StateComparator(boolean increasing, KPI kpi) {
		this.kpi = kpi;
		factor = increasing ? 1 : -1;
	}

	
	@Override
	public int compare(State o1, State o2) {
		// we prefer strategies with the higher indicated KPI
		int result = ((Double) o1.getResult().getDouble(kpi)).compareTo(o2.getResult().getDouble(kpi));
		if (result == 0) {
			// we prefer strategies with more trades
			result = ((Double) o1.getResult().getDouble(KPI.NumberOfTrades)).compareTo(o2.getResult().getDouble(KPI.NumberOfTrades));
		}
		return factor * result;
	}

}
