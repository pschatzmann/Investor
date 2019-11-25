package ch.pschatzmann.stocks.data.universe;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipInputStream;

import org.apache.commons.jcs.JCS;
import org.apache.commons.jcs.access.CacheAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.pschatzmann.stocks.Context;
import ch.pschatzmann.stocks.IStockID;
import ch.pschatzmann.stocks.StockID;
import ch.pschatzmann.stocks.errors.UniverseException;
import ch.pschatzmann.stocks.input.IReader;
import ch.pschatzmann.stocks.utils.Streams;

/**
 * Provides the stocks which are available via the Quandl WIKI database
 * 
 * @author pschatzmann
 *
 */

public abstract class QuandlCommonUniverse implements IUniverse, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(QuandlCommonUniverse.class);
	private static String apiKey = Context.getPropertyMandatory("QuandlAPIKey");
	private static CacheAccess<String, List<IStockID>> cache;;
	private String databaseCode;
	private String synbolRegex = ".*";
	private IReader reader;

	public QuandlCommonUniverse(String databaseCode, IReader reader) {
		this.databaseCode = databaseCode;
		this.reader = reader;
		if (cache == null) {
			try {
				if (Context.isCacheActive()) {
					cache = JCS.getInstance("default");
				}
			} catch (Exception ex) {
				LOG.warn(ex.getLocalizedMessage(), ex);
			}
		}
	}

	protected String getPrefix() {
		return "https://www.quandl.com/api/v3/databases/"+databaseCode+"/metadata";
	}


	public QuandlCommonUniverse(String databaseCode, IReader reader, String symbolRegex) {
		this(databaseCode, reader);
		this.synbolRegex = symbolRegex;
	}

	/**
	 * List all available valid StockID for this universe which are matching with
	 * the defined stock exchange or symbol (if indicated).
	 */
	@Override
	public Stream<IStockID> stream() {
		try {
			List<IStockID> data = cache == null ? null : cache.get(getPrefix());
			if (data == null) {
				String urlString = getPrefix() + "?api_key=" + QuandlCommonUniverse.apiKey;
				LOG.info(urlString);
				URL url = new URL(urlString);
				InputStream is = getFirstZipInputStream(url.openStream());
				data = Streams.asStream(is, "utf-8").map(s -> toStockID(s))
						.filter(id -> !Context.isEmpty(id.getTicker()) && id.getTicker().matches(synbolRegex))
						.collect(Collectors.toList());
				if (cache != null) {
					cache.put(getPrefix(), data);
				}
			}
			return data.stream();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}

	}

	/**
	 * The codes request returns a zip file
	 * 
	 * @param is
	 * @return
	 * @throws IOException
	 */
	protected InputStream getFirstZipInputStream(InputStream is) throws IOException {
		ZipInputStream zis = new ZipInputStream(is);
		zis.getNextEntry();
		return zis;
	}

	/**
	 * The first 2 words are WKIKI and the stock id
	 * 
	 * @param str
	 * @return
	 */
	protected IStockID toStockID(String str) {
		String stockID = null;
		String exchange = null;
		String sa[] = str.split(",|/");
		if (sa.length >= 3 && !sa[1].equals("name")) {
			stockID = sa[0];
			exchange = "";
		}
		return new StockID(stockID, exchange);
	}

	public IReader getReader() {
		return this.reader;
	}

	public static void setApiKey(String apiKey) {
		QuandlCommonUniverse.apiKey = apiKey;
	}

	public static String getApiKey() {
		return apiKey;
	}
	
	@Override
	public List<IStockID> list()  {
		return stream().collect(Collectors.toList());
	}

}
