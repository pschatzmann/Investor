package ch.pschatzmann.stocks.input.parser;

import ch.pschatzmann.stocks.IStockID;
import ch.pschatzmann.stocks.StockID;

public class MarketArchiveParser extends CsvParser {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String path;
	private StockID id;

	public MarketArchiveParser(String string, boolean corrected) {
		super(corrected);
		this.path = string;
	}

	
	public String getPath() {
		return path;
	}

	public IStockID parseFileName(String filePath) {
		id = new StockID();
		String name = filePath.replaceAll(".csv", "");
		// split
		String sa[] = name.split("/");
		id.setTicker(sa[sa.length-1]);
		if (sa.length>2) {
			id.setExchange(sa[sa.length-2]);
		}
		return id;
	}
	
	public IStockID getStockID() {
		return this.id;
	}


}
