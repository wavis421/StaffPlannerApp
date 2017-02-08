package TestDatabase;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JFrame;

import model.Database;

public class TestDatabase {
	private static Connection dbConnection;
	private static URLConnection urlConnection;

	private static JFrame frame = new JFrame("Database connector");
	private static JButton connectButton = new JButton();

	public static void main(String[] args) {
		Database db = new Database();

		try {
			dbConnection = connectDatabase();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
		}

		updateConnectionStatus();

		connectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (connectButton.getText().equals("Disconnect")) {
					disconnectDatabase();
				} else {
					try {
						dbConnection = connectDatabase();

					} catch (Exception e2) {
						// TODO Auto-generated catch block
						System.out.println(e2.getMessage());
					}
				}
				updateConnectionStatus();
			}
		});

		frame.add(connectButton);
		frame.pack();
		frame.setVisible(true);
	}

	public static Connection connectDatabase() throws Exception {
		if (isDatabaseConnected())
			return dbConnection;

		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();

		} catch (ClassNotFoundException e) {
			System.out.println("Database driver not found");
			return null;
		}

		try {
			String url = "jdbc:mysql://www.programplanner.org:3306/ProgramPlanner";
			dbConnection = DriverManager.getConnection(url, "SB_hpfMnj6YZBdQM", "Apk13002");
			return dbConnection;

		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}

		return null;
	}

	public URLConnection connectUrl() throws Exception {
		if (urlConnection != null)
			return urlConnection;

		try {
			URL dbURL = new URL("http://www.programplanner.org/");
			URLConnection urlConnection = dbURL.openConnection();
			urlConnection.connect();
			return urlConnection;

		} catch (MalformedURLException e) {
			// new URL() failed
			System.out.println("Database URL failed: " + e.getMessage());

		} catch (IOException e) {
			// openConnection() failed
			System.out.println("Database connection failed: " + e.getMessage());
		}

		return null;
	}

	public static boolean isDatabaseConnected() {
		if (dbConnection == null)
			return false;

		try {
			return !dbConnection.isClosed();

		} catch (SQLException e) {
			System.out.println(e.getMessage());
			return false;
		}
	}

	public static void disconnectDatabase() {
		if (dbConnection != null)
			try {
				dbConnection.close();
				dbConnection = null;
			} catch (SQLException e) {
				System.out.println("Can't close database connection: " + e.getMessage());
			}
	}

	private static void updateConnectionStatus() {
		if (isDatabaseConnected()) {
			frame.setTitle("Connected");
			connectButton.setText("Disconnect");
		} else {
			frame.setTitle("Disconnected");
			connectButton.setText("Connect");
		}
	}
}
