package ch.pschatzmann.stocks.data.universe;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.pschatzmann.stocks.IStockID;
import ch.pschatzmann.stocks.StockID;
import ch.pschatzmann.stocks.errors.UniverseException;

/**
 * Provides the stock symbols from https://api.iextrading.com/1.0/ref-data/symbols
 * @author pschatzmann
 *
 */
public class IEXUniverse implements IUniverse {
	private List<IStockID> data;


	@Override
	public List<IStockID> list() {
		if (data==null) {
			try {
				data = new ArrayList();
				ObjectMapper mapper = new ObjectMapper();
				URL url = new URL("https://api.iextrading.com/1.0/ref-data/symbols");
				List<Object> values = mapper.readValue(url, List.class);
				for (Object v : values) {
					Map<String,String> map = (Map<String, String>) v;
					StockID id = new StockID(map.get("symbol"),"");
					data.add(id);
				}
			} catch(Exception ex) {
				throw new RuntimeException(ex);
			}
		}
		return data;
	}

}
