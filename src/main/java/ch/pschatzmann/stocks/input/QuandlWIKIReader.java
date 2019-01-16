package ch.pschatzmann.stocks.input;

import ch.pschatzmann.stocks.IStockID;
import ch.pschatzmann.stocks.input.parser.IInputParser;
import ch.pschatzmann.stocks.input.parser.QuandlWIKIParser;

/**
 * Reader to load the stock history from MarketArchive files 
 * 
 * @author pschatzmann
 *
 */
public class QuandlWIKIReader extends QuandlReader {
	private static final long serialVersionUID = 1L;

	public QuandlWIKIReader() {
		this.setInputParser(new QuandlWIKIParser());
	}

	public QuandlWIKIReader(IInputParser parser) {
		this.setInputParser(parser);
	}

	@Override
	protected String getExchange(IStockID id) {
		return "WIKI";
	}

}
