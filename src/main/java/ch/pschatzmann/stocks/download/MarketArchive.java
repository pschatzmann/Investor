//package ch.pschatzmann.stocks.download;
//
//import java.io.BufferedOutputStream;
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.io.OutputStream;
//import java.net.MalformedURLException;
//
//import org.apache.commons.cli.CommandLine;
//import org.apache.commons.cli.CommandLineParser;
//import org.apache.commons.cli.DefaultParser;
//import org.apache.commons.cli.Options;
//import org.apache.commons.cli.ParseException;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import com.gargoylesoftware.htmlunit.BrowserVersion;
//import com.gargoylesoftware.htmlunit.WebClient;
//import com.gargoylesoftware.htmlunit.html.DomElement;
//import com.gargoylesoftware.htmlunit.html.HtmlButton;
//import com.gargoylesoftware.htmlunit.html.HtmlForm;
//import com.gargoylesoftware.htmlunit.html.HtmlPage;
//import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
//import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
//
//import ch.pschatzmann.stocks.errors.UserException;
//
///**
// * 
// * Download the stock data from the Market Archive and updates the stocks.csv
// * file
// * 
// * We use the htmlunit library for driving the html communication.
// * 
// * @author pschatzmann
// *
// */
//
//public class MarketArchive {
//	private static Logger LOG = LoggerFactory.getLogger(MarketArchive.class);
//	static final String TITLE1 = "Complete Historical Stock Data - Market Archive";
//	static final String TITLE2 = "Data Download - Market Archive";
//
//	public static void main(String[] sa) {
//		try {
//			CommandLine cl = getCommandLine(sa);
//			String fileName = cl.getOptionValue("file");
//			if (fileName == null) {
//				fileName = "stocks.7z";
//			}
//			File file = new File(fileName);
//			file.delete();
//			download(cl.getOptionValue("user"), cl.getOptionValue("password"), file);
//			extract(file);
//
//			UpdateStocksCSV.createCSVUniverse(file.getParentFile());
//
//		} catch (Exception ex) {
//			LOG.error(ex.getLocalizedMessage(), ex);
//		}
//	}
//
//	public static void download(String user, String password, File file) throws UserException {
//		if (user == null) {
//			throw new UserException("'user' must not be null");
//		}
//		if (password == null) {
//			throw new UserException("'password' must not be null");
//		}
//		final WebClient webClient = new WebClient(BrowserVersion.INTERNET_EXPLORER_11);
//		webClient.getOptions().setCssEnabled(false);
//		//webClient.getOptions().setJavaScriptEnabled(false);
//		webClient.getOptions().setThrowExceptionOnScriptError(false);
//
//		try {
//			final HtmlForm form = processLogin(user, password, webClient);
//			processDownloadScreen(file, form, webClient);		
//		} catch(Exception ex) {
//			throw new UserException(ex);
//		}
//
//		if (!file.exists()) {
//			throw new UserException("The file was not created " + file);
//		}
//
//		LOG.info("New file '" + file.getAbsolutePath() + "' created with " + file.getTotalSpace() + " bytes");
//
//	}
//
//	private static void processDownloadScreen(File file, final HtmlForm form, WebClient webClient)
//			throws IOException, FileNotFoundException {
//		
//		final HtmlButton button = form.getButtonByName("");
//		HtmlPage page2 = button.click();
//		
//		String title2 = page2.getTitleText(); 
//		if (!TITLE2.equals(title2)) {						
//			page2 = webClient.getPage("https://market-archive.appspot.com/download");
//		}
//
//		file.delete();
//		DomElement downloadLink = (DomElement) page2.getByXPath("//a[@class='btn btn-primary btn-large']").get(0);
//		LOG.info("Downloading...");
//		InputStream is = downloadLink.click().getWebResponse().getContentAsStream();
//		OutputStream os = new BufferedOutputStream(new FileOutputStream(file));
//		int read = 0;
//		byte[] bytes = new byte[10240];
//
//		while ((read = is.read(bytes)) != -1) {
//			os.write(bytes, 0, read);
//		}
//
//		os.flush();
//		os.close();
//		is.close();
//
//	}
//
//	private static HtmlForm processLogin(String user, String password, final WebClient webClient)
//			throws IOException, MalformedURLException, UserException {
//		final HtmlPage page = webClient.getPage("https://market-archive.appspot.com");
//		final HtmlForm form = page.getForms().get(0);
//
//		//final HtmlHiddenInput xsrf = form.getInputByName("_xsrf");
//		final HtmlTextInput email = form.getInputByName("email");
//		final HtmlPasswordInput pwd = form.getInputByName("pwd");
//
//		email.setValueAttribute(user);
//		pwd.setValueAttribute(password);
//
//		String title1 = page.getTitleText();
//		if (!TITLE1.equals(title1)) {
//			LOG.info(page.asXml());
//			throw new UserException("Could not open the market archive inital page. The current tile is " + title1);
//		}
//		return form;
//	}
//
//	private static void extract(File file) {
//		try {
//			// new Extract7zip(file, file.getParentFile(), false, null).extract( "/out/", "/");
//			File out = new File("out");
//			if (!out.exists()) {
//				exec("ln -s "+file.getParentFile()+" out");
//			}
//			exec("7z -y x " + file.getAbsolutePath());
//		} catch (Exception e) {
//			LOG.error("Could not extract 7z file ", e);
//		}
//	}
//
//	private static void exec(String command) {
//		Process p;
//		try {
//			LOG.info(command);
//			p = Runtime.getRuntime().exec(command);
//			p.waitFor();
//			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
//
//			String line = "";
//			while ((line = reader.readLine()) != null) {
//				LOG.info(line);
//			}
//
//		} catch (Exception e) {
//			LOG.error(e.getLocalizedMessage(), e);
//		}
//
//	}
//
//	private static CommandLine getCommandLine(String[] args) throws ParseException {
//		Options options = new Options();
//		options.addOption("u", "user", true, "mail address");
//		options.addOption("p", "password", true, "password");
//		options.addOption("f", "file", true, "file path");
//
//		CommandLineParser parser = new DefaultParser();
//		CommandLine cmd = parser.parse(options, args);
//		return cmd;
//	}
//}
