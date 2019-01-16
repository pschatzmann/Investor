package ch.pschatzmann.stocks.errors;

/**
 * Common generic error
 * 
 * @author pschatzmann
 *
 */
public class CommonException extends Exception {
	private static final long serialVersionUID = 1L;

	public CommonException(Exception ex) {
		super(ex);
	}

	public CommonException(String msg) {
		super(msg);
	}

	public CommonException(String msg, Exception ex) {
		super(msg,ex);
	}
}
