package ch.pschatzmann.stocks.errors;

public class TradingException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public TradingException(Exception ex){
		super(ex);
	}
	public TradingException(String ex){
		super(ex);
	}
}
