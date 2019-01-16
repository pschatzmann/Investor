//package ch.pschatzmann.stocks.download;
//
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.RandomAccessFile;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//import java.util.regex.Pattern;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import ch.pschatzmann.stocks.errors.UserException;
//import net.sf.sevenzipjbinding.IInArchive;
//import net.sf.sevenzipjbinding.PropID;
//import net.sf.sevenzipjbinding.SevenZip;
//import net.sf.sevenzipjbinding.SevenZipException;
//import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
//
///**
// * Extract the files from a 7zip compressed archive file
// * 
// * @author pschatzmann
// *
// */
//
//public class Extract7zip {
//	private static Logger LOG = LoggerFactory.getLogger(Extract7zip.class);
//	private File archive;
//	private File outputDirectory;
//	private boolean test;
//	private String filterRegex;
//	private String replaceFrom=null;
//	private String replaceWith=null;
//
//	public Extract7zip(File archive, File outputDirectory, boolean test, String filter) {
//		this.archive = archive;
//		this.outputDirectory = outputDirectory;
//		this.test = test;
//		this.filterRegex = filterToRegex(filter);
//	}
//
//	public void extract(String replaceFrom, String replaceWith) throws UserException {
//		checkArchiveFile();
//		prepareOutputDirectory();
//		extractArchive( replaceFrom,  replaceWith);
//	}
//
//	private void prepareOutputDirectory() throws UserException {
//		if (!outputDirectory.exists()) {
//			outputDirectory.mkdirs();
//		} else {
//			if (outputDirectory.list().length != 0) {
//				LOG.info("Output directory not empty: " + outputDirectory);
//			}
//		}
//	}
//
//	private void checkArchiveFile() throws UserException {
//		if (!archive.exists()) {
//			throw new UserException("Archive file not found: " + archive);
//		}
//		if (!archive.canRead()) {
//			LOG.error("Can't read archive file: " + archive);
//		}
//	}
//
//	public void extractArchive(String replaceFrom, String replaceWith) throws UserException {
//		RandomAccessFile randomAccessFile;
//		boolean ok = false;
//		try {
//			randomAccessFile = new RandomAccessFile(archive, "r");
//		} catch (FileNotFoundException e) {
//			throw new UserException( e);
//		}
//		try {
//			extractArchive(randomAccessFile);
//			ok = true;
//		} finally {
//			try {
//				randomAccessFile.close();
//			} catch (Exception e) {
//				if (ok) {
//					throw new UserException("Error closing archive file");
//				}
//			}
//		}
//	}
//
//	private static String filterToRegex(String filter) {
//		if (filter == null) {
//			return null;
//		}
//		return "\\Q" + filter.replace("*", "\\E.*\\Q") + "\\E";
//	}
//
//	private void extractArchive(RandomAccessFile file) throws UserException {
//		IInArchive inArchive;
//		boolean ok = false;
//		try {
//			LOG.info("Platforms:"+SevenZip.getPlatformList());
//			LOG.info("Best Match: "+SevenZip.getPlatformBestMatch());
//            SevenZip.initSevenZipFromPlatformJAR();
//			inArchive = SevenZip.openInArchive(null, new RandomAccessFileInStream(file));
//		} catch (Exception e) {
//			throw new UserException("Error opening archive", e);
//		}
//		try {
//
//			int[] ids = null; // All items
//			if (filterRegex != null) {
//				ids = filterIds(inArchive, filterRegex);
//			}
//			inArchive.extract(ids, test, new ExtractCallback(inArchive,this.outputDirectory, this.replaceFrom, this.replaceWith));
//			ok = true;
//		} catch (SevenZipException e) {
//			StringBuilder stringBuilder = new StringBuilder();
//			stringBuilder.append("Error extracting archive '");
//			stringBuilder.append(archive);
//			stringBuilder.append("': ");
//			stringBuilder.append(e.getMessage());
//			if (e.getCause() != null) {
//				stringBuilder.append(" (");
//				stringBuilder.append(e.getCause().getMessage());
//				stringBuilder.append(')');
//			}
//			String message = stringBuilder.toString();
//
//			throw new UserException(message, e);
//		} finally {
//			try {
//				inArchive.close();
//			} catch (SevenZipException e) {
//				if (ok) {
//					throw new UserException("Error closing archive", e);
//				}
//			}
//		}
//	}
//
//	private static int[] filterIds(IInArchive inArchive, String regex) throws SevenZipException {
//		List<Integer> idList = new ArrayList<>();
//
//		int numberOfItems = inArchive.getNumberOfItems();
//
//		Pattern pattern = Pattern.compile(regex);
//		for (int i = 0; i < numberOfItems; i++) {
//			String path = (String) inArchive.getProperty(i, PropID.PATH);
//			String fileName = new File(path).getName();
//			if (pattern.matcher(fileName).matches()) {
//				idList.add(i);
//			}
//		}
//
//		int[] result = new int[idList.size()];
//		for (int i = 0; i < result.length; i++) {
//			result[i] = idList.get(i);
//		}
//		return result;
//	}
//
//	public static void main(String[] args) {
//		boolean test = false;
//		String filter = null;
//		List<String> argList = new ArrayList<>(Arrays.asList(args));
//		if (argList.size() > 0 && argList.get(0).equals("-t")) {
//			argList.remove(0);
//			test = true;
//		}
//		if (argList.size() != 2 && argList.size() != 3) {
//			System.out.println("Usage: java -cp ch.pschatzmann.stocks.download.Extract7zip [-t] <archive> <output-dir> [filter]");
//			System.exit(1);
//		}
//		if (argList.size() == 3) {
//			filter = argList.get(2);
//		}
//		try {
//			LOG.info("Extract7zip...");
//			new Extract7zip(new File(argList.get(0)), new File(argList.get(1)), test, filter).extract(null,null);
//			LOG.info("Extraction successfull");
//		} catch (Exception e) {
//			LOG.error("ERROR: " + e.getLocalizedMessage(),e);
//		}
//		System.exit(0);
//	}
//}
