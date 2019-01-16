package ch.pschatzmann.stocks.download;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;

import ch.pschatzmann.stocks.IStockID;
import ch.pschatzmann.stocks.data.universe.IUniverse;
import ch.pschatzmann.stocks.data.universe.MarketDirectoryUniverse;
import ch.pschatzmann.stocks.errors.UniverseException;

/**
 * Creates the stocks.csv form the existing csv files
 * 
 * @author pschatzmann
 *
 */

public class UpdateStocksCSV {
	
	public static void createCSVUniverse(File path) throws  UniverseException{
		try {
			IUniverse fu = new MarketDirectoryUniverse(path);	
			List<IStockID> result = fu.list().stream().collect(Collectors.toList());
			FileWriter fw = new FileWriter(new File(path, "stocks.csv"));
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter out = new PrintWriter(bw);
			for (IStockID s : result){
				out.write(s.getExchange());
				out.write(",");
				out.write(s.getTicker());
				out.println();
			}
			out.flush();
			out.close();
		} catch(Exception ex) {
			throw new UniverseException(ex);
		}

	}
	
	public static void main(String[] args) throws UniverseException  {
		String path = "/var/www/stocks-data";
		if (args.length>0) {
			path = args[0];
		}
		createCSVUniverse(new File(path));
	}

}
