package ch.pschatzmann.stocks.input;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.pschatzmann.stocks.IStockID;
import ch.pschatzmann.stocks.IStockRecord;
import ch.pschatzmann.stocks.IStockTarget;
import ch.pschatzmann.stocks.input.parser.MarketArchiveParser;

/**
 * Reader to load the stock history from MarketArchive files
 * 
 * @author pschatzmann
 *
 */
public class MarketArchiveFileReader implements IReaderEx, Serializable {
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(MarketArchiveFileReader.class);
	private static String direcotry = "/var/www/stock-data";
	private Path path;
	private MarketArchiveParser parser;
	private boolean adjusted = true;

	public MarketArchiveFileReader() {
	}
	
	public MarketArchiveFileReader(boolean adjusted) {
		this.adjusted = adjusted;
	}

	public MarketArchiveFileReader(Path s) {
		this.path = s;
	}

	@Override
	public int read(IStockTarget sd) {
		return read(sd, null);
	}

	@Override
	public int read(IStockTarget sd, Date fromDate) {
		int count = 0;
		try {
			if (path == null) {
				path = getStockDataFile(sd.getStockID());
			}
			String fileName = path.toString();
			parser = this.getMarketArchiveParser();
			parser.parseFileName(fileName);
			Path path = Paths.get(fileName);
			if (path.toFile().isFile()) {
				Collection<String> lines = Files.readAllLines(path);
				if (lines != null) {
					for (String line : lines) {
						if (!line.startsWith("Date") && !line.isEmpty()) {
							try {
								IStockRecord r = parser.parse(line);
								if (r != null) {
									IStockRecord latest = sd.getValue();
									Date recordDate = r.getDate();
									if (recordDate != null && isValid(recordDate, fromDate)) {
										if (latest == null || recordDate.after(latest.getDate())) {
											sd.addRecord(r);
											count++;
										}
									}
								}
							} catch (Exception ex) {
								LOG.error(ex.getLocalizedMessage(), ex);
							}
						}
					}
					LOG.info(sd.getStockID().getTicker() + ":"  + Thread.currentThread().getName());
				}
			}
			return count;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private boolean isValid(Date recordDate, Date fromDate) {
		return fromDate == null || recordDate.after(fromDate);
	}

	public static Path getStockDataFile(IStockID id) {
		String fileName = direcotry + "/" + id.getExchange() + "/" + id.getTicker() + ".csv";
		return new File(fileName).toPath();
	}

	protected boolean isOldToNew() {
		return false;
	}
	
	public MarketArchiveParser getMarketArchiveParser() {
		if (parser==null) {
			parser = new  MarketArchiveParser(direcotry, adjusted);
		}
		return parser;
	}
	
}
