package ch.pschatzmann.stocks.company;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.apache.commons.jcs.JCS;
import org.apache.commons.jcs.access.CacheAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.pschatzmann.stocks.cache.RedisCache;

/**
 * Provides the Company Information with the help of the IEX API (see
 * https://iextrading.com/developer/docs/#company)
 * 
 * @author pschatzmann
 *
 */
public class Company {
	private static final Logger LOG = LoggerFactory.getLogger(Company.class);
	public static CacheAccess<String, Company> cache;
	private String ticker;
	private String companyName;
	private String description;
	private String exchange;
	private String industry;
	private String sector;

	public Company(String symbol) throws JsonParseException, JsonMappingException, MalformedURLException, IOException {
		setValues(symbol);		
	}


	public String getTicker() {
		return ticker;
	}

	public void setTicker(String ticker) {
		this.ticker = ticker;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getExchange() {
		return exchange;
	}

	public void setExchange(String exchange) {
		this.exchange = exchange;
	}

	public String getIndustry() {
		return industry;
	}

	public void setIndustry(String industry) {
		this.industry = industry;
	}

	public String getSector() {
		return sector;
	}

	public void setSector(String sector) {
		this.sector = sector;
	}

	protected void setValues(String symbol)
			throws IOException, JsonParseException, JsonMappingException, MalformedURLException {
		this.setTicker(symbol);
		if (cache==null) {
			cache = JCS.getInstance("companyCache");
		}
		Company result = null;
		if (cache!=null) {
			result = cache.get(symbol);
		}
		if (result==null) {
			String url = "https://api.iextrading.com/1.0/stock/"+symbol+"/company";
			LOG.info(url);
			ObjectMapper mapper = new ObjectMapper();
			Map<String,String> map = mapper.readValue(new URL(url), Map.class);
			this.setCompanyName(map.get("companyName"));
			this.setDescription(map.get("description"));
			this.setExchange(map.get("exchange"));
			this.setIndustry(map.get("industry"));
			this.setSector(map.get("sector"));
		} else {
			this.setCompanyName(result.getCompanyName());
			this.setDescription(result.getDescription());
			this.setExchange(result.getExchange());
			this.setIndustry(result.getIndustry());
			this.setSector(result.getSector());			
		}
	}
	
	
	public String toString() {
		return this.companyName;
	}
	
}
