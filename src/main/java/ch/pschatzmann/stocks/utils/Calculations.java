package ch.pschatzmann.stocks.utils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import ch.pschatzmann.stocks.accounting.HistoricValue;
import ch.pschatzmann.stocks.accounting.HistoricValueComparator;
import ch.pschatzmann.stocks.accounting.IHistoricValue;

/**
 * Common statistical calculations on collections of HistoricValues
 * 
 * @author pschatzmann
 *
 */

public class Calculations implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * Calculate the % increase of values between the different values 
	 * @param history
	 * @return
	 */
	public static List<IHistoricValue> getReturns(List<IHistoricValue> history) {
		return getReturns(history, 0.0);
	}

	/**
	 * Calculate the % increase of values between the different values. The result is adjusted
	 * by the indicated adjustement value (which is usually the risk free return)
	 * 
	 * @param history
	 * @param adjument
	 * @return
	 */

	public static List<IHistoricValue> getReturns(List<IHistoricValue> history, double adjument) {
		List<IHistoricValue> historyList = new ArrayList(history);
		List<IHistoricValue> result = new ArrayList();

		for (int i = 1; i < historyList.size(); i++) {
			Date date = historyList.get(i).getDate();
			double value = historyList.get(i).getValue();
			double priorValue = historyList.get(i - 1).getValue();
			if (priorValue!=0) {
				result.add(new HistoricValue(date, ((value - priorValue) / priorValue) + adjument));
			}
		}
		return result;
	}

	/**
	 * Calculates the absolute increase of values
	 * 
	 * @param history
	 * @return
	 */
	public static List<IHistoricValue> getAbsoluteReturns(List<IHistoricValue> history) {
		List<IHistoricValue> historyList = new ArrayList(history);
		Collections.sort(historyList, new HistoricValueComparator());
		List<IHistoricValue> result = new ArrayList();

		for (int i = 1; i < historyList.size(); i++) {
			Date date = historyList.get(i).getDate();
			double value = historyList.get(i).getValue();
			double priorValue = historyList.get(i - 1).getValue();
			result.add(new HistoricValue(date, (value - priorValue)));
		}
		return result;
	}

	/**
	 * Calculates the total sum of all values
	 * 
	 * @param history
	 * @return
	 */
	public static double sum(Collection<IHistoricValue> history) {
		double total = history.stream().mapToDouble(a -> a.getValue()).sum();
		return total;
	}

	/**
	 * Calculates the average of all values
	 * 
	 * @param history
	 * @return
	 */
	public static double avg(Collection<IHistoricValue> history) {
		double total = history.stream().mapToDouble(a -> a.getValue()).sum();
		return total / history.size();
	}

	/**
	 * Calculates the standard deviation of all values
	 * 
	 * @param history
	 * @return
	 */
	public static double stddevFast(Collection<IHistoricValue> history) {
		double powerSum1 = 0.0;
		double powerSum2 = 0.0;
		for (IHistoricValue h : history) {
			powerSum1 += h.getValue();
			powerSum2 += Math.pow(h.getValue(), 2.0);
		}
		double stdev = Math.sqrt(history.size() * powerSum2 - Math.pow(powerSum1, 2.0)) / history.size();
		return stdev;
	}

	/**
	 * Calculates the variance 
	 * @param history
	 * @return
	 */
	public static double variance(Collection<IHistoricValue> history) {
		double avg = avg(history);
		double temp = 0;
		for (IHistoricValue h : history) {
			double a = h.getValue();
			temp += (avg - a) * (avg - a);
		}
		return temp / history.size();
	}
	
	/**
	 * Calculates the standard deviation
	 * @param history
	 * @return
	 */
	public static double stddev(Collection<IHistoricValue> history) {
        return Math.sqrt(variance(history));
	}
	
	/**
	 * Converts a double to a long
	 * @param d
	 * @return
	 */
	
	public static Long toLong(Double d) {
		return d==null ? null : d.longValue();
	}

	/**
	 * Rounds a double to <places> digits
	 * @param value
	 * @param places
	 * @return
	 */
	public static Double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();
	    if (Double.isNaN(value)) return 0.0;
	    if (Double.isInfinite(value)) return 0.0;
	    BigDecimal bd = new BigDecimal(value);
	    bd = bd.setScale(places, RoundingMode.HALF_UP);
	    return bd.doubleValue();
	}
	
	
	
	
	
	
	public static void removeLeadingZeros(List<IHistoricValue> valueHistory) {
		if (!valueHistory.isEmpty()) {
			int j=0;
			Collection toRemove = new ArrayList();
			while (j<valueHistory.size() && valueHistory.get(j).getValue()==0 ) {
				toRemove.add(valueHistory.get(j++));
			}
			valueHistory.removeAll(toRemove);
			
		}
	}
	
	public static IHistoricValue lastValue(List<IHistoricValue> valueHistory) {
		return valueHistory==null ? null : valueHistory.get(valueHistory.size()-1);
	}

	public static Date lastDate(List<IHistoricValue> valueHistory, Date defaultDate) {
		return valueHistory==null || valueHistory.isEmpty() ? defaultDate : lastValue(valueHistory).getDate();
	}
	
}
