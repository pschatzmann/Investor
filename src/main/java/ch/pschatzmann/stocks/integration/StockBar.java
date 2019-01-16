package ch.pschatzmann.stocks.integration;

import java.io.Serializable;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.ta4j.core.Bar;
import org.ta4j.core.num.Num;

import ch.pschatzmann.stocks.Context;
import ch.pschatzmann.stocks.IStockRecord;

/**
 * Class which implements org.ta4j.core Tick Interface
 * 
 * @author pschatzmann
 *
 */

public class StockBar implements Bar, Serializable {
	private static final long serialVersionUID = 1L;
	private int trades;
	private Num volume;
	private Num closePrice;
	private Num maxPrice;
	private Num minPrice;
	private Num openPrice;
	private Num amount;
	private Duration timePeriod;
	private ZonedDateTime beginTime;
	private ZonedDateTime endTime;

	public StockBar(ZonedDateTime time, Num open, Num high, Num low, Num closing, Num volume) {
		this.beginTime = time;
		this.endTime = time;
		this.openPrice = open;
		this.maxPrice = high;
		this.minPrice = low;
		this.closePrice = closing;
		this.volume = volume;
	}

	public StockBar(Date time, double open, double high, double low, double closing, double volume) {
		this.beginTime = time.toInstant().atZone(ZoneId.systemDefault());
		this.endTime = beginTime;
		this.openPrice = Context.number(open);
		this.maxPrice = Context.number(high);
		this.minPrice = Context.number(low);
		this.closePrice = Context.number(closing);
		this.volume = Context.number(volume);
	}

	public StockBar(IStockRecord sr) {
		this.beginTime = sr.getDate().toInstant().atZone(ZoneId.systemDefault());
		this.endTime = beginTime;
		this.openPrice = decimal(sr.getOpen());
		this.maxPrice = decimal(sr.getHigh());
		this.minPrice = decimal(sr.getLow());
		this.closePrice = decimal(sr.getClosing());
		this.volume = decimal(sr.getVolume());		
	}
	
	public StockBar(Date time) {
		this.beginTime = time.toInstant().atZone(ZoneId.systemDefault());
		this.endTime = beginTime;
		this.openPrice = Context.number(Double.NaN);
		this.maxPrice = Context.number(Double.NaN);
		this.minPrice = Context.number(Double.NaN);
		this.closePrice = Context.number(Double.NaN);
		this.volume = Context.number(Double.NaN);
	}

	public StockBar(Date time, Double value) {
		this.beginTime = time.toInstant().atZone(ZoneId.systemDefault());
		this.endTime = beginTime;
		this.openPrice = Context.number(Double.NaN);
		this.maxPrice = Context.number(Double.NaN);
		this.minPrice = Context.number(Double.NaN);
		this.closePrice = Context.number(value);
		this.volume = Context.number(Double.NaN);
	}
	

	private Num decimal(Number n) {		
		return n==null ? null : Context.number(n) ;
	}

	@Override
	public Num getOpenPrice() {
		return this.openPrice;
	}

	@Override
	public Num getMinPrice() {
		return this.minPrice;
	}

	@Override
	public Num getMaxPrice() {
		return this.maxPrice;
	}

	@Override
	public Num getClosePrice() {
		return this.closePrice;
	}

	@Override
	public Num getVolume() {
		return this.volume;
	}

	@Override
	public int getTrades() {
		return this.trades;
	}

	@Override
	public Num getAmount() {
		return this.amount;
	}

	@Override
	public Duration getTimePeriod() {
		return this.timePeriod;
	}

	@Override
	public ZonedDateTime getBeginTime() {
		return this.beginTime;
	}

	@Override
	public ZonedDateTime getEndTime() {
		return this.endTime;
	}

	@Override
	public void addTrade(Num tradeVolume, Num tradePrice) {
		throw new RuntimeException("Not implemented");
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(DateTimeFormatter.ofPattern("yyyy-MM-dd").format(this.getEndTime()));
		sb.append(":");
		sb.append(this.getClosePrice());
		return sb.toString();
	}

	@Override
	public void addPrice(Num price) {
	}

}
