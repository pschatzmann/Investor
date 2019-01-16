package ch.pschatzmann.stocks.data.index;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import ch.pschatzmann.stocks.IStockID;

/**
 * Representation of a stock index. We provide the ticker symbol and the
 * information of it's components
 * 
 * @author pschatzmann
 *
 */
public interface IIndex {

	/**
	 * Returns all components of the index with the corresponding ticker symbol and
	 * weight
	 * 
	 * @return
	 * @throws IOException
	 */
	public List<IndexRecord> list() throws IOException;

	/**
	 * Provides the ticker symbol of the index itself
	 * 
	 * @return
	 */
	public IStockID getStockID();

	/**
	 * Provides the list of Stock IDs for the components
	 * 
	 * @return
	 * @throws IOException
	 */
	public default List<IStockID> listID() throws IOException {
		return list().stream().map(rec -> rec.getId()).collect(Collectors.toList());
	}

}