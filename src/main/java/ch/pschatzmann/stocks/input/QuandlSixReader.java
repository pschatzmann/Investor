package ch.pschatzmann.stocks.input;

import ch.pschatzmann.stocks.IStockID;
import ch.pschatzmann.stocks.input.parser.IInputParser;
import ch.pschatzmann.stocks.input.parser.SixParser;

/**
 * Reader to load the stock history from MarketArchive files 
 * 
 * @author pschatzmann
 *
 */
public class QuandlSixReader extends QuandlReader {
	private static final long serialVersionUID = 1L;

	public QuandlSixReader() {
		this.setInputParser(new SixParser());
	}

	public QuandlSixReader(IInputParser parser) {
		this.setInputParser(parser);
	}
	
	@Override
	protected String getExchange(IStockID id) {
		return "SIX";
	}
	
}
