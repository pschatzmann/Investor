package ch.pschatzmann.stocks.data.universe;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import ch.pschatzmann.stocks.IStockID;
import ch.pschatzmann.stocks.StockID;
import ch.pschatzmann.stocks.errors.UniverseException;

/**
 * Interface for Universe which provides a stream of stocks
 * 
 * @author pschatzmann
 *
 */
public interface IUniverse {

	public List<IStockID> list(); 

	/**
	 * Writes the data to a file
	 * 
	 * @param file
	 * @throws IOException
	 * @throws UniverseException
	 */
	public default void save(File file) throws IOException, UniverseException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		for (IStockID stock : list()) {
			writer.write(stock.toString());
			writer.newLine();
			writer.flush();
		}
		writer.close();
	}

	/**
	 * Loads the data from a file
	 * 
	 * @param file
	 * @throws IOException
	 * @throws UniverseException
	 */
	public default void load(File file) throws IOException, UniverseException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line;
		while ((line = reader.readLine()) != null) {
			list().add(StockID.parse(line));
		}
		reader.close();
	}

	/**
	 * Checks if the Universe is empty
	 * 
	 * @return
	 * @throws UniverseException
	 */
	public default boolean isEmpty() throws UniverseException {
		return list().isEmpty();
	}

	/**
	 * 
	 * @return the size of the universe
	 * @throws UniverseException
	 */
	public default int size() throws UniverseException {
		return list().size();
	}

	/**
	 * 
	 * @return the data as Stream
	 * @throws UniverseException
	 */
	public default Stream<IStockID> stream()  {
		return list().stream();
	}

}