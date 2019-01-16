package ch.pschatzmann.stocks.execution.fees;

/**
 * CH Feeds
 * @author pschatzmann
 */

public class InteractiveBrokersCHFees implements IFeesModel {
	@Override
	public double getFeesPerTrade(double qty, double value) {
		// 0.1%
		double result = 0.001 * value;
		//min 10
		result = Math.min(result, 10);
		return result;
	}
}
