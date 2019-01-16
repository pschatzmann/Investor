package ch.pschatzmann.stocks.input;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.pschatzmann.stocks.Context;
import ch.pschatzmann.stocks.IStockID;
import ch.pschatzmann.stocks.IStockRecord;
import ch.pschatzmann.stocks.IStockTarget;
import ch.pschatzmann.stocks.StockRecord;
import ch.pschatzmann.stocks.input.parser.CsvParser;
import ch.pschatzmann.stocks.input.parser.IInputParser;

/**
 * Reader to load the stock history via HTTP from a url 
 * 
 * @author pschatzmann
 *
 */
abstract public class HttpReader implements IReaderEx, Serializable  {
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(HttpReader.class);
	private IInputParser parser = new CsvParser();
	private Date startDate = Context.getDefaultStartDate();
	
	public HttpReader() {
	}

	@Override
	public int read(IStockTarget sd)  {
		return read(sd, startDate);
	}

	@Override
	public int read(IStockTarget sd, Date startDate)  {
		this.startDate = startDate;
		InputStream is=null;
		int count = 0;
		try {
			if (sd!=null && sd.getStockID()!=null) {
				String urlString = getPrefix(sd.getStockID())+getSuffix(sd.getStockID(), startDate);
				is =  new URL(urlString).openStream();
				if (is!=null) {
					List<String> lines = IOUtils.readLines(new BufferedReader(new InputStreamReader(is)));

					if (lines != null && !lines.isEmpty()) {
						LOG.info("Reading "+urlString+": Number of lines: "+lines.size());
						
						String header = lines.get(0);
						while (!parser.isValid(header) && !lines.isEmpty()) {
							parser.setup(header);
							lines.remove(0);
							if (!lines.isEmpty()) {
								header = lines.get(0);
							}
						}
						
						if (!isOldToNew()) {
							Collections.reverse(lines);
							LOG.info("lines are reordered");
						}
						
						for (String line : lines) {
							if (parser.isValid(line)) {
								try {
									StockRecord r = parser.parse(line);
									if (r != null && r.isValid()) {
										IStockRecord latest = sd.getValue();
										Date recordDate = r.getDate();
										if (recordDate != null && isValidDate(startDate, recordDate)) {
											if (latest == null || recordDate.after(latest.getDate())) {
												sd.addRecord(r);
												r.setIndex(count);
												count++;
											}
										}
									}
								} catch (Exception ex) {
									LOG.error(ex.getMessage(), ex);
								}
							} 
						}
						LOG.debug(sd.getStockID().getTicker() + Thread.currentThread().getName());
					}
				} else {
					LOG.info(urlString+"- not found");	
				}
			}
		} catch (Exception ex) {
			LOG.error(ex.getMessage(),ex);
			count = 0;
		} finally {
			if (is!=null)
				try {
					is.close();
				} catch (IOException e) {
				}
		}
		return count;
	}


	private boolean isValidDate(Date startDate, Date recordDate) {
		boolean valid = true;
		if (startDate !=null && recordDate.before(startDate)) {
			valid = false;
		}
		return valid;
	}

	public IInputParser getInputParser() {
		return parser;
	}


	public void setInputParser(IInputParser inputParser) {
		this.parser = inputParser;
	}

	abstract protected String getPrefix(IStockID id);
	
	abstract protected String getSuffix(IStockID id, Date startDate);
		
	abstract protected boolean isOldToNew();
	
	public Date getStartDate() {
		return this.startDate;
	}
	
}
