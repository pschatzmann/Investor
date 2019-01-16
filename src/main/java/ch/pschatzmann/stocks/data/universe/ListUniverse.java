package ch.pschatzmann.stocks.data.universe;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ch.pschatzmann.stocks.Context;
import ch.pschatzmann.stocks.IStockID;
import ch.pschatzmann.stocks.StockID;
import ch.pschatzmann.stocks.errors.UniverseException;

/**
 * Simple way to define a Universe by passing in a collection of IStockID. We
 * prevent multiple occurrences of the same ID. The order is retained!
 * 
 * @author pschatzmann
 *
 */
public class ListUniverse implements IUniverse, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<IStockID> stocks = new ArrayList();

	/**
	 * Constructor with a list of ids
	 * 
	 * @param stocks
	 */
	public ListUniverse(Collection<IStockID> stocks) {
		add(stocks);
	}

	/**
	 * Constructor with multiple lists of ids
	 * 
	 * @param listOfStocks
	 */
	public ListUniverse(Collection<IStockID>... listOfStocks) {
		add(listOfStocks);
	}

	/**
	 * Constructor with a string of the syntax "exchange:id, exchange2:id2"
	 * 
	 * @param input
	 */
	public ListUniverse(String input) {
		stocks = Arrays.stream(input.split(",")).map(str -> StockID.parse(str)).collect(Collectors.toList());
	}

	/**
	 * Adds multiple collection of ids
	 * 
	 * @param listOfStocks
	 */
	public void add(Collection<IStockID>... listOfStocks) {
		for (Collection<IStockID> stocks : listOfStocks) {
			add(stocks);
		}
	}

	/**
	 * Adds a collection of ids
	 * 
	 * @param stocks
	 */
	public void add(Collection<IStockID> stocks) {
		add(stocks.stream());
	}

	/**
	 * Adds a stream of ids
	 * 
	 * @param stocks
	 */
	public void add(Stream<IStockID> stocks) {
		stocks.forEach(id -> this.add(id));
	}

	/**
	 * Adds the stock ids of th indicated universe
	 * 
	 * @param iniverse
	 * @throws UniverseException
	 */
	public void add(IUniverse universe) throws UniverseException {
		add(universe.list());
	}

	/**
	 * Retains all entries which are both in the current set and the stocksToRetain.
	 * 
	 * @param stocksToRetain
	 */
	public void retainAll(Collection<IStockID> stocksToRetain) {
		this.stocks.retainAll(stocksToRetain);
	}

	/**
	 * Retains all indicated entries. The matching is done on both the symbol and
	 * the exchange
	 * 
	 * @param stocksToRetain
	 */
	public void removeAll(Collection<IStockID> stocksToRemove) {
		this.stocks.removeAll(stocksToRemove);
	}

	protected void add(IStockID id) {
		if (!this.stocks.contains(id)) {
			this.stocks.add(id);
		}
	}

	@Override
	public List<IStockID> list()  {
		return this.stocks;
	}

	/**
	 * Returns the first n entries
	 * 
	 * @param n
	 * @return
	 * @throws UniverseException
	 */
	public List<IStockID> list(int n)  {
		int min = Math.min(n, this.stocks.size());
		return this.stocks.subList(0, min);
	}

	@Override
	public String toString() {
		return this.stocks.toString();
	}

	/**
	 * Saves the content to a file
	 * 
	 * @throws IOException
	 */
	@Override
	public void save(File file) throws IOException {
		FileOutputStream fos = new FileOutputStream(file);
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
		for (IStockID stock : stocks) {
			bw.write(stock.toString());
			bw.newLine();
		}
		bw.close();

	}

	/**
	 * Loads the content from a file
	 * 
	 * @throws IOException
	 */
	@Override
	public void load(File file) throws IOException {
		FileInputStream fos = new FileInputStream(file);
		BufferedReader br = new BufferedReader(new InputStreamReader(fos));
		String thisLine = null;
		while ((thisLine = br.readLine()) != null) {
			if (!Context.isEmpty(thisLine)) {
				this.stocks.add(StockID.parse(thisLine));
			}
		}
	}

}
