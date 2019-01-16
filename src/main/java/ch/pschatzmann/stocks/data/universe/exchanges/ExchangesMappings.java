package ch.pschatzmann.stocks.data.universe.exchanges;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

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

public class ExchangesMappings implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public enum ExchangeNameSpace { MarketExchange, WIKI};
	private static final Logger LOG = LoggerFactory.getLogger(ExchangesMappings.class);
	private static Map<String,String> toLocalMap = new HashMap();
	private static Map<String,String> fromLocalMap = new HashMap();
		
	/**
	 * Tries to translate the common code into the coding used by the target system.
	 * if no mapping is found we return the input data
	 * @param commonCode
	 * @param ns
	 * @return
	 * @throws IOException
	 */
	public String toLocalNamespace(String commonCode,ExchangeNameSpace ns) throws IOException {
		setup();
		String result = toLocalMap.get(toString(commonCode,ns));
		return result !=null ? result : commonCode;
	}

	/**
	 * We try to translate the local namespace to the global namespace. If no mapping is found
	 * the input is used.
	 * 
	 * @param localCode
	 * @param ns
	 * @return
	 * @throws IOException
	 */
	public String fromLocalNamespace(String localCode,ExchangeNameSpace ns) throws IOException {
		setup();
		String result = fromLocalMap.get(localCode);
		return result !=null ? result : localCode;
	}
	
	private void setup() throws IOException {
		if (toLocalMap.isEmpty()) {
			Streams.asStream(Streams.getInputStream("exchangesMapping.csv")).map(line -> line.split(",")).forEach(array -> classify(array));
		}
	}
	
	private void classify(String[]sa) {
		String common = sa[0].trim();
		String yahoo = sa[1].trim();
		String marketEx = sa[2].trim();
		String wiki = sa[3].trim();
		// a empty value is valid and needs to map to no exchange. If the entry does not exist it is marked with
		// a - . so we need to ignore these entries

		if (!marketEx.equals("-")) {
			fromLocalMap.put(marketEx, common);
			toLocalMap.put(toString(common, ExchangeNameSpace.MarketExchange), marketEx);
		}
		
		if (!wiki.equals("-")) {
			fromLocalMap.put(wiki, common);
			toLocalMap.put(toString(common, ExchangeNameSpace.WIKI), wiki);
		}
		
	} 

	private String toString(String str, ExchangeNameSpace ns) {
		return ns.name()+"/"+str;
	}
	
}
