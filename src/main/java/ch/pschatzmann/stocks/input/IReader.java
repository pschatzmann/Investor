package ch.pschatzmann.stocks.input;

import ch.pschatzmann.stocks.IStockTarget;

public interface IReader {

	public int read(IStockTarget sd);
}