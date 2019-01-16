package ch.pschatzmann.stocks.strategy;

import java.util.Date;

public interface ITradeEvent {
	void beforeTrade(Date date);
}
