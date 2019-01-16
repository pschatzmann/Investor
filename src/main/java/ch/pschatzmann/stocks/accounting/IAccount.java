package ch.pschatzmann.stocks.accounting;

import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import ch.pschatzmann.dates.DateRange;
import ch.pschatzmann.stocks.IStockData;
import ch.pschatzmann.stocks.IStockID;
import ch.pschatzmann.stocks.accounting.kpi.KPI;
import ch.pschatzmann.stocks.accounting.kpi.KPIValue;

/**
 * Basic account functionality which is needed by the execution and allocations
 * 
 * @author pschatzmann
 *
 */

public interface IAccount extends IBasicAccount {

	public Long getQuantity(IStockID id);

	public IStockData getStockData(IStockID id);

	public Portfolio getPortfolio(Date date);

	public IBasicAccount getAccount();

	public double getCash();

	public double getTotalValue(Date date);

	public double getActualValue(Date date, IStockID id);

	public double getCash(Date date);

	public Double getStockPrice(IStockID id, Date date);
	
	public void putStockData(IStockData sd);

	public DateRange getDateRange();

	public List<KPIValue> getKPIValues();

	public Stream<Transaction> getTransactions(IStockID stockID);

	public List<IStockID> getStockIDs();


}
