package ch.pschatzmann.stocks.input;

import java.io.Serializable;
import java.util.Date;

import ch.pschatzmann.stocks.Context;
import ch.pschatzmann.stocks.IStockID;
import ch.pschatzmann.stocks.input.parser.CsvParser;
import ch.pschatzmann.stocks.input.parser.IInputParser;

/**
 * Reader to load the stock history from MarketArchive files 
 * 
 * @author pschatzmann
 *
 */
public class MarketArchiveHttpReader extends HttpReader implements Serializable  {
	private static final long serialVersionUID = 1L;
	
	public MarketArchiveHttpReader(boolean adjusted) {
		super();
		this.setInputParser(new CsvParser(adjusted));		
	}

	public MarketArchiveHttpReader() {
		super();
		this.setInputParser(new CsvParser(true));
	}

	public MarketArchiveHttpReader(IInputParser parser) {
		super();
		this.setInputParser(parser);
	}

	@Override
	protected String getPrefix(IStockID id) {
		return Context.getPropertyMandatory("MarketArchiveURL")+"/stocks-data/"+id.getExchange()+"/"+id.getTicker()+".csv";
	}
	
	protected String getSuffix(IStockID id) {
		return "";
	}
	
	@Override
	protected boolean isOldToNew() {
		return false ;
	}

	@Override
	protected String getSuffix(IStockID id, Date startDate) {
		return "";
	}
	

}
