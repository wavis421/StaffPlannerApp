package model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JOptionPane;

import com.mysql.jdbc.exceptions.jdbc4.CommunicationsException;

import utilities.Utilities;

public class MySqlDatabase {
	private static Connection dbConnection;

	// Locally used constants
	private static final int PERSON_NOT_AVAIL = -2;
	private static final int NO_MATCH_FOUND = -1;
	private static final int ASSIGNED_TASK_MATCH = 0;
	private static final int SINGLE_INSTANCE_TASK_MATCH = 1;

	private SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy");

	public MySqlDatabase() {
		// Make initial connection to database
		connectDatabase();
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
		if (dbConnection != null) {
			try {
				dbConnection.close();
				dbConnection = null;
			} catch (SQLException e) {
				System.out.println("Failure closing database connection: " + e.getMessage());
			}
		}
	}

	private static boolean checkDatabaseConnection() {
		// If database connection has been lost, try re-connecting
		if (dbConnection == null)
			connectDatabase();

		return (dbConnection == null ? false : true);
	}

	/*
	 * ------- Programs -------
	 */
	public void addProgram(String programName, String startDate, String endDate) {
		if (!checkDatabaseConnection())
			return;

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement addProgramStmt = dbConnection
						.prepareStatement("INSERT INTO Programs (ProgramName, StartDate, EndDate) VALUES (?, ?, ?);");

				// Add new program
				int col = 1;
				addProgramStmt.setString(col++, programName);
				addProgramStmt.setDate(col++, java.sql.Date.valueOf(Utilities.getSqlDate(startDate)));
				addProgramStmt.setDate(col++, java.sql.Date.valueOf(Utilities.getSqlDate(endDate)));

				addProgramStmt.executeUpdate();
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
	}

