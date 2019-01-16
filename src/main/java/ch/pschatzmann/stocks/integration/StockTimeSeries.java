package ch.pschatzmann.stocks.integration;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.ta4j.core.Bar;
import org.ta4j.core.BaseTimeSeries;
import org.ta4j.core.TimeSeries;

import ch.pschatzmann.dates.DateRange;
import ch.pschatzmann.stocks.Context;
import ch.pschatzmann.stocks.IStockData;
import ch.pschatzmann.stocks.IStockRecord;

/**
 * Class which implements org.ta4j.core TimeSeries
 * 
 * @author pschatzmann
 *
 */

public class StockTimeSeries extends BaseTimeSeries implements TimeSeries, Serializable {
	private static final long serialVersionUID = 1L;

	public StockTimeSeries(String ticker, List<Bar> ticks) {
		super(ticker, ticks, Context.getNumberImplementation());
	}

	public StockTimeSeries(List<Bar> ticks) {
		super("StockTimeSeries",ticks,Context.getNumberImplementation());
	}

	public StockTimeSeries(String ticker) {
		super(ticker,new ArrayList(),Context.getNumberImplementation());
	}

	public StockTimeSeries(IStockData stockData, DateRange dateRange) {
		super(stockData.getStockID().toString(),new ArrayList(),Context.getNumberImplementation());
		for (IStockRecord sr :stockData.getHistory()) {
			if (sr.isValid() && dateRange.isValid(sr.getDate())) {
				addBar(new StockBar(sr));
			}
		}
	}

	public StockTimeSeries(IStockData stockData) {
		super(stockData.getStockID().toString(),new ArrayList(),Context.getNumberImplementation());
		for (IStockRecord sr :stockData.getHistory()) {
			if (sr.isValid()) {
				addBar(new StockBar(sr));
			}
		}
	}
	
	public StockTimeSeries(Collection<IStockRecord> stockData)  {
		super("StockTimeSeries",new ArrayList(),Context.getNumberImplementation());
		for (IStockRecord sr : stockData) {
			if (sr.isValid()) {
				this.addBar(new StockBar(sr));
			}
		}
	}
	
    @Override
    public void addBar(Bar bar, boolean replace) {
    	List<Bar> bars = this.getBarData();
    	if (!bars.isEmpty()) {
	        final int lastBarIndex = bars.size() - 1;
	        ZonedDateTime seriesEndTime = bars.get(lastBarIndex).getEndTime();
	        if (bar.getEndTime().equals(seriesEndTime)) {
	        	super.addBar(bar, true);        	
	        } else {
	        	super.addBar(bar, replace);
	        }
    	} else {
        	super.addBar(bar, replace);    		
    	}
    }

	
	@Override
	public String toString() {
		return this.getName();
	}


}
