package de.uniba.wiai.lspi.ws1213.ba.application.importer;

import de.uniba.wiai.lspi.ws1213.ba.application.ValidatorException;
import de.uniba.wiai.lspi.ws1213.ba.application.xsdvalidation.ResourceResolver;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filter;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.located.LocatedJDOMFactory;
import org.jdom2.util.IteratorIterable;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Helper Class to Import all referenced BPMN, WSDL and XSD files referenced by
 * a BPMN process.
 * 
 * @author Matthias Geiger, Andreas Vorndran
 * @version 1.0
 * 
 * @see ValidatorException
 * 
 */
public class FileImporter {

	private Properties language;
	private final SAXBuilder builder;

	/**
	 * Constructs a new {@code FileImporter}
	 * 
	 * @param language
	 *            - in order to enable language specific error messages language
	 *            dependent properties are needed
	 */
	public FileImporter(Properties language) {
		this.language = language;
		builder = new SAXBuilder();
		builder.setJDOMFactory(new LocatedJDOMFactory());
	}

	/**
	 * Setter to change Language after initial creation.
	 * 
	 * @param language
	 *            - the new language properties to use
	 */
	public void setLanguage(Properties language) {
		this.language = language;
	}

	/**
	 * Load all the file found at {@code pathToBaseFile}
	 * 
	 * @param pathToBaseFile
	 *            - the base file for import processing, this file is always
	 *            imported
	 * @param processImports
	 *            - if {@code true} all files referenced by the base file will
	 *            also be imported
	 * @return - A {@link ProcessFileSet} containing the
	 *         {@link org.jdom2.Document} representation of all imported files
	 * @throws ValidatorException
	 *             - thrown if either a file cannot be resolved or the file is
	 *             not schema valid
	 */
	public ProcessFileSet loadAllFiles(String pathToBaseFile,
			boolean processImports) throws ValidatorException {

		File baseFile = checkPathAndCreateFile(pathToBaseFile);

		try {
			validateFileAgainstBpmnXSD(baseFile);
			Document baseDoc = builder.build(baseFile);
			List<String> processedFiles = new ArrayList<>();
			processedFiles.add(baseFile.getAbsolutePath());
			if (processImports) {		
				return new ProcessFileSet(baseDoc, processImports(baseDoc,
						baseFile.toPath(), processedFiles), processedFiles);
			} else {
				return new ProcessFileSet(baseDoc, null, null);
			}

		} catch (JDOMException e) {
			String errorText = language.getProperty("validator.jdom") + "'"
					+ pathToBaseFile + "'.";
			throw new ValidatorException(errorText, e);
		} catch (IOException e) {

			String errorText = language.getProperty("validator.io") + "'"
					+ pathToBaseFile + "'.";
			throw new ValidatorException(errorText, e);
		}

	}

	private File checkPathAndCreateFile(String path) throws ValidatorException {
		if (path == null || path.equals("")) {
			throw new ValidatorException(
					language.getProperty("importer.path.notempty.notnull"),
					new IllegalArgumentException(language
							.getProperty("importer.path.notempty.notnull")));
		}
		Path file = Paths.get(path);
		if (Files.notExists(file) || Files.isDirectory(file)) {
			throw new ValidatorException(
					language.getProperty("importer.path.invalid")
							+ path
							+ language
									.getProperty("importer.path.notexistentOrDir"));
		}

		return file.toFile();
	}

