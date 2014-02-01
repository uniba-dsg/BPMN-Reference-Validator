package de.uniba.wiai.lspi.ws1213.ba.application;

import java.util.Properties;

/**
 * This class contains the descriptive data for a found existence violation of a
 * reference. It is one of the implementations of the violation interface and
 * therefore creates a violation message from the given data. Existence
 * violation means, that the referenced element could not be found.
 * 
 * @author Andreas Vorndran
 * @version 1.0
 * @see Violation
 * 
 */
public class ExistenceViolation implements Violation {

	private String element;
	private String reference;
	private int line;
	private int textVersion;
	private String additionalInfo;
	private Properties language;

	public static final int DEFAULT = 0;
	public static final int PREFIX = 1;

	/**
	 * Constructor
	 * 
	 * @param element
	 *            the name of the BPMN element, where the violation occurred
	 * @param reference
	 *            the name of the reference, where the violation occurred
	 * @param line
	 *            the line of the file, where the violation occurred
	 * @param textVersion
	 *            the version of the violation message text (use constants:
	 *            DEFAULT, PREFIX)
	 * @param additionalInfo
	 *            use for additional information for special violation message
	 *            text versions. For the DEFAULT version it is null, for the
	 *            PREFIX it is the prefix of the namespace.
	 * @param language
	 *            the reference to the language properties (for the violation
	 *            message)
	 */
	public ExistenceViolation(String element, String reference, int line,
			int textVersion, String additionalInfo, Properties language) {
		this.element = element;
		this.reference = reference;
		this.line = line;
		this.textVersion = textVersion;
		this.additionalInfo = additionalInfo;
		this.language = language;
	}

	/**
	 * @return the element
	 */
	public String getElement() {
		return element;
	}

	/**
	 * @return the reference
	 */
	public String getReference() {
		return reference;
	}

	/**
	 * @return the line
	 */
	public int getLine() {
		return line;
	}

	@Override
	public String getViolationMessage() {
		if (textVersion == DEFAULT) {
			return language.getProperty("existenceviolation.line")
					+ line
					+ " "
					+ language
							.getProperty("existenceviolation.reference.default")
					+ reference + " "
					+ language.getProperty("existenceviolation.element")
					+ element + " "
					+ language.getProperty("existenceviolation.end.default");
		} else if (textVersion == PREFIX) {
			return language.getProperty("existenceviolation.line")
					+ line
					+ " "
					+ language
							.getProperty("existenceviolation.reference.prefix")
					+ reference + " "
					+ language.getProperty("existenceviolation.element")
					+ element + " "
					+ language.getProperty("existenceviolation.prefix")
					+ additionalInfo + " "
					+ language.getProperty("existenceviolation.end.prefix");
		} else {
			return null;
		}
	}
}
