package ch.pschatzmann.stocks.data.index;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.pschatzmann.stocks.StockID;


/**
 * Functionality which provides the scope of the known and supported exchanges
 * which are used for evaluating data
 * 
 * @author pschatzmann
 *
 */

public abstract class IndexFromFile implements Serializable, IIndex {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final Logger LOG = LoggerFactory.getLogger(IndexFromFile.class);
	private List<IndexRecord> list;
	private String fileName;
	
	public IndexFromFile(String fileName) {
		this.fileName = fileName;
	}
	
	/* (non-Javadoc)
	 * @see ch.pschatzmann.stocks.data.index.IIndex#list()
	 */
	@Override
	public  List<IndexRecord> list() throws IOException {
		if (list==null) {
			InputStream is = getInputStream();
			Iterable<String> iterable = () -> new Scanner(is).useDelimiter("\\n");
			list = StreamSupport.stream(iterable.spliterator(), false)
					.map(fn -> toIndexRecord(fn))
					.filter (fn -> fn!=null)
					.collect(Collectors.toList());
			
			// sort by weight 
			list.sort(Comparator.comparing(IndexRecord::getWeight).reversed());

		}
		return list;
	}
	
	/**
	 * Calculates the target amout of each stock for the indicates total portfolio value
	 * @param value
	 * @return
	 * @throws IOException
	 */
	public Collection<IndexRecord> calculate(Double value) throws IOException {
		double total = list().stream().mapToDouble(rec -> rec.getWeight()).sum();		
		Collection<IndexRecord> result = list().stream()
			.map(rec -> new IndexRecord(rec.getId(), rec.getWeight()/total*100.0*value))
			.collect(Collectors.toList());
		return result;
	}
	
	private IndexRecord toIndexRecord(String str) {
		String sa[] = str.split(",");
		if (!sa[0].equals("exchange")) {
			return new IndexRecord(new StockID(sa[1],sa[0]),sa[2]);
		} else {
			return null;
		}
	}
	
	private InputStream getInputStream() throws FileNotFoundException {
		InputStream is = IndexFromFile.class.getResourceAsStream(getFileName());
		if (is==null) {
			LOG.warn("Could not determine the exchanges.csv on the classpath. We use the absolte path instead");
			is = new FileInputStream("/Users/pschatzmann/Documents/workspace/stocks-result/"+getFileName());
		}    
		return is;
	}
	
	public String getFileName() {
		return fileName;
	}
	
}
