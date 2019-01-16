package ch.pschatzmann.stocks.utils;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import ch.pschatzmann.stocks.Context;
import ch.pschatzmann.stocks.IStockData;
import ch.pschatzmann.stocks.IStockRecord;

/**
 * Writes the Stock records to a CSV file
 * 
 * @author pschatzmann
 *
 */
public class CSVWriter {
	private String del = ",";
	
	public CSVWriter() {}
	
	public CSVWriter(String delim){
		this.del = delim;
	}

	public void write(IStockData sd, OutputStream os) throws IOException {
		BufferedOutputStream bos = new BufferedOutputStream(os);
		writeLine(bos,"Date, Open, High, Low, Close, Volume");

		sd.getHistory().forEach(rec -> writeRecord(rec, bos));
		bos.flush();
	} 

	private void writeRecord(IStockRecord rec, BufferedOutputStream bos)  {
		StringBuffer sb = new StringBuffer();
		sb.append(Context.format(rec.getDate()));
		sb.append(del);
		sb.append(str(rec.getOpen()));
		sb.append(del);
		sb.append(str(rec.getHigh()));
		sb.append(del);
		sb.append(str(rec.getLow()));
		sb.append(del);
		sb.append(str(rec.getClosing()));
		sb.append(del);
		sb.append(str(rec.getVolume()));
		
		writeLine(bos, sb.toString());
	}
	
	private String str(Number num) {
		return num==null ? "":num.toString();
	}
	
	private void writeLine(BufferedOutputStream bos, String line) {
		try {
			bos.write((line+System.lineSeparator()).getBytes());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
}
