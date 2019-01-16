package ch.pschatzmann.stocks.parameters;

public interface IValueSource {
	public boolean nextValue(ParameterValue pv);
}
