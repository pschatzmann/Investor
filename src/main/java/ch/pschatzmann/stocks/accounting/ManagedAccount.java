package ch.pschatzmann.stocks.accounting;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.pschatzmann.stocks.Context;
import ch.pschatzmann.stocks.execution.fees.IFeesModel;

/**
 * 
 * Account that supports the saving and loading of the account information 
 * 
 * @author pschatzmann
 *
 */

public class ManagedAccount extends Account {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(ManagedAccount.class);

	/**
	 * Loads the account information if it exists otherwise it creates a new account
	 * @param id
	 * @param currency
	 * @param cash
	 * @param openDate
	 * @param fees
	 */
	public ManagedAccount(String id, String currency, Double cash, Date openDate, IFeesModel fees) {
		super(getBasicAccount(id,currency,cash,openDate,fees));
	}
	
	/**
	 * Loads the existing account information
	 * @param id
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public ManagedAccount(String id) throws JsonParseException, JsonMappingException, IOException {
		super(getBasicAccount(id));
	}
	
	/**
	 * Saves the basicAccount to a json file
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public void save() throws JsonGenerationException, JsonMappingException, IOException {
		resetStockData();
		
		String path = Context.getProperty("accountPath", "accounts");
		String fileName = path+File.separator +this.getId()+".json";
		new File(path).mkdirs();
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.writeValue(new File(fileName),this.getAccount());
	}
	
	private static BasicAccount getBasicAccount(String id, String currency, Double cash, Date openDate, IFeesModel fees) {
		BasicAccount account=null;
		try {
			account = getBasicAccount(id);
		} catch(Exception ex) {
			LOG.error("Could not load the account "+id+".json; "+ex);
			account = new BasicAccount(id, currency, cash, openDate, fees);
		}
		return account;
	}

	private static BasicAccount getBasicAccount(String id) throws IOException, JsonParseException, JsonMappingException {
		BasicAccount account;
		ObjectMapper mapper = new ObjectMapper();
		String path = Context.getProperty("accountPath", "accounts");
		String fileName = path+File.separator +id+".json";
		account = mapper.readValue(new File(fileName), BasicAccount.class);
		return account;
	}

}
