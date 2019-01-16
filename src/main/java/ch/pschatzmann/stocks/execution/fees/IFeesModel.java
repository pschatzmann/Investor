package ch.pschatzmann.stocks.execution.fees;

public interface IFeesModel {
	double getFeesPerTrade(double qty, double value);
}
