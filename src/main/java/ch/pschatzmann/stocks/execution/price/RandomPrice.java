package ch.pschatzmann.stocks.execution.price;

import java.io.Serializable;

import ch.pschatzmann.stocks.IStockRecord;

/**
 * Random number between min and max
 * @author pschatzmann
 *
 */
public class RandomPrice implements IPriceLogic, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public Number getPrice(IStockRecord t, boolean enter) {
		return t.getLow().doubleValue() + Math.random() * (t.getHigh().doubleValue() - t.getLow().doubleValue());
	}
}