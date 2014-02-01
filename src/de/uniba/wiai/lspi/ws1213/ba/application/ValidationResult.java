package de.uniba.wiai.lspi.ws1213.ba.application;

import java.util.List;

/**
 * This class contains the result of a validation run for one file. If the file
 * is not valid there is a list of violations, which can be used to get the
 * violation messages.
 * 
 * @author Andreas Vorndran
 * @version 1.0
 * @see BPMNReferenceValidatorImpl
 * 
 */
public class ValidationResult {

	private String filePath;
	private boolean valid;
	private List<Violation> violations;

	/**
	 * Constructor
	 * 
	 * @param filePath
	 *            the path of the validated file
	 * @param valid
	 *            are violations found?
	 * @param violations
	 *            the found violations or null
	 */
	public ValidationResult(String filePath, boolean valid,
			List<Violation> violations) {
		this.filePath = filePath;
		this.valid = valid;
		this.violations = violations;
	}

	/**
	 * @return the filePath
	 */
	public String getFilePath() {
		return filePath;
	}

	/**
	 * @return the valid
	 */
	public boolean isValid() {
		return valid;
	}

	/**
	 * @return the violations
	 */
	public List<Violation> getViolations() {
		return violations;
	}
}
