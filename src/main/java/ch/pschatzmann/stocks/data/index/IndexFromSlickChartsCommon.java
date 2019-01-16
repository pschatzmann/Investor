package ch.pschatzmann.stocks.data.index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import ch.pschatzmann.stocks.StockID;
import ch.pschatzmann.stocks.errors.UniverseException;

/**
 * Shared implementation for scraping index data with the help of jsoup from
 * https://www.slickcharts.com
 * 
 * @author pschatzmann
 *
 */

public abstract class IndexFromSlickChartsCommon implements IIndex {
	private List<IndexRecord> result;

	public IndexFromSlickChartsCommon(String url) throws UniverseException {
		try {
			result = new ArrayList();
			Document doc = Jsoup.connect(url).get();
			Elements trs = doc.select("body tr");
			for (Element tr : trs) {
				Elements tds = tr.getElementsByTag("td");
				if (tds.size() > 3) {
					String symbol = tds.get(2).text();
					String weight = tds.get(3).text();
					IndexRecord rec = new IndexRecord(new StockID(symbol, ""), weight);
					result.add(rec);
				}
			}

		} catch (Exception ex) {
			throw new UniverseException(ex);
		}
	}

	@Override
	public List<IndexRecord> list() throws IOException {
		return result;
	}

	@Override
	public String toString() {
		return result.toString();
	}
}
