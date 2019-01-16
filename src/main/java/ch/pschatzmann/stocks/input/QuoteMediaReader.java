package ch.pschatzmann.stocks.input;

import java.text.SimpleDateFormat;
import java.util.Date;

import ch.pschatzmann.stocks.IStockID;
import ch.pschatzmann.stocks.input.parser.CsvParser;

/**
 * http://app.quotemedia.com/quotetools/getHistoryDownload.csv?&webmasterId=501&startDay=1&startMonth=0&startYear=2000&endDay=31&endMonth=12&endYear=2018&isRanged=true&symbol=AAPL
 * 
 * date,open,high,low,close,volume,changed,changep,adjclose,tradeval,tradevol
 * 2018-09-07,221.85,225.37,220.71,221.30,37619810,-1.80,-0.81%,221.30,8396774584.57,302907
 * 2018-09-06,226.23,227.35,221.30,223.10,34289976,-3.77,-1.66%,223.10,7665167656.21,281784
 * 2018-09-05,228.99,229.67,225.10,226.87,33332960,-1.49,-0.65%,226.87,7573536975.44,265328
 * 2018-09-04,228.41,229.18,226.63,228.36,27390132,0.73,0.32%,228.36,6247167628.48,221427
 * 2018-08-31,226.51,228.87,226.00,227.63,43340134,2.60,1.16%,227.63,9868122825.00,289588
 * 2018-08-30,223.25,228.26,222.40,225.03,48793824,2.05,0.92%,225.03,10988680810.95,364681
 * 2018-08-29,220.15,223.49,219.41,222.98,27254804,3.28,1.49%,222.98,6045248612.41,216651

 * @author pschatzmann
 *
 */

public class QuoteMediaReader extends HttpReader  {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private SimpleDateFormat dfStart = new SimpleDateFormat("'&startDay='dd'&startMonth='MM'&startYear='yyyy");
	private SimpleDateFormat dfEnd = new SimpleDateFormat("'startDay='dd'&startMonth='MM'&startYear='yyyy");
	
	public QuoteMediaReader(){
		super();
		((CsvParser) this.getInputParser()).setTargetLength(11);
	}

	@Override
	protected String getPrefix(IStockID id) {
		return "http://app.quotemedia.com/quotetools/getHistoryDownload.csv";
	}

	@Override
	protected String getSuffix(IStockID id, Date startDate) {
		String suffix = "?&webmasterId=501&%START&%END&isRanged=true&symbol=%SYMBOL";
		suffix = suffix.replace("%SYMBOL", id.getTicker());
		suffix = suffix.replace("%START", dfStart.format(this.getStartDate()));
		suffix = suffix.replace("%END", dfEnd.format(new Date()));
		return suffix;
	}

	@Override
	protected boolean isOldToNew() {
		return false;
	}

}
