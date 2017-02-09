package TestDatabase;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JFrame;

import model.PersonModel;
import model.ProgramModel;
import model.TaskModel;

public class TestDatabase {
	private static Connection dbConnection;
	private static URLConnection urlConnection;

	private static JFrame frame = new JFrame("Database connector");
	private static JButton connectButton = new JButton();
	private static JButton showButton = new JButton("Show");
	private static LinkedList<PersonModel> personList;
	private static LinkedList<ProgramModel> programList;

	public static void initializeDatabase() {
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

		showButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showDatabase();
			}
		});

		frame.setLayout(new FlowLayout(FlowLayout.CENTER));
		frame.add(connectButton);
		frame.add(showButton);
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

	public static void showDatabase() {
		try {
			PreparedStatement checkStmt = dbConnection.prepareStatement("SELECT * FROM Persons");
			ResultSet result = checkStmt.executeQuery();
			int row;

			while (true) {
				result.next();

				row = result.getRow();
				if (row == 0)
					break;

				System.out.println("Row " + row + ": " + result.getString("PersonName"));
			}

			checkStmt.close();

		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

	public static void importPersonDatabase(LinkedList<PersonModel> persons) {
		personList = persons;
		try {
			PreparedStatement checkStmt = dbConnection
					.prepareStatement("SELECT COUNT(*) AS count FROM Persons WHERE PersonName=?");
			PreparedStatement addStmt = dbConnection.prepareStatement(
					"INSERT INTO Persons (PersonName, PhoneNumber, EMail, isLeader) " + " VALUES (?, ?, ?, ?)");
			ResultSet result;

			for (int i = 0; i < personList.size(); i++) {
				String personName = personList.get(i).getName();
				checkStmt.setString(1, personName);
				result = checkStmt.executeQuery();
				result.next();
				String sqlName = result.getString(1);

				if (sqlName == null || sqlName.equals("") || sqlName.equals("0")) {
					// Add new person
					PersonModel person = personList.get(i);
					int col = 1;
					addStmt.setString(col++, personName);
					addStmt.setString(col++, person.getPhone());
					addStmt.setString(col++, person.getEmail());
					addStmt.setBoolean(col, person.isLeader());

					addStmt.executeUpdate();
					System.out.println("Adding " + personName);
				} else
					System.out.println("Person " + personName + " already exists!");
			}

			addStmt.close();
			checkStmt.close();
			System.out.println("Connection status = " + isDatabaseConnected());

		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

	public static void importProgramsDatabase(LinkedList<ProgramModel> programs) {
		programList = programs;
		try {
			PreparedStatement checkProgramStmt = dbConnection
					.prepareStatement("SELECT COUNT(*) AS count FROM Programs WHERE ProgramName=?");
			PreparedStatement addProgramStmt = dbConnection
					.prepareStatement("INSERT INTO Programs (ProgramName, StartDate, EndDate) VALUES (?, ?, ?)");
			ResultSet result;

			for (int i = 0; i < programList.size(); i++) {
				String programName = programList.get(i).getProgramName();
				checkProgramStmt.setString(1, programName);
				result = checkProgramStmt.executeQuery();
				result.next();
				String sqlProgName = result.getString(1);

				ProgramModel program = programList.get(i);
				if (sqlProgName == null || sqlProgName.equals("") || sqlProgName.equals("0")) {
					// Add new program
					int col = 1;
					addProgramStmt.setString(col++, programName);
					addProgramStmt.setString(col++, program.getStartDate());
					addProgramStmt.setString(col++, program.getEndDate());

					addProgramStmt.executeUpdate();
					System.out.println("Adding " + programName);
				} else {
					System.out.println("Program " + programName + " already exists!");
				}

				addTasksByProgram(program);
			}

			addProgramStmt.close();
			checkProgramStmt.close();
			System.out.println("Connection status = " + isDatabaseConnected());

		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

	private static void addTasksByProgram(ProgramModel program) {
		try {
			PreparedStatement checkTaskStmt = dbConnection.prepareStatement("SELECT COUNT(*) AS count FROM Tasks WHERE TaskName=?");
			PreparedStatement addTaskStmt = dbConnection.prepareStatement(
					"INSERT INTO Tasks (TaskName, Location, NumLeadersReqd, TotalPersonsReqd, DaysOfWeek, DowInMonth, Color) VALUES "
							+ "(?, ?, ?, ?, ?, ?, ?)");
			ResultSet result;

			// Add task for this program
			LinkedList<TaskModel> tasks = program.getTaskList();
			for (int j = 0; j < tasks.size(); j++) {
				String taskName = tasks.get(j).getTaskName();
				checkTaskStmt.setString(1, taskName);
				result = checkTaskStmt.executeQuery();
				result.next();
				String sqlTaskName = result.getString(1);

				if (sqlTaskName == null || sqlTaskName.equals("") || sqlTaskName.equals("0")) {
					// Add new task
					TaskModel task = tasks.get(j);
					int dow = 0, wom = 0;
					for (int k = 0; k < 7; k++)
						dow = (dow << 1) | (task.getDayOfWeek()[k] ? 1 : 0);
					for (int k = 0; k < 5; k++)
						wom = (wom << 1) | (task.getWeekOfMonth()[k] ? 1 : 0);

					int col = 1;
					addTaskStmt.setString(col++, taskName);
					addTaskStmt.setString(col++, task.getLocation());
					addTaskStmt.setInt(col++, task.getNumLeadersReqd());
					addTaskStmt.setInt(col++, task.getTotalPersonsReqd());
					addTaskStmt.setInt(col++, dow);
					addTaskStmt.setInt(col++, wom);
					addTaskStmt.setInt(col++, task.getColor());

					addTaskStmt.executeUpdate();
					System.out.println("Adding task " + taskName);
				} else
					System.out.println("Task " + taskName + " already exists!");
			}

			addTaskStmt.close();
			checkTaskStmt.close();
			System.out.println("Connection status = " + isDatabaseConnected());
			
		} catch (SQLException e) {
			System.out.println(e.getMessage());
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
