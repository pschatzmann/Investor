package ch.pschatzmann.stocks.accounting;

import java.util.Date;

/**
 * Interface for Value at Date
 * @author pschatzmann
 *
 */
public interface IHistoricValue {

	Date getDate();

	Double getValue();

}