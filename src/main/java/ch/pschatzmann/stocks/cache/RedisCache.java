package ch.pschatzmann.stocks.cache;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.redisson.Redisson;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.pschatzmann.stocks.Context;
import ch.pschatzmann.stocks.IStockRecord;

/**
 * Cache which is using Redis as caching system. The following parameters are
 * supported: RedisCacheAddress, RedisCacheMaxSize, RedisCacheTTLInHours
 */
public class RedisCache implements ICache {
	private static final Logger LOG = LoggerFactory.getLogger(RedisCache.class);
	private RedissonClient client;
	private RMapCache<String, List<IStockRecord>> map;

	public RedisCache() {
		Config config = new Config();
		config.useSingleServer().setAddress(Context.getPropertyMandatory("RedisCacheAddress"))
				.setConnectTimeout(1000).setTimeout(20000);
		config.setCodec(new org.redisson.codec.SnappyCodec());

		RedissonClient client = Redisson.create(config);
		map = client.getMapCache("stocks");
		map.setMaxSize(Integer.parseInt(Context.getProperty("RedisCacheMaxSize", "500")));
	}

	@Override
	public void put(String key, List<IStockRecord> records) {
		if (map != null && key != null && records != null) {
			LOG.info("put {}", key);
			map.put(key, records, Integer.parseInt(Context.getProperty("RedisCacheTTLInHours", "6")), TimeUnit.HOURS);
		}
	}

	@Override
	public List<IStockRecord> get(String key) {
		if (map != null && key != null) {
			LOG.info("get {}", key);
			return map.get(key);
		}
		return null;
	}

	@Override
	public void clear() {
		LOG.info("clear");
		map.clear();

	}

	@Override
	public void remove(String key) {
		LOG.info("remove {}", key);
		map.remove(key);
	}

}
