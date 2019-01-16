package ch.pschatzmann.stocks.strategy.allocation;

import java.util.Date;

import ch.pschatzmann.stocks.IStockRecord;
import ch.pschatzmann.stocks.accounting.IAccount;
import ch.pschatzmann.stocks.strategy.ITradingStrategy;

/**
 * Logic which determines the buy and sell quantities
 * 
 * @author pschatzmann
 *
 */
public interface IAllocationStrategy {

	public Long onBuy(IAccount account, IStockRecord stockRecord, ITradingStrategy strategy);

	public Long onSell(IAccount account, IStockRecord stockRecord, ITradingStrategy strategy);

	public void onEndOfDate(IAccount account, Date date);

}