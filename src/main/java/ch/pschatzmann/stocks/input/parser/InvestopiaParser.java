package ch.pschatzmann.stocks.input.parser;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import ch.pschatzmann.stocks.Context;
import ch.pschatzmann.stocks.StockRecord;

/*
*	Date			Open	High	Low		Adj. Close	Volume
*	Dec 05, 2017	167.14	169.57	166.49	167.71	27,350,154
*	Dec 04, 2017	170.52	170.66	167.70	167.87	32,542,385
*	Dec 01, 2017	168.02	169.72	166.59	169.11	39,759,288
*	Nov 30, 2017	168.50	170.19	166.53	169.90	41,527,218
*	Nov 29, 2017	170.67	170.96	165.26	167.56	41,666,364
*	Nov 28, 2017	172.32	172.88	169.91	171.11	26,428,802
*/

public class InvestopiaParser implements IInputParser {
	SimpleDateFormat df = new SimpleDateFormat("MMM dd, yyyy");

	@Override
	public StockRecord parse(String line) throws ParseException {
		String sa[] = line.split("\t");
		StockRecord sr = new StockRecord();
		sr.setDate(df.parse(sa[0]));
		sr.setOpen(toDouble(sa[1]));
		sr.setHigh(toDouble(sa[2]));
		sr.setLow(toDouble(sa[3]));
		sr.setClosing(toDouble(sa[4]));
		sr.setVolume(toDouble(sa[5].replaceAll(",", "")));
		return sr;
	}
	
	protected Double toDouble(String v) {
		return Context.isEmpty(v) || v.equals("null") ? null : Double.valueOf(v);
	}

	@Override
	public void setup(String line) {
	}

	@Override
	public boolean isValid(String line) {
		return !line.startsWith("Date") && !line.trim().isEmpty();
	}

}
