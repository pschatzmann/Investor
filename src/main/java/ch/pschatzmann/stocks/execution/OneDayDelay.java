package ch.pschatzmann.stocks.execution;

import java.io.Serializable;

/**
 * Trades are executed with the cloing rates of the next day
 * 
 * @author pschatzmann
 *
 */

public class OneDayDelay implements ITradingDelayModel, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public long getDelayInMs() {
		return 24*60*60*1000;
	}

}
