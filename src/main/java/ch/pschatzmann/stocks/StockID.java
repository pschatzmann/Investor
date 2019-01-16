package ch.pschatzmann.stocks;

import java.io.Serializable;

/**
 * Identifies a stock and the related exchange. This object is used as key for all stock
 * related functionality
 * 
 * @author pschatzmann
 *
 */
public class StockID implements Serializable, IStockID {
	private static final long serialVersionUID = 1L;
	private String ticker="";
	private String exchange="";

	/**
	 * Default Consructor
	 * @param ticker
	 * @param exchange
	 */
	public StockID(String ticker, String exchange){
		this.ticker = ticker;
		this.exchange = exchange;
	}
	
	/**
	 * Stock with no Exchange defined
	 * @param ticker
	 */
	public StockID(String ticker) {
		this(ticker,"");
	}
	
	public StockID() {
	}

	public StockID(IStockID id) {
		this.ticker = id.getTicker();
		this.exchange = id.getExchange();
	}

	@Override
	public String getTicker() {
		return str(ticker);
	}
	public void setTicker(String ticker) {
		this.ticker = ticker;
	}
	@Override
	public String getExchange() {
		return str(exchange);
	}
	public void setExchange(String exchange) {
		this.exchange = exchange;
	}
	
	@Override
	public String toString() {
		return Context.isEmpty(exchange) ? ticker : exchange+":"+ticker;
	}
	
	public static IStockID parse(String str) {
		String[]sa = str.split(":|,");
		StockID result = new StockID();
		if (sa.length==1){
			result.setTicker(sa[0]);
		} else {
			result.setTicker(sa[1]);
			result.setExchange(sa[0]);
		}
		return result;
	}
	
	@Override
	public int compareTo(IStockID oi) {
		if (oi==null) {
			return -1;
		}
		// check exchange if both ids have an exchange defined
		int result = this.getTicker().compareTo(oi.getTicker());
		if (result==0 && !Context.isEmpty(oi.getExchange()) && !Context.isEmpty(this.getExchange())) {
			 result = this.getExchange().compareTo(oi.getExchange());			
		}
		return result;
	}
	
	private String str(String str) {
		return str==null ? "":str;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj==null) {
			// prevent NPE
			return false;
		} else if (obj instanceof IStockID) {
			// standard case
			return this.compareTo((IStockID)obj) == 0;
		} else {
			// if we compare with a string we convert the string into a StockID
			return this.compareTo(StockID.parse(obj.toString())) == 0;
		}
	}
		
	public static int compare(IStockID o1, IStockID o2) {
		return o1.compareTo(o2);
	}
	
	@Override
	public int hashCode() {
		return this.getTicker().hashCode();
	}
}
