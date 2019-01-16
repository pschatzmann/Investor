package ch.pschatzmann.stocks.execution;

import java.io.Serializable;

public class NoDelay implements ITradingDelayModel, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public long getDelayInMs() {
		return 0;
	}

}
