package model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

import TestDatabase.TestDatabase;
import utilities.Utilities;

public class Database {
	// Locally used constants
	private static final int PERSON_NOT_AVAIL = -2;
	private static final int NO_MATCH_FOUND = -1;
	private static final int ASSIGNED_TASK_MATCH = 0;
	private static final int SINGLE_INSTANCE_TASK_MATCH = 1;
	private static final int FLOATER_MATCH = 2;

	private LinkedList<ProgramModel> programList;
	private LinkedList<PersonModel> personList;
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy");

	public Database() {
		// Connect to database
		TestDatabase.initializeDatabase();

		programList = new LinkedList<ProgramModel>();
		personList = new LinkedList<PersonModel>();
	}

	/*
	 * ------- Programs -------
	 */
	public void addProgram(String programName, String startDate, String endDate) {
		int programID = TestDatabase.addProgram(programName, startDate, endDate);

		if (programID > 0) {
			LinkedList<TaskModel> taskList = new LinkedList<TaskModel>();
			programList.add(new ProgramModel(programID, programName, startDate, endDate, taskList));
			Collections.sort(programList);
		}
	}

	public void updateProgram(String programName, String startDate, String endDate) {
		ProgramModel program = getProgramByName(programName);
		if (!program.getStartDate().equals(startDate) || !program.getEndDate().equals(endDate)) {
			// Update database
			TestDatabase.updateProgramDates(program.getProgramID(), startDate, endDate);

			program.setStartDate(startDate);
			program.setEndDate(endDate);
		}
	}

	public void renameProgram(String oldName, String newName) {
		ProgramModel program = getProgramByName(oldName);
		if (program != null) {
			// Update database
			TestDatabase.updateProgramName(program.getProgramID(), newName);

			program.setProgramName(newName);
			Collections.sort(programList);

			// Update persons' assigned tasks lists
			updateProgramNameByPerson(oldName, newName);

		} else
			JOptionPane.showMessageDialog(null, "Program '" + oldName + "' not found!", "Error renaming program",
					JOptionPane.ERROR_MESSAGE);
	}

	public ProgramModel getProgramByName(String programName) {
		for (int i = 0; i < programList.size(); i++) {
			ProgramModel p = programList.get(i);
			if (p.getProgramName().equals(programName)) {
				return p;
			}
		}
		return null;
	}

	public JList<String> getAllProgramsAsString() {
		DefaultListModel<String> nameModel = new DefaultListModel<String>();
		for (int i = 0; i < programList.size(); i++) {
			ProgramModel p = programList.get(i);
			nameModel.addElement(new String(p.getProgramName()));
		}
		return (new JList<String>(nameModel));
	}

	public LinkedList<ProgramModel> getAllPrograms() {
		// return (List<ProgramModel>)
		// Collections.unmodifiableList(programList);
		return programList;
	}

	public int getNumPrograms() {
		return programList.size();
	}

	private int getProgramIndexByName(String programName) {
		int progIdx = 0;

		for (int i = 0; progIdx < programList.size(); i++) {
			ProgramModel p = programList.get(i);
			if (p.getProgramName().equals(programName))
				return progIdx;
			progIdx++;
		}
		return -1;
	}

	private void updateProgramNameByPerson(String oldName, String newName) {
		for (int i = 0; i < personList.size(); i++) {
			PersonModel person = personList.get(i);
			for (int j = 0; j < person.getAssignedTasks().size(); j++) {
				AssignedTasksModel assignedTask = person.getAssignedTasks().get(j);
				if (assignedTask.getProgramName().equals(oldName))
					assignedTask.setProgramName(newName);
			}
		}
	}

	/*
	 * ------- Task data -------
	 */
	public void addTask(String programName, String taskName, String location, int numLeadersReqd, int totalPersonsReqd,
			boolean[] dayOfWeek, boolean[] weekOfMonth, TimeModel time, int color) {
		ProgramModel program = getProgramByName(programName);
		int taskID = TestDatabase.addTask(program.getProgramID(), taskName, location, numLeadersReqd, totalPersonsReqd,
				dayOfWeek, weekOfMonth, time, color);

		if (taskID > 0) {
			program.getTaskList().add(new TaskModel(taskID, program.getProgramID(), taskName, location, numLeadersReqd,
					totalPersonsReqd, dayOfWeek, weekOfMonth, time, color));
			Collections.sort(program.getTaskList());
		}
	}

	public void updateTask(String programName, String taskName, String location, int numLeadersReqd,
			int totalPersonsReqd, boolean[] dayOfWeek, boolean[] weekOfMonth, TimeModel time, int color) {
		ProgramModel program = getProgramByName(programName);
		int taskListIdx = getTaskIndexByName(program, taskName);

		if (taskListIdx != -1) {
			TaskModel task = program.getTaskList().get(taskListIdx);
			// Update database
			TestDatabase.updateTaskFields(task.getTaskID(), taskName, location, numLeadersReqd, totalPersonsReqd,
					dayOfWeek, weekOfMonth, time, color);

			TaskModel newTask = new TaskModel(task.getTaskID(), task.getProgramID(), taskName, location, numLeadersReqd,
					totalPersonsReqd, dayOfWeek, weekOfMonth, time, color);

			program.getTaskList().set(taskListIdx, newTask);
			Collections.sort(program.getTaskList());

		} else
			JOptionPane.showMessageDialog(null, "Task '" + taskName + "' not found!", "Error updating task",
					JOptionPane.ERROR_MESSAGE);
	}

	public void renameTask(String programName, String oldName, String newName) {
		int taskListIdx;

		ProgramModel program = getProgramByName(programName);
		taskListIdx = getTaskIndexByName(program, oldName);
		if (taskListIdx != -1) {
			TaskModel task = program.getTaskList().get(taskListIdx);
			// Update database
			TestDatabase.updateTaskName(task.getTaskID(), newName);

			task.setTaskName(newName);
			program.getTaskList().set(taskListIdx, task);

			// Update persons' assigned tasks lists
			updateTaskNameByPerson(oldName, newName);

		} else
			JOptionPane.showMessageDialog(null, "Task '" + oldName + "' not found!", "Error renaming task",
					JOptionPane.ERROR_MESSAGE);
	}

	public TaskModel getTaskByName(String programName, String taskName) {
		ProgramModel program = getProgramByName(programName);
		if (program == null)
			return null;

		for (int i = 0; i < program.getTaskList().size(); i++) {
			TaskModel t = program.getTaskList().get(i);
			if (t.getTaskName().equals(taskName)) {
				return t;
			}
		}
		return null;
	}

	public String findProgramByTaskName(String taskName) {
		LinkedList<ProgramModel> allPrograms = getAllPrograms();
		for (int i = 0; i < allPrograms.size(); i++) {
			ProgramModel p = allPrograms.get(i);
			for (int j = 0; j < p.getTaskList().size(); j++) {
				TaskModel t = p.getTaskList().get(j);
				if (t.getTaskName().equals(taskName))
					return p.getProgramName();
			}
		}
		return null;
	}

	public LinkedList<CalendarDayModel> getTasksByDayByProgram(Calendar calendar, JList<String> programFilterList) {
		LinkedList<CalendarDayModel> thisDaysTasks = getAllTasksByDay(calendar);

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

		for (int taskIdx = 0; taskIdx < thisDaysTasks.size(); taskIdx++) {
			match = false;
			String thisDaysTaskName = thisDaysTasks.get(taskIdx).getTask().getTaskName();

			for (int i = 0; i < persons.getModel().getSize(); i++) {
				PersonModel pModel = getPersonByName(persons.getModel().getElementAt(i));
				// -1 = no match, 0 = assigned task, 1 = single instance task
				if (checkPersonMatchForTaskByDay(pModel, thisDaysTaskName, thisDay, dayOfWeekIdx,
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

		// Get all tasks for today
		LinkedList<CalendarDayModel> thisDaysTasks = getAllTasksByDay(calendar);

		// Now add floaters to the list
		for (int i = 0; i < personList.size(); i++) {
			PersonModel person = personList.get(i);

			// Check if person is a floater (not associated with task).
			for (int j = 0; j < person.getSingleInstanceTasks().size(); j++) {
				SingleInstanceTaskModel task = person.getSingleInstanceTasks().get(j);
				if (checkSingleInstanceTaskMatch(task, "", thisDay, dayOfWeekIdx, dayOfWeekInMonthIdx) >= 0) {
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

	// public List<TaskModel> getAllTasks() {
	// return Collections.unmodifiableList(taskList);
	// }

	public JList<TaskModel> getAllTasksByProgram(String programName) {
		DefaultListModel<TaskModel> taskModel = new DefaultListModel<TaskModel>();
		ProgramModel program = getProgramByName(programName);

		for (int i = 0; i < program.getTaskList().size(); i++) {
			TaskModel t = program.getTaskList().get(i);
			taskModel.addElement(t);
		}
		return new JList<TaskModel>(taskModel);
	}

	public JList<TaskModel> getAllTasks() {
		DefaultListModel<TaskModel> taskModel = new DefaultListModel<TaskModel>();

		for (int i = 0; i < programList.size(); i++) {
			ProgramModel p = programList.get(i);

			for (int j = 0; j < p.getTaskList().size(); j++) {
				TaskModel t = p.getTaskList().get(j);
				taskModel.addElement(t);
			}
		}
		return (new JList<TaskModel>(taskModel));
	}

	public JList<String> getAllLocationsAsString() {
		DefaultListModel<String> locationModel = new DefaultListModel<String>();
		JList<String> locationList = new JList<String>(locationModel);

		for (int i = 0; i < programList.size(); i++) {
			ProgramModel prog = programList.get(i);
			JList<TaskModel> taskList = getAllTasksByProgram(prog.getProgramName());
			for (int j = 0; j < taskList.getModel().getSize(); j++) {
				// Check whether already in list before adding
				String loc = taskList.getModel().getElementAt(j).getLocation();
				if (!loc.equals("") && !Utilities.findStringMatchInJList(loc, locationList)) {
					locationModel.addElement(loc);
				}
			}
		}
		return (locationList);
	}

	public JList<String> getAllTimesAsString() {
		DefaultListModel<String> timeModel = new DefaultListModel<String>();
		ArrayList<TimeModel> timeArray = new ArrayList<TimeModel>();

		for (int i = 0; i < programList.size(); i++) {
			ProgramModel prog = programList.get(i);
			JList<TaskModel> taskList = getAllTasksByProgram(prog.getProgramName());
			for (int j = 0; j < taskList.getModel().getSize(); j++) {
				// Check whether already in list before adding
				TimeModel time = taskList.getModel().getElementAt(j).getTime();
				if (!findTimeMatchInArray(time, timeArray)) {
					timeArray.add(time);
				}
			}
		}
		Collections.sort(timeArray);
		for (int i = 0; i < timeArray.size(); i++)
			timeModel.addElement(timeArray.get(i).toString());

		return (new JList<String>(timeModel));
	}

	public JList<TimeModel> getAllTimesByDay(Calendar calendar) {
		DefaultListModel<TimeModel> timeModel = new DefaultListModel<TimeModel>();
		ArrayList<TimeModel> timeArray = new ArrayList<TimeModel>();

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

	private int getTaskIndexByName(ProgramModel program, String taskName) {
		int taskIdx = 0;

		for (int i = 0; i < program.getTaskList().size(); i++) {
			TaskModel t = program.getTaskList().get(i);
			if (t.getTaskName().equals(taskName))
				return taskIdx;
			taskIdx++;
		}
		return -1;
	}

	private void updateTaskNameByPerson(String oldTaskName, String newTaskName) {
		for (int i = 0; i < personList.size(); i++) {
			PersonModel person = personList.get(i);

			for (int j = 0; j < person.getAssignedTasks().size(); j++) {
				AssignedTasksModel assignedTask = person.getAssignedTasks().get(j);
				if (assignedTask.getTaskName().equals(oldTaskName))
					assignedTask.setTaskName(newTaskName);
			}
		}
	}

	private int checkPersonMatchForTask(PersonModel person, String taskName) {
		LinkedList<AssignedTasksModel> assignedTaskList = person.getAssignedTasks();

		// Check if task is in person's assigned task list
		for (int i = 0; i < assignedTaskList.size(); i++) {
			AssignedTasksModel assignedTask = assignedTaskList.get(i);
			if (assignedTask.getTaskName().equals(taskName)) {
				return ASSIGNED_TASK_MATCH;
			}
		}
		return NO_MATCH_FOUND;
	}

	private int checkPersonMatchForTaskByDay(PersonModel person, String taskName, Date today, int dayOfWeekIdx,
			int dowInMonthIdx) {
		if (!isPersonAvailable(person, today))
			return PERSON_NOT_AVAIL;

		else {
			LinkedList<AssignedTasksModel> assignedTaskList = person.getAssignedTasks();

			// Check if task is in person's assigned task list for today
			for (int i = 0; i < assignedTaskList.size(); i++) {
				AssignedTasksModel assignedTask = assignedTaskList.get(i);
				if (assignedTask.getTaskName().equals(taskName) && assignedTask.getDaysOfWeek()[dayOfWeekIdx]
						&& assignedTask.getWeeksOfMonth()[dowInMonthIdx]) {
					return ASSIGNED_TASK_MATCH;
				}
			}

			for (int i = 0; i < person.getSingleInstanceTasks().size(); i++) {
				SingleInstanceTaskModel singleInstanceTask = person.getSingleInstanceTasks().get(i);
				// Check if this person is a sub for today
				Calendar subCalendar = singleInstanceTask.getTaskDate();
				if (singleInstanceTask.getTaskName().equals(taskName)
						&& Utilities.checkForDateAndTimeMatch(today, dayOfWeekIdx, dowInMonthIdx, subCalendar)) {
					return SINGLE_INSTANCE_TASK_MATCH;
				}
			}
		}
		return NO_MATCH_FOUND;
	}

	private int checkSingleInstanceTaskMatch(SingleInstanceTaskModel singleInstanceTask, String taskName, Date today,
			int dayOfWeekIdx, int dowInMonthIdx) {
		// Check if this person is a sub for today
		Calendar subCalendar = singleInstanceTask.getTaskDate();

		if (singleInstanceTask.getTaskName().equals(taskName)
				&& Utilities.checkForDateAndTimeMatch(today, dayOfWeekIdx, dowInMonthIdx, subCalendar))
			return SINGLE_INSTANCE_TASK_MATCH;
		else
			return NO_MATCH_FOUND;
	}

	private int getPersonCountForTaskByDay(TaskModel task, Date today, int dayOfWeekIdx, int dowInMonthIdx) {
		JList<PersonModel> persons = getAllPersons();
		short personCount = 0;
		short leaderCount = 0;

		for (int idx = 0; idx < persons.getModel().getSize(); idx++) {
			PersonModel person = persons.getModel().getElementAt(idx);
			// -1 = no match, 0 = assigned task, 1 = single instance task
			if (checkPersonMatchForTaskByDay(person, task.getTaskName(), today, dayOfWeekIdx, dowInMonthIdx) >= 0) {
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
		int personID = TestDatabase.addPerson(name, phone, email, leader, notes);
		if (personID <= 0)
			return;

		for (int i = 0; i < assignedTasks.size(); i++) {
			AssignedTasksModel task = assignedTasks.get(i);
			int assignedTaskID = TestDatabase.addAssignedTask(personID, task.getTaskID(), task.getDaysOfWeek(),
					task.getWeeksOfMonth());
			if (assignedTaskID > 0) {
				task.setPersonID(personID);
				task.setAssignedTaskID(assignedTaskID);
			} else {
				assignedTasks.remove(i);
				i--;
			}
		}
		Collections.sort(assignedTasks);

		for (int i = 0; i < extraTasks.size(); i++) {
			SingleInstanceTaskModel task = extraTasks.get(i);
			int singleTaskID = TestDatabase.addSingleInstanceTask(personID, task.getTaskID(), task.getTaskDate());
			if (singleTaskID > 0) {
				task.setPersonID(personID);
				task.setSingleTaskID(singleTaskID);
			} else {
				extraTasks.remove(i);
				i--;
			}
		}
		Collections.sort(extraTasks);

		for (int i = 0; i < datesUnavailable.size(); i++) {
			DateRangeModel dates = datesUnavailable.get(i);
			int datesID = TestDatabase.addUnavailDates(personID, dates.getStartDate(), dates.getEndDate());
			if (datesID > 0) {
				dates.setPersonID(personID);
				dates.setUnavailDatesID(datesID);
			} else {
				datesUnavailable.remove(i);
				i--;
			}
		}
		Collections.sort(datesUnavailable);

		personList.add(new PersonModel(personID, name, phone, email, leader, notes, assignedTasks, datesUnavailable,
				extraTasks));
		Collections.sort(personList);
	}

	public void updatePerson(String personName, String personPhone, String personEmail, boolean personIsLeader,
			String personNotes, LinkedList<AssignedTasksModel> personAssignedTasks,
			LinkedList<SingleInstanceTaskModel> extraTasks, LinkedList<DateRangeModel> personDatesUnavailable) {

		int personIdx = getPersonIndexByName(personName);
		if (personIdx != -1) {
			// Merge in the assigned tasks (list ONLY contains changes!!)
			int taskIdx;
			PersonModel thisPerson = personList.get(personIdx);
			int personID = thisPerson.getPersonID();
			LinkedList<AssignedTasksModel> dbAssignedTaskList = thisPerson.getAssignedTasks();

			for (int i = 0; i < personAssignedTasks.size(); i++) {
				// Update database
				AssignedTasksModel assignedTask = personAssignedTasks.get(i);
				taskIdx = findAssignedTaskIdx(assignedTask.getTaskName(), dbAssignedTaskList);
				if (taskIdx != -1) {
					// Assigned task already in database, so update
					int assignedTaskID = dbAssignedTaskList.get(taskIdx).getAssignedTaskID();
					TestDatabase.updateAssignedTask(assignedTaskID, assignedTask.getDaysOfWeek(),
							assignedTask.getWeeksOfMonth());
					assignedTask.setPersonID(personID);
					assignedTask.setAssignedTaskID(assignedTaskID);
					thisPerson.getAssignedTasks().set(taskIdx, assignedTask);
				} else {
					// New task was assigned, add to database
					int assignedTaskID = TestDatabase.addAssignedTask(personID, assignedTask.getTaskID(),
							assignedTask.getDaysOfWeek(), assignedTask.getWeeksOfMonth());
					if (assignedTaskID > 0) {
						assignedTask.setPersonID(personID);
						assignedTask.setAssignedTaskID(assignedTaskID);
						thisPerson.getAssignedTasks().add(assignedTask);
					}
				}
			}

			// Add extraTasks (list only contains additions!!)
			for (int i = 0; i < extraTasks.size(); i++) {
				SingleInstanceTaskModel singleTask = extraTasks.get(i);

				// Add single instance task to database
				int taskID = singleTask.getTaskID();
				int singleInstanceID = TestDatabase.addSingleInstanceTask(personID, taskID, singleTask.getTaskDate());
				if (singleInstanceID > 0) {
					thisPerson.getSingleInstanceTasks().add(new SingleInstanceTaskModel(singleInstanceID, personID,
							taskID, singleTask.getTaskName(), singleTask.getTaskDate(), singleTask.getColor()));
				}
			}
			Collections.sort(thisPerson.getSingleInstanceTasks());

			// Add dates unavailable (list only contains additions!!)
			for (int i = 0; i < personDatesUnavailable.size(); i++) {
				DateRangeModel date = personDatesUnavailable.get(i);

				// Add unavailable dates if not a duplicate
				if (!findDateMatchInDateRangeModel(thisPerson.getDatesUnavailable(), date)) {
					int datesID = TestDatabase.addUnavailDates(personID, date.getStartDate(), date.getEndDate());
					if (datesID > 0) {
						date.setPersonID(personID);
						date.setUnavailDatesID(datesID);
						thisPerson.getDatesUnavailable().add(date);
					}
				}
			}
			Collections.sort(thisPerson.getDatesUnavailable());

			// Now update remaining fields
			TestDatabase.updatePerson(thisPerson.getPersonID(), personName, personPhone, personEmail, personIsLeader,
					personNotes);
			thisPerson.setName(personName);
			thisPerson.setPhone(personPhone);
			thisPerson.setEmail(personEmail);
			thisPerson.setLeader(personIsLeader);
			thisPerson.setNotes(personNotes);

		} else
			JOptionPane.showMessageDialog(null, "Person '" + personName + "' not found!");
	}

	public void addSingleInstanceTask(String personName, Calendar calendar, TaskModel task, int color) {
		// Get person
		PersonModel person = getPersonByName(personName);
		int taskID, singleTaskID;
		String taskName;

		if (task == null) {
			taskID = 0;
			taskName = "";
		} else {
			taskID = task.getTaskID();
			taskName = task.getTaskName();
		}
		singleTaskID = TestDatabase.addSingleInstanceTask(person.getPersonID(), taskID, calendar);

		if (singleTaskID > 0) {
			// Add task to single instance task list
			person.getSingleInstanceTasks().add(
					new SingleInstanceTaskModel(singleTaskID, person.getPersonID(), taskID, taskName, calendar, color));
			Collections.sort(person.getSingleInstanceTasks());
		}
	}

	public void renamePerson(String oldName, String newName) {
		int personIdx = getPersonIndexByName(oldName);
		if (personIdx != -1) {
			// Update person name
			PersonModel person = personList.get(personIdx);
			person.setName(newName);
			Collections.sort(personList);
		} else
			JOptionPane.showMessageDialog(null, "Person '" + oldName + "' not found!");
	}

	public void markPersonUnavail(String personName, Calendar today) {
		// Get person
		PersonModel person = getPersonByName(personName);

		if (person != null) {
			// Mark person unavailable for today
			String displayDate = Utilities.getDisplayDate(today);
			int unavailDatesID = TestDatabase.addUnavailDates(person.getPersonID(), displayDate, displayDate);

			if (unavailDatesID > 0) {
				DateRangeModel dateModel = new DateRangeModel(unavailDatesID, person.getPersonID(), displayDate,
						displayDate);
				person.getDatesUnavailable().add(dateModel);
			}
		}
	}

	public PersonModel getPersonByName(String name) {
		for (int i = 0; i < personList.size(); i++) {
			PersonModel p = personList.get(i);
			if (p.getName().equals(name)) {
				return p;
			}
		}
		return null;
	}

	public JList<String> getAllPersonsAsString() {
		DefaultListModel<String> nameModel = new DefaultListModel<String>();

		for (int i = 0; i < personList.size(); i++) {
			PersonModel p = personList.get(i);
			nameModel.addElement(new String(p.getName()));
		}
		return (new JList<String>(nameModel));
	}

	public JList<String> getAvailPersonsAsString(Calendar today) {
		Date thisDay = Utilities.getDateFromCalendar(today);

		// Get all persons who are available today
		DefaultListModel<String> nameModel = new DefaultListModel<String>();

		for (int i = 0; i < personList.size(); i++) {
			PersonModel p = personList.get(i);
			if (isPersonAvailable(p, thisDay))
				nameModel.addElement(new String(p.getName()));
		}
		return (new JList<String>(nameModel));
	}

	public JList<PersonModel> getAllPersons() {
		DefaultListModel<PersonModel> personModel = new DefaultListModel<>();
		for (int i = 0; i < personList.size(); i++) {
			PersonModel p = personList.get(i);
			personModel.addElement(p);
		}
		JList<PersonModel> list = new JList<>(personModel);
		return list;
	}

	public LinkedList<PersonByTaskModel> getAllPersonsList() {
		LinkedList<PersonByTaskModel> personsByTask = new LinkedList<PersonByTaskModel>();
		for (int i = 0; i < personList.size(); i++) {
			PersonModel p = personList.get(i);
			PersonByTaskModel person = new PersonByTaskModel(p, null, false, 0, null);
			personsByTask.add(person);
		}
		return (LinkedList<PersonByTaskModel>) personsByTask;
	}

	public LinkedList<PersonByTaskModel> getPersonsByTask(TaskModel task) {
		JList<PersonModel> persons = getAllPersons();
		LinkedList<PersonByTaskModel> thisTasksPersons = new LinkedList<PersonByTaskModel>();

		for (int i = 0; i < persons.getModel().getSize(); i++) {
			PersonModel pModel = persons.getModel().getElementAt(i);

			// -1 = no match, 0 = assigned task
			if (checkPersonMatchForTask(pModel, task.getTaskName()) == 0) {
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
				int match = checkPersonMatchForTaskByDay(pModel, task.getTaskName(), thisDay, dayOfWeekIdx,
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
				if (checkSingleInstanceTaskMatch(task, "", thisDay, dayOfWeekIdx, dayOfWeekInMonthIdx) >= 0) {
					PersonByTaskModel personByTask = new PersonByTaskModel(pModel, null, false, task.getColor(),
							task.getTaskDate());
					thisDaysPersons.add(personByTask);
				}
			}
		}
		return (LinkedList<PersonByTaskModel>) thisDaysPersons;
	}

	// TODO: Currently not used!
	// If it is used later, add check for 'isPersonAvailable'.
	public LinkedList<PersonByTaskModel> getPersonsByDayByTask(Calendar calendar, TaskModel task) {
		int dayOfWeekInMonthIdx = calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH) - 1;
		int dayOfWeekIdx = calendar.get(Calendar.DAY_OF_WEEK) - 1;
		Date thisDay = Utilities.getDateFromCalendar(calendar);

		JList<PersonModel> persons = getAllPersons();
		LinkedList<PersonByTaskModel> thisDaysPersons = new LinkedList<PersonByTaskModel>();

		for (int i = 0; i < persons.getModel().getSize(); i++) {
			PersonModel pModel = getPersonByName(persons.getModel().getElementAt(i).toString());

			// -1 = no match, 0 = assigned task, 1 = single instance task
			if (checkPersonMatchForTaskByDay(pModel, task.getTaskName(), thisDay, dayOfWeekIdx,
					dayOfWeekInMonthIdx) >= 0) {
				Utilities.addTimeToCalendar(calendar, task.getTime());
				PersonByTaskModel personByTask = new PersonByTaskModel(pModel, task, false, task.getColor(), calendar);
				thisDaysPersons.add(personByTask);
			}
		}
		return (LinkedList<PersonByTaskModel>) thisDaysPersons;
	}

	public LinkedList<PersonByTaskModel> getPersonsByDayByTime(Calendar calendar) {
		LinkedList<PersonByTaskModel> persons = getPersonsByDay(calendar);

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
		return personList.size();
	}

	private int getPersonIndexByName(String personName) {
		int personIdx = 0;

		for (int i = 0; i < personList.size(); i++) {
			PersonModel p = personList.get(i);
			if (p.getName().equals(personName))
				return personIdx;
			personIdx++;
		}
		return -1;
	}

	private boolean findTimeMatchInArray(TimeModel findTime, ArrayList<TimeModel> list) {
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).compareTo(findTime) == 0)
				return true;
		}
		return false;
	}

	private int findAssignedTaskIdx(String taskName, LinkedList<AssignedTasksModel> assignedTaskList) {
		int taskIdx = 0;

		for (int i = 0; i < assignedTaskList.size(); i++) {
			AssignedTasksModel task = assignedTaskList.get(i);
			if (task.getTaskName().equals(taskName))
				return taskIdx;
			taskIdx++;
		}
		return -1;
	}

	private boolean findDateMatchInDateRangeModel(LinkedList<DateRangeModel> dateList, DateRangeModel compareDate) {
		for (int i = 0; i < dateList.size(); i++) {
			DateRangeModel date = dateList.get(i);
			if (date.compareTo(compareDate) == 0)
				return true;
		}
		return false;
	}

	/*
	 * ------- File save/restore items -------
	 */
	public void saveProgramToFile(JList<String> programNameList, File file) throws IOException {
		FileOutputStream fos = new FileOutputStream(file);
		ObjectOutputStream oos = new ObjectOutputStream(fos);

		// Create linked list of selected programs
		LinkedList<ProgramModel> pList = new LinkedList<ProgramModel>();
		for (int i = 0; i < programNameList.getModel().getSize(); i++) {
			pList.add(getProgramByName(programNameList.getModel().getElementAt(i)));
		}

		// Convert to array
		ProgramModel[] programs = pList.toArray(new ProgramModel[pList.size()]);

		oos.writeObject(programs);
		oos.close();
	}

	public void loadProgramFromDatabase() {
		programList = TestDatabase.loadPrograms();
	}

	public void loadProgramFromFile(File file) throws IOException {
		FileInputStream fis = new FileInputStream(file);
		ObjectInputStream ois = new ObjectInputStream(fis);

		try {
			// Convert from array to program
			ProgramModel[] programs = (ProgramModel[]) ois.readObject();
			for (int i = 0; i < programs.length; i++) {
				ProgramModel p = programs[i];
				// Check if program already exists, if so then replace it
				int index = getProgramIndexByName(p.getProgramName());
				if (index >= 0)
					programList.remove(index);
			}
			programList.addAll(Arrays.asList(programs));

		} catch (ClassNotFoundException e) {
			JOptionPane.showMessageDialog(null, "Invalid file format.", "Error Loading Program File",
					JOptionPane.ERROR_MESSAGE);
			// e.printStackTrace();
		} catch (InvalidClassException e) {
			JOptionPane.showMessageDialog(null, "File version does not match.", "Error Loading Program File",
					JOptionPane.ERROR_MESSAGE);
		} catch (ClassCastException e) {
			JOptionPane.showMessageDialog(null, "Invalid file format.", "Error Loading Program File",
					JOptionPane.ERROR_MESSAGE);
		}
		ois.close();
	}

	public void saveRosterToFile(File file) throws IOException {
		FileOutputStream fos = new FileOutputStream(file);
		ObjectOutputStream oos = new ObjectOutputStream(fos);

		// Convert to array
		PersonModel[] roster = personList.toArray(new PersonModel[personList.size()]);

		oos.writeObject(roster);
		oos.close();
	}

	public void loadRosterFromDatabase() {
		personList = TestDatabase.loadRoster();
	}

	public void loadRosterFromFile(File file) throws IOException {
		FileInputStream fis = new FileInputStream(file);
		ObjectInputStream ois = new ObjectInputStream(fis);

		try {
			// Convert from array
			PersonModel[] roster = (PersonModel[]) ois.readObject();
			personList.clear();
			personList.addAll(Arrays.asList(roster));

		} catch (ClassNotFoundException e) {
			JOptionPane.showMessageDialog(null, "Invalid file format.", "Error Loading Roster File",
					JOptionPane.ERROR_MESSAGE);
			// e.printStackTrace();
		} catch (InvalidClassException e) {
			JOptionPane.showMessageDialog(null, "File version does not match.", "Error Loading Roster File",
					JOptionPane.ERROR_MESSAGE);
		} catch (ClassCastException e) {
			JOptionPane.showMessageDialog(null, "Invalid file format.", "Error Loading Roster File",
					JOptionPane.ERROR_MESSAGE);
		}
		ois.close();
	}
}
