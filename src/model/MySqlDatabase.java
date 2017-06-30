package model;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JOptionPane;

import com.mysql.jdbc.CommunicationsException;

import utilities.Utilities;

public class MySqlDatabase {
	private static Connection dbConnection;

	public MySqlDatabase() {
		// Make initial connection to database
		connectDatabase();
	}

	/*
	 * ------- Database Connections -------
	 */
	private void connectDatabase() {
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();

		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			System.out.println("Unable to connect to database: " + e.getMessage());
			return;
		}

		try {
			String url = "jdbc:mysql://www.programplanner.org:3306/TestDb421";
			dbConnection = DriverManager.getConnection(url, "wavisTester1", "ImGladToBeTesting555&");
			return;

		} catch (SQLException e) {
			System.out.println("Unable to connect to database: " + e.getMessage());
			return;
		}
	}

	public void disconnectDatabase() {
		if (dbConnection != null) {
			try {
				dbConnection.close();
				dbConnection = null;
			} catch (SQLException e) {
				System.out.println("Failure closing database connection: " + e.getMessage());
			}
		}
	}

	private boolean checkDatabaseConnection() {
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
					System.out.println(Utilities.getCurrTime() + " - Attempting to re-connect to database...");
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
					System.out.println(Utilities.getCurrTime() + " - Attempting to re-connect to database...");
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
					System.out.println(Utilities.getCurrTime() + " - Attempting to re-connect to database...");
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

	public void deleteProgram(String programName) {
		if (!checkDatabaseConnection())
			return;

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement deleteProgramStmt = dbConnection
						.prepareStatement("DELETE FROM Programs WHERE ProgramName=?;");

				// Delete program
				deleteProgramStmt.setString(1, programName);
				deleteProgramStmt.executeUpdate();
				deleteProgramStmt.close();
				break;

			} catch (CommunicationsException e) {
				if (i == 0) {
					// First attempt to connect
					System.out.println(Utilities.getCurrTime() + " - Attempting to re-connect to database...");
					connectDatabase();
				} else
					// Second try
					System.out.println("Unable to connect to database: " + e.getMessage());

			} catch (SQLException e) {
				System.out.println("Failure deleting " + programName + ": " + e.getMessage());
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
				PreparedStatement selectStmt = dbConnection
						.prepareStatement("SELECT ProgramID, StartDate, EndDate FROM Programs WHERE ProgramName=?;");
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
					System.out.println(Utilities.getCurrTime() + " - Attempting to re-connect to database...");
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

	public ArrayList<String> getAllProgramsAsString() {
		ArrayList<String> nameList = new ArrayList<String>();

		if (!checkDatabaseConnection())
			return nameList;

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement selectStmt = dbConnection
						.prepareStatement("SELECT ProgramName FROM Programs ORDER BY ProgramName;");
				ResultSet result = selectStmt.executeQuery();
				while (result.next()) {
					nameList.add(result.getString("ProgramName"));
				}

				result.close();
				selectStmt.close();
				break;

			} catch (CommunicationsException e) {
				if (i == 0) {
					// First attempt to connect
					System.out.println(Utilities.getCurrTime() + " - Attempting to re-connect to database...");
					connectDatabase();
				} else
					// Second try
					System.out.println("Unable to connect to database: " + e.getMessage());

			} catch (SQLException e) {
				System.out.println("Failure retrieving Programs from database: " + e.getMessage());
				break;
			}
		}
		return nameList;
	}

	public ArrayList<ProgramModel> getAllPrograms() {
		ArrayList<ProgramModel> programList = new ArrayList<>();

		if (!checkDatabaseConnection())
			return programList;

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement selectStmt = dbConnection
						.prepareStatement("SELECT * FROM Programs ORDER BY ProgramName;");

				ResultSet result = selectStmt.executeQuery();
				while (result.next()) {
					programList.add(new ProgramModel(result.getInt("ProgramID"), result.getString("ProgramName"),
							Utilities.convertSqlDateToString(result.getDate("StartDate")),
							Utilities.convertSqlDateToString(result.getDate("EndDate")), null));
				}
				result.close();
				selectStmt.close();
				break;

			} catch (CommunicationsException e) {
				if (i == 0) {
					// First attempt to connect
					System.out.println(Utilities.getCurrTime() + " - Attempting to re-connect to database...");
					connectDatabase();
				} else
					// Second try
					System.out.println("Unable to connect to database: " + e.getMessage());

			} catch (SQLException e) {
				System.out.println("Failure retreiving program list from database: " + e.getMessage());
				break;
			}
		}
		return programList;
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
					System.out.println(Utilities.getCurrTime() + " - Attempting to re-connect to database...");
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
								+ "VALUES ((SELECT Programs.ProgramID FROM Programs WHERE Programs.ProgramName = '"
								+ programName + "'), ?, ?, ?, ?, ?, ?, ?, ?, ?);");

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
					System.out.println(Utilities.getCurrTime() + " - Attempting to re-connect to database...");
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

	public void updateTask(int taskID, String programName, String taskName, String location, int numLeadersReqd,
			int totalPersonsReqd, boolean[] dayOfWeek, boolean[] weekOfMonth, TimeModel time, int color,
			TimeModel origTaskTime) {
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

				// Check if task time has changed, and update single-instance
				// tasks
				if (!origTaskTime.equals(time))
					updateSingleInstanceTaskTime(taskID, time);
				break;

			} catch (CommunicationsException e) {
				if (i == 0) {
					// First attempt to connect
					System.out.println(Utilities.getCurrTime() + " - Attempting to re-connect to database...");
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

	public void updateSingleInstanceTaskId(String personName, Calendar calendar, String taskName) {
		if (!checkDatabaseConnection())
			return;

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement updateTaskStmt;
				int col = 1;

				if (taskName == null) {
					// Update SUB to Floater
					updateTaskStmt = dbConnection.prepareStatement("UPDATE SingleInstanceTasks "
							+ "INNER JOIN Persons ON SingleInstanceTasks.PersonID = Persons.PersonID "
							+ "SET TaskID=NULL WHERE Persons.PersonName=? AND SingleDate=? AND SingleTime=?;");
				} else {
					// Update floater to SUB
					updateTaskStmt = dbConnection.prepareStatement("UPDATE SingleInstanceTasks "
							+ "INNER JOIN Persons ON SingleInstanceTasks.PersonID = Persons.PersonID "
							+ "SET TaskID=(SELECT Tasks.TaskID FROM Tasks WHERE Tasks.TaskName=?) "
							+ "WHERE Persons.PersonName=? AND SingleDate=? AND SingleTime=?;");
					updateTaskStmt.setString(col++, taskName);
				}
				updateTaskStmt.setString(col++, personName);
				updateTaskStmt.setDate(col++, java.sql.Date.valueOf(Utilities.getSqlDate(calendar)));
				updateTaskStmt.setTime(col, java.sql.Time.valueOf(Utilities.getSqlTime(calendar)));

				updateTaskStmt.executeUpdate();
				updateTaskStmt.close();
				break;

			} catch (CommunicationsException e) {
				if (i == 0) {
					// First attempt to connect
					System.out.println(Utilities.getCurrTime() + " - Attempting to re-connect to database...");
					connectDatabase();
				} else
					// Second try
					System.out.println("Unable to connect to database: " + e.getMessage());

			} catch (SQLException e) {
				System.out.println("Failure switching sub & floater in database: " + e.getMessage());
				break;
			}
		}
	}

	private void updateSingleInstanceTaskTime(int taskID, TimeModel newTime) {
		if (!checkDatabaseConnection())
			return;

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement updateTaskStmt = dbConnection
						.prepareStatement("UPDATE SingleInstanceTasks SET SingleTime=? WHERE TaskID=?;");

				int col = 1;
				updateTaskStmt.setTime(col++, java.sql.Time.valueOf(Utilities.getSqlTime(newTime.getCalTime())));
				updateTaskStmt.setInt(col++, taskID);

				updateTaskStmt.executeUpdate();
				updateTaskStmt.close();
				break;

			} catch (CommunicationsException e) {
				if (i == 0) {
					// First attempt to connect
					System.out.println(Utilities.getCurrTime() + " - Attempting to re-connect to database...");
					connectDatabase();
				} else
					// Second try
					System.out.println("Unable to connect to database: " + e.getMessage());

			} catch (SQLException e) {
				System.out.println("Failure updating task time in database: " + e.getMessage());
				break;
			}
		}
	}

	public TaskModel renameTask(String programName, String oldName, String newName) {
		if (!checkDatabaseConnection())
			return null;

		TaskModel updatedTask = null;
		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement updateTaskStmt = dbConnection
						.prepareStatement("UPDATE Tasks SET TaskName=? WHERE TaskName=?;");

				int col = 1;
				updateTaskStmt.setString(col++, newName);
				updateTaskStmt.setString(col++, oldName);

				updateTaskStmt.executeUpdate();
				updateTaskStmt.close();

				updatedTask = getTaskByName(newName);
				break;

			} catch (CommunicationsException e) {
				if (i == 0) {
					// First attempt to connect
					System.out.println(Utilities.getCurrTime() + " - Attempting to re-connect to database...");
					connectDatabase();
				} else
					// Second try
					System.out.println("Unable to connect to database: " + e.getMessage());

			} catch (SQLException e) {
				System.out.println("Failure updating " + oldName + " task in database: " + e.getMessage());
				break;
			}
		}
		return updatedTask;
	}

	public void deleteTask(String taskName) {
		if (!checkDatabaseConnection())
			return;

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement deleteTaskStmt = dbConnection.prepareStatement("DELETE FROM Tasks WHERE TaskName=?;");

				// Delete task
				deleteTaskStmt.setString(1, taskName);
				deleteTaskStmt.executeUpdate();
				deleteTaskStmt.close();
				break;

			} catch (CommunicationsException e) {
				if (i == 0) {
					// First attempt to connect
					System.out.println(Utilities.getCurrTime() + " - Attempting to re-connect to database...");
					connectDatabase();
				} else
					// Second try
					System.out.println("Unable to connect to database: " + e.getMessage());

			} catch (SQLException e) {
				System.out.println("Failure deleting " + taskName + ": " + e.getMessage());
				break;
			}
		}
	}

	public TaskModel getTaskByName(String taskName) {
		if (!checkDatabaseConnection())
			return null;

		TaskModel task = null;
		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement selectStmt = dbConnection.prepareStatement("SELECT * FROM Tasks WHERE TaskName=?;");
				selectStmt.setString(1, taskName);

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
					System.out.println(Utilities.getCurrTime() + " - Attempting to re-connect to database...");
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
					System.out.println(Utilities.getCurrTime() + " - Attempting to re-connect to database...");
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

	public ArrayList<ArrayList<CalendarDayModel>> getAllTasksAndFloatersByMonth(Calendar calendar) {
		// Create a calendar list for each day of the month
		ArrayList<ArrayList<CalendarDayModel>> calendarList = new ArrayList<>();

		// Create an empty array list for each day of month
		for (int i = 0; i < 31; i++)
			calendarList.add(new ArrayList<CalendarDayModel>());

		// Return empty list if unable to connect to database
		if (!checkDatabaseConnection())
			return calendarList;

		int day, personCount;
		String taskName;
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		String date = Utilities.getSqlDate(calendar);
		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement updateMonthStmt = dbConnection
						.prepareStatement("CALL MonthlyCalendar('" + date + "');");
				ResultSet results = updateMonthStmt.executeQuery();

				while (results.next()) {
					day = results.getInt("Today");
					taskName = results.getString("TaskName");
					personCount = results.getInt("PersonCount") - results.getInt("UnavailCount");

					if (taskName == null) {
						// Floater
						Calendar cal = (Calendar) calendar.clone();
						cal.set(Calendar.DAY_OF_MONTH, day);
						Utilities.addTimeToCalendar(cal,
								new TimeModel(results.getInt("TaskHour"), results.getInt("TaskMinute")));
						if (personCount == 1)
							calendarList.get(day - 1)
									.add(new CalendarDayModel(null, personCount,
											results.getInt("LeaderCount") - results.getInt("UnavailLdrCount"),
											results.getInt("TaskColor"), cal, "Floater", true));
						else
							calendarList.get(day - 1)
									.add(new CalendarDayModel(null, personCount,
											results.getInt("LeaderCount") - results.getInt("UnavailLdrCount"),
											results.getInt("TaskColor"), cal, personCount + " Floaters", true));
					} else {
						// Don't need all of the task fields for calendar day
						TaskModel newTask = new TaskModel(results.getInt("TaskID"), results.getInt("ProgramID"),
								taskName, "", results.getInt("NumLdrsReqd"), results.getInt("NumPersonsReqd"), null,
								null, new TimeModel(results.getInt("TaskHour"), results.getInt("TaskMinute")),
								results.getInt("TaskColor"));
						calendarList.get(day - 1)
								.add(new CalendarDayModel(newTask, personCount + results.getInt("SubCount"),
										results.getInt("LeaderCount") - results.getInt("UnavailLdrCount")
												+ results.getInt("SubLeaderCount"),
										results.getInt("TaskColor"), null, "", true));
					}
				}
				results.close();
				updateMonthStmt.close();
				break;

			} catch (CommunicationsException e) {
				if (i == 0) {
					// First attempt to connect
					System.out.println(Utilities.getCurrTime() + " - Attempting to re-connect to database...");
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

	public ArrayList<ArrayList<CalendarDayModel>> getTasksByLocationByMonth(Calendar calendar,
			ArrayList<String> locations) {
		return getTasksByFilterByMonth(calendar, locations, "MonthlyCalendarByLocation", true);
	}

	public ArrayList<ArrayList<CalendarDayModel>> getTasksByPersonsByMonth(Calendar calendar,
			ArrayList<String> personList) {
		return getTasksByFilterByMonth(calendar, personList, "MonthlyCalendarByPersons", false);
	}

	public ArrayList<ArrayList<CalendarDayModel>> getTasksByProgramByMonth(Calendar calendar,
			ArrayList<String> programList) {
		return getTasksByFilterByMonth(calendar, programList, "MonthlyCalendarByProgram", true);
	}

	private ArrayList<ArrayList<CalendarDayModel>> getTasksByFilterByMonth(Calendar calendar,
			ArrayList<String> filterList, String subroutineName, boolean showCounts) {
		// Create a calendar list for each day of the month
		ArrayList<ArrayList<CalendarDayModel>> calendarList = new ArrayList<>();

		// Create an empty array list for each day of month
		for (int i = 0; i < 31; i++)
			calendarList.add(new ArrayList<CalendarDayModel>());

		// Return empty list if unable to connect to database
		if (!checkDatabaseConnection())
			return calendarList;

		int day, personCount;
		String taskName;
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		String date = Utilities.getSqlDate(calendar);

		// Create filter string
		String filterString = "";
		for (int i = 0; i < filterList.size(); i++) {
			if (i > 0)
				filterString += ",";
			filterString += filterList.get(i);
		}

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement updateMonthStmt = dbConnection
						.prepareStatement("CALL " + subroutineName + "('" + date + "', '" + filterString + "');");
				ResultSet results = updateMonthStmt.executeQuery();

				while (results.next()) {
					day = results.getInt("Today");
					taskName = results.getString("TaskName");
					personCount = results.getInt("PersonCount") - results.getInt("UnavailCount");

					// Don't need all task fields for calendar
					TaskModel newTask = new TaskModel(results.getInt("TaskID"), results.getInt("ProgramID"), taskName,
							results.getString("Location"), results.getInt("NumLdrsReqd"),
							results.getInt("NumPersonsReqd"), null, null,
							new TimeModel(results.getInt("TaskHour"), results.getInt("TaskMinute")),
							results.getInt("TaskColor"));
					calendarList.get(day - 1)
							.add(new CalendarDayModel(newTask, personCount + results.getInt("SubCount"),
									results.getInt("LeaderCount") - results.getInt("UnavailLdrCount")
											+ results.getInt("SubLeaderCount"),
									results.getInt("TaskColor"), null, "", showCounts));
				}
				results.close();
				updateMonthStmt.close();
				break;

			} catch (CommunicationsException e) {
				if (i == 0) {
					// First attempt to connect
					System.out.println(Utilities.getCurrTime() + " - Attempting to re-connect to database...");
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

	public ArrayList<ArrayList<CalendarDayModel>> getTasksByTimeByMonth(Calendar calendar, ArrayList<String> times) {
		// Create a calendar list for each day of the month
		ArrayList<ArrayList<CalendarDayModel>> calendarList = new ArrayList<>();

		// Create an empty array list for each day of month
		for (int i = 0; i < 31; i++)
			calendarList.add(new ArrayList<CalendarDayModel>());

		// Return empty list if unable to connect to database
		if (!checkDatabaseConnection())
			return calendarList;

		int day, personCount;
		String taskName;
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		String date = Utilities.getSqlDate(calendar);

		// Create filter string with times
		String timeFilter = "";
		for (int i = 0; i < times.size(); i++) {
			if (i > 0)
				timeFilter += ",";
			timeFilter += Utilities.getSqlTime(Utilities.getCalendarTime(times.get(i)));
		}

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement updateMonthStmt = dbConnection
						.prepareStatement("CALL MonthlyCalendarByTime('" + date + "', '" + timeFilter + "');");
				ResultSet results = updateMonthStmt.executeQuery();

				while (results.next()) {
					day = results.getInt("Today");
					taskName = results.getString("TaskName");
					personCount = results.getInt("PersonCount") - results.getInt("UnavailCount");

					// Don't need all of the task fields for calendar
					TaskModel newTask = new TaskModel(results.getInt("TaskID"), results.getInt("ProgramID"), taskName,
							results.getString("Location"), results.getInt("NumLdrsReqd"),
							results.getInt("NumPersonsReqd"), null, null,
							new TimeModel(results.getInt("TaskHour"), results.getInt("TaskMinute")),
							results.getInt("TaskColor"));
					calendarList.get(day - 1)
							.add(new CalendarDayModel(newTask, personCount + results.getInt("SubCount"),
									results.getInt("LeaderCount") - results.getInt("UnavailLdrCount")
											+ results.getInt("SubLeaderCount"),
									results.getInt("TaskColor"), null, "", true));
				}
				results.close();
				updateMonthStmt.close();
				break;

			} catch (CommunicationsException e) {
				if (i == 0) {
					// First attempt to connect
					System.out.println(Utilities.getCurrTime() + " - Attempting to re-connect to database...");
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

	public ArrayList<ArrayList<CalendarDayModel>> getTasksByIncompleteRosterByMonth(Calendar calendar) {
		// Create a calendar list for each day of the month
		ArrayList<ArrayList<CalendarDayModel>> calendarList = new ArrayList<>();

		// Create an empty array list for each day of month
		for (int i = 0; i < 31; i++)
			calendarList.add(new ArrayList<CalendarDayModel>());

		// Return empty list if unable to connect to database
		if (!checkDatabaseConnection())
			return calendarList;

		int day, personCount;
		String taskName;
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		String date = Utilities.getSqlDate(calendar);

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement updateMonthStmt = dbConnection
						.prepareStatement("CALL MonthlyCalendarByRoster('" + date + "');");
				ResultSet results = updateMonthStmt.executeQuery();

				while (results.next()) {
					day = results.getInt("Today");
					taskName = results.getString("TaskName");
					personCount = results.getInt("PersonCount") - results.getInt("UnavailCount");

					// Don't need all of the task fields for calendar
					TaskModel newTask = new TaskModel(results.getInt("TaskID"), results.getInt("ProgramID"), taskName,
							results.getString("Location"), results.getInt("NumLdrsReqd"),
							results.getInt("NumPersonsReqd"), null, null,
							new TimeModel(results.getInt("TaskHour"), results.getInt("TaskMinute")),
							results.getInt("TaskColor"));
					calendarList.get(day - 1)
							.add(new CalendarDayModel(newTask, personCount + results.getInt("SubCount"),
									results.getInt("LeaderCount") - results.getInt("UnavailLdrCount")
											+ results.getInt("SubLeaderCount"),
									results.getInt("TaskColor"), null, "", true));
				}
				results.close();
				updateMonthStmt.close();
				break;

			} catch (CommunicationsException e) {
				if (i == 0) {
					// First attempt to connect
					System.out.println(Utilities.getCurrTime() + " - Attempting to re-connect to database...");
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
				PreparedStatement selectStmt = dbConnection.prepareStatement("SELECT * FROM Tasks, Programs "
						+ "WHERE ProgramName=? AND Programs.ProgramID = Tasks.ProgramID "
						+ "ORDER BY Hour, Minute, TaskName;");
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
					System.out.println(Utilities.getCurrTime() + " - Attempting to re-connect to database...");
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

	public ArrayList<TaskModel> getAllTasks() {
		ArrayList<TaskModel> taskList = new ArrayList<TaskModel>();

		if (!checkDatabaseConnection())
			return taskList;

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement selectStmt = dbConnection
						.prepareStatement("SELECT * FROM Tasks ORDER BY Hour, Minute, TaskName;");

				ResultSet result = selectStmt.executeQuery();
				while (result.next()) {
					taskList.add((new TaskModel(result.getInt("TaskID"), result.getInt("ProgramID"),
							result.getString("TaskName"), result.getString("Location"), result.getInt("NumLeadersReqd"),
							result.getInt("TotalPersonsReqd"), createDaysOfWeekArray(result.getInt("DaysOfWeek")),
							createDowInMonthArray(result.getInt("DowInMonth")),
							new TimeModel(result.getInt("Hour"), result.getInt("Minute")), result.getInt("Color"))));
				}
				result.close();
				selectStmt.close();
				break;

			} catch (CommunicationsException e) {
				if (i == 0) {
					// First attempt to connect
					System.out.println(Utilities.getCurrTime() + " - Attempting to re-connect to database...");
					connectDatabase();
				} else
					// Second try
					System.out.println("Unable to connect to database: " + e.getMessage());

			} catch (SQLException e) {
				System.out.println("Failure retreiving task list from database: " + e.getMessage());
				break;
			}
		}
		return taskList;
	}

	public ArrayList<TaskTimeModel> getAllTasksWithTimeByDay(Calendar calendar) {
		int dayOfWeekInMonthIdx = calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH) - 1;
		int dayOfWeekIdx = calendar.get(Calendar.DAY_OF_WEEK) - 1;
		Calendar localCalendar = (Calendar) calendar.clone();
		String sqlDate = Utilities.getSqlDate(localCalendar);

		ArrayList<TaskTimeModel> taskTimeList = new ArrayList<TaskTimeModel>();

		if (!checkDatabaseConnection())
			return taskTimeList;

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement selectStmt = dbConnection.prepareStatement(
						// Select tasks with matching DOW and WOM
						"SELECT TaskName, Hour, Minute FROM Tasks, Programs WHERE (Tasks.ProgramID = Programs.ProgramID "
								// Check if program expired
								+ "  AND ((Programs.StartDate IS NULL) OR (? >= Programs.StartDate)) "
								+ "  AND ((Programs.EndDate IS NULL) OR (? <= Programs.EndDate))) "

								// Check if task is active today
								+ "  AND (Tasks.DaysOfWeek & (1 << ?)) != 0 "
								+ "  AND (Tasks.DowInMonth & (1 << ?)) != 0;");

				int row = 1;
				selectStmt.setString(row++, sqlDate);
				selectStmt.setString(row++, sqlDate);
				selectStmt.setInt(row++, dayOfWeekIdx);
				selectStmt.setInt(row, dayOfWeekInMonthIdx);

				ResultSet result = selectStmt.executeQuery();
				while (result.next()) {
					taskTimeList.add(new TaskTimeModel(result.getString("TaskName"),
							new TimeModel(result.getInt("Hour"), result.getInt("Minute"))));
				}
				result.close();
				selectStmt.close();
				break;

			} catch (CommunicationsException e) {
				if (i == 0) {
					// First attempt to connect
					System.out.println(Utilities.getCurrTime() + " - Attempting to re-connect to database...");
					connectDatabase();
				} else
					// Second try
					System.out.println("Unable to connect to database: " + e.getMessage());

			} catch (SQLException e) {
				System.out.println("Failure retreiving task list from database: " + e.getMessage());
				break;
			}
		}
		return taskTimeList;
	}

	public ArrayList<String> getAllLocationsAsString() {
		ArrayList<String> locationList = new ArrayList<String>();

		if (!checkDatabaseConnection())
			return locationList;

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement selectStmt = dbConnection.prepareStatement("SELECT Location "
						+ "FROM Tasks WHERE Location != '' " + "GROUP BY Location ORDER BY Location;");

				ResultSet result = selectStmt.executeQuery();
				while (result.next()) {
					locationList.add(result.getString("Location"));
				}
				result.close();
				selectStmt.close();
				break;

			} catch (CommunicationsException e) {
				if (i == 0) {
					// First attempt to connect
					System.out.println(Utilities.getCurrTime() + " - Attempting to re-connect to database...");
					connectDatabase();
				} else
					// Second try
					System.out.println("Unable to connect to database: " + e.getMessage());

			} catch (SQLException e) {
				System.out.println("Failure retreiving task locations from database: " + e.getMessage());
				break;
			}
		}
		return locationList;
	}

	public ArrayList<String> getAllTimesAsString() {
		ArrayList<String> timeList = new ArrayList<String>();

		if (!checkDatabaseConnection())
			return timeList;

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement selectStmt = dbConnection.prepareStatement("SELECT Hour, Minute "
						+ "FROM Tasks WHERE Hour IS NOT NULL GROUP BY Hour, Minute ORDER BY Hour, Minute;");

				ResultSet result = selectStmt.executeQuery();
				while (result.next()) {
					timeList.add(new TimeModel(result.getInt("Hour"), result.getInt("Minute")).toString());
				}
				result.close();
				selectStmt.close();
				break;

			} catch (CommunicationsException e) {
				if (i == 0) {
					// First attempt to connect
					System.out.println(Utilities.getCurrTime() + " - Attempting to re-connect to database...");
					connectDatabase();
				} else
					// Second try
					System.out.println("Unable to connect to database: " + e.getMessage());

			} catch (SQLException e) {
				System.out.println("Failure retreiving task times from database: " + e.getMessage());
				break;
			}
		}
		return timeList;
	}

	public ArrayList<TimeModel> getAllTimesByDay(Calendar calendar) {
		int dayOfWeekInMonthIdx = calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH) - 1;
		int dayOfWeekIdx = calendar.get(Calendar.DAY_OF_WEEK) - 1;
		Calendar localCalendar = (Calendar) calendar.clone();
		String sqlDate = Utilities.getSqlDate(localCalendar);

		ArrayList<TimeModel> timeList = new ArrayList<TimeModel>();

		if (!checkDatabaseConnection())
			return timeList;

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement selectStmt = dbConnection.prepareStatement(
						// Select tasks with matching DOW and WOM
						"SELECT Hour, Minute FROM Tasks, Programs WHERE (Tasks.ProgramID = Programs.ProgramID "
								// Check if program expired
								+ "  AND ((Programs.StartDate IS NULL) OR (? >= Programs.StartDate)) "
								+ "  AND ((Programs.EndDate IS NULL) OR (? <= Programs.EndDate))) "

								// Check if task is active today
								+ "  AND (Tasks.DaysOfWeek & (1 << ?)) != 0 "
								+ "  AND (Tasks.DowInMonth & (1 << ?)) != 0 "

								+ "GROUP BY Hour, Minute "

								+ "ORDER BY Hour, Minute;");

				int row = 1;
				selectStmt.setString(row++, sqlDate);
				selectStmt.setString(row++, sqlDate);
				selectStmt.setInt(row++, dayOfWeekIdx);
				selectStmt.setInt(row, dayOfWeekInMonthIdx);

				ResultSet result = selectStmt.executeQuery();
				while (result.next()) {
					timeList.add(new TimeModel(result.getInt("Hour"), result.getInt("Minute")));
				}
				result.close();
				selectStmt.close();
				break;

			} catch (CommunicationsException e) {
				if (i == 0) {
					// First attempt to connect
					System.out.println(Utilities.getCurrTime() + " - Attempting to re-connect to database...");
					connectDatabase();
				} else
					// Second try
					System.out.println("Unable to connect to database: " + e.getMessage());

			} catch (SQLException e) {
				System.out.println("Failure retreiving time list from database: " + e.getMessage());
				break;
			}
		}
		return timeList;
	}

	/*
	 * ------- Person data -------
	 */
	public void addPerson(String name, String phone, String email, boolean leader, String notes,
			ArrayList<AssignedTasksModel> assignedTasks, ArrayList<SingleInstanceTaskModel> extraTasks,
			ArrayList<DateRangeModel> datesUnavailable) {

		int personID = addPersonInfo(name, phone, email, leader, notes);
		if (personID <= 0)
			return;

		for (int i = 0; i < assignedTasks.size(); i++) {
			AssignedTasksModel task = assignedTasks.get(i);
			addAssignedTask(personID, task.getTaskID(), task.getDaysOfWeek(), task.getWeeksOfMonth());
		}

		for (int i = 0; i < extraTasks.size(); i++) {
			SingleInstanceTaskModel task = extraTasks.get(i);
			addSingleInstanceTask(name, task.getProgramName(), task.getTaskID(), task.getTaskDate(), task.getColor());
		}

		for (int i = 0; i < datesUnavailable.size(); i++) {
			DateRangeModel dates = datesUnavailable.get(i);
			addUnavailDates(personID, dates.getStartDate(), dates.getEndDate());
		}
	}

	public void updatePerson(String personName, String personPhone, String personEmail, boolean personIsLeader,
			String personNotes, ArrayList<AssignedTasksModel> personAssignedTasks,
			ArrayList<SingleInstanceTaskModel> extraTasks, ArrayList<DateRangeModel> personDatesUnavailable) {

		// Update person info
		updatePersonInfo(personName, personPhone, personEmail, personIsLeader, personNotes);

		// Add or update the assigned tasks
		for (int i = 0; i < personAssignedTasks.size(); i++) {
			// Update Assigned Tasks database for this person
			AssignedTasksModel assignedTask = personAssignedTasks.get(i);
			if (assignedTask.getElementStatus() == ListStatus.LIST_ELEMENT_NEW)
				// Assigned task not already in list, so insert
				addAssignedTask(assignedTask.getPersonID(), assignedTask.getTaskID(), assignedTask.getDaysOfWeek(),
						assignedTask.getWeeksOfMonth());
			else if (assignedTask.getElementStatus() == ListStatus.LIST_ELEMENT_UPDATE)
				// Assigned task exists, so update fields
				updateAssignedTask(assignedTask.getAssignedTaskID(), assignedTask.getDaysOfWeek(),
						assignedTask.getWeeksOfMonth());
			else if (assignedTask.getElementStatus() == ListStatus.LIST_ELEMENT_DELETE)
				// Assigned task being deleted
				deleteAssignedTask(assignedTask.getAssignedTaskID());
		}

		// Add/remove extraTasks
		for (int i = 0; i < extraTasks.size(); i++) {
			// Add/remove single instance task in database
			SingleInstanceTaskModel singleTask = extraTasks.get(i);
			if (singleTask.getElementStatus() == ListStatus.LIST_ELEMENT_DELETE)
				removeSingleInstanceTask(personName, singleTask.getTaskDate());
			else if (singleTask.getElementStatus() == ListStatus.LIST_ELEMENT_NEW)
				addSingleInstanceTask(personName, singleTask.getProgramName(), singleTask.getTaskID(),
						singleTask.getTaskDate(), singleTask.getColor());
		}

		// Add/remove dates unavailable (check for duplicates)
		for (int i = 0; i < personDatesUnavailable.size(); i++) {
			// Add/remove unavailable dates if not a duplicate
			DateRangeModel date = personDatesUnavailable.get(i);
			if (date.getElementStatus() == ListStatus.LIST_ELEMENT_DELETE)
				removeUnavailDates(personName, date.getStartDate(), date.getEndDate());
			else if (date.getElementStatus() == ListStatus.LIST_ELEMENT_NEW)
				updateUnavailDates(personName, date.getStartDate(), date.getEndDate());
		}
	}

	public void updatePersonNotes(String personName, String personNotes) {
		if (!checkDatabaseConnection())
			return;

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement updatePersonStmt = dbConnection
						.prepareStatement("UPDATE Persons SET Notes=? WHERE PersonName=?;");

				// Update person notes field
				updatePersonStmt.setString(1, personNotes);
				updatePersonStmt.setString(2, personName);

				updatePersonStmt.executeUpdate();
				updatePersonStmt.close();
				break;

			} catch (CommunicationsException e) {
				if (i == 0) {
					// First attempt to connect
					System.out.println(Utilities.getCurrTime() + " - Attempting to re-connect to database...");
					connectDatabase();
				} else
					// Second try
					System.out.println("Unable to connect to database: " + e.getMessage());

			} catch (SQLException e) {
				System.out.println("Failure updating " + personName + " notes to database: " + e.getMessage());
				break;
			}
		}
		return;
	}

	public void removePerson(String personName) {
		if (!checkDatabaseConnection())
			return;

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement deletePersonStmt = dbConnection
						.prepareStatement("DELETE FROM Persons WHERE PersonName=?;");

				// Delete assigned task
				deletePersonStmt.setString(1, personName);
				deletePersonStmt.executeUpdate();
				deletePersonStmt.close();
				break;

			} catch (CommunicationsException e) {
				if (i == 0) {
					// First attempt to connect
					System.out.println(Utilities.getCurrTime() + " - Attempting to re-connect to database...");
					connectDatabase();
				} else
					// Second try
					System.out.println("Unable to connect to database: " + e.getMessage());

			} catch (SQLException e) {
				System.out.println("Failure deleting " + personName + ": " + e.getMessage());
				break;
			}
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
					System.out.println(Utilities.getCurrTime() + " - Attempting to re-connect to database...");
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

	private void updatePersonInfo(String personName, String phone, String email, boolean leader, String notes) {
		if (!checkDatabaseConnection())
			return;

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement updatePersonStmt = dbConnection.prepareStatement(
						"UPDATE Persons SET PhoneNumber=?, EMail=?, isLeader=?, Notes=? WHERE PersonName=?;");

				// Update person
				int col = 1;
				updatePersonStmt.setString(col++, phone);
				updatePersonStmt.setString(col++, email);
				updatePersonStmt.setBoolean(col++, leader);
				updatePersonStmt.setString(col++, notes);
				updatePersonStmt.setString(col, personName);

				updatePersonStmt.executeUpdate();
				updatePersonStmt.close();
				break;

			} catch (CommunicationsException e) {
				if (i == 0) {
					// First attempt to connect
					System.out.println(Utilities.getCurrTime() + " - Attempting to re-connect to database...");
					connectDatabase();
				} else
					// Second try
					System.out.println("Unable to connect to database: " + e.getMessage());

			} catch (SQLException e) {
				System.out.println("Failure updating " + personName + " info to database: " + e.getMessage());
				break;
			}
		}
		return;
	}

	private void addAssignedTask(int personID, int taskID, boolean[] daysOfWeek, boolean[] weeksOfMonth) {
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
					System.out.println(Utilities.getCurrTime() + " - Attempting to re-connect to database...");
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

	private void updateAssignedTask(int assignedTaskID, boolean[] daysOfWeek, boolean[] weeksOfMonth) {
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
					System.out.println(Utilities.getCurrTime() + " - Attempting to re-connect to database...");
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

	private void deleteAssignedTask(int assignedTaskID) {
		if (!checkDatabaseConnection())
			return;

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement deleteAssignedTaskStmt = dbConnection
						.prepareStatement("DELETE FROM AssignedTasks WHERE AssignedTaskID=?;");

				// Delete assigned task
				deleteAssignedTaskStmt.setInt(1, assignedTaskID);
				deleteAssignedTaskStmt.executeUpdate();
				deleteAssignedTaskStmt.close();
				break;

			} catch (CommunicationsException e) {
				if (i == 0) {
					// First attempt to connect
					System.out.println(Utilities.getCurrTime() + " - Attempting to re-connect to database...");
					connectDatabase();
				} else
					// Second try
					System.out.println("Unable to connect to database: " + e.getMessage());

			} catch (SQLException e) {
				System.out.println("Failure deleting task assignment: " + e.getMessage());
				break;
			}
		}
	}

	private ArrayList<AssignedTasksModel> getAssignedTasks(String personName) {
		ArrayList<AssignedTasksModel> taskList = new ArrayList<>();

		if (!checkDatabaseConnection())
			return taskList;

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement selectStmt = dbConnection.prepareStatement("SELECT ProgramName, "
						+ "  TaskName, Persons.PersonID AS PersonID, AssignedTasks.AssignedTaskID AS AssignedTaskID, "
						+ "  AssignedTasks.DaysOfWeek AS DaysOfWeek, AssignedTasks.DowInMonth AS DowInMonth "
						+ "FROM AssignedTasks, Persons, Tasks, Programs WHERE Persons.PersonName = ? "
						+ "  AND Persons.PersonID = AssignedTasks.PersonID "
						+ "  AND Tasks.TaskID = AssignedTasks.TaskID AND Tasks.ProgramID = Programs.ProgramID "
						+ "ORDER BY ProgramName, TaskName;");
				selectStmt.setString(1, personName);

				ResultSet result = selectStmt.executeQuery();
				while (result.next()) {
					taskList.add(new AssignedTasksModel(result.getInt("AssignedTaskID"), result.getInt("PersonID"), 0,
							result.getString("ProgramName"), result.getString("TaskName"),
							createDaysOfWeekArray(result.getInt("DaysOfWeek")),
							createDowInMonthArray(result.getInt("DowInMonth"))));
				}
				result.close();
				selectStmt.close();
				break;

			} catch (CommunicationsException e) {
				if (i == 0) {
					// First attempt to connect
					System.out.println(Utilities.getCurrTime() + " - Attempting to re-connect to database...");
					connectDatabase();
				} else
					// Second try
					System.out.println("Unable to connect to database: " + e.getMessage());

			} catch (SQLException e) {
				System.out.println(
						"Failure retreiving task list for " + personName + " from database: " + e.getMessage());
				break;
			}
		}
		return taskList;
	}

	private void addSingleInstanceTask(String personName, String programName, int taskID, Calendar taskTime,
			int color) {
		if (!checkDatabaseConnection())
			return;

		for (int i = 0; i < 2; i++) {
			PreparedStatement addSingleTaskStmt = null;
			try {
				if (taskID == 0) {
					addSingleTaskStmt = dbConnection.prepareStatement(
							"INSERT INTO SingleInstanceTasks (PersonID, ProgramID, SingleDate, SingleTime, Color) "
									+ "VALUES ((SELECT PersonID FROM Persons WHERE PersonName=?), "
									+ "(SELECT ProgramID FROM Programs WHERE ProgramName=?), ?, ?, ?);");
				} else {
					addSingleTaskStmt = dbConnection.prepareStatement(
							"INSERT INTO SingleInstanceTasks (PersonID, ProgramID, TaskID, SingleDate, SingleTime, Color) "
									+ "VALUES ((SELECT PersonID FROM Persons WHERE PersonName=?), "
									+ "   (SELECT ProgramID FROM Tasks WHERE Tasks.TaskID=?), ?, ?, ?, ?);");
				}

				// Add new Single Instance Task
				int col = 1;
				addSingleTaskStmt.setString(col++, personName);
				if (taskID == 0)
					addSingleTaskStmt.setString(col++, programName);
				else {
					addSingleTaskStmt.setInt(col++, taskID);
					addSingleTaskStmt.setInt(col++, taskID);
				}
				addSingleTaskStmt.setDate(col++, java.sql.Date.valueOf(Utilities.getSqlDate(taskTime)));
				addSingleTaskStmt.setTime(col++, java.sql.Time.valueOf(Utilities.getSqlTime(taskTime)));
				addSingleTaskStmt.setInt(col, color);

				addSingleTaskStmt.executeUpdate();
				addSingleTaskStmt.close();
				break;

			} catch (CommunicationsException e) {
				if (i == 0) {
					// First attempt to connect
					System.out.println(Utilities.getCurrTime() + " - Attempting to re-connect to database...");
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

	private void removeSingleInstanceTask(String personName, Calendar singleDate) {
		if (!checkDatabaseConnection())
			return;

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement deleteExtraTaskStmt = dbConnection.prepareStatement("DELETE FROM SingleInstanceTasks "
						+ "WHERE PersonID=(SELECT PersonID FROM Persons WHERE PersonName=?) "
						+ "AND SingleDate=? AND SingleTime=?;");

				// Delete single instance task
				deleteExtraTaskStmt.setString(1, personName);
				deleteExtraTaskStmt.setDate(2, java.sql.Date.valueOf(Utilities.getSqlDate(singleDate)));
				deleteExtraTaskStmt.setTime(3, java.sql.Time.valueOf(Utilities.getSqlTime(singleDate)));
				deleteExtraTaskStmt.executeUpdate();
				deleteExtraTaskStmt.close();
				break;

			} catch (CommunicationsException e) {
				if (i == 0) {
					// First attempt to connect
					System.out.println(Utilities.getCurrTime() + " - Attempting to re-connect to database...");
					connectDatabase();
				} else
					// Second try
					System.out.println("Unable to connect to database: " + e.getMessage());

			} catch (SQLException e) {
				System.out.println("Failure deleting extra task: " + e.getMessage());
				break;
			}
		}
	}

	public void addSingleInstanceTask(String personName, String programName, Calendar singleDate, TaskModel task,
			int color) {
		if (!checkDatabaseConnection())
			return;

		for (int i = 0; i < 2; i++) {
			PreparedStatement addSingleTaskStmt = null;
			try {
				int col = 1;
				if (task == null || task.getTaskID() == 0) {
					addSingleTaskStmt = dbConnection.prepareStatement(
							"INSERT INTO SingleInstanceTasks (PersonID, ProgramID, SingleDate, SingleTime, Color) VALUES "
									+ "((SELECT PersonID FROM Persons WHERE PersonName=?), "
									+ "(SELECT ProgramID FROM Programs WHERE ProgramName=?), ?, ?, ?);");
					addSingleTaskStmt.setString(col++, personName);
					addSingleTaskStmt.setString(col++, programName);
				} else {
					addSingleTaskStmt = dbConnection.prepareStatement(
							"INSERT INTO SingleInstanceTasks (PersonID, TaskID, ProgramID, SingleDate, SingleTime, Color) "
									+ "VALUES ((SELECT PersonID FROM Persons WHERE PersonName=?), ?, ?, ?, ?, ?);");
					addSingleTaskStmt.setString(col++, personName);
					addSingleTaskStmt.setInt(col++, task.getTaskID());
					addSingleTaskStmt.setInt(col++, task.getProgramID());
				}

				// Add new Single Instance Task
				addSingleTaskStmt.setDate(col++, java.sql.Date.valueOf(Utilities.getSqlDate(singleDate)));
				addSingleTaskStmt.setTime(col++, java.sql.Time.valueOf(Utilities.getSqlTime(singleDate)));
				addSingleTaskStmt.setInt(col, color);

				addSingleTaskStmt.executeUpdate();
				addSingleTaskStmt.close();
				break;

			} catch (CommunicationsException e) {
				if (i == 0) {
					// First attempt to connect
					System.out.println(Utilities.getCurrTime() + " - Attempting to re-connect to database...");
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

	public ArrayList<SingleInstanceTaskModel> getSingleInstanceTasks(String personName) {
		ArrayList<SingleInstanceTaskModel> singleTaskList = new ArrayList<>();

		if (!checkDatabaseConnection())
			return singleTaskList;

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement selectStmt = dbConnection.prepareStatement(
						"SELECT TaskName, SingleInstanceTasks.TaskID, " + "Programs.ProgramName AS ProgramName, "
								+ "SingleDate, SingleTime, SingleInstanceTasks.Color  "
								+ "FROM SingleInstanceTasks, Tasks, Persons, Programs "
								+ "WHERE Persons.PersonName=? AND Persons.PersonID = SingleInstanceTasks.PersonID "
								+ "AND Programs.ProgramID = SingleInstanceTasks.ProgramID "
								+ "AND (SingleInstanceTasks.TaskID IS NULL OR SingleInstanceTasks.TaskID = Tasks.TaskID) "
								+ "GROUP BY SingleInstanceID ORDER BY SingleDate, SingleTime;");
				selectStmt.setString(1, personName);

				ResultSet result = selectStmt.executeQuery();
				while (result.next()) {
					String taskName = "";
					int taskID = result.getInt("SingleInstanceTasks.TaskID");
					if (taskID > 0)
						taskName = result.getString("TaskName");

					singleTaskList.add(new SingleInstanceTaskModel(taskID, result.getString("ProgramName"), taskName,
							Utilities.convertSqlDateTime(result.getDate("SingleDate"), result.getTime("SingleTime")),
							result.getInt("SingleInstanceTasks.Color")));
				}
				result.close();
				selectStmt.close();
				break;

			} catch (CommunicationsException e) {
				if (i == 0) {
					// First attempt to connect
					System.out.println(Utilities.getCurrTime() + " - Attempting to re-connect to database...");
					connectDatabase();
				} else
					// Second try
					System.out.println("Unable to connect to database: " + e.getMessage());

			} catch (SQLException e) {
				System.out.println(
						"Failure retreiving sub/floater list for " + personName + " from database: " + e.getMessage());
				break;
			}
		}
		return singleTaskList;
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
				addUnavailDatesStmt.setDate(2, java.sql.Date.valueOf(startDate));
				addUnavailDatesStmt.setDate(3, java.sql.Date.valueOf(endDate));

				addUnavailDatesStmt.executeUpdate();
				addUnavailDatesStmt.close();
				break;

			} catch (CommunicationsException e) {
				if (i == 0) {
					// First attempt to connect
					System.out.println(Utilities.getCurrTime() + " - Attempting to re-connect to database...");
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

	private void addUnavailDates(String personName, String startDate, String endDate) {
		if (!checkDatabaseConnection())
			return;

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement addUnavailDatesStmt = dbConnection
						.prepareStatement("INSERT INTO UnavailDates (PersonID, StartDate, EndDate) VALUES "
								+ "((SELECT PersonID FROM Persons WHERE PersonName=?), ?, ?);");

				// Add new Unavailable Dates
				addUnavailDatesStmt.setString(1, personName);
				addUnavailDatesStmt.setDate(2, java.sql.Date.valueOf(startDate));
				addUnavailDatesStmt.setDate(3, java.sql.Date.valueOf(endDate));

				addUnavailDatesStmt.executeUpdate();
				addUnavailDatesStmt.close();
				break;

			} catch (CommunicationsException e) {
				if (i == 0) {
					// First attempt to connect
					System.out.println(Utilities.getCurrTime() + " - Attempting to re-connect to database...");
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

	private void updateUnavailDates(String personName, String startDate, String endDate) {
		if (!checkDatabaseConnection())
			return;

		for (int i = 0; i < 2; i++) {
			try {
				// Check whether start/end dates within existing unavailable
				// date range
				PreparedStatement prepStmt = dbConnection.prepareStatement("SELECT COUNT(*) FROM UnavailDates, Persons "
						+ "WHERE (? BETWEEN StartDate AND EndDate) AND (? BETWEEN StartDate AND EndDate) "
						+ "   AND PersonName=? AND UnavailDates.PersonID = Persons.PersonID;");
				prepStmt.setDate(1, java.sql.Date.valueOf(startDate));
				prepStmt.setDate(2, java.sql.Date.valueOf(endDate));
				prepStmt.setString(3, personName);
				ResultSet result = prepStmt.executeQuery();
				result.next();

				if (result.getInt(1) == 0) {
					// No match for start/end dates, so add date range
					addUnavailDates(personName, startDate, endDate);
				} else {
					System.out.println("Date(s) for " + personName + " already marked as unavailable.");
				}

				result.close();
				prepStmt.close();
				break;

			} catch (CommunicationsException e) {
				if (i == 0) {
					// First attempt to connect
					System.out.println(Utilities.getCurrTime() + " - Attempting to re-connect to database...");
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

	private void removeUnavailDates(String personName, String startDate, String endDate) {
		if (!checkDatabaseConnection())
			return;

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement deletePersonStmt = dbConnection.prepareStatement("DELETE FROM UnavailDates "
						+ "WHERE PersonID=(SELECT PersonID FROM Persons WHERE PersonName=?) "
						+ "AND StartDate=? AND EndDate=?;");

				// Delete assigned task
				deletePersonStmt.setString(1, personName);
				deletePersonStmt.setString(2, startDate);
				deletePersonStmt.setString(3, endDate);
				deletePersonStmt.executeUpdate();
				deletePersonStmt.close();
				break;

			} catch (CommunicationsException e) {
				if (i == 0) {
					// First attempt to connect
					System.out.println(Utilities.getCurrTime() + " - Attempting to re-connect to database...");
					connectDatabase();
				} else
					// Second try
					System.out.println("Unable to connect to database: " + e.getMessage());

			} catch (SQLException e) {
				System.out.println("Failure deleting " + personName + ": " + e.getMessage());
				break;
			}
		}
	}

	public ArrayList<DateRangeModel> getUnavailDates(String personName) {
		ArrayList<DateRangeModel> dateList = new ArrayList<>();

		if (!checkDatabaseConnection())
			return dateList;

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement selectStmt = dbConnection.prepareStatement(
						"SELECT Persons.PersonID AS PersonID, StartDate, EndDate " + "FROM UnavailDates, Persons "
								+ "WHERE Persons.PersonName = ? AND Persons.PersonID = UnavailDates.PersonID "
								+ "ORDER BY StartDate, EndDate;");
				selectStmt.setString(1, personName);

				ResultSet result = selectStmt.executeQuery();
				while (result.next()) {
					dateList.add(new DateRangeModel(result.getInt("PersonID"), result.getDate("StartDate").toString(),
							result.getDate("EndDate").toString()));
				}
				result.close();
				selectStmt.close();
				break;

			} catch (CommunicationsException e) {
				if (i == 0) {
					// First attempt to connect
					System.out.println(Utilities.getCurrTime() + " - Attempting to re-connect to database...");
					connectDatabase();
				} else
					// Second try
					System.out.println("Unable to connect to database: " + e.getMessage());

			} catch (SQLException e) {
				System.out.println("Failure retreiving Unavail Dates list for " + personName + " from database: "
						+ e.getMessage());
				break;
			}
		}
		return dateList;
	}

	public ArrayList<DateRangeModel> getUnavailDates() {
		ArrayList<DateRangeModel> dateList = new ArrayList<>();

		if (!checkDatabaseConnection())
			return dateList;

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement selectStmt = dbConnection.prepareStatement(
						"SELECT PersonID, StartDate, EndDate FROM UnavailDates ORDER BY StartDate, EndDate;");

				ResultSet result = selectStmt.executeQuery();
				while (result.next()) {
					dateList.add(new DateRangeModel(result.getInt("PersonID"), result.getDate("StartDate").toString(),
							result.getDate("EndDate").toString()));
				}
				result.close();
				selectStmt.close();
				break;

			} catch (CommunicationsException e) {
				if (i == 0) {
					// First attempt to connect
					System.out.println(Utilities.getCurrTime() + " - Attempting to re-connect to database...");
					connectDatabase();
				} else
					// Second try
					System.out.println("Unable to connect to database: " + e.getMessage());

			} catch (SQLException e) {
				System.out.println("Failure retreiving Unavail Dates list from database: " + e.getMessage());
				break;
			}
		}
		return dateList;
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
					System.out.println(Utilities.getCurrTime() + " - Attempting to re-connect to database...");
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
				PreparedStatement prepStmt = dbConnection.prepareStatement("SELECT COUNT(*) FROM UnavailDates, Persons "
						+ "WHERE (? BETWEEN StartDate AND EndDate) AND PersonName=? "
						+ "AND UnavailDates.PersonID = Persons.PersonID;");
				prepStmt.setDate(1, java.sql.Date.valueOf(Utilities.getSqlDate(today)));
				prepStmt.setString(2, personName);
				ResultSet result = prepStmt.executeQuery();
				result.next();

				String displayDate = Utilities.getSqlDate(today);
				if (result.getInt(1) == 0) {
					// No match for start/end dates, so add date range
					addUnavailDates(personName, displayDate, displayDate);
				} else
					System.out.println(displayDate + " for " + personName + " already marked as unavailable.");

				result.close();
				prepStmt.close();
				break;

			} catch (CommunicationsException e) {
				if (i == 0) {
					// First attempt to connect
					System.out.println(Utilities.getCurrTime() + " - Attempting to re-connect to database...");
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

	public ArrayList<String> getAllPersonsAsString() {
		ArrayList<String> nameList = new ArrayList<String>();

		if (!checkDatabaseConnection())
			return nameList;

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement selectStmt = dbConnection
						.prepareStatement("SELECT PersonName FROM Persons ORDER BY PersonName;");

				ResultSet result = selectStmt.executeQuery();
				while (result.next()) {
					nameList.add(new String(result.getString("PersonName")));
				}
				result.close();
				selectStmt.close();
				break;

			} catch (CommunicationsException e) {
				if (i == 0) {
					// First attempt to connect
					System.out.println(Utilities.getCurrTime() + " - Attempting to re-connect to database...");
					connectDatabase();
				} else
					// Second try
					System.out.println("Unable to connect to database: " + e.getMessage());

			} catch (SQLException e) {
				System.out.println("Failure retreiving person list from database: " + e.getMessage());
				break;
			}
		}
		return nameList;
	}

	public ArrayList<PersonByTaskModel> getAllPersons() {
		ArrayList<PersonByTaskModel> personsByTask = new ArrayList<PersonByTaskModel>();

		if (!checkDatabaseConnection())
			return personsByTask;

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement selectStmt = dbConnection
						.prepareStatement("SELECT * FROM Persons ORDER BY PersonName;");

				ResultSet result = selectStmt.executeQuery();
				while (result.next()) {
					PersonModel p = new PersonModel(result.getInt("PersonID"), result.getString("PersonName"),
							result.getString("PhoneNumber"), result.getString("EMail"), result.getBoolean("isLeader"),
							result.getString("Notes"), null, null, null);
					personsByTask.add(new PersonByTaskModel(p, null, false, 0, null));
				}
				result.close();
				selectStmt.close();
				break;

			} catch (CommunicationsException e) {
				if (i == 0) {
					// First attempt to connect
					System.out.println(Utilities.getCurrTime() + " - Attempting to re-connect to database...");
					connectDatabase();
				} else
					// Second try
					System.out.println("Unable to connect to database: " + e.getMessage());

			} catch (SQLException e) {
				System.out.println("Failure retreiving person list from database: " + e.getMessage());
				break;
			}
		}
		return personsByTask;
	}

	public ArrayList<PersonByTaskModel> getAllPersonsWithNotes() {
		ArrayList<PersonByTaskModel> personsByTask = new ArrayList<PersonByTaskModel>();

		if (!checkDatabaseConnection())
			return personsByTask;

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement selectStmt = dbConnection.prepareStatement("SELECT * FROM Persons "
						+ "WHERE Persons.Notes IS NOT NULL AND Persons.Notes != '' ORDER BY PersonName;");

				ResultSet result = selectStmt.executeQuery();
				while (result.next()) {
					PersonModel p = new PersonModel(result.getInt("PersonID"), result.getString("PersonName"),
							result.getString("PhoneNumber"), result.getString("EMail"), result.getBoolean("isLeader"),
							result.getString("Notes"), null, null, null);
					personsByTask.add(new PersonByTaskModel(p, null, false, 0, null));
				}
				result.close();
				selectStmt.close();
				break;

			} catch (CommunicationsException e) {
				if (i == 0) {
					// First attempt to connect
					System.out.println(Utilities.getCurrTime() + " - Attempting to re-connect to database...");
					connectDatabase();
				} else
					// Second try
					System.out.println("Unable to connect to database: " + e.getMessage());

			} catch (SQLException e) {
				System.out.println("Failure retreiving person list from database: " + e.getMessage());
				break;
			}
		}
		return personsByTask;
	}

	public ArrayList<String> getAvailPersonsAsString(Calendar today) {
		// Get all persons who are available today
		ArrayList<String> nameList = new ArrayList<String>();
		java.sql.Date sqlToday = java.sql.Date.valueOf(Utilities.getSqlDate(today));

		if (!checkDatabaseConnection())
			return nameList;

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement selectStmt = dbConnection.prepareStatement(
						"SELECT PersonName, Persons.PersonID, StartDate, EndDate FROM Persons, UnavailDates "
								// Don't select if person has entries in unavail
								// list with matching date
								+ "WHERE (SELECT COUNT(*) FROM UnavailDates "
								+ "   WHERE (SELECT COUNT(*) FROM UnavailDates "
								+ "         WHERE Persons.PersonID = UnavailDates.PersonID) > 0 "
								+ "   AND Persons.PersonID = UnavailDates.PersonID "
								+ "   AND ? BETWEEN StartDate AND EndDate) = 0 "
								+ "GROUP BY PersonName ORDER BY PersonName;");

				selectStmt.setDate(1, sqlToday);
				ResultSet result = selectStmt.executeQuery();
				while (result.next())
					nameList.add(new String(result.getString("PersonName")));

				result.close();
				selectStmt.close();
				break;

			} catch (CommunicationsException e) {
				if (i == 0) {
					// First attempt to connect
					System.out.println(Utilities.getCurrTime() + " - Attempting to re-connect to database...");
					connectDatabase();
				} else
					// Second try
					System.out.println("Unable to connect to database: " + e.getMessage());

			} catch (SQLException e) {
				System.out.println("Failure retreiving person list from database: " + e.getMessage());
				break;
			}
		}
		return nameList;
	}

	public boolean checkPersonExists(String personName) {
		if (!checkDatabaseConnection())
			return false;

		boolean personExists = false;
		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement selectStmt = dbConnection
						.prepareStatement("SELECT COUNT(*) FROM Persons WHERE PersonName=?;");
				selectStmt.setString(1, personName);

				ResultSet result = selectStmt.executeQuery();
				result.next();
				if (result.getInt(1) > 0)
					personExists = true;

				result.close();
				selectStmt.close();
				break;

			} catch (CommunicationsException e) {
				if (i == 0) {
					// First attempt to connect
					System.out.println(Utilities.getCurrTime() + " - Attempting to re-connect to database...");
					connectDatabase();
				} else
					// Second try
					System.out.println("Unable to connect to database: " + e.getMessage());

			} catch (SQLException e) {
				System.out.println("Failure retreiving " + personName + " info from database: " + e.getMessage());
				break;
			}
		}
		return personExists;
	}

	public PersonModel getPersonByName(String personName) {
		if (!checkDatabaseConnection())
			return null;

		PersonModel person = null;
		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement selectStmt = dbConnection
						.prepareStatement("SELECT * FROM Persons WHERE PersonName=?;");
				selectStmt.setString(1, personName);

				ResultSet result = selectStmt.executeQuery();
				if (result.next()) {
					ArrayList<SingleInstanceTaskModel> singleTaskList = getSingleInstanceTasks(personName);
					ArrayList<AssignedTasksModel> assignedTaskList = getAssignedTasks(personName);
					ArrayList<DateRangeModel> dateList = getUnavailDates(personName);
					person = new PersonModel(result.getInt("PersonID"), result.getString("PersonName"),
							result.getString("PhoneNumber"), result.getString("EMail"), result.getBoolean("isLeader"),
							result.getString("Notes"), assignedTaskList, dateList, singleTaskList);
				}
				result.close();
				selectStmt.close();
				break;

			} catch (CommunicationsException e) {
				if (i == 0) {
					// First attempt to connect
					System.out.println(Utilities.getCurrTime() + " - Attempting to re-connect to database...");
					connectDatabase();
				} else
					// Second try
					System.out.println("Unable to connect to database: " + e.getMessage());

			} catch (SQLException e) {
				System.out.println("Failure retreiving " + personName + " from database: " + e.getMessage());
				break;
			}
		}
		return person;
	}

	public ArrayList<PersonByTaskModel> getPersonsByTask(TaskModel task) {
		ArrayList<PersonByTaskModel> thisTasksPersons = new ArrayList<PersonByTaskModel>();

		if (!checkDatabaseConnection())
			return thisTasksPersons;

		// Check if task is in person's assigned task list
		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement selectStmt = dbConnection
						.prepareStatement("SELECT * FROM Persons, AssignedTasks, Tasks WHERE Tasks.TaskID=?"
								+ "  AND AssignedTasks.TaskID = Tasks.TaskID "
								+ "  AND AssignedTasks.PersonID = Persons.PersonID " + "ORDER BY PersonName;");
				selectStmt.setInt(1, task.getTaskID());

				ResultSet result = selectStmt.executeQuery();
				while (result.next()) {
					// Getting person by task, so only need to put matching task
					// in assigned task list
					ArrayList<AssignedTasksModel> assignedTaskList = new ArrayList<AssignedTasksModel>();
					assignedTaskList.add(new AssignedTasksModel(result.getInt("AssignedTaskID"),
							result.getInt("AssignedTasks.PersonID"), result.getInt("AssignedTasks.TaskID"), "",
							result.getString("TaskName"),
							createDaysOfWeekArray(result.getInt("AssignedTasks.DaysOfWeek")),
							createDaysOfWeekArray(result.getInt("AssignedTasks.DowInMonth"))));

					PersonModel p = new PersonModel(result.getInt("Persons.PersonID"), result.getString("PersonName"),
							result.getString("PhoneNumber"), result.getString("EMail"), result.getBoolean("isLeader"),
							result.getString("Notes"), assignedTaskList, null, null);
					thisTasksPersons.add(new PersonByTaskModel(p, task, false, task.getColor(), null));
				}
				result.close();
				selectStmt.close();
				break;

			} catch (CommunicationsException e) {
				if (i == 0) {
					// First attempt to connect
					System.out.println(Utilities.getCurrTime() + " - Attempting to re-connect to database...");
					connectDatabase();
				} else
					// Second try
					System.out.println("Unable to connect to database: " + e.getMessage());

			} catch (SQLException e) {
				System.out.println("Failure retreiving person list from database: " + e.getMessage());
				break;
			}
		}
		return thisTasksPersons;
	}

	// Return list of all persons assigned to this day, including single
	// instance assignments (subs) and floaters
	public ArrayList<PersonByTaskModel> getPersonsByDay(Calendar calendar) {
		int dayOfWeekInMonthIdx = calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH) - 1;
		int dayOfWeekIdx = calendar.get(Calendar.DAY_OF_WEEK) - 1;
		Calendar localCalendar = (Calendar) calendar.clone();
		String sqlDate = Utilities.getSqlDate(localCalendar);

		PersonByTaskModel personByTask;
		ArrayList<PersonByTaskModel> thisDaysPersons = new ArrayList<PersonByTaskModel>();
		if (!checkDatabaseConnection())
			return (ArrayList<PersonByTaskModel>) thisDaysPersons;

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement selectStmt = dbConnection.prepareStatement(
						// Assigned tasks with matching DOW and WOM
						"SELECT PersonName, isLeader AS Leader, false AS SingleInstance, Tasks.TaskID AS TaskID, "
								+ "  TaskName, Hour AS Hour, Minute AS Minute, Location, PhoneNumber, EMail, "
								+ "Tasks.Color AS TaskColor, 0 AS SingleInstanceColor "
								+ "FROM Tasks, Persons, Programs, AssignedTasks "
								+ "WHERE (Tasks.ProgramID = Programs.ProgramID "
								// Check if program expired
								+ "  AND ((Programs.StartDate IS NULL) OR (? >= Programs.StartDate)) "
								+ "  AND ((Programs.EndDate IS NULL) OR (? <= Programs.EndDate))) "
								// Check if task is active today
								+ "  AND (Tasks.DaysOfWeek & (1 << ?)) != 0 "
								+ "  AND (Tasks.DowInMonth & (1 << ?)) != 0 "
								// Check if assigned task is active today
								+ "  AND Tasks.TaskID = AssignedTasks.TaskID "
								+ "  AND (AssignedTasks.DaysOfWeek & (1 << ?)) != 0 "
								+ "  AND (AssignedTasks.DowInMonth & (1 << ?)) != 0 "
								// Find associated person
								+ "  AND Persons.PersonID = AssignedTasks.PersonID "
								// Check if person available today
								+ "  AND (SELECT COUNT(*) FROM Persons, UnavailDates "
								+ "      WHERE ((SELECT COUNT(*) FROM Persons, UnavailDates "
								+ "         WHERE Persons.PersonID = UnavailDates.PersonID) > 0 "
								+ "      AND Persons.PersonID = AssignedTasks.PersonID "
								+ "      AND Persons.PersonID = UnavailDates.PersonID "
								+ "      AND ? BETWEEN UnavailDates.StartDate AND UnavailDates.EndDate)) = 0 "

								+ "UNION " +

								// Floaters + Subs from Single Instance Tasks
								"SELECT PersonName, isLeader AS Leader, true AS SingleInstance, SingleInstanceTasks.TaskID AS TaskID, "
								+ "TaskName, HOUR(SingleTime) AS Hour, MINUTE(SingleTime) AS Minute, Location, PhoneNumber, EMail, "
								+ "Tasks.Color AS TaskColor, SingleInstanceTasks.Color AS SingleInstanceColor "
								+ "FROM Tasks, Persons, Programs, SingleInstanceTasks "
								+ "WHERE (Tasks.ProgramID = Programs.ProgramID "
								// Check if program expired
								+ "  AND ((Programs.StartDate IS NULL) OR (? >= Programs.StartDate)) "
								+ "	 AND ((Programs.EndDate IS NULL) OR (? <= Programs.EndDate))) "
								// Check if task assigned to today
								+ "	 AND (SingleInstanceTasks.TaskID = Tasks.TaskID OR SingleInstanceTasks.TaskID IS NULL) "
								+ "	 AND SingleDate=? "
								// Find associated person
								+ "	 AND SingleInstanceTasks.PersonID = Persons.PersonID "
								// Check if person available today
								+ "  AND (SELECT COUNT(*) FROM Persons, UnavailDates "
								+ "      WHERE ((SELECT COUNT(*) FROM Persons, UnavailDates "
								+ "         WHERE Persons.PersonID = UnavailDates.PersonID) > 0 "
								+ "      AND Persons.PersonID = SingleInstanceTasks.PersonID "
								+ "      AND Persons.PersonID = UnavailDates.PersonID "
								+ "      AND ? BETWEEN UnavailDates.StartDate AND UnavailDates.EndDate)) = 0 "

								+ "GROUP BY PersonName, TaskID " + "ORDER BY PersonName, TaskName;");

				int row = 1;
				selectStmt.setString(row++, sqlDate);
				selectStmt.setString(row++, sqlDate);
				selectStmt.setInt(row++, dayOfWeekIdx);
				selectStmt.setInt(row++, dayOfWeekInMonthIdx);
				selectStmt.setInt(row++, dayOfWeekIdx);
				selectStmt.setInt(row++, dayOfWeekInMonthIdx);
				selectStmt.setString(row++, sqlDate);
				selectStmt.setString(row++, sqlDate);
				selectStmt.setString(row++, sqlDate);
				selectStmt.setString(row++, sqlDate);
				selectStmt.setString(row, sqlDate);

				ResultSet result = selectStmt.executeQuery();
				while (result.next()) {
					Utilities.addTimeToCalendar(localCalendar,
							new TimeModel(result.getInt("Hour"), result.getInt("Minute")));

					PersonModel pModel = new PersonModel(0, result.getString("PersonName"),
							result.getString("PhoneNumber"), result.getString("EMail"),
							result.getInt("Leader") == 1 ? true : false, "", null, null, null);

					if (result.getInt("TaskID") == 0) {
						// Floater
						personByTask = new PersonByTaskModel(pModel, null, false, result.getInt("SingleInstanceColor"),
								localCalendar);
					} else {
						personByTask = new PersonByTaskModel(pModel,
								new TaskModel(result.getInt("TaskID"), 0, result.getString("TaskName"),
										result.getString("Location"), 0, 0, null, null,
										new TimeModel(result.getInt("Hour"), result.getInt("Minute")),
										result.getInt("TaskColor")),
								result.getBoolean("SingleInstance"), result.getInt("TaskColor"), localCalendar);
					}
					thisDaysPersons.add(personByTask);
				}
				result.close();
				selectStmt.close();
				break;

			} catch (CommunicationsException e) {
				if (i == 0) {
					// First attempt to connect
					System.out.println(Utilities.getCurrTime() + " - Attempting to re-connect to database...");
					connectDatabase();
				} else
					// Second try
					System.out.println("Unable to connect to database: " + e.getMessage());

			} catch (SQLException e) {
				System.out.println("Failure retreiving person list from database: " + e.getMessage());
				break;
			}
		}
		return (ArrayList<PersonByTaskModel>) thisDaysPersons;
	}

	public ArrayList<PersonByTaskModel> getPersonsByDayByTime(Calendar calendar) {
		int dayOfWeekInMonthIdx = calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH) - 1;
		int dayOfWeekIdx = calendar.get(Calendar.DAY_OF_WEEK) - 1;
		Calendar localCalendar = (Calendar) calendar.clone();
		String sqlDate = Utilities.getSqlDate(localCalendar);
		int hour = localCalendar.get(Calendar.HOUR);
		int minute = localCalendar.get(Calendar.MINUTE);
		if (localCalendar.get(Calendar.AM_PM) == Calendar.PM)
			hour += 12;

		PersonByTaskModel personByTask;
		ArrayList<PersonByTaskModel> persons = new ArrayList<PersonByTaskModel>();
		if (!checkDatabaseConnection())
			return (ArrayList<PersonByTaskModel>) persons;

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement selectStmt = dbConnection.prepareStatement(
						// Assigned tasks with matching DOW and WOM
						"(SELECT PersonName, isLeader AS Leader, false AS SingleInstance, Tasks.TaskID AS TaskID, "
								+ "  TaskName, Hour AS Hour, Minute AS Minute, Location, PhoneNumber, EMail, "
								+ "Tasks.Color AS TaskColor, 0 AS SingleInstanceColor, 0 AS SingleInstanceID "
								+ "FROM Tasks, Persons, Programs, AssignedTasks "

								+ "WHERE (Tasks.ProgramID = Programs.ProgramID "
								// Check if program expired
								+ "  AND ((Programs.StartDate IS NULL) OR (? >= Programs.StartDate)) "
								+ "  AND ((Programs.EndDate IS NULL) OR (? <= Programs.EndDate))) "
								// Check if task is active today
								+ "  AND (Tasks.DaysOfWeek & (1 << ?)) != 0 "
								+ "  AND (Tasks.DowInMonth & (1 << ?)) != 0 "
								// Check if assigned task is active today
								+ "  AND Tasks.TaskID = AssignedTasks.TaskID "
								+ "  AND (AssignedTasks.DaysOfWeek & (1 << ?)) != 0 "
								+ "  AND (AssignedTasks.DowInMonth & (1 << ?)) != 0 "
								// Find associated person
								+ "  AND Persons.PersonID = AssignedTasks.PersonID "
								// Check if person available today
								+ "  AND (SELECT COUNT(*) FROM Persons, UnavailDates "
								+ "      WHERE ((SELECT COUNT(*) FROM Persons, UnavailDates "
								+ "         WHERE Persons.PersonID = UnavailDates.PersonID) > 0 "
								+ "      AND Persons.PersonID = AssignedTasks.PersonID "
								+ "      AND Persons.PersonID = UnavailDates.PersonID "
								+ "      AND ? BETWEEN UnavailDates.StartDate AND UnavailDates.EndDate)) = 0 "
								// Check for time match
								+ "  AND Tasks.Hour = ? AND Tasks.Minute = ?) "

								+ "UNION " +

								// Floaters + Subs from Single Instance Tasks
								"(SELECT PersonName, isLeader AS Leader, true AS SingleInstance, SingleInstanceTasks.TaskID AS TaskID, "
								+ "TaskName, HOUR(SingleTime) AS Hour, MINUTE(SingleTime) AS Minute, Location, PhoneNumber, EMail, "
								+ "Tasks.Color AS TaskColor, SingleInstanceTasks.Color AS SingleInstanceColor, SingleInstanceID "

								+ "FROM Tasks, Persons, Programs, SingleInstanceTasks "

								+ "WHERE (SingleInstanceTasks.ProgramID = Programs.ProgramID "
								// Check if program expired
								+ "  AND ((Programs.StartDate IS NULL) OR (? >= Programs.StartDate)) "
								+ "	 AND ((Programs.EndDate IS NULL) OR (? <= Programs.EndDate))) "
								// Check if task assigned to today
								+ "	 AND (SingleInstanceTasks.TaskID = Tasks.TaskID OR SingleInstanceTasks.TaskID IS NULL) "
								+ "	 AND SingleDate=? "
								// Find associated person
								+ "	 AND SingleInstanceTasks.PersonID = Persons.PersonID "
								// Check if person available today
								+ "  AND (SELECT COUNT(*) FROM Persons, UnavailDates "
								+ "      WHERE ((SELECT COUNT(*) FROM Persons, UnavailDates "
								+ "         WHERE Persons.PersonID = UnavailDates.PersonID) > 0 "
								+ "      AND Persons.PersonID = SingleInstanceTasks.PersonID "
								+ "      AND Persons.PersonID = UnavailDates.PersonID "
								+ "      AND ? BETWEEN UnavailDates.StartDate AND UnavailDates.EndDate)) = 0 "
								// Check for time match
								+ "  AND HOUR(SingleTime)=? AND MINUTE(SingleTime)=? " + "GROUP BY SingleInstanceID) "

								+ "ORDER BY PersonName, TaskName;");

				int row = 1;
				selectStmt.setString(row++, sqlDate);
				selectStmt.setString(row++, sqlDate);
				selectStmt.setInt(row++, dayOfWeekIdx);
				selectStmt.setInt(row++, dayOfWeekInMonthIdx);
				selectStmt.setInt(row++, dayOfWeekIdx);
				selectStmt.setInt(row++, dayOfWeekInMonthIdx);
				selectStmt.setString(row++, sqlDate);
				selectStmt.setInt(row++, hour);
				selectStmt.setInt(row++, minute);
				selectStmt.setString(row++, sqlDate);
				selectStmt.setString(row++, sqlDate);
				selectStmt.setString(row++, sqlDate);
				selectStmt.setString(row++, sqlDate);
				selectStmt.setInt(row++, hour);
				selectStmt.setInt(row, minute);

				ResultSet result = selectStmt.executeQuery();
				while (result.next()) {
					Utilities.addTimeToCalendar(localCalendar,
							new TimeModel(result.getInt("Hour"), result.getInt("Minute")));

					PersonModel pModel = new PersonModel(0, result.getString("PersonName"),
							result.getString("PhoneNumber"), result.getString("EMail"),
							result.getInt("Leader") == 1 ? true : false, "", null, null, null);

					if (result.getInt("TaskID") == 0) {
						// Floater
						personByTask = new PersonByTaskModel(pModel, null, false, result.getInt("SingleInstanceColor"),
								localCalendar);
					} else {
						// Task or substitute
						personByTask = new PersonByTaskModel(pModel,
								new TaskModel(result.getInt("TaskID"), 0, result.getString("TaskName"),
										result.getString("Location"), 0, 0, null, null,
										new TimeModel(result.getInt("Hour"), result.getInt("Minute")),
										result.getInt("TaskColor")),
								result.getBoolean("SingleInstance"), result.getInt("TaskColor"), localCalendar);
					}
					persons.add(personByTask);
				}
				result.close();
				selectStmt.close();
				break;

			} catch (CommunicationsException e) {
				if (i == 0) {
					// First attempt to connect
					System.out.println(Utilities.getCurrTime() + " - Attempting to re-connect to database...");
					connectDatabase();
				} else
					// Second try
					System.out.println("Unable to connect to database: " + e.getMessage());

			} catch (SQLException e) {
				System.out.println("Failure retreiving person list from database: " + e.getMessage());
				break;
			}
		}
		return (ArrayList<PersonByTaskModel>) persons;
	}

	public ArrayList<PersonByTaskModel> getPersonsByDayByLocation(Calendar calendar, String location) {
		int dayOfWeekInMonthIdx = calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH) - 1;
		int dayOfWeekIdx = calendar.get(Calendar.DAY_OF_WEEK) - 1;
		Calendar localCalendar = (Calendar) calendar.clone();
		String sqlDate = Utilities.getSqlDate(localCalendar);

		PersonByTaskModel personByLocation;
		ArrayList<PersonByTaskModel> persons = new ArrayList<PersonByTaskModel>();

		if (!checkDatabaseConnection())
			return (ArrayList<PersonByTaskModel>) persons;

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement selectStmt = dbConnection.prepareStatement(
						// Assigned tasks with matching DOW and WOM
						"SELECT PersonName, isLeader AS Leader, false AS SingleInstance, Tasks.TaskID AS TaskID, "
								+ "  TaskName, Hour AS Hour, Minute AS Minute, Location, PhoneNumber, EMail, "
								+ "Tasks.Color AS TaskColor, 0 AS SingleInstanceColor "
								+ "FROM Tasks, Persons, Programs, AssignedTasks "

								+ "WHERE (Tasks.ProgramID = Programs.ProgramID "
								// Check if program expired
								+ "  AND ((Programs.StartDate IS NULL) OR (? >= Programs.StartDate)) "
								+ "  AND ((Programs.EndDate IS NULL) OR (? <= Programs.EndDate))) "
								// Check if task is active today
								+ "  AND (Tasks.DaysOfWeek & (1 << ?)) != 0 "
								+ "  AND (Tasks.DowInMonth & (1 << ?)) != 0 "
								// Check if assigned task is active today
								+ "  AND Tasks.TaskID = AssignedTasks.TaskID "
								+ "  AND (AssignedTasks.DaysOfWeek & (1 << ?)) != 0 "
								+ "  AND (AssignedTasks.DowInMonth & (1 << ?)) != 0 "
								// Find associated person
								+ "  AND Persons.PersonID = AssignedTasks.PersonID "
								// Check if person available today
								+ "  AND (SELECT COUNT(*) FROM Persons, UnavailDates "
								+ "      WHERE ((SELECT COUNT(*) FROM Persons, UnavailDates "
								+ "         WHERE Persons.PersonID = UnavailDates.PersonID) > 0 "
								+ "      AND Persons.PersonID = AssignedTasks.PersonID "
								+ "      AND Persons.PersonID = UnavailDates.PersonID "
								+ "      AND ? BETWEEN UnavailDates.StartDate AND UnavailDates.EndDate)) = 0 "
								// Check for location match
								+ "  AND Tasks.Location = ? "

								+ "UNION " +

								// Floaters + Subs from Single Instance Tasks
								"SELECT PersonName, isLeader AS Leader, true AS SingleInstance, SingleInstanceTasks.TaskID AS TaskID, "
								+ "TaskName, HOUR(SingleTime) AS Hour, MINUTE(SingleTime) AS Minute, Location, PhoneNumber, EMail, "
								+ "Tasks.Color AS TaskColor, SingleInstanceTasks.Color AS SingleInstanceColor "
								+ "FROM Tasks, Persons, Programs, SingleInstanceTasks "

								+ "WHERE (Tasks.ProgramID = Programs.ProgramID "
								// Check if program expired
								+ "  AND ((Programs.StartDate IS NULL) OR (? >= Programs.StartDate)) "
								+ "	 AND ((Programs.EndDate IS NULL) OR (? <= Programs.EndDate))) "
								// Check if task assigned to today
								+ "	 AND (SingleInstanceTasks.TaskID = Tasks.TaskID OR SingleInstanceTasks.TaskID IS NULL) "
								+ "	 AND SingleDate=? "
								// Find associated person
								+ "	 AND SingleInstanceTasks.PersonID = Persons.PersonID "
								// Check if person available today
								+ "  AND (SELECT COUNT(*) FROM Persons, UnavailDates "
								+ "      WHERE ((SELECT COUNT(*) FROM Persons, UnavailDates "
								+ "         WHERE Persons.PersonID = UnavailDates.PersonID) > 0 "
								+ "      AND Persons.PersonID = SingleInstanceTasks.PersonID "
								+ "      AND Persons.PersonID = UnavailDates.PersonID "
								+ "      AND ? BETWEEN UnavailDates.StartDate AND UnavailDates.EndDate)) = 0 "
								// Check for time match
								+ "  AND Tasks.Location = ? "

								+ "GROUP BY Location ORDER BY PersonName, TaskName;");

				int row = 1;
				selectStmt.setString(row++, sqlDate);
				selectStmt.setString(row++, sqlDate);
				selectStmt.setInt(row++, dayOfWeekIdx);
				selectStmt.setInt(row++, dayOfWeekInMonthIdx);
				selectStmt.setInt(row++, dayOfWeekIdx);
				selectStmt.setInt(row++, dayOfWeekInMonthIdx);
				selectStmt.setString(row++, sqlDate);
				selectStmt.setString(row++, location);
				selectStmt.setString(row++, sqlDate);
				selectStmt.setString(row++, sqlDate);
				selectStmt.setString(row++, sqlDate);
				selectStmt.setString(row++, sqlDate);
				selectStmt.setString(row++, location);

				ResultSet result = selectStmt.executeQuery();
				while (result.next()) {
					Utilities.addTimeToCalendar(localCalendar,
							new TimeModel(result.getInt("Hour"), result.getInt("Minute")));

					PersonModel pModel = new PersonModel(0, result.getString("PersonName"),
							result.getString("PhoneNumber"), result.getString("EMail"),
							result.getInt("Leader") == 1 ? true : false, "", null, null, null);

					// Task or substitute; floaters don't have location
					personByLocation = new PersonByTaskModel(pModel, new TaskModel(result.getInt("TaskID"), 0,
							result.getString("TaskName"), result.getString("Location"), 0, 0, null, null,
							new TimeModel(result.getInt("Hour"), result.getInt("Minute")), result.getInt("TaskColor")),
							result.getBoolean("SingleInstance"), result.getInt("TaskColor"), localCalendar);
					persons.add(personByLocation);
				}
				result.close();
				selectStmt.close();
				break;

			} catch (CommunicationsException e) {
				if (i == 0) {
					// First attempt to connect
					System.out.println(Utilities.getCurrTime() + " - Attempting to re-connect to database...");
					connectDatabase();
				} else
					// Second try
					System.out.println("Unable to connect to database: " + e.getMessage());

			} catch (SQLException e) {
				System.out.println("Failure retreiving person list from database: " + e.getMessage());
				break;
			}
		}
		return (ArrayList<PersonByTaskModel>) persons;
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
					System.out.println(Utilities.getCurrTime() + " - Attempting to re-connect to database...");
					connectDatabase();
				} else // Second try
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
		for (int i = 0; i < 7; i++) {
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
	public void saveProgramToFile(ArrayList<String> programNameList, File file) throws IOException {
		JOptionPane.showMessageDialog(null, "Currently not supported");
	}

	public void loadProgramFromFile(File file) throws IOException {
		JOptionPane.showMessageDialog(null, "Currently not supported");
	}

	public void loadProgramFromDatabase() {
		JOptionPane.showMessageDialog(null, "Currently not supported");
	}

	public void loadRosterFromDatabase() {
		JOptionPane.showMessageDialog(null, "Currently not supported");
	}

	public void saveRosterToFile(File file) throws IOException {
		JOptionPane.showMessageDialog(null, "Currently not supported");
	}

	public void loadRosterFromFile(File file) throws IOException {
		JOptionPane.showMessageDialog(null, "Currently not supported");
	}
}
