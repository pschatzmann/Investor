package ch.pschatzmann.stocks.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.pschatzmann.stocks.data.universe.exchanges.ExchangesFromFile;
import ch.pschatzmann.stocks.data.universe.exchanges.ExchangesMappings;

public class Streams  implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(ExchangesFromFile.class);

    public static <T> Stream<T> asStream(Iterator<T> sourceIterator) {
        return asStream(sourceIterator, false);
    }

    public static <T> Stream<T> asStream(Iterator<T> sourceIterator, boolean parallel) {
        Iterable<T> iterable = () -> sourceIterator;
        return StreamSupport.stream(iterable.spliterator(), parallel);
    }
    
    public static <T> Stream<T> asStream(Collection<T> collection, Consumer<Collection<T>> consumer) {
   		consumer.accept(collection);
        return collection.stream();
    }
    
    public static Supplier asSupplier(Collection c) {
    	return new Supplier() {
			@Override
			public Object get() {
				return c;
			}   		
    	};
    }
    
    public static Stream<String> asStream(InputStream is) throws UnsupportedEncodingException {
    	return asStream(new InputStreamIterator(is));
    }
    public static Stream<String> asStream(InputStream is, String encoding) throws UnsupportedEncodingException {
    	return asStream(new InputStreamIterator(is, encoding));
    }
        
	public static InputStream getInputStream(String name) throws FileNotFoundException {
		InputStream is = ExchangesMappings.class.getResourceAsStream("/"+name);
		if (is==null) {
			LOG.warn("Could not determine the exchanges.csv on the classpath. We use the absolte path instead");
			is = new FileInputStream(name);
		}    
		return is;
	}
  
}

