package ch.pschatzmann.access;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.slf4j.LoggerFactory;

/**
 * Determinaiton of the root logger and resetting of the logging system
 * 
 * @author pschatzmann
 *
 */
public class Logger {
	/**
	 * slf4j does not provide the functionality to reset the logger. Here we have
	 * the missing implementation
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 */
	public static void reset() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {

		Class<?> clazz = LoggerFactory.class;
		Method method = clazz.getDeclaredMethod("reset");
	    method.setAccessible(true);
	    method.invoke(new Object[0]);
	    
	    method = clazz.getDeclaredMethod("performInitialization");
	    method.setAccessible(true);
	    method.invoke(new Object[0]);
	    
	}

	/**
	 * Returns the root logger
	 * @return
	 */
	public static org.slf4j.Logger getRootLogger() {
		return LoggerFactory.getLogger("ROOT");
	}
	
	/** The unique elements of the classpath, as an ordered list. */
	private final ArrayList<File> classpathElements = new ArrayList<>();

	/** The unique elements of the classpath, as a set. */
	private final HashSet<String> classpathElementsSet = new HashSet<>();

	/** Clear the classpath. */
	private void clearClasspath() {
	    classpathElements.clear();
	    classpathElementsSet.clear();
	}

	/** Add a classpath element. */
	private void addClasspathElement(String pathElement) {
	    if (classpathElementsSet.add(pathElement)) {
	        final File file = new File(pathElement);
	        if (file.exists()) {
	            classpathElements.add(file);
	        }
	    }
	}

	/** Parse the system classpath. */
	private void parseSystemClasspath() {
	    // Look for all unique classloaders.
	    // Keep them in an order that (hopefully) reflects the order in which class resolution occurs.
	    ArrayList<ClassLoader> classLoaders = new ArrayList<>();
	    HashSet<ClassLoader> classLoadersSet = new HashSet<>();
	    classLoadersSet.add(ClassLoader.getSystemClassLoader());
	    classLoaders.add(ClassLoader.getSystemClassLoader());
	    if (classLoadersSet.add(Thread.currentThread().getContextClassLoader())) {
	        classLoaders.add(Thread.currentThread().getContextClassLoader());
	    }
	    // Dirty method for looking for any other classloaders on the call stack
	    try {
	        // Generate stacktrace
	        throw new Exception();
	    } catch (Exception e) {
	        StackTraceElement[] stacktrace = e.getStackTrace();
	        for (StackTraceElement elt : stacktrace) {
	            try {
	                ClassLoader cl = Class.forName(elt.getClassName()).getClassLoader();
	                if (classLoadersSet.add(cl)) {
	                    classLoaders.add(cl);
	                }
	            } catch (ClassNotFoundException e1) {
	            }
	        }
	    }

	    // Get file paths for URLs of each classloader.
	    clearClasspath();
	    for (ClassLoader cl : classLoaders) {
	        if (cl != null) {
	            for (URL url : getURLs(cl)) {
	            	
	                if ("file".equals(url.getProtocol())) {
	                    addClasspathElement(url.getFile());
	                }
	            }
	        }
	    }
	}

	private URL[] getURLs(ClassLoader cl) {
		try {
			return ((URLClassLoader) cl).getURLs();
		} catch(Exception ex) {
			return new URL[0];
		}
	}


	/**
	 * Get a list of unique elements on the classpath (directories and files) as File objects, preserving order.
	 * Classpath elements that do not exist are not included in the list.
	 */
	public List<File> getClasspathElements() {
		this.parseSystemClasspath();
	    return classpathElements;
	}

	
}
