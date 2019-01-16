package ch.pschatzmann.stocks.data.universe;

import java.io.File;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.pschatzmann.stocks.IStockID;
import ch.pschatzmann.stocks.StockID;
import ch.pschatzmann.stocks.errors.UniverseException;

/**
 * Loads universe from json
 * 
 * @author pschatzmann
 *
 */
public class JsonUniverse implements IUniverse, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private URL file;

	public JsonUniverse(String fileName) {
		this.file = this.getClass().getResource(fileName);
	}

	public JsonUniverse(File file) throws MalformedURLException {
		this.file = file.toURI().toURL();
	}

	@Override
	public Stream<IStockID> stream() {
		ObjectMapper mapper = new ObjectMapper();
		try {
			JsonNode root = mapper.readTree(file);
			Iterator<JsonNode> it = root.elements();
			int characteristics = Spliterator.DISTINCT | Spliterator.SORTED | Spliterator.ORDERED;
			Spliterator<JsonNode> spliterator = Spliterators.spliteratorUnknownSize(it, characteristics);
			return StreamSupport.stream(spliterator, false).map(json -> toID(json));
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}

	}

	private IStockID toID(JsonNode node) {
		JsonNode obj = node.get("id");
		String ticker = obj.get("ticker").textValue();
		String exchange = obj.get("exchange").textValue();
		return new StockID(ticker, exchange);
	}

	@Override
	public List<IStockID> list()  {
		return stream().collect(Collectors.toList());
	}
}
