package de.uniba.wiai.lspi.ws1213.ba.application;

import java.util.Properties;

/**
 * This class contains the descriptive data for a single found reference type
 * violation. The violation occurred for <code>reference</code> of a BPMN
 * <code>element</code> in a <code>line</code> and has the
 * <code>incorrectType</code> and the <code>expectedType</code>. It is one of
 * the implementations of the violation interface and therefore creates a
 * violation message from the given data. Reference type violation means, that
 * the type of the referenced element (which is its name) is incompatible with
 * the types requested by the BPMN standard.
 * 
 * @author Andreas Vorndran
 * @version 1.0
 * @see Violation
 */
public class TypeViolation implements Violation {

	private String element;
	private int line;
	private String reference;
	private String incorrectType;
	private String expectedType;
	private Properties language;

	/**
	 * Constructor
	 * 
	 * @param element
	 *            the name of the BPMN element, where the violation occurred
	 * @param line
	 *            the line of the file, where the violation occurred
	 * @param reference
	 *            the name of the reference, where the violation occurred
	 * @param incorrectType
	 *            the incorrect type, which caused the violation
	 * @param expectedType
	 *            the types expected instead of the incorrect type
	 * @param language
	 *            the reference to the language properties (for the violation
	 *            message)
	 */
	public TypeViolation(String element, int line, String reference,
			String incorrectType, String expectedType, Properties language) {
		this.element = element;
		this.line = line;
		this.reference = reference;
		this.incorrectType = incorrectType;
		this.expectedType = expectedType;
		this.language = language;
	}

	/**
	 * @return the element
	 */
	public String getElement() {
		return element;
	}

	/**
	 * @return the line
	 */
	public int getLine() {
		return line;
	}

	/**
	 * @return the reference
	 */
	public String getReference() {
		return reference;
	}

	/**
	 * @return the incorrectType
	 */
	public String getIncorrectType() {
		return incorrectType;
	}

	/**
	 * @return the expectedType
	 */
	public String getExpectedType() {
		return expectedType;
	}

	@Override
	public String getViolationMessage() {
		return language.getProperty("typeviolation.reference") + reference
				+ " " + language.getProperty("typeviolation.line") + line + " "
				+ language.getProperty("typeviolation.element") + element + " "
				+ language.getProperty("typeviolation.incorrecttype")
				+ incorrectType
				+ language.getProperty("typeviolation.expectedtype")
				+ expectedType + ".";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TypeViolation) {
			TypeViolation type2 = (TypeViolation) obj;
			boolean sameElement = this.element.equals(type2.getElement());
			boolean sameLine = this.line == type2.getLine();
			boolean sameReference = this.reference.equals(type2.getReference());
			boolean sameIncorrectType = this.incorrectType.equals(type2
					.getIncorrectType());
			if (sameElement && sameLine && sameReference && sameIncorrectType) {
				return true;
			}
		}
		return false;
	}
}
