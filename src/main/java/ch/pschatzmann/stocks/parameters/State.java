package ch.pschatzmann.stocks.parameters;

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ch.pschatzmann.stocks.accounting.kpi.KPI;
import ch.pschatzmann.stocks.strategy.optimization.InputParameterName;

/**
 * A set of input and result parameters
 * 
 * @author pschatzmann
 *
 */

public class State implements Cloneable, Serializable {
	private static final long serialVersionUID = 1L;
	private Parameters<InputParameterName> input = new Parameters();
	private Parameters<KPI> result = new Parameters();

	public State() {}
	
	public State(State state) {
		this.input = state.input;
		this.result = state.result;
	}
	
	public Parameters<InputParameterName> input() {
		return input;
	}
	public Parameters<KPI> result() {
		return result;
	}
	
	public Parameters<InputParameterName> getInput() {
		return input;
	}
	public void setInput(Parameters input) {
		this.input = input;
	}
	public Parameters<KPI> getResult() {
		return result;
	}
	public void setResult(Parameters result) {
		this.result = result;
	}
	
	/**
	 * Retrns the parameter values as a map
	 * @return
	 */
	@JsonIgnore
	public Map<String,Object> getMap() {
		Map<String,Object> result = new TreeMap();
		for (Entry<InputParameterName, ParameterValue<Number>> i : this.input.getMap().entrySet()) {
			result.put(i.getKey().toString(), i.getValue().getValue());
		}
		for (Entry<KPI, ParameterValue<Number>> r : this.result.getMap().entrySet()) {
			result.put(r.getKey().toString(), r.getValue().getValue());
		}
		return result;
	}
	@JsonIgnore
	public Map<String,Object> getInputMap() {
		Map<String,Object> result = new TreeMap();
		for (Entry<InputParameterName, ParameterValue<Number>> i : this.input.getMap().entrySet()) {
			result.put(i.getKey().toString(), i.getValue().getValue());
		}
		return result;
	}

	@JsonIgnore
	public Map<String,Object> getOutputMap() {
		Map<String,Object> result = new TreeMap();
		for (Entry<KPI, ParameterValue<Number>> r : this.result.getMap().entrySet()) {
			result.put(r.getKey().toString(), r.getValue().getValue());
		}
		return result;
	}
	
	@Override
	public State clone() {
		State result = new State();
		this.input.getMap().entrySet().forEach(entry -> result.input.addValue(entry.getKey(), entry.getValue().clone()));
		this.result.getMap().entrySet().forEach(entry -> result.result.addValue(entry.getKey(), entry.getValue().clone()));
		return result;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(input);
		sb.append(" -> ");
		sb.append(result);
		return sb.toString();
	}
		
 }
