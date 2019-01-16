package ch.pschatzmann.stocks.accounting.kpi;

import java.io.Serializable;

import ch.pschatzmann.stocks.Context;

/**
 * Value for a KPI
 * 
 * @author pschatzmann
 *
 */
public class KPIValue implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String name;
	private Object value;
	private KPI kpi;

	public KPIValue(KPI kpi, String name, Object value) {
		this.kpi = kpi;
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Double getDoubleValue() {
		return value instanceof Number ? ((Number) value).doubleValue() : null;
	}

	public Object getValue() {
		return value;
	}

	public String getString() {
		String result = "";
		if (value != null) {
			if (value instanceof Number) {
				Number vn = (Number) value;
				if (Double.isFinite(vn.doubleValue())) {
					result = Context.format(vn);
				}
			} else {
				result = value.toString();
			}
		}
		return result;
	}

	public void setValue(Double value) {
		this.value = value;
	}

	public KPI getKpi() {
		return kpi;
	}

	public void setKpi(KPI kpi) {
		this.kpi = kpi;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(this.getName());
		sb.append(" ");
		sb.append(value);
		return sb.toString();
	}

}
