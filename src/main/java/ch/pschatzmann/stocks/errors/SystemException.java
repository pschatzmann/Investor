package ch.pschatzmann.stocks.errors;

/**
 * Generic System problems
 * 
 * @author pschatzmann
 *
 */
public class SystemException extends CommonException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SystemException(Exception ex) {
		super(ex);
	}

}
