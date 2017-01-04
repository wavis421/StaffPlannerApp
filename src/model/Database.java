package model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Time;
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

import utilities.Utilities;

public class Database {
	private LinkedList<ProgramModel> programList;
	private LinkedList<PersonModel> personList;
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy");

	public Database() {
		programList = new LinkedList<ProgramModel>();
		personList = new LinkedList<PersonModel>();
	}

	/*
	 * ------- Programs -------
	 */
	public void addProgram(String programName, String startDate, String endDate) {
		LinkedList<TaskModel> taskList = new LinkedList<TaskModel>();
		programList.add(new ProgramModel(programName, startDate, endDate, taskList));
		Collections.sort(programList);
	}

	public void updateProgram(String programName, String startDate, String endDate) {
		ProgramModel program = getProgramByName(programName);
		program.setStartDate(startDate);
		program.setEndDate(endDate);
	}

	public void renameProgram(String oldName, String newName) {
		ProgramModel program = getProgramByName(oldName);
		if (program != null) {
			program.setProgramName(newName);
			Collections.sort(programList);

			// Update persons' assigned tasks lists
			updateProgramNameByPerson(oldName, newName);

		} else
			JOptionPane.showMessageDialog(null, "Program '" + oldName + "' not found!", "Error renaming program",
					JOptionPane.ERROR_MESSAGE);
	}

	public ProgramModel getProgramByName(String programName) {
		for (ProgramModel p : programList) {
			if (p.getProgramName().equals(programName)) {
				return p;
			}
		}
		return null;
	}

