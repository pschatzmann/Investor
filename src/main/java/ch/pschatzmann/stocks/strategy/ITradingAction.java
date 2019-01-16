package ch.pschatzmann.stocks.strategy;

import java.util.Date;

/**
 * Interface which defines the trading action to be used by the strategy
 * 
 * @author pschatzmann
 *
 */
public interface ITradingAction {
	TradingAction get(Date date);
}
