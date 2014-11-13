package de.uniba.wiai.lspi.ws1213.ba.application;

/**
 * This exception encapsulates technology specific exceptions and is thrown by general errors.
 * 
 * @author Andreas Vorndran
 * @version 1.0
 * 
 */
public class ValidatorException extends Exception {

	private static final long serialVersionUID = -5558139552907672510L;

	/**
	 * Constructor for use without a causing exception
	 * 
	 * @param message
	 *            the error message
	 */
	public ValidatorException(String message) {
		super(message);
	}

	/**
	 * Constructor for use with a causing exception
	 * 
	 * @param message
	 *            the error message
	 * @param cause
	 *            the exception causes this validator exception
	 */
	public ValidatorException(String message, Throwable cause) {
		super(message, cause);
	}
}
