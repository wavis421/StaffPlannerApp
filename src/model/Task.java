package model;

import java.io.Serializable;
import java.sql.Time;

public class Task implements Serializable {
	private String taskName;
	private Time time;
	private String location;
	private int dayOfWeek;
	private int weekOfMonth;
	
	public Task (String taskName, String location, int dayOfWeek, int weekOfMonth, Time time) {
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

	public int getWeekOfMonth() {
		return weekOfMonth;
	}
}
