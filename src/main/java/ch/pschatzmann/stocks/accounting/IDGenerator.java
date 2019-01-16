package ch.pschatzmann.stocks.accounting;

/**
 * We use the system nano time as identifier. We inititially used a GUID but this was too long
 * for e-trade where the id maximum size was 20 characters
 * 
 * @author pschatzmann
 *
 */
public class IDGenerator {
	private long lastID = 0l;
	
	public IDGenerator() {		
	}
	
	public String getID() {
		long time = System.nanoTime();
		while (lastID == time) {	
			time = System.nanoTime();
		}
		lastID = time;
		return String.valueOf(time);
	}
	
}
