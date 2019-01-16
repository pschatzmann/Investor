package ch.pschatzmann.stocks.strategy.optimization;

import java.io.Serializable;

import ch.pschatzmann.stocks.accounting.Account;
import ch.pschatzmann.stocks.accounting.BasicAccount;
import ch.pschatzmann.stocks.accounting.IAccount;
import ch.pschatzmann.stocks.accounting.IBasicAccount;
import ch.pschatzmann.stocks.execution.PaperTrader;

/**
 * The Fitness class is using the paper trader to evaluate a trading strategy
 * and calculate the KPIs.
 * 
 * The trading account is not updated but a separate simulation account is created.
 * 
 * @author pschatzmann
 *
 */

public class SimulatedFitness extends Fitness implements Serializable {
	private static final long serialVersionUID = 1L;

	public SimulatedFitness(IAccount account) {
		super(new PaperTrader(new BasicAccount(account.getAccount())));
	}

	public SimulatedFitness(IBasicAccount account) {
		super(new PaperTrader(new Account(new BasicAccount(account))));
	}

}
