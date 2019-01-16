package ch.pschatzmann.stocks.download;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.pschatzmann.stocks.errors.UserException;

/**
 * Test for calling the Main Class for the download of market data from
 * https://market-archive.appspot.com/
 * 
 * @author pschatzmann
 *
 */

public class DownloadMain {
	private static Logger LOG = LoggerFactory.getLogger(DownloadMain.class);

	public static void download(String user, String password, File file) throws UserException {
		if (user == null) {
			throw new UserException("'user' must not be null");
		}
		if (password == null) {
			throw new UserException("'password' must not be null");
		}
	}

	public static void main(String[] sa) {
		try {
			CommandLine cl = getCommandLine(sa);
			String fileName = cl.getOptionValue("file");
			if (fileName == null) {
				fileName = "stocks.7z";
			}
			download(cl.getOptionValue("user"), cl.getOptionValue("password"), new File(fileName));
		} catch (Exception ex) {
			LOG.error(ex.getLocalizedMessage(), ex);
		}
	}

	private static CommandLine getCommandLine(String[] args) throws ParseException {
		Options options = new Options();
		options.addOption("u", "user", true, "mail address");
		options.addOption("p", "password", true, "password");
		options.addOption("f", "file", true, "file path");

		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = parser.parse(options, args);
		return cmd;
	}

}
