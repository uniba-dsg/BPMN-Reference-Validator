package de.uniba.wiai.lspi.ws1213.ba.application;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.Filter;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.located.LocatedElement;
import org.jdom2.located.LocatedJDOMFactory;
import org.jdom2.util.IteratorIterable;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import de.uniba.wiai.lspi.ws1213.ba.application.xsdvalidation.ResourceResolver;

/**
 * The implementation of the BPMNReferenceValidator. For more information and an
 * example: {@link BPMNReferenceValidator}.
 * 
 * @author Andreas Vorndran
 * @version 1.0
 * @see BPMNReferenceValidator
 * @see ValidationResult
 * @see ValidatorException
 * 
 */
public class BPMNReferenceValidatorImpl implements BPMNReferenceValidator {

	private Level level;
	private Properties language;
	private File bpmnFile;
	private HashMap<String, BPMNElement> bpmnElements;
	private HashMap<String, BPMNFileTree> importedFiles;
	private Logger LOGGER;
	private List<SAXParseException> XSDErrorList;
	private String ownPrefix;

	public static final int ENGLISH = 0;
	public static final int GERMAN = 1;

	/**
	 * Constructor sets the defaults. Log level = OFF and language = ENGLISH.
	 * 
	 * @throws ValidatorException
	 *             if problems with the language files exist
	 */
	public BPMNReferenceValidatorImpl() throws ValidatorException {
		setLanguage(ENGLISH);
		setLogLevel(Level.OFF);
	}

	@Override
	public List<ValidationResult> validate(String path)
			throws ValidatorException {
		List<ValidationResult> results = new ArrayList<>();
		List<Violation> violations = startValidation(path, "referenceType");
		boolean valid = violations.size() == 0;
		results.add(new ValidationResult(path, valid, violations));
		List<String> importedFilePaths = getImportedFilePaths();
		for (String filePath : importedFilePaths) {
			List<Violation> importedFileViolations = startValidation(filePath,
					"referenceType");
			valid = importedFileViolations.size() == 0;
			results.add(new ValidationResult(filePath, valid,
					importedFileViolations));
		}
		return results;
	}

	@Override
	public List<ValidationResult> validateExistenceOnly(String path)
			throws ValidatorException {
		List<ValidationResult> results = new ArrayList<>();
		List<Violation> violations = startValidation(path, "existence");
		boolean valid = violations.size() == 0;
		results.add(new ValidationResult(path, valid, violations));
		List<String> importedFilePaths = getImportedFilePaths();
		for (String filePath : importedFilePaths) {
			List<Violation> importedFileViolations = startValidation(filePath,
					"existence");
			valid = importedFileViolations.size() == 0;
			results.add(new ValidationResult(filePath, valid,
					importedFileViolations));
		}
		return results;
	}

	@Override
	public ValidationResult validateSingleFile(String path)
			throws ValidatorException {
		List<Violation> violations = startValidation(path, "referenceType");
		boolean valid = violations.size() == 0;
		ValidationResult result = new ValidationResult(path, valid, violations);
		getImportedFilePaths(); // for logging
		return result;
	}

	@Override
	public ValidationResult validateSingleFileExistenceOnly(String path)
			throws ValidatorException {
		List<Violation> violations = startValidation(path, "existence");
		boolean valid = violations.size() == 0;
		ValidationResult result = new ValidationResult(path, valid, violations);
		getImportedFilePaths(); // for logging
		return result;
	}

	@Override
	public void setLogLevel(Level level) {
		if (LOGGER != null) {
			LOGGER.setLevel(level);
		}
		this.level = level;
	}

	@Override
	public void setLanguage(int languageNumber) throws ValidatorException {
		if (languageNumber == ENGLISH) {
			language = new Properties();
			try {
				language.load(getClass().getResourceAsStream("/en.lang"));
			} catch (FileNotFoundException e) {
				throw new ValidatorException(
						"Could not find the language file 'lang/en.lang'.", e);
			} catch (IOException e) {
				throw new ValidatorException(
						"IO problems with the language file 'lang/en.lang'.", e);
			}
		} else if (languageNumber == GERMAN) {
			language = new Properties();
			try {
				language.load(getClass().getResourceAsStream("/ger.lang"));
			} catch (FileNotFoundException e) {
				throw new ValidatorException(
						"Die Sprachdatei 'lang/ger.lang' kann nicht gefunden werden.",
						e);
			} catch (IOException e) {
				throw new ValidatorException(
						"Es sind I/O-Probleme bei der Sprachdatei 'lang/ger.lang' aufgetreten.",
						e);
			}
		} else {
			throw new ValidatorException(
					"The desired language is not available. Please choose ENGLISH or GERMAN.");
		}
	}

