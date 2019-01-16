package ch.pschatzmann.stocks.utils;

import java.util.AbstractList;
import java.util.List;

import com.github.dakusui.combinatoradix.Enumerator;

public class EnumeratorAdapter<E> extends AbstractList<List<E>>  {
	final Enumerator<E> enumerator;

	public EnumeratorAdapter(Enumerator<E> enumerator) {
		this.enumerator = enumerator;
	}

	@Override
	public List<E> get(int index) {
		return this.enumerator.get(index);
	}

	@Override
	public int size() {
		long ret = this.enumerator.size();
		if (ret > Integer.MAX_VALUE) {
			throw new IllegalStateException();
		}
		return (int) ret;
	}
}
