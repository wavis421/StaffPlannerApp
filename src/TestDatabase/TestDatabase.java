package TestDatabase;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;

import com.mysql.jdbc.exceptions.jdbc4.CommunicationsException;

import model.AssignedTasksModel;
import model.CalendarDayModel;
import model.DateRangeModel;
import model.PersonModel;
import model.ProgramModel;
import model.SingleInstanceTaskModel;
import model.TaskModel;
import model.TimeModel;
import utilities.Utilities;

public class TestDatabase {
	private static Connection dbConnection;
	private static boolean initialized = false;

	private static JFrame frame = new JFrame("Database connector");
	private static JButton connectButton = new JButton();
	private static JButton showRosterButton = new JButton("Show Roster");
	private static JButton showTasksButton = new JButton("Show Tasks");
	private static ArrayList<PersonModel> personList;
	private static ArrayList<ProgramModel> programList;

	public static void initializeDatabase() {
		if (initialized)
			return;

		connectDatabase();
		updateConnectionStatus();

		connectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (connectButton.getText().equals("Disconnect")) {
					disconnectDatabase();
				} else if (!isDatabaseConnected()) {
					connectDatabase();
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

		// Create icon
		ImageIcon img = new ImageIcon("PPicon24.png");
		frame.setIconImage(img.getImage());

		frame.setLayout(new FlowLayout(FlowLayout.CENTER));
		frame.add(connectButton);
		frame.add(showRosterButton);
		frame.add(showTasksButton);
		frame.pack();
		frame.setVisible(true);

		initialized = true;
	}

	/*
	 * ------- Database Connections -------
	 */
	public static void connectDatabase() {
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();

		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			System.out.println("Unable to connect to database: " + e.getMessage());
			return;
		}

		try {
			String url = "jdbc:mysql://www.programplanner.org:3306/ProgramPlanner";
			dbConnection = DriverManager.getConnection(url, "wavisTester1", "ImGladToBeTesting555&");
			return;

		} catch (SQLException e) {
			System.out.println("Unable to connect to database: " + e.getMessage());
			return;
		}
	}

	public static void disconnectDatabase() {
		if (dbConnection != null)
			try {
				dbConnection.close();
				dbConnection = null;
			} catch (SQLException e) {
				System.out.println("Failure closing database connection: " + e.getMessage());
			}
	}

	public static boolean isDatabaseConnected() {
		if (dbConnection == null)
			return false;

		try {
			System.out.println("Attempting to connect to database...");
			return (dbConnection.isValid(15));

		} catch (SQLException e) {
			System.out.println("Check database connection: " + e.getMessage());
			return false;
		}
	}

	private static boolean checkDatabaseConnection() {
		// If database connection has been lost, try re-connecting
		if (dbConnection == null)
			connectDatabase();

		return (dbConnection == null ? false : true);
	}

	private static void updateConnectionStatus() {
		if (dbConnection == null) {
			frame.setTitle("Disconnected");
			connectButton.setText("Connect");
		} else {
			frame.setTitle("Connected");
			connectButton.setText("Disconnect");
		}
	}