	/**
	 * This method loads the BPMN elements with the checkable references for the
	 * validation.
	 * 
	 * @throws ValidatorException
	 *             if technical problems with the files "references.xml" and
	 *             "references.xsd" occurred
	 */
	private void loadReferences() throws ValidatorException {
		LOGGER = ValidationLoggerFactory.createLogger(null, level, language);
		ReferenceLoader referenceLoader = new ReferenceLoader(language, LOGGER);
		bpmnElements = referenceLoader.load("/references.xml",
				"/references.xsd");
		String bpmnElementsLogText = "";
		for (String key : bpmnElements.keySet()) {
			bpmnElementsLogText = bpmnElementsLogText + key + " :: "
					+ bpmnElements.get(key) + System.lineSeparator();
		}
		LOGGER.info(language.getProperty("validator.logger.bpmnelements")
				+ System.lineSeparator() + bpmnElementsLogText);
	}

	/**
	 * This method validates the BPMN file given through the path for the given
	 * validation level. It is the entrance point for the validation. Therefore
	 * it is used by the public methods of the interface.
	 * 
	 * @param path
	 *            the path of the file, which should be validated
	 * @param validationLevel
	 *            the validation level as String: "existence" or "referenceType"
	 * @return a list of violations, which can be empty if no violations were
	 *         found
	 * @throws ValidatorException
	 *             if technical problems occurred
	 */
	private List<Violation> startValidation(String path, String validationLevel)
			throws ValidatorException {
		// preparations
		if (bpmnElements == null) {
			loadReferences();
		}
		importedFiles = new HashMap<>();
		List<Violation> violationList = new ArrayList<>();
		SAXBuilder builder = new SAXBuilder();
		builder.setJDOMFactory(new LocatedJDOMFactory());
		bpmnFile = new File(path);
		LOGGER = ValidationLoggerFactory.createLogger(bpmnFile.getName(),
				level, language);
		try {
			// XSD validation
			validateFileAgainstXSD(bpmnFile);
			// load and traverse the file to get all elements with IDs and all
			// imports
			Document document = (Document) builder.build(bpmnFile);
			HashMap<String, Element> elements = getAllElements(document, true,
					null, LOGGER);
			LOGGER.info(language.getProperty("validator.logger.elements")
					+ System.lineSeparator() + elements.toString());
			String importedFilesLogText = "";
			for (String key : importedFiles.keySet()) {
				importedFilesLogText = importedFilesLogText
						+ language.getProperty("validator.logger.prefix") + key
						+ System.lineSeparator() + importedFiles.get(key)
						+ System.lineSeparator();
			}
			LOGGER.info(language.getProperty("validator.logger.importedfiles")
					+ System.lineSeparator() + importedFilesLogText);
			// get all elements of the file for validate their references
			Filter<Element> filter = Filters.element();
			IteratorIterable<Element> list = document.getDescendants(filter);
			while (list.hasNext()) {
				Element currentElement = list.next();
				String currentName = currentElement.getName();
				// proof if the current element can have references
				if (bpmnElements.containsKey(currentName)) {
					BPMNElement bpmnElement = bpmnElements.get(currentName);
					// create list of all inherited elements and their
					// references
					List<BPMNElement> checkingBPMNElements = new ArrayList<>();
					checkingBPMNElements.add(bpmnElement);
					while (bpmnElement.getParent() != null) {
						bpmnElement = bpmnElements.get(bpmnElement.getParent());
						checkingBPMNElements.add(bpmnElement);
					}
					// proof each possible reference
					for (BPMNElement checkingElement : checkingBPMNElements) {
						for (Reference checkingReference : checkingElement
								.getReferences()) {
							LOGGER.info(language
									.getProperty("validator.logger.checkingreference")
									+ System.lineSeparator()
									+ currentName
									+ "  ::   " + checkingReference);
							// try to get the reference ID
							String referencedId = null;
							int line = -1;
							if (checkingReference.isAttribute()) {
								referencedId = currentElement
										.getAttributeValue(checkingReference
												.getName());
								line = ((LocatedElement) currentElement)
										.getLine();
							} else {
								for (Element child : currentElement
										.getChildren()) {
									if (child.getName().equals(
											checkingReference.getName())) {
										referencedId = child.getText();
										line = ((LocatedElement) child)
												.getLine();
										break;
									}
								}
							}
							// if the current element has the reference start
							// the validation
							if (referencedId != null) {
								if ("existence".equals(validationLevel)) {
									validateExistence(elements, violationList,
											currentName, line,
											checkingReference, referencedId);
								} else if ("referenceType"
										.equals(validationLevel)) {
									validateReferenceType(elements,
											violationList, currentElement,
											line, checkingReference,
											referencedId);
								} else {
									LOGGER.severe(language
											.getProperty("validator.illegalargument.validatinglevel.part1")
											+ validationLevel
											+ " "
											+ language
													.getProperty("validator.illegalargument.validatinglevel.part2"));
									throw new ValidatorException(
											language.getProperty("validator.illegalargument.validatinglevel.part1")
													+ validationLevel
													+ " "
													+ language
															.getProperty("validator.illegalargument.validatinglevel.part2"));
								}
							}
						}
					}
				}
			}
			// log violations
			if (violationList.size() > 0) {
				String violationListLogText = "";
				for (Violation violation : violationList) {
					violationListLogText = violationListLogText
							+ violation.getViolationMessage()
							+ System.lineSeparator();
				}
				LOGGER.info(language
						.getProperty("validator.logger.violationlist")
						+ System.lineSeparator() + violationListLogText);
			}
		} catch (JDOMException e) {
			LOGGER.severe(language.getProperty("validator.jdom") + path);
			throw new ValidatorException(language.getProperty("validator.jdom")
					+ path, e);
		} catch (IOException e) {
			LOGGER.severe(language.getProperty("validator.io") + path);
			throw new ValidatorException(language.getProperty("validator.io")
					+ path, e);
		}
		return violationList;
	}

