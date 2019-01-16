package ch.pschatzmann.stocks.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import org.apache.commons.jcs.JCS;
import org.apache.commons.jcs.access.CacheAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.pschatzmann.stocks.Context;
import ch.pschatzmann.stocks.IStockRecord;

/**
 * Cache implementation using JCS. JCS behaves badly if we have objects from
 * different JVM instance. To prevent these issues we add an instance dependent
 * key to our object keys.
 * 
 * @author pschatzmann
 *
 */
public class JcsCache implements ICache {
	private static final Logger LOG = LoggerFactory.getLogger(JcsCache.class);
	public static CacheAccess<String, List<IStockRecord>> cache;

	public JcsCache() {
		try {
			// support for csf configuration in current directory
			File config = new File("cache.ccf");
			if (config.exists()) {
				InputStream input = new FileInputStream(config);
				Properties configProps = new Properties();
				// load a properties file
				configProps.load(input);
				input.close();	
				JCS.setConfigProperties(configProps);
			}
			cache = JCS.getInstance("stocksCache");
		} catch (Exception ex) {
			LOG.error(ex.getMessage(), ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.pschatzmann.stocks.cache.ICache#put(java.lang.String, java.util.List)
	 */
	@Override
	public void put(String key, List<IStockRecord> records) {
		if (cache != null && key != null && records != null) {
			LOG.info("put {}",key);
			cache.put(Context.getID() + "/" + key, records);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.pschatzmann.stocks.cache.ICache#get(java.lang.String)
	 */
	@Override
	public List<IStockRecord> get(String key) {
		LOG.info("get {}", key);
		return key == null || cache == null ? null : cache.get(Context.getID() + "/" + key);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.pschatzmann.stocks.cache.ICache#clear()
	 */
	@Override
	public void clear() {
		LOG.info("clear");
		cache.clear();
	}

	@Override
	public void remove(String key) {
		if (cache != null && key != null) {
			LOG.info("remove {}", key);
			cache.remove(Context.getID() + "/" + key);
		}
		
	}
}
