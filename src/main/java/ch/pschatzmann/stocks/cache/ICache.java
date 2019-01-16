package ch.pschatzmann.stocks.cache;

import java.util.List;

import ch.pschatzmann.stocks.IStockRecord;

/**
 * Interface of the Cache for the stock history
 * 
 * @author pschatzmann
 *
 */

public interface ICache {

	/**
	 * Stores the stock history in the cache
	 * 
	 * @param key
	 * @param records
	 */
	void put(String key, List<IStockRecord> records);

	/**
	 * Returns the stock history
	 * 
	 * @param key
	 * @return
	 */
	List<IStockRecord> get(String key);

	/**
	 * Removes the entry which is identified by the key
	 * 
	 * @param key
	 */
	void remove(String key);

	/**
	 * Clears the cache
	 */
	void clear();

}