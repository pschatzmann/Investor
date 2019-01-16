package ch.pschatzmann.stocks.input;

import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import ch.pschatzmann.stocks.Context;
import ch.pschatzmann.stocks.IStockID;
import ch.pschatzmann.stocks.IStockTarget;
import ch.pschatzmann.stocks.StockRecord;

/**
 * https://www.investopedia.com/markets/api/partial/historical/?Symbol=AAPL&Type=Historical+Prices&Timeframe=Daily&StartDate=Nov+28%2C+2017&EndDate=Dec+05%2C+2017
 *
 * <div class="page">
 * 
 * 
 * 
 * <table class="data">
 * <tbody data-rows="6" data-start-date="Nov 28, 2017">
 * <tr class="header-row">
 * <th class="date">Date</th>
 * <th class="num">Open</th>
 * <th class="num">High</th>
 * <th class="num">Low</th>
 * <th class="num">Adj. Close</th>
 * <th class="num">Volume</th>
 * </tr>
 * 
 * <tr class="in-the-money">
 * <td class="date">Dec 05, 2017</td>
 * <td class="num">167.14</td>
 * <td class="num">169.57</td>
 * <td class="num">166.49</td>
 * <td class="num">167.71</td>
 * <td class="num">27,350,154</td>
 * </tr>
 * <tr class="in-the-money">
 * <td class="date">Dec 04, 2017</td>
 * <td class="num">170.52</td>
 * <td class="num">170.66</td>
 * <td class="num">167.70</td>
 * <td class="num">167.87</td>
 * <td class="num">32,542,385</td>
 * </tr>
 * 
 * @author pschatzmann
 *
 */
public class InvestopiaReader implements IReaderEx {
	private static final Logger LOG = LoggerFactory.getLogger(InvestopiaReader.class);
	private DateFormat df = new SimpleDateFormat("MMM'+'dd'%2C+'yyyy");
	private boolean isHeader = true;
	private int recordCount=0;

	public InvestopiaReader() {
	}

	protected String getURL(IStockID id, Date startDate) {
		String parameters = "https://www.investopedia.com/markets/api/partial/historical/?Symbol=%SYMBOL&Type=Historical+Prices&Timeframe=Daily&StartDate=%START&EndDate=%END";
		parameters = parameters.replace("%SYMBOL", id.getTicker());
		parameters = parameters.replace("%END", df.format(new Date()));
		parameters = parameters.replace("%START", df.format(startDate));
		return parameters;
	}

	@Override
	public int read(IStockTarget sd) {
		return read(sd, Context.getDefaultStartDate());
	}

	@Override
	public int read(IStockTarget sd, Date startDate) {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			InputStream is = new URL(getURL(sd.getStockID(), startDate)).openStream();
			saxParser.parse(is, new UserHandler(sd));
			return recordCount;
		} catch (Exception ex) {
			LOG.error(ex.getMessage(), ex);
			return 0;
		}
	}

	protected Double getDouble(String val) {
		return Double.valueOf(val);
	}

	/**
	 * Parser
	 * 
	 * @author pschatzmann
	 *
	 */
	class UserHandler extends DefaultHandler {
		private DateFormat df = new SimpleDateFormat("MMM dd,yyyy");
		private int j = -1;
		private IStockTarget sd;
		private StockRecord rec;
		private StringBuffer sb = new StringBuffer();

		UserHandler(IStockTarget sd) {
			this.sd = sd;
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
			if (qName.equalsIgnoreCase("tr")) {
				j = -1;
				rec = new StockRecord();
			} else if (qName.equalsIgnoreCase("td")) {
				j++;
				sb.setLength(0);
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if (qName.equalsIgnoreCase("tr")) {
				if (!isHeader && rec.getOpen()!=null) {
					sd.addRecord(rec);
					recordCount++;
				}  
				isHeader = false;
				j = -1;
			} if (qName.equalsIgnoreCase("td")) {
				String value = sb.toString().trim();
				try {
					if (!isHeader && j >=0 && !value.isEmpty()) {
						switch (j) {
						case 0:
							rec.setDate(df.parse(value));
							break;
						case 1:
							if (!value.endsWith("Dividend") && !value.endsWith("Split")) {
								rec.setOpen(getDouble(value));
							}
							break;
						case 2:
							rec.setHigh(getDouble(value));
							break;
						case 3:
							rec.setLow(getDouble(value));
							break;
						case 4:
							rec.setClosing(getDouble(value));
							break;
						case 5:
							rec.setVolume(getDouble(value.replaceAll(",", "")));
							break;
						}
					}
				} catch (Exception ex) {
					LOG.error(ex.getMessage()+"("+value+")",ex);;
				}
				
			}
		}

		@Override
		public void characters(char ch[], int start, int length) throws SAXException {
			sb.append(new String(ch, start, length));
		}

	}

}
