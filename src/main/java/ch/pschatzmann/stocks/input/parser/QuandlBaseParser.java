package ch.pschatzmann.stocks.input.parser;

import java.io.Serializable;
import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public abstract class QuandlBaseParser implements IInputParser, Serializable  {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

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
	public boolean isValid(String line) {
		return !line.startsWith("Date") && !line.isEmpty();
	}

}
