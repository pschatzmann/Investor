package ch.pschatzmann.stocks.execution.fees;

/**
 * Trading fees for postfinance 
 * https://www.postfinance.ch/content/dam/pfch/doc/prod/acc/etrad_cond_en.pdf
 * @author pschatzmann
 *
 */
public class PostFinanceFees implements IFeesModel {

	@Override
	public double getFeesPerTrade(double qty, double value) {
		if (value <= 1000) {
			return 25;
		}
		if (value <= 5000) {
			return 35;
		}
		if (value <= 10000) {
			return 40;
		}
		if (value <= 15000) {
			return 50;
		}
		if (value <= 20000) {
			return 70;
		}
		if (value <= 30000) {
			return 95;
		}
		if (value <= 50000) {
			return 130;
		}
		if (value <= 100000) {
			return 180;
		}
		if (value <= 150000) {
			return 270;
		}
		return 350;
	}

}
