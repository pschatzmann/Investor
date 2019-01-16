package ch.pschatzmann.stocks.input.parser;

import java.io.Serializable;
import java.text.ParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.pschatzmann.stocks.Context;
import ch.pschatzmann.stocks.StockID;
import ch.pschatzmann.stocks.StockRecord;

public class QuandlWIKIParser extends QuandlBaseParser implements IInputParser, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(QuandlWIKIParser.class);
	private StockID id = new StockID();
	private String path;

	public QuandlWIKIParser() {
	}

	public QuandlWIKIParser(String path) {
		this.path = path;
	}

	@Override
	public StockRecord parse(String line) throws ParseException {
		StockRecord sr = new StockRecord();
		if (!line.isEmpty()) {
			String[] sa = line.split(getSeparator());
			// 0      1     2       3     4       5       6              7             8           9           10         11           12          
			// Date","Open","High","Low","Close","Volume","Ex-Dividend","Split Ratio","Adj. Open","Adj. High","Adj. Low","Adj. Close","Adj. Volume"
			if (sa.length==13) {
				if (Context.isEmpty(sa[1])) {
					return null;
				}

				double adjustmentFactor = 1.0;
				try {
					adjustmentFactor = Double.valueOf(Double.valueOf(sa[11])) / Double.valueOf(Double.valueOf(sa[4]));
					sr.setAdjustmentFactor(adjustmentFactor);
				} catch(Exception ex) {
					LOG.info("Could not determine factor for "+line,ex);				
				}
				
				try {
					sr.setDate(this.getDate(sa[0]));
					if (!sa[0].isEmpty()) {
						sr.setOpen(Double.valueOf(sa[8]));
					}
					if (!sa[9].isEmpty()) {
						sr.setHigh(Double.valueOf(sa[9]));
					}
					if (!sa[10].isEmpty()) {
						sr.setLow(Double.valueOf(sa[10]));
					}
					if (!sa[12].isEmpty()) {
						sr.setVolume(Double.parseDouble(sa[12]));
					}
					if (!sa[11].isEmpty()) {
						sr.setClosing(Double.valueOf(sa[11]));
					}
				} catch(Exception ex) {
					sr = null;
					LOG.info("Could not determine data for "+line,ex);				
				}
			}
		}
		return sr;
	}
	
	@Override
	public void setup(String line) {
	}

}
