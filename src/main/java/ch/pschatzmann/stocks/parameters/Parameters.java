package ch.pschatzmann.stocks.parameters;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ch.pschatzmann.stocks.strategy.optimization.InputParameterName;

public class Parameters<E> implements Serializable {
	private static final long serialVersionUID = 1L;
	private Map<E,ParameterValue<Number>> parameters = new HashMap();

	public Parameters() {}
	
	public Number getValue(E name) {
		ParameterValue pv = parameters.get(name);
		return pv == null ? 0 : pv.getValue();
	}
	
	public double getDouble(E name) {
		return getValue(name).doubleValue();
	}

	public int getInteger(E name) {
		return getValue(name).intValue();
	}
	
	public Parameters<E> setValue(E parameter, Number value, Number from, Number to, int decimals) {
		parameters.put(parameter, new ParameterValue(value,from, to, decimals));
		return this;
	}
	public Parameters<E> setValue(E parameter, Number value) {
		ParameterValue pv = parameters.get(parameter);
		if (pv==null){
			pv = new ParameterValue();
			parameters.put(parameter, pv);
		} 
		pv.setValue(value);
		return this;
	}
	
	public ParameterValue getParameterValue(InputParameterName name) {
		return parameters.get(name);
	}
	
	public List<E> names() {
		return new ArrayList(parameters.keySet());
	}

	public List<ParameterValue> values() {
		return new ArrayList(parameters.values());
	}

	
	protected Map<E,ParameterValue<Number>> getMap() {
		return parameters;
	} 
	
	public Map<E, ParameterValue<Number>> getParameters() {
		return parameters;
	}

	public void setParameters(Map<E, ParameterValue<Number>> parameters) {
		this.parameters = parameters;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (Entry <E,ParameterValue<Number>> e : parameters.entrySet()) {
			sb.append(e.getKey());
			sb.append(":");
			sb.append(e.getValue());
			sb.append("; ");
		}
		return sb.toString();
	}
	
	public void clear() {
		this.parameters.clear();
	}

	public Parameters<E> addValue(E key, ParameterValue<Number> value) {
		parameters.put(key, value);
		return this;
	}

	
}
