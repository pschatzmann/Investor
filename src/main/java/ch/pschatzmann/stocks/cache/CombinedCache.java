package ch.pschatzmann.stocks.cache;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.pschatzmann.stocks.IStockRecord;

/**
 * Combined local and redis cache to improve the performance
 * 
 * @author pschatzmann
 *
 */
public class CombinedCache implements ICache {
	private static final Logger LOG = LoggerFactory.getLogger(CombinedCache.class);
	private static List<ICache> caches = new ArrayList();

	public CombinedCache() {
		if (caches.isEmpty()) {
			try {
				caches.add(new JcsCache());
			} catch(Exception ex) {
				LOG.error(ex.getMessage(),ex);
			}
			
			try {
				caches.add(new RedisCache());				
			} catch(Exception ex) {
				LOG.error(ex.getMessage(),ex);
			}
		}
	}

	@Override
	public void put(String key, List<IStockRecord> records) {
		LOG.info("put {}",key);
		caches.stream().forEach(c -> c.put(key, records));
	}

	@Override
	public List<IStockRecord> get(String key) {
		LOG.info("get {}", key);
		List<ICache> visitedCaches = new ArrayList();
		for (ICache c : caches) {
			try {
				List<IStockRecord> result = c.get(key);
				if (result != null) {
					// add to fast cache to speed up access in next reqest
					for (ICache cOld : visitedCaches) {
						try {
							cOld.put(key, result);
						} catch(Exception ex) {
							LOG.warn("Could not add enty to cache {}",cOld.getClass().getSimpleName());
						}
					}
					return result;
				}
			} catch (Exception ex) {
				LOG.warn("Could not access cache - we try the next: {}", ex.getMessage());
			}
			visitedCaches.add(c);
		}
		return null;
	}

	@Override
	public void remove(String key) {
		caches.stream().forEach(c -> c.remove(key));

	}

	@Override
	public void clear() {
		caches.stream().forEach(c -> c.clear());
	}

}
