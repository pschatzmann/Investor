package ch.pschatzmann.stocks.input.parser;

import java.io.Serializable;
import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import ch.pschatzmann.stocks.Context;
import ch.pschatzmann.stocks.StockID;
import ch.pschatzmann.stocks.StockRecord;

/**
 * Parses lines which contain the following structure:
 * 
 * Date,	Open,	High,	Low,	Close,	Volume,	Adj Close
 * 
 * @author pschatzmann
 *
 */

public class SixParser implements IInputParser, Serializable {
	private static final long serialVersionUID = 1L;
	private DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
	private String path;
	private StockID id;

	public SixParser() {
	}
	
	public SixParser(String string) {
		this.path = string;
	}
	
	
	@Override
	public StockRecord parse(String line) throws ParseException {
		StockRecord sr = new StockRecord();
		String[] sa = line.split(getSeparator());
		if (sa.length==3) {
			sr.setDate(this.getDate(sa[0]));
			sr.setOpen(toDouble(sa[1],1.0));
			sr.setHigh(toDouble(sa[1],1.0));
			sr.setLow(toDouble(sa[1],1.0));
			sr.setClosing(toDouble(sa[1],1.0));
			String volume = sa[2];
			if (!Context.isEmpty(volume)) {
				sr.setVolume(Double.parseDouble(volume));
			}
		}
		return sr;
	}
	
	Double toDouble(String v, double adjustmentFactor) {
		return Context.isEmpty(v) ? null : Double.valueOf(v)*adjustmentFactor;
	}
	

	public String getSeparator() {
		return ",";
	}

	public synchronized Date getDate(String date) throws ParseException {
		return new Date(df.parse(date).getTime());
	}

	public synchronized String getString(Date date) {
		return df.format(date);
	}

	@Override
	public void setup(String line) {
	}

	@Override
	public boolean isValid(String line) {
		return !line.startsWith("Date") && !line.isEmpty();
	}


}
