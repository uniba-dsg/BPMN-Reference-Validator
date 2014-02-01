package de.uniba.wiai.lspi.ws1213.ba.application;

/**
 * This Interface specifies the general form of a violation. Every violation has
 * a violation message for the user to understand where and why there was this
 * violation found.
 * 
 * @author Andreas Vorndran
 * @version 1.0
 * @see ExistenceViolation
 * @see TypeViolation
 * 
 */
public interface Violation {

	/**
	 * @return the violation in a String message beginning with the violation
	 *         type (e.g. existing, reference type)
	 */
	String getViolationMessage();
}
