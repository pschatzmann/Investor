package ch.pschatzmann.stocks.accounting;

import java.util.Date;
import java.util.List;
import ch.pschatzmann.stocks.execution.fees.IFeesModel;

public interface IBasicAccount {

	/**
	 * Adds a new transaction for execution.
	 * 
	 * @param order
	 */
	void addTransaction(Transaction order);

	/**
	 * Returns all orders
	 * 
	 * @return
	 */
	List<Transaction> getTransactions();

	/**
	 * Returns the model to determine trading fees
	 * 
	 * @return
	 */
	IFeesModel getFeesModel();

	/**
	 * Date on which the account was opened and from which we start the kpi
	 * calculations
	 * 
	 * @return
	 */
	Date getOpenDate();
	
	/**
	 * Inital opening cash which will be available for trading
	 * @return
	 */
	double getInitialCash();

	/**
	 * If we support buys w/o having corresponging cash
	 * @return
	 */
	boolean isMargin();

	/**
	 * Deletes all transactions and restores the initial cash
	 */
	void reset();

	/**
	 * Returns the date up to which we allow trading
	 * @return
	 */
	Date getCloseDate();

	/**
	 * Closes the account to prevent it from further processing
	 * @param date
	 */
	void setCloseDate(Date date);

	/**
	 * Returns the account ID
	 * @return
	 */
	String getId();

	/**
	 * Returns the Currency in which the values are specified
	 * @return
	 */
	String getCurrency();


}