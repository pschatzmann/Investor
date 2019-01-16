package ch.pschatzmann.stocks.accounting;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import ch.pschatzmann.dates.DateRange;
import ch.pschatzmann.stocks.execution.fees.IFeesModel;
import ch.pschatzmann.stocks.execution.fees.PerTradeFees;

/**
 * Trading account which is supporting to record stock trading orders and orders
 * to transfer money in and out of the account;
 * 
 * @author pschatzmann
 *
 */

@JsonInclude(Include.NON_NULL)
public class BasicAccount implements Serializable, IBasicAccount {
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(BasicAccount.class);
	private String id;
	private String revision;
	private String currency;
	private double initialCash;
	private List<Transaction> transactionsList = new ArrayList();
	private IFeesModel fees;
	private boolean margin = false;
	private DateRange period;

	
	public BasicAccount() {
	}

	/**
	 * Default Constructor
	 * 
	 * @param id
	 * @param currency
	 * @param cash
	 * @param openDate
	 * @param fees
	 */
	public BasicAccount(String id, String currency, Double cash, Date openDate, IFeesModel fees) {
		this();
		this.id = id;
		this.currency = currency;
		this.fees = fees;
		this.period = new DateRange(openDate, null);
		this.initialCash = cash;
		if (cash.doubleValue()!=0.0)
			this.addTransaction(new Transaction(openDate, cash));
	}

	/**
	 * Clone an existing account
	 * 
	 * @param copyFrm
	 */
	public BasicAccount(IBasicAccount copyFrm) {
		this();
		this.id = copyFrm.getId();
		this.currency = copyFrm.getCurrency();
		this.fees = copyFrm.getFeesModel();
		this.period = new DateRange(copyFrm.getOpenDate(), copyFrm.getCloseDate());
		this.initialCash = copyFrm.getInitialCash();
		if (initialCash!=0.0)
			this.addTransaction(new Transaction(period.getStart(), initialCash));
	}

	/* (non-Javadoc)
	 * @see ch.pschatzmann.stocks.accounting.IAccount#addTransaction(ch.pschatzmann.stocks.accounting.Transaction)
	 */
	@Override
	public void addTransaction(Transaction order) {
		LOG.debug("addTransaction " + order);
		transactionsList.add(order);
	}

	/**
	 * Returns the currency of the account
	 * 
	 * @return
	 */
	@Override
	public String getCurrency() {
		return currency;
	}

	/* (non-Javadoc)
	 * @see ch.pschatzmann.stocks.accounting.IAccount#getTransactions()
	 */
	@Override
	public synchronized List<Transaction> getTransactions() {
		return transactionsList;
	}

	public synchronized void setTransactions(List<Transaction>transactions) {
		this.transactionsList = transactions;
	}


	/* (non-Javadoc)
	 * @see ch.pschatzmann.stocks.accounting.IAccount#getFeesModel()
	 */
	@Override
	@JsonDeserialize(as = PerTradeFees.class)
	public IFeesModel getFeesModel() {
		return this.fees;
	}

	@JsonDeserialize(as = PerTradeFees.class)
	public void setFeesModel(IFeesModel m) {
		this.fees = m;
	}

	/**
	 * Returns true if it is a margin account
	 * 
	 * @return
	 */
	@Override
	public boolean isMargin() {
		return margin;
	}

	/**
	 * Defines if this is a margin account which allows the investor to borrow money
	 * 
	 * @param margin
	 */
	public void setMargin(boolean margin) {
		this.margin = margin;
	}

	/* (non-Javadoc)
	 * @see ch.pschatzmann.stocks.accounting.IAccount#getOpenDate()
	 */
	@Override
	public Date getOpenDate() {
		return this.period.getStart();
	}

	/**
	 * Redefines the date on which the account was opened
	 * 
	 * @param start
	 */
	public void setOpenDate(Date start) {
		this.period.setStart(start);
	}



	/**
	 * If the account has been closed we return the date. Otherwise this is null
	 * 
	 * @return
	 */
	@Override
	public Date getCloseDate() {
		return this.period.getEnd();
	}

	/**
	 * Defines the date on which the account was closed. The impacts the trading
	 * simulation and makes sure that no update after this date will take place.
	 * 
	 * @param endDate
	 */
	@Override
	public void setCloseDate(Date endDate) {
		this.period.setEnd(endDate);
	}

	/**
	 * In order to facilitate debugging and logging we print the id
	 */
	@Override
	public String toString() {
		return id;
	}

	/**
	 * Resets the account to the inital state. Removes all simmulated stock trades
	 */
	@Override
	public void reset() {
		this.transactionsList.clear();
		this.addTransaction(new Transaction(this.getOpenDate(), initialCash));
	}

	/**
	 * Returns the period from the open date to the close date (or now if the
	 * account does not have a closing date
	 */
	public DateRange getDateRange() {
		return this.period;
	}

	/**
	 * Checks if the date is between the open and closing date
	 * 
	 * @param date
	 * @return
	 */
	public boolean isValidDate(Date date) {
		return this.getDateRange().isValid(date);
	}

	/**
	 * Sets the open and closing date
	 * 
	 * @param period
	 */
	public void setDateRange(DateRange period) {
		this.period = period;
	}


	/**
	 * Returns the account ID which is used to identify the account
	 * 
	 * @return
	 */

	@Override
	@JsonProperty("_id")
	public String getId() {
		return id;
	}

	@JsonProperty("_id")
	public void setId(String id) {
		this.id = id;
	}

	@JsonProperty("_rev")
	public String getRevision() {
		return revision;
	}

	@JsonProperty("_rev")
	public void setRevision(String revision) {
		this.revision = revision;
	}
	
	@Override
	public double getInitialCash() {
		return this.initialCash;
	}
}
