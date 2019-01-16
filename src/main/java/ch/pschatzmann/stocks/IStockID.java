package ch.pschatzmann.stocks;

/**
 * Interface with Identifies a stock and the related exchange.
 *
 * @author pschatzmann
 *
 */
public interface IStockID extends Comparable<IStockID> {
	/**
	 * Determines the ticker symbol
	 * 
	 * @return
	 */
	public String getTicker();

	/**
	 * Provides the exchange where the stock is traded
	 * 
	 * @return
	 */
	public String getExchange();

}
