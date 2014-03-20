package de.uniba.wiai.lspi.ws1213.ba.view;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import de.uniba.wiai.lspi.ws1213.ba.Main;
import de.uniba.wiai.lspi.ws1213.ba.application.BPMNReferenceValidator;
import de.uniba.wiai.lspi.ws1213.ba.application.BPMNReferenceValidatorImpl;
import de.uniba.wiai.lspi.ws1213.ba.application.ValidationResult;
import de.uniba.wiai.lspi.ws1213.ba.application.ValidatorException;
import de.uniba.wiai.lspi.ws1213.ba.application.Violation;

/**
 * This class starts the application from a GUI, based on this JFrame extension.
 * 
 * @author Andreas Vorndran
 * @version 1.0
 * @see Main
 * 
 */
public class Frame extends JFrame {

	private static final long serialVersionUID = -5484560361207659948L;
	private JMenuBar validatorJMenuBar;
	private JMenu languageJMenu;
	private JRadioButtonMenuItem englishJRadioButtonMenuItem;
	private JRadioButtonMenuItem germanJRadioButtonMenuItem;
	private JPanel jContentPane;
	private JTextField pathJTextField;
	private JButton openJButton;
	private JButton validateJButton;
	private JLabel validationLevelJLabel;
	private JRadioButton existenceJRadioButton;
	private JRadioButton referenceJRadioButton;
	private JLabel logLevelJLabel;
	private JRadioButton offJRadioButton;
	private JRadioButton severeJRadioButton;
	private JRadioButton infoJRadioButton;
	private JCheckBox singleJCheckbox;
	private JScrollPane infoDisplayJScrollPane;
	private JTextArea infoDisplayJTextArea;
	private Properties language;
	private int languageNumber;
	
	private BPMNReferenceValidator validator;
	private File lastPath;

	/**
	 * Constructor for starting the GUI.
	 */
	public Frame() {
		super();
		loadLanguage(BPMNReferenceValidatorImpl.ENGLISH);
		languageNumber = BPMNReferenceValidatorImpl.ENGLISH;
		initialize();
		setTexts();
		
		try {
			validator = new BPMNReferenceValidatorImpl();
		} catch (ValidatorException e) {
			getInfoDisplayJTextArea().setText(e.getMessage());
			validator = null;
		}
		
		
	}

	private void initialize() {
		this.setSize(600, 400);
		this.setMinimumSize(new Dimension(500, 300));
		this.setJMenuBar(getValidatorJMenuBar());
		this.setContentPane(getJContentPane());
		this.getRootPane().setDefaultButton(getValidateJButton());
		createRadioButtonGroups();
	}

	private JMenuBar getValidatorJMenuBar() {
		if (validatorJMenuBar == null) {
			validatorJMenuBar = new JMenuBar();
			validatorJMenuBar.add(getLanguageJMenu());
		}
		return validatorJMenuBar;
	}

	private JMenu getLanguageJMenu() {
		if (languageJMenu == null) {
			languageJMenu = new JMenu();
			languageJMenu.add(getEnglishJRadioButtonMenuItem());
			languageJMenu.add(getGermanJRadioButtonMenuItem());
		}
		return languageJMenu;
	}