	/**
	 * This method puts all elements with an id found in the given document into
	 * a hash map. The key is the id, the value the element. The method also
	 * loads the imported BPMN files and puts them into a tree structure (the
	 * field <code>importedFiles</code>).
	 * 
	 * @param document
	 *            the document to get the elements from
	 * @param root
	 *            a flag for the root document (only the root document uses the
	 *            <code>importedFiles</code> directly with the prefixes)
	 * @param parentTree
	 *            the parent tree of the document to get the elements; only
	 *            available if not root
	 * @param LOGGER
	 *            the logger to use
	 * @return the hash map with elements reachable through their id
	 * @throws ValidatorException
	 */
	private HashMap<String, Element> getAllElements(Document document,
			boolean root, BPMNFileTree parentTree, Logger LOGGER)
			throws ValidatorException {
		HashMap<String, Element> elements = new HashMap<>();
		Element rootNode = document.getRootElement();
		Filter<Element> filter = Filters.element();
		IteratorIterable<Element> list = rootNode.getDescendants(filter);
		while (list.hasNext()) {
			Element element = list.next();
			Attribute id = element.getAttribute("id");
			// special handling for imports
			if (element.getName().equals("import")) {
				handleImport(root, parentTree, rootNode, element, LOGGER);
			}
			// put the element if it has an id
			if (id != null) {
				String id_value = id.getValue();
				if (id_value != null && !id_value.equals("")) {
					elements.put(id_value, element);
				}
			}
		}
		if (root) {
			ownPrefix = "";
			// special case if a prefix is used for the target namespace
			String targetNamespace = rootNode
					.getAttributeValue("targetNamespace");
			for (Namespace namespace : rootNode.getNamespacesInScope()) {
				if (targetNamespace.equals(namespace.getURI())) {
					ownPrefix = namespace.getPrefix();
					break;
				}
			}
		}
		return elements;
	}

