package ch.pschatzmann.stocks.input.parser;

import java.io.Serializable;
import java.text.ParseException;

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

public class QuandlBSEParser extends QuandlBaseParser implements IInputParser, Serializable {
	private static final long serialVersionUID = 1L;
	private StockID id;

	public QuandlBSEParser() {
	}
	
	
	@Override
	public StockRecord parse(String line) throws ParseException {
		StockRecord sr = new StockRecord();
		String[] sa = line.split(getSeparator());
		if (sa.length==13) {
			double adjustmentFactor = 1.0;
			sr.setAdjustmentFactor(adjustmentFactor);;

			sr.setDate(this.getDate(sa[0]));
			sr.setOpen(toDouble(sa[1],adjustmentFactor));
			sr.setHigh(toDouble(sa[2],adjustmentFactor));
			sr.setLow(toDouble(sa[3],adjustmentFactor));
			sr.setClosing(toDouble(sa[3],adjustmentFactor));
			String volume = sa[5];
			if (!Context.isEmpty(volume)) {
				sr.setVolume(Double.parseDouble(volume));
			}
		}
		return sr;
	}
	
	Double toDouble(String v, double adjustmentFactor) {
		return Context.isEmpty(v) ? null : Double.valueOf(v)*adjustmentFactor;
	}

	@Override
	public void setup(String line) {
	}
}
