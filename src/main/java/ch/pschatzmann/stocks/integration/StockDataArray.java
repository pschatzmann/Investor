package ch.pschatzmann.stocks.integration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.ta4j.core.Bar;
import org.ta4j.core.TimeSeries;

import ch.pschatzmann.stocks.Context;
import ch.pschatzmann.stocks.IStockRecord;
import ch.pschatzmann.stocks.StockData;

/**
 * Convert StockData and ta4j TimeSeries to arrays 
 * 
 * @author pschatzmann
 *
 */

public class StockDataArray implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private double[][] values;
	private Date[] dates;
	private String[] datesFormatted;
	private String name;

	public enum FieldName {
		Open, Closing, High, Low, Volume
	};

	public StockDataArray(StockData data) {
		this(data.getHistory(), true);
		name = data.getStockID().toString();
	}

	public StockDataArray(List<IStockRecord> data) {
		this(data, true);
	}

	public StockDataArray(List<IStockRecord> data, boolean adjusted) {
		values = new double[5][data.size()];
		dates = new Date[data.size()];
		datesFormatted = new String[data.size()];
		for (int j = 0; j < data.size(); j++) {
			IStockRecord rec = data.get(j);
			values[FieldName.Open.ordinal()][j] = rec.getOpen().doubleValue();
			values[FieldName.Closing.ordinal()][j] = rec.getClosing().doubleValue();
			values[FieldName.Low.ordinal()][j] = rec.getLow().doubleValue();
			values[FieldName.High.ordinal()][j] = rec.getHigh().doubleValue();
			values[FieldName.Volume.ordinal()][j] = rec.getVolume().doubleValue();
			if (adjusted) {
				for (int i = 0; i < 5; i++) {
					values[i][j] *= rec.getAdjustmentFactor().doubleValue();
				}
			}
			dates[j] = rec.getDate();
			datesFormatted[j] = Context.format(rec.getDate());
		}
	}

	public StockDataArray(TimeSeries series) {
		int size = series.getBarCount();
		dates = new Date[size];
		datesFormatted = new String[size];
		for (int j = 0; j < size; j++) {
			Bar rec = series.getBar(j);
			values[FieldName.Open.ordinal()][j] = rec.getOpenPrice().doubleValue();
			values[FieldName.Closing.ordinal()][j] = rec.getClosePrice().doubleValue();
			values[FieldName.Low.ordinal()][j] = rec.getMinPrice().doubleValue();
			values[FieldName.High.ordinal()][j] = rec.getMaxPrice().doubleValue();
			values[FieldName.Volume.ordinal()][j] = rec.getVolume().doubleValue();
			dates[j] = Date.from(rec.getEndTime().toInstant());
			datesFormatted[j] = Context.format(dates[j]);
		}
		values = new double[5][series.getBarCount()];
		dates = new Date[size];
	}

	public double[] getFieldVaues(FieldName fieldName) {
		return values[fieldName.ordinal()];
	}

	

	public double[][] getValues() {
		return this.values;
	}

	public Date[] getDates() {
		return dates;
	}

	public String[] getDatesFormatted() {
		return datesFormatted;
	}
	
	public List<Double> getValueList(FieldName fieldName){
		List<Double> result = new ArrayList();
		for (double v : getValues()[fieldName.ordinal()]) {
			result.add(v);
		}
		return result;
	}
	
	public List<String> getDatesList() {
		return Arrays.asList(getDatesFormatted());
	}
	


}
