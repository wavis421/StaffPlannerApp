/**
 * The StaffPlannerApp provides...
 * TODO
 */
package gui;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.net.URL;

import javax.swing.SwingUtilities;

public class App {
	public static void main(String[] args) {

		// If executing from a JAR, re-direct errors to a file...
		URL myClass = App.class.getResource("App.class");
		if (!myClass.toString().substring(0, 4).equals("file")) {
			try {
				// create a file for re-directing errors
				// TODO: Append to end of file; if file size is too large, create new file
				FileOutputStream f = new FileOutputStream("PP_error.log");
				PrintStream p = new PrintStream(f);

				System.setOut(p);
				System.setErr(p);
				// TODO: Add date/time that file is opened/created
				System.out.println("*** Program Planner log:");

			} catch (FileNotFoundException e) {
				// Ignore errors if file cannot be created
			}
		}

		// Start application from MainFrame
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new MainFrame();
			}
		});
	}
}
