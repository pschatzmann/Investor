package ch.pschatzmann.stocks.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;

public class InputStreamIterator implements Iterator<String>, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private BufferedReader br;
	private String line;

	public InputStreamIterator(InputStream is, String encoding) throws UnsupportedEncodingException {
		this.br = new BufferedReader(new InputStreamReader(is, encoding));
		advance();
	}

	public InputStreamIterator(InputStream is) throws UnsupportedEncodingException {
		this.br = new BufferedReader(new InputStreamReader(is));
		advance();
	}

	@Override
	public boolean hasNext() {
		return line != null;
	}

	@Override
	public String next() {
		String retval = line;
		advance();
		return retval;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("Remove not supported on BufferedReader iteration.");
	}

	private void advance() {
		try {
			line = br.readLine();
		} catch (IOException e) {
			if (line == null && br != null) {
				try {
					br.close();
				} catch (IOException ex) {
				}
				br = null;
			}
		}
	}
}
