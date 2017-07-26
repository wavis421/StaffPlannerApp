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
	private static final String SSH_HOST = "ec2-54-183-162-235.us-west-1.compute.amazonaws.com";
	private static final String SSH_USER = "ec2-user";
	// TODO: This should be on server??
	private static final String SSH_KEY_FILE_PATH = "wavisadmin-keypair-ncal.pem";
	private static final String REMOTE_HOST = "127.0.0.1";
	private static final int REMOTE_PORT = 3306;

	// Save SSH Session and database connection
	private static Session session = null;
	private static Connection connection = null;

	public static Connection connectToServer(String dataBaseName) throws SQLException {
		// If re-connecting, close current session and connection first
		closeConnections();

		// Create new SSH and database connections
		if (connectSSH())
			connectToDataBase(dataBaseName);
		return connection;
	}

	private static boolean connectSSH() throws SQLException {
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
			JOptionPane.showMessageDialog(null, "Failed to connect to Database: " + e.getMessage());
		}
		return false;
	}

	private static void connectToDataBase(String dataBaseName) throws SQLException {
		try {
			String driverName = "com.mysql.jdbc.Driver";
			Class.forName(driverName).newInstance();

			// Old way to connect...
			// String url = "jdbc:mysql://www.programplanner.org:3306/TestDb421";
			// dbConnection = DriverManager.getConnection(url, "wavisTester1", "ImGladToBeTesting555&");
			// dbConnection = DriverManager.getConnection(url, "tester421", "Rwarwe310");

			// mySql database connectivity
			MysqlDataSource dataSource = new MysqlDataSource();
			dataSource.setServerName("localhost");
			dataSource.setPortNumber(LOCAL_PORT);
			dataSource.setUser("root");
			// dataSource.setAllowMultiQueries(true);
			// dataSource.setPassword("");
			dataSource.setDatabaseName(dataBaseName);

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
	private static ResultSet executeMyQuery(String query, String dataBaseName) {
		ResultSet resultSet = null;

		try {
			connectToServer(dataBaseName);
			Statement stmt = connection.createStatement();
			resultSet = stmt.executeQuery(query);
			System.out.println("Database connection success");
		} catch (SQLException e) {
			System.out.println("Database query error: " + e.getMessage());
		}

		return resultSet;
	}
}