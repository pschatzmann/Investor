package ch.pschatzmann.stocks.data.universe;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import ch.pschatzmann.stocks.IStockID;
import ch.pschatzmann.stocks.StockID;
import ch.pschatzmann.stocks.errors.UniverseException;

/**
 * 
 * List of symbols traded on NASDAQ
 * 
 * http://www.nasdaq.com/screening/companies-by-name.aspx?letter=0&exchange=nasdaq&render=download
 * (replace exchange=nasdaq with exchange=nyse for nyse symbols).
 * 
 * "Symbol","Name","LastSale","MarketCap","IPOyear","Sector","industry","Summary Quote",
 * "PIH","1347 Property Insurance Holdings, Inc.","6.45","$38.6M","2014","Finance","Property-Casualty Insurers","https://www.nasdaq.com/symbol/pih",
 * "PIHPP","1347 Property Insurance Holdings, Inc.","25.45","n/a","n/a","Finance","Property-Casualty Insurers","https://www.nasdaq.com/symbol/pihpp",
 * "TURN","180 Degree Capital Corp.","2.16","$67.22M","n/a","Finance","Finance/Investors Services","https://www.nasdaq.com/symbol/turn",
 *
 * @author pschatzmann
 *
 */
public class NasdaqUniverse implements IUniverse {
	private List<IStockID> result;

	public NasdaqUniverse() throws UniverseException {
		this("NASDAQ");
	}

	public NasdaqUniverse(String exchange) throws UniverseException {
		try {
			URL url = new URL("https://www.nasdaq.com/screening/companies-by-name.aspx?letter=0&exchange="+exchange.toLowerCase()+"&render=download");
			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
			result = new ArrayList();
			String inputLine;
			boolean isData = false;
			while ((inputLine = in.readLine()) != null) {
				if (isData) {
					int end = inputLine.indexOf(",")-1;
					String symbol = inputLine.substring(1, end);
					result.add(new StockID(symbol,exchange));
				}
				isData = true;
			}
			in.close();
		} catch (Exception ex) {
			throw new UniverseException(ex);
		}
	}


	@Override
	public List<IStockID> list() {
		return result;
	}

}
