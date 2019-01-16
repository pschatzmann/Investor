package ch.pschatzmann.stocks.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public interface IScaler {

	void setValues(Collection<Double> values);

	Double normalizeValue(Double value);

	Double denormalizeValue(Double value);
	
	default List<Double> normalizeList(List<Double> values) {
		return values.stream().map(v -> normalizeValue(v)).collect(Collectors.toList());
	}

	default List<Double> normalizeArray(Double[] values) {
		return normalizeList(Arrays.asList(values));
	}
	
	default List<Double> denormalizeList(List<Double> values) {
		return values.stream().map(v -> denormalizeValue(v)).collect(Collectors.toList());
	}
	
	default List<Double> denormalizeArray(Double[] values) {
		return denormalizeList(Arrays.asList(values));
	}	
}