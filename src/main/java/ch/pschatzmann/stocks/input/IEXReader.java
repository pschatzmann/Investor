package ch.pschatzmann.stocks.input;

import java.io.Serializable;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.pschatzmann.stocks.Context;
import ch.pschatzmann.stocks.IStockTarget;
import ch.pschatzmann.stocks.StockRecord;

/**
 * Read data from https://iextrading.com/developer/docs/#iex-api-1-0
 * 
 * @author pschatzmann
 *
 */

public class IEXReader implements IReaderEx, Serializable {
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(IEXReader.class);
	private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

	@Override
	public int read(IStockTarget sd) {
		return read(sd, null);
	}

	@Override
	public int read(IStockTarget sd, Date date) {
		int count = 0;
		try {
			String ticker = sd.getStockID().getTicker().trim();
			ObjectMapper mapper = new ObjectMapper();
			String urlString = "https://cloud.iexapis.com/stable/stock/" + ticker + "/chart/5y?token="+Context.getPropertyMandatory("IEXAPIKey");
			LOG.debug(urlString);
			URL url = new URL(urlString);
			List<Object> values = mapper.readValue(url, List.class);
			for (Object v : values) {
				Map<String, Object> map = (Map<String, Object>) v;
				StockRecord sr = new StockRecord();
				sr.setClosing(value(map, "close"));
				sr.setDate(date(map, "date"));
				sr.setHigh(value(map, "high"));
				sr.setLow(value(map, "low"));
				sr.setOpen(value(map, "open"));
				sr.setVolume(value(map, "volume"));
				if (date == null || sr.getDate().after(date)) {
					sd.addRecord(sr);
					sr.setIndex(count);
					count++;
				}
			}
		} catch (Exception ex) {
			count = 0;
			LOG.error(ex.getMessage(), ex);
		}

		return count;
	}

	private Date date(Map<String, Object> map, String name) throws ParseException {
		return dateFormat.parse(map.get(name).toString());
	}

	private Number value(Map<String, Object> map, String name) {
		Number number = (Number) map.get(name);
		return number;
	}

}
