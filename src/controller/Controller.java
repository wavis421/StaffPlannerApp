package controller;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.LinkedList;

import javax.swing.JList;

import gui.ProgramEvent;
import gui.TaskEvent;
import model.Database;
import model.ProgramModel;
import model.TaskModel;

public class Controller {
	Database db = new Database();

	/*
	 * ------- Programs -------
	 */
	public void addProgram(ProgramEvent ev) {
		db.addProgram(ev.getProgramName(), ev.getDefaultColor());
	}

	public ProgramModel getProgramByName(String programName) {
		return db.getProgramByName(programName);
	}

	public JList<String> getAllProgramsAsString() {
		return db.getAllProgramsAsString();
	}

	/*
	 * ------- Task data -------
	 */
	public void addTask(TaskEvent ev) {
		TaskModel task = new TaskModel(ev.getTaskName(), ev.getLocation(), ev.getDayOfWeek(),
				ev.getWeekOfMonth(), ev.getTime(), ev.getEndDate(), ev.getColor());
		db.addTask(ev.getProgramName(), task);
	}

	public void updateTask(TaskEvent ev) {
		TaskModel task = new TaskModel(ev.getTaskName(), ev.getLocation(), ev.getDayOfWeek(),
				ev.getWeekOfMonth(), ev.getTime(), ev.getEndDate(), ev.getColor());
		db.updateTask(ev.getProgramName(), task);
	}

	public void renameTask(String programName, String oldName, String newName) {
		db.renameTask(programName, oldName, newName);
	}

	public void removeTaskFromDay(Calendar calendar, String taskName) {
		db.removeTaskFromDay(calendar, taskName);
	}

	public TaskModel getTaskByName(String programName, String taskName) {
		return db.getTaskByName(programName, taskName);
	}

	public LinkedList<TaskModel> getTasksByDayByProgram(Calendar calendar, JList<String> programs) {
		return db.getTasksByDayByProgram(calendar, programs);
	}
	
	public LinkedList<TaskModel> getAllTasksByDay(Calendar calendar) {
		return db.getAllTasksByDay(calendar);
	}

	/*
	 * public List<TaskModel> getAllTasks() { return db.getAllTasks(); }
	 */

	public JList<TaskModel> getAllTasks(String programName) {
		return db.getAllTasks(programName);
	}
	
	/*
	public JList<String> getAllTasksAsString(String programName) {
		return db.getAllTasksAsString(programName);
	}
	*/

	/*
	 * ------- File save/restore items -------
	 */
	public void saveToFile(File file) throws IOException {
		db.saveToFile(file);
	}

	public void loadFromFile(File file) throws IOException {
		db.loadFromFile(file);
	}
}
