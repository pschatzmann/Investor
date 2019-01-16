package ch.pschatzmann.stocks.execution.fees;

/**
 * US Fees
 */

public class InteractiveBrokersFees implements IFeesModel {

	@Override
	public double getFeesPerTrade(double qty, double value) {
		double result = 0.005 * qty;
		//min 1USD
		result = Math.min(result, 1);
		//max 0.5% of trade value
		result = Math.max(result, 0.005*value);
		return result;
	}
}
