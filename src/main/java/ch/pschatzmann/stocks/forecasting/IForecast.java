package ch.pschatzmann.stocks.forecasting;

import java.util.Date;

import ch.pschatzmann.stocks.integration.HistoricValues;
import ch.pschatzmann.stocks.ta4j.indicator.Name;

public interface IForecast extends Name {

	/**
	 * Calculate the forecast for the indicated number of periods
	 * 
	 * @param numberOfForecasts
	 * @return
	 * @throws Exception
	 */
	HistoricValues forecast(int numberOfForecasts) throws Exception;

	/**
	 * Calculate the forecast up to the end date
	 * @param endDate
	 * @return
	 * @throws Exception
	 */
	HistoricValues forecast(Date endDate) throws Exception;
	
	/**
	 * Returns the name for the forecast
	 * @return
	 */
	String getName();
	
	/**
	 * Allows to define a name
	 * @param name
	 */
	void setName(String name);


}