package ch.pschatzmann.stocks.input;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import ch.pschatzmann.stocks.IStockID;
import ch.pschatzmann.stocks.input.parser.CsvParser;

/**
 * 
 * http://markets.financialcontent.com/stocks/action/gethistoricaldata?Month=12&Symbol=AAPL&Range=300&Year=2018
 * 
 * Symbol,Date,Open,High,Low,Close,Volume,Change,% Change
 * AAPL,09/07/18,221.85,225.37,220.71,221.30,37619800,-1.80,-0.81%
 * AAPL,09/06/18,226.23,227.35,221.30,223.10,34273178,-3.77,-1.66%
 * AAPL,09/05/18,228.99,229.67,225.10,226.87,33319740,-1.49,-0.65%
 * AAPL,09/04/18,228.41,229.18,226.63,228.36,27382263,0.73,0.32%

 * @author pschatzmann
 *
 */

public class FinancialContentReader extends HttpReader {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private DateFormat df = new SimpleDateFormat("yyyy");
	
	public FinancialContentReader() {
		((CsvParser)this.getInputParser()).setDateFormat(new SimpleDateFormat("MM/dd/yy"));
		((CsvParser)this.getInputParser()).setTargetLength(9);
	}
	
	@Override
	protected boolean isOldToNew() {
		return false;
	}

	@Override
	protected String getPrefix(IStockID id) {
		return "http://markets.financialcontent.com/stocks/action/gethistoricaldata";
	}

	@Override
	protected String getSuffix(IStockID id, Date startDate) {
		String parameters = "?Month=12&Symbol=%SYMBOL&Range=300&Year=%YEAR";
		parameters = parameters.replace("%SYMBOL", id.getTicker());
		parameters = parameters.replace("%YEAR", df.format(new Date()));
		return parameters;
	}

}