	public JList<String> getAllProgramsAsString() {
		DefaultListModel<String> nameModel = new DefaultListModel<String>();
		for (ProgramModel p : programList) {
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
		int i = 0;

		for (ProgramModel p : programList) {
			if (p.getProgramName().equals(programName))
				return i;
			i++;
		}
		return -1;
	}

	private void updateProgramNameByPerson(String oldName, String newName) {
		for (PersonModel person : personList) {
			for (AssignedTasksModel assignedTask : person.getAssignedTasks()) {
				if (assignedTask.getProgramName().equals(oldName))
					assignedTask.setProgramName(newName);
			}
		}
	}

	/*
	 * ------- Task data -------
	 */
	public void addTask(String programName, TaskModel task) {
		ProgramModel program = getProgramByName(programName);
		program.getTaskList().add(task);
		Collections.sort(program.getTaskList());
	}

	public void updateTask(String programName, TaskModel task) {
		ProgramModel program = getProgramByName(programName);
		int taskIdx = getTaskIndexByName(program, task.getTaskName());
		if (taskIdx != -1) {
			program.getTaskList().set(taskIdx, task);
			Collections.sort(program.getTaskList());
		} else
			JOptionPane.showMessageDialog(null, "Task '" + task.getTaskName() + "' not found!", "Error updating task",
					JOptionPane.ERROR_MESSAGE);
	}

	public void renameTask(String programName, String oldName, String newName) {
		int taskIdx;

		ProgramModel program = getProgramByName(programName);
		taskIdx = getTaskIndexByName(program, oldName);
		if (taskIdx != -1) {
			TaskModel task = program.getTaskList().get(taskIdx);
			task.setTaskName(newName);
			program.getTaskList().set(taskIdx, task);

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

		for (TaskModel t : program.getTaskList()) {
			if (t.getTaskName().equals(taskName)) {
				return t;
			}
		}
		return null;
	}

	public String findProgramByTaskName(String taskName) {
		LinkedList<ProgramModel> allPrograms = getAllPrograms();
		for (ProgramModel p : allPrograms) {
			for (TaskModel t : p.getTaskList()) {
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
			if (!findStringMatchInList(programName, programFilterList)) {
				thisDaysTasks.remove(taskIdx);
				taskIdx--;
			}
		}
		return thisDaysTasks;
	}

	public LinkedList<CalendarDayModel> getTasksByDayByPerson(Calendar calendar, JList<String> persons) {
		int dayOfWeekInMonthIdx = calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH) - 1;
		int dayOfWeekIdx = calendar.get(Calendar.DAY_OF_WEEK) - 1;
		Date thisDay = getDay(calendar);
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
			if (thisDaysTasks.get(taskIdx).getPersonCount() >= thisDaysTasks.get(taskIdx).getTask()
					.getTotalPersonsReqd()) {
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
			if (!findStringMatchInList(taskLoc, locations)) {
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
			String taskTime = Utilities.formatTime(matchingTasks.get(taskIdx).getTask().getTime());
			if (!findStringMatchInList(taskTime, timeList)) {
				matchingTasks.remove(taskIdx);
				taskIdx--;
			}
		}
		return matchingTasks;
	}

	public LinkedList<CalendarDayModel> getAllTasksByDay(Calendar calendar) {
		int dayOfWeekInMonthIdx = calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH) - 1;
		int dayOfWeekIdx = calendar.get(Calendar.DAY_OF_WEEK) - 1;
		Date thisDay = getDay(calendar);

		LinkedList<CalendarDayModel> thisDaysTasks = new LinkedList<CalendarDayModel>();
		for (ProgramModel prog : programList) {
			if (isProgramExpired(thisDay, prog))
				continue;

			for (TaskModel task : prog.getTaskList()) {
				if ((task.getDayOfWeek()[dayOfWeekIdx]) && (task.getWeekOfMonth()[dayOfWeekInMonthIdx])) {
					thisDaysTasks.add(new CalendarDayModel(task,
							getPersonCountForTaskByDay(task, thisDay, dayOfWeekIdx, dayOfWeekInMonthIdx),
							task.getColor(), null, null));
				}
			}
		}
		return (LinkedList<CalendarDayModel>) thisDaysTasks.clone();
	}

	public LinkedList<CalendarDayModel> getAllTasksAndFloatersByDay(Calendar calendar) {
		int dayOfWeekInMonthIdx = calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH) - 1;
		int dayOfWeekIdx = calendar.get(Calendar.DAY_OF_WEEK) - 1;
		Date thisDay = getDay(calendar);

		// Get all tasks for today
		LinkedList<CalendarDayModel> thisDaysTasks = getAllTasksByDay(calendar);

		// Now add floaters to the list
		for (PersonModel person : personList) {
			// Check if person is a floater (not associated with task).
			for (SingleInstanceTaskModel task : person.getSingleInstanceTasks()) {
				if (checkSingleInstanceTaskMatch(task, "", thisDay, dayOfWeekIdx, dayOfWeekInMonthIdx) >= 0) {
					thisDaysTasks.add(new CalendarDayModel(null, 0, task.getColor(), task.getTaskDate(), "Floater"));
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

	/*
	 * public List<TaskModel> getAllTasks() { return
	 * Collections.unmodifiableList(taskList); }
	 */

	public JList<TaskModel> getAllTasks(String programName) {
		DefaultListModel<TaskModel> taskModel = new DefaultListModel<TaskModel>();
		JList<TaskModel> taskList = new JList<TaskModel>(taskModel);
		ProgramModel program = getProgramByName(programName);

		for (TaskModel t : program.getTaskList()) {
			taskModel.addElement(t);
		}
		return taskList;
	}

	public JList<String> getAllLocationsAsString() {
		DefaultListModel<String> locationModel = new DefaultListModel<String>();
		JList<String> locationList = new JList<String>(locationModel);

		for (ProgramModel prog : programList) {
			JList<TaskModel> taskList = getAllTasks(prog.getProgramName());
			for (int i = 0; i < taskList.getModel().getSize(); i++) {
				// Check whether already in list before adding
				String loc = taskList.getModel().getElementAt(i).getLocation();
				if (!loc.equals("") && !findStringMatchInList(loc, locationList)) {
					locationModel.addElement(loc);
				}
			}
		}
		return (locationList);
	}

	public JList<String> getAllTimesAsString() {
		DefaultListModel<String> timeModel = new DefaultListModel<String>();
		ArrayList<Time> timeArray = new ArrayList<Time>();

		for (ProgramModel prog : programList) {
			JList<TaskModel> taskList = getAllTasks(prog.getProgramName());
			for (int i = 0; i < taskList.getModel().getSize(); i++) {
				// Check whether already in list before adding
				Time time = taskList.getModel().getElementAt(i).getTime();
				if (!findTimeMatchInArray(time, timeArray)) {
					timeArray.add(time);
				}
			}
		}
		Collections.sort(timeArray);
		for (int i = 0; i < timeArray.size(); i++)
			timeModel.addElement(Utilities.formatTime(timeArray.get(i)));

		return (new JList<String>(timeModel));
	}

	public JList<Time> getAllTimesByDay(Calendar calendar) {
		DefaultListModel<Time> timeModel = new DefaultListModel<Time>();
		ArrayList<Time> timeArray = new ArrayList<Time>();

		LinkedList<CalendarDayModel> taskList = getAllTasksByDay(calendar);
		for (int taskIdx = 0; taskIdx < taskList.size(); taskIdx++) {
			// Check whether already in list before adding
			Time taskTime = taskList.get(taskIdx).getTask().getTime();
			if (!findTimeMatchInArray(taskTime, timeArray))
				timeArray.add(taskTime);
		}

		Collections.sort(timeArray);
		for (int i = 0; i < timeArray.size(); i++)
			timeModel.addElement(timeArray.get(i));
		JList<Time> timeList = new JList<Time>(timeModel);
		return timeList;
	}

	private int getTaskIndexByName(ProgramModel program, String taskName) {
		int i = 0;

		for (TaskModel t : program.getTaskList()) {
			if (t.getTaskName().equals(taskName))
				return i;
			i++;
		}
		return -1;
	}

	private void updateTaskNameByPerson(String oldTaskName, String newTaskName) {
		for (PersonModel person : personList) {
			for (AssignedTasksModel assignedTask : person.getAssignedTasks()) {
				if (assignedTask.getTaskName().equals(oldTaskName))
					assignedTask.setTaskName(newTaskName);
			}
		}
	}

	private int checkPersonMatchForTaskByDay(PersonModel person, String taskName, Date today, int dayOfWeekIdx,
			int dowInMonthIdx) {
		if (isPersonAvailable(person, today)) {
			LinkedList<AssignedTasksModel> assignedTaskList = person.getAssignedTasks();

			// Check if task is in person's assigned task list for today
			for (AssignedTasksModel assignedTask : assignedTaskList) {
				if (assignedTask.getTaskName().equals(taskName) && assignedTask.getDaysOfWeek()[dayOfWeekIdx]
						&& assignedTask.getWeeksOfMonth()[dowInMonthIdx]) {
					return 0;
				}
			}

			for (SingleInstanceTaskModel singleInstanceTask : person.getSingleInstanceTasks()) {
				// Check if this person is a sub for today
				Calendar subCalendar = singleInstanceTask.getTaskDate();
				if (singleInstanceTask.getTaskName().equals(taskName)
						&& checkForDateAndTimeMatch(today, dayOfWeekIdx, dowInMonthIdx, subCalendar)) {
					return 1;
				}
			}
		}
		return -1;
	}

	private int checkSingleInstanceTaskMatch(SingleInstanceTaskModel singleInstanceTask, String taskName, Date today,
			int dayOfWeekIdx, int dowInMonthIdx) {
		// Check if this person is a sub for today
		Calendar subCalendar = singleInstanceTask.getTaskDate();

		if (singleInstanceTask.getTaskName().equals(taskName)
				&& checkForDateAndTimeMatch(today, dayOfWeekIdx, dowInMonthIdx, subCalendar))
			return 1;
		else
			return -1;
	}

	private boolean checkForDateAndTimeMatch(Date todayDate, int todayDOW, int todayWOM, Calendar calendar) {
		Date calDay = getDay(calendar);
		int calWeekIdx = calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH) - 1;
		int calDayIdx = calendar.get(Calendar.DAY_OF_WEEK) - 1;

		if ((calDay.compareTo(todayDate) == 0) && (calWeekIdx == todayWOM) && (calDayIdx == todayDOW)) {
			return true;
		} else {
			return false;
		}
	}

	private int getPersonCountForTaskByDay(TaskModel task, Date today, int dayOfWeekIdx, int dowInMonthIdx) {
		JList<PersonModel> persons = getAllPersons();
		int count = 0;

		for (int idx = 0; idx < persons.getModel().getSize(); idx++) {
			PersonModel person = persons.getModel().getElementAt(idx);
			// -1 = no match, 0 = assigned task, 1 = single instance task
			if (checkPersonMatchForTaskByDay(person, task.getTaskName(), today, dayOfWeekIdx, dowInMonthIdx) >= 0)
				count++;
		}
		return count;
	}

	private Date getDay(Calendar calendar) {
		try {
			return (dateFormatter.parse((calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.DAY_OF_MONTH)
					+ "/" + calendar.get(Calendar.YEAR)));

		} catch (ParseException e1) {
			return null;
		}
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
		if (today == null)
			return true; // impossible?

		if (!person.getDatesUnavailable().getStartDate().equals("")) {
			try {
				Date startDate = dateFormatter.parse(person.getDatesUnavailable().getStartDate());
				Date endDate = dateFormatter.parse(person.getDatesUnavailable().getEndDate());
				if (today.compareTo(startDate) >= 0 && today.compareTo(endDate) <= 0) {
					// Person unavailable, today is between start and end
					return false;
				} else
					return true;

			} catch (ParseException e) {
				JOptionPane.showMessageDialog(null,
						"Unable to parse " + person.getName() + "'s unavailable start/end dates.",
						"Error parsing dates", JOptionPane.ERROR_MESSAGE);
				return true;
			}
		}
		return true;
	}

	/*
	 * ------- Person data -------
	 */
	public void addPerson(String name, String phone, String email, boolean leader, String notes,
			LinkedList<AssignedTasksModel> assignedTasks, DateRangeModel datesUnavailable) {
		personList.add(new PersonModel(name, phone, email, leader, notes, assignedTasks, datesUnavailable,
				new LinkedList<SingleInstanceTaskModel>()));
		Collections.sort(personList);
	}

	public void updatePerson(String personName, String personPhone, String personEmail, boolean personIsLeader,
			String personNotes, LinkedList<AssignedTasksModel> personAssignedTasks,
			DateRangeModel personDatesUnavailable) {

		int personIdx = getPersonIndexByName(personName);
		if (personIdx != -1) {
			// Merge in the assigned task changes (assigned task list ONLY
			// contains changes!!)
			int taskIdx;
			PersonModel thisPerson = personList.get(personIdx);
			LinkedList<AssignedTasksModel> dbAssignedTaskList = thisPerson.getAssignedTasks();

			for (AssignedTasksModel assignedTask : personAssignedTasks) {
				taskIdx = findAssignedTaskIdx(assignedTask.getTaskName(), dbAssignedTaskList);
				if (taskIdx != -1)
					// Assigned task already in database, so update
					thisPerson.getAssignedTasks().set(taskIdx, assignedTask);
				else
					// New task was assigned, add to database
					thisPerson.getAssignedTasks().add(assignedTask);
			}

			// Now update remaining fields (except one-time assignments)
			thisPerson.setName(personName);
			thisPerson.setPhone(personPhone);
			thisPerson.setEmail(personEmail);
			thisPerson.setLeader(personIsLeader);
			thisPerson.setNotes(personNotes);
			thisPerson.setDatesUnavailable(personDatesUnavailable);

		} else
			JOptionPane.showMessageDialog(null, "Person '" + personName + "' not found!");
	}

	public void addSingleInstanceTask(String personName, Calendar calendar, String taskName, int color) {
		// Get person
		PersonModel person = getPersonByName(personName);

		// Add task to single instance task list
		person.getSingleInstanceTasks().add(new SingleInstanceTaskModel(taskName, calendar, color));
		Collections.sort(person.getSingleInstanceTasks());
	}

	public void renamePerson(String oldName, String newName) {
		int personIdx = getPersonIndexByName(oldName);
		if (personIdx != -1) {
			PersonModel person = personList.get(personIdx);
			person.setName(newName);
			Collections.sort(personList);
		} else
			JOptionPane.showMessageDialog(null, "Person '" + oldName + "' not found!");
	}

	public PersonModel getPersonByName(String name) {
		for (PersonModel p : personList) {
			if (p.getName().equals(name)) {
				return p;
			}
		}
		return null;
	}

	public JList<String> getAllPersonsAsString() {
		DefaultListModel<String> nameModel = new DefaultListModel<String>();

		for (PersonModel p : personList) {
			nameModel.addElement(new String(p.getName()));
		}
		return (new JList<String>(nameModel));
	}

	public JList<PersonModel> getAllPersons() {
		DefaultListModel<PersonModel> personModel = new DefaultListModel<>();
		for (PersonModel p : personList) {
			personModel.addElement(p);
		}
		JList<PersonModel> list = new JList<>(personModel);
		return list;
	}

	public LinkedList<PersonByTaskModel> getAllPersonsList() {
		LinkedList<PersonByTaskModel> personsByTask = new LinkedList<PersonByTaskModel>();
		for (PersonModel p : personList) {
			PersonByTaskModel person = new PersonByTaskModel(p, null, false, 0, null);
			personsByTask.add(person);
		}
		return (LinkedList<PersonByTaskModel>) personsByTask.clone();
	}

	// Return list of all persons assigned to this day, including single
	// instance assignments (subs) and floaters
	public LinkedList<PersonByTaskModel> getPersonsByDay(Calendar calendar) {
		int dayOfWeekInMonthIdx = calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH) - 1;
		int dayOfWeekIdx = calendar.get(Calendar.DAY_OF_WEEK) - 1;
		Date thisDay = getDay(calendar);
		Calendar localCalendar = (Calendar) calendar.clone();

		JList<PersonModel> persons = getAllPersons();
		LinkedList<CalendarDayModel> tasksForToday = getAllTasksByDay(localCalendar);
		LinkedList<PersonByTaskModel> thisDaysPersons = new LinkedList<PersonByTaskModel>();

		for (int i = 0; i < persons.getModel().getSize(); i++) {
			PersonModel pModel = getPersonByName(persons.getModel().getElementAt(i).toString());

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
			for (SingleInstanceTaskModel task : pModel.getSingleInstanceTasks()) {
				if (checkSingleInstanceTaskMatch(task, "", thisDay, dayOfWeekIdx, dayOfWeekInMonthIdx) >= 0) {
					PersonByTaskModel personByTask = new PersonByTaskModel(pModel, null, false, task.getColor(),
							task.getTaskDate());
					thisDaysPersons.add(personByTask);
				}
			}
		}
		return (LinkedList<PersonByTaskModel>) thisDaysPersons.clone();
	}

	// TODO: Currently not used!
	public LinkedList<PersonByTaskModel> getPersonsByDayByTask(Calendar calendar, TaskModel task) {
		int dayOfWeekInMonthIdx = calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH) - 1;
		int dayOfWeekIdx = calendar.get(Calendar.DAY_OF_WEEK) - 1;
		Date thisDay = getDay(calendar);

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
		return (LinkedList<PersonByTaskModel>) thisDaysPersons.clone();
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
		return (LinkedList<PersonByTaskModel>) persons.clone();
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
		return (LinkedList<PersonByTaskModel>) personList.clone();
	}

	public int getNumPersons() {
		return personList.size();
	}

	private int getPersonIndexByName(String personName) {
		int i = 0;

		for (PersonModel p : personList) {
			if (p.getName().equals(personName))
				return i;
			i++;
		}
		return -1;
	}

	private TaskModel findTaskInList(String taskName, LinkedList<CalendarDayModel> dayList) {
		for (CalendarDayModel day : dayList) {
			if (day.getTask().getTaskName().equals(taskName))
				return day.getTask();
		}
		return null;
	}

	private boolean findStringMatchInList(String findString, JList<String> list) {
		for (int i = 0; i < list.getModel().getSize(); i++) {
			if (list.getModel().getElementAt(i).equals(findString))
				return true;
		}
		return false;
	}

	private boolean findTimeMatchInArray(Time findTime, ArrayList<Time> list) {
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).compareTo(findTime) == 0)
				return true;
		}
		return false;
	}

	private int findAssignedTaskIdx(String taskName, LinkedList<AssignedTasksModel> assignedTaskList) {
		int i = 0;

		for (AssignedTasksModel task : assignedTaskList) {
			if (task.getTaskName().equals(taskName))
				return i;
			i++;
		}
		return -1;
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

	public void loadProgramFromFile(File file) throws IOException {
		FileInputStream fis = new FileInputStream(file);
		ObjectInputStream ois = new ObjectInputStream(fis);

		try {
			// Convert from array to program
			ProgramModel[] programs = (ProgramModel[]) ois.readObject();
			for (ProgramModel p : programs) {
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
