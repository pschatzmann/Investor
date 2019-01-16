package ch.pschatzmann.stocks.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.commons.io.IOUtils;

public class FileUtils implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public synchronized static void writeLine(BufferedWriter fw, String line) {
		try {
			fw.write(line);
			fw.newLine();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Reads the file of resource indicated by the path string
	 * @param path
	 * @param encoding
	 * @return
	 * @throws IOException
	 */
	public static String read(String path, Charset encoding) throws IOException {
		File file = new File(path);
		String result = "";
		if (file.exists()) {
			result = read(file,encoding);

		} else {
			URL url = FileUtils.class.getResource(path);
			if (url!=null) {
				result = read(url,encoding);
			}
		}
		return result;
	}
 

	public static String read(File path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path.getPath()));
		return new String(encoded, encoding);
	}

	public static String read(URL url, Charset encoding) throws IOException {
		InputStream in = url.openStream();
		String result = "";
		try {
			result = IOUtils.toString(in, encoding);
		} finally {
			IOUtils.closeQuietly(in);
		}
		return result;
	}
	
	/**
	 * Loads the properties from the indicated file
	 * @param properties
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static Properties getProperties(File properties) throws FileNotFoundException, IOException {
		InputStream input = new FileInputStream(properties);
		Properties prop = null;
		// load a properties file
		try {
			if (input!=null) {
				prop = new Properties();
				prop.load(input);
			}
		} finally {
			if (input!=null)
				input.close();
		}
		return prop;
	}

}
