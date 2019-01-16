package ch.pschatzmann.stocks.execution.price;

import java.io.Serializable;

import ch.pschatzmann.stocks.IStockRecord;

/**
 * Avarage between min and max price
 * @author pschatzmann
 *
 */
public class AvaragePrice implements IPriceLogic, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public Number getPrice(IStockRecord t, boolean enter) {
		return t.getLow().doubleValue()+t.getHigh().doubleValue() / 2.0;
	}
}
