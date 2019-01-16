package ch.pschatzmann.stocks.errors;

/**
 * User (entry) error
 * 
 * @author pschatzmann
 *
 */
public class UserException extends CommonException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UserException(Exception ex) {
		super(ex);
	}
	
	public UserException(String msg) {
		super(msg);
	}

	public UserException(String msg, Exception ex) {
		super(msg,ex);
	}


}
