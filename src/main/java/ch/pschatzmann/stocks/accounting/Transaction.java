package ch.pschatzmann.stocks.accounting;

import java.io.Serializable;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import ch.pschatzmann.stocks.Context;
import ch.pschatzmann.stocks.IStockID;
import ch.pschatzmann.stocks.StockID;

/**
 * Order to buy or sell an individual stock. This object is also used to record
 * cash transfers in and out of the account;
 * 
 * @author pschatzmann
 *
 */

public class Transaction implements Serializable, Comparable<Transaction> {
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(Transaction.class);
	private static IDGenerator idGenerator = new IDGenerator();

	/**
	 * Type of Order (Market, Limit, Stop...)
	 */
	public enum Type {
		Market, Limit, Stop, CashTransfer
	}

	/**
	 * Requested Action
	 */
	public enum Action {
		SellAll, BuyMax
	}

	public enum Status {
		Planned, Submitted, Filled, Cancelled
	}

	/**
	 * Automatically Determined if Buy or Sell
	 */
	public enum BuyOrSell {
		Sell, Buy, NA
	};

	@JsonDeserialize(as = StockID.class)
	private IStockID stockID;
	private Date date;
	private long quantity;
	private double requestedPrice;
	private double cash;
	private Type transactionType;
	private double filledPrice;
	private double fees;
	private String comment = "";
	private String id = null;
	private Status status = Status.Planned;

	public Transaction() {
	}

	public Transaction(Date date, IStockID id, long quantity) {
		this.stockID = id;
		this.date = date;
		this.quantity = quantity;
		this.transactionType = Type.Market;
	}

	public Transaction(Date date, IStockID id, IAccount acc, Action act) {
		this.stockID = id;
		this.date = date;

		switch (act) {
		case SellAll:
			Portfolio p = acc.getPortfolio(date);
			transactionType = Type.Market;
			PortfolioStockInfo line = p.getInfo(id);
			this.quantity = line.getQuantity();
		case BuyMax:
			Double cash = acc.getCash(date);
			Double price = acc.getStockPrice(id, date);
			this.quantity = Double.valueOf(cash / price).longValue();
		}
	}

	public Transaction(Date date, StockID id, long quantity, double price, Type orderType) {
		this.stockID = id;
		this.date = date;
		this.quantity = quantity;
		this.transactionType = orderType;
		this.requestedPrice = price;
	}

	/**
	 * Transfer cash into the account (positive) or out of the account (negative)
	 */
	public Transaction(Date date, double cash) {
		this.transactionType = Type.CashTransfer;
		this.stockID = Context.cashID();
		this.cash = cash;
		this.date = date;
		this.status = Status.Filled;
	}

	public IStockID getStockID() {
		return stockID;
	}

	public Date getDate() {
		return date;
	}

	public Long getQuantity() {
		return quantity;
	}

	/**
	 * Indicates the oder type
	 */
	public Type getRequestedPriceType() {
		return transactionType;
	}

	/**
	 * Defines the Price Type
	 * 
	 * @param t
	 */
	public void setRequestedPriceType(Type t) {
		this.transactionType = t;
	}

	/**
	 * Not available for market orders. Indicates the Limit or Stop price of the
	 * order
	 * 
	 * @return
	 */
	public Double getRequestedPrice() {
		return requestedPrice;
	}

	public double getFees() {
		return fees;
	}

	public void setFees(double fees) {
		this.fees = fees;
	}

	public double getFilledPrice() {
		return filledPrice;
	}

	public BuyOrSell getBuyOrSell() {
		if (this.getQuantity() == 0) {
			return BuyOrSell.NA;
		} else if (this.getQuantity() > 0) {
			return BuyOrSell.Buy;
		} else {
			return BuyOrSell.Sell;
		}
	}

