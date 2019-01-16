package ch.pschatzmann.stocks.execution;

import java.io.Serializable;

/**
 * Trades are executed with  n days of delay
 * 
 * @author pschatzmann
 *
 */

public class DelayInDays implements ITradingDelayModel, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int days;

	public DelayInDays(int days) {
		this.days = days;
	}

	@Override
	public long getDelayInMs() {
		return days*24*60*60*1000;
	}

}
