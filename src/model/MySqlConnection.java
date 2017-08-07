package model;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.JOptionPane;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

public class MySqlConnection {
	// Constants
	private static final int LOCAL_PORT = 8740; // any free port can be used
	private static final String SSH_HOST = "www.ProgramPlanner.org";
	private static final String SSH_USER = "wavis421";
	// TODO: This should be on server??
	private static final String SSH_KEY_FILE_PATH = "C:\\Users\\Wendy\\Documents\\AppDevelopment\\keystore\\wavisadmin-keypair-ncal.pem";
	private static final String REMOTE_HOST = "127.0.0.1";
	private static final int REMOTE_PORT = 3306;

	// Save SSH Session and database connection
	private static Session session = null;
	private static Connection connection = null;

	public static Connection connectToServer(String dataBaseName, String user, String password) throws SQLException {
		// If re-connecting, close current session and connection first
		closeConnections();

		// Create new SSH and database connections
		if (connectSSH()) {
			connectToDataBase(dataBaseName, user, password);
		}
		return connection;
	}

	private static boolean connectSSH() {
		try {
			java.util.Properties config = new java.util.Properties();
			JSch jsch = new JSch();
			session = jsch.getSession(SSH_USER, SSH_HOST, 22);
			jsch.addIdentity(SSH_KEY_FILE_PATH);
			config.put("StrictHostKeyChecking", "no");
			config.put("ConnectionAttempts", "1");

			session.setConfig(config);
			session.connect();
			session.setPortForwardingL(LOCAL_PORT, REMOTE_HOST, REMOTE_PORT);
			return true;

		} catch (Exception e) {
			JOptionPane.showMessageDialog(null,
					"Failure connecting to website.\n" + "Check your internet connection and make sure "
							+ "Program Planner not already running on this machine.\n(" + e.getMessage() + ")");
		}
		return false;
	}

	private static void connectToDataBase(String dataBaseName, String user, String password) throws SQLException {
		try {
			String driverName = "com.mysql.jdbc.Driver";
			Class.forName(driverName).newInstance();
			
			// Connecting through SSH tunnel
			MysqlDataSource dataSource = new MysqlDataSource();
			dataSource.setServerName("localhost");
			dataSource.setPortNumber(LOCAL_PORT);
			dataSource.setDatabaseName(dataBaseName);
			dataSource.setUser(user);
			dataSource.setPassword(password);

			connection = dataSource.getConnection();

		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Failed to connect to Database: " + e.getMessage());
		}
	}

	public static void closeConnections() {
		CloseDataBaseConnection();
		CloseSSHConnection();
	}

	private static void CloseDataBaseConnection() {
		try {
			if (connection != null && !connection.isClosed()) {
				connection.close();
			}
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(null, "Error closing database connection: " + e.getMessage());
		}
		connection = null;
	}

	private static void CloseSSHConnection() {
		if (session != null && session.isConnected()) {
			session.disconnect();
			session = null;
		}
	}

	// TODO: MySqlDatabase should use this method
	// Works ONLY FOR single query (one SELECT or one DELETE etc)
	public static ResultSet executeMyQuery(String query, String dataBaseName) {
		ResultSet resultSet = null;

		try {
			Statement stmt = connection.createStatement();
			resultSet = stmt.executeQuery(query);

		} catch (SQLException e) {
			System.out.println("Database query error: " + e.getMessage());
		}

		return resultSet;
	}
}