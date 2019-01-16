package ch.pschatzmann.stocks.strategy.selection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.pschatzmann.stocks.Context;
import ch.pschatzmann.stocks.IStockData;
import ch.pschatzmann.stocks.IStockID;
import ch.pschatzmann.stocks.errors.UniverseException;
import ch.pschatzmann.stocks.input.IReader;
import ch.pschatzmann.stocks.parameters.StateComparator;

/**
 * Determines the best n stocks out of a collection. The functionality can be
 * restarted if a Restartable has been defined and is picking up the last
 * processed state
 * 
 * @author pschatzmann
 *
 */
public class StockSelector implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(StockSelector.class);
	private IStategySelector selector;
	private Collection<IStockID> processed = new ArrayList();
	private Restartable restartable;
	private int count = 0;
	private Collection<SelectionState> initTopN;

	public StockSelector() {		
	}
	
	public StockSelector(IStategySelector strategySelector) {
		this.selector = strategySelector;
	}

	public StockSelector(IStategySelector strategySelector, Restartable restarable) {
		this.selector = strategySelector;
		this.restartable = restarable;

		if (restarable != null) {
			Map<String, Object> map = restarable.load();
			if (map != null) {
				initTopN = (Collection<SelectionState>) map.get("topN");
				processed = (Collection<IStockID>) map.get("processed");
				if (processed != null && !processed.isEmpty()) {
					System.out.println("Restarting with " + processed.size() + " records.");
				}
			}
		}
	}

	
	public SelectionResult getSelection(int number, Collection<IStockID> list, IReader reader)
			throws UniverseException {
		IReader readerX = reader !=null ? reader : Context.getDefaultReader();
		TopNSet<SelectionState> top = setupTopN(number);
		list.stream().filter(id -> !processed.contains(id)).map(id -> Context.getStockData(id, readerX))
				.forEach(sd -> process(top, sd));

		saveRestartable(top);
		return new SelectionResult(top);
	}

	
	public SelectionResult getSelection(int number, Collection<IStockData> stockData) {
		TopNSet<SelectionState> top = setupTopN(number);
		stockData.stream().filter(sd -> !processed.contains(sd.getStockID())).forEach(sd -> process(top, sd));

		saveRestartable(top);
		return new SelectionResult(top);
	}

	private void process(TopNSet<SelectionState> top, IStockData sd) {
		try {
			SelectionState state = selector.getMax(sd);
			add(top, state);
		} catch (Exception ex) {
			LOG.error("Could not process +" + sd.getStockID());
		}
	}

	private TopNSet<SelectionState> setupTopN(int number) {
		TopNSet<SelectionState> top = new TopNSet(number, new StateComparator(selector.getOptimizationParameter()));
		if (initTopN != null) {
			top.addAll(initTopN);
		}
		return top;
	}

	private void add(TopNSet<SelectionState> top, SelectionState rec) {
		try {
			LOG.info("==> " + rec.getResult().getDouble(this.selector.getOptimizationParameter()) + " " + rec);
			top.add(rec);
			commit(top, rec);
		} catch(Exception ex) {
			LOG.error(ex.getLocalizedMessage(),ex);
		}
	}

	private void commit(TopNSet<SelectionState> top, SelectionState rec) {
		if (restartable != null) {
			count++;
			processed.add(rec.getStockID());
			try {
				if (count % restartable.getCount() == 0) {
					saveRestartable(top);
				}
			} catch (Exception ex) {
				LOG.error(ex.getLocalizedMessage());
			}
		}
	}

	private void saveRestartable(TopNSet<SelectionState> top) {
		try {
			HashMap data = new HashMap();
			data.put("topN", top);
			data.put("processed", this.processed);
			if (restartable!=null) {
				restartable.save(data);
			}
		} catch (Exception ex) {
			LOG.warn("Could not save Restartable", ex);
		}
	}

}
