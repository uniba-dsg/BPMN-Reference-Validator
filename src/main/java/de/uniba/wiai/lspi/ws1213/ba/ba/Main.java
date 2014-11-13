package de.uniba.wiai.lspi.ws1213.ba.ba;

import javax.swing.JFrame;

import de.uniba.wiai.lspi.ws1213.ba.ba.view.Frame;
import de.uniba.wiai.lspi.ws1213.ba.ba.view.Console;

/**
 * This class has the main method and therefore is the starting point of the
 * whole program. The program can be used in 3 ways: API, console and GUI. For
 * the last 2 ways this class is needed. <br />
 * <ul>
 * <li><strong>Console:</strong><br />
 * The call of the jar from the console needs at least the file path as
 * argument. The other flags are optional and have default values. A call might
 * look like this
 * <code>java -jar tool.jar path/to/bpmn_example.bpmn -en -off -ref</code>. The
 * used flags here are the defaults.</li>
 * <li><strong>GUI:</strong><br />
 * To use the GUI no arguments are allowed. So only start the jar.</li>
 * 
 * @author Andreas Vorndran
 * @version 1.0
 * @see de.uniba.wiai.lspi.ws1213.ba.ba.application.BPMNReferenceValidator
 * @see Console
 * @see de.uniba.wiai.lspi.ws1213.ba.ba.view.Frame
 * 
 */
public class Main {

	/**
	 * This main method starts the stand alone program. For starting the console
	 * the <code>args</code> must be set. For starting the GUI no arguments are
	 * allowed for <code>args</code>.
	 * 
	 * @param args
	 *            the arguments for starting the application by console: file
	 *            path, language, log level, validation type. The arguments must
	 *            look like:
	 *            <code>fileToValidate.bpmn [-en|ger] [-off|info|severe]
	 *            [-ref|exist] [-single]</code>
	 */
	public static void main(String[] args) {
		if (args.length > 0) {
			new Console().startApplication(args);
		} else {
			Frame frame = new Frame();
			frame.setVisible(true);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		}
	}

}
