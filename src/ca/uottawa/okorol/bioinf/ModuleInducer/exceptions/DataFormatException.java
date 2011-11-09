package ca.uottawa.okorol.bioinf.ModuleInducer.exceptions;

public class DataFormatException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public DataFormatException(String msg) {
		super(msg);
	}

	public DataFormatException(String msg, Throwable t) {
		super(msg, t);
	}

}
