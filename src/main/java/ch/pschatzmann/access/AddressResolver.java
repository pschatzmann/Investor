package ch.pschatzmann.access;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resolve the address with the help of system properties
 * 
 * @author pschatzmann
 *
 */

public class AddressResolver implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(AddressResolver.class);
	private String propertyName;
	private String DEFAULT = "10.0.1.20";
	private String DEFAULT_USER = "pschatzmann";
	private String DEFAULT_PASSWORD = "sabrina1";
	private Properties properties;

	public AddressResolver(String name) {
		this.propertyName = name;
	}

	public String getAddress() {
		loadProperties();
		String result = getProperty(propertyName, DEFAULT);
		LOG.debug(result);
		return result;

	}

	public String getUser() {
		loadProperties();
		return getProperty(propertyName + ".user", DEFAULT_USER);
	}

	public String getPassword() {
		loadProperties();
		return getProperty(propertyName + ".password", DEFAULT_PASSWORD);
	}

	private String getProperty(String string, String defaultResult) {
		String value = null;
		if (properties != null) {
			value = (String) properties.get(string);
		}
		return value == null ? defaultResult : value;
	}

	private boolean loadProperties() {
		if (properties == null) {
			final InputStream is = this.getClass().getResourceAsStream("stocks.properties");
			if (is != null) {
				try {
					properties = new Properties();
					properties.load(is);
				} catch (IOException e) {
				} finally {
					try {
						is.close();
					} catch (IOException e) {
					}
				}
			}
		}
		return properties != null;
	}

}
