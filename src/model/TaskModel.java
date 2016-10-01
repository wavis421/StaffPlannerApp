package model;

import java.io.Serializable;
import java.sql.Time;

public class TaskModel implements Serializable {
	private String taskName;
	private Time time;
	private String location;
	private int dayOfWeek;
	private boolean[] weekOfMonth;
	private String endDate;
	private int color;

	public TaskModel(String taskName, String location, int dayOfWeek, boolean[] weekOfMonth, Time time, String endDate,
			int color) {
		this.taskName = taskName;
		this.location = location;
		this.dayOfWeek = dayOfWeek;
		this.weekOfMonth = weekOfMonth;
		this.time = time;
		this.endDate = endDate;
		this.color = color;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public Time getTime() {
		return time;
	}

	public String getLocation() {
		return location;
	}

	public String getEndDate() {
		return endDate;
	}

	public int getDayOfWeek() {
		return dayOfWeek;
	}

	public boolean[] getWeekOfMonth() {
		return weekOfMonth;
	}

	public int getColor() {
		return color;
	}
}