	/**
	 * This is a helper method for handling imports while traversing the
	 * elements.
	 * 
	 * @param root
	 *            a flag for the root document (only the root document uses the
	 *            <code>importedFiles</code> directly with the prefixes)
	 * @param parentTree
	 *            the parent tree of the document to get the elements; only
	 *            available if not root
	 * @param rootNode
	 *            the root element of the document
	 * @param element
	 *            the import element
	 * @param LOGGER
	 *            the logger to use
	 * @throws ValidatorException
	 */
	private void handleImport(boolean root, BPMNFileTree parentTree,
			Element rootNode, Element element, Logger LOGGER)
			throws ValidatorException {
		if ("http://www.omg.org/spec/BPMN/20100524/MODEL".equals(element
				.getAttributeValue("importType"))) {
			String location = element.getAttributeValue("location");
			location = identifyPath(location);
			// special handling for the root document: start to build up the
			// prefix trees
			if (root) {
				String namespace = element.getAttributeValue("namespace");
				if (location != null && !location.equals("")
						&& namespace != null && !namespace.equals("")) {
					// getting the namespace acronym
					List<Namespace> rootNamespaces = rootNode
							.getNamespacesInScope();
					String prefix = "";
					for (Namespace rootNamespace : rootNamespaces) {
						if (rootNamespace.getURI().equals(namespace)) {
							prefix = rootNamespace.getPrefix();
							break;
						}
					}
					// join the files if they have the same namespace
					if (importedFiles.containsKey(prefix)) {
						BPMNFileTree tree = importedFiles.get(prefix);
						File file = new File(location);
						validateFileAgainstXSD(file);
						Document treeDocument = getDocumentFromFile(file,
								location);
						HashMap<String, Element> treeElements = getAllElements(
								treeDocument, false, tree, LOGGER);
						boolean success = tree.joinElements(treeElements,
								location, false);
						if (!success) {
							LOGGER.severe(language
									.getProperty("validator.illegalargument.namespace.part1")
									+ namespace
									+ " "
									+ language
											.getProperty("validator.illegalargument.namespace.part2"));
							throw new ValidatorException(
									language.getProperty("validator.illegalargument.namespace.part1")
											+ namespace
											+ " "
											+ language
													.getProperty("validator.illegalargument.namespace.part2"));
						}
						// normal handling of root imports (files do not have
						// the same namespace)
					} else {
						File file = new File(location);
						validateFileAgainstXSD(file);
						Document treeDocument = getDocumentFromFile(file,
								location);
						BPMNFileTree tree = new BPMNFileTree(null, location,
								language);
						HashMap<String, Element> treeElements = getAllElements(
								treeDocument, false, tree, LOGGER);
						tree.setElements(treeElements);
						importedFiles.put(prefix, tree);
					}

				}
				// !root: the normal handling of imports with put it into the
				// tree structure
			} else {
				File file = new File(location);
				validateFileAgainstXSD(file);
				String fileName = file.getName();
				BPMNFileTree rootTree = parentTree;
				while (rootTree.getParentTree() != null) {
					rootTree = rootTree.getParentTree();
				}
				if (!fileName.equals(bpmnFile.getName())
						&& !rootTree.contains(fileName)) {
					Document treeDocument = getDocumentFromFile(file, location);
					BPMNFileTree tree = new BPMNFileTree(parentTree, location,
							language);
					HashMap<String, Element> treeElements = getAllElements(
							treeDocument, false, tree, LOGGER);
					tree.setElements(treeElements);
					parentTree.addChild(tree);
				}
			}
		}
	}

	/**
	 * This helper method encapsulates the document building from a given file.
	 * 
	 * @param file
	 *            the file to get the document from
	 * @param location
	 *            the location of the file for the error message
	 * @return the document of the file
	 * @throws ValidatorException
	 *             if problems occurred while loading or traversing the file
	 */
	private Document getDocumentFromFile(File file, String location)
			throws ValidatorException {
		SAXBuilder builder = new SAXBuilder();
		try {
			return (Document) builder.build(file);
		} catch (JDOMException e) {
			LOGGER.severe(language.getProperty("validator.jdom.imported.part1")
					+ location
					+ language.getProperty("validator.jdom.imported.part2"));
			throw new ValidatorException(
					language.getProperty("validator.jdom.imported.part1")
							+ location
							+ language
									.getProperty("validator.jdom.imported.part2"),
					e);
		} catch (IOException e) {
			LOGGER.severe(language.getProperty("validator.io.imorted.part1")
					+ location
					+ language.getProperty("validator.io.imorted.part2"));
			throw new ValidatorException(
					language.getProperty("validator.io.imorted.part1")
							+ location
							+ language
									.getProperty("validator.io.imorted.part2"),
					e);
		}
	}

