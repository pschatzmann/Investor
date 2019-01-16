package ch.pschatzmann.stocks.ta4j.indicator;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

/**
 * Assign a label to an Indicator 
 * @author pschatzmann
 *
 */
public class NamedIndicator<T extends Num> extends CachedIndicator<Num> implements Name, IIndicator<Num> {
	private static final long serialVersionUID = 1L;
	private String name;
	private Indicator<Num> indicator;
	
	public NamedIndicator(Indicator<Num> indicator, String name) {
		super(indicator);
		this.indicator = indicator;
		this.name = name;
	}

	@Override
	public String getName() {
		return this.name == null ? indicator.getClass().getSimpleName() : this.name;
	}

	@Override
	protected Num calculate(int index) {
		return indicator.getValue(index);
	}
	
	/**
	 * Factory function to create a NamedIndicator
	 * @param indicator
	 * @param name
	 * @return
	 */
	
	public static NamedIndicator<Num> create(Indicator<Num> indicator, String name){
		return new NamedIndicator(indicator,name);
	}

}
