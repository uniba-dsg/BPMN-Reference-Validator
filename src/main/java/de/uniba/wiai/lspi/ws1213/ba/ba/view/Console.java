package de.uniba.wiai.lspi.ws1213.ba.ba.view;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;

import de.uniba.dsg.bpmnspector.common.ValidationResult;
import de.uniba.dsg.bpmnspector.common.Violation;
import de.uniba.wiai.lspi.ws1213.ba.ba.application.BPMNReferenceValidator;
import de.uniba.wiai.lspi.ws1213.ba.ba.application.BPMNReferenceValidatorImpl;
import de.uniba.wiai.lspi.ws1213.ba.ba.application.ValidatorException;


/**
 * This class is for starting the application in the console modus. Therefore it
 * uses String flags for the options of the validator and gives back print
 * lines. An example for the console call can be found here:
 * {@link de.uniba.wiai.lspi.ws1213.ba.ba.Main}.
 * 
 * @author Andreas Vorndran
 * @version 1.0
 * @see de.uniba.wiai.lspi.ws1213.ba.ba.Main
 * 
 */
public class Console {

	/**
	 * This method starts the application for console use with print lines. The
	 * arguments must look like: <br />
	 * fileToValidate.bpmn [-en|ger] [-off|info|severe] [-ref|exist] [-single]<br />
	 * The order of the flags is arbitrary and all flags are optional. If there
	 * are more than one flag of one type the last flag will be used.
	 * 
	 * @param args
	 *            the arguments for starting the application: file path,
	 *            language, log level, validation type, single file
	 */
	public void startApplication(String[] args) {
		if (args[0].equals("-help")) {
			help();
			return;
		}
		// default values
		String path = args[0];
		int languageNumber = BPMNReferenceValidatorImpl.ENGLISH;
		Level level = Level.OFF;
		String validationType = "reference";
		boolean singleFile = false;
		// change defaults with existing flags
		for (int i = 1; i < args.length; i++) {
			if (args[i].equals("-en")) {
				languageNumber = BPMNReferenceValidatorImpl.ENGLISH;
			} else if (args[i].equals("-ger")) {
				languageNumber = BPMNReferenceValidatorImpl.GERMAN;
			} else if (args[i].equals("-off")) {
				level = Level.OFF;
			} else if (args[i].equals("-info")) {
				level = Level.INFO;
			} else if (args[i].equals("-severe")) {
				level = Level.SEVERE;
			} else if (args[i].equals("-ref")) {
				validationType = "reference";
			} else if (args[i].equals("-exist")) {
				validationType = "existence";
			} else if (args[i].equals("-single")) {
				singleFile = true;
			} else if (args[i].equals("-help")) {
				help();
				return;
			} else {
				System.out.println("Invalid flag: " + args[i]);
				help();
				return;
			}
		}

		// load language
		Properties language = new Properties();
		if (languageNumber == BPMNReferenceValidatorImpl.ENGLISH) {
			try {
				language.load(new FileInputStream("lang/en.lang"));
			} catch (FileNotFoundException e) {
				System.out
						.println("Could not find the language file 'lang/en.lang'.");
				return;
			} catch (IOException e) {
				System.out
						.println("IO problems with the language file 'lang/en.lang'.");
				return;
			}

		} else if (languageNumber == BPMNReferenceValidatorImpl.GERMAN) {
			try {
				language.load(new FileInputStream("lang/ger.lang"));
			} catch (FileNotFoundException e) {
				System.out
						.println("Die Sprachdatei 'lang/ger.lang' kann nicht gefunden werden.");
				return;
			} catch (IOException e) {
				System.out
						.println("Es sind I/O-Probleme bei der Sprachdatei 'lang/ger.lang' aufgetreten.");
				return;
			}
		}

		// start application
		try {
			BPMNReferenceValidator validator = new BPMNReferenceValidatorImpl();
			validator.setLogLevel(level);
			validator.setLanguage(languageNumber);
			ValidationResult result = new ValidationResult();
			if (validationType.equals("reference")) {	
				if (singleFile) {
					result = validator
							.validateSingleFile(path);
					
				} else {
					result = validator.validate(path);
				}
				
			} else if (validationType.equals("existence")) {
				if (singleFile) {
					result = validator
							.validateSingleFileExistenceOnly(path);
				} else {
					result = validator
							.validateExistenceOnly(path);
				}
			}
			printValidationResult(result, language);
		} catch (ValidatorException e) {
			System.out.println(e.getMessage());
		}
	}

	/**
	 * This helper method prints the result of a validation run of one file.
	 * 
	 * @param result
	 *            the result to print
	 * @param language
	 *            the language to use
	 */
	private void printValidationResult(ValidationResult result,
			Properties language) {
//		System.out.println(result.getFilePath());
		if (result.isValid()) {
			System.out.println(language.getProperty("view.no_violations"));
		} else {
			System.out.println(language.getProperty("view.violations"));
			int number = 1;
			for (Violation violation : result.getViolations()) {
				System.out.println(number + ". "
						+ violation.toString());
				number++;
			}
		}
	}

	/**
	 * This method prints an explanation of the call and the usable flags for
	 * user support. (Only available in English).
	 */
	private void help() {
		System.out
				.println("the call must look like the following: java -jar tool.jar path/to/bpmn_example.bpmn [-en|ger] [-off|info|severe] [-ref|exist] [-single]");
		System.out.println("language:");
		System.out.println("\t\t-en \t= english");
		System.out.println("\t\t-ger \t= german");
		System.out.println("log level:");
		System.out.println("\t\t-off \t= no logging");
		System.out.println("\t\t-info \t= the program flow will be logged");
		System.out
				.println("\t\t-severe \t= only technical problems will be logged");
		System.out.println("validation level:");
		System.out
				.println("\t\t-ref \t= type of the referenced element will be validated");
		System.out
				.println("\t\t-exist \t= only the existence of the referenced element will be validated");
		System.out.println("validated files:");
		System.out
				.println("\t\t-single \t= only the given file will be validated, the dependent files will not be validated");

	}
}
