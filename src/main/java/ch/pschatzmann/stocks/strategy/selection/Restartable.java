package ch.pschatzmann.stocks.strategy.selection;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Functionality to save and load the current data so that we can implement
 * functionality which is restartable.
 * 
 * @author pschatzmann
 *
 */

public class Restartable implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static Logger LOG = LoggerFactory.getLogger(Restartable.class);
	private String fileName;
	private int count = 100;

	public Restartable(String fileName) {
		this.fileName = fileName;
	}

	public Restartable(String fileName, int count) {
		this.fileName = fileName;
		this.count = count;
	}

	public void save(Map data) throws IOException {
		LOG.info("Saving "+fileName);
		ObjectOutputStream oos = null;
		FileOutputStream fout = null;
		try {
			fout = new FileOutputStream(fileName, false);
			oos = new ObjectOutputStream(fout);
			oos.writeObject(data);
			oos.flush();
		} catch (Exception ex) {
			LOG.error(ex.getLocalizedMessage(), ex);
		} finally {
			if (oos != null) {
				oos.close();
			}
		}

	}

	public int getCount() {
		return count;
	}

	public Map load() {
		Map result = null;
		ObjectInputStream objectinputstream = null;
		try {
			FileInputStream streamIn = new FileInputStream(fileName);
			objectinputstream = new ObjectInputStream(streamIn);
			Object obj = objectinputstream.readObject();
			result = (Map) obj;
		} catch (FileNotFoundException e) {
			LOG.info(e.getMessage());
		} catch (Exception e) {
			LOG.error("{}",e);
		} finally {
			if (objectinputstream != null) {
				try {
					objectinputstream.close();
				} catch (IOException e) {
					LOG.error(e.getLocalizedMessage(), e);
				}
			}
		}

		return result;
	}

}
