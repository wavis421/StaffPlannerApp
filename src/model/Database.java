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
import java.util.List;

import javax.swing.JOptionPane;

public class Database {
	private List<TaskModel> taskList;

	public Database() {
		taskList = new LinkedList<TaskModel>();
	}

	public void addTask(TaskModel task) {
		System.out.println("Added task to database: " + task.getTaskName() + ", location - " + task.getLocation()
				+ ", DOW = " + task.getDayOfWeek() + ", time - " + task.getTime());
		taskList.add(task);
	}

	public void updateTask(TaskModel task) {
		System.out.println("Update task: " + task.getTaskName() + ", location - " + task.getLocation() + ", DOW = "
				+ task.getDayOfWeek() + ", time - " + task.getTime());

		int taskIdx = getIndexByName(task.getTaskName());
		if (taskIdx != -1)
			taskList.set(taskIdx, task);
		else
			JOptionPane.showMessageDialog(null, "Task '" + task.getTaskName() + "' not found!");
	}

	public void removeTaskFromDay (Calendar calendar, String taskName) {
		// TBD
	}
	
	public TaskModel getTaskByName(String taskName) {
		for (TaskModel t : taskList) {
			if (t.getTaskName().equals(taskName)) {
				return t;
			}
		}
		return null;
	}

	public LinkedList<TaskModel> getTasksByDay(Calendar calendar) {
		int weekOfMonthIdx = calendar.get(Calendar.WEEK_OF_MONTH) - 1;

		LinkedList<TaskModel> thisMonthTasks = new LinkedList<TaskModel>();
		for (TaskModel t : taskList) {
			System.out.println("Task: " + t.getTaskName() + ", DOW = " + t.getDayOfWeek());
			if ((t.getDayOfWeek() == calendar.get(Calendar.DAY_OF_WEEK)) && (t.getWeekOfMonth()[weekOfMonthIdx])) {
				thisMonthTasks.add(t);
			}
		}
		return thisMonthTasks;
	}

	public List<TaskModel> getAllTasks() {
		return Collections.unmodifiableList(taskList);
	}

	private int getIndexByName(String taskName) {
		int i = 0;
		for (TaskModel t : taskList) {
			if (t.getTaskName().equals(taskName))
				return i;
			i++;
		}
		return -1;
	}

	public void saveToFile(File file) throws IOException {
		FileOutputStream fos = new FileOutputStream(file);
		ObjectOutputStream oos = new ObjectOutputStream(fos);

		// Convert to array
		TaskModel[] tasks = taskList.toArray(new TaskModel[taskList.size()]);

		oos.writeObject(tasks);
		oos.close();
	}

	public void loadFromFile(File file) throws IOException {
		FileInputStream fis = new FileInputStream(file);
		ObjectInputStream ois = new ObjectInputStream(fis);

		try {
			// Convert from array to people
			TaskModel[] tasks = (TaskModel[]) ois.readObject();
			taskList.clear();
			taskList.addAll(Arrays.asList(tasks));

		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ois.close();
	}
}