	/**
	 * This method checks the path and completes it if necessary. It maybe tries
	 * to add a given relative path to the path of the root file (=
	 * <code>bpmnFile</code>).
	 * 
	 * @param location
	 *            the path to check
	 * @return the identified absolute path
	 */
	private String identifyPath(String location) {
		// TODO check/refactor to platform independency
		String path = "";
		if (location.startsWith("\\") || location.startsWith("/")) {
			location = location.substring(1);
		}
		if (location.contains("/")) {
			String[] pathElements = location.split("/");
			if (Pattern.matches("[a-zA-Z]:", pathElements[0])) {
				path = location;
			} else {
				path = checkRelativPath(location, pathElements);
			}
		} else if (location.contains("\\")) {
			String[] pathElements = location.split("\\\\");
			if (Pattern.matches("[a-zA-Z]:", pathElements[0])) {
				path = location;
			} else {
				path = checkRelativPath(location, pathElements);
			}
		} else {
			path = bpmnFile.getParent() + File.separator + location;
		}

		return path;
	}

	/**
	 * This method checks the relative path and completes it. For that it tries
	 * to find the relative starting point of the given path in the path of the
	 * root file (= <code>bpmnFile</code>).
	 * 
	 * @param location
	 *            the relative path
	 * @param pathElements
	 *            the parts of location
	 * @return the absolute path
	 */
	private String checkRelativPath(String location, String[] pathElements) {
		// TODO check/refactor to platform independency
		String path = "";
		String[] pathBPMNFile = bpmnFile.getParent().split("\\\\");
		int number = -1;
		for (int i = 0; i < pathBPMNFile.length; i++) {
			if (pathBPMNFile[i].equals(pathElements[0])) {
				number = i;
				int counter = 1;
				for (int j = i + 1; j < pathBPMNFile.length; j++) {
					if (counter == pathElements.length
							|| !pathBPMNFile[j].equals(pathElements[counter])) {
						number = -1;
						break;
					}
					counter++;
				}
				break;
			}
		}
		if (number == -1) {
			path = bpmnFile.getParent() + "\\" + location.replace('/', '\\');
		} else {
			for (int i = 0; i < number; i++) {
				path = path + pathBPMNFile[i] + "\\";
			}
			path = path + location.replace('/', '\\');
		}
		return path;
	}

	/**
	 * This method validates the current element against the checking reference.
	 * Some already existing information will be expected as parameters.
	 * 
	 * @param elements
	 *            the elements of the root file (= <code>bpmnFile</code>)
	 * @param violationList
	 *            the violation list for adding found violations
	 * @param currentElement
	 *            the current element to validate
	 * @param line
	 *            the line of the reference in the root file for the violation
	 *            message
	 * @param checkingReference
	 *            the reference to validate against
	 * @param referencedId
	 *            the referenced ID
	 */
	private void validateReferenceType(HashMap<String, Element> elements,
			List<Violation> violationList, Element currentElement, int line,
			Reference checkingReference, String referencedId) {
		if (checkingReference.isQname()) {
			// reference ID is prefixed and therefore probably to find in an
			// imported file
			if (referencedId.contains(":")) {
				String[] parts = referencedId.split(":");
				String prefix = parts[0];
				String importedId = parts[1];
				BPMNFileTree currentTree = importedFiles.get(prefix);
				// case if the namespace is used for the root file and an
				// imported file
				if (ownPrefix.equals(prefix) && currentTree != null) {
					// case if the element could not be found in the root file
					// and the imported file
					if (!elements.containsKey(importedId)
							&& currentTree.getElement(importedId) == null) {
						addExistenceViolation(violationList, line,
								currentElement.getName(), checkingReference);
						// case if the element could be found in the root file
						// (and perhaps in the imported file, which does not
						// matter because of the same namespace)
					} else if (elements.containsKey(importedId)) {
						Element referencedElement = elements.get(importedId);
						checkTypeAndAddViolation(violationList, line,
								currentElement, checkingReference,
								referencedElement);
						// case if the element could be found only in the
						// imported file
					} else {
						Element referencedElement = currentTree
								.getElement(importedId);
						checkTypeAndAddViolation(violationList, line,
								currentElement, checkingReference,
								referencedElement);
					}
					// case if the namespace is only used for the root file
				} else if (ownPrefix.equals(prefix)) {
					if (!elements.containsKey(importedId)) {
						addExistenceViolation(violationList, line,
								currentElement.getName(), checkingReference);
					} else {
						Element referencedElement = elements.get(importedId);
						checkTypeAndAddViolation(violationList, line,
								currentElement, checkingReference,
								referencedElement);
					}
					// case if the namespace is not used for the roor file or an
					// imported file
				} else if (currentTree == null) {
					// import does not exist or is no BPMN file (as it has to
					// be)
					Violation violation = new ExistenceViolation(
							currentElement.getName(),
							checkingReference.getName(), line,
							ExistenceViolation.PREFIX, prefix, language);
					violationList.add(violation);
					// case if the namespace is used by an imported file
				} else {
					Element referencedElement = currentTree
							.getElement(importedId);
					if (referencedElement == null) {
						addExistenceViolation(violationList, line,
								currentElement.getName(), checkingReference);
					} else {
						checkTypeAndAddViolation(violationList, line,
								currentElement, checkingReference,
								referencedElement);
					}
				}
				// the referenced element has no prefix and therefore is to find
				// in the root file
			} else {
				if (!elements.containsKey(referencedId)) {
					addExistenceViolation(violationList, line,
							currentElement.getName(), checkingReference);
				} else {
					Element referencedElement = elements.get(referencedId);
					checkTypeAndAddViolation(violationList, line,
							currentElement, checkingReference,
							referencedElement);
				}
			}
			// checking reference is IDREF (!QName) and therefore is to find in
			// the root file
		} else {
			if (!elements.containsKey(referencedId)) {
				addExistenceViolation(violationList, line,
						currentElement.getName(), checkingReference);
			} else {
				Element referencedElement = elements.get(referencedId);
				checkTypeAndAddViolation(violationList, line, currentElement,
						checkingReference, referencedElement);
			}
		}
	}

