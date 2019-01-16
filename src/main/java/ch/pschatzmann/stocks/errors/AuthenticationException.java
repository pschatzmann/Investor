package ch.pschatzmann.stocks.errors;

/**
 * Exceptions related to authorizations
 * 
 * @author pschatzmann
 *
 */
public class AuthenticationException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AuthenticationException(String error){
		super(error);
	}
}
