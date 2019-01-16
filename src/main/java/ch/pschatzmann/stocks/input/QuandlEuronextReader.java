package ch.pschatzmann.stocks.input;

import ch.pschatzmann.stocks.IStockID;
import ch.pschatzmann.stocks.input.parser.IInputParser;
import ch.pschatzmann.stocks.input.parser.QuandlEuronextParser;

/**
 * Reader to load the stock history from MarketArchive files 
 * 
 * @author pschatzmann
 *
 */
public class QuandlEuronextReader extends QuandlReader {
	private static final long serialVersionUID = 1L;

	public QuandlEuronextReader() {
		this.setInputParser(new QuandlEuronextParser());
	}

	public QuandlEuronextReader(IInputParser parser) {
		this.setInputParser(parser);
	}

	@Override
	protected String getExchange(IStockID id) {
		return "EURONEXT";
	}

}
