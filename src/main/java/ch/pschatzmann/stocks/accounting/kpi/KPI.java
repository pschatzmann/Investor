package ch.pschatzmann.stocks.accounting.kpi;

/**
 * Enum of all supported KPIs
 * 
 * @author pschatzmann
 *
 */
public enum KPI {
	SharpeRatio, 
	SharpeRatioAnualized, 
	AbsoluteReturn, 
	AbsoluteReturnAveragePerDay, 
	AbsoluteReturnStdDev, 
	ReturnPercent, 
	ReturnPercentAnualized, 
	ReturnPercentStdDev, 
	MaxDrawDownPercent, 
	MaxDrawDownAbsolute, 
	MaxDrawDownNumberOfDays, 
	MaxDrawDownHighValue, 
	MaxDrawDownLowValue, 
	MaxDrawDownPeriod, 
	NumberOfTrades, 
	NumberOfBuys, 
	NumberOfSells, 
	TotalFees, 
	NumberOfCashTransfers, 
	Cash, 
	PurchasedValue, 
	ActualValue, 
	RealizedGains, 
	UnrealizedGains,
	Profit,
	NumberOfTradedStocks

}
