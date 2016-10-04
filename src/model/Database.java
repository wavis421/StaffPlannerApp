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

import gui.TimeComparator;

public class Database {
	private LinkedList<ProgramModel> programList;

	public Database() {
		programList = new LinkedList<ProgramModel>();
	}

	/*
	 * ------- Programs -------
	 */
	public void addProgram(String programName, int defaultColor) {
		System.out.println("Added program to database: " + programName + ", color " + defaultColor);
		LinkedList<TaskModel> taskList = new LinkedList<TaskModel>();
		programList.add(new ProgramModel(programName, defaultColor, taskList));
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
		JList<String> nameList = new JList<String>(nameModel);
		for (ProgramModel p : programList) {
			nameModel.addElement(new String(p.getProgramName()));
		}
		return nameList;
	}

	/*
	 * ------- Task data -------
	 */
	public void addTask(String programName, TaskModel task) {
		System.out.println("Added task to database: " + task.getTaskName() + ", location - " + task.getLocation()
				+ ", DOW - " + task.getDayOfWeek() + ", time - " + task.getTime() + ", colorIdx - " + task.getColor());
		ProgramModel program = getProgramByName(programName);
		program.getTaskList().add(task);
	}

	public void updateTask(String programName, TaskModel task) {
		System.out.println("Update task: " + task.getTaskName() + ", location - " + task.getLocation() + ", DOW - "
				+ task.getDayOfWeek() + ", time - " + task.getTime() + ", color - " + task.getColor());

		ProgramModel program = getProgramByName(programName);
		int taskIdx = getTaskIndexByName(program, task.getTaskName());
		if (taskIdx != -1)
			program.getTaskList().set(taskIdx, task);
		else
			JOptionPane.showMessageDialog(null, "Task '" + task.getTaskName() + "' not found!");
	}

	public void renameTask(String programName, String oldName, String newName) {
		System.out.println("Rename task: " + oldName + " to " + newName);
		int taskIdx;

		ProgramModel program = getProgramByName(programName);
		taskIdx = getTaskIndexByName(program, oldName);
		if (taskIdx != -1) {
			TaskModel task = program.getTaskList().get(taskIdx);
			task.setTaskName(newName);
			program.getTaskList().set(taskIdx, task);
		} else
			JOptionPane.showMessageDialog(null, "Task '" + oldName + "' not found!");
	}

	public void removeTaskFromDay(Calendar calendar, String taskName) {
		// TBD
	}

	public TaskModel getTaskByName(String programName, String taskName) {
		ProgramModel program = getProgramByName(programName);
		for (TaskModel t : program.getTaskList()) {
			if (t.getTaskName().equals(taskName)) {
				return t;
			}
		}
		return null;
	}

	public LinkedList<TaskModel> getTasksByDay(Calendar calendar) {
		int dayOfWeekInMonthIdx = calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH) - 1;
		int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

		LinkedList<TaskModel> thisMonthTasks = new LinkedList<TaskModel>();
		for (ProgramModel p : programList) {
			for (TaskModel t : p.getTaskList()) {
				if ((t.getDayOfWeek() == dayOfWeek) && (t.getWeekOfMonth()[dayOfWeekInMonthIdx])) {
					thisMonthTasks.add(t);
				}
			}
		}
		Collections.sort(thisMonthTasks, new TimeComparator());
		return thisMonthTasks;
	}

	/*
	 * public List<TaskModel> getAllTasks() { 
	 * 	return Collections.unmodifiableList(taskList); 
	 * }
	 */

	public JList<String> getAllTasksAsString(String programName) {
		DefaultListModel<String> nameModel = new DefaultListModel<String>();
		JList<String> nameList = new JList<String>(nameModel);
		ProgramModel program = getProgramByName(programName);

		for (TaskModel t : program.getTaskList()) {
			nameModel.addElement(new String(t.getTaskName()));
		}
		return nameList;
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

	/*
	 * ------- File save/restore items -------
	 */
	public void saveToFile(File file) throws IOException {
		FileOutputStream fos = new FileOutputStream(file);
		ObjectOutputStream oos = new ObjectOutputStream(fos);

		// Convert to array
		ProgramModel[] programs = programList.toArray(new ProgramModel[programList.size()]);

		oos.writeObject(programs);
		oos.close();
	}

	public void loadFromFile(File file) throws IOException {
		FileInputStream fis = new FileInputStream(file);
		ObjectInputStream ois = new ObjectInputStream(fis);

		try {
			// Convert from array to people
			ProgramModel[] programs = (ProgramModel[]) ois.readObject();
			programList.clear();
			programList.addAll(Arrays.asList(programs));

		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ois.close();
	}
}