	private Map<String, List<Document>> processImports(Document baseDoc,
			Path baseDocPath, List<String> processedFiles)
			throws ValidatorException {
		Map<String, List<Document>> resolvedFiles = new HashMap<>();
		Element rootNode = baseDoc.getRootElement();
		Filter<Element> filter = Filters.element();
		IteratorIterable<Element> list = rootNode.getDescendants(filter);

		List<Document> resolvedBpmnFiles = new ArrayList<>();
		List<Document> resolvedXsdFiles = new ArrayList<>();
		List<Document> resolvedWsdlFiles = new ArrayList<>();

		while (list.hasNext()) {
			Element element = list.next();
			if (element.getName().equals("import")) {
				String path = element.getAttributeValue("location");

				// Navigate from the folder containing the baseFile
				// (baseFile.getParent()) to the given location and
				// normalize the result
				String absPath = baseDocPath.getParent()
						.resolve(Paths.get(path)).normalize().toString();
				File file = checkPathAndCreateFile(absPath);
				if (!processedFiles.contains(file.getAbsolutePath())) {
					if (ProcessFileSet.BPMN2_NAMESPACE.equals(element
							.getAttributeValue("importType"))) {

						try {
							validateFileAgainstBpmnXSD(file);
							Document bpmnDoc = builder.build(file);
							processedFiles.add(file.getAbsolutePath());
							resolvedBpmnFiles.add(bpmnDoc);
							Map<String, List<Document>> processResults = processImports(
									bpmnDoc, file.toPath(), processedFiles);
							resolvedBpmnFiles.addAll(processResults
									.get(ProcessFileSet.BPMN2_NAMESPACE));
							resolvedWsdlFiles.addAll(processResults
									.get(ProcessFileSet.WSDL_NAMESPACE));
							resolvedXsdFiles.addAll(processResults
									.get(ProcessFileSet.XSD_NAMESPACE));

						} catch (JDOMException e) {
							String errorText = language
									.getProperty("validator.jdom.imported.part1")
									+ path
									+ "("
									+ absPath
									+ ")"
									+ language
											.getProperty("validator.jdom.imported.part2");
							throw new ValidatorException(errorText, e);
						} catch (IOException e) {
							String errorText = language
									.getProperty("validator.io.imorted.part1")
									+ path
									+ "("
									+ absPath
									+ ")"
									+ language
											.getProperty("validator.io.imorted.part2");
							throw new ValidatorException(errorText, e);
						}
					} else if (ProcessFileSet.WSDL_NAMESPACE.equals(element
							.getAttributeValue("importType"))) {
						// TODO implement import processing of WSDL interfaces
					} else if (ProcessFileSet.XSD_NAMESPACE.equals(element
							.getAttributeValue("importType"))) {
						// TODO implement import processing of XSDs
					}
				}
			}
		}
		resolvedFiles.put(ProcessFileSet.BPMN2_NAMESPACE, resolvedBpmnFiles);
		resolvedFiles.put(ProcessFileSet.WSDL_NAMESPACE, resolvedWsdlFiles);
		resolvedFiles.put(ProcessFileSet.XSD_NAMESPACE, resolvedXsdFiles);
		return resolvedFiles;
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
	private void validateFileAgainstBpmnXSD(File file)
			throws ValidatorException {
		List<SAXParseException> XSDErrorList = new ArrayList<>();
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
            validator.setErrorHandler(new ValidationErrorHandler(XSDErrorList));
			validator.validate(new StreamSource(file));
			if (XSDErrorList.size() > 0) {
				String xsdErrorText = language
						.getProperty("validator.xsd.general.part1")
						+ file.getName()
						+ language.getProperty("validator.xsd.general.part2")
						+ System.lineSeparator();
				for (SAXParseException saxParseException : XSDErrorList) {
					xsdErrorText = xsdErrorText
							+ language.getProperty("loader.xsd.error.part1")
							+ saxParseException.getLineNumber() + " "
							+ language.getProperty("loader.xsd.error.part2")
							+ saxParseException.getMessage()
							+ System.lineSeparator();
				}
				throw new ValidatorException(xsdErrorText);
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

    private class ValidationErrorHandler implements ErrorHandler {

        List<SAXParseException> XSDErrorList = new ArrayList<>();

        public ValidationErrorHandler(List<SAXParseException> XSDErrorList) {
            this.XSDErrorList = XSDErrorList;
        }

        @Override
        public void error(SAXParseException e) throws SAXException {
            XSDErrorList.add(e);
        }

        @Override
        public void fatalError(SAXParseException e) throws SAXException {
            XSDErrorList.add(e);
        }

        @Override
        public void warning(SAXParseException e) throws SAXException {
            XSDErrorList.add(e);
        }
    }
}
