package ch.pschatzmann.stocks.strategy.allocation;

import java.io.Serializable;
import java.util.Date;

import ch.pschatzmann.dates.DateRange;
import ch.pschatzmann.stocks.Context;
import ch.pschatzmann.stocks.accounting.Account;
import ch.pschatzmann.stocks.accounting.IAccount;
import ch.pschatzmann.stocks.accounting.kpi.KPI;
import ch.pschatzmann.stocks.execution.PaperTrader;
import ch.pschatzmann.stocks.parameters.State;
import ch.pschatzmann.stocks.strategy.ITradingStrategy;
import ch.pschatzmann.stocks.strategy.optimization.Fitness;

/**
 * Distribution by Sharpe Ration of the Strategy
 * @author pschatzmann
 *
 */

public class SharpeRatioDistributor extends Distributor implements Serializable {
	private static final long serialVersionUID = 1L;
	private IAccount account;
	private int evaluationPeriods = 365;
	private double minFactor = 0.0;
	private double riskFreeReturnInPercent;
	
	public SharpeRatioDistributor(IAccount account) {
		super(0.0);
		this.account = account;
	}
	
	public SharpeRatioDistributor(IAccount account, Double riskFreeReturnInPercent) {
		super(0.0);
		this.account = account;
		this.riskFreeReturnInPercent = riskFreeReturnInPercent;
	}
	
	@Override
	public void add(ITradingStrategy strategy, Date date) {
		double ratio = getSharpeRatio(strategy, date);
		super.add(strategy, ratio);		
	}

	private double getSharpeRatio(ITradingStrategy strategy, Date date) {
		DateRange dr = new DateRange(Context.getDateWithOffsetDays(date, -evaluationPeriods),date);
		Account simulationAccount = new Account("Sharpe Ratio",account.getCurrency(), account.getInitialCash(),Context.getDateWithOffsetDays(date, -365) , account.getFeesModel());
		simulationAccount.setRiskFreeReturnInPercent(riskFreeReturnInPercent);
		PaperTrader trader = new PaperTrader(simulationAccount);
		State state = new Fitness(trader).getFitness(strategy, dr);
		return state.getResult().getDouble(KPI.SharpeRatio);
	}

	public int getEvaluationPeriods() {
		return evaluationPeriods;
	}

	public void setEvaluationPeriods(int evaluationPeriods) {
		this.evaluationPeriods = evaluationPeriods;
	}

	public double getMinFactor() {
		return minFactor;
	}

	public void setMinFactor(double minFactor) {
		this.minFactor = minFactor;
	}


}
