package ch.pschatzmann.stocks.input.parser;

import java.text.ParseException;

import ch.pschatzmann.stocks.StockRecord;

public interface IInputParser {
//	public IStockID parseFileName(String filePath);
	public StockRecord parse(String line) throws ParseException;
//	public boolean ignoreFirstLine();
//	public String getPath();
//	public StockID getStockID();
	public void 	setup(String line);
	public boolean isValid(String line);

}