package ch.pschatzmann.stocks.input;

import java.util.Date;

import ch.pschatzmann.stocks.Context;
import ch.pschatzmann.stocks.IStockID;
import ch.pschatzmann.stocks.input.parser.CsvParser;

/**
 * Get Stock Quotes from https://unibit.ai/docs
 * https://api.unibit.ai/historicalstockprice/AAPL?range=20y&interval=1&datatype=csv&AccessKey=7TDUyJuC7-4ASR3eT3V2U2fZJu88ZVbI
 * date,open,high,low,close,adj close,volume
 *  2019-04-08,196.42,200.22,196.34,200.1,200.1,25872966
 *
 * @author pschatzmann
 *
 */

public class UnitBitReader extends HttpReader  {
	private static final long serialVersionUID = 1L;

	public UnitBitReader() {
		this.setInputParser(new CsvParser());
	}

	@Override
	protected String getPrefix(IStockID id) {
		return "https://api.unibit.ai/historicalstockprice";
	}
	
	protected String getSuffix(IStockID id) {
		return id.getTicker()+"?"+"range=20y&interval=1&datatype=csv&AccessKey="+this.getApiKey();
	}

	@Override
	protected String getSuffix(IStockID id, Date date) {
		return getSuffix(id);
	}

	protected Object getApiKey() {
		return Context.getPropertyMandatory("UnitBitAPIKey");
	}

	@Override
	protected boolean isOldToNew() {
		return false ;
	}

}
