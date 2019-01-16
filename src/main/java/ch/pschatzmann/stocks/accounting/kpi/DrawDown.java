package ch.pschatzmann.stocks.accounting.kpi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import ch.pschatzmann.dates.DateRange;
import ch.pschatzmann.stocks.accounting.IBasicAccount;
import ch.pschatzmann.stocks.accounting.IHistoricValue;

/**
 * A drawdown is the peak-to-trough decline during a specific record period of
 * an investment, fund or commodity. A drawdown is usually quoted as the
 * percentage between the peak and the trough.
 * 
 * We calculate and make all draw downs and the related information.
 * 
 * @author pschatzmann
 *
 */

public class DrawDown implements IKPICollector, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private IBasicAccount ta;
	private Collection<IHistoricValue> history;
	private Collection<DrawDownValue> result = null;
	private DrawDownValue maxResult;

	public DrawDown(List<IHistoricValue> historyList) {
		this.history = historyList;
	}

	/**
	 * Returns all draw downs sorted by draw down % - big to small. So the first
	 * entry is the max draw down.
	 * 
	 * @return
	 */
	public List<DrawDownValue> getDrawDowns() {
		DrawDownValue drawDown = null;
		if (result == null) {
			result = new TreeSet();
			for (IHistoricValue value : history) {
				// detect new maximum
				if (drawDown == null || value.getValue() > drawDown.getMax()) {
					if (drawDown != null && drawDown.isDatesDifferent()) {
						result.add(drawDown);
					}
					drawDown = new DrawDownValue();
					drawDown.setMax(value.getValue());
					drawDown.setMin(value.getValue());
					drawDown.setRange(new DateRange(value.getDate(), value.getDate()));
				} else {
					drawDown.incNumberOfDays();
					if (value.getValue() < drawDown.getMin()) {
						drawDown.getRange().setEnd(value.getDate());
						drawDown.setMin(value.getValue());
					}
				}
			}
			if (drawDown!=null && drawDown.getMax()>drawDown.getMin()){
				result.add(drawDown);
			}
		}
		return new ArrayList(result);
	}

	/**
	 * Returns the biggest % draw down
	 * 
	 * @return
	 */
	public DrawDownValue getMaxDrowDown() {
		if (maxResult == null) {
			List<DrawDownValue> drawDowns = getDrawDowns();
			if (!drawDowns.isEmpty()) {
				maxResult = drawDowns.get(0);
			}
		}
		return maxResult;
	}

	public Double getMaxDrowDownValue() {
		DrawDownValue r = getMaxDrowDown();
		return r == null ? null : r.getValue();
	}

	public Integer getMaxDrowDownNumberOfDays() {
		DrawDownValue r = getMaxDrowDown();
		return r == null ? null : r.getNumberOfDays();
	}

	public Double getMaxDrowDownPercent() {
		DrawDownValue r = getMaxDrowDown();
		return r == null ? null : r.getPercent();
	}

	public DateRange getMaxDrowDownPeriod() {
		DrawDownValue r = getMaxDrowDown();
		return r == null ? null : r.getPeriod();
	}

	public Double getMaxDrowDownMaxValue() {
		DrawDownValue r = getMaxDrowDown();
		return r == null ? null : r.getMax();
	}

	public Double getMaxDrowDownMinValue() {
		DrawDownValue r = getMaxDrowDown();
		return r == null ? null : r.getMin();
	}

	/**
	 * Determines the biggest draw down within the shortest period of time
	 * 
	 * @return
	 */
	public DrawDownValue getShortesDrawDownPeriod() {
		Comparator<DrawDownValue> comp = (DrawDownValue a, DrawDownValue b) -> {
			int c = Integer.valueOf(a.getNumberOfDays()).compareTo(b.getNumberOfDays());
			if (c == 0) {
				c = b.getPercent().compareTo(a.getPercent());
			}
			return c;
		};

		return this.getDrawDowns().stream().min(comp).get();
	}

	@Override
	public void collectKPIValues(Collection<KPIValue> result) {
		result.add(new KPIValue(KPI.MaxDrawDownPercent, "Max Draw Down %", getMaxDrowDownPercent()));
		result.add(new KPIValue(KPI.MaxDrawDownPercent, "Max Draw Down Absolute", getMaxDrowDownValue()));
		result.add(new KPIValue(KPI.MaxDrawDownNumberOfDays, "Max Draw Down - Number of days",
				getMaxDrowDownNumberOfDays()));
		result.add(new KPIValue(KPI.MaxDrawDownHighValue, "Max Draw Down - High", getMaxDrowDownMaxValue()));
		result.add(new KPIValue(KPI.MaxDrawDownLowValue, "Max Draw Down - Low", getMaxDrowDownMinValue()));
		result.add(new KPIValue(KPI.MaxDrawDownPeriod, "Max Draw Down - Period", getMaxDrowDownPeriod()));
	}

}