	public void updateProgram(String programName, String startDate, String endDate) {
		if (!checkDatabaseConnection())
			return;

		for (int i = 0; i < 2; i++) {
			try {
				// Update program name
				PreparedStatement updateProgramStmt = dbConnection
						.prepareStatement("UPDATE Programs SET StartDate=?, EndDate=? WHERE ProgramName=?;");
				updateProgramStmt.setDate(1, java.sql.Date.valueOf(Utilities.getSqlDate(startDate)));
				updateProgramStmt.setDate(2, java.sql.Date.valueOf(Utilities.getSqlDate(endDate)));
				updateProgramStmt.setString(3, programName);

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

	public void renameProgram(String oldName, String newName) {
		if (!checkDatabaseConnection())
			return;

		for (int i = 0; i < 2; i++) {
			try {
				// Update program name
				PreparedStatement updateProgramStmt = dbConnection
						.prepareStatement("UPDATE Programs SET ProgramName=? WHERE ProgramName=?;");
				updateProgramStmt.setString(1, newName);
				updateProgramStmt.setString(2, oldName);

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
				System.out.println("Failure renaming " + oldName + " program in database: " + e.getMessage());
				break;
			}
		}
	}

	public ProgramModel getProgramByName(String programName) {
		if (!checkDatabaseConnection())
			return null;

		ProgramModel program = null;
		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement selectStmt = dbConnection.prepareStatement(
						"SELECT ProgramID, StartDate, EndDate " + "FROM Programs WHERE ProgramName=?;");
				selectStmt.setString(1, programName);

				ResultSet result = selectStmt.executeQuery();
				if (result.next()) {
					program = new ProgramModel(result.getInt("ProgramID"), programName,
							Utilities.convertSqlDateToString(result.getDate("StartDate")),
							Utilities.convertSqlDateToString(result.getDate("EndDate")), null);
				}
				result.close();
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
				System.out.println("Failure retreiving " + programName + " program from database: " + e.getMessage());
				break;
			}
		}
		return program;
	}

	public JList<String> getAllProgramsAsString() {
		DefaultListModel<String> nameModel = new DefaultListModel<String>();
		if (!checkDatabaseConnection())
			return (new JList<String>(nameModel));

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement selectStmt = dbConnection
						.prepareStatement("SELECT ProgramName FROM Programs ORDER BY ProgramName;");
				ResultSet result = selectStmt.executeQuery();
				while (result.next()) {
					nameModel.addElement(result.getString("ProgramName"));
				}

				result.close();
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
				System.out.println("Failure retrieving Programs from database: " + e.getMessage());
				break;
			}
		}
		return (new JList<String>(nameModel));
	}

	public LinkedList<ProgramModel> getAllPrograms() {
		// TODO: Used to create JTree
		return null;
	}

	public int getNumPrograms() {
		if (!checkDatabaseConnection())
			return 0;

		int count = 0;
		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement selectStmt = dbConnection.prepareStatement("SELECT COUNT(*) AS Count FROM Programs;");
				ResultSet result = selectStmt.executeQuery();
				result.next();
				count = result.getInt("Count");

				result.close();
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
				System.out.println("Failure accessing Program database: " + e.getMessage());
				break;
			}
		}
		return count;
	}

	private int getProgramIndexByName(String programName) {
		// TODO: Shouldn't need this anymore
		return -1;
	}

	/*
	 * ------- Task data -------
	 */
	public void addTask(String programName, String taskName, String location, int numLeadersReqd, int totalPersonsReqd,
				boolean[] dayOfWeek, boolean[] weekOfMonth, TimeModel time, int color) {
		if (!checkDatabaseConnection())
			return;

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement addTaskStmt = dbConnection.prepareStatement(
						"INSERT INTO Tasks (ProgramID, TaskName, Hour, Minute, Location, NumLeadersReqd, TotalPersonsReqd, "
								+ "DaysOfWeek, DowInMonth, Color) "
								+ "VALUES ((SELECT ProgramID FROM Programs WHERE Programs.ProgramName = programName), "
								+ "?, ?, ?, ?, ?, ?, ?, ?, ?) ");

				int col = 1;
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
				System.out.println("Failure adding " + taskName + " task to database: " + e.getMessage());
				break;
			}
		}
	}

	public void updateTask(String programName, String taskName, String location, int numLeadersReqd,
			int totalPersonsReqd, boolean[] dayOfWeek, boolean[] weekOfMonth, TimeModel time, int color) {
		// TODO: Update later to pass in TaskID
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
				updateTaskStmt.setInt(col++, 0 /* taskID */);

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
				System.out.println("Failure updating " + taskName + " task in database: " + e.getMessage());
				break;
			}
		}
	}

	public void renameTask(String programName, String oldName, String newName) {
		if (!checkDatabaseConnection())
			return;

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement updateTaskStmt = dbConnection
						.prepareStatement("UPDATE Tasks SET TaskName=? WHERE TaskName=?;");

				int col = 1;
				updateTaskStmt.setString(col++, newName);
				updateTaskStmt.setString(col++, oldName);

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
				System.out.println("Failure updating " + oldName + " task in database: " + e.getMessage());
				break;
			}
		}
	}

	public TaskModel getTaskByName(String programName, String taskName) {
		if (!checkDatabaseConnection())
			return null;

		TaskModel task = null;
		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement selectStmt = dbConnection
						.prepareStatement("SELECT * FROM Tasks, Programs WHERE ProgramName=? AND TaskName=?;");
				selectStmt.setString(1, programName);
				selectStmt.setString(2, taskName);

				ResultSet result = selectStmt.executeQuery();
				if (result.next()) {
					task = new TaskModel(result.getInt("TaskID"), result.getInt("ProgramID"), taskName,
							result.getString("Location"), result.getInt("NumLeadersReqd"),
							result.getInt("TotalPersonsReqd"), createDaysOfWeekArray(result.getInt("DaysOfWeek")),
							createDowInMonthArray(result.getInt("DowInMonth")),
							new TimeModel(result.getInt("Hour"), result.getInt("Minute")), result.getInt("Color"));
				}
				result.close();
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
				System.out.println("Failure retreiving " + taskName + " task from database: " + e.getMessage());
				break;
			}
		}
		return task;
	}

	public TaskModel getTaskByID(String programName, int taskID) {
		if (!checkDatabaseConnection())
			return null;

		// TODO: Remove programName parameter
		TaskModel task = null;
		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement selectStmt = dbConnection.prepareStatement("SELECT * FROM Tasks WHERE TaskID=?;");
				selectStmt.setInt(1, taskID);

				ResultSet result = selectStmt.executeQuery();
				if (result.next()) {
					task = new TaskModel(result.getInt("TaskID"), result.getInt("ProgramID"),
							result.getString("TaskName"), result.getString("Location"), result.getInt("NumLeadersReqd"),
							result.getInt("TotalPersonsReqd"), createDaysOfWeekArray(result.getInt("DaysOfWeek")),
							createDowInMonthArray(result.getInt("DowInMonth")),
							new TimeModel(result.getInt("Hour"), result.getInt("Minute")), result.getInt("Color"));
				}
				result.close();
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
				System.out.println("Failure retreiving taskID=" + taskID + " from database: " + e.getMessage());
				break;
			}
		}
		return task;
	}

	public String findProgramByTaskName(String taskName) {
		if (!checkDatabaseConnection())
			return null;

		String progName = null;
		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement selectStmt = dbConnection
						.prepareStatement("SELECT ProgramName FROM Programs, Tasks WHERE TaskName=?;");
				selectStmt.setString(1, taskName);

				ResultSet result = selectStmt.executeQuery();
				if (result.next()) {
					progName = result.getString("ProgramName");
				}
				result.close();
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
				System.out.println("Failure retreiving " + taskName + " task from database: " + e.getMessage());
				break;
			}
		}
		return progName;
	}

	public LinkedList<CalendarDayModel> getTasksByDayByProgram(Calendar calendar, JList<String> programFilterList) {
		LinkedList<CalendarDayModel> thisDaysTasks = getAllTasksByDay(calendar);

		// TODO: Possibly create a new procedure?
		for (int taskIdx = 0; taskIdx < thisDaysTasks.size(); taskIdx++) {
			String programName = findProgramByTaskName(thisDaysTasks.get(taskIdx).getTask().getTaskName());
			if (!Utilities.findStringMatchInJList(programName, programFilterList)) {
				thisDaysTasks.remove(taskIdx);
				taskIdx--;
			}
		}
		return thisDaysTasks;
	}

	public LinkedList<CalendarDayModel> getTasksByDayByPerson(Calendar calendar, JList<String> persons) {
		int dayOfWeekInMonthIdx = calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH) - 1;
		int dayOfWeekIdx = calendar.get(Calendar.DAY_OF_WEEK) - 1;
		Date thisDay = Utilities.getDateFromCalendar(calendar);
		LinkedList<CalendarDayModel> thisDaysTasks = getAllTasksByDay(calendar);
		boolean match;

		// TODO: Possibly create a new procedure?
		for (int taskIdx = 0; taskIdx < thisDaysTasks.size(); taskIdx++) {
			match = false;
			int thisDaysTaskID = thisDaysTasks.get(taskIdx).getTask().getTaskID();

			for (int i = 0; i < persons.getModel().getSize(); i++) {
				PersonModel pModel = getPersonByName(persons.getModel().getElementAt(i));
				// -1 = no match, 0 = assigned task, 1 = single instance task
				if (checkPersonMatchForTaskByDay(pModel, thisDaysTaskID, thisDay, dayOfWeekIdx,
						dayOfWeekInMonthIdx) >= 0) {
					match = true;
					break;
				}
			}
			if (!match) {
				thisDaysTasks.remove(taskIdx);
				taskIdx--;
			}
		}
		return thisDaysTasks;
	}

	public LinkedList<CalendarDayModel> getTasksByDayByIncompleteRoster(Calendar calendar) {
		LinkedList<CalendarDayModel> thisDaysTasks = getAllTasksByDay(calendar);

		// TODO: Possibly create a new procedure?
		for (int taskIdx = 0; taskIdx < thisDaysTasks.size(); taskIdx++) {
			if ((thisDaysTasks.get(taskIdx).getPersonCount() >= thisDaysTasks.get(taskIdx).getTask()
					.getTotalPersonsReqd())
					&& (thisDaysTasks.get(taskIdx).getLeaderCount() >= thisDaysTasks.get(taskIdx).getTask()
							.getNumLeadersReqd())) {
				thisDaysTasks.remove(taskIdx);
				taskIdx--;
			}
		}
		return thisDaysTasks;
	}

	public LinkedList<CalendarDayModel> getTasksByDayByLocation(Calendar calendar, JList<String> locations) {
		LinkedList<CalendarDayModel> matchingTasks = getAllTasksByDay(calendar);

		// TODO: Possibly create a new procedure?
		for (int taskIdx = 0; taskIdx < matchingTasks.size(); taskIdx++) {
			String taskLoc = matchingTasks.get(taskIdx).getTask().getLocation();
			if (!Utilities.findStringMatchInJList(taskLoc, locations)) {
				matchingTasks.remove(taskIdx);
				taskIdx--;
			}
		}
		return matchingTasks;
	}

	public LinkedList<CalendarDayModel> getTasksByDayByTime(Calendar calendar, JList<String> timeList) {
		LinkedList<CalendarDayModel> matchingTasks = getAllTasksByDay(calendar);
		Collections.sort(matchingTasks);

		// TODO: Possibly create a new procedure?
		for (int taskIdx = 0; taskIdx < matchingTasks.size(); taskIdx++) {
			String taskTime = matchingTasks.get(taskIdx).getTask().getTime().toString();
			if (!Utilities.findStringMatchInJList(taskTime, timeList)) {
				matchingTasks.remove(taskIdx);
				taskIdx--;
			}
		}
		return matchingTasks;
	}

	public LinkedList<CalendarDayModel> getAllTasksByDay(Calendar calendar) {
		int dayOfWeekInMonthIdx = calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH) - 1;
		int dayOfWeekIdx = calendar.get(Calendar.DAY_OF_WEEK) - 1;
		Date thisDay = Utilities.getDateFromCalendar(calendar);

		// TODO: Possibly create a new procedure?
		LinkedList<CalendarDayModel> thisDaysTasks = new LinkedList<CalendarDayModel>();
		for (int i = 0; i < programList.size(); i++) {
			ProgramModel prog = programList.get(i);
			if (isProgramExpired(thisDay, prog))
				continue;

			for (int j = 0; j < prog.getTaskList().size(); j++) {
				TaskModel task = prog.getTaskList().get(j);
				if ((task.getDayOfWeek()[dayOfWeekIdx]) && (task.getWeekOfMonth()[dayOfWeekInMonthIdx])) {
					int count = getPersonCountForTaskByDay(task, thisDay, dayOfWeekIdx, dayOfWeekInMonthIdx);
					thisDaysTasks.add(new CalendarDayModel(task, count & 0xFFFF, (count >> 16) & 0xFFFF,
							task.getColor(), null, null));
				}
			}
		}
		return (LinkedList<CalendarDayModel>) thisDaysTasks;
	}

	public LinkedList<CalendarDayModel> getAllTasksAndFloatersByDay(Calendar calendar) {
		int dayOfWeekInMonthIdx = calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH) - 1;
		int dayOfWeekIdx = calendar.get(Calendar.DAY_OF_WEEK) - 1;
		Date thisDay = Utilities.getDateFromCalendar(calendar);

		// TODO: Replaced with getAllTasksAndFloatersByMonth (?)

		// Get all tasks for today
		LinkedList<CalendarDayModel> thisDaysTasks = getAllTasksByDay(calendar);

		// Now add floaters to the list
		for (int i = 0; i < personList.size(); i++) {
			PersonModel person = personList.get(i);

			// Check if person is a floater (not associated with task).
			for (int j = 0; j < person.getSingleInstanceTasks().size(); j++) {
				SingleInstanceTaskModel task = person.getSingleInstanceTasks().get(j);
				if (checkFloaterMatch(task, thisDay, dayOfWeekIdx, dayOfWeekInMonthIdx) >= 0) {
					thisDaysTasks.add(new CalendarDayModel(null, 0, 0, task.getColor(), task.getTaskDate(), "Floater"));
				}
			}
		}

		// Merge duplicate floaters
		for (int i = 0; i < thisDaysTasks.size(); i++) {
			CalendarDayModel calDay = thisDaysTasks.get(i);
			if (calDay.getTask() == null) { // Found floater
				Calendar taskTime = calDay.getFloaterTime();
				int floaterCount = 0;
				int firstFloaterIndex = 0;

				// Find floaters with matching time
				for (int taskIdx = 0; taskIdx < thisDaysTasks.size(); taskIdx++) {
					if (thisDaysTasks.get(taskIdx).getFloaterTime() == null) {
						// Not a floater
						continue;
					}

					if (Utilities.checkForTimeMatch(taskTime, thisDaysTasks.get(taskIdx).getFloaterTime())) {
						if (floaterCount == 0) {
							// First match, keep in list
							firstFloaterIndex = taskIdx;
						} else {
							// Multiple matches, remove from list
							thisDaysTasks.remove(taskIdx);
							taskIdx--;
						}
						floaterCount++;
					}
				}

				// Update floater name if more than 1 match
				if (floaterCount > 1)
					thisDaysTasks.get(firstFloaterIndex).setFloaterTaskName(floaterCount + " Floaters");
			}
		}
		Collections.sort(thisDaysTasks);
		return thisDaysTasks;
	}

	public LinkedList<CalendarDayModel> getAllTasksAndFloatersByMonth(Calendar calendar) {
		LinkedList<CalendarDayModel> calendarList = new LinkedList<CalendarDayModel>();
		if (!checkDatabaseConnection())
			return calendarList;

		String taskName;
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		String date = Utilities.getSqlDate(calendar);
		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement updateMonthStmt = dbConnection
						.prepareStatement("CALL MonthlyCalendar('" + date + "');");
				ResultSet results = updateMonthStmt.executeQuery();

				while (results.next()) {
					// TODO: Return linked list of calendar days
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

	public JList<TaskModel> getAllTasksByProgram(String programName) {
		DefaultListModel<TaskModel> taskModel = new DefaultListModel<TaskModel>();

		if (!checkDatabaseConnection())
			return new JList<TaskModel>(taskModel);

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement selectStmt = dbConnection.prepareStatement(
						"SELECT * FROM Tasks, Programs WHERE ProgramName=? ORDER BY Hour, Minute, TaskName;");
				selectStmt.setString(1, programName);

				ResultSet result = selectStmt.executeQuery();
				while (result.next()) {
					taskModel.addElement(new TaskModel(result.getInt("TaskID"), result.getInt("ProgramID"),
							result.getString("TaskName"), result.getString("Location"), result.getInt("NumLeadersReqd"),
							result.getInt("TotalPersonsReqd"), createDaysOfWeekArray(result.getInt("DaysOfWeek")),
							createDowInMonthArray(result.getInt("DowInMonth")),
							new TimeModel(result.getInt("Hour"), result.getInt("Minute")), result.getInt("Color")));
				}
				result.close();
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
				System.out.println("Failure retreiving task list from database: " + e.getMessage());
				break;
			}
		}
		return new JList<TaskModel>(taskModel);
	}

	public JList<TaskModel> getAllTasks() {
		DefaultListModel<TaskModel> taskModel = new DefaultListModel<TaskModel>();

		if (!checkDatabaseConnection())
			return new JList<TaskModel>(taskModel);

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement selectStmt = dbConnection
						.prepareStatement("SELECT * FROM Tasks ORDER BY Hour, Minute, TaskName;");

				ResultSet result = selectStmt.executeQuery();
				while (result.next()) {
					taskModel.addElement(new TaskModel(result.getInt("TaskID"), result.getInt("ProgramID"),
							result.getString("TaskName"), result.getString("Location"), result.getInt("NumLeadersReqd"),
							result.getInt("TotalPersonsReqd"), createDaysOfWeekArray(result.getInt("DaysOfWeek")),
							createDowInMonthArray(result.getInt("DowInMonth")),
							new TimeModel(result.getInt("Hour"), result.getInt("Minute")), result.getInt("Color")));
				}
				result.close();
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
				System.out.println("Failure retreiving task list from database: " + e.getMessage());
				break;
			}
		}
		return new JList<TaskModel>(taskModel);
	}

	public JList<String> getAllLocationsAsString() {
		DefaultListModel<String> locationModel = new DefaultListModel<String>();

		if (!checkDatabaseConnection())
			return new JList<String>(locationModel);

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement selectStmt = dbConnection.prepareStatement("SELECT DISTINCT(Location) AS Location "
						+ "FROM Tasks WHERE Location NOT NULL ORDER BY Location;");

				ResultSet result = selectStmt.executeQuery();
				while (result.next()) {
					locationModel.addElement(result.getString("Location"));
				}
				result.close();
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
				System.out.println("Failure retreiving task locations from database: " + e.getMessage());
				break;
			}
		}
		return new JList<String>(locationModel);
	}

	public JList<String> getAllTimesAsString() {
		DefaultListModel<String> timeModel = new DefaultListModel<String>();

		if (!checkDatabaseConnection())
			return new JList<String>(timeModel);

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement selectStmt = dbConnection.prepareStatement("SELECT Hour, Minute "
						+ "FROM Tasks WHERE Hour NOT NULL GROUP BY Hour, Minute ORDER BY Hour, Minute;");

				ResultSet result = selectStmt.executeQuery();
				while (result.next()) {
					timeModel.addElement(new TimeModel(result.getInt("Hour"), result.getInt("Minute")).toString());
				}
				result.close();
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
				System.out.println("Failure retreiving task times from database: " + e.getMessage());
				break;
			}
		}
		return new JList<String>(timeModel);
	}

	public JList<TimeModel> getAllTimesByDay(Calendar calendar) {
		DefaultListModel<TimeModel> timeModel = new DefaultListModel<TimeModel>();
		ArrayList<TimeModel> timeArray = new ArrayList<TimeModel>();

		// TODO:
		LinkedList<CalendarDayModel> taskList = getAllTasksByDay(calendar);
		for (int taskIdx = 0; taskIdx < taskList.size(); taskIdx++) {
			// Check whether already in list before adding
			TimeModel taskTime = taskList.get(taskIdx).getTask().getTime();
			if (!findTimeMatchInArray(taskTime, timeArray))
				timeArray.add(taskTime);
		}

		Collections.sort((ArrayList<TimeModel>) timeArray);
		for (int i = 0; i < timeArray.size(); i++)
			timeModel.addElement(timeArray.get(i));
		JList<TimeModel> timeList = new JList<TimeModel>(timeModel);
		return timeList;
	}

	private int checkPersonMatchForTask(PersonModel person, int taskID) {
		LinkedList<AssignedTasksModel> assignedTaskList = person.getAssignedTasks();

		// TODO: Probably not needed after conversion completed
		// Check if task is in person's assigned task list
		for (int i = 0; i < assignedTaskList.size(); i++) {
			AssignedTasksModel assignedTask = assignedTaskList.get(i);
			if (assignedTask.getTaskID() == taskID) {
				return ASSIGNED_TASK_MATCH;
			}
		}
		return NO_MATCH_FOUND;
	}

	private int checkPersonMatchForTaskByDay(PersonModel person, int taskID, Date today, int dayOfWeekIdx,
			int dowInMonthIdx) {
		// TODO: Probably not needed after conversion
		if (!isPersonAvailable(person, today))
			return PERSON_NOT_AVAIL;

		else {
			LinkedList<AssignedTasksModel> assignedTaskList = person.getAssignedTasks();

			// Check if task is in person's assigned task list for today
			for (int i = 0; i < assignedTaskList.size(); i++) {
				AssignedTasksModel assignedTask = assignedTaskList.get(i);
				if ((assignedTask.getTaskID() == taskID) && assignedTask.getDaysOfWeek()[dayOfWeekIdx]
						&& assignedTask.getWeeksOfMonth()[dowInMonthIdx]) {
					return ASSIGNED_TASK_MATCH;
				}
			}

			for (int i = 0; i < person.getSingleInstanceTasks().size(); i++) {
				SingleInstanceTaskModel singleInstanceTask = person.getSingleInstanceTasks().get(i);
				// Check if this person is a sub for today
				Calendar subCalendar = singleInstanceTask.getTaskDate();
				if ((singleInstanceTask.getTaskID() == taskID)
						&& Utilities.checkForDateAndTimeMatch(today, dayOfWeekIdx, dowInMonthIdx, subCalendar)) {
					return SINGLE_INSTANCE_TASK_MATCH;
				}
			}
		}
		return NO_MATCH_FOUND;
	}

	private int checkFloaterMatch(SingleInstanceTaskModel singleInstanceTask, Date today, int dayOfWeekIdx,
			int dowInMonthIdx) {
		// TODO: Probably not needed after conversion
		// Check if this person is a sub for today
		Calendar subCalendar = singleInstanceTask.getTaskDate();

		if ((singleInstanceTask.getTaskID() == 0)
				&& Utilities.checkForDateAndTimeMatch(today, dayOfWeekIdx, dowInMonthIdx, subCalendar))
			return SINGLE_INSTANCE_TASK_MATCH;
		else
			return NO_MATCH_FOUND;
	}

	private int getPersonCountForTaskByDay(TaskModel task, Date today, int dayOfWeekIdx, int dowInMonthIdx) {
		JList<PersonModel> persons = getAllPersons();
		short personCount = 0;
		short leaderCount = 0;

		// TODO: Probably not needed after conversion
		for (int idx = 0; idx < persons.getModel().getSize(); idx++) {
			PersonModel person = persons.getModel().getElementAt(idx);
			// -1 = no match, 0 = assigned task, 1 = single instance task
			if (checkPersonMatchForTaskByDay(person, task.getTaskID(), today, dayOfWeekIdx, dowInMonthIdx) >= 0) {
				personCount++;
				if (person.isLeader())
					leaderCount++;
			}
		}
		return (personCount | (leaderCount << 16));
	}

	private boolean isProgramExpired(Date today, ProgramModel prog) {
		if (today == null)
			return false; // impossible?

		// TODO: Probably not needed after conversion
		if ((prog.getStartDate() != null) && !prog.getStartDate().equals("")) {
			try {
				Date progDate = dateFormatter.parse(prog.getStartDate());
				if (today.compareTo(progDate) < 0)
					// Program expired
					return true;

			} catch (ParseException e) {
				JOptionPane.showMessageDialog(null,
						"Unable to parse start-date for program '" + prog.getProgramName() + "'",
						"Error retrieving program", JOptionPane.ERROR_MESSAGE);
				prog.setStartDate(null);
			}
		}

		if ((prog.getEndDate() != null) && !prog.getEndDate().equals("")) {
			try {
				Date progDate = dateFormatter.parse(prog.getEndDate());
				if (today.compareTo(progDate) > 0)
					// Program expired
					return true;

			} catch (ParseException e) {
				JOptionPane.showMessageDialog(null,
						"Unable to parse end-date for program '" + prog.getProgramName() + "'",
						"Error retrieving program", JOptionPane.ERROR_MESSAGE);
				prog.setEndDate(null);
			}
		}
		return false;
	}

	private boolean isPersonAvailable(PersonModel person, Date today) {
		// TODO: Probably not needed after conversion
		for (int i = 0; i < person.getDatesUnavailable().size(); i++) {
			DateRangeModel datesUnavail = person.getDatesUnavailable().get(i);
			if (Utilities.isDateWithinDateRange(today, datesUnavail.getStartDate(), datesUnavail.getEndDate(),
					"Unable to parse " + person.getName() + "'s Unavailable start/end Dates."))
				return false;
		}
		return true;
	}

	/*
	 * ------- Person data -------
	 */
	public void addPerson(String name, String phone, String email, boolean leader, String notes,
			LinkedList<AssignedTasksModel> assignedTasks, LinkedList<SingleInstanceTaskModel> extraTasks,
			LinkedList<DateRangeModel> datesUnavailable) {

		int personID = addPersonInfo(name, phone, email, leader, notes);
		if (personID <= 0)
			return;

		for (int i = 0; i < assignedTasks.size(); i++) {
			AssignedTasksModel task = assignedTasks.get(i);
			addAssignedTask(personID, task.getTaskID(), task.getDaysOfWeek(), task.getWeeksOfMonth());
		}

		for (int i = 0; i < extraTasks.size(); i++) {
			// TODO: Also add floater color
			SingleInstanceTaskModel task = extraTasks.get(i);
			addSingleInstanceTask(personID, task.getTaskID(), task.getTaskDate());
		}

		for (int i = 0; i < datesUnavailable.size(); i++) {
			DateRangeModel dates = datesUnavailable.get(i);
			addUnavailDates(personID, dates.getStartDate(), dates.getEndDate());
		}
	}

	private int addPersonInfo(String personName, String phone, String email, boolean leader, String notes) {
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
				if (result.next())
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
				System.out.println("Failure adding " + personName + " to database: " + e.getMessage());
				break;
			}
		}
		return personID;
	}

	private int updatePersonInfo(String personName, String phone, String email, boolean leader, String notes) {
		int personID = 0;
		if (!checkDatabaseConnection())
			return -1;

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement updatePersonStmt = dbConnection.prepareStatement(
						"UPDATE Persons SET PhoneNumber=?, EMail=?, isLeader=?, Notes=? " + "WHERE PersonName=?;",
						Statement.RETURN_GENERATED_KEYS);

				// Update person
				int col = 1;
				updatePersonStmt.setString(col++, phone);
				updatePersonStmt.setString(col++, email);
				updatePersonStmt.setBoolean(col++, leader);
				updatePersonStmt.setString(col, notes);
				updatePersonStmt.setString(col++, personName);

				updatePersonStmt.executeUpdate();
				ResultSet result = updatePersonStmt.getGeneratedKeys();
				if (result.next())
					personID = result.getInt(1);

				result.close();
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
				System.out.println("Failure updating " + personName + " info to database: " + e.getMessage());
				break;
			}
		}
		return personID;
	}

	public void addAssignedTask(int personID, int taskID, boolean[] daysOfWeek, boolean[] weeksOfMonth) {
		if (!checkDatabaseConnection())
			return;

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement addAssignedTaskStmt = dbConnection.prepareStatement(
						"INSERT INTO AssignedTasks (PersonID, taskID, DaysOfWeek, DowInMonth) VALUES (?, ?, ?, ?);");

				// Add new assigned task
				int col = 1;
				addAssignedTaskStmt.setInt(col++, personID);
				addAssignedTaskStmt.setInt(col++, taskID);
				addAssignedTaskStmt.setInt(col++, getDowAsInt(daysOfWeek));
				addAssignedTaskStmt.setInt(col++, getWomAsInt(weeksOfMonth));

				addAssignedTaskStmt.executeUpdate();
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
	}

	public void updateAssignedTask(int assignedTaskID, boolean[] daysOfWeek, boolean[] weeksOfMonth) {
		if (!checkDatabaseConnection())
			return;

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement updateAssignedTaskStmt = dbConnection.prepareStatement(
						"UPDATE AssignedTasks SET DaysOfWeek=?, DowInMonth=? WHERE AssignedTaskID=?;");

				// Update assigned task
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

	private void mergeAssignedTask(int personID, int taskID, boolean[] daysOfWeek, boolean[] weeksOfMonth) {
		if (!checkDatabaseConnection())
			return;

		String personName = null;
		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement prepStmt = dbConnection
						.prepareStatement("SELECT COUNT(*), AssignedTaskID, Persons.PersonID AS PersonID, PersonName "
								+ "FROM AssignedTasks, Persons "
								+ "WHERE TaskID=? AND Persons.PersonID=? AND AssignedTasks.PersonID = Persons.PersonID;");
				prepStmt.setInt(1, taskID);
				prepStmt.setInt(2, personID);
				ResultSet result = prepStmt.executeQuery();

				// TODO: Optimize this later by avoiding duplicating overhead
				if (result.getInt(1) > 0) {
					// Assigned task exists, so update fields
					updateAssignedTask(result.getInt("AssignedTaskID"), daysOfWeek, weeksOfMonth);
				} else {
					// Assigned task not already in list, so insert
					addAssignedTask(result.getInt("PersonID"), taskID, daysOfWeek, weeksOfMonth);
				}
				personName = result.getString("PersonName");

				result.close();
				prepStmt.close();
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
				if (personName == null)
					System.out.println("Failure adding tasks: " + e.getMessage());
				else
					System.out.println("Failure adding tasks for " + personName + ": " + e.getMessage());
				break;
			}
		}
	}

	public void addSingleInstanceTask(int personID, int taskID, Calendar taskTime) {
		if (!checkDatabaseConnection())
			return;

		for (int i = 0; i < 2; i++) {
			int col = 1;
			PreparedStatement addSingleTaskStmt = null;
			try {
				if (taskID == 0) {
					addSingleTaskStmt = dbConnection.prepareStatement(
							"INSERT INTO SingleInstanceTasks (PersonID, SingleDate, SingleTime) VALUES (?, ?, ?);");
				} else {
					addSingleTaskStmt = dbConnection.prepareStatement(
							"INSERT INTO SingleInstanceTasks (PersonID, TaskId, SingleDate, SingleTime) VALUES (?, ?, ?, ?);");
				}

				// Add new Single Instance Task
				addSingleTaskStmt.setInt(col++, personID);
				if (taskID != 0)
					addSingleTaskStmt.setInt(col++, taskID);
				addSingleTaskStmt.setDate(col++, java.sql.Date.valueOf(Utilities.getSqlDate(taskTime)));
				addSingleTaskStmt.setTime(col++, java.sql.Time.valueOf(Utilities.getSqlTime(taskTime)));

				addSingleTaskStmt.executeUpdate();
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
	}

	public void addSingleInstanceTask_orig(String personName, Calendar taskTime, TaskModel task, int color) {
		if (!checkDatabaseConnection())
			return;

		for (int i = 0; i < 2; i++) {
			int col = 1;
			PreparedStatement addSingleTaskStmt = null;

			try {
				// TODO: Add color later
				if (task == null) {
					addSingleTaskStmt = dbConnection
							.prepareStatement("INSERT INTO SingleInstanceTasks (PersonID, SingleDate, SingleTime) "
									+ "VALUES ((SELECT PersonID FROM Persons WHERE PersonName=?), ?, ?) ");
				} else {
					addSingleTaskStmt = dbConnection.prepareStatement(
							"INSERT INTO SingleInstanceTasks (PersonID, TaskId, SingleDate, SingleTime) "
									+ "VALUES ((SELECT PersonID FROM Persons WHERE PersonName=?), ?, ?, ?) ");
				}

				// Add new Single Instance Task
				addSingleTaskStmt.setString(col++, personName);
				if (task != null)
					addSingleTaskStmt.setInt(col++, task.getTaskID());
				addSingleTaskStmt.setDate(col++, java.sql.Date.valueOf(Utilities.getSqlDate(taskTime)));
				addSingleTaskStmt.setTime(col++, java.sql.Time.valueOf(Utilities.getSqlTime(taskTime)));

				addSingleTaskStmt.executeUpdate();
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
				if (task == null)
					System.out.println("Failure adding Floater task for " + personName + ": " + e.getMessage());
				else
					System.out.println("Failure adding " + task.getTaskName() + " as one-time task for " + personName
							+ ": " + e.getMessage());
				break;
			}
		}
	}

	private void addUnavailDates(int personID, String startDate, String endDate) {
		if (!checkDatabaseConnection())
			return;

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement addUnavailDatesStmt = dbConnection
						.prepareStatement("INSERT INTO UnavailDates (PersonID, StartDate, EndDate) VALUES (?, ?, ?);");

				// Add new Unavailable Dates
				addUnavailDatesStmt.setInt(1, personID);
				addUnavailDatesStmt.setString(2, startDate);
				addUnavailDatesStmt.setString(3, endDate);

				addUnavailDatesStmt.executeUpdate();
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
	}

	private void updateUnavailDates(int personID, String startDate, String endDate) {
		if (!checkDatabaseConnection())
			return;

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement prepStmt = dbConnection.prepareStatement("SELECT COUNT(*) FROM UnavailDates, Persons "
						+ "WHERE StartDate=? AND EndDate=? AND Persons.PersonID=? "
						+ "AND UnavailDates.PersonID = Persons.PersonID;");
				prepStmt.setString(1, startDate);
				prepStmt.setString(2, endDate);
				prepStmt.setInt(3, personID);
				ResultSet result = prepStmt.executeQuery();
				result.next();

				// TODO: Check whether this is a superset of another date range
				if (result.getInt(1) == 0) {
					// No match for start/end dates, so add date range
					addUnavailDates(personID, startDate, endDate);
				}
				result.close();
				prepStmt.close();
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
	}

	public void updatePerson(String personName, String personPhone, String personEmail, boolean personIsLeader,
			String personNotes, LinkedList<AssignedTasksModel> personAssignedTasks,
			LinkedList<SingleInstanceTaskModel> extraTasks, LinkedList<DateRangeModel> personDatesUnavailable) {

		// TODO: Check if we have to check for person NAME changes???

		// Update person info
		int personID = updatePersonInfo(personName, personPhone, personEmail, personIsLeader, personNotes);
		if (personID <= 0)
			return;

		// Merge in the assigned tasks (list ONLY contains changes!!)
		for (int i = 0; i < personAssignedTasks.size(); i++) {
			// Update Assigned Tasks database for this person
			AssignedTasksModel assignedTask = personAssignedTasks.get(i);
			mergeAssignedTask(personID, assignedTask.getTaskID(), assignedTask.getDaysOfWeek(),
					assignedTask.getWeeksOfMonth());
		}

		// Add extraTasks (list only contains additions!!)
		for (int i = 0; i < extraTasks.size(); i++) {
			// Add single instance task to database
			SingleInstanceTaskModel singleTask = extraTasks.get(i);
			addSingleInstanceTask(personID, singleTask.getTaskID(), singleTask.getTaskDate());
		}

		// Add dates unavailable (check for duplicates)
		for (int i = 0; i < personDatesUnavailable.size(); i++) {
			// Add unavailable dates if not a duplicate
			DateRangeModel date = personDatesUnavailable.get(i);
			updateUnavailDates(personID, date.getStartDate(), date.getEndDate());
		}
	}

	public void renamePerson(String oldName, String newName) {
		if (!checkDatabaseConnection())
			return;

		for (int i = 0; i < 2; i++) {
			try {
				// Update person name
				PreparedStatement updatePersonStmt = dbConnection
						.prepareStatement("UPDATE Persons SET PersonName=? WHERE PersonName=?;");
				updatePersonStmt.setString(1, newName);
				updatePersonStmt.setString(2, oldName);

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
				System.out.println("Failure renaming " + oldName + " in database: " + e.getMessage());
				break;
			}
		}
	}

	public void markPersonUnavail(String personName, Calendar today) {
		if (!checkDatabaseConnection())
			return;

		for (int i = 0; i < 2; i++) {
			try {
				String displayDate = Utilities.getDisplayDate(today);
				PreparedStatement prepStmt = dbConnection
						.prepareStatement("SELECT COUNT(*), Persons.PersonID AS PersonID FROM UnavailDates, Persons "
								+ "WHERE StartDate=? AND EndDate=? AND PersonName=? "
								+ "AND UnavailDates.PersonID = Persons.PersonID;");
				prepStmt.setString(1, displayDate);
				prepStmt.setString(2, displayDate);
				prepStmt.setString(3, personName);
				ResultSet result = prepStmt.executeQuery();
				result.next();

				// TODO: Check whether this is a superset of another date range
				if (result.getInt(1) == 0) {
					// No match for start/end dates, so add date range
					addUnavailDates(result.getInt(2), displayDate, displayDate);
				} else
					System.out.println("Date range for " + personName + " already exists.");

				result.close();
				prepStmt.close();
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
	}

	public JList<String> getAllPersonsAsString() {
		DefaultListModel<String> nameModel = new DefaultListModel<String>();

		if (!checkDatabaseConnection())
			return new JList<String>(nameModel);

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement selectStmt = dbConnection
						.prepareStatement("SELECT PersonName FROM Persons ORDER BY PersonName;");

				ResultSet result = selectStmt.executeQuery();
				while (result.next()) {
					nameModel.addElement(new String(result.getString("PersonName")));
				}
				result.close();
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
				System.out.println("Failure retreiving person list from database: " + e.getMessage());
				break;
			}
		}
		return (new JList<String>(nameModel));
	}

	public JList<String> getAvailPersonsAsString(Calendar today) {
		Date thisDay = Utilities.getDateFromCalendar(today);

		// TODO:
		// Get all persons who are available today
		DefaultListModel<String> nameModel = new DefaultListModel<String>();

		for (int i = 0; i < personList.size(); i++) {
			PersonModel p = personList.get(i);
			if (isPersonAvailable(p, thisDay))
				nameModel.addElement(new String(p.getName()));
		}
		return (new JList<String>(nameModel));
	}

	public LinkedList<PersonByTaskModel> getPersonsByTask(TaskModel task) {
		JList<PersonModel> persons = getAllPersons();
		LinkedList<PersonByTaskModel> thisTasksPersons = new LinkedList<PersonByTaskModel>();

		// TODO:
		for (int i = 0; i < persons.getModel().getSize(); i++) {
			PersonModel pModel = persons.getModel().getElementAt(i);

			// -1 = no match, 0 = assigned task
			if (checkPersonMatchForTask(pModel, task.getTaskID()) == 0) {
				// Match found, add to list
				thisTasksPersons.add(new PersonByTaskModel(pModel, task, false, task.getColor(), null));
			}
		}
		return thisTasksPersons;
	}

	// Return list of all persons assigned to this day, including single
	// instance assignments (subs) and floaters
	public LinkedList<PersonByTaskModel> getPersonsByDay(Calendar calendar) {
		int dayOfWeekInMonthIdx = calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH) - 1;
		int dayOfWeekIdx = calendar.get(Calendar.DAY_OF_WEEK) - 1;
		Date thisDay = Utilities.getDateFromCalendar(calendar);
		Calendar localCalendar = (Calendar) calendar.clone();

		// TODO:
		JList<PersonModel> persons = getAllPersons();
		LinkedList<CalendarDayModel> tasksForToday = getAllTasksByDay(localCalendar);
		LinkedList<PersonByTaskModel> thisDaysPersons = new LinkedList<PersonByTaskModel>();

		for (int i = 0; i < persons.getModel().getSize(); i++) {
			PersonModel pModel = getPersonByName(persons.getModel().getElementAt(i).toString());
			if (!isPersonAvailable(pModel, thisDay))
				continue;

			// Search through today's tasks for a person match
			for (int taskIdx = 0; taskIdx < tasksForToday.size(); taskIdx++) {
				TaskModel task = tasksForToday.get(taskIdx).getTask();

				// -1 = no match, 0 = assigned task, 1 = single instance task
				int match = checkPersonMatchForTaskByDay(pModel, task.getTaskID(), thisDay, dayOfWeekIdx,
						dayOfWeekInMonthIdx);

				if (match >= 0) {
					Utilities.addTimeToCalendar(localCalendar, task.getTime());
					PersonByTaskModel personByTask = new PersonByTaskModel(pModel, task, match == 0 ? false : true,
							task.getColor(), localCalendar);
					thisDaysPersons.add(personByTask);
				}
			}

			// Check if person is a floater (not associated with task)
			for (int j = 0; j < pModel.getSingleInstanceTasks().size(); j++) {
				SingleInstanceTaskModel task = pModel.getSingleInstanceTasks().get(j);
				if (checkFloaterMatch(task, thisDay, dayOfWeekIdx, dayOfWeekInMonthIdx) >= 0) {
					PersonByTaskModel personByTask = new PersonByTaskModel(pModel, null, false, task.getColor(),
							task.getTaskDate());
					thisDaysPersons.add(personByTask);
				}
			}
		}
		return (LinkedList<PersonByTaskModel>) thisDaysPersons;
	}

	public LinkedList<PersonByTaskModel> getPersonsByDayByTime(Calendar calendar) {
		LinkedList<PersonByTaskModel> persons = getPersonsByDay(calendar);

		// TODO:
		for (int i = 0; i < persons.size(); i++) {
			PersonByTaskModel person = persons.get(i);

			if (!Utilities.checkForTimeMatch(person.getTaskDate(), calendar)) {
				persons.remove(i);
				i--;
			}
		}
		return (LinkedList<PersonByTaskModel>) persons;
	}

	public LinkedList<PersonByTaskModel> getPersonsByDayByLocation(Calendar calendar, String location) {
		LinkedList<PersonByTaskModel> personList = getPersonsByDay(calendar);

		// TODO:
		for (int i = 0; i < personList.size(); i++) {
			PersonByTaskModel person = personList.get(i);

			if (person.getTask() == null || !person.getTask().getLocation().equals(location)) {
				personList.remove(i);
				i--;
			}
		}
		return (LinkedList<PersonByTaskModel>) personList;
	}

	public int getNumPersons() {
		if (!checkDatabaseConnection())
			return 0;

		int count = 0;
		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement selectStmt = dbConnection.prepareStatement("SELECT COUNT(*) AS Count FROM Persons;");
				ResultSet result = selectStmt.executeQuery();
				result.next();
				count = result.getInt("Count");

				result.close();
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
				System.out.println("Failure accessing Persons database: " + e.getMessage());
				break;
			}
		}
		return count;
	}

	/*
	 * ------- WOM and DOW Utilities --------
	 */
	private boolean[] createDaysOfWeekArray(int dow) {
		boolean[] dowBool = { false, false, false, false, false, false, false };
		for (int i = 0; i < 6; i++) {
			if ((dow & 1) == 1)
				dowBool[i] = true;
			dow >>= 1;
		}
		return dowBool;
	}

	private boolean[] createDowInMonthArray(int wom) {
		boolean[] womBool = { false, false, false, false, false };
		for (int i = 0; i < 5; i++) {
			if ((wom & 1) == 1)
				womBool[i] = true;
			wom >>= 1;
		}
		return womBool;
	}

	private int getDowAsInt(boolean[] dowArray) {
		int dow = 0;
		for (int k = 6; k >= 0; k--) {
			dow <<= 1;
			dow = dow | (dowArray[k] ? 1 : 0);
		}
		return dow;
	}

	private int getWomAsInt(boolean[] womArray) {
		int wom = 0;
		for (int k = 4; k >= 0; k--) {
			wom <<= 1;
			wom = wom | (womArray[k] ? 1 : 0);
		}
		return wom;
	}

	/*
	 * ------- mySQL import/export tables -------
	 */
	// TODO: Export mySQL Tables and save to a file, plus Import mySQL Tables
	// from file
}