	/**
	 * This method validates that the checking reference of the current element
	 * refers to an existing element. Some already existing information will be
	 * expected as parameters.
	 * 
	 * @param elements
	 *            the elements of the root file (= <code>bpmnFile</code>)
	 * @param violationList
	 *            the violation list for adding found violations
	 * @param currentName
	 *            the name of the current element
	 * @param line
	 *            the line of the reference in the root file for the violation
	 *            message
	 * @param checkingReference
	 *            the reference to validate against
	 * @param referencedId
	 *            the referenced ID
	 */
	private void validateExistence(HashMap<String, Element> elements,
			List<Violation> violationList, String currentName, int line,
			Reference checkingReference, String referencedId) {
		if (checkingReference.isQname()) {
			// reference ID is prefixed and therefore probably to find in an
			// imported file
			if (referencedId.contains(":")) {
				String[] parts = referencedId.split(":");
				String prefix = parts[0];
				String importedId = parts[1];
				BPMNFileTree currentTree = importedFiles.get(prefix);
				// case if the namespace is used for the root file and an
				// imported file
				if (ownPrefix.equals(prefix) && currentTree != null) {
					if (!elements.containsKey(importedId)
							&& currentTree.getElement(importedId) == null) {
						addExistenceViolation(violationList, line, currentName,
								checkingReference);
					}
					// case if the namespace is only used for the root file
				} else if (ownPrefix.equals(prefix)) {
					if (!elements.containsKey(importedId)) {
						addExistenceViolation(violationList, line, currentName,
								checkingReference);
					}
					// case if the namespace is not used for the roor file or an
					// imported file
				} else if (currentTree == null) {
					// import does not exist or is no BPMN file (as it has to
					// be)
					Violation violation = new ExistenceViolation(currentName,
							checkingReference.getName(), line,
							ExistenceViolation.PREFIX, prefix, language);
					violationList.add(violation);
					// case if the namespace is used by an imported file
				} else {
					Element referencedElement = currentTree
							.getElement(importedId);
					if (referencedElement == null) {
						addExistenceViolation(violationList, line, currentName,
								checkingReference);
					}
				}
				// the referenced element has no prefix and therefore is to find
				// in the root file
			} else {
				if (!elements.containsKey(referencedId)) {
					addExistenceViolation(violationList, line, currentName,
							checkingReference);
				}
			}
			// checking reference is IDREF (!QName) and therefore is to find in
			// the root file
		} else {
			if (!elements.containsKey(referencedId)) {
				addExistenceViolation(violationList, line, currentName,
						checkingReference);
			}
		}
	}

