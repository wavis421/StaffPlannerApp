package controller;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import gui.TaskEvent;
import model.Database;
import model.Task;

public class Controller {
	Database db = new Database();

	public void addTask(TaskEvent ev) {
		Task task = new Task(ev.getTaskName(), ev.getLocation(), ev.getDayOfWeek(), ev.getWeekOfMonth(), ev.getTime());
		db.addTask(task);
	}

	public Task findTask (Calendar calendar)
	{
		return db.findTask(calendar);
	}
	
	public void saveToFile(File file) throws IOException {
		db.saveToFile(file);
	}

	public void loadFromFile(File file) throws IOException {
		db.loadFromFile(file);
	}
}
