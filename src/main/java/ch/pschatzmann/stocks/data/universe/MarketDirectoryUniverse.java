package ch.pschatzmann.stocks.data.universe;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.pschatzmann.stocks.IStockID;
import ch.pschatzmann.stocks.errors.UniverseException;
import ch.pschatzmann.stocks.input.parser.MarketArchiveParser;

/**
 * All stock from the Market Universe (loading the data from a file
 * 
 * @author pschatzmann
 *
 */
public class MarketDirectoryUniverse implements IUniverse, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(MarketDirectoryUniverse.class);
	private File root = null;;
	private File directory;
	private String tickerRegex;

	public MarketDirectoryUniverse() {
		this(new File("/var/www/stocks-data"), null, null);
	}

	public MarketDirectoryUniverse(String exchange) {
		this(new File("/var/www/stocks-data"), exchange, null);
	}

	public MarketDirectoryUniverse(String exchange, String regex) {
		this(new File("/var/www/stocks-data"), exchange, regex);
	}

	public MarketDirectoryUniverse(File path) {
		this(path, null, null);
	}

	public MarketDirectoryUniverse(File path, String exchange) {
		this(path, exchange, null);
	}

	public MarketDirectoryUniverse(File path, String exchange, String tickerRegex) {
		directory = path;
		if (exchange != null && !exchange.isEmpty()) {
			directory = new File(path, exchange);
		}
		if (tickerRegex != null && !tickerRegex.isEmpty()) {
			this.tickerRegex = tickerRegex;
		} else {
			this.tickerRegex = ".*";
		}
	}

	@Override
	public Stream<IStockID> stream()  {
		return stream(true);
	}

	public Stream<IStockID> stream(boolean adusted)  {
		final MarketArchiveParser ip = new MarketArchiveParser(directory.getAbsolutePath(), adusted);
		try {
			Stream<IStockID> result = Files.walk(Paths.get(directory.getAbsolutePath()))
					.filter(f -> !f.toFile().isDirectory()).map(f -> f.normalize().toString()).parallel()
					.map(s -> ip.parseFileName(s)).filter(id -> id != null).filter(id -> id.getExchange() != null)
					.filter(id -> !id.getExchange().matches("out|stocks.csv")).filter(id -> id.getTicker() != null)
					.filter(id -> id.getTicker().matches(tickerRegex));

			return result;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public List<IStockID> list()  {
		return stream().collect(Collectors.toList());
	}


}
