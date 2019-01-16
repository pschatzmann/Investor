package ch.pschatzmann.stocks.forecasting;

/**
 * Drift calculation for random forecast
 * 
 * @author pschatzmann
 *
 */
public class RandomDrift implements IDrift {
	private java.util.Random r = new java.util.Random();
	private int driftPeriods = 0;
	private double drift = 0.0;
	private int maxDriftPeriods = 400;
	private double driftFactor = 0.001;

	public RandomDrift() {
	}

	public RandomDrift(int maxDriftPeriods, Double driftFactor) {
		this.maxDriftPeriods = maxDriftPeriods;
		this.driftFactor = driftFactor;
	}

	public double drift() {
		if (driftPeriods-- <= 0) {
			driftPeriods = r.nextInt(maxDriftPeriods);
			drift = r.nextDouble() * driftFactor;
		}
		return drift;
	}

}
