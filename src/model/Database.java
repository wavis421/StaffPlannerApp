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

public class Database {
	private List<Task> taskList;

	public Database() {
		taskList = new LinkedList<Task>();
	}

	public void addTask(Task task) {
		System.out.println("Added task to database: " + task.getTaskName() + ", location - " + task.getLocation()
						+ "DOW = " + task.getDayOfWeek() + "WOM = " + task.getWeekOfMonth() + ", time - " + task.getTime());
		taskList.add(task);
	}

	public Task findTask(Calendar calendar) {
		for (Task t : taskList)
		{
			if (false)
			{
				return t;
			}
		}
		return null;
	}

	public List<Task> getTasks() {
		return Collections.unmodifiableList(taskList);
	}

	public void saveToFile(File file) throws IOException {
		FileOutputStream fos = new FileOutputStream(file);
		ObjectOutputStream oos = new ObjectOutputStream(fos);

		// Convert to array
		Task[] tasks = taskList.toArray(new Task[taskList.size()]);

		oos.writeObject(tasks);
		oos.close();
	}

	public void loadFromFile(File file) throws IOException {
		FileInputStream fis = new FileInputStream(file);
		ObjectInputStream ois = new ObjectInputStream(fis);

		try {
			// Convert from array to people
			Task[] tasks = (Task[]) ois.readObject();
			taskList.clear();
			taskList.addAll(Arrays.asList(tasks));

		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ois.close();
	}

}
