package model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JOptionPane;

import gui.PersonComparator;
import gui.ProgramComparator;
import gui.TimeComparator;

public class Database {
	private LinkedList<ProgramModel> programList;
	private LinkedList<PersonModel> personList;

	public Database() {
		programList = new LinkedList<ProgramModel>();
		personList = new LinkedList<PersonModel>();
	}

	/*
	 * ------- Programs -------
	 */
	public void addProgram(String programName, String endDate) {
		LinkedList<TaskModel> taskList = new LinkedList<TaskModel>();
		programList.add(new ProgramModel(programName, endDate, taskList));
		Collections.sort(programList, new ProgramComparator());
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

	/*
	 * ------- Task data -------
	 */
	public void addTask(String programName, TaskModel task) {
		ProgramModel program = getProgramByName(programName);
		program.getTaskList().add(task);
		Collections.sort(program.getTaskList(), new TimeComparator());
	}

	public void updateTask(String programName, TaskModel task) {
		ProgramModel program = getProgramByName(programName);
		int taskIdx = getTaskIndexByName(program, task.getTaskName());
		if (taskIdx != -1) {
			program.getTaskList().set(taskIdx, task);
			Collections.sort(program.getTaskList(), new TimeComparator());
		} else
			JOptionPane.showMessageDialog(null, "Task '" + task.getTaskName() + "' not found!");
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
			JOptionPane.showMessageDialog(null, "Task '" + oldName + "' not found!");
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

	public LinkedList<CalendarDayModel> getTasksByDayByProgram(Calendar calendar, JList<String> programList) {
		int dayOfWeekInMonthIdx = calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH) - 1;
		int dayOfWeekIdx = calendar.get(Calendar.DAY_OF_WEEK) - 1;

		LinkedList<CalendarDayModel> thisDaysTasks = new LinkedList<CalendarDayModel>();
		for (int i = 0; i < programList.getModel().getSize(); i++) {
			ProgramModel pModel = getProgramByName(programList.getModel().getElementAt(i));
			for (TaskModel t : pModel.getTaskList()) {
				if ((t.getDayOfWeek()[dayOfWeekIdx]) && (t.getWeekOfMonth()[dayOfWeekInMonthIdx])) {
					thisDaysTasks.add(
							new CalendarDayModel(t, getPersonCountForTaskByDay(t, dayOfWeekIdx, dayOfWeekInMonthIdx)));
				}
			}
		}
		return thisDaysTasks;
	}

	public LinkedList<CalendarDayModel> getTasksByDayByPerson(Calendar calendar, JList<String> personList) {
		int dayOfWeekInMonthIdx = calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH) - 1;
		int dayOfWeekIdx = calendar.get(Calendar.DAY_OF_WEEK) - 1;

		LinkedList<CalendarDayModel> thisDaysTasks = new LinkedList<CalendarDayModel>();
		for (int i = 0; i < personList.getModel().getSize(); i++) {
			PersonModel pModel = getPersonByName(personList.getModel().getElementAt(i));
			for (int j = 0; j < pModel.getAssignedTasks().size(); j++) {
				AssignedTasksModel task = (AssignedTasksModel) pModel.getAssignedTasks().get(j);

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
								getPersonCountForTaskByDay(t, dayOfWeekIdx, dayOfWeekInMonthIdx)));
					}
				}
			}
		}
		return thisDaysTasks;
	}

	public LinkedList<CalendarDayModel> getAllTasksByDay(Calendar calendar) {
		int dayOfWeekInMonthIdx = calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH) - 1;
		int dayOfWeekIdx = calendar.get(Calendar.DAY_OF_WEEK) - 1;

		LinkedList<CalendarDayModel> thisDaysTasks = new LinkedList<CalendarDayModel>();
		for (ProgramModel prog : programList) {
			for (TaskModel task : prog.getTaskList()) {
				if ((task.getDayOfWeek()[dayOfWeekIdx]) && (task.getWeekOfMonth()[dayOfWeekInMonthIdx])) {
					thisDaysTasks.add(new CalendarDayModel(task,
							getPersonCountForTaskByDay(task, dayOfWeekIdx, dayOfWeekInMonthIdx)));
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
		DefaultListModel<TaskModel> taskModel = new DefaultListModel<>();
		JList<TaskModel> taskList = new JList<>(taskModel);
		ProgramModel program = getProgramByName(programName);

		for (TaskModel t : program.getTaskList()) {
			taskModel.addElement(t);
		}
		return taskList;
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
	
	private int getPersonCountForTaskByDay(TaskModel task, int dayOfWeekIdx, int dowInMonthIdx) {
		JList<PersonModel> personList = getAllPersons();
		int count = 0;

		for (int idx = 0; idx < personList.getModel().getSize(); idx++) {
			LinkedList<AssignedTasksModel> assignedTaskList = personList.getModel().getElementAt(idx)
					.getAssignedTasks();
			for (AssignedTasksModel assignedTask : assignedTaskList) {
				if (assignedTask.getTaskName().equals(task.getTaskName()) && assignedTask.getDaysOfWeek()[dayOfWeekIdx]
						&& assignedTask.getWeeksOfMonth()[dowInMonthIdx]) {
					count++;
					break;
				}
			}
		}
		return count;
	}

	/*
	 * ------- Person -------
	 */
	public void addPerson(String name, String phone, String email, boolean staff, String notes,
			LinkedList<AssignedTasksModel> assignedTasks) {
		personList.add(new PersonModel(name, phone, email, staff, notes, assignedTasks));
		Collections.sort(personList, new PersonComparator());
	}

	public void updatePerson(PersonModel person) {
		int personIdx = getPersonIndexByName(person.getName());
		if (personIdx != -1)
			personList.set(personIdx, person);
		else
			JOptionPane.showMessageDialog(null, "Person '" + person.getName() + "' not found!");
	}

	public void renamePerson(String oldName, String newName) {
		int personIdx = getPersonIndexByName(oldName);
		if (personIdx != -1) {
			PersonModel person = personList.get(personIdx);
			person.setName(newName);
			Collections.sort(personList, new PersonComparator());
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

	private int getPersonIndexByName(String personName) {
		int i = 0;

		for (PersonModel p : personList) {
			if (p.getName().equals(personName))
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
			JOptionPane.showMessageDialog(null, "Failed to load Program file -- invalid format.");
			// e.printStackTrace();
		}
		ois.close();
	}

	public void saveStaffToFile(File file) throws IOException {
		FileOutputStream fos = new FileOutputStream(file);
		ObjectOutputStream oos = new ObjectOutputStream(fos);

		// Convert to array
		PersonModel[] staff = personList.toArray(new PersonModel[personList.size()]);

		oos.writeObject(staff);
		oos.close();
	}

	public void loadStaffFromFile(File file) throws IOException {
		FileInputStream fis = new FileInputStream(file);
		ObjectInputStream ois = new ObjectInputStream(fis);

		try {
			// Convert from array
			PersonModel[] staff = (PersonModel[]) ois.readObject();
			personList.clear();
			personList.addAll(Arrays.asList(staff));

		} catch (ClassNotFoundException e) {
			JOptionPane.showMessageDialog(null, "Failed to load Staff file -- invalid format.");
			// e.printStackTrace();
		}
		ois.close();
	}
}