	/*
	 * ------- Showing Database Content -------
	 */
	public static void showRosterDatabase() {
		if (!checkDatabaseConnection())
			return;

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement checkStmt = dbConnection
						.prepareStatement("SELECT * FROM Persons ORDER BY PersonName;");
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
				break;

			} catch (CommunicationsException e) {
				if (i == 0) {
					// First attempt to connect
					System.out.println("Attempting to re-connect to database...");
					connectDatabase();
				} else
					// Second try
					System.out.println("Unable to connect to database: " + e.getMessage());

			} catch (SQLException e) {
				System.out.println(e.getMessage());
				break;
			}
		}
	}

	public static void showTasksDatabase() {
		if (!checkDatabaseConnection())
			return;

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement checkStmt = dbConnection.prepareStatement("SELECT * FROM Tasks, Programs "
						+ "WHERE Tasks.ProgramID = Programs.ProgramID " + "ORDER BY Hour, Minute;");
				ResultSet result = checkStmt.executeQuery();
				int row;

				while (true) {
					result.next();

					row = result.getRow();
					if (row == 0)
						break;

					TimeModel time = new TimeModel(result.getInt("Hour"), result.getInt("Minute"));
					String location = result.getString("Location");
					if (location == null || location.equals(""))
						System.out.println("Row " + row + ": " + result.getString("ProgramName") + " - "
								+ result.getString("TaskName") + " at " + time.toString());
					else
						System.out.println("Row " + row + ": " + result.getString("ProgramName") + " - "
								+ result.getString("TaskName") + " at " + time.toString() + " in "
								+ result.getString("Location"));
				}
				result.close();
				checkStmt.close();
				break;

			} catch (CommunicationsException e) {
				if (i == 0) {
					// First attempt to connect
					System.out.println("Attempting to re-connect to database...");
					connectDatabase();
				} else
					// Second try
					System.out.println("Unable to connect to database: " + e.getMessage());

			} catch (SQLException e) {
				System.out.println(e.getMessage());
				break;
			}
		}
	}

	/*
	 * ------- Importing Files to SQL Database -------
	 */
	public static void importPersonDatabase(ArrayList<PersonModel> persons) {
		if (!checkDatabaseConnection())
			return;

		personList = persons;
		int personID = 0;

		for (int k = 0; k < 2; k++) {
			try {
				PreparedStatement checkStmt = dbConnection.prepareStatement(
						"SELECT COUNT(*) AS count, PersonID as personID FROM Persons WHERE PersonName=?;");
				ResultSet result = null;

				for (int i = 0; i < personList.size(); i++) {
					String personName = personList.get(i).getName();
					checkStmt.setString(1, personName);
					result = checkStmt.executeQuery();
					result.next();

					PersonModel person = personList.get(i);
					if (result.getInt("count") == 0) {
						// Add new person
						personID = addPerson(person.getName(), person.getPhone(), person.getEmail(), person.isLeader(),
								person.getNotes());
						System.out.println("ImportPersonDatabase (count = 0): personID = " + personID + ", name = "
								+ person.getName());
					} else {
						// Program already exists
						personID = result.getInt("personID");
						System.out.println("ImportPersonDatabase (count = " + result.getInt("count") + "): personID = "
								+ personID + ", name = " + person.getName());
					}
					// importAssignedTasks(person.getAssignedTasks(), personID);
					// importSingleInstanceTasks(person.getSingleInstanceTasks(),
					// personID);
					importUnavailDates(person.getDatesUnavailable(), personID);
				}
				if (result != null)
					result.close();
				checkStmt.close();
				break;

			} catch (CommunicationsException e) {
				if (k == 0) {
					// First attempt to connect
					System.out.println("Attempting to re-connect to database...");
					connectDatabase();
				} else
					// Second try
					System.out.println("Unable to connect to database: " + e.getMessage());

			} catch (SQLException e) {
				System.out.println(e.getMessage());
				break;
			}
		}
	}

	// TODO: Figure out how to import assigned tasks
	public static void importAssignedTasks(ArrayList<AssignedTasksModel> assignedTasks, int personID) {
		if (!checkDatabaseConnection())
			return;

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement checkTaskStmt = dbConnection
						.prepareStatement("SELECT COUNT(*) AS count, TaskID, TaskName FROM Tasks WHERE TaskName=?;");
				ResultSet result = null;

				// Add assigned tasks for this person
				for (int j = 0; j < assignedTasks.size(); j++) {
					AssignedTasksModel assignedTask = assignedTasks.get(j);
					String taskName = "" /* TODO: assignedTask.getTaskName() */;
					checkTaskStmt.setString(1, taskName);
					result = checkTaskStmt.executeQuery();
					result.next();

					if (result.getInt("count") > 0) {
						// Task match found, add new assigned task
						addAssignedTask(personID, result.getInt("taskID"), assignedTask.getDaysOfWeek(),
								assignedTask.getWeeksOfMonth());
					} else
						System.out.println("Assigned task name '" + taskName + "' not found in Task Database!!");
				}
				if (result != null)
					result.close();
				checkTaskStmt.close();
				break;

			} catch (CommunicationsException e) {
				if (i == 0) {
					// First attempt to connect
					System.out.println("Attempting to re-connect to database...");
					connectDatabase();
				} else
					// Second try
					System.out.println("Unable to connect to database: " + e.getMessage());

			} catch (SQLException e) {
				System.out.println(e.getMessage());
			}
		}
	}

	public static void importUnavailDates(ArrayList<DateRangeModel> unavailDates, int personID) {
		if (!checkDatabaseConnection())
			return;

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement checkDateRangeStmt = dbConnection.prepareStatement(
						"SELECT COUNT(*) AS count FROM UnavailDates, Persons WHERE " + "UnavailDates.PersonID=? AND "
								+ "UnavailDates.StartDate = ? AND UnavailDates.EndDate = ?;");
				ResultSet result = null;

				// Add Unavail Dates for this person
				for (int j = 0; j < unavailDates.size(); j++) {
					DateRangeModel dateRange = unavailDates.get(j);
					checkDateRangeStmt.setInt(1, personID);
					checkDateRangeStmt.setString(2, dateRange.getStartDate());
					checkDateRangeStmt.setString(3, dateRange.getEndDate());
					result = checkDateRangeStmt.executeQuery();
					result.next();

					if (result.getInt("count") == 0) {
						// Date range match NOT found, add new
						addUnavailDates(personID, dateRange.getStartDate(), dateRange.getEndDate());
					}
				}
				if (result != null)
					result.close();
				checkDateRangeStmt.close();
				break;

			} catch (CommunicationsException e) {
				if (i == 0) {
					// First attempt to connect
					System.out.println("Attempting to re-connect to database...");
					connectDatabase();
				} else
					// Second try
					System.out.println("Unable to connect to database: " + e.getMessage());

			} catch (SQLException e) {
				System.out.println(e.getMessage());
				break;
			}
		}
	}

	// TODO: Figure out how to import SingleInstanceTasks
	public static void importSingleInstanceTasks(ArrayList<SingleInstanceTaskModel> singleInstanceTask, int personID) {
		if (!checkDatabaseConnection())
			return;

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement checkTaskStmt = dbConnection.prepareStatement(
						"SELECT COUNT(*) AS count, Tasks.TaskID as taskID, Persons.PersonID as personID, Persons.PersonName as personName "
								+ "FROM SingleInstanceTasks, Tasks, Persons "
								+ "WHERE Persons.PersonID = SingleInstanceTasks.PersonID "
								+ "AND SingleInstanceTasks.DateTime=? "
								+ "AND ((SingleInstanceTasks.TaskID IS NULL) || (!(SingleInstanceTasks.TaskID IS NULL) && SingleInstanceTasks.TaskID=?));");
				ResultSet result = null;

				// Add Single Instance Tasks (subs, floaters) for this person
				for (int j = 0; j < singleInstanceTask.size(); j++) {
					SingleInstanceTaskModel singleTask = singleInstanceTask.get(j);
					checkTaskStmt.setTimestamp(1,
							java.sql.Timestamp.valueOf(Utilities.getSqlTimestamp(singleTask.getTaskDate())));
					checkTaskStmt.setInt(2, 200);
					result = checkTaskStmt.executeQuery();
					result.next();

					System.out.println(
							result.getInt("personID") + " " + result.getString("personName") + ": Single Task = "
									+ "" /* TODO: singleTask.getTaskName() */ + ", count = " + result.getInt("count")
									+ ", taskID = " + result.getInt("taskID"));
					if (result.getInt("count") == 0) {
						// Single instance task match NOT found, add new
						addSingleInstanceTask(personID, result.getInt("taskID"), singleTask.getTaskDate());
					}
				}
				if (result != null)
					result.close();
				checkTaskStmt.close();
				break;

			} catch (CommunicationsException e) {
				if (i == 0) {
					// First attempt to connect
					System.out.println("Attempting to re-connect to database...");
					connectDatabase();
				} else
					// Second try
					System.out.println("Unable to connect to database: " + e.getMessage());

			} catch (SQLException e) {
				System.out.println(e.getMessage());
				break;
			}
		}
	}

	public static void importProgramDatabase(ArrayList<ProgramModel> programs) {
		if (!checkDatabaseConnection())
			return;

		programList = programs;
		int progID = 0;

		for (int k = 0; k < 2; k++) {
			try {
				PreparedStatement checkProgramStmt = dbConnection.prepareStatement(
						"SELECT COUNT(*) AS count, ProgramID as progID FROM Programs WHERE ProgramName=?;");
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
					importTasksByProgram(program, progID);
				}
				if (result != null)
					result.close();
				checkProgramStmt.close();
				break;

			} catch (CommunicationsException e) {
				if (k == 0) {
					// First attempt to connect
					System.out.println("Attempting to re-connect to database...");
					connectDatabase();
				} else
					// Second try
					System.out.println("Unable to connect to database: " + e.getMessage());

			} catch (SQLException e) {
				System.out.println(e.getMessage());
				break;
			}
		}
	}

	private static void importTasksByProgram(ProgramModel program, int progID) {
		if (!checkDatabaseConnection())
			return;

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement checkTaskStmt = dbConnection
						.prepareStatement("SELECT COUNT(*) AS count FROM Tasks WHERE TaskName=?;");
				ResultSet result = null;
				ArrayList<TaskModel> tasks = program.getTaskList();

				// Add task for this program
				for (int j = 0; j < tasks.size(); j++) {
					TaskModel thisTask = tasks.get(j);
					String taskName = thisTask.getTaskName();
					checkTaskStmt.setString(1, taskName);
					result = checkTaskStmt.executeQuery();
					result.next();

					if (result.getInt("count") == 0) {
						// Add new task
						addTask(progID, taskName, thisTask.getLocation(), thisTask.getNumLeadersReqd(),
								thisTask.getTotalPersonsReqd(), thisTask.getDayOfWeek(), thisTask.getWeekOfMonth(),
								thisTask.getTime(), thisTask.getColor());
					}
				}
				if (result != null)
					result.close();
				checkTaskStmt.close();
				break;

			} catch (CommunicationsException e) {
				if (i == 0) {
					// First attempt to connect
					System.out.println("Attempting to re-connect to database...");
					connectDatabase();
				} else
					// Second try
					System.out.println("Unable to connect to database: " + e.getMessage());

			} catch (SQLException e) {
				System.out.println(e.getMessage());
				break;
			}
		}
	}

	/*
	 * ------- Load SQL Database into Application -------
	 */
	public static ArrayList<ProgramModel> loadPrograms() {
		ArrayList<ProgramModel> progList = new ArrayList<ProgramModel>();
		if (!checkDatabaseConnection())
			return progList;

		for (int i = 0; i < 2; i++) {
			try {
				Statement selectStmt = dbConnection.createStatement();
				ResultSet results = selectStmt.executeQuery(
						"SELECT ProgramID, ProgramName, StartDate, EndDate FROM Programs ORDER BY ProgramName;");

				while (results.next()) {
					int progID = results.getInt("ProgramID");
					progList.add(new ProgramModel(progID, results.getString("ProgramName"),
							Utilities.convertSqlDateToString(results.getDate("StartDate")),
							Utilities.convertSqlDateToString(results.getDate("EndDate")), loadTasksByProgram(progID)));
				}
				results.close();
				selectStmt.close();
				break;

			} catch (CommunicationsException e) {
				if (i == 0) {
					// First attempt to connect
					System.out.println("Attempting to re-connect to database...");
					connectDatabase();
				} else
					// Second try
					System.out.println("Unable to connect to database: " + e.getMessage());

			} catch (SQLException e) {
				System.out.println("Failure loading Programs from database: " + e.getMessage());
				break;
			}
		}
		return progList;
	}

	private static ArrayList<TaskModel> loadTasksByProgram(int progID) {
		ArrayList<TaskModel> taskList = new ArrayList<TaskModel>();
		if (!checkDatabaseConnection())
			return taskList;

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement selectStmt = dbConnection.prepareStatement(
						"SELECT TaskID, Tasks.ProgramID, Programs.ProgramID, TaskName, Location, NumLeadersReqd, TotalPersonsReqd, "
								+ "Color, DaysOfWeek, DowInMonth, Hour, Minute FROM Programs, Tasks "
								+ "WHERE Programs.ProgramID = Tasks.ProgramID AND Programs.ProgramID=? ORDER BY Hour, Minute;");
				selectStmt.setInt(1, progID);
				ResultSet results = selectStmt.executeQuery();

				while (results.next()) {
					taskList.add(new TaskModel(results.getInt("TaskID"), results.getInt("Tasks.ProgramID"),
							results.getString("TaskName"), results.getString("Location"),
							results.getInt("NumLeadersReqd"), results.getInt("TotalPersonsReqd"),
							createDaysOfWeekArray(results.getInt("DaysOfWeek")),
							createDowInMonthArray(results.getInt("DowInMonth")),
							new TimeModel(results.getInt("Hour"), results.getInt("Minute")), results.getInt("Color")));
				}
				results.close();
				selectStmt.close();
				break;

			} catch (CommunicationsException e) {
				if (i == 0) {
					// First attempt to connect
					System.out.println("Attempting to re-connect to database...");
					connectDatabase();
				} else
					// Second try
					System.out.println("Unable to connect to database: " + e.getMessage());

			} catch (SQLException e) {
				System.out.println("Failure loading Tasks from database: " + e.getMessage());
				break;
			}
		}
		return taskList;
	}

	public static ArrayList<PersonModel> loadRoster() {
		System.out.println("Enter LOAD ROSTER at " + Calendar.getInstance().getTime());
		ArrayList<PersonModel> personList = new ArrayList<PersonModel>();
		if (!checkDatabaseConnection())
			return personList;
		System.out.println("CONNECTED at " + Calendar.getInstance().getTime());

		for (int i = 0; i < 2; i++) {
			try {
				Statement selectStmt = dbConnection.createStatement();
				ResultSet results = selectStmt.executeQuery("SELECT Persons.PersonID, PersonName, "
						+ "PhoneNumber, EMail, isLeader, Notes FROM Persons ORDER BY PersonName;");

				while (results.next()) {
					// Add person
					int personID = results.getInt("PersonID");
					String personName = results.getString("PersonName");
					personList.add(new PersonModel(results.getInt("PersonID"), personName,
							results.getString("PhoneNumber"), results.getString("EMail"),
							results.getBoolean("isLeader"), results.getString("Notes"), loadAssignedTasks(personID),
							loadUnavailDates(personID), loadSingleInstanceTasks(personID)));
				}
				results.close();
				selectStmt.close();
				break;

			} catch (CommunicationsException e) {
				if (i == 0) {
					// First attempt to connect
					System.out.println("Attempting to re-connect to database...");
					connectDatabase();
				} else
					// Second try
					System.out.println("Unable to connect to database: " + e.getMessage());

			} catch (SQLException e) {
				System.out.println("Failure loading Roster from database: " + e.getMessage());
				break;
			}
		}

		System.out.println("Exit LOAD ROSTER at " + Calendar.getInstance().getTime());
		return personList;
	}

	private static ArrayList<AssignedTasksModel> loadAssignedTasks(int personID) {
		ArrayList<AssignedTasksModel> assignedTasksList = new ArrayList<AssignedTasksModel>();
		if (!checkDatabaseConnection())
			return assignedTasksList;

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement selectStmt = dbConnection.prepareStatement(
						"SELECT AssignedTasks.AssignedTaskID AS AssignedTaskID, AssignedTasks.PersonID, "
						+ "AssignedTasks.TaskID, AssignedTasks.DaysOfWeek, "
								+ "AssignedTasks.DowInMonth, Tasks.TaskName, ProgramName, Hour, Minute "
								+ "FROM AssignedTasks, Tasks, Programs WHERE AssignedTasks.PersonID=? "
								+ "AND AssignedTasks.TaskID = Tasks.TaskID AND Tasks.ProgramID = Programs.ProgramID "
								+ "ORDER BY Tasks.Hour, Tasks.Minute;");
				selectStmt.setInt(1, personID);
				ResultSet results = selectStmt.executeQuery();

				while (results.next()) {
					// Add assigned tasks
					assignedTasksList.add(new AssignedTasksModel(results.getInt("AssignedTaskID"),
							results.getInt("AssignedTasks.PersonID"), results.getInt("AssignedTasks.TaskID"),
							results.getString("ProgramName"), results.getString("Tasks.TaskName"),
							createDaysOfWeekArray(results.getInt("DaysOfWeek")),
							createDowInMonthArray(results.getInt("DowInMonth"))));
				}
				results.close();
				selectStmt.close();
				break;

			} catch (CommunicationsException e) {
				if (i == 0) {
					// First attempt to connect
					System.out.println("Attempting to re-connect to database...");
					connectDatabase();
				} else
					// Second try
					System.out.println("Unable to connect to database: " + e.getMessage());

			} catch (SQLException e) {
				System.out.println("Failure loading Assigned Tasks by PersonID from database: " + e.getMessage());
				break;
			}
		}
		return assignedTasksList;
	}

	private static ArrayList<AssignedTasksModel> loadAssignedTasks() {
		ArrayList<AssignedTasksModel> assignedTasksList = new ArrayList<AssignedTasksModel>();
		if (!checkDatabaseConnection())
			return assignedTasksList;

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement selectStmt = dbConnection.prepareStatement(
						"SELECT AssignedTasks.AssignedTaskID, AssignedTasks.PersonID, AssignedTasks.TaskID, "
								+ "AssignedTasks.DaysOfWeek, AssignedTasks.DowInMonth, "
								+ "Tasks.TaskName, Tasks.Hour, Tasks.Minute, Programs.ProgramName "
								+ "FROM AssignedTasks, Tasks, Programs "
								+ "WHERE AssignedTasks.TaskID = Tasks.TaskID AND Programs.ProgramID = Tasks.ProgramID " 
								+ "ORDER BY Tasks.Hour, Tasks.Minute;");
				ResultSet results = selectStmt.executeQuery();

				while (results.next()) {
					// Add assigned tasks
					assignedTasksList.add(new AssignedTasksModel(results.getInt("AssignedTaskID"),
							results.getInt("AssignedTasks.PersonID"), results.getInt("AssignedTasks.TaskID"),
							results.getString("Programs.ProgramName"), results.getString("Tasks.TaskName"),
							createDaysOfWeekArray(results.getInt("DaysOfWeek")),
							createDowInMonthArray(results.getInt("DowInMonth"))));
				}
				results.close();
				selectStmt.close();
				break;

			} catch (CommunicationsException e) {
				if (i == 0) {
					// First attempt to connect
					System.out.println("Attempting to re-connect to database...");
					connectDatabase();
				} else
					// Second try
					System.out.println("Unable to connect to database: " + e.getMessage());

			} catch (SQLException e) {
				System.out.println("Failure loading Assigned Tasks from database: " + e.getMessage());
				break;
			}
		}
		return assignedTasksList;
	}

	private static ArrayList<DateRangeModel> loadUnavailDates(int personID) {
		ArrayList<DateRangeModel> unavailDatesList = new ArrayList<DateRangeModel>();
		if (!checkDatabaseConnection())
			return unavailDatesList;

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement selectStmt = dbConnection.prepareStatement(
						"SELECT UnavailDatesID, UnavailDates.PersonID, StartDate, EndDate FROM UnavailDates "
								+ "WHERE UnavailDates.PersonID=?;");
				selectStmt.setInt(1, personID);
				ResultSet results = selectStmt.executeQuery();

				while (results.next()) {
					// Add date range
					unavailDatesList.add(new DateRangeModel(results.getInt("UnavailDatesID"),
							results.getInt("PersonID"), results.getString("StartDate"), results.getString("EndDate")));
				}
				results.close();
				selectStmt.close();
				break;

			} catch (CommunicationsException e) {
				if (i == 0) {
					// First attempt to connect
					System.out.println("Attempting to re-connect to database...");
					connectDatabase();
				} else
					// Second try
					System.out.println("Unable to connect to database: " + e.getMessage());

			} catch (SQLException e) {
				System.out.println("Failure loading Unavailable Dates from database: " + e.getMessage());
				break;
			}
		}
		return unavailDatesList;
	}

	private static ArrayList<SingleInstanceTaskModel> loadSingleInstanceTasks(int personID) {
		ArrayList<SingleInstanceTaskModel> singleTasksList = new ArrayList<SingleInstanceTaskModel>();
		if (!checkDatabaseConnection())
			return singleTasksList;

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement selectStmt = dbConnection.prepareStatement(
						"SELECT SingleInstanceID, Tasks.TaskName, SingleInstanceTasks.PersonID, SingleInstanceTasks.TaskID, "
								+ "SingleInstanceTasks.SingleDate, SingleInstanceTasks.SingleTime, SingleInstanceTasks.Color "
								+ "FROM SingleInstanceTasks, Tasks " 
								+ "WHERE SingleInstanceTasks.PersonID=? AND SingleInstanceTasks.TaskID = Tasks.TaskID "
								+ "ORDER BY SingleInstanceTasks.SingleDate;");
				selectStmt.setInt(1, personID);
				ResultSet results = selectStmt.executeQuery();

				while (results.next()) {
					// Initialize taskname and timestamp
					Calendar cal = Utilities.convertSqlDateTime(results.getDate("SingleDate"),
							results.getTime("SingleTime"));
					String taskName = "";
					int taskId = results.getInt("TaskID");

					// Add Single Instance Tasks
					singleTasksList.add(new SingleInstanceTaskModel(results.getInt("SingleInstanceID"),
							results.getInt("SingleInstanceTasks.personID"),
							results.getInt("SingleInstanceTasks.TaskID"), 
							results.getString("TaskName"), cal, results.getInt("Color")));
				}
				results.close();
				selectStmt.close();
				break;

			} catch (CommunicationsException e) {
				if (i == 0) {
					// First attempt to connect
					System.out.println("Attempting to re-connect to database...");
					connectDatabase();
				} else
					// Second try
					System.out.println("Unable to connect to database: " + e.getMessage());

			} catch (SQLException e) {
				System.out.println("Failure loading Single Instance Tasks from database: " + e.getMessage());
				break;
			}
		}

		return singleTasksList;
	}

	public static JList<String> getProgramList() {
		DefaultListModel<String> dbModel = new DefaultListModel<String>();
		JList<String> programList = new JList<String>(dbModel);

		if (!checkDatabaseConnection())
			return programList;

		for (int i = 0; i < 2; i++) {
			try {
				Statement selectStmt = dbConnection.createStatement();
				ResultSet results = selectStmt.executeQuery("SELECT ProgramName FROM Programs ORDER BY ProgramName;");

				while (results.next()) {
					dbModel.addElement(new String(results.getString("ProgramName")));
				}
				results.close();
				selectStmt.close();
				break;

			} catch (CommunicationsException e) {
				if (i == 0) {
					// First attempt to connect
					System.out.println("Attempting to re-connect to database...");
					connectDatabase();
				} else
					// Second try
					System.out.println("Unable to connect to database: " + e.getMessage());

			} catch (SQLException e) {
				System.out.println("Failure retrieving Program List: " + e.getMessage());
				break;
			}
		}
		return programList;
	}

	private static boolean[] createDaysOfWeekArray(int dow) {
		boolean[] dowBool = { false, false, false, false, false, false, false };
		for (int i = 0; i < 6; i++) {
			if ((dow & 1) == 1)
				dowBool[i] = true;
			dow >>= 1;
		}
		return dowBool;
	}

	private static boolean[] createDowInMonthArray(int wom) {
		boolean[] womBool = { false, false, false, false, false };
		for (int i = 0; i < 5; i++) {
			if ((wom & 1) == 1)
				womBool[i] = true;
			wom >>= 1;
		}
		return womBool;
	}

	private static int getDowAsInt(boolean[] dowArray) {
		int dow = 0;
		for (int k = 6; k >= 0; k--) {
			dow <<= 1;
			dow = dow | (dowArray[k] ? 1 : 0);
		}
		return dow;
	}

	private static int getWomAsInt(boolean[] womArray) {
		int wom = 0;
		for (int k = 4; k >= 0; k--) {
			wom <<= 1;
			wom = wom | (womArray[k] ? 1 : 0);
		}
		return wom;
	}

	/*
	 * ------- Programs Database addition/updates -------
	 */
	public static int addProgram(String programName, String startDate, String endDate) {
		int progID = 0;
		if (!checkDatabaseConnection())
			return -1;

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement addProgramStmt = dbConnection.prepareStatement(
						"INSERT INTO Programs (ProgramName, StartDate, EndDate) VALUES (?, ?, ?)",
						Statement.RETURN_GENERATED_KEYS);

				// Add new program
				int col = 1;
				addProgramStmt.setString(col++, programName);
				addProgramStmt.setDate(col++, java.sql.Date.valueOf(Utilities.getSqlDate(startDate)));
				addProgramStmt.setDate(col++, java.sql.Date.valueOf(Utilities.getSqlDate(endDate)));

				addProgramStmt.executeUpdate();
				ResultSet result = addProgramStmt.getGeneratedKeys();
				result.next();
				progID = result.getInt(1);

				result.close();
				addProgramStmt.close();
				break;

			} catch (CommunicationsException e) {
				if (i == 0) {
					// First attempt to connect
					System.out.println("Attempting to re-connect to database...");
					connectDatabase();
				} else
					// Second try
					System.out.println("Unable to connect to database: " + e.getMessage());

			} catch (SQLException e) {
				System.out.println("Failure adding program to databae: " + e.getMessage());
				break;
			}
		}
		return progID;
	}

	public static void updateProgramName(int programID, String progName) {
		if (!checkDatabaseConnection())
			return;

		for (int i = 0; i < 2; i++) {
			try {
				// Update program name
				PreparedStatement updateProgramStmt = dbConnection
						.prepareStatement("UPDATE Programs SET ProgramName=? WHERE ProgramID=?;");
				updateProgramStmt.setString(1, progName);
				updateProgramStmt.setInt(2, programID);

				updateProgramStmt.executeUpdate();
				updateProgramStmt.close();
				break;

			} catch (CommunicationsException e) {
				if (i == 0) {
					// First attempt to connect
					System.out.println("Attempting to re-connect to database...");
					connectDatabase();
				} else
					// Second try
					System.out.println("Unable to connect to database: " + e.getMessage());

			} catch (SQLException e) {
				System.out.println("Failure updating Program Name in database: " + e.getMessage());
				break;
			}
		}
	}

	public static void updateProgramDates(int programID, String startDate, String endDate) {
		if (!checkDatabaseConnection())
			return;

		for (int i = 0; i < 2; i++) {
			try {
				// Update program name
				PreparedStatement updateProgramStmt = dbConnection
						.prepareStatement("UPDATE Programs SET StartDate=?, EndDate=? WHERE ProgramID=?;");
				updateProgramStmt.setDate(1, java.sql.Date.valueOf(Utilities.getSqlDate(startDate)));
				updateProgramStmt.setDate(2, java.sql.Date.valueOf(Utilities.getSqlDate(endDate)));
				updateProgramStmt.setInt(3, programID);

				updateProgramStmt.executeUpdate();
				updateProgramStmt.close();
				break;

			} catch (CommunicationsException e) {
				if (i == 0) {
					// First attempt to connect
					System.out.println("Attempting to re-connect to database...");
					connectDatabase();
				} else
					// Second try
					System.out.println("Unable to connect to database: " + e.getMessage());

			} catch (SQLException e) {
				System.out.println("Failure updating Program Dates in database: " + e.getMessage());
				break;
			}
		}
	}

	/*
	 * ------- Task Database additions/updates -------
	 */
	public static int addTask(int progID, String taskName, String location, int numLeadersReqd, int totalPersonsReqd,
			boolean[] dayOfWeek, boolean[] weekOfMonth, TimeModel time, int color) {
		int taskID = 0;
		if (!checkDatabaseConnection())
			return -1;

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement addTaskStmt = dbConnection.prepareStatement(
						"INSERT INTO Tasks (ProgramID, TaskName, Hour, Minute, Location, NumLeadersReqd, TotalPersonsReqd, "
								+ "DaysOfWeek, DowInMonth, Color) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
						Statement.RETURN_GENERATED_KEYS);

				int col = 1;
				addTaskStmt.setInt(col++, progID);
				addTaskStmt.setString(col++, taskName);
				addTaskStmt.setInt(col++, time.get24Hour());
				addTaskStmt.setInt(col++, time.getMinute());
				addTaskStmt.setString(col++, location);
				addTaskStmt.setInt(col++, numLeadersReqd);
				addTaskStmt.setInt(col++, totalPersonsReqd);
				addTaskStmt.setInt(col++, getDowAsInt(dayOfWeek));
				addTaskStmt.setInt(col++, getWomAsInt(weekOfMonth));
				addTaskStmt.setInt(col++, color);

				addTaskStmt.executeUpdate();
				ResultSet result = addTaskStmt.getGeneratedKeys();
				result.next();
				taskID = result.getInt(1);

				result.close();
				addTaskStmt.close();
				break;

			} catch (CommunicationsException e) {
				if (i == 0) {
					// First attempt to connect
					System.out.println("Attempting to re-connect to database...");
					connectDatabase();
				} else
					// Second try
					System.out.println("Unable to connect to database: " + e.getMessage());

			} catch (SQLException e) {
				System.out.println("Failure adding task to database: " + e.getMessage());
				break;
			}
		}
		return taskID;
	}

	public static void updateTaskName(int taskID, String taskName) {
		if (!checkDatabaseConnection())
			return;

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement updateTaskStmt = dbConnection
						.prepareStatement("UPDATE Tasks SET TaskName=? WHERE TaskID=?;");

				int col = 1;
				updateTaskStmt.setString(col++, taskName);
				updateTaskStmt.setInt(col++, taskID);

				updateTaskStmt.executeUpdate();
				updateTaskStmt.close();
				break;

			} catch (CommunicationsException e) {
				if (i == 0) {
					// First attempt to connect
					System.out.println("Attempting to re-connect to database...");
					connectDatabase();
				} else
					// Second try
					System.out.println("Unable to connect to database: " + e.getMessage());

			} catch (SQLException e) {
				System.out.println("Failure updating task name in database: " + e.getMessage());
				break;
			}
		}
	}

	public static void updateTaskFields(int taskID, String taskName, String location, int numLeadersReqd,
			int totalPersonsReqd, boolean[] dayOfWeek, boolean[] weekOfMonth, TimeModel time, int color) {
		if (!checkDatabaseConnection())
			return;

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement updateTaskStmt = dbConnection.prepareStatement(
						"UPDATE Tasks SET TaskName=?, Location=?, NumLeadersReqd=?, TotalPersonsReqd=?, DaysOfWeek=?, DowInMonth=?, "
								+ "Hour=?, Minute=?, Color=? WHERE TaskID=?;");

				int col = 1;
				updateTaskStmt.setString(col++, taskName);
				updateTaskStmt.setString(col++, location);
				updateTaskStmt.setInt(col++, numLeadersReqd);
				updateTaskStmt.setInt(col++, totalPersonsReqd);
				updateTaskStmt.setInt(col++, getDowAsInt(dayOfWeek));
				updateTaskStmt.setInt(col++, getWomAsInt(weekOfMonth));
				updateTaskStmt.setInt(col++, time.get24Hour());
				updateTaskStmt.setInt(col++, time.getMinute());
				updateTaskStmt.setInt(col++, color);
				updateTaskStmt.setInt(col++, taskID);

				updateTaskStmt.executeUpdate();
				updateTaskStmt.close();
				break;

			} catch (CommunicationsException e) {
				if (i == 0) {
					// First attempt to connect
					System.out.println("Attempting to re-connect to database...");
					connectDatabase();
				} else
					// Second try
					System.out.println("Unable to connect to database: " + e.getMessage());

			} catch (SQLException e) {
				System.out.println("Failure updating task to database: " + e.getMessage());
				break;
			}
		}
	}

	/*
	 * ------- Person Database additions/updates -------
	 */
	public static int addPerson(String personName, String phone, String email, boolean leader, String notes) {
		int personID = 0;
		if (!checkDatabaseConnection())
			return -1;

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement addPersonStmt = dbConnection
						.prepareStatement("INSERT INTO Persons (PersonName, PhoneNumber, EMail, isLeader, Notes) "
								+ " VALUES (?, ?, ?, ?, ?);", Statement.RETURN_GENERATED_KEYS);

				// Add new person
				int col = 1;
				addPersonStmt.setString(col++, personName);
				addPersonStmt.setString(col++, phone);
				addPersonStmt.setString(col++, email);
				addPersonStmt.setBoolean(col++, leader);
				addPersonStmt.setString(col, notes);

				addPersonStmt.executeUpdate();
				ResultSet result = addPersonStmt.getGeneratedKeys();
				result.next();
				personID = result.getInt(1);

				result.close();
				addPersonStmt.close();
				break;

			} catch (CommunicationsException e) {
				if (i == 0) {
					// First attempt to connect
					System.out.println("Attempting to re-connect to database...");
					connectDatabase();
				} else
					// Second try
					System.out.println("Unable to connect to database: " + e.getMessage());

			} catch (SQLException e) {
				System.out.println("Failure adding Person to database: " + e.getMessage());
				break;
			}
		}
		return personID;
	}

	public static void updatePerson(int personID, String personName, String phone, String email, boolean leader,
			String notes) {
		if (!checkDatabaseConnection())
			return;

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement updatePersonStmt = dbConnection.prepareStatement(
						"UPDATE Persons SET PersonName=?, PhoneNumber=?, EMail=?, isLeader=?, Notes=? "
								+ "WHERE PersonID=?;");

				// Add new person
				int col = 1;
				updatePersonStmt.setString(col++, personName);
				updatePersonStmt.setString(col++, phone);
				updatePersonStmt.setString(col++, email);
				updatePersonStmt.setBoolean(col++, leader);
				updatePersonStmt.setString(col++, notes);
				updatePersonStmt.setInt(col, personID);

				updatePersonStmt.executeUpdate();
				updatePersonStmt.close();
				break;

			} catch (CommunicationsException e) {
				if (i == 0) {
					// First attempt to connect
					System.out.println("Attempting to re-connect to database...");
					connectDatabase();
				} else
					// Second try
					System.out.println("Unable to connect to database: " + e.getMessage());

			} catch (SQLException e) {
				System.out.println("Failure updating Person in database: " + e.getMessage());
				break;
			}
		}
	}

	public static int addAssignedTask(int personID, int taskID, boolean[] daysOfWeek, boolean[] weeksOfMonth) {
		int assignedTaskID = 0;
		if (!checkDatabaseConnection())
			return -1;

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement addAssignedTaskStmt = dbConnection.prepareStatement(
						"INSERT INTO AssignedTasks (PersonID, taskID, DaysOfWeek, DowInMonth) VALUES (?, ?, ?, ?)",
						Statement.RETURN_GENERATED_KEYS);

				// Add new assigned task
				int col = 1;
				addAssignedTaskStmt.setInt(col++, personID);
				addAssignedTaskStmt.setInt(col++, taskID);
				addAssignedTaskStmt.setInt(col++, getDowAsInt(daysOfWeek));
				addAssignedTaskStmt.setInt(col++, getWomAsInt(weeksOfMonth));

				addAssignedTaskStmt.executeUpdate();
				ResultSet result = addAssignedTaskStmt.getGeneratedKeys();
				result.next();
				assignedTaskID = result.getInt(1);

				result.close();
				addAssignedTaskStmt.close();
				break;

			} catch (CommunicationsException e) {
				if (i == 0) {
					// First attempt to connect
					System.out.println("Attempting to re-connect to database...");
					connectDatabase();
				} else
					// Second try
					System.out.println("Unable to connect to database: " + e.getMessage());

			} catch (SQLException e) {
				System.out.println("Failure adding task assignment: " + e.getMessage());
				break;
			}
		}
		return assignedTaskID;
	}

	public static void updateAssignedTask(int assignedTaskID, boolean[] daysOfWeek, boolean[] weeksOfMonth) {
		if (!checkDatabaseConnection())
			return;

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement updateAssignedTaskStmt = dbConnection.prepareStatement(
						"UPDATE AssignedTasks SET DaysOfWeek=?, DowInMonth=? WHERE AssignedTaskID=?;");

				// Add new assigned task
				int col = 1;
				updateAssignedTaskStmt.setInt(col++, getDowAsInt(daysOfWeek));
				updateAssignedTaskStmt.setInt(col++, getWomAsInt(weeksOfMonth));
				updateAssignedTaskStmt.setInt(col, assignedTaskID);

				updateAssignedTaskStmt.executeUpdate();
				updateAssignedTaskStmt.close();
				break;

			} catch (CommunicationsException e) {
				if (i == 0) {
					// First attempt to connect
					System.out.println("Attempting to re-connect to database...");
					connectDatabase();
				} else
					// Second try
					System.out.println("Unable to connect to database: " + e.getMessage());

			} catch (SQLException e) {
				System.out.println("Failure updating task assignment: " + e.getMessage());
				break;
			}
		}
	}

	public static int addUnavailDates(int personID, String startDate, String endDate) {
		int unavailDatesID = 0;
		if (!checkDatabaseConnection())
			return -1;

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement addUnavailDatesStmt = dbConnection.prepareStatement(
						"INSERT INTO UnavailDates (PersonID, StartDate, EndDate) VALUES (?, ?, ?)",
						Statement.RETURN_GENERATED_KEYS);

				// Add new Unavail Dates
				addUnavailDatesStmt.setInt(1, personID);
				addUnavailDatesStmt.setString(2, startDate);
				addUnavailDatesStmt.setString(3, endDate);

				addUnavailDatesStmt.executeUpdate();
				ResultSet result = addUnavailDatesStmt.getGeneratedKeys();
				result.next();
				unavailDatesID = result.getInt(1);

				result.close();
				addUnavailDatesStmt.close();
				break;

			} catch (CommunicationsException e) {
				if (i == 0) {
					// First attempt to connect
					System.out.println("Attempting to re-connect to database...");
					connectDatabase();
				} else
					// Second try
					System.out.println("Unable to connect to database: " + e.getMessage());

			} catch (SQLException e) {
				System.out.println("Failure adding unavailable dates: " + e.getMessage());
				break;
			}
		}
		return unavailDatesID;
	}

	public static int addSingleInstanceTask(int personID, int taskID, Calendar taskTime) {
		int singleInstanceID = 0;
		if (!checkDatabaseConnection())
			return -1;

		for (int i = 0; i < 2; i++) {
			int col = 1;
			PreparedStatement addSingleTaskStmt = null;
			try {
				if (taskID == 0) {
					addSingleTaskStmt = dbConnection.prepareStatement(
							"INSERT INTO SingleInstanceTasks (PersonID, SingleDate, SingleTime) VALUES (?, ?, ?)",
							Statement.RETURN_GENERATED_KEYS);
				} else {
					addSingleTaskStmt = dbConnection.prepareStatement(
							"INSERT INTO SingleInstanceTasks (PersonID, TaskId, SingleDate, SingleTime) VALUES (?, ?, ?, ?)",
							Statement.RETURN_GENERATED_KEYS);
				}

				// Add new Single Instance Task
				addSingleTaskStmt.setInt(col++, personID);
				if (taskID != 0)
					addSingleTaskStmt.setInt(col++, taskID);
				// addSingleTaskStmt.setTimestamp(3,
				// java.sql.Timestamp.valueOf(taskTime.toString()));
				addSingleTaskStmt.setDate(col++, java.sql.Date.valueOf(Utilities.getSqlDate(taskTime)));
				addSingleTaskStmt.setTime(col++, java.sql.Time.valueOf(Utilities.getSqlTime(taskTime)));

				addSingleTaskStmt.executeUpdate();
				ResultSet result = addSingleTaskStmt.getGeneratedKeys();
				result.next();
				singleInstanceID = result.getInt(1);

				result.close();
				addSingleTaskStmt.close();
				break;

			} catch (CommunicationsException e) {
				if (i == 0) {
					// First attempt to connect
					System.out.println("Attempting to re-connect to database...");
					connectDatabase();
				} else
					// Second try
					System.out.println("Unable to connect to database: " + e.getMessage());

			} catch (SQLException e) {
				System.out.println("Failure adding single instance task: " + e.getMessage());
				break;
			}
		}
		return singleInstanceID;
	}

	public static ArrayList<CalendarDayModel> updateMonth(String date) {
		ArrayList<CalendarDayModel> calendarList = new ArrayList<CalendarDayModel>();
		if (!checkDatabaseConnection())
			return calendarList;

		String taskName;
		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement updateMonthStmt = dbConnection
						.prepareStatement("CALL MonthlyCalendar('" + date + "');");
				ResultSet results = updateMonthStmt.executeQuery();

				while (results.next()) {
					// Add date range
					// calendarList.add(new CalendarDayModel());
					taskName = results.getString(2);
					if (taskName == null)
						System.out.println("Day " + results.getInt(1) + ": " + results.getInt(6) + " Floater(s) at "
								+ results.getInt(4) + ":" + results.getInt(5));
					else
						System.out.println("Day " + results.getInt(1) + ": " + taskName + " (" + results.getInt(3)
								+ ") at " + results.getInt(4) + ":" + results.getInt(5) + ", Persons "
								+ results.getInt(6) + "/" + results.getInt(8) + ", Leaders " + results.getInt(7) + "/"
								+ results.getInt(9));
				}
				results.close();
				updateMonthStmt.close();
				break;

			} catch (CommunicationsException e) {
				if (i == 0) {
					// First attempt to connect
					System.out.println("Attempting to re-connect to database...");
					connectDatabase();
				} else
					// Second try
					System.out.println("Unable to connect to database: " + e.getMessage());

			} catch (SQLException e) {
				System.out.println("Failure loading Calendar month from database: " + e.getMessage());
				break;
			}
		}
		return calendarList;
	}
}
