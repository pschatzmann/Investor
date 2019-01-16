package ch.pschatzmann.stocks.input;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import ch.pschatzmann.stocks.IStockID;
import ch.pschatzmann.stocks.input.parser.CsvParser;

/**
 * http://quotes.wsj.com/AAPL/historical-prices/download?MOD_VIEW=page&num_rows=6299.041666666667&range_days=6299.041666666667&startDate=09/06/2000&endDate=12/05/2017
 * 
 * Date, Open, High, Low, Close, Volume
 * 12/05/17, 169.06, 171.52, 168.40, 169.64, 27350154
 * 12/04/17, 172.48, 172.62, 169.63, 169.80, 32542385
 * 12/01/17, 169.95, 171.67, 168.50, 171.05, 39759288
 *
 * @author pschatzmann
 *
 */
public class WallstreetJournalReader extends HttpReader {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
	
	public WallstreetJournalReader(){
		((CsvParser) this.getInputParser()).setDateFormat(new SimpleDateFormat("MM/dd/yy"));
		((CsvParser) this.getInputParser()).setTargetLength(6);
	}

	@Override
	protected String getPrefix(IStockID id) {
		return "https://quotes.wsj.com/"+id.getTicker()+"/historical-prices/download";
	}

	@Override
	protected String getSuffix(IStockID id, Date startDate) {
		String parameters = "?MOD_VIEW=page&num_rows=6299.041666666667&range_days=6299.041666666667&startDate=%START&endDate=%END";
		parameters = parameters.replace("%END", df.format(new Date()));
		parameters = parameters.replace("%START", df.format(this.getStartDate()));
		return parameters;
	}

	@Override
	protected boolean isOldToNew() {
		return false;
	}
}
