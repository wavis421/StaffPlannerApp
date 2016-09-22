package model;

import java.io.Serializable;
import java.sql.Time;

public class TaskModel implements Serializable {
	private String taskName;
	private Time time;
	private String location;
	private int dayOfWeek;
	private boolean[] weekOfMonth;
	
	public TaskModel (String taskName, String location, int dayOfWeek, boolean[] weekOfMonth, Time time) {
		this.taskName = taskName;
		this.location = location;
		this.dayOfWeek = dayOfWeek;
		this.weekOfMonth = weekOfMonth;
		this.time = time;
	}

	public String getTaskName() {
		return taskName;
	}

	public Time getTime() {
		return time;
	}

	public String getLocation() {
		return location;
	}

	public int getDayOfWeek() {
		return dayOfWeek;
	}

	public boolean[] getWeekOfMonth() {
		return weekOfMonth;
	}
}
