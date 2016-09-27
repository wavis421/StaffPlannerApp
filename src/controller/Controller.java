package controller;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.LinkedList;

import gui.TaskEvent;
import model.Database;
import model.TaskModel;

public class Controller {
	Database db = new Database();

	public void addTask(TaskEvent ev) {
		TaskModel task = new TaskModel(ev.getTaskName(), ev.getLocation(), ev.getDayOfWeek(), ev.getWeekOfMonth(), ev.getTime());
		db.addTask(task);
	}
	
	public void updateTask(TaskEvent ev) {
		TaskModel task = new TaskModel (ev.getTaskName(), ev.getLocation(), ev.getDayOfWeek(), ev.getWeekOfMonth(), ev.getTime());
		db.updateTask(task);
	}
	
	public void removeTaskFromDay (Calendar calendar, String taskName) {
		db.removeTaskFromDay (calendar, taskName);
	}
	
	public TaskModel getTaskByName (String taskName) {
		return db.getTaskByName(taskName);
	}
	
	public LinkedList<TaskModel> getTasksByDay (Calendar calendar)
	{
		return db.getTasksByDay(calendar);
	}
	
	public void saveToFile(File file) throws IOException {
		db.saveToFile(file);
	}

	public void loadFromFile(File file) throws IOException {
		db.loadFromFile(file);
	}
}