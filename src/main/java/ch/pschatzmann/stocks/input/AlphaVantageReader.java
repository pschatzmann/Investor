package ch.pschatzmann.stocks.input;

import java.util.Date;

import ch.pschatzmann.stocks.Context;
import ch.pschatzmann.stocks.IStockID;
import ch.pschatzmann.stocks.input.parser.AlphaVantageParser;

/**
 * https://www.alphavantage.co/documentation/#dailyadj
 * 
 * example: https://www.alphavantage.co/query?function=TIME_SERIES_DAILY_ADJUSTED&symbol=MSFT&datatype=csv&apikey=demo
 * timestamp,open,high,low,close,adjusted_close,volume,dividend_amount,split_coefficient
 * 2018-05-22,97.6800,98.1700,97.2000,97.5950,97.5950,9711773,0.0000,1.0000
 * 2018-05-21,97.0000,98.0100,96.8000,97.6000,97.6000,18996380,0.0000,1.0000
 * 
 * @author pschatzmann
 *
 */

public class AlphaVantageReader extends HttpReader  {
	private static final long serialVersionUID = 1L;

	public AlphaVantageReader() {
		this.setInputParser(new AlphaVantageParser());
	}


	@Override
	protected String getPrefix(IStockID id) {
		return "https://www.alphavantage.co/query";
	}
	
	protected String getSuffix(IStockID id) {
		return "?datatype=csv&outputsize=full&function=TIME_SERIES_DAILY_ADJUSTED&symbol="+id.getTicker()+"&apikey="+this.getApiKey();
	}

	@Override
	protected String getSuffix(IStockID id, Date date) {
		return getSuffix(id);
	}

	protected Object getApiKey() {
		return Context.getProperty("AlphaVantageAPIKey", "FVR0D4P12YX49UAU");
	}

	@Override
	protected boolean isOldToNew() {
		return false ;
	}
	

}
