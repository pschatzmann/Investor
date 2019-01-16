package ch.pschatzmann.stocks.input;

import java.util.Date;

import ch.pschatzmann.stocks.IStockID;
import ch.pschatzmann.stocks.input.parser.CsvParser;

/**
 * http://download.macrotrends.net/assets/php/stock_data_export.php?t=AAPL

"MacroTrends Data Download"
"AAPL - Historical Price and Volume Data"
"Historical prices are adjusted for both splits and dividends"

"Disclaimer and Terms of Use: Historical stock data is provided 'as is' and solely for informational purposes, not for trading purposes or advice."
"MacroTrends LLC expressly disclaims the accuracy, adequacy, or completeness of any data and shall not be liable for any errors, omissions or other defects in, "
"delays or interruptions in such data, or for any actions taken in reliance thereon.  Neither MacroTrends LLC nor any of our information providers will be liable"
"for any damages relating to your use of the data provided."


date,open,high,low,close,volume
1980-12-12,0.4164,0.4182,0.4164,0.4164,117258400
1980-12-15,0.3966,0.3966,0.3947,0.3947,43971200
1980-12-16,0.3675,0.3675,0.3657,0.3657,26432000


 * @author pschatzmann
 *
 */
public class MacroTrendsReader extends HttpReader {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MacroTrendsReader() {	
		((CsvParser)this.getInputParser()).setTargetLength(6);

	}
	
	@Override
	protected String getPrefix(IStockID id) {
		return "http://download.macrotrends.net/assets/php/stock_data_export.php";
	}

	@Override
	protected String getSuffix(IStockID id, Date startDate) {
		String parameters = "?t=%SYMBOL";
		parameters = parameters.replace("%SYMBOL", id.getTicker());
		return parameters;
	}

	@Override
	protected boolean isOldToNew() {
		return true;
	}

}
