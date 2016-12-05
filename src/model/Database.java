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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JOptionPane;

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

	public LinkedList<CalendarDayModel> getTasksByDayByProgram(Calendar calendar, JList<String> programList) {
		int dayOfWeekInMonthIdx = calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH) - 1;
		int dayOfWeekIdx = calendar.get(Calendar.DAY_OF_WEEK) - 1;
		Date thisDay = getDay(calendar);

		LinkedList<CalendarDayModel> thisDaysTasks = new LinkedList<CalendarDayModel>();
		for (int i = 0; i < programList.getModel().getSize(); i++) {
			ProgramModel program = getProgramByName(programList.getModel().getElementAt(i));
			if (isProgramExpired(thisDay, program))
				continue;

			for (TaskModel t : program.getTaskList()) {
				if ((t.getDayOfWeek()[dayOfWeekIdx]) && (t.getWeekOfMonth()[dayOfWeekInMonthIdx])) {
					thisDaysTasks.add(new CalendarDayModel(t,
							getPersonCountForTaskByDay(t, thisDay, dayOfWeekIdx, dayOfWeekInMonthIdx)));
				}
			}
		}
		return thisDaysTasks;
	}

	public LinkedList<CalendarDayModel> getTasksByDayByPerson(Calendar calendar, JList<String> persons) {
		int dayOfWeekInMonthIdx = calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH) - 1;
		int dayOfWeekIdx = calendar.get(Calendar.DAY_OF_WEEK) - 1;
		Date thisDay = getDay(calendar);

		LinkedList<CalendarDayModel> thisDaysTasks = new LinkedList<CalendarDayModel>();
		for (int i = 0; i < persons.getModel().getSize(); i++) {
			PersonModel pModel = getPersonByName(persons.getModel().getElementAt(i));
			if (!isPersonAvailable(pModel, thisDay))
				continue;

			for (int j = 0; j < pModel.getAssignedTasks().size(); j++) {
				AssignedTasksModel task = (AssignedTasksModel) pModel.getAssignedTasks().get(j);
				if (isProgramExpired(thisDay, getProgramByName(task.getProgramName())))
					continue;

				boolean alreadyInList = false;
				for (int k = 0; k < thisDaysTasks.size(); k++) {
					if (thisDaysTasks.get(k).getTask().getTaskName().equals(task.getTaskName())) {
						alreadyInList = true;
						break;
					}
				}

				if (!alreadyInList) {
					boolean[] daysOfWeek = task.getDaysOfWeek();
					boolean[] weeksOfMonth = task.getWeeksOfMonth();
					TaskModel t = getTaskByName(task.getProgramName(), task.getTaskName());
					if ((daysOfWeek[dayOfWeekIdx]) && (weeksOfMonth[dayOfWeekInMonthIdx])) {
						thisDaysTasks.add(new CalendarDayModel(t,
								getPersonCountForTaskByDay(t, thisDay, dayOfWeekIdx, dayOfWeekInMonthIdx)));
					}
				}
			}
		}
		return thisDaysTasks;
	}

	public LinkedList<CalendarDayModel> getTasksByDayByIncompleteRoster(Calendar calendar) {
		int dayOfWeekInMonthIdx = calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH) - 1;
		int dayOfWeekIdx = calendar.get(Calendar.DAY_OF_WEEK) - 1;
		Date thisDay = getDay(calendar);
		int personCount;

		LinkedList<CalendarDayModel> thisDaysTasks = new LinkedList<CalendarDayModel>();
		for (ProgramModel prog : programList) {
			if (isProgramExpired(thisDay, prog))
				continue;

			for (TaskModel task : prog.getTaskList()) {
				if ((task.getDayOfWeek()[dayOfWeekIdx]) && (task.getWeekOfMonth()[dayOfWeekInMonthIdx])) {
					personCount = getPersonCountForTaskByDay(task, thisDay, dayOfWeekIdx, dayOfWeekInMonthIdx);
					if (personCount < task.getTotalPersonsReqd())
						thisDaysTasks.add(new CalendarDayModel(task, personCount));
				}
			}
		}
		return thisDaysTasks;
	}

	public LinkedList<CalendarDayModel> getTasksByDayByLocation(Calendar calendar, JList<String> locations) {
		LinkedList<CalendarDayModel> matchingTasks = getAllTasksByDay(calendar);

		for (int taskIdx = 0; taskIdx < matchingTasks.size(); taskIdx++) {
			String taskLoc = matchingTasks.get(taskIdx).getTask().getLocation();
			boolean match = false;
			for (int locIdx = 0; locIdx < locations.getModel().getSize(); locIdx++) {
				if (taskLoc.equals(locations.getModel().getElementAt(locIdx))) {
					match = true;
					break;
				}
			}
			if (!match) {
				// No match in location list, so remove; adjust loop index to
				// handle element deletion
				matchingTasks.remove(taskIdx);
				taskIdx--;
			}
		}
		return matchingTasks;
	}

	public LinkedList<CalendarDayModel> getTasksByDayByTime(Calendar calendar, JList<String> timeList) {
		LinkedList<CalendarDayModel> matchingTasks = getAllTasksByDay(calendar);
		Collections.sort(matchingTasks, new TimeComparator());

		for (int taskIdx = 0; taskIdx < matchingTasks.size(); taskIdx++) {
			String taskTime = formatTime(matchingTasks.get(taskIdx).getTask().getTime());
			boolean match = false;
			for (int timeIdx = 0; timeIdx < timeList.getModel().getSize(); timeIdx++) {
				if (taskTime.equals(timeList.getModel().getElementAt(timeIdx))) {
					match = true;
					break;
				}
			}
			if (!match) {
				// No match in timeList, so remove; adjust loop index to handle
				// element deletion
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
							getPersonCountForTaskByDay(task, thisDay, dayOfWeekIdx, dayOfWeekInMonthIdx)));
				}
			}
		}
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
		JList<String> timeList = new JList<String>(timeModel);

		for (ProgramModel prog : programList) {
			JList<TaskModel> taskList = getAllTasks(prog.getProgramName());
			for (int i = 0; i < taskList.getModel().getSize(); i++) {
				// Check whether already in list before adding
				String time = formatTime(taskList.getModel().getElementAt(i).getTime());
				if (!findStringMatchInList(time, timeList)) {
					timeModel.addElement(time);
				}
			}
		}
		return (timeList);
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

	private int getPersonCountForTaskByDay(TaskModel task, Date today, int dayOfWeekIdx, int dowInMonthIdx) {
		JList<PersonModel> persons = getAllPersons();
		int count = 0;

		for (int idx = 0; idx < persons.getModel().getSize(); idx++) {
			PersonModel person = persons.getModel().getElementAt(idx);
			if (isPersonAvailable(person, today)) {
				LinkedList<AssignedTasksModel> assignedTaskList = person.getAssignedTasks();
				for (AssignedTasksModel assignedTask : assignedTaskList) {
					if (assignedTask.getTaskName().equals(task.getTaskName())
							&& assignedTask.getDaysOfWeek()[dayOfWeekIdx]
							&& assignedTask.getWeeksOfMonth()[dowInMonthIdx]) {
						count++;
						break;
					}
				}
			}
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

	private String formatTime(Time time) {
		// Time format for hour 1 - 12 and AM/PM field
		SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");

		// Set time and add an hour to convert from 0-11 to 1-12
		Calendar cal = Calendar.getInstance();
		cal.setTime(time);
		cal.add(Calendar.HOUR, 1);

		// If hour transitioned to 12:00 am/pm, then switch the AM/PM
		int hour = cal.get(Calendar.HOUR);
		if (hour == 0 || hour == 12)
			cal.set(Calendar.AM_PM, cal.get(Calendar.AM_PM) == Calendar.AM ? Calendar.PM : Calendar.AM);

		return timeFormat.format(cal.getTime());
	}

	/*
	 * ------- Person data -------
	 */
	public void addPerson(String name, String phone, String email, boolean leader, String notes,
			LinkedList<AssignedTasksModel> assignedTasks, DateRangeModel datesUnavailable) {
		personList.add(new PersonModel(name, phone, email, leader, notes, assignedTasks, datesUnavailable));
		Collections.sort(personList);
	}

	public void updatePerson(PersonModel updatedPerson) {
		int personIdx = getPersonIndexByName(updatedPerson.getName());

		if (personIdx != -1) {
			// Merge in the assigned task changes (assigned task list ONLY
			// contains changes!!)
			int taskIdx;
			PersonModel thisPerson = personList.get(personIdx);
			LinkedList<AssignedTasksModel> dbAssignedTaskList = thisPerson.getAssignedTasks();

			for (AssignedTasksModel assignedTask : updatedPerson.getAssignedTasks()) {
				taskIdx = findAssignedTaskIdx(assignedTask.getTaskName(), dbAssignedTaskList);
				if (taskIdx != -1)
					// Assigned task already in database, so update
					thisPerson.getAssignedTasks().set(taskIdx, assignedTask);
				else
					// New task was assigned, add to database
					thisPerson.getAssignedTasks().add(assignedTask);
			}

			// Now update remaining fields
			thisPerson.setName(updatedPerson.getName());
			thisPerson.setPhone(updatedPerson.getPhone());
			thisPerson.setEmail(updatedPerson.getEmail());
			thisPerson.setLeader(updatedPerson.isLeader());
			thisPerson.setNotes(updatedPerson.getNotes());
			thisPerson.setDatesUnavailable(updatedPerson.getDatesUnavailable());

		} else
			JOptionPane.showMessageDialog(null, "Person '" + updatedPerson.getName() + "' not found!");
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

	public LinkedList<PersonModel> getAllPersonsList() {
		LinkedList<PersonModel> persons = new LinkedList<PersonModel>();
		for (PersonModel p : personList) {
			persons.add(p);
		}
		return persons;
	}

	public LinkedList<PersonModel> getPersonsByDay(Calendar calendar) {
		int dayOfWeekInMonthIdx = calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH) - 1;
		int dayOfWeekIdx = calendar.get(Calendar.DAY_OF_WEEK) - 1;
		Date thisDay = getDay(calendar);

		JList<PersonModel> persons = getAllPersons();
		LinkedList<CalendarDayModel> tasksForToday = getAllTasksByDay(calendar);
		LinkedList<PersonModel> thisDaysPersons = new LinkedList<PersonModel>();

		for (int i = 0; i < persons.getModel().getSize(); i++) {
			PersonModel pModel = getPersonByName(persons.getModel().getElementAt(i).toString());
			if (!isPersonAvailable(pModel, thisDay))
				continue;

			for (int j = 0; j < pModel.getAssignedTasks().size(); j++) {
				AssignedTasksModel assignedTask = (AssignedTasksModel) pModel.getAssignedTasks().get(j);
				TaskModel task = findTaskInList(assignedTask.getTaskName(), tasksForToday);
				if (task != null) {
					// Got a task match!! Check if today is assigned.
					if ((assignedTask.getDaysOfWeek()[dayOfWeekIdx])
							&& (assignedTask.getWeeksOfMonth()[dayOfWeekInMonthIdx])) {
						thisDaysPersons.add(pModel);
						break;
					}
				}
			}
		}
		return thisDaysPersons;
	}

	public LinkedList<PersonModel> getPersonsByDayByTask(Calendar calendar, String taskName) {
		int dayOfWeekInMonthIdx = calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH) - 1;
		int dayOfWeekIdx = calendar.get(Calendar.DAY_OF_WEEK) - 1;
		Date thisDay = getDay(calendar);

		JList<PersonModel> persons = getAllPersons();
		LinkedList<PersonModel> thisDaysPersons = new LinkedList<PersonModel>();

		for (int i = 0; i < persons.getModel().getSize(); i++) {
			PersonModel pModel = getPersonByName(persons.getModel().getElementAt(i).toString());
			if (!isPersonAvailable(pModel, thisDay))
				continue;

			for (int j = 0; j < pModel.getAssignedTasks().size(); j++) {
				AssignedTasksModel assignedTask = (AssignedTasksModel) pModel.getAssignedTasks().get(j);
				if (taskName.equals(assignedTask.getTaskName())) {
					// Got a task match!! Check if today is assigned.
					if ((assignedTask.getDaysOfWeek()[dayOfWeekIdx])
							&& (assignedTask.getWeeksOfMonth()[dayOfWeekInMonthIdx])) {
						thisDaysPersons.add(pModel);
						break;
					}
				}
			}
		}
		return thisDaysPersons;
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
