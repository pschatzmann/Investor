package ch.pschatzmann.stocks.input;

import ch.pschatzmann.stocks.IStockID;
import ch.pschatzmann.stocks.input.parser.QuandlBSEParser;

/**
 * Reader to load the stock history from Quandl Stock prices and indices from
 * the BSE (formerly the Bombay Stock Exchange).
 * 
 * 
 * @author pschatzmann
 *
 */
public class QuandlBSEReader extends QuandlReader {
	private static final long serialVersionUID = 1L;

	public QuandlBSEReader() {
		this.setInputParser(new QuandlBSEParser());
	}
	
	@Override
	protected String getExchange(IStockID id) {
		return "BSE";
	}


}
