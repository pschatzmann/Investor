package ch.pschatzmann.stocks.accounting;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ch.pschatzmann.dates.DateRange;
import ch.pschatzmann.stocks.Context;
import ch.pschatzmann.stocks.IStockData;
import ch.pschatzmann.stocks.IStockID;
import ch.pschatzmann.stocks.IStockRecord;
import ch.pschatzmann.stocks.StockID;
import ch.pschatzmann.stocks.accounting.Transaction.Status;
import ch.pschatzmann.stocks.accounting.kpi.DrawDown;
import ch.pschatzmann.stocks.accounting.kpi.IKPICollector;
import ch.pschatzmann.stocks.accounting.kpi.KPI;
import ch.pschatzmann.stocks.accounting.kpi.KPIValue;
import ch.pschatzmann.stocks.accounting.kpi.NumberOfTrades;
import ch.pschatzmann.stocks.accounting.kpi.Return;
import ch.pschatzmann.stocks.accounting.kpi.SharpeRatio;
import ch.pschatzmann.stocks.execution.fees.IFeesModel;
import ch.pschatzmann.stocks.input.IReader;

/**
 * Implementation of Account KPIs for any basic Account.
 * 
 * @author pschatzmann
 *
 */
public class Account implements IKPICollector, IAccount, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(Account.class);
	private static IStockID defaultValue = new StockID("", "");
	private IBasicAccount account;
	private double riskFreeReturnInPercent = 0.0;
	protected transient Map<IStockID, IReader> readerMap = new HashMap();
	protected transient Map<IStockID, IStockData> stockDataMap = new HashMap();
	

	/**
	 * No Arg Constructor for Serialization
	 */
	public Account() {
	}

	/**
	 * Create new Account based on IBasicAccount
	 * 
	 * @param account
	 */
	public Account(IBasicAccount account) {
		this.account = account;
		//this.account = new BasicAccount(account.getId(),account.getCurrency(),account.getInitialCash(),account.getOpenDate(), account.getFeesModel());
		if (account instanceof Account) {
			this.stockDataMap.putAll(((Account) account).stockDataMap);
			this.readerMap.putAll(((Account) account).readerMap);
		}
	}

	public Account(String id, String currency, Double cash, Date openDate, IFeesModel fees) {
		this(new BasicAccount(id, currency, cash, openDate, fees));
	}

	@Override
	public List<Transaction> getTransactions() {
		return account.getTransactions();
	}

	@Override
	public void collectKPIValues(Collection<KPIValue> result) {
		result.add(new KPIValue(KPI.TotalFees, "Total Fees", this.getTotalFees()));
		result.add(new KPIValue(KPI.Cash, "Cash", this.getCash()));
		result.add(new KPIValue(KPI.ActualValue, "Total Value (at actual rates) including cash", this.getTotalValue()));
		result.add(new KPIValue(KPI.PurchasedValue, "Total Value (at purchased rates)", this.getTotalPurchasedValue()));

		result.add(new KPIValue(KPI.RealizedGains, "Realized Gains", this.getRealizedGain()));
		result.add(new KPIValue(KPI.UnrealizedGains, "Unrealized Gains", this.getUnrealizedGain()));

	}

	/**
	 * Returns the amout of cash which is currently available
	 * 
	 * @return
	 */
	@Override
	public double getCash() {
		return this.getTransactions()
			.stream()
			.mapToDouble(o -> o.getImpactOnCash())
			.sum();
	}

	/**
	 * Returns the amout of cash which is available at the indicated date
	 * 
	 * @param date
	 * @return
	 */
	@Override
	public double getCash(Date date) {
		return this.getTransactions().stream().filter(d -> d.getDate().getTime() <= date.getTime())
				.mapToDouble(o -> o.getImpactOnCash()).sum();
	}

	/**
	 * Returns the value of all available stocks valuated at the market price
	 * 
	 * @return
	 */
	public double getActualValue() {
		return this.getPortfolio().getActualValue();
	}

	/**
	 * Returns the value of all available stocks valuated at purchasing price
	 * 
	 * @return
	 */
	public double getPurchasedValue() {
		return this.getPortfolio().getPurchasedValue();
	}

	/**
	 * Returns the total value of the account (stocks valuated at the market price
	 * and cash)
	 * 
	 * @return
	 */
	public double getTotalValue() {
		return getCash() + getActualValue();
	}

	/**
	 * Returns the total of the values of the account ( stocks valuated at
	 * purchasing price and cash)
	 * 
	 * @return
	 */
	public double getTotalPurchasedValue() {
		return getCash() + getPurchasedValue();
	}

	/**
	 * Returns the total of all fees
	 * 
	 * @return
	 */
	public double getTotalFees() {
		return this.getTransactions().stream().filter(t -> !t.isCashTransfer()).mapToDouble(t -> t.getFees()).sum();
	}

	/**
	 * Returns the realized gain. We do not consider trading fees!
	 * 
	 * @return
	 */
	public double getRealizedGain() {
		return this.getPortfolio().getRealizedGains();
	}

	/**
	 * Returns the unrealized gains
	 * 
	 * @return
	 */
	public double getUnrealizedGain() {
		return this.getPortfolio().getUnrealizedGains();
	}

	/**
	 * Returns the total profit
	 * 
	 * @return
	 */
	public double getTotalProfit() {
		return this.getPortfolio().getTotalProfit();
	}

	/**
	 * Determines the total number of trades
	 * 
	 * @return
	 */

	public long getNumberOfTrades() {
		return getPortfolio().getNumberOfTrades();
	}

	/**
	 * Returns the portfolio history for all transaction dates
	 * 
	 * @return
	 */
	public Stream<Portfolio> getTradingPortfolioHistory() {
		List<Portfolio> result = new ArrayList();
		Portfolio lastPortfolio = null;
		Date currentDate = new Date();
		for (Date date : getAllTransactionDates()) {
			Portfolio portfolio = this.getPortfolio(date);
			if (portfolio == null) {
				Portfolio temp = new Portfolio(this, date, lastPortfolio);
				getTransactionsForDate(date).forEach(t -> temp.recordOrder(t, date));
				temp.updateActualPrices();
				portfolio = temp;
			}
			result.add(portfolio);
			lastPortfolio = portfolio;
		}
		return result.stream();
	}

	/**
	 * Gets the portfolio history over all dates
	 * 
	 * @return
	 */
	public Stream<Portfolio> getPortfolioHistory() {
		List<Portfolio> result = new ArrayList();
		Portfolio lastPortfolio = null;
		Date currentDate = new Date();
		for (Date date : getAllDates()) {
			Portfolio portfolio = this.getPortfolio(date);
			if (portfolio == null) {
				Portfolio temp = new Portfolio(this, date, lastPortfolio);
				getTransactionsForDate(date).forEach(t -> temp.recordOrder(t, date));
				temp.updateActualPrices();
				portfolio = temp;
			}
			result.add(portfolio);
			lastPortfolio = portfolio;
		}
		return result.stream();
	}

	/**
	 * Returns the portfolio history for a single stock
	 * 
	 * @return
	 */
	public Stream<PortfolioStockInfo> getPortfolioStockInfoHistory(IStockID id) {
		return getPortfolioHistory().map(p -> p.getInfo(id));
	}

	/**
	 * Determines the history of the % returns for the indicated stock. The return
	 * includes the fees.
	 * 
	 * @param id
	 * @param adjument
	 * @return
	 */
	public Stream<IHistoricValue> getStockHistoryReturns(StockID id, double adjument) {
		List<PortfolioStockInfo> historyList = getPortfolioStockInfoHistory(id).sorted().collect(Collectors.toList());
		List<IHistoricValue> result = new ArrayList();

		for (int i = 1; i < historyList.size(); i++) {
			Date date = historyList.get(i).getDate();
			double value = historyList.get(i).getActualValue() - historyList.get(i).getFees();
			double priorValue = historyList.get(i - 1).getActualValue() - historyList.get(i - 1).getFees();
			long qty = historyList.get(i).getQuantity();
			if (qty != 0L) {
				result.add(new HistoricValue(date, ((value - priorValue) / priorValue) + adjument));
			} else {
				result.add(new HistoricValue(date, 0.0));
			}
		}
		return result.stream();
	}

	private Set<Date> getAllTransactionDates() {
		Set<Date> result = new TreeSet();
		result.addAll(this.getOrderDates());
		result.addAll(this.getTradingDates());
		return result;
	}

	/**
	 * Returns the information on the portfolio which with the latest information
	 * 
	 * @return
	 */
	@JsonIgnore
	public Portfolio getPortfolio() {
		// Date lastDate = getOrderDates().stream().max(Date::compareTo).get();
		return getPortfolio(this.getCloseDate()!=null ? this.getCloseDate() : Context.date(Context.format(new Date())));
	}

	/**
	 * Returns the current stock portfolio information of an indicated stock
	 * 
	 * @param id
	 * @return
	 */
	public PortfolioStockInfo getPortfolioStockInfo(IStockID id) {
		return this.getPortfolio().getInfo(id);
	}

	/**
	 * Returns the stock portfolio information which was valid at the indicated date
	 * 
	 * @param forDate
	 * @return
	 */
	@Override
	public Portfolio getPortfolio(Date forDate) {
		LOG.debug("getPortfolio "+forDate);
		Portfolio portfolio = new Portfolio(this, forDate);
		for (Date d : getOrderDates()) {
			if (d.getTime() <= forDate.getTime()) {
				Portfolio temp = portfolio;
				getTransactionsForDate(d).forEach(t -> temp.recordOrder(t, forDate));
				// portfolio.setDate(d);
			}
		}
		// portfolio.setDate(forDate);
		portfolio.updateActualPrices();
		return portfolio;
	}

	/**
	 * Returns all dates on which we executed orders sorted ascending
	 * 
	 * @return
	 */
	public synchronized Set<Date> getOrderDates() {
		return new TreeSet<>(this.getTransactions().stream().map(o -> Context.date(o.getDate())).collect(Collectors.toSet()));
	}

	/**
	 * Returns all transactions for the indicated date
	 * 
	 * @param forDate
	 * @return
	 */
	public Stream<Transaction> getTransactionsForDate(Date forDate) {
		return this.getTransactions().stream().filter(ol -> Context.date(ol.getDate()).equals(Context.date(forDate)));
	}

	/**
	 * Determines the stock price of the indicated stock at the indicated date. If
	 * no date is available we provide the data at the latest available date before.
	 * 
	 * @param id2
	 * @param date
	 * @return
	 */
	@Override
	public Double getStockPrice(IStockID id2, Date date) {
		if (id2 == Context.cashID()) {
			return 1.0;
		}
		Double result = 0.0;
		IStockData sd = getStockData(id2);
		IStockRecord sr = sd.getValue(date);
		if (sr != null) {
			result = sr.getClosing().doubleValue();
		} else {
			LOG.warn("No rate found for " + sd + " " + date);
		}
		return result;
	}

	/**
	 * Determines the cached StockData for the indicated StockIID. The data is
	 * cached
	 * 
	 * @param id2
	 * @return
	 */
	@Override
	public IStockData getStockData(IStockID id2) {
		IStockData result = stockDataMap.get(id2);
		if (result == null) {
			IReader r = this.readerMap.get(id2);
			if (r == null) {
				r = this.readerMap.get(defaultValue);
			}
			if(r==null) {
				r = Context.getDefaultReader();
			}
			result = Context.getStockData(id2, r);
			stockDataMap.put(id2, result);
		}
		return result;
	}

	@Override
	public void putStockData(IStockData sd) {
		// only replace stock data if it does not exist
		if (stockDataMap.get(sd.getStockID()) == null) {
			stockDataMap.put(sd.getStockID(), sd);
		}
	}

	/**
	 * Determines the quantity of the stocks which are in our possision for the
	 * indicated stock id. We also include orders
	 * 
	 * @param id
	 * @return
	 */
	@Override
	public Long getQuantity(IStockID id) {
		return this.getTransactions().stream().
				filter(t -> t.getStatus()!=Status.Cancelled && t.getStockID().equals(id)).
				mapToLong(t -> t.getQuantity()).
				sum();
	}

	/**
	 * Determines the all the dates on which the stock that we have aor had was
	 * traded starting from the account opening date
	 * 
	 * @return
	 */
	public List<Date> getAllDates() {
		Set<Date> dates = new TreeSet();
		List<Transaction> transactionsList = this.getTransactions().stream().collect(Collectors.toList());
		if (!transactionsList.isEmpty()) {
			Date startDate = transactionsList.iterator().next().getDate();
			dates.add(startDate);
			for (IStockID id : this.getStockIDs()) {
				IStockData data = this.getStockData(id);
				for (IStockRecord sr : data.getHistory()) {
					if (this.getDateRange().isValid(sr.getDate())) {
						dates.add(sr.getDate());
					}
				}
			}
		}
		return new ArrayList(dates);
	}

	public void putReader(StockID id, IReader reader) {
		readerMap.put(id, reader);
	}

	public void putReader(IReader reader) {
		readerMap.put(defaultValue, reader);
	}

	/**
	 * Determines the list of all dates on which some stocks were traded on the
	 * market and therefore there was a change in the valuation starting from the
	 * opening date of the account
	 * 
	 * @return
	 */
	public List<Date> getTradingDates() {
		List<Date> result = this.getTransactions().stream().filter(t -> !t.isCashTransfer()).map(t -> t.getStockID()).distinct()
				.map(id -> Context.getStockData(id)).map(stockData -> stockData.getHistory())
				.flatMap(stockRecordCollection -> stockRecordCollection.stream())
				.map(stockRecord -> stockRecord.getDate()).filter(d -> getDateRange().isValid(d)).sorted().distinct()
				.collect(Collectors.toList());
		return result;
	}

	/**
	 * Returns a date range from the opening date up to the closing date. If the account is not closed yet we set the
	 * range up to the current date
	 */
	@Override
	public DateRange getDateRange() {
		return new DateRange(account.getOpenDate(), account.getCloseDate()==null?new Date():account.getCloseDate());
	}

	/**
	 * Returns a list of all stock ID which were traded via this account
	 * 
	 * @return
	 */

	@Override
	public List<IStockID> getStockIDs() {
		List<IStockID> result = this.getTransactions().stream().filter(t -> !t.isCashTransfer()).map(t -> t.getStockID())
				.sorted().distinct().collect(Collectors.toList());
		return result;
	}

	/**
	 * Returns the risk free return (in percent) which is used to calculate the
	 * Sharpe Ratio
	 * 
	 * @return
	 */
	public double getRiskFreeReturnInPercent() {
		return riskFreeReturnInPercent;
	}

	/**
	 * Defines the risk free return (in percent) which is used to calculate the
	 * Sharpe Ratio
	 * 
	 * @param riskFreeReturnInPercent
	 */
	public void setRiskFreeReturnInPercent(double riskFreeReturnInPercent) {
		this.riskFreeReturnInPercent = riskFreeReturnInPercent;
	}

	/**
	 * Determines all KPIs to evaluate the history of the account
	 * 
	 * @return
	 */
	@Override
	public List<KPIValue> getKPIValues() {
		List<IHistoricValue> historyList = getTotalValueHistory().sorted().collect(Collectors.toList());
		List<KPIValue> result = new ArrayList();
		new Return(historyList).collectKPIValues(result);
		new SharpeRatio(historyList, riskFreeReturnInPercent).collectKPIValues(result);
		new DrawDown(historyList).collectKPIValues(result);
		new NumberOfTrades(this.getAccount()).collectKPIValues(result);
		new Account(this).collectKPIValues(result);
		return result;
	}
	
	/**
	 * Returns the KPI values as Map
	 * @return
	 */
	public Map<KPI, Double> getKPIValuesMap(){
		Map<KPI, Double> result = new TreeMap();
		for (KPIValue v : this.getKPIValues()) {
			result.put(v.getKpi(), v.getDoubleValue());
		}
		return result;
	}
	
	/**
	 * Calculate the simulated KPI for the stock ID
	 * @param id
	 * @return
	 */
	public List<KPIValue> getKPIValues(IStockID id) {
		Account oneStockAccount = new Account(this);	
		
		this.getTransactions(id).forEach(t -> oneStockAccount.addTransaction(t));
		List<IHistoricValue> historyList = oneStockAccount.getTotalValueHistory().sorted().collect(Collectors.toList());
		List<KPIValue> result = new ArrayList();
		new Return(historyList).collectKPIValues(result);
		new SharpeRatio(historyList, riskFreeReturnInPercent).collectKPIValues(result);
		new DrawDown(historyList).collectKPIValues(result);
		new NumberOfTrades(this.getAccount()).collectKPIValues(result);
		new Account(this).collectKPIValues(result);
		return result;
	}
	
	/**
	 * Returns the indicated KPI for all stock IDs
	 * @param kpi
	 * @return
	 */
	public Map<IStockID,Double> getKPIValueByStockID(KPI kpi) {
		Map<IStockID, Double> result = new TreeMap();
		for (IStockID id : this.getStockIDs()) {
			Double kpiValue = this.getKPIValue(kpi, this.getKPIValues(id));
			result.put(id, kpiValue);
		}
		return result;
	}
	

	/**
	 * Returns the IBasicAccount
	 */
	@Override
	public IBasicAccount getAccount() {
		return this.account;
	}

	/**
	 * Retrieves the requested KPI double value from the list
	 * 
	 * @param kpi
	 * @param values
	 * @return
	 */
	public Double getKPIValue(KPI kpi) {
		for (KPIValue v : getKPIValues()) {
			if (v.getKpi() == kpi) {
				return v.getDoubleValue();
			}
		}
		return null;
	}

	/**
	 * Retrieves the requested KPI double value from the list
	 * 
	 * @param kpi
	 * @param values
	 * @return
	 */
	public Double getKPIValue(KPI kpi, List<KPIValue> values) {
		for (KPIValue v : values) {
			if (v.getKpi() == kpi) {
				return v.getDoubleValue();
			}
		}
		return null;
	}

	/**
	 * Returns all orders related to the indicated stock
	 * 
	 * @param id
	 * @return
	 */
	@Override
	public Stream<Transaction> getTransactions(IStockID id) {
		return getTransactions().stream().sorted().filter(t -> t.getStockID().equals(id));
	}

	/**
	 * Returns the evolution of the available cash flow over time
	 * 
	 * @return
	 */
	public Stream<IHistoricValue> getCashFlowHistory() {
		Map<Date, Double> map = getTransactions().stream().collect(
				Collectors.groupingBy(Transaction::getDate, Collectors.summingDouble(Transaction::getImpactOnCash)));
		return map.entrySet().stream().map(v -> (IHistoricValue) new HistoricValue(v.getKey(), v.getValue())).sorted();
	}

	/**
	 * Gets the evolution of the cash total over time
	 * 
	 * @return
	 */
	public Stream<IHistoricValue> getCashHistory() {
		List<IHistoricValue> result = new ArrayList();
		double sum = 0.0;
		for (IHistoricValue v : getCashFlowHistory().collect(Collectors.toList())) {
			sum += v.getValue();
			result.add(new HistoricValue(v.getDate(), sum));
		}
		return result.stream();
	}

	/**
	 * Gets the evaluation of the cash total for all dates
	 * 
	 * @return
	 */
	public Stream<IHistoricValue> getCashHistoryForAllDates() {
		return this.getAllDates().stream().map(date -> new HistoricValue(date, getCash(date)))
				.map(i -> (IHistoricValue) i);
	}

	/**
	 * Returns the evolution of the actual value over time
	 * 
	 * @return
	 */

	public Stream<IHistoricValue> getActualValueHistory() {
		return getPortfolioHistory().map(p -> new HistoricValue(p.getDate(), p.getActualValue()))
				.map(i -> (IHistoricValue) i).sorted();
	}

	/**
	 * Returns the historic values for the indicated stock
	 * 
	 * @param id
	 * @return
	 */
	public Stream<IHistoricValue> getActualValueHistory(IStockID id) {
		return getPortfolioStockInfoHistory(id).map(i -> (IHistoricValue) i)
				.filter(p -> p.getDate() != null && p.getValue() != null);
	}

	/**
	 * Returns the evolution of the actual value over time
	 * 
	 * @return
	 */
	public Stream<IHistoricValue> getTotalValueHistory() {
		return getPortfolioHistory().map(p -> new HistoricValue(p.getDate(), p.getTotalValue()))
				.map(i -> (IHistoricValue) i).sorted();
	}

	/**
	 * Returns the evolution of the value (at purchased prices) and cash over time
	 * 
	 * @return
	 */
	public Stream<IHistoricValue> getPurchasedValueHistory() {
		return getPortfolioHistory().map(p -> new HistoricValue(p.getDate(), p.getPurchasedValue()))
				.map(i -> (IHistoricValue) i).sorted();
	}

	@Override
	public void addTransaction(Transaction order) {
		this.account.addTransaction(order);

	}

	@Override
	public IFeesModel getFeesModel() {
		return this.account.getFeesModel();
	}

	@Override
	public Date getOpenDate() {
		return this.account.getOpenDate();
	}

	@Override
	public double getInitialCash() {
		return this.account.getInitialCash();
	}

	@Override
	public boolean isMargin() {
		return this.account.isMargin();
	}

	@Override
	public void reset() {
		this.account.reset();
	}

	@Override
	public Date getCloseDate() {
		return this.account.getCloseDate();
	}
	
	@Override
	public void setCloseDate(Date date) {
		this.account.setCloseDate(date);
	}

	@Override
	public String getId() {
		return this.account.getId();
	}

	@Override
	public String getCurrency() {
		return this.account.getCurrency();
	}

	@Override
	public double getTotalValue(Date date) {
		return this.getPortfolio(date).getTotalValue();
	}

	@Override
	public double getActualValue(Date date, IStockID id) {
		return this.getPortfolio(date).getInfo(id).getActualValue();
	}
	
	protected void resetStockData() {
		stockDataMap.clear();
	}
	
	@Override
	public String toString() {
		return "Account: "+this.getId();
	}

}
