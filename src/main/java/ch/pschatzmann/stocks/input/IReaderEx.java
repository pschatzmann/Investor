package ch.pschatzmann.stocks.input;

import java.util.Date;

import ch.pschatzmann.stocks.IStockTarget;

public interface IReaderEx extends IReader {
	public int read(IStockTarget sd, Date startDate);
}