package ch.pschatzmann.stocks.execution;

/**
 * Trades are usually take some time to get executed.
 *  We can model some delays
 * @author pschatzmann
 *
 */
public interface ITradingDelayModel {
	public long getDelayInMs();
}
