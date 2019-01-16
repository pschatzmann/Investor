package ch.pschatzmann.stocks.strategy;

import java.util.ArrayList;
import java.util.List;

import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.Strategy;
import org.ta4j.core.TimeSeries;

import ch.pschatzmann.stocks.IStockData;
import ch.pschatzmann.stocks.strategy.optimization.InputParameterName;

/**
 * Strategies implemented by a scripting language. e.e JavaScript
 * 
 * @author pschatzmann
 *
 */

public class SciptStrategy extends CommonTradingStrategy implements ITradingStrategy {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final static Logger LOG = LoggerFactory.getLogger(SciptStrategy.class);
	private ScriptEngine engine;
	private String script;
	private String engineName;

	public SciptStrategy(IStockData stockData) {
		super(stockData);
	}

	public SciptStrategy(IStockData stockData, String engineName, String script) {
		super( stockData);
		this.engineName = engineName;
		this.script = script;
	}

	@Override
	public List<InputParameterName> getParameterOptimizationSequence() {
		return new ArrayList();
	}

	@Override
	public Strategy buildStrategy(TimeSeries timeSeries) {
		try {
			if (engine == null) {
				ScriptEngineManager factory = new ScriptEngineManager();
				engine = factory.getEngineByName(engineName);
			}
			if (engine==null) {
				throw new RuntimeException("The engine could not be loaded: "+engineName);
			}
			
			engine.getContext().setAttribute("timeSeries", timeSeries, ScriptContext.ENGINE_SCOPE);
			engine.getContext().setAttribute("strategy", null, ScriptContext.ENGINE_SCOPE);
			Object obj = engine.eval(script);
			if (engine instanceof Invocable) {
				Invocable inv = (Invocable) engine;
				obj = inv.invokeFunction("execute", timeSeries);
			}
			Strategy result = null;
			if (obj instanceof Strategy) {
				result = (Strategy) obj;
			} else {
				result = (Strategy) engine.get("strategy");
			}
			return result;
		} catch (Exception e) {
			LOG.error(e.getLocalizedMessage(), e);
			throw new RuntimeException(e);
		}
	}

	public String getScript() {
		return script;
	}

	public void setScript(String script) {
		this.script = script;
	}

	public String getEngineName() {
		return engineName;
	}

	public void setEngineName(String engineName) {
		this.engineName = engineName;
	}

}
