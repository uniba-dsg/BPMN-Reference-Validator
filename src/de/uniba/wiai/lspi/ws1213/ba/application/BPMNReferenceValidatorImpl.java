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

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.Filter;
import org.jdom2.filter.Filters;
import org.jdom2.located.LocatedElement;
import org.jdom2.util.IteratorIterable;

import de.uniba.wiai.lspi.ws1213.ba.application.importer.FileImporter;
import de.uniba.wiai.lspi.ws1213.ba.application.importer.ProcessFileSet;

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
	private HashMap<String, BPMNElement> bpmnRefElements;
	private Logger LOGGER;

	private final FileImporter bpmnImporter;

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
		loadReferences();

		bpmnImporter = new FileImporter(language);
	}

	@Override
	public List<ValidationResult> validate(String path)
			throws ValidatorException {
		List<ValidationResult> results = new ArrayList<>();

		ProcessFileSet fileSet = bpmnImporter.loadAllFiles(path, true);

		for (String filePath : fileSet.getProcessedFiles()) {
			ProcessFileSet fileSetImport = bpmnImporter.loadAllFiles(filePath,
					true);
			List<Violation> importedFileViolations = startValidation(
					fileSetImport, "referenceType");
			boolean valid = importedFileViolations.size() == 0;
			results.add(new ValidationResult(filePath, valid,
					importedFileViolations));
		}
		return results;
	}

	@Override
	public List<ValidationResult> validateExistenceOnly(String path)
			throws ValidatorException {
		List<ValidationResult> results = new ArrayList<>();
		ProcessFileSet fileSet = bpmnImporter.loadAllFiles(path, true);

		for (String filePath : fileSet.getProcessedFiles()) {
			ProcessFileSet fileSetImport = bpmnImporter.loadAllFiles(filePath,
					true);
			List<Violation> importedFileViolations = startValidation(
					fileSetImport, "existence");
			boolean valid = importedFileViolations.size() == 0;
			results.add(new ValidationResult(filePath, valid,
					importedFileViolations));
		}
		return results;
	}

	@Override
	public ValidationResult validateSingleFile(String path)
			throws ValidatorException {
		ProcessFileSet fileSet = bpmnImporter.loadAllFiles(path, true);
		List<Violation> violations = startValidation(fileSet, "referenceType");
		boolean valid = violations.size() == 0;
		return new ValidationResult(path, valid, violations);
	}

	@Override
	public ValidationResult validateSingleFileExistenceOnly(String path)
			throws ValidatorException {
		ProcessFileSet fileSet = bpmnImporter.loadAllFiles(path, true);
		List<Violation> violations = startValidation(fileSet, "existence");
		boolean valid = violations.size() == 0;
		return new ValidationResult(path, valid, violations);
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
		bpmnRefElements = referenceLoader.load("/references.xml",
				"/references.xsd");
		String bpmnElementsLogText = "";
		for (String key : bpmnRefElements.keySet()) {
			bpmnElementsLogText = bpmnElementsLogText + key + " :: "
					+ bpmnRefElements.get(key) + System.lineSeparator();
		}
		LOGGER.info(language.getProperty("validator.logger.bpmnelements")
				+ System.lineSeparator() + bpmnElementsLogText);
	}

	/**
	 * This method validates the BPMN file given through the path for the given
	 * validation level. It is the entrance point for the validation. Therefore
	 * it is used by the public methods of the interface.
	 * 
	 * @param fileSet
	 *            the ProcessFileSet of the file, which should be validated
	 * @param validationLevel
	 *            the validation level as String: "existence" or "referenceType"
	 * @return a list of violations, which can be empty if no violations were
	 *         found
	 * @throws ValidatorException
	 *             if technical problems occurred
	 */
	private List<Violation> startValidation(ProcessFileSet fileSet,
			String validationLevel) throws ValidatorException {

		List<Violation> violationList = new ArrayList<>();
		// TODO improve logging
		File file = new File(fileSet.getBpmnBaseFile().getBaseURI());
		LOGGER = ValidationLoggerFactory.createLogger(file.getName(), level,
				language);

		Document baseDocument = fileSet.getBpmnBaseFile();

		// Get all Elements to Check from Base BPMN Process
		HashMap<String, Element> elements = getAllElements(baseDocument);

		String ownPrefix = "";
		// special case if a prefix is used for the target namespace
		Element rootNode = baseDocument.getRootElement();
		String targetNamespace = rootNode.getAttributeValue("targetNamespace");
		for (Namespace namespace : rootNode.getNamespacesInScope()) {
			if (targetNamespace.equals(namespace.getURI())) {
				ownPrefix = namespace.getPrefix();
				break;
			}
		}

		LOGGER.fine("ownprefix after getAllElements():" + ownPrefix);

		// Store all Elements and their IDs in referenced Files into nested
		// HashMap:
		// outerKey: namespace, innerKey: Id
		HashMap<String, HashMap<String, Element>> importedElements = getAllElementsGroupedByNamespace(fileSet
				.getReferencedBpmnFiles());

		LOGGER.info(language.getProperty("validator.logger.elements")
				+ System.lineSeparator() + elements.toString());
		String importedFilesLogText = "";
		for (String key : importedElements.keySet()) {
			importedFilesLogText = importedFilesLogText
					+ language.getProperty("validator.logger.prefix") + key
					+ System.lineSeparator() + importedElements.get(key)
					+ System.lineSeparator();
		}
		LOGGER.info(language.getProperty("validator.logger.importedfiles")
				+ System.lineSeparator() + importedFilesLogText);
		// get all elements of the file for validate their references
		Filter<Element> filter = Filters.element();
		IteratorIterable<Element> list = baseDocument.getDescendants(filter);
		while (list.hasNext()) {
			Element currentElement = list.next();
			String currentName = currentElement.getName();
			// proof if the current element can have references
			if (bpmnRefElements.containsKey(currentName)) {
				BPMNElement bpmnElement = bpmnRefElements.get(currentName);
				// create list of all inherited elements and their
				// references
				List<BPMNElement> checkingBPMNElements = new ArrayList<>();
				checkingBPMNElements.add(bpmnElement);
				while (bpmnElement.getParent() != null) {
					bpmnElement = bpmnRefElements.get(bpmnElement.getParent());
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
							line = ((LocatedElement) currentElement).getLine();
						} else {
							for (Element child : currentElement.getChildren()) {
								if (child.getName().equals(
										checkingReference.getName())) {
									referencedId = child.getText();
									line = ((LocatedElement) child).getLine();
									break;
								}
							}
						}
						// if the current element has the reference start
						// the validation
						if (referencedId != null) {
							if ("existence".equals(validationLevel)) {
								validateExistence(elements, importedElements,
										violationList, currentName, line,
										checkingReference, referencedId,
										ownPrefix);
							} else if ("referenceType".equals(validationLevel)) {
								validateReferenceType(elements,
										importedElements, violationList,
										currentElement, line,
										checkingReference, referencedId,
										ownPrefix);
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
			LOGGER.info(language.getProperty("validator.logger.violationlist")
					+ System.lineSeparator() + violationListLogText);
		}
		return violationList;
	}

	/**
	 * This method puts all elements with an id found in the given document into
	 * a hash map. The key is the id, the value the element.
	 * 
	 * @param document
	 *            the document to get the elements from
	 * @return the hash map with elements reachable through their id
	 */
	private HashMap<String, Element> getAllElements(Document document) {
		HashMap<String, Element> elements = new HashMap<>();
		Element rootNode = document.getRootElement();
		Filter<Element> filter = Filters.element();
		IteratorIterable<Element> list = rootNode.getDescendants(filter);
		while (list.hasNext()) {
			Element element = list.next();
			Attribute id = element.getAttribute("id");
			// put the element if it has an id
			if (id != null) {
				String id_value = id.getValue();
				if (id_value != null && !id_value.equals("")) {
					elements.put(id_value, element);
				}
			}
		}

		return elements;
	}

	/**
	 * Creates a HashMap which uses the namespace-URI as an ID and another
	 * HashMap as value. The inner HashMap contains all Elements accessible via
	 * the ID as key {@see getAllElements()}
	 * 
	 * @param bpmnFiles
	 * @return
	 * @throws ValidatorException
	 */
	private HashMap<String, HashMap<String, Element>> getAllElementsGroupedByNamespace(
			List<Document> bpmnFiles) throws ValidatorException {
		HashMap<String, HashMap<String, Element>> groupedElements = new HashMap<>();

		for (Document doc : bpmnFiles) {
			String targetNamespace = doc.getRootElement().getAttributeValue(
					"targetNamespace");
			HashMap<String, Element> docElements = getAllElements(doc);

			if (groupedElements.containsKey(targetNamespace)) {
				HashMap<String, Element> previousElems = groupedElements
						.get(targetNamespace);
				previousElems.putAll(docElements);
			} else {
				groupedElements.put(targetNamespace, docElements);
			}
		}

		return groupedElements;

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
			HashMap<String, HashMap<String, Element>> importedElements,
			List<Violation> violationList, Element currentElement, int line,
			Reference checkingReference, String referencedId, String ownPrefix) {
		if (checkingReference.isQname()) {
			// reference ID is prefixed and therefore probably to find in an
			// imported file
			if (referencedId.contains(":")) {
				String[] parts = referencedId.split(":");
				String prefix = parts[0];
				String importedId = parts[1];

				String namespace = "";

				for (Namespace nsp : currentElement.getNamespacesInScope()) {
					if (nsp.getPrefix().equals(prefix)) {
						namespace = nsp.getURI();
					}
				}
				HashMap<String, Element> relevantElements = importedElements
						.get(namespace);
				// case if the namespace is used for the root file and an
				// imported file
				if (ownPrefix.equals(prefix) && relevantElements != null) {
					// case if the element could not be found in the root file
					// and the imported file
					if (!elements.containsKey(importedId)
							&& !relevantElements.containsKey(importedId)) {
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
						Element referencedElement = relevantElements
								.get(importedId);
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
					// case if the namespace is not used for the root file or an
					// imported file
				} else if (relevantElements == null) {
					// import does not exist or is no BPMN file (as it has to
					// be)
					Violation violation = new ExistenceViolation(
							currentElement.getName(),
							checkingReference.getName(), line,
							ExistenceViolation.PREFIX, prefix, language);
					violationList.add(violation);
					// case if the namespace is used by an imported file
				} else {
					Element referencedElement = relevantElements
							.get(importedId);
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
			HashMap<String, HashMap<String, Element>> importedElements,
			List<Violation> violationList, String currentName, int line,
			Reference checkingReference, String referencedId, String ownPrefix) {
		if (checkingReference.isQname()) {
			// reference ID is prefixed and therefore probably to find in an
			// imported file
			if (referencedId.contains(":")) {
				String[] parts = referencedId.split(":");
				String prefix = parts[0];
				String importedId = parts[1];
				HashMap<String, Element> relevantElements = importedElements
						.get(prefix);
				// case if the namespace is used for the root file and an
				// imported file
				if (ownPrefix.equals(prefix) && relevantElements != null) {
					if (!elements.containsKey(importedId)
							&& !relevantElements.containsKey(importedId)) {
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
				} else if (relevantElements == null) {
					// import does not exist or is no BPMN file (as it has to
					// be)
					Violation violation = new ExistenceViolation(currentName,
							checkingReference.getName(), line,
							ExistenceViolation.PREFIX, prefix, language);
					violationList.add(violation);
					// case if the namespace is used by an imported file
				} else {
					if (relevantElements.get(importedId) == null) {
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
					if (bpmnRefElements.containsKey(type)) {
						BPMNElement bpmnElement = bpmnRefElements.get(type);
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

}
