package ch.pschatzmann.stocks.accounting;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.pschatzmann.stocks.Context;
import ch.pschatzmann.stocks.IStockID;

/**
 * Stock Portfolio information for a specific date. We provide summarize information at a specific date
 * for a each individual stocks which was held at any point of time.
 * 
 * @author pschatzmann
 *
 */

public class Portfolio implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(Portfolio.class);
	private IAccount ta;
	private Date date;
	private Map <IStockID, PortfolioStockInfo> data = new HashMap();
	/**
	 * Basic first constructor which connects the portfolio with the account
	 * @param ta
	 * @param date
	 */
	public Portfolio(IAccount ta, Date date) {
		LOG.debug("Portfolio new "+Context.format(date));
		this.ta = ta;
		this.date = date;
	}
	
	/**
	 * We setup the protfolio for a new date as a copy of the information of the prior date.
	 * 
	 * @param ta
	 * @param date
	 * @param prior
	 */
		
	public Portfolio(IAccount ta, Date date, Portfolio prior) {
		LOG.debug("Portfolio copy");
		this.ta = ta;
		this.date = date;
		if (prior!=null) {
			for (Entry <IStockID, PortfolioStockInfo> e : prior.data.entrySet()) {
				this.data.put(e.getKey(), new PortfolioStockInfo(date, e.getValue()));				
			}
		}
	} 
	

	/**
	 * We update the value of the stock based on the actual prices on the market
	 */
	public void updateActualPrices() {
		for (Entry<IStockID, PortfolioStockInfo> e : data.entrySet()) {
			PortfolioStockInfo line = e.getValue();
			if (!line.isCash()){
				Double price = ta.getStockPrice(line.getStockID(), this.date);
				if (price!=null) {
					line.setActualValue(line.getQuantity() * price.doubleValue());
				} else {
					LOG.info("No rate found for "+line.getStockID()+" for "+this.date);
				}
			}
		}
	}
	
	/**
	 * Update the date
	 * @param date
	 */
	public void setDate(Date d) {
		this.date = d;
	}
	
	/**
	 * Determines the date for which the portfolio totals and prices are updated
	 * @return
	 */
	public Date getDate() {
		return this.date;
	}
	
	/**
	 * Total of all values of all stocks valuated at actual rates
	 * @return actualValue
	 */
	public double getActualValue() {
		return data.entrySet().stream().mapToDouble(entry -> entry.getValue().getActualValue()).sum();
	}

	/**
	 * Total of cash and all values of all stocks valuated at actual rates
	 * @return total Value
	 */
	public double getTotalValue() {
		return data.entrySet().stream().mapToDouble(entry -> entry.getValue().getActualValue()+entry.getValue().getImpactOnCash()).sum();
	}

	/**
	 * Total of all values of all stocks valuated at purchase prices
	 * @return
	 */
	public double getPurchasedValue() {
		return data.entrySet().stream().mapToDouble(entry -> entry.getValue().getPurchasedValue()).sum();
	}

	/**
	 * Determines the total fees
	 * @return fees
	 */
	public double getFees() {
		return data.entrySet().stream().mapToDouble(entry -> entry.getValue().getFees()).sum();
	}
	/**
	 * Determines the impact on the amount of cash of the trade
	 * @return
	 */
	public double getImpactOnCash() {
		return data.entrySet().stream().mapToDouble(entry -> entry.getValue().getImpactOnCash()).sum();
	}

	/**
	 * Calculates the unrealized gains
	 * @return
	 */
	public double getUnrealizedGains() {
		return data.entrySet().stream().mapToDouble(entry -> entry.getValue().getUnrealizedGains()).sum();
	}

	/**
	 * Calculates the realized gains. We do not consider any fees!
	 * @return
	 */
	public double getRealizedGains() {
		return data.entrySet().stream().mapToDouble(entry -> entry.getValue().getRealizedGains()).sum();
	}
	
	/**
	 * Determines the total number of sales and buys
	 * @return
	 */
	public long getNumberOfTrades() {
		return data.entrySet().stream().mapToLong(entry -> entry.getValue().getNumberOfTrades()).sum();
	}

	/**
	 * Determines the current total profit (realized and unrealized gains) minus the fees
	 * @return
	 */
	public double getTotalProfit() {
		return this.getRealizedGains() + this.getUnrealizedGains() - getFees();
	}

	/**
	 * Determines the information for an individual stock
	 * @param id
	 * @return
	 */
	public PortfolioStockInfo getInfo(IStockID id) {
		PortfolioStockInfo result = this.data.get(id);
		if (result==null){
			result = new PortfolioStockInfo(this.getDate(), id);
		}
		return result;
	}
	
	public Collection<PortfolioStockInfo> getInfo() {
		return this.data.values();
	}
	
	/**
	 * Determines all stock ids from this portfilio
	 * @return
	 */
	public Collection<IStockID> getStockIDs() {
		return data.keySet();
	}
	
	
	@Override
	public String toString() {
		return Context.format(this.getDate()) +" "+this.getTotalValue()+" ("+this.getActualValue()+" "+this.getImpactOnCash()+") -> "+this.getTotalProfit();
	}
	
	/**
	 * We process the transactions to update the portfolio information.
	 * @param order
	 */
	protected void recordOrder(Transaction order, Date forDate) {
		PortfolioStockInfo line = data.get(order.getStockID());
		long numerOfTrades = (!order.isCashTransfer()) ? 1L : 0L;
		if (line==null) {
			// add new line because the stock does not exist yet
			line = new PortfolioStockInfo(forDate,order.getStockID(),order.getQuantity(), order.getPurchasedValue(), order.getFees(), order.getImpactOnCash(), numerOfTrades);
			data.put(order.getStockID(), line);
		} else {
			// calcluate realized gains when we sell the stock
			double realizedGain = 0.0;
			if (order.getQuantity().doubleValue()<0.0) {
				realizedGain = (order.getFilledPrice() - line.getPurchasedAveragePrice()) * -order.getQuantity();
			}
			// update existing value
			Double avgPrice = line.getPurchasedAveragePrice();
			line.addQuantity(order.getQuantity());
			line.addPurchasedValue(order.getQuantity()>0.0 ?  order.getPurchasedValue() : order.getQuantity() * avgPrice);
			line.addFees(order.getFees());
			line.addImpactOnCash (order.getImpactOnCash());			
			line.addNumberOfTrades(numerOfTrades);
			line.addRealizedGains(realizedGain);
			data.put(order.getStockID(), line);			
		}
		
		// collect the related transactions
		LOG.debug("recordOrder "+order +" realized gains: "+line.getRealizedGains());

		line.addTransaction(order);
	}
	
	

}
