package ch.pschatzmann.stocks.accounting;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.pschatzmann.stocks.Context;
import ch.pschatzmann.stocks.IStockID;
import ch.pschatzmann.stocks.StockID;

/**
 * Individual line of the portfolio which represents the kpis for an individual
 * stock. 
 * 
 * @author pschatzmann
 *
 */

public class PortfolioStockInfo implements IHistoricValue, Serializable, Comparable<PortfolioStockInfo> {
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(PortfolioStockInfo.class);
	private IStockID id;
	private Date date;
	private long quantity;
	private double purchaseValue;
	private double currentValue;
	private double realizedGains;
	private double fees;
	private double impactOnCash;
	private long numberOfTrades;
	private List<Transaction> transactions = new ArrayList();

	protected PortfolioStockInfo(Date date, IStockID id) {
		this.id = id;
		this.date = date;
	}

	protected PortfolioStockInfo(Date date, IStockID id, long quantity, double value, double fees, double cash, long numberOfTrades) {
		LOG.debug("PortfolioStockInfo (new) "+id);
		this.id = id;
		this.date = date;
		this.quantity = quantity;
		this.purchaseValue = value;
		this.fees = fees;
		this.impactOnCash = cash;
		this.numberOfTrades = numberOfTrades;
	}
	
	protected PortfolioStockInfo(Date date, PortfolioStockInfo pi) {
		LOG.debug("PortfolioStockInfo - copy "+pi.id);
		this.id = pi.id;
		this.date = date;
		this.quantity = pi.quantity;
		this.purchaseValue = pi.purchaseValue;
		this.fees = pi.fees;
		this.impactOnCash = pi.impactOnCash;
		this.numberOfTrades = pi.numberOfTrades;
		this.realizedGains = pi.realizedGains;
		this.transactions.addAll(pi.getTransactions());
	}
	
	public IStockID getStockID() {
		return id;
	}
	
	public void setStockID(IStockID id) {
		this.id = id;
	}
	
	public Long getQuantity() {
		return quantity;
	}
	
	public void setQuantity(long quantity) {
		this.quantity = quantity;
	}
	
	public double getPurchasedAveragePrice() {
		return purchaseValue / quantity;
	}
	
	public double getCurrentPrice() {
		return currentValue / quantity;
	}
		
	public double getActualValue() {
		return currentValue;
	}

	public double getPurchasedValue() {
		return purchaseValue;
	}
	
	public double getUnrealizedGains() {
		return getActualValue() - getPurchasedValue();
	}

	public void setActualValue(double d) {
		this.currentValue = d;
	}

	public double getFees() {
		return fees;
	}

	public double getImpactOnCash() {
		return impactOnCash;
	}
	
	public double getRealizedGains() {
		return this.realizedGains;
	}
	
	public List<Transaction> getTransactions() {
		return this.transactions;
	}
	
	public long getNumberOfTrades() {
		return numberOfTrades;
	}

	@Override
	public int compareTo(PortfolioStockInfo o) {
		return StockID.compare(this.getStockID(),o.getStockID());
	}

	protected void addQuantity(Long q) {
		this.quantity += q;
	}

	protected void addPurchasedValue(double v) {
		this.purchaseValue += v;		
	}

	protected void addImpactOnCash(double c) {
		this.impactOnCash += c;
	}

	protected void addNumberOfTrades(long n) {
		this.numberOfTrades += n;
	}
	
	public void addFees(double fees) {
		this.fees+=fees;
	}

	public void addRealizedGains(double gain) {
		realizedGains +=gain;	
		LOG.debug(this.id+" "+realizedGains);
	}

	public void addTransaction(Transaction t) {
		this.transactions.add(t);
	}
	
	@Override
	public Date getDate() {
		return this.date;
	}
	
	public boolean isCash() {
		return this.id.getTicker().equals("Cash");
	}

	@Override
	public Double getValue() {
		return this.getActualValue();
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(this.getStockID());
		sb.append(" ");
		sb.append(Context.format(this.getDate()));
		sb.append(" ");
		sb.append(this.getValue());
		return sb.toString();
	}


}
