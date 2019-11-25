package ch.pschatzmann.stocks.input;

import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.pschatzmann.stocks.Context;
import ch.pschatzmann.stocks.IStockID;
import ch.pschatzmann.stocks.IStockTarget;
import ch.pschatzmann.stocks.StockRecord;

/**
 * https://api.tiingo.com/tiingo/daily/AAPL/prices?startDate=2010-1-1&endDate=2018-12-31
 * 
 * @author pschatzmann
 *
 */
public class TiingoReader implements IReaderEx {
	private static final Logger LOG = LoggerFactory.getLogger(TiingoReader.class);
	private DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
	private String token = Context.getPropertyMandatory("TiingoToken");
	private ObjectMapper objectMapper = new ObjectMapper();

	public TiingoReader() {
	}

	protected String getURL(IStockID id, Date startDate) {
		String parameters = "https://api.tiingo.com/tiingo/daily/%SYMBOL/prices?startDate=%START&endDate=%END";
		parameters = parameters.replace("%SYMBOL", id.getTicker());
		parameters = parameters.replace("%END", df.format(new Date()));
		parameters = parameters.replace("%START", df.format(startDate));
		return parameters;
	}

	@Override
	public int read(IStockTarget sd) {
		return read(sd, Context.getDefaultStartDate());
	}

	@Override
	public int read(IStockTarget sd, Date startDate) {
		try {
			URL myURL = new URL(this.getURL(sd.getStockID(), startDate));
			HttpURLConnection con = (HttpURLConnection) myURL.openConnection();
			con.setRequestProperty("Authorization", "Token "+token);
			con.setRequestProperty("Content-Type", "application/json "+token);
			con.setUseCaches(true);
			Rec[] records = objectMapper.readValue(con.getInputStream(), Rec[].class);
			int pos = 0;
			for (Rec rec : records) {
				double factor = rec.adjClose / rec.close;
				StockRecord stockRecord = new StockRecord();
				stockRecord.setDate(rec.date);
				stockRecord.setOpen(rec.open * factor);
				stockRecord.setClosing(rec.close * factor);
				stockRecord.setHigh(rec.high * factor);
				stockRecord.setLow(rec.low * factor);
				stockRecord.setVolume(rec.volume);
				stockRecord.setAdjustmentFactor(factor);
				stockRecord.setIndex(pos++);
				sd.addRecord(stockRecord);
			}
			return records.length;
		} catch (Exception ex) {
			LOG.error(ex.getMessage(),ex);
			return 0;
		}

	}

	public static class Rec {
		public Date date;
		public double close;
		public double high;
		public double low;
		public double open;
		public double volume;
		public double adjClose;
		public double adjHigh;
		public double adjLow;
		public double adjOpen;
		public double adjVolume;
		public double divCash;
		public double splitFactor;

		public Rec() {	
		}
		
	}

}
