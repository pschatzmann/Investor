package ch.pschatzmann.stocks.accounting;

import java.io.Serializable;
import java.util.Comparator;

public class HistoricValueComparator implements Comparator<IHistoricValue>,Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public int compare(IHistoricValue o1, IHistoricValue o2) {
		return o1.getDate().compareTo(o2.getDate());
	}
}