	/**
	 * fills the order with the indicated information. The fees are calculated with
	 * the help of the fees model. The quantity is reduced if there is not enogh
	 * cash
	 * 
	 * @param date
	 * @param quantity
	 * @param fillPrice
	 * @param account
	 */
	public synchronized void fill(Date date, long quantity, double fillPrice, IAccount account) {
		if (account.getAccount().isMargin()) {
			this.quantity = quantity;
		} else {
			double currentCash = account.getCash(date);
			if (quantity > 0) {
				// LOG.info("fill qty:"+quantity+" price:"+fillPrice+" currentCash:"+
				// currentCash+" date "+Context.format(date));
				if (quantity * fillPrice <= currentCash) {
					this.quantity = quantity;
				} else {
					Long oldQty = quantity;
					this.quantity = (long) (currentCash / fillPrice);
					// the quantity can not be negative
					if (this.quantity < 0) {
						this.quantity = 0;
					}

					LOG.info("Not enough cash to fill the full order: " + this + " setting quantity to " + this.quantity
							+ " instead of " + oldQty);
				}
			}
		}

		this.filledPrice = fillPrice;
		this.date = date;
		this.fees = account.getAccount().getFeesModel().getFeesPerTrade(quantity, fillPrice * quantity);
		this.setStatus(Status.Filled);
		LOG.debug("-> fill " + this.getStockID() + " " + Context.format(this.getDate()) + " " + this.quantity + " * "
				+ fillPrice);
	}

	/**
	 * Fills the order with the indicated information. 
	 * 
	 * @param date
	 * @param quantity
	 * @param fillPrice
	 * @param fees
	 */
	public synchronized void fill(Date date, long quantity, double fillPrice, double fees) {
		this.date = date;
		this.quantity = quantity;
		this.filledPrice = fillPrice;
		this.fees = fees;
		this.setStatus(Status.Filled);
	}

	/**
	 * if cashIn we return the cash value if we sell (= negative quantity) the cash
	 * increases. if we buy (= positive quantity) the cash decreases
	 * 
	 * @return
	 */
	public double getImpactOnCash() {
		return this.cash != 0 ? cash
				: this.transactionType == Type.CashTransfer ? 0.0 : (-this.quantity * filledPrice) - getFees();
	}

	public void setImpactOnCash(double value) {
		this.cash = value;
	}

	@JsonIgnore
	public double getPurchasedValue() {
		return this.quantity * filledPrice;
	}

	/**
	 * Returns true if the order is to buy stock on the market (indicated by a
	 * positive quantity)
	 * 
	 * @return
	 */
	@JsonIgnore
	public boolean isBuy() {
		return this.getQuantity() > 0;
	}

	/**
	 * Returns true if the order is to sell stock on the market (indicated by a
	 * negative quantity)
	 * 
	 * @return
	 */
	@JsonIgnore
	public boolean isSell() {
		return this.getQuantity() < 0;
	}

	/**
	 * Determine if the order is filled (true) or still open (false)
	 * 
	 * @return
	 */
	@JsonIgnore
	public boolean isFilled() {
		return this.getStatus() == Status.Filled;
	}

	/**
	 * Determine if the order is filled (false) or still open (true)
	 * 
	 * @return
	 */
	@JsonIgnore
	public boolean isOpen() {
		return !isFilled();
	}

	@JsonIgnore
	public boolean isCashTransfer() {
		return this.transactionType == Type.CashTransfer;
	}

	@Override
	public int compareTo(Transaction o) {
		int result = this.getDate().compareTo(o.getDate());
		if (result == 0) {
			result = StockID.compare(this.getStockID(), o.getStockID());
			if (result == 0) {
				result = this.getID().compareTo(o.getID());
			}
		}
		return result;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(this.stockID);
		sb.append(" ");
		sb.append(Context.format(this.getDate()));
		sb.append(" ");
		if (this.isCashTransfer()) {
			sb.append(this.getImpactOnCash());
		} else {
			sb.append(this.getQuantity());
			sb.append(" * ");
			sb.append(this.getFilledPrice());
		}

		if (!isFilled()) {
			sb.append(" (not filled!)");
		}
		sb.append(" ");
		sb.append(this.getComment());
		return sb.toString();
	}

	public void cancel() {
		LOG.info("cancel "+this);
		this.setStatus(Status.Cancelled);
		this.fees = 0;
		this.filledPrice = 0.0;
	}
	
	public void cancel(String comment) {
		LOG.info("cancel "+this);
		this.setStatus(Status.Cancelled);
		this.setComment(comment);
		this.fees = 0;
		this.filledPrice = 0.0;
	}
	

	public boolean isActive() {
		return this.getStatus() != Status.Cancelled;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public void setQuantity(long total) {
		this.quantity = total;
	}

	public String getID() {
		if (this.id == null) {
			this.id = String.valueOf(idGenerator.getID());
		}
		return this.id;
	}

	public void setID(String id) {
		this.id = id;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

}
