package ch.pschatzmann.stocks.strategy.selection;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;

/**
 * Collection which collects the top n elements
 * 
 * Implemented as TreeSet
 * 
 * @author pschatzmann
 *
 */

public class TopNSet<E> extends TreeSet<E> implements Supplier<Set<E>>, Serializable {
	private static final long serialVersionUID = 1L;
	private int max=1;
	
	public TopNSet(int size, Comparator<E> c) {
		super(c);
		this.max = size;			
	}
	
	public TopNSet(int size) {
		this.max = size;			
	}

	public TopNSet() {
		this.max = 10;			
	}

	@Override
	public synchronized boolean add(E e) {
		boolean result = super.add(e);
		if (super.size()>max) {
			this.remove(this.last());
		}			
		return result;			
	}

	@Override
	public Set<E> get() {
		return this;
	}

} 