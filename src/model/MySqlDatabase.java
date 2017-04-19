package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.swing.DefaultListModel;
import javax.swing.JList;

import com.mysql.jdbc.exceptions.jdbc4.CommunicationsException;

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
			String url = "jdbc:mysql://www.programplanner.org:3306/ProgramPlanner";
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
					System.out.println("Attempting to re-connect to database...");
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

		// TODO: Remove programName parameter, fill in missing fields
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

	//
	// TODO:
	// public ArrayList<CalendarDayModel> getTasksByDayByProgram(Calendar
	// calendar, JList<String> programFilterList)
	// {
	// ArrayList<CalendarDayModel>thisDaysTasks = getAllTasksByDay(calendar);
	//
	// TODO: Possibly create a new procedure?
	// for (int taskIdx = 0; taskIdx < thisDaysTasks.size(); taskIdx++) {
	// String programName =
	// findProgramByTaskName(thisDaysTasks.get(taskIdx).getTask().getTaskName());
	// if (!Utilities.findStringMatchInJList(programName, programFilterList)) {
	// thisDaysTasks.remove(taskIdx); taskIdx--;
	// }
	// }
	// return thisDaysTasks;
	// }
	//
	// public ArrayList<CalendarDayModel> getTasksByDayByPerson(Calendar
	// calendar, JList<String> persons) {
	// int dayOfWeekInMonthIdx = calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH) -
	// 1;
	// int dayOfWeekIdx = calendar.get(Calendar.DAY_OF_WEEK) - 1;
	// Date thisDay = Utilities.getDateFromCalendar(calendar);
	// ArrayList<CalendarDayModel> thisDaysTasks = getAllTasksByDay(calendar);
	// boolean match;
	//
	// TODO: Possibly create a new procedure?
	// for (int taskIdx = 0; taskIdx < thisDaysTasks.size(); taskIdx++) {
	// match = false;
	// int thisDaysTaskID = thisDaysTasks.get(taskIdx).getTask().getTaskID();
	// for (int i = 0; i < persons.getModel().getSize(); i++) {
	// PersonModel pModel = getPersonByName(persons.getModel().getElementAt(i));
	// // -1 = no match, 0 = assigned task, 1 = single instance task
	// if (checkPersonMatchForTaskByDay(pModel, thisDaysTaskID, thisDay,
	// dayOfWeekIdx, dayOfWeekInMonthIdx) >= 0) {
	// match = true; break;
	// }
	// }
	// if (!match) {
	// thisDaysTasks.remove(taskIdx);
	// taskIdx--;
	// }
	// } return thisDaysTasks;
	// }

	// public ArrayList<CalendarDayModel>
	// getTasksByDayByIncompleteRoster(Calendar calendar) {
	// ArrayList<CalendarDayModel> thisDaysTasks = getAllTasksByDay(calendar);
	//
	// TODO: Possibly create a new procedure?
	// for (int taskIdx = 0; taskIdx < thisDaysTasks.size(); taskIdx++) {
	// if ((thisDaysTasks.get(taskIdx).getPersonCount() >=
	// thisDaysTasks.get(taskIdx).getTask().getTotalPersonsReqd()) &&
	// (thisDaysTasks.get(taskIdx).getLeaderCount() >=
	// thisDaysTasks.get(taskIdx).getTask().getNumLeadersReqd())) {
	// thisDaysTasks.remove(taskIdx); taskIdx--;
	// }
	// }
	// return thisDaysTasks;
	// }

	// public ArrayList<CalendarDayModel> getTasksByDayByLocation(Calendar
	// calendar, JList<String> locations) {
	// ArrayList<CalendarDayModel> matchingTasks = getAllTasksByDay(calendar);
	// // TODO: Possibly create a new procedure?
	// for (int taskIdx = 0; taskIdx < matchingTasks.size(); taskIdx++) {
	// String taskLoc = matchingTasks.get(taskIdx).getTask().getLocation();
	// if (!Utilities.findStringMatchInJList(taskLoc, locations)) {
	// matchingTasks.remove(taskIdx); taskIdx--;
	// }
	// } return matchingTasks;
	// }
	//
	// public ArrayList<CalendarDayModel> getTasksByDayByTime(Calendar calendar,
	// JList<String> timeList) {
	// ArrayList<CalendarDayModel> matchingTasks = getAllTasksByDay(calendar);
	// Collections.sort(matchingTasks);
	//
	// // TODO: Possibly create a new procedure?
	// for (int taskIdx = 0; taskIdx < matchingTasks.size(); taskIdx++) {
	// String taskTime =
	// matchingTasks.get(taskIdx).getTask().getTime().toString();
	// if (!Utilities.findStringMatchInJList(taskTime, timeList)) {
	// matchingTasks.remove(taskIdx); taskIdx--;
	// }
	// }
	// return matchingTasks;
	// }
	//

	//
	// public ArrayList<CalendarDayModel> getAllTasksAndFloatersByDay(Calendar
	// calendar) {
	// int dayOfWeekInMonthIdx = calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH) -
	// 1;
	// int dayOfWeekIdx = calendar.get(Calendar.DAY_OF_WEEK) - 1;
	// Date thisDay = Utilities.getDateFromCalendar(calendar);
	//
	// TODO: Replaced with getAllTasksAndFloatersByMonth (?)
	//
	// Get all tasks for today
	// ArrayList<CalendarDayModel> thisDaysTasks = getAllTasksByDay(calendar);

	// Now add floaters to the list
	// for (int i = 0; i < personList.size(); i++) {
	// PersonModel person = personList.get(i);
	//
	// Check if person is a floater (not associated with task).
	// for (int j = 0; j < person.getSingleInstanceTasks().size(); j++) {
	// SingleInstanceTaskModel task = person.getSingleInstanceTasks().get(j);
	// if (checkFloaterMatch(task, thisDay, dayOfWeekIdx, dayOfWeekInMonthIdx)
	// >= 0) {
	// thisDaysTasks.add(new CalendarDayModel(null, 0, 0, task.getColor(),
	// task.getTaskDate(), "Floater"));
	// }
	// }
	// }
	//
	// Merge duplicate floaters
	// for (int i = 0; i < thisDaysTasks.size(); i++) {
	// CalendarDayModel calDay = thisDaysTasks.get(i);
	// if (calDay.getTask() == null) {
	// Found floater
	// Calendar taskTime = calDay.getFloaterTime();
	// int floaterCount = 0;
	// int firstFloaterIndex = 0;
	//
	// Find floaters with matching time
	// for (int taskIdx = 0; taskIdx < thisDaysTasks.size(); taskIdx++) {
	// if (thisDaysTasks.get(taskIdx).getFloaterTime() == null) { // Not a
	// floater
	// continue;
	// }
	//
	// if (Utilities.checkForTimeMatch(taskTime,
	// thisDaysTasks.get(taskIdx).getFloaterTime())) {
	// if (floaterCount == 0) {
	// First match, keep in list
	// firstFloaterIndex = taskIdx;
	// } else {
	// // Multiple matches, remove from list
	// thisDaysTasks.remove(taskIdx);
	// taskIdx--;
	// }
	// floaterCount++;
	// }
	// }
	//
	// Update floater name if more than 1 match
	// if (floaterCount > 1)
	// thisDaysTasks.get(firstFloaterIndex).setFloaterTaskName(floaterCount + "
	// Floaters");
	// }
	// } Collections.sort(thisDaysTasks); return thisDaysTasks;
	// }

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

				boolean[] dayOfWeek = { false, true, true, true, true, true, false };
				boolean[] weekOfMonth = { true, true, true, true, true, true };
				while (results.next()) {
					day = results.getInt("Today");
					taskName = results.getString("TaskName");
					personCount = results.getInt("PersonCount");
					Calendar cal = Calendar.getInstance();
					Utilities.addTimeToCalendar(calendar,
							new TimeModel(results.getInt("TaskHour"), results.getInt("TaskMinute")));

					// TODO: Add color field, figure out TaskModel, don't
					// hard-code columns
					if (taskName == null) {
						// Floater
						if (personCount == 1)
							calendarList.get(day - 1).add(new CalendarDayModel(null, personCount,
									results.getInt("LeaderCount"), results.getInt("TaskColor"), cal, "Floater"));
						else
							calendarList.get(day - 1)
									.add(new CalendarDayModel(null, personCount, results.getInt("LeaderCount"),
											results.getInt("TaskColor"), cal, personCount + " Floaters"));
					} else {
						TaskModel newTask = new TaskModel(results.getInt("TaskID"), results.getInt("ProgramID"),
								taskName, "" /* location */, results.getInt("NumLdrsReqd"),
								results.getInt("NumPersonsReqd"), dayOfWeek, weekOfMonth,
								new TimeModel(results.getInt("TaskHour"), results.getInt("TaskMinute")),
								results.getInt("TaskColor"));
						calendarList.get(day - 1).add(new CalendarDayModel(newTask, personCount,
								results.getInt("LeaderCount"), results.getInt("TaskColor"), null, ""));
					}
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

	// TODO:
	public JList<TimeModel> getAllTimesByDay(Calendar calendar) {
		int dayOfWeekInMonthIdx = calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH) - 1;
		int dayOfWeekIdx = calendar.get(Calendar.DAY_OF_WEEK) - 1;
		Calendar localCalendar = (Calendar) calendar.clone();
		String sqlDate = Utilities.getSqlDate(localCalendar);

		DefaultListModel<TimeModel> timeModel = new DefaultListModel<TimeModel>();

		if (!checkDatabaseConnection())
			return new JList<TimeModel>(timeModel);

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement selectStmt = dbConnection.prepareStatement(
						// Select tasks with matching DOW and WOM
						"SELECT Hour, Minute " + "FROM Tasks, Programs "
								+ "WHERE (Tasks.ProgramID = Programs.ProgramID "
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
					timeModel.addElement(new TimeModel(result.getInt("Hour"), result.getInt("Minute")));
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
				System.out.println("Failure retreiving time list from database: " + e.getMessage());
				break;
			}
		}
		return new JList<TimeModel>(timeModel);
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
			// TODO: Also add floater color
			SingleInstanceTaskModel task = extraTasks.get(i);
			addSingleInstanceTask(name, task.getTaskID(), task.getTaskDate());
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

	private void mergeAssignedTask(String personName, int taskID, boolean[] daysOfWeek, boolean[] weeksOfMonth) {
		if (!checkDatabaseConnection())
			return;

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement prepStmt = dbConnection
						.prepareStatement("SELECT COUNT(*) AS Count, AssignedTaskID, Persons.PersonID AS PersonID "
								+ "FROM AssignedTasks, Persons "
								+ "WHERE TaskID=? AND Persons.PersonName=? AND AssignedTasks.PersonID = Persons.PersonID;");
				prepStmt.setInt(1, taskID);
				prepStmt.setString(2, personName);
				ResultSet result = prepStmt.executeQuery();

				// TODO: Optimize this later by avoiding duplicating overhead
				if (result.getInt("Count") > 0) {
					// Assigned task exists, so update fields
					updateAssignedTask(result.getInt("AssignedTaskID"), daysOfWeek, weeksOfMonth);
				} else {
					// Assigned task not already in list, so insert
					addAssignedTask(result.getInt("PersonID"), taskID, daysOfWeek, weeksOfMonth);
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

				System.out.println("Failure adding tasks for " + personName + ": " + e.getMessage());
				break;
			}
		}
	}

	public ArrayList<AssignedTasksModel> getAssignedTasks(String personName) {
		ArrayList<AssignedTasksModel> taskList = new ArrayList<>();

		if (!checkDatabaseConnection())
			return taskList;

		// TODO: Add missing fields
		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement selectStmt = dbConnection.prepareStatement("SELECT ProgramName, TaskName, "
						+ "  AssignedTasks.DaysOfWeek AS DaysOfWeek, AssignedTasks.DowInMonth AS DowInMonth "
						+ "FROM AssignedTasks, Persons, Tasks, Programs WHERE Persons.PersonName = ? "
						+ "  AND Persons.PersonID = AssignedTasks.PersonID "
						+ "  AND Tasks.TaskID = AssignedTasks.TaskID " + "  AND Tasks.ProgramID = Programs.ProgramID "
						+ "ORDER BY ProgramName, TaskName;");
				selectStmt.setString(1, personName);

				ResultSet result = selectStmt.executeQuery();
				while (result.next()) {
					taskList.add(new AssignedTasksModel(0, 0, 0, result.getString("ProgramName"),
							result.getString("TaskName"), createDaysOfWeekArray(result.getInt("DaysOfWeek")),
							createDowInMonthArray(result.getInt("DowInMonth"))));
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
				System.out.println(
						"Failure retreiving task list for " + personName + " from database: " + e.getMessage());
				break;
			}
		}
		return taskList;
	}

	public ArrayList<DateRangeModel> getUnavailDates(String personName) {
		ArrayList<DateRangeModel> dateList = new ArrayList<>();

		if (!checkDatabaseConnection())
			return dateList;

		// TODO: Add missing fields
		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement selectStmt = dbConnection.prepareStatement(
						"SELECT UnavailDates.UnavailDatesID AS UnavailID, Persons.PersonID AS PersonID, StartDate, EndDate "
								+ "FROM UnavailDates, Persons "
								+ "WHERE Persons.PersonName = ? AND Persons.PersonID = UnavailDates.PersonID "
								+ "ORDER BY StartDate, EndDate;");
				selectStmt.setString(1, personName);

				ResultSet result = selectStmt.executeQuery();
				while (result.next()) {
					dateList.add(new DateRangeModel(result.getInt("UnavailID"), result.getInt("PersonID"),
							result.getDate("StartDate").toString(), result.getDate("EndDate").toString()));
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
				System.out.println("Failure retreiving Unavail Dates list for " + personName + " from database: "
						+ e.getMessage());
				break;
			}
		}
		return dateList;
	}

	public void addSingleInstanceTask(String personName, int taskID, Calendar taskTime) {
		if (!checkDatabaseConnection())
			return;

		for (int i = 0; i < 2; i++) {
			int col = 1;
			PreparedStatement addSingleTaskStmt = null;
			try {
				if (taskID == 0) {
					addSingleTaskStmt = dbConnection
							.prepareStatement("INSERT INTO SingleInstanceTasks (PersonID, SingleDate, SingleTime) "
									+ "VALUES ((SELECT PersonID FROM Persons WHERE PersonName=?), ?, ?);");
				} else {
					addSingleTaskStmt = dbConnection.prepareStatement(
							"INSERT INTO SingleInstanceTasks (PersonID, TaskId, SingleDate, SingleTime) "
									+ "VALUES ((SELECT PersonID FROM Persons WHERE PersonName=?), ?, ?, ?);");
				}

				// Add new Single Instance Task
				addSingleTaskStmt.setString(col++, personName);
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

	public void addSingleInstanceTask(String personName, Calendar singleDate, TaskModel task, int color) {
		if (!checkDatabaseConnection())
			return;

		for (int i = 0; i < 2; i++) {
			int col = 1;
			PreparedStatement addSingleTaskStmt = null;
			try {
				if (task.getTaskID() == 0) {
					addSingleTaskStmt = dbConnection.prepareStatement(
							"INSERT INTO SingleInstanceTasks (PersonID, SingleDate, SingleTime, Color) VALUES "
									+ "((SELECT PersonID FROM Persons WHERE PersonName=?), ?, ?, ?);");
				} else {
					addSingleTaskStmt = dbConnection.prepareStatement(
							"INSERT INTO SingleInstanceTasks (PersonID, TaskId, SingleDate, SingleTime Color) VALUES "
									+ "((SELECT PersonID FROM Persons WHERE PersonName=?), ?, ?, ?, ?);");
				}

				// Add new Single Instance Task
				addSingleTaskStmt.setString(col++, personName);
				if (task.getTaskID() != 0)
					addSingleTaskStmt.setInt(col++, task.getTaskID());
				addSingleTaskStmt.setDate(col++, java.sql.Date.valueOf(Utilities.getSqlDate(singleDate)));
				addSingleTaskStmt.setTime(col++, java.sql.Time.valueOf(Utilities.getSqlTime(singleDate)));
				addSingleTaskStmt.setInt(col, color);

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

	private void addSingleInstanceTask_orig(String personName, Calendar taskTime, TaskModel task, int color) {
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
				addUnavailDatesStmt.setDate(2, java.sql.Date.valueOf(startDate));
				addUnavailDatesStmt.setDate(3, java.sql.Date.valueOf(endDate));

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

	private void updateUnavailDates(String personName, String startDate, String endDate) {
		if (!checkDatabaseConnection())
			return;

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement prepStmt = dbConnection.prepareStatement("SELECT COUNT(*) FROM UnavailDates, Persons "
						+ "WHERE StartDate=? AND EndDate=? AND PersonName=? "
						+ "   AND UnavailDates.PersonID = Persons.PersonID;");
				prepStmt.setDate(1, java.sql.Date.valueOf(startDate));
				prepStmt.setDate(2, java.sql.Date.valueOf(endDate));
				prepStmt.setString(3, personName);
				ResultSet result = prepStmt.executeQuery();
				result.next();

				// TODO: Check whether this is a superset of another date range
				if (result.getInt(1) == 0) {
					// No match for start/end dates, so add date range
					addUnavailDates(personName, startDate, endDate);
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
			String personNotes, ArrayList<AssignedTasksModel> personAssignedTasks,
			ArrayList<SingleInstanceTaskModel> extraTasks, ArrayList<DateRangeModel> personDatesUnavailable) {

		// TODO: Check if we have to check for person NAME changes???

		// Update person info
		updatePersonInfo(personName, personPhone, personEmail, personIsLeader, personNotes);

		// Merge in the assigned tasks (list ONLY contains changes!!)
		for (int i = 0; i < personAssignedTasks.size(); i++) {
			// Update Assigned Tasks database for this person
			AssignedTasksModel assignedTask = personAssignedTasks.get(i);
			mergeAssignedTask(personName, assignedTask.getTaskID(), assignedTask.getDaysOfWeek(),
					assignedTask.getWeeksOfMonth());
		}

		// Add extraTasks (list only contains additions!!)
		for (int i = 0; i < extraTasks.size(); i++) {
			// Add single instance task to database
			SingleInstanceTaskModel singleTask = extraTasks.get(i);
			addSingleInstanceTask(personName, singleTask.getTaskID(), singleTask.getTaskDate());
		}

		// Add dates unavailable (check for duplicates)
		for (int i = 0; i < personDatesUnavailable.size(); i++) {
			// Add unavailable dates if not a duplicate
			DateRangeModel date = personDatesUnavailable.get(i);
			updateUnavailDates(personName, date.getStartDate(), date.getEndDate());
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

		String displayDate = Utilities.getDisplayDate(today);
		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement prepStmt = dbConnection.prepareStatement("SELECT COUNT(*) FROM UnavailDates, Persons "
						+ "WHERE StartDate=? AND EndDate=? AND PersonName=? "
						+ "AND UnavailDates.PersonID = Persons.PersonID;");
				prepStmt.setDate(1, java.sql.Date.valueOf(Utilities.getSqlDate(today)));
				prepStmt.setDate(2, java.sql.Date.valueOf(Utilities.getSqlDate(today)));
				prepStmt.setString(3, personName);
				ResultSet result = prepStmt.executeQuery();
				result.next();

				// TODO: Check whether this is a superset of another date range
				if (result.getInt(1) == 0) {
					// No match for start/end dates, so add date range
					addUnavailDates(personName, displayDate, displayDate);
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

		// Get all persons who are available today
		DefaultListModel<String> nameModel = new DefaultListModel<String>();

		if (!checkDatabaseConnection())
			return new JList<String>(nameModel);

		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement selectStmt = dbConnection
						.prepareStatement("SELECT PersonName FROM Persons, UnavailDates "
								+ "WHERE Persons.PersonID = UnavailDates.PersonID "
								+ "AND ? NOT BETWEEN UnavailDates.StartDate AND UnavailDates.EndDate "
								+ "ORDER BY PersonName;");
				selectStmt.setDate(1, java.sql.Date.valueOf(Utilities.getSqlDate(today)));

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
					ArrayList<DateRangeModel> dateList = getUnavailDates(personName);
					person = new PersonModel(result.getInt("PersonID"), result.getString("PersonName"),
							result.getString("PhoneNumber"), result.getString("EMail"), result.getBoolean("isLeader"),
							result.getString("Notes"), null, dateList, null);
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
				System.out.println("Failure retreiving " + personName + " from database: " + e.getMessage());
				break;
			}
		}
		return person;
	}

	public ArrayList<PersonByTaskModel> getPersonsByTask(TaskModel task) {
		ArrayList<PersonByTaskModel> thisTasksPersons = new ArrayList<PersonByTaskModel>();

		// TODO: Check if task is in person's assigned task list
		// This method is used to get complete roster for a task

		return thisTasksPersons;
	}

	// Return list of all persons assigned to this day, including single
	// instance assignments (subs) and floaters
	public ArrayList<PersonByTaskModel> getPersonsByDay(Calendar calendar) {
		int dayOfWeekInMonthIdx = calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH) - 1;
		int dayOfWeekIdx = calendar.get(Calendar.DAY_OF_WEEK) - 1;
		Date thisDay = Utilities.getDateFromCalendar(calendar);
		Calendar localCalendar = (Calendar) calendar.clone();
		String sqlDate = Utilities.getSqlDate(localCalendar);

		PersonByTaskModel personByTask;
		ArrayList<PersonByTaskModel> thisDaysPersons = new ArrayList<PersonByTaskModel>();
		if (!checkDatabaseConnection())
			return (ArrayList<PersonByTaskModel>) thisDaysPersons;

		// TODO: add color, check for single instance task TIME
		for (int i = 0; i < 2; i++) {
			try {
				PreparedStatement selectStmt = dbConnection.prepareStatement(
						// Assigned tasks with matching DOW and WOM
						"SELECT PersonName, isLeader AS Leader, false AS SingleInstance, Tasks.TaskID AS TaskID, "
								+ "  TaskName, Hour AS Hour, Minute AS Minute, Location, PhoneNumber, EMail, "
								+ "Tasks.Color AS TaskColor, 0 AS SingleInstanceColor "
								+ "FROM Tasks, Persons, Programs, AssignedTasks, UnavailDates "
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
								+ "  AND ((SELECT COUNT(*) FROM UnavailDates WHERE Persons.PersonID = UnavailDates.PersonID) = 0 "
								+ "      OR ? NOT BETWEEN UnavailDates.StartDate AND UnavailDates.EndDate) "

								+ "UNION " +

								// Floaters + Subs from Single Instance Tasks
								"SELECT PersonName, isLeader AS Leader, true AS SingleInstance, SingleInstanceTasks.TaskID AS TaskID, "
								+ "TaskName, HOUR(SingleTime) AS Hour, MINUTE(SingleTime) AS Minute, Location, PhoneNumber, EMail, "
								+ "Tasks.Color AS TaskColor, SingleInstanceTasks.Color AS SingleInstanceColor "
								+ "FROM Tasks, Persons, Programs, SingleInstanceTasks, UnavailDates "
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
								+ "  AND ((SELECT COUNT(*) FROM UnavailDates WHERE Persons.PersonID = UnavailDates.PersonID) = 0 "
								+ "      OR ? NOT BETWEEN UnavailDates.StartDate AND UnavailDates.EndDate) "

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
		return (ArrayList<PersonByTaskModel>) thisDaysPersons;
	}

	// public ArrayList<PersonByTaskModel> getPersonsByDayByTime(Calendar
	// calendar) {
	// ArrayList<PersonByTaskModel> persons = getPersonsByDay(calendar);

	// TODO:
	// for (int i = 0; i < persons.size(); i++) {
	// PersonByTaskModel person = persons.get(i);

	// if (!Utilities.checkForTimeMatch(person.getTaskDate(), calendar)) {
	// persons.remove(i);
	// i--;
	// }
	// }
	// return (ArrayList<PersonByTaskModel>) persons;
	// }

	// public ArrayList<PersonByTaskModel> getPersonsByDayByLocation(Calendar
	// calendar, String location) {
	// ArrayList<PersonByTaskModel> personList = getPersonsByDay(calendar);

	// TODO:
	// for (int i = 0; i < personList.size(); i++) {
	// PersonByTaskModel person = personList.get(i);

	// if (person.getTask() == null ||
	// !person.getTask().getLocation().equals(location)) {
	// personList.remove(i);
	// i--;
	// }
	// }
	// return (ArrayList<PersonByTaskModel>) personList;
	// }

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
