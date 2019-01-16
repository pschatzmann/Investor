package ch.pschatzmann.stocks.ta4j.indicator;

import java.io.Serializable;
import java.util.Date;

import org.ta4j.core.Bar;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

import ch.pschatzmann.stocks.StockID;
import ch.pschatzmann.stocks.accounting.IAccount;
import ch.pschatzmann.stocks.accounting.Portfolio;
import ch.pschatzmann.stocks.accounting.PortfolioStockInfo;

public class PortfolioAveragePriceIndicator extends CachedIndicator<Num>  implements IIndicator<Num> {
	private static final long serialVersionUID = 1L;
	private IAccount account;
	private StockID id;
	
	public PortfolioAveragePriceIndicator(TimeSeries series, IAccount account, StockID id) {
		super(series);
		this.account = account;
	}

	@Override
	protected Num calculate(int index) {
		Bar tick = this.getTimeSeries().getBar(index);
		Date date = Date.from(tick.getBeginTime().toInstant());
		Portfolio p = account.getPortfolio(date);
		if (p!=null) {
			PortfolioStockInfo pi = p.getInfo(id);
			if (pi!=null) {
				return  this.numOf(pi.getPurchasedAveragePrice());
			}
		}
		return numOf(0.0);
	}

}
