package ch.pschatzmann.stocks.errors;

public class UniverseException extends CommonException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UniverseException(Exception ex) {
		super(ex);
	}

	public UniverseException(String msg) {
		super(msg);
	}

}
