package ch.pschatzmann.stocks.strategy.selection;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import ch.pschatzmann.stocks.Context;
import ch.pschatzmann.stocks.IStockID;
import ch.pschatzmann.stocks.input.IReader;
import ch.pschatzmann.stocks.strategy.ITradingStrategy;

/**
 * Result of a stock selection and evaluation run. We use a separate object so
 * that we can save, load and re-process the result easily.
 * 
 * @author pschatzmann
 *
 */

public class SelectionResult implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Collection<SelectionState> selectionStates;
	@JsonIgnore
	private ObjectMapper mapper = new ObjectMapper();

	public SelectionResult() {		
	}
	
	public SelectionResult(Collection<SelectionState> selectionStates) {
		this.selectionStates = selectionStates;
	}

	public Collection<SelectionState> getResult() {
		return selectionStates;
	}

	@JsonIgnore
	public Collection<ITradingStrategy> getStrategies(IReader reader) {
		return selectionStates.stream().
				filter(ss -> !Context.isEmpty(ss.getStrategyName())).
				map(s -> s.getStrategy(reader)).
				collect(Collectors.toList());
	}
	
	@JsonIgnore
	public Collection<ITradingStrategy> getStrategies() {
		return this.getStrategies(Context.getDefaultReader());
	}


	@JsonIgnore
	public Collection<IStockID> getStocks() {
		return selectionStates.stream().
				filter(ss -> !Context.isEmpty(ss.getStrategyName())).
				map(s -> s.getStockID()).
				collect(Collectors.toList());
	}
	
	@JsonIgnore
	public Collection<Map> getCollectionOfMap() {
		return selectionStates.stream().
				filter(ss -> !Context.isEmpty(ss.getStrategyName())).
				map(s -> s.getMap()).
				collect(Collectors.toList());		
	}

	public void save(File file) throws IOException {
		ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file, false)));
		writer.writeValue(out, selectionStates);
		out.flush();
		out.close();
	}

	public void save(OutputStream os) throws IOException {
		ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
		PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(os)));
		writer.writeValue(out, selectionStates);
		out.flush();
		out.close();
	}

	public void load(File file) throws JsonParseException, JsonMappingException, IOException {
	    TypeReference<List<SelectionState>> tRef = new TypeReference<List<SelectionState>>() {};
		selectionStates = mapper.readValue(file, tRef);
	}
	
	public void load(InputStream is) throws JsonParseException, JsonMappingException, IOException {
	    TypeReference<List<SelectionState>> tRef = new TypeReference<List<SelectionState>>() {};
		selectionStates = mapper.readValue(is, tRef);
	}
	
	@Override
	public String toString() {
		return selectionStates.size()+" items";
	}


}