	private JRadioButtonMenuItem getEnglishJRadioButtonMenuItem() {
		if (englishJRadioButtonMenuItem == null) {
			englishJRadioButtonMenuItem = new JRadioButtonMenuItem();
			englishJRadioButtonMenuItem.setSelected(true);
			englishJRadioButtonMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					languageNumber = BPMNReferenceValidatorImpl.ENGLISH;
					loadLanguage(BPMNReferenceValidatorImpl.ENGLISH);
					setTexts();
				}
			});
		}
		return englishJRadioButtonMenuItem;
	}

	private JRadioButtonMenuItem getGermanJRadioButtonMenuItem() {
		if (germanJRadioButtonMenuItem == null) {
			germanJRadioButtonMenuItem = new JRadioButtonMenuItem();
			germanJRadioButtonMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					languageNumber = BPMNReferenceValidatorImpl.GERMAN;
					loadLanguage(BPMNReferenceValidatorImpl.GERMAN);
					setTexts();
				}
			});
		}
		return germanJRadioButtonMenuItem;
	}

	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			GridBagLayout gridBagLayout = new GridBagLayout();
			jContentPane.setLayout(gridBagLayout);
			addGridBagComponent(gridBagLayout, getPathJTextField(), 0, 0, 4, 1,
					1.0, 0.0);
			addGridBagComponent(gridBagLayout, getOpenJButton(), 4, 0, 1, 1,
					0.0, 0.0);
			addGridBagComponent(gridBagLayout, getValidateJButton(), 5, 0, 1,
					1, 0.0, 0.0);
			addGridBagComponent(gridBagLayout, getValidationLevelJLabel(), 0,
					1, 1, 1, 0.0, 0.0);
			addGridBagComponent(gridBagLayout, getExistenceJRadioButton(), 1,
					1, 1, 1, 0.0, 0.0);
			addGridBagComponent(gridBagLayout, getReferenceJRadioButton(), 2,
					1, 1, 1, 0.0, 0.0);
			addGridBagComponent(gridBagLayout, getLogLevelJLabel(), 0, 2, 1, 1,
					0.0, 0.0);
			addGridBagComponent(gridBagLayout, getOffJRadioButton(), 1, 2, 1,
					1, 0.0, 0.0);
			addGridBagComponent(gridBagLayout, getSevereJRadioButton(), 2, 2,
					1, 1, 0.0, 0.0);
			addGridBagComponent(gridBagLayout, getInfoJRadioButton(), 3, 2, 1,
					1, 0.0, 0.0);
			addGridBagComponent(gridBagLayout, getSingleJCheckBox(), 1, 3, 1,
					1, 0.0, 0.0);
			addGridBagComponent(gridBagLayout, getInfoDisplayJScrollPane(), 0,
					4, 6, 6, 1.0, 1.0);
		}
		return jContentPane;
	}

	private JTextField getPathJTextField() {
		if (pathJTextField == null) {
			pathJTextField = new JTextField();
			pathJTextField.addFocusListener(new FocusListener() {
				@Override
				public void focusLost(FocusEvent arg0) {
				}

				@Override
				public void focusGained(FocusEvent arg0) {
					pathJTextField.selectAll();
				}
			});
		}
		return pathJTextField;
	}

	private JButton getOpenJButton() {
		if (openJButton == null) {
			openJButton = new JButton();
			openJButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					openFile();
				}
			});
		}
		return openJButton;
	}

	private JButton getValidateJButton() {
		if (validateJButton == null) {
			validateJButton = new JButton();
			validateJButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					startApplication();
				}
			});
		}
		return validateJButton;
	}

	private JLabel getValidationLevelJLabel() {
		if (validationLevelJLabel == null) {
			validationLevelJLabel = new JLabel();
		}
		return validationLevelJLabel;
	}

	private JRadioButton getExistenceJRadioButton() {
		if (existenceJRadioButton == null) {
			existenceJRadioButton = new JRadioButton();
		}
		return existenceJRadioButton;
	}

	private JRadioButton getReferenceJRadioButton() {
		if (referenceJRadioButton == null) {
			referenceJRadioButton = new JRadioButton();
			referenceJRadioButton.setSelected(true);
		}
		return referenceJRadioButton;
	}

	private JLabel getLogLevelJLabel() {
		if (logLevelJLabel == null) {
			logLevelJLabel = new JLabel();
		}
		return logLevelJLabel;
	}

	private JRadioButton getOffJRadioButton() {
		if (offJRadioButton == null) {
			offJRadioButton = new JRadioButton();
			offJRadioButton.setSelected(true);
		}
		return offJRadioButton;
	}

	private JRadioButton getSevereJRadioButton() {
		if (severeJRadioButton == null) {
			severeJRadioButton = new JRadioButton();
		}
		return severeJRadioButton;
	}

	private JRadioButton getInfoJRadioButton() {
		if (infoJRadioButton == null) {
			infoJRadioButton = new JRadioButton();
		}
		return infoJRadioButton;
	}

	private JCheckBox getSingleJCheckBox() {
		if (singleJCheckbox == null) {
			singleJCheckbox = new JCheckBox();
		}
		return singleJCheckbox;
	}

	private JScrollPane getInfoDisplayJScrollPane() {
		if (infoDisplayJScrollPane == null) {
			infoDisplayJScrollPane = new JScrollPane(getInfoDisplayJTextArea());
			infoDisplayJScrollPane
					.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
			infoDisplayJScrollPane
					.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		}
		return infoDisplayJScrollPane;
	}

	private JTextArea getInfoDisplayJTextArea() {
		if (infoDisplayJTextArea == null) {
			infoDisplayJTextArea = new JTextArea();
			infoDisplayJTextArea.setEditable(false);
			infoDisplayJTextArea.setLineWrap(true);
			infoDisplayJTextArea.setWrapStyleWord(true);
		}
		return infoDisplayJTextArea;
	}

	/**
	 * This helper method supports the use of the grid bag layout. It configures
	 * the grid bag constraints and adds the component to the jContent pane.
	 * 
	 * @param gridBagLayout
	 *            the used grid bag layout
	 * @param component
	 *            the component to add
	 * @param x
	 *            the starting line
	 * @param y
	 *            the starting row
	 * @param width
	 *            the number of rows
	 * @param height
	 *            the number of lines
	 * @param weightx
	 *            the scaling weight for more than the minimum space in a line
	 * @param weighty
	 *            the scaling weight for more than the minimum space in a row
	 */
	private void addGridBagComponent(GridBagLayout gridBagLayout,
			Component component, int x, int y, int width, int height,
			double weightx, double weighty) {
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.gridx = x;
		gridBagConstraints.gridy = y;
		gridBagConstraints.gridwidth = width;
		gridBagConstraints.gridheight = height;
		gridBagConstraints.weightx = weightx;
		gridBagConstraints.weighty = weighty;
		gridBagLayout.setConstraints(component, gridBagConstraints);
		getJContentPane().add(component);
	}

	private void createRadioButtonGroups() {
		ButtonGroup validationTypeGroup = new ButtonGroup();
		validationTypeGroup.add(getExistenceJRadioButton());
		validationTypeGroup.add(getReferenceJRadioButton());
		ButtonGroup logLevelGroup = new ButtonGroup();
		logLevelGroup.add(getOffJRadioButton());
		logLevelGroup.add(getSevereJRadioButton());
		logLevelGroup.add(getInfoJRadioButton());
		ButtonGroup languageGroup = new ButtonGroup();
		languageGroup.add(getEnglishJRadioButtonMenuItem());
		languageGroup.add(getGermanJRadioButtonMenuItem());
	}

	private void startApplication() {
		String path = getPathJTextField().getText();
		if (path.equals("")) {
			JOptionPane.showMessageDialog(this,
					language.getProperty("view.path.info"),
					language.getProperty("info"),
					JOptionPane.INFORMATION_MESSAGE);
		} else {
			String output = "";
			Level level = Level.OFF;
			if (getOffJRadioButton().isSelected()) {
				level = Level.OFF;
			} else if (getSevereJRadioButton().isSelected()) {
				level = Level.SEVERE;
			} else if (getInfoJRadioButton().isSelected()) {
				level = Level.INFO;
			}
			try {
				
				validator.setLogLevel(level);
				validator.setLanguage(languageNumber);
				if (getReferenceJRadioButton().isSelected()) {
					if (getSingleJCheckBox().isSelected()) {
						ValidationResult result = validator
								.validateSingleFile(path);
						output = output + getValidationOutput(result);
					} else {
						List<ValidationResult> results = validator
								.validate(path);
						for (ValidationResult validationResult : results) {
							output = output
									+ getValidationOutput(validationResult);
						}
					}
				} else if (getExistenceJRadioButton().isSelected()) {
					if (getSingleJCheckBox().isSelected()) {
						ValidationResult result = validator
								.validateSingleFileExistenceOnly(path);
						output = output + getValidationOutput(result);
					} else {
						List<ValidationResult> results = validator
								.validateExistenceOnly(path);
						for (ValidationResult validationResult : results) {
							output = output
									+ getValidationOutput(validationResult);
						}
					}
				}
			} catch (ValidatorException e) {
				output = output + e.getMessage();
			}
			getInfoDisplayJTextArea().setText(output);
		}
	}

	private String getValidationOutput(ValidationResult result) {
		String output = result.getFilePath() + System.lineSeparator();
		if (result.isValid()) {
			output = output + language.getProperty("view.no_violations")
					+ System.lineSeparator();
		} else {
			output = output + language.getProperty("view.violations")
					+ System.lineSeparator();
			int number = 1;
			for (Violation violation : result.getViolations()) {
				output = output + number + ". "
						+ violation.getViolationMessage()
						+ System.lineSeparator();
				number++;
			}
		}
		return output;
	}

	private void loadLanguage(int languageNumber) {
		language = new Properties();
		if (languageNumber == BPMNReferenceValidatorImpl.ENGLISH) {
			try {
				InputStream stream = getClass().getResourceAsStream("/en.lang");
				if (stream == null) {
					JOptionPane.showMessageDialog(this,
							"Could not find the language file 'lang/en.lang'.",
							"Error", JOptionPane.ERROR_MESSAGE);
				} else {
					language.load(stream);
				}
			} catch (IOException e) {
				JOptionPane.showMessageDialog(this,
						"IO problems with the language file 'lang/en.lang'.",
						"Error", JOptionPane.ERROR_MESSAGE);
			}

		} else if (languageNumber == BPMNReferenceValidatorImpl.GERMAN) {
			try {
				InputStream stream = getClass()
						.getResourceAsStream("/ger.lang");
				if (stream == null) {
					JOptionPane
							.showMessageDialog(
									this,
									"Die Sprachdatei 'lang/ger.lang' kann nicht gefunden werden.",
									"Fehler", JOptionPane.ERROR_MESSAGE);
				} else {
					language.load(stream);
				}
			} catch (IOException e) {
				JOptionPane
						.showMessageDialog(
								this,
								"Es sind I/O-Probleme bei der Sprachdatei 'lang/ger.lang' aufgetreten.",
								"Fehler", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void setTexts() {
		this.setTitle(language.getProperty("view.title"));
		getLanguageJMenu().setText(language.getProperty("view.menu.language"));
		getEnglishJRadioButtonMenuItem().setText(
				language.getProperty("view.menu.english"));
		getGermanJRadioButtonMenuItem().setText(
				language.getProperty("view.menu.german"));
		getPathJTextField().setText(language.getProperty("view.path.example"));
		getPathJTextField().setToolTipText(
				language.getProperty("view.path.info"));
		getOpenJButton().setText(language.getProperty("view.button.open"));
		getValidateJButton().setText(
				language.getProperty("view.button.validate"));
		getValidationLevelJLabel().setText(
				language.getProperty("view.label.validation"));
		getExistenceJRadioButton().setText(
				language.getProperty("view.radio.existence"));
		getExistenceJRadioButton().setToolTipText(
				language.getProperty("view.tooltip.existence"));
		getReferenceJRadioButton().setText(
				language.getProperty("view.radio.reference"));
		getReferenceJRadioButton().setToolTipText(
				language.getProperty("view.tooltip.reference"));
		getLogLevelJLabel().setText(language.getProperty("view.label.log"));
		getOffJRadioButton().setText(language.getProperty("view.radio.off"));
		getOffJRadioButton().setToolTipText(
				language.getProperty("view.tooltip.off"));
		getSevereJRadioButton().setText(
				language.getProperty("view.radio.severe"));
		getSevereJRadioButton().setToolTipText(
				language.getProperty("view.tooltip.severe"));
		getInfoJRadioButton().setText(language.getProperty("view.radio.info"));
		getInfoJRadioButton().setToolTipText(
				language.getProperty("view.tooltip.info"));
		getSingleJCheckBox().setText(language.getProperty("view.check.single"));
		getSingleJCheckBox().setToolTipText(
				language.getProperty("view.tooltip.single"));
	}

	private void openFile() {
		JFileChooser jFileChooser = new JFileChooser(lastPath);
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"BPMN files (.bpmn(2) & .xml)", "bpmn", "xml", "bpmn2");
		jFileChooser.setFileFilter(filter);
		int returnValue = jFileChooser.showOpenDialog(this);
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			lastPath = jFileChooser.getSelectedFile();
			getPathJTextField().setText(lastPath.getAbsolutePath());
		}
	}
}
