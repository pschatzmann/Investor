package ch.pschatzmann.stocks.data.universe;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ch.pschatzmann.stocks.Context;
import ch.pschatzmann.stocks.IStockID;
import ch.pschatzmann.stocks.StockID;
import ch.pschatzmann.stocks.errors.UniverseException;
import ch.pschatzmann.stocks.utils.Streams;

/**
 * Determines the universe from a csv file
 * 
 * @author pschatzmann
 *
 */
public class MarketUniverse implements IUniverse, Serializable {
	private static final long serialVersionUID = 1L;
	private URL file;
	private String exchangeRegex = ".*";
	private String tickerRegex = ".*";

	public MarketUniverse() {
		try {
			this.file = new URL(Context.getPropertyMandatory("MarketArchiveURL") + "/stocks-data/stocks.csv");
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	public MarketUniverse(String exchangeRegex) throws MalformedURLException {
		this();
		this.exchangeRegex = exchangeRegex;
	}

	public MarketUniverse(String exchangeRegex, String tickerRegex) throws MalformedURLException {
		this();
		this.exchangeRegex = exchangeRegex;
		this.tickerRegex = tickerRegex;
	}

	public MarketUniverse(URL file) {
		this.file = file;
	}

	@Override
	public Stream<IStockID> stream() {
		try {
			Stream<IStockID> stream = Streams.asStream(file.openStream()).map(str -> StockID.parse(str))
					.filter(st -> !st.getExchange().equals("stocks-data")).filter(st -> !st.getTicker().isEmpty()
							&& st.getTicker().matches(tickerRegex) && st.getExchange().matches(exchangeRegex));
			return stream;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public List<IStockID> list()  {
		return stream().collect(Collectors.toList());
	}

	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("MarketUniverese: ");
		sb.append("exchange=");
		sb.append(exchangeRegex);
		sb.append(" ticker=");
		sb.append(this.tickerRegex);
		return sb.toString();
	}

}
