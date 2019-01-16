package ch.pschatzmann.stocks.strategy.allocation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import ch.pschatzmann.stocks.IStockID;
import ch.pschatzmann.stocks.accounting.HistoricValue;
import ch.pschatzmann.stocks.accounting.IAccount;
import ch.pschatzmann.stocks.accounting.IHistoricValue;
import ch.pschatzmann.stocks.accounting.kpi.SharpeRatio;
import ch.pschatzmann.stocks.strategy.ITradingStrategy;

/**
 * Distribution by sharpe ratio of stock values
 * 
 * @author pschatzmann
 *
 */
public class SharpeRatioOfStockDistributor implements IDistributor, Serializable {
	private static final long serialVersionUID = 1L;
	private List<ITradingStrategy> activeStrategies = new ArrayList();
	private Set<ITradingStrategy> allStrategies = new HashSet();
	private IAccount account;
	private double riskFreeRetrun;

	public SharpeRatioOfStockDistributor(IAccount account, double riskFreeRetrun) {
		this.account = account;
		this.riskFreeRetrun = riskFreeRetrun;
	}

	@Override
	public void add(ITradingStrategy id, Date date) {
		activeStrategies.add(id);
		allStrategies.add(id);
	}

	@Override
	public void remove(ITradingStrategy id) {
		activeStrategies.remove(id);
	}

	@Override
	public double getFactor(ITradingStrategy ts, Date date) {
		Map<IStockID, Double> sharpeRatioMap = new HashMap();
		for (ITradingStrategy strategy : activeStrategies) {
			IStockID stock = strategy.getStockData().getStockID();
			List<IHistoricValue> values = account.getStockData(stock).getHistory().stream()
					.filter(r -> r.getDate().before(date))
					.map(r -> new HistoricValue(r.getDate(), r.getClosing().doubleValue()))
					.collect(Collectors.toList());
			Double sharpeRatio = new SharpeRatio(values, riskFreeRetrun).getValue();
			sharpeRatioMap.put(stock, sharpeRatio);
		}
		double total = sharpeRatioMap.values().stream().mapToDouble(v -> v).sum();
		Double sharpRatio = sharpeRatioMap.get(ts.getStockData().getStockID());
		// no stocks without a buy signar and no negative sharp ratios
		return sharpRatio == null ? 0.0 : sharpRatio.doubleValue() / total;
	}

	@Override
	public Collection<ITradingStrategy> getAllStrategies() {
		return allStrategies;
	}

}
