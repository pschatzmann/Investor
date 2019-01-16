package ch.pschatzmann.stocks.input;

import java.util.Date;

import ch.pschatzmann.stocks.Context;
import ch.pschatzmann.stocks.IStockID;
import ch.pschatzmann.stocks.data.universe.QuandlCommonUniverse;
import ch.pschatzmann.stocks.input.parser.IInputParser;

/**
 * Reader to load the stock history from MarketArchive files 
 * 
 * @author pschatzmann
 *
 */
public class QuandlReader extends HttpReader {
	private static final long serialVersionUID = 1L;

	public QuandlReader() {
	}

	public QuandlReader(IInputParser parser) {
		this.setInputParser(parser);
	}

	@Override
	protected String getPrefix(IStockID id) {
		return "https://www.quandl.com/api/v3/datasets/"+getExchange(id)+"/"+id.getTicker()+".csv";
	}
	
	protected String getExchange(IStockID id) {
		return id.getExchange();
	}

	protected String getSuffix(IStockID id) {
		return "?api_key="+QuandlCommonUniverse.getApiKey();
	}

	@Override
	protected String getSuffix(IStockID id, Date date) {
		StringBuffer sb = new StringBuffer();
		sb.append("?api_key=");
		sb.append(QuandlCommonUniverse.getApiKey());
		if (date!=null) {
			sb.append("&start_date=");
			sb.append(Context.format(date));
		}
		return sb.toString();
	}

	@Override
	protected boolean isOldToNew() {
		return false ;
	}
		
}
