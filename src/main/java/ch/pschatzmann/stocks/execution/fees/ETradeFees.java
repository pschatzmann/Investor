package ch.pschatzmann.stocks.execution.fees;

import java.io.Serializable;

/**
 * E-Trade Fees in US
 * 
 * @author pschatzmann
 *
 */
public class ETradeFees extends PerTradeFees implements Serializable {
	private static final long serialVersionUID = 1L;

	public ETradeFees() {
		super(7.99);
	}

}
