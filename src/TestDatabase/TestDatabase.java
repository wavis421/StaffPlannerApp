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
import java.util.LinkedList;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;

import model.AssignedTasksModel;
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
	private static LinkedList<PersonModel> personList;
	private static LinkedList<ProgramModel> programList;

	public static void initializeDatabase() {
		if (initialized)
			return;

		connectDatabase();
		updateConnectionStatus();

		connectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (connectButton.getText().equals("Disconnect")) {
					disconnectDatabase();
				} else {
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
	public static Boolean connectDatabase() {
		if (isDatabaseConnected())
			return true;

		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();

		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			System.out.println("Unable to connect to database: " + e.getMessage());
			return false;
		}

		try {
			String url = "jdbc:mysql://www.programplanner.org:3306/ProgramPlanner";
			dbConnection = DriverManager.getConnection(url, "wavisTester1", "ImGladToBeTesting555&");
			return true;

		} catch (SQLException e) {
			System.out.println("Unable to connect to database: " + e.getMessage());
			return false;
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
			return (dbConnection.isValid(30));

		} catch (SQLException e) {
			System.out.println("Check database connection: " + e.getMessage());
			return false;
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
	 * ------- Showing Database Content -------
	 */
	public static void showRosterDatabase() {
		try {
			PreparedStatement checkStmt = dbConnection.prepareStatement("SELECT * FROM Persons ORDER BY PersonName;");
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
			PreparedStatement checkStmt = dbConnection
					.prepareStatement("SELECT * FROM Tasks, Programs " + "WHERE Tasks.ProgramID = Programs.ProgramID " 
			+ "ORDER BY Hour, Minute;");
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
							+ result.getString("TaskName") + " at " + time.toString() + " in " + result.getString("Location"));
			}
			result.close();
			checkStmt.close();

		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

	/*
	 * ------- Importing Files to SQL Database -------
	 */
	public static void importPersonDatabase(LinkedList<PersonModel> persons) {
		personList = persons;
		int personID = 0;
		try {
			PreparedStatement checkStmt = dbConnection
					.prepareStatement("SELECT COUNT(*) AS count, PersonID as personID FROM Persons WHERE PersonName=?;");
			ResultSet result = null;

			for (int i = 0; i < personList.size(); i++) {
				String personName = personList.get(i).getName();
				checkStmt.setString(1, personName);
				result = checkStmt.executeQuery();
				result.next();

				PersonModel person = personList.get(i);
				if (result.getInt("count") == 0) {
					// Add new person
					personID = addPerson(person.getName(), person.getPhone(), person.getEmail(), person.isLeader(), person.getNotes());
					System.out.println("ImportPersonDatabase (count = 0): personID = " + personID + ", name = "
							+ person.getName());
				} else {
					// Program already exists
					personID = result.getInt("personID");
					System.out.println("ImportPersonDatabase (count = " + result.getInt("count") + "): personID = "
							+ personID + ", name = " + person.getName());
				}
				importAssignedTasks(person.getAssignedTasks(), personID);
				importUnavailDates(person.getDatesUnavailable(), personID);
				// importSingleInstanceTasks(person.getSingleInstanceTasks(),
				// personID);
			}
			if (result != null)
				result.close();
			checkStmt.close();

		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

	public static void importAssignedTasks(LinkedList<AssignedTasksModel> assignedTasks, int personID) {
		try {
			PreparedStatement checkTaskStmt = dbConnection
					.prepareStatement("SELECT COUNT(*) AS count, TaskID AS taskID FROM Tasks WHERE TaskName=?;");
			ResultSet result = null;

			// Add assigned tasks for this person
			for (int j = 0; j < assignedTasks.size(); j++) {
				AssignedTasksModel assignedTask = assignedTasks.get(j);
				String taskName = assignedTask.getTaskName();
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

		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

	public static void importUnavailDates(LinkedList<DateRangeModel> unavailDates, int personID) {
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

		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

	public static void importSingleInstanceTasks(LinkedList<SingleInstanceTaskModel> singleInstanceTask, int personID) {
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

				System.out.println(result.getInt("personID") + " " + result.getString("personName") + ": Single Task = "
						+ singleTask.getTaskName() + ", count = " + result.getInt("count") + ", taskID = "
						+ result.getInt("taskID"));
				if (result.getInt("count") == 0) {
					// Single instance task match NOT found, add new
					addSingleInstanceTask(personID, result.getInt("taskID"), singleTask.getTaskDate());
				}
			}
			if (result != null)
				result.close();
			checkTaskStmt.close();

		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

	public static void importProgramDatabase(LinkedList<ProgramModel> programs) {
		programList = programs;
		int progID = 0;
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

		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

	private static void importTasksByProgram(ProgramModel program, int progID) {
		try {
			PreparedStatement checkTaskStmt = dbConnection
					.prepareStatement("SELECT COUNT(*) AS count FROM Tasks WHERE TaskName=?;");
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
					addTask(progID, taskName, thisTask.getLocation(), thisTask.getNumLeadersReqd(),
							thisTask.getTotalPersonsReqd(), thisTask.getDayOfWeek(), thisTask.getWeekOfMonth(),
							thisTask.getTime(), thisTask.getColor());
				}
			}
			if (result != null)
				result.close();
			checkTaskStmt.close();

		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

	/*
	 * ------- Load SQL Database into Application -------
	 */
	public static LinkedList<ProgramModel> loadPrograms() {
		LinkedList<ProgramModel> progList = new LinkedList<ProgramModel>();
		if (!connectDatabase())
			return progList;

		try {
			Statement selectStmt = dbConnection.createStatement();
			ResultSet results = selectStmt.executeQuery(
					"SELECT ProgramID, ProgramName, StartDate, EndDate FROM Programs ORDER BY ProgramName;");

			while (results.next()) {
				int progID = results.getInt("ProgramID");
				progList.add(new ProgramModel(progID, results.getString("ProgramName"), results.getString("StartDate"),
						results.getString("EndDate"), loadTasksByProgram(progID)));
			}
			results.close();
			selectStmt.close();

		} catch (SQLException e) {
			System.out.println("Failure loading Programs from database: " + e.getMessage());
		}
		return progList;
	}

	private static LinkedList<TaskModel> loadTasksByProgram(int progID) {
		LinkedList<TaskModel> taskList = new LinkedList<TaskModel>();
		try {
			PreparedStatement selectStmt = dbConnection.prepareStatement(
					"SELECT TaskID, Tasks.ProgramID, Programs.ProgramID, TaskName, Location, NumLeadersReqd, TotalPersonsReqd, "
							+ "Color, DaysOfWeek, DowInMonth, Hour, Minute FROM Programs, Tasks "
							+ "WHERE Programs.ProgramID = Tasks.ProgramID AND Programs.ProgramID=? ORDER BY Hour, Minute;");
			selectStmt.setInt(1, progID);
			ResultSet results = selectStmt.executeQuery();

			while (results.next()) {
				taskList.add(new TaskModel(results.getInt("TaskID"), results.getInt("Tasks.ProgramID"),
						results.getString("TaskName"), results.getString("Location"), results.getInt("NumLeadersReqd"),
						results.getInt("TotalPersonsReqd"), createDaysOfWeekArray(results.getInt("DaysOfWeek")),
						createDowInMonthArray(results.getInt("DowInMonth")),
						new TimeModel(results.getInt("Hour"), results.getInt("Minute")), results.getInt("Color")));
			}
			results.close();
			selectStmt.close();

		} catch (SQLException e) {
			System.out.println("Failure loading Tasks from database: " + e.getMessage());
		}
		return taskList;
	}

	public static LinkedList<PersonModel> loadRoster() {
		LinkedList<PersonModel> personList = new LinkedList<PersonModel>();
		if (!connectDatabase())
			return personList;

		try {
			Statement selectStmt = dbConnection.createStatement();
			ResultSet results = selectStmt.executeQuery("SELECT Persons.PersonID, PersonName, "
					+ "PhoneNumber, EMail, isLeader, Notes FROM Persons ORDER BY PersonName;");

			while (results.next()) {
				// Add person
				int personID = results.getInt("PersonID");
				String personName = results.getString("PersonName");
				personList.add(new PersonModel(results.getInt("PersonID"), personName, results.getString("PhoneNumber"),
						results.getString("EMail"), results.getBoolean("isLeader"), results.getString("Notes"),
						loadAssignedTasks(personID), loadUnavailDates(personID), loadSingleInstanceTasks(personID)));
			}
			results.close();
			selectStmt.close();

		} catch (SQLException e) {
			System.out.println("Failure loading Roster from database: " + e.getMessage());
		}

		return personList;
	}

	private static LinkedList<AssignedTasksModel> loadAssignedTasks(int personID) {
		LinkedList<AssignedTasksModel> assignedTasksList = new LinkedList<AssignedTasksModel>();
		if (!connectDatabase())
			return assignedTasksList;

		try {
			PreparedStatement selectStmt = dbConnection.prepareStatement(
					"SELECT AssignedTaskID, AssignedTasks.PersonID, AssignedTasks.TaskID, AssignedTasks.DaysOfWeek, "
							+ "AssignedTasks.DowInMonth, TaskName, ProgramName, Hour, Minute "
							+ "FROM AssignedTasks, Tasks, Programs " + "WHERE AssignedTasks.PersonID=? "
							+ "AND AssignedTasks.TaskID = Tasks.TaskID " + "AND Tasks.ProgramID = Programs.ProgramID "
							+ "ORDER BY Tasks.Hour, Tasks.Minute;");
			selectStmt.setInt(1, personID);
			ResultSet results = selectStmt.executeQuery();

			while (results.next()) {
				// Add assigned tasks
				assignedTasksList.add(new AssignedTasksModel(results.getInt("AssignedTaskID"),
						results.getInt("AssignedTasks.PersonID"), results.getInt("AssignedTasks.TaskID"),
						results.getString("ProgramName"), results.getString("TaskName"),
						createDaysOfWeekArray(results.getInt("DaysOfWeek")),
						createDowInMonthArray(results.getInt("DowInMonth"))));
			}
			results.close();
			selectStmt.close();

		} catch (SQLException e) {
			System.out.println("Failure loading Assigned Tasks from database: " + e.getMessage());
		}

		return assignedTasksList;
	}

	private static LinkedList<DateRangeModel> loadUnavailDates(int personID) {
		LinkedList<DateRangeModel> unavailDatesList = new LinkedList<DateRangeModel>();
		if (!connectDatabase())
			return unavailDatesList;

		try {
			PreparedStatement selectStmt = dbConnection.prepareStatement(
					"SELECT UnavailDatesID, UnavailDates.PersonID, StartDate, EndDate FROM UnavailDates "
							+ "WHERE UnavailDates.PersonID=?;");
			selectStmt.setInt(1, personID);
			ResultSet results = selectStmt.executeQuery();

			while (results.next()) {
				// Add date range
				unavailDatesList.add(new DateRangeModel(results.getInt("UnavailDatesID"), results.getInt("PersonID"),
						results.getString("StartDate"), results.getString("EndDate")));
			}
			results.close();
			selectStmt.close();

		} catch (SQLException e) {
			System.out.println("Failure loading Unavailable Dates from database: " + e.getMessage());
		}

		return unavailDatesList;
	}

	private static LinkedList<SingleInstanceTaskModel> loadSingleInstanceTasks(int personID) {
		LinkedList<SingleInstanceTaskModel> singleTasksList = new LinkedList<SingleInstanceTaskModel>();
		if (!connectDatabase())
			return singleTasksList;

		try {
			PreparedStatement selectStmt = dbConnection.prepareStatement(
					"SELECT SingleInstanceID, SingleInstanceTasks.PersonID, SingleInstanceTasks.TaskID, "
							+ "SingleInstanceTasks.Date, SingleInstanceTasks.Time, SingleInstanceTasks.Color "
							+ "FROM SingleInstanceTasks " + "WHERE SingleInstanceTasks.PersonID=? " 
							+ "ORDER BY SingleInstanceTasks.Date;");
			selectStmt.setInt(1, personID);
			ResultSet results = selectStmt.executeQuery();

			while (results.next()) {
				// Initialize taskname and timestamp
				Calendar cal = Utilities.convertSqlDateTime(results.getDate("Date"), results.getTime("Time"));
				String taskName = "";
				int taskId = results.getInt("TaskID");
				if (taskId > 0) {
					// Get task name for SUBSTITUTE
					taskName = getTaskName(taskId);
				}

				// Add Single Instance Tasks
				singleTasksList.add(new SingleInstanceTaskModel(results.getInt("SingleInstanceID"),
						results.getInt("SingleInstanceTasks.personID"), results.getInt("SingleInstanceTasks.TaskID"),
						taskName, cal, results.getInt("Color")));
			}
			results.close();
			selectStmt.close();

		} catch (SQLException e) {
			System.out.println("Failure loading Single Instance Tasks from database: " + e.getMessage());
		}

		return singleTasksList;
	}

	public static JList<String> getProgramList () {
		DefaultListModel<String> dbModel = new DefaultListModel<String>();
		JList<String> programList = new JList<String>(dbModel);
		
		if (!connectDatabase())
			return programList;

		try {
			Statement selectStmt = dbConnection.createStatement();
			ResultSet results = selectStmt.executeQuery(
					"SELECT ProgramName FROM Programs ORDER BY ProgramName;");

			while (results.next()) {
				dbModel.addElement(new String(results.getString("ProgramName")));
			}
			results.close();
			selectStmt.close();

		} catch (SQLException e) {
			System.out.println("Failure retrieving Program List: " + e.getMessage());
		}
		return programList;
	}
	
	private static boolean[] createDaysOfWeekArray(int dow) {
		boolean[] dowBool = { false, false, false, false, false, false, false };
		for (int i = 6; i >= 0; i--) {
			if ((dow & 1) == 1)
				dowBool[i] = true;
			dow >>= 1;
		}
		return dowBool;
	}

	private static boolean[] createDowInMonthArray(int wom) {
		boolean[] womBool = { false, false, false, false, false };
		for (int i = 4; i >= 0; i--) {
			if ((wom & 1) == 1)
				womBool[i] = true;
			wom >>= 1;
		}
		return womBool;
	}

	private static int getDowAsInt (boolean[] dowArray) {
		int dow = 0;
		for (int k = 0; k < 7; k++)
			dow = (dow << 1) | (dowArray[k] ? 1 : 0);
		return dow;
	}
	
	private static int getWomAsInt (boolean[] womArray) {
		int wom = 0;
		for (int k = 0; k < 5; k++)
			wom = (wom << 1) | (womArray[k] ? 1 : 0);
		return wom;
	}
	
	private static String getTaskName(int taskId) {
		try {
			PreparedStatement selectStmt = dbConnection
					.prepareStatement("SELECT TaskName FROM Tasks WHERE TaskID=?;");
			selectStmt.setInt(1, taskId);
			ResultSet result = selectStmt.executeQuery();
			result.next();

			String taskName = result.getString("TaskName");
			selectStmt.close();
			result.close();
			return taskName;

		} catch (SQLException e) {
			System.out.println("Failure obtaining Task Name from database: " + e.getMessage());
			return "";
		}
	}

	/*
	 * ------- Programs Database addition/updates -------
	 */
	public static int addProgram(String programName, String startDate, String endDate) {
		int progID = 0;
		if (!connectDatabase())
			return -1;

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
			System.out.println("Failure adding program to databae: " + e.getMessage());
		}
		return progID;
	}
	
	public static void updateProgramName(int programID, String progName) {
		if (!connectDatabase())
			return;

		try {
			// Update program name
			PreparedStatement updateProgramStmt = dbConnection.prepareStatement(
					"UPDATE Programs SET ProgramName=? WHERE ProgramID=?;");
			updateProgramStmt.setString(1, progName);
			updateProgramStmt.setInt(2, programID);
			
			updateProgramStmt.executeUpdate();
			updateProgramStmt.close();

		} catch (SQLException e) {
			System.out.println("Failure updating Program Name in database: " + e.getMessage());
		}
	}
	
	public static void updateProgramDates(int programID, String startDate, String endDate) {
		if (!connectDatabase())
			return;

		try {
			// Update program name
			PreparedStatement updateProgramStmt = dbConnection.prepareStatement(
					"UPDATE Programs SET StartDate=?, EndDate=? WHERE ProgramID=?;");
			updateProgramStmt.setString(1, startDate);
			updateProgramStmt.setString(2,  endDate);
			updateProgramStmt.setInt(3, programID);
			
			updateProgramStmt.executeUpdate();
			updateProgramStmt.close();

		} catch (SQLException e) {
			System.out.println("Failure updating Program Dates in database: " + e.getMessage());
		}
	}

	/*
	 * ------- Task Database additions/updates -------
	 */
	public static int addTask(int progID, String taskName, String location, int numLeadersReqd, int totalPersonsReqd,
			boolean[] dayOfWeek, boolean[] weekOfMonth, TimeModel time, int color) {
		int taskID = 0;
		if (!connectDatabase())
			return -1;
		
		try {
			PreparedStatement addTaskStmt = dbConnection.prepareStatement(
					"INSERT INTO Tasks (ProgramID, TaskName, Hour, Minute, Location, NumLeadersReqd, TotalPersonsReqd, "
							+ "DaysOfWeek, DowInMonth, Color) VALUES " + "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
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

		} catch (SQLException e) {
			System.out.println("Failure adding task to database: " + e.getMessage());
		}
		return taskID;
	}
	
	public static void updateTaskName(int taskID, String taskName) {
		if (!connectDatabase())
			return;
		
		try {
			PreparedStatement updateTaskStmt = dbConnection.prepareStatement(
					"UPDATE Tasks SET TaskName=? WHERE TaskID=?;");

			int col = 1;
			updateTaskStmt.setString(col++, taskName);
			updateTaskStmt.setInt(col++, taskID);

			updateTaskStmt.executeUpdate();
			updateTaskStmt.close();

		} catch (SQLException e) {
			System.out.println("Failure updating task name in database: " + e.getMessage());
		}
	}
	
	public static void updateTaskFields(int taskID, String taskName, String location, int numLeadersReqd, int totalPersonsReqd,
			boolean[] dayOfWeek, boolean[] weekOfMonth, TimeModel time, int color) {
		if (!connectDatabase())
			return;
		
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

		} catch (SQLException e) {
			System.out.println("Failure updating task to database: " + e.getMessage());
		}
	}

	/*
	 * ------- Person Database additions/updates -------
	 */
	public static int addPerson(String personName, String phone, String email, boolean leader, String notes) {
		int personID = 0;
		if (!connectDatabase())
			return -1;
		
		try {
			PreparedStatement addPersonStmt = dbConnection.prepareStatement(
					"INSERT INTO Persons (PersonName, PhoneNumber, EMail, isLeader, Notes) " + " VALUES (?, ?, ?, ?, ?);",
					Statement.RETURN_GENERATED_KEYS);

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

		} catch (SQLException e) {
			System.out.println("Failure adding Person to database: " + e.getMessage());
		}
		return personID;
	}

	public static void updatePerson(int personID, String personName, String phone, String email, boolean leader, String notes) {
		if (!connectDatabase())
			return;
		
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

		} catch (SQLException e) {
			System.out.println("Failure updating Person in database: " + e.getMessage());
		}
	}

	public static int addAssignedTask(int personID, int taskID, boolean[] daysOfWeek, boolean[] weeksOfMonth) {
		int assignedTaskID = 0;
		int dow = 0, wom = 0;
		if (!connectDatabase())
			return -1;

		try {
			PreparedStatement addAssignedTaskStmt = dbConnection.prepareStatement(
					"INSERT INTO AssignedTasks (PersonID, taskID, DaysOfWeek, DowInMonth) VALUES (?, ?, ?, ?)",
					Statement.RETURN_GENERATED_KEYS);

			for (int k = 0; k < 7; k++)
				dow = (dow << 1) | (daysOfWeek[k] ? 1 : 0);
			for (int k = 0; k < 5; k++)
				wom = (wom << 1) | (weeksOfMonth[k] ? 1 : 0);

			// Add new assigned task
			int col = 1;
			addAssignedTaskStmt.setInt(col++, personID);
			addAssignedTaskStmt.setInt(col++, taskID);
			addAssignedTaskStmt.setInt(col++, dow);
			addAssignedTaskStmt.setInt(col++, wom);

			addAssignedTaskStmt.executeUpdate();
			ResultSet result = addAssignedTaskStmt.getGeneratedKeys();
			result.next();
			assignedTaskID = result.getInt(1);

			result.close();
			addAssignedTaskStmt.close();

		} catch (SQLException e) {
			System.out.println("Failure adding task assignment: " + e.getMessage());
		}
		return assignedTaskID;
	}

	public static int addUnavailDates(int personID, String startDate, String endDate) {
		int unavailDatesID = 0;
		if (!connectDatabase())
			return -1;
		
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

		} catch (SQLException e) {
			System.out.println("Failure adding unavailable dates: " + e.getMessage());
		}
		return unavailDatesID;
	}

	public static int addSingleInstanceTask(int personID, int taskID, Calendar taskTime) {
		int singleInstanceID = 0;
		if (!connectDatabase())
			return -1;
		
		int col = 1;
		PreparedStatement addSingleTaskStmt = null;
		try {
			if (taskID == 0) {
				addSingleTaskStmt = dbConnection.prepareStatement(
						"INSERT INTO SingleInstanceTasks (PersonID, Date, Time) VALUES (?, ?, ?)",
						Statement.RETURN_GENERATED_KEYS);
			} else {
				addSingleTaskStmt = dbConnection.prepareStatement(
						"INSERT INTO SingleInstanceTasks (PersonID, TaskId, Date, Time) VALUES (?, ?, ?, ?)",
						Statement.RETURN_GENERATED_KEYS);
			}

			// Add new Single Instance Task
			addSingleTaskStmt.setInt(col++, personID);
			if (taskID != 0)
				addSingleTaskStmt.setInt(col++, taskID);
			// addSingleTaskStmt.setTimestamp(3, java.sql.Timestamp.valueOf(taskTime.toString()));
			addSingleTaskStmt.setDate(col++, java.sql.Date.valueOf(Utilities.getSqlDate(taskTime)));
			addSingleTaskStmt.setTime(col++, java.sql.Time.valueOf(Utilities.getSqlTime(taskTime)));

			addSingleTaskStmt.executeUpdate();
			ResultSet result = addSingleTaskStmt.getGeneratedKeys();
			result.next();
			singleInstanceID = result.getInt(1);

			result.close();
			addSingleTaskStmt.close();

		} catch (SQLException e) {
			System.out.println("Failure adding single instance task: " + e.getMessage());
		}
		return singleInstanceID;
	}
}