	/**
	 * This method validates the type of the referenced element against the
	 * checking reference and adds a violation if found.
	 * 
	 * @param violationList
	 *            the violation list for adding found violations
	 * @param line
	 *            the line of the reference in the root file for the violation
	 *            message
	 * @param currentElement
	 *            the current element to validate
	 * @param checkingReference
	 *            the reference to validate against
	 * @param referencedElement
	 *            the referenced element to validate
	 */
	private void checkTypeAndAddViolation(List<Violation> violationList,
			int line, Element currentElement, Reference checkingReference,
			Element referencedElement) {
		boolean foundType = false;
		ArrayList<String> referencedTypes = checkingReference.getTypes();
		// do not check references which are only for existence validation
		if (referencedTypes != null) {
			// find all possible types (with subtypes/children)
			@SuppressWarnings("unchecked")
			ArrayList<String> types = (ArrayList<String>) referencedTypes
					.clone();
			boolean childfound = false;
			do {
				childfound = false;
				@SuppressWarnings("unchecked")
				ArrayList<String> typesCopy = (ArrayList<String>) types.clone();
				for (String type : typesCopy) {
					if (bpmnElements.containsKey(type)) {
						BPMNElement bpmnElement = bpmnElements.get(type);
						List<String> children = bpmnElement.getChildren();
						if (children != null) {
							for (String child : children) {
								if (!typesCopy.contains(child)) {
									types.add(child);
									childfound = true;
								}
							}
						}
					}
				}
			} while (childfound);
			// validate if the referenced element has one of the correct types
			for (String type : types) {
				if (referencedElement.getName().equals(type)) {
					foundType = true;
					break;
				}
			}
			if (!foundType) {
				Violation violation = new TypeViolation(
						currentElement.getName(), line,
						checkingReference.getName(),
						referencedElement.getName(), types.toString(), language);
				violationList.add(violation);
			} else {
				// special cases for additional checks (look up bachelor thesis
				// for the reference number)
				if (checkingReference.isSpecial()) {
					if (checkingReference.getNumber() == 18
							|| checkingReference.getNumber() == 19) {
						String isEventSubprocess = referencedElement
								.getAttributeValue("triggeredByEvent");
						if (isEventSubprocess != null
								&& isEventSubprocess.equals("true")) {
							Violation violation = new TypeViolation(
									currentElement.getName(), line,
									checkingReference.getName(),
									"Event Sub-Process", types.toString(),
									language);
							violationList.add(violation);
						}
					} else if (checkingReference.getNumber() == 62) {
						Element parent = currentElement.getParentElement();
						if (parent != null) {
							ArrayList<String> tasks = getBPMNTasksList();
							ArrayList<String> subprocesses = getBPMNSubProcessList();
							if (tasks.contains(parent.getName())) {
								if (!referencedElement.getName().equals(
										"dataInput")) {
									Violation violation = new TypeViolation(
											currentElement.getName(),
											line,
											checkingReference.getName(),
											referencedElement.getName(),
											language.getProperty("validator.special.62.version1"),
											language);
									violationList.add(violation);
								}
							} else if (subprocesses.contains(parent.getName())) {
								if (!referencedElement.getName().equals(
										"dataObject")) {
									Violation violation = new TypeViolation(
											currentElement.getName(),
											line,
											checkingReference.getName(),
											referencedElement.getName(),
											language.getProperty("validator.special.62.version2"),
											language);
									violationList.add(violation);
								}
							}
						}
					} else if (checkingReference.getNumber() == 63) {
						Element parent = currentElement.getParentElement();
						if (parent != null) {
							ArrayList<String> tasks = getBPMNTasksList();
							ArrayList<String> subprocesses = getBPMNSubProcessList();
							if (tasks.contains(parent.getName())) {
								if (!referencedElement.getName().equals(
										"dataOutput")) {
									Violation violation = new TypeViolation(
											currentElement.getName(),
											line,
											checkingReference.getName(),
											referencedElement.getName(),
											language.getProperty("validator.special.63.version1"),
											language);
									violationList.add(violation);
								}
							} else if (subprocesses.contains(parent.getName())) {
								if (!referencedElement.getName().equals(
										"dataObject")) {
									Violation violation = new TypeViolation(
											currentElement.getName(),
											line,
											checkingReference.getName(),
											referencedElement.getName(),
											language.getProperty("validator.special.63.version2"),
											language);
									violationList.add(violation);
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * @return a list with BPMN task and its subtypes
	 */
	private ArrayList<String> getBPMNTasksList() {
		ArrayList<String> tasks = new ArrayList<>();
		tasks.add("task");
		tasks.add("serviceTask");
		tasks.add("sendTask");
		tasks.add("receiveTask");
		tasks.add("userTask");
		tasks.add("manualTask");
		tasks.add("scriptTask");
		tasks.add("businessRuleTask");
		return tasks;
	}

	/**
	 * @return a list with BPMN subProcesses
	 */
	private ArrayList<String> getBPMNSubProcessList() {
		ArrayList<String> subprocesses = new ArrayList<>();
		subprocesses.add("subProcess");
		subprocesses.add("adHocSubProcess");
		return subprocesses;
	}

	/**
	 * Adds a found existence violation to the list.
	 * 
	 * @param violationList
	 *            the violation list for adding the found violation
	 * @param line
	 *            the line of the reference in the root file
	 * @param currentName
	 *            the name of the element causes the violation
	 * @param checkingReference
	 *            the violated reference
	 */
	private void addExistenceViolation(List<Violation> violationList, int line,
			String currentName, Reference checkingReference) {
		Violation violation = new ExistenceViolation(currentName,
				checkingReference.getName(), line, ExistenceViolation.DEFAULT,
				null, language);
		violationList.add(violation);
	}

	/**
	 * @return the paths of the <code>importedFiles</code> field
	 */
	private List<String> getImportedFilePaths() {
		List<String> importedFilePaths = new ArrayList<>();
		for (String key : importedFiles.keySet()) {
			BPMNFileTree file = importedFiles.get(key);
			for (String path : file.getPaths()) {
				if (!importedFilePaths.contains(path)) {
					importedFilePaths.add(path);
				}
			}
			for (BPMNFileTree tree : file.getAllChildren()) {
				for (String path : tree.getPaths()) {
					if (!importedFilePaths.contains(path)) {
						importedFilePaths.add(path);
					}
				}
			}
		}
		String importedFilePathsLogText = "";
		for (String filePath : importedFilePaths) {
			importedFilePathsLogText = importedFilePathsLogText + filePath
					+ System.lineSeparator();
		}
		LOGGER.info(language.getProperty("validator.logger.importedfilepaths")
				+ System.lineSeparator() + importedFilePathsLogText);
		return importedFilePaths;
	}

	/**
	 * This method validates the given BPMN file against the BPMN XSDs.
	 * 
	 * @param file
	 *            the file to validate
	 * @throws ValidatorException
	 *             if technical problems occurred or the file is not schema
	 *             valid
	 */
	private void validateFileAgainstXSD(File file) throws ValidatorException {
		XSDErrorList = new ArrayList<>();
		try {
			SchemaFactory schemaFactory = SchemaFactory
					.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			schemaFactory.setResourceResolver(new ResourceResolver());
			Schema schema = schemaFactory
					.newSchema(new Source[] {
							new StreamSource(getClass().getResourceAsStream(
									"/DC.xsd")),
							new StreamSource(getClass().getResourceAsStream(
									"/DI.xsd")),
							new StreamSource(getClass().getResourceAsStream(
									"/BPMNDI.xsd")),
							new StreamSource(getClass().getResourceAsStream(
									"/BPMN20.xsd")) });
			Validator validator = schema.newValidator();
			validator.setErrorHandler(new XSDValidationLoggingErrorHandler());
			validator.validate(new StreamSource(file));
			if (XSDErrorList.size() > 0) {
				String xsdErroText = language
						.getProperty("validator.xsd.general.part1")
						+ file.getName()
						+ language.getProperty("validator.xsd.general.part2")
						+ System.lineSeparator();
				;
				for (SAXParseException saxParseException : XSDErrorList) {
					xsdErroText = xsdErroText
							+ language.getProperty("loader.xsd.error.part1")
							+ saxParseException.getLineNumber() + " "
							+ language.getProperty("loader.xsd.error.part2")
							+ saxParseException.getMessage()
							+ System.lineSeparator();
				}
				LOGGER.severe(xsdErroText);
				throw new ValidatorException(xsdErroText);
			}
		} catch (SAXException e) {
			throw new ValidatorException(
					language.getProperty("validator.xsd.sax") + file.getName(),
					e);
		} catch (IOException e) {
			throw new ValidatorException(language.getProperty("validator.io")
					+ file.getName(), e);
		}
	}

	/**
	 * Inner class for getting the XSD violations.
	 * 
	 * @author Andreas Vorndran
	 * 
	 */
	class XSDValidationLoggingErrorHandler implements ErrorHandler {

		@Override
		public void error(SAXParseException exception) throws SAXException {
			XSDErrorList.add(exception);
		}

		@Override
		public void fatalError(SAXParseException exception) throws SAXException {
			XSDErrorList.add(exception);
		}

		@Override
		public void warning(SAXParseException exception) throws SAXException {
			XSDErrorList.add(exception);
		}

	}

}
