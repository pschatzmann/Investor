package ch.pschatzmann.stocks.strategy.optimization;

import ch.pschatzmann.dates.DateRange;
import ch.pschatzmann.stocks.execution.ITrader;
import ch.pschatzmann.stocks.parameters.State;
import ch.pschatzmann.stocks.strategy.ITradingStrategy;

public interface IFitness {
	State getFitness(ITradingStrategy ts, DateRange period);
	ITrader getTrader();
}
