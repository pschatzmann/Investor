package ch.pschatzmann.stocks.input.parser;

import java.io.Serializable;
import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import ch.pschatzmann.stocks.StockRecord;

public class SolrParser implements IInputParser, Serializable  {
	private static final long serialVersionUID = 1L;
	private DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

	//  "date,volume,open,closing,low,high",

	@Override
	public StockRecord parse(String line) throws ParseException {
		StockRecord sr = new StockRecord();
		String[] sa = line.split(getSeparator());
		if (sa.length==6) {

			sr.setDate(this.getDate(sa[0]));
			sr.setOpen(Double.valueOf(sa[2]));
			sr.setHigh(Double.valueOf(sa[4]));
			sr.setLow(Double.valueOf(sa[3]));
			sr.setClosing(Double.valueOf(sa[3]));
			sr.setVolume(Long.parseLong(sa[1]));
		}
		return sr;
	}
	
	public String getSeparator() {
		return ",";
	}

	public synchronized Date getDate(String date) throws ParseException {
		return new Date(df.parse(date).getTime());
	}

	@Override
	public void setup(String line) {
	}

	@Override
	public boolean isValid(String line) {
		return !line.startsWith("Date") && !line.isEmpty();
	}

}
