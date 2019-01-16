package ch.pschatzmann.stocks.strategy.allocation;

import java.util.Collection;
import java.util.Date;

import ch.pschatzmann.stocks.strategy.ITradingStrategy;

public interface IDistributor {

	void add(ITradingStrategy strategy, Date date);

	void remove(ITradingStrategy strategy);

	double getFactor(ITradingStrategy strategy, Date date);
	
	public Collection<ITradingStrategy> getAllStrategies();

}