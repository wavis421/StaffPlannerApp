package TestDatabase;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JFrame;

import model.PersonModel;
import model.ProgramModel;
import model.TaskModel;
import model.TimeModel;

public class TestDatabase {
	private static Connection dbConnection;
	private static URLConnection urlConnection;

	private static JFrame frame = new JFrame("Database connector");
	private static JButton connectButton = new JButton();
	private static JButton showRosterButton = new JButton("Show Roster");
	private static JButton showTasksButton = new JButton("Show Tasks");
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

		showRosterButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showRosterDatabase();
			}
		});

		showTasksButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showTasksDatabase();
			}
		});

		frame.setLayout(new FlowLayout(FlowLayout.CENTER));
		frame.add(connectButton);
		frame.add(showRosterButton);
		frame.add(showTasksButton);
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
			dbConnection = DriverManager.getConnection(url, "SB_nAzSqi6pAaluq", "Apk13002");
			return dbConnection;

		} catch (SQLException e) {
			System.out.println(e.getMessage());
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

	public static void showRosterDatabase() {
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
			result.close();
			checkStmt.close();

		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

	public static void showTasksDatabase() {
		try {
			PreparedStatement checkStmt = dbConnection.prepareStatement("SELECT * FROM Tasks, Programs");
			ResultSet result = checkStmt.executeQuery();
			int row;

			while (true) {
				result.next();

				row = result.getRow();
				if (row == 0)
					break;

				String location = result.getString("Location");
				if (location == null || location.equals(""))
					System.out.println("Row " + row + ": " + result.getString("ProgramName") + " - "
							+ result.getString("TaskName"));
				else
					System.out.println("Row " + row + ": " + result.getString("ProgramName") + " - "
							+ result.getString("TaskName") + " at " + result.getString("Location"));
			}
			result.close();
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
			ResultSet result = null;

			for (int i = 0; i < personList.size(); i++) {
				String personName = personList.get(i).getName();
				checkStmt.setString(1, personName);
				result = checkStmt.executeQuery();
				result.next();

				if (result.getInt("count") == 0) {
					// Add new person
					PersonModel person = personList.get(i);
					int col = 1;
					addStmt.setString(col++, personName);
					addStmt.setString(col++, person.getPhone());
					addStmt.setString(col++, person.getEmail());
					addStmt.setBoolean(col, person.isLeader());

					addStmt.executeUpdate();
				}
			}
			if (result != null)
				result.close();
			addStmt.close();
			checkStmt.close();

		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

	public static void importProgramDatabase(LinkedList<ProgramModel> programs) {
		programList = programs;
		int progID = 0;
		try {
			PreparedStatement checkProgramStmt = dbConnection.prepareStatement(
					"SELECT COUNT(*) AS count, ProgramID as progID FROM Programs WHERE ProgramName=?");
			ResultSet result = null;

			for (int i = 0; i < programList.size(); i++) {
				String programName = programList.get(i).getProgramName();
				checkProgramStmt.setString(1, programName);
				result = checkProgramStmt.executeQuery();
				result.next();

				ProgramModel program = programList.get(i);
				if (result.getInt("count") == 0) {
					// Add new program
					progID = addProgram(programName, program.getStartDate(), program.getEndDate());
				} else {
					// Program already exists
					progID = result.getInt("progID");
				}
				addTasksByProgram(program, progID);
			}
			if (result != null)
				result.close();
			checkProgramStmt.close();

		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

	public static LinkedList<PersonModel> loadRoster() {
		try {
			connectDatabase();
		} catch (Exception e1) {
			System.out.println("Unable to connect to database: " + e1.getMessage());
		}

		LinkedList<PersonModel> personList = new LinkedList<PersonModel>();
		try {
			Statement selectStmt = dbConnection.createStatement();
			ResultSet results = selectStmt.executeQuery("SELECT PersonID, PersonName FROM Persons ORDER BY PersonName");

			while (results.next()) {
				int personID = results.getInt("PersonID");
				System.out.println("Found person " + results.getString("PersonName") + " with ID " + personID);
			}
			results.close();
			selectStmt.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return personList;
	}

	public static LinkedList<ProgramModel> loadPrograms() {
		try {
			connectDatabase();
		} catch (Exception e1) {
			System.out.println("Unable to connect to database: " + e1.getMessage());
		}

		LinkedList<ProgramModel> progList = new LinkedList<ProgramModel>();
		try {
			Statement selectStmt = dbConnection.createStatement();
			ResultSet results = selectStmt.executeQuery(
					"SELECT ProgramID, ProgramName, StartDate, EndDate FROM Programs ORDER BY ProgramName");

			while (results.next()) {
				int progID = results.getInt("ProgramID");
				progList.add(new ProgramModel(progID, results.getString("ProgramName"), results.getString("StartDate"),
						results.getString("EndDate"), loadTasks(progID)));
			}
			results.close();
			selectStmt.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return progList;
	}

	public static LinkedList<TaskModel> loadTasks(int progID) {
		LinkedList<TaskModel> taskList = new LinkedList<TaskModel>();
		try {
			PreparedStatement selectStmt = dbConnection.prepareStatement(
					"SELECT TaskID, Tasks.ProgramID, Programs.ProgramID, TaskName, Location, NumLeadersReqd, TotalPersonsReqd, "
							+ "Color, DaysOfWeek, DowInMonth, Hour, Minute FROM Programs, Tasks "
							+ "WHERE Programs.ProgramID = Tasks.ProgramID AND Programs.ProgramID=? ORDER BY Hour, Minute");

			selectStmt.setInt(1, progID);
			ResultSet results = selectStmt.executeQuery();

			while (results.next()) {
				int dow = results.getInt("DaysOfWeek");
				boolean[] dowBool = { false, false, false, false, false, false, false };
				for (int i = 6; i >= 0; i--) {
					if ((dow & 1) == 1)
						dowBool[i] = true;
					dow >>= 1;
				}
				int wom = results.getInt("DowInMonth");
				boolean[] womBool = { false, false, false, false, false };
				for (int i = 4; i >= 0; i--) {
					if ((wom & 1) == 1)
						womBool[i] = true;
					wom >>= 1;
				}

				taskList.add(new TaskModel(results.getInt("TaskID"), results.getInt("Tasks.ProgramID"),
						results.getString("TaskName"), results.getString("Location"), results.getInt("NumLeadersReqd"),
						results.getInt("TotalPersonsReqd"), dowBool, womBool,
						new TimeModel(results.getInt("Hour"), results.getInt("Minute")), results.getInt("Color")));
			}
			results.close();
			selectStmt.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return taskList;
	}

	private static void addTasksByProgram(ProgramModel program, int progID) {
		try {
			PreparedStatement checkTaskStmt = dbConnection
					.prepareStatement("SELECT COUNT(*) AS count FROM Tasks WHERE TaskName=?");
			ResultSet result = null;
			LinkedList<TaskModel> tasks = program.getTaskList();

			// Add task for this program
			for (int j = 0; j < tasks.size(); j++) {
				TaskModel thisTask = tasks.get(j);
				String taskName = thisTask.getTaskName();
				checkTaskStmt.setString(1, taskName);
				result = checkTaskStmt.executeQuery();
				result.next();

				if (result.getInt("count") == 0) {
					// Add new task
					addTask(progID, taskName, thisTask.getLocation(), thisTask.getNumLeadersReqd(), thisTask.getTotalPersonsReqd(),
							thisTask.getDayOfWeek(), thisTask.getWeekOfMonth(), thisTask.getTime(), thisTask.getColor());
				}
			}
			if (result != null)
				result.close();
			checkTaskStmt.close();

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

	/*
	 * ------- Programs -------
	 */
	public static int addProgram(String programName, String startDate, String endDate) {
		int progID = 0;

		try {
			PreparedStatement addProgramStmt = dbConnection.prepareStatement(
					"INSERT INTO Programs (ProgramName, StartDate, EndDate) VALUES (?, ?, ?)",
					Statement.RETURN_GENERATED_KEYS);

			// Add new program
			int col = 1;
			addProgramStmt.setString(col++, programName);
			addProgramStmt.setString(col++, startDate);
			addProgramStmt.setString(col++, endDate);

			addProgramStmt.executeUpdate();
			ResultSet result = addProgramStmt.getGeneratedKeys();
			result.next();
			progID = result.getInt(1);

			result.close();
			addProgramStmt.close();

		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		return progID;
	}

	/*
	 * ------- Task data -------
	 */
	public static void addTask(int progID, String taskName, String location, int numLeadersReqd, int totalPersonsReqd,
			boolean[] dayOfWeek, boolean[] weekOfMonth, TimeModel time, int color) {
		try {
			PreparedStatement addTaskStmt = dbConnection.prepareStatement(
					"INSERT INTO Tasks (ProgramID, TaskName, Hour, Minute, Location, NumLeadersReqd, TotalPersonsReqd, "
							+ "DaysOfWeek, DowInMonth, Color) VALUES " + "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			int dow = 0, wom = 0;

			for (int k = 0; k < 7; k++)
				dow = (dow << 1) | (dayOfWeek[k] ? 1 : 0);
			for (int k = 0; k < 5; k++)
				wom = (wom << 1) | (weekOfMonth[k] ? 1 : 0);

			int col = 1;
			addTaskStmt.setInt(col++, progID);
			addTaskStmt.setString(col++, taskName);
			addTaskStmt.setInt(col++, time.get24Hour());
			addTaskStmt.setInt(col++, time.getMinute());
			addTaskStmt.setString(col++, location);
			addTaskStmt.setInt(col++, numLeadersReqd);
			addTaskStmt.setInt(col++, totalPersonsReqd);
			addTaskStmt.setInt(col++, dow);
			addTaskStmt.setInt(col++, wom);
			addTaskStmt.setInt(col++, color);

			addTaskStmt.executeUpdate();
			addTaskStmt.close();

		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}
}
