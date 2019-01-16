package ch.pschatzmann.stocks.accounting.kpi;

import java.util.Collection;

/**
 * Interface for collection account specific KPIs
 * 
 * @author pschatzmann
 *
 */
public interface IKPICollector {
	public void collectKPIValues(Collection<KPIValue> result);
}
