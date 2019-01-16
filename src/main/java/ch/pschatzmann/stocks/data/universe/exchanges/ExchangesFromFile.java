package ch.pschatzmann.stocks.data.universe.exchanges;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.pschatzmann.stocks.utils.Streams;


/**
 * Functionality which provides the scope of the known and supported exchanges
 * which are used for evaluating data
 * 
 * @author pschatzmann
 *
 */

public class ExchangesFromFile implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(ExchangesFromFile.class);
	private  List<String> list;
	private String fileName = "exchanges.csv";
	
	public ExchangesFromFile() {
	}

	public ExchangesFromFile(String fileName) {
		this.fileName = fileName;
	}
	
	/**
	 * Returns all exchanges for which we have stock data
	 * @return
	 * @throws IOException 
	 */
	public Collection<String> list() throws IOException {
		if (list==null) {
			InputStream is = Streams.getInputStream(fileName);
			list = Streams.asStream(is).collect(Collectors.toList());
		}
		return list;
	}
	
	/**
	 * Returns all matching exchanges
	 * @param regex
	 * @return
	 */	
	public List<String> list(String regex) {
		return list.stream().filter(exchange -> exchange.matches(regex)).collect(Collectors.toList());
	}
	
}
