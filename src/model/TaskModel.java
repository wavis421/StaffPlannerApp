package model;

import java.io.Serializable;
import java.sql.Time;

public class TaskModel implements Serializable {
	private static final long serialVersionUID = 12340001L;
	private String taskName;
	private Time time;
	private String location;
	private int numStaffReqd;
	private int totalPersonsReqd;
	private boolean[] dayOfWeek;
	private boolean[] weekOfMonth;
	private int color;

	public TaskModel(String taskName, String location, int numStaffReqd, int totalPersonsReqd, boolean[] dayOfWeek,
			boolean[] weekOfMonth, Time time, int color) {
		this.taskName = taskName;
		this.location = location;
		this.numStaffReqd = numStaffReqd;
		this.totalPersonsReqd = totalPersonsReqd;
		this.dayOfWeek = dayOfWeek;
		this.weekOfMonth = weekOfMonth;
		this.time = time;
		this.color = color;
	}

	public String toString() {
		return taskName;
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

	public int getNumStaffReqd() {
		return numStaffReqd;
	}

	public int getTotalPersonsReqd() {
		return totalPersonsReqd;
	}

	public boolean[] getDayOfWeek() {
		return dayOfWeek;
	}

	public boolean[] getWeekOfMonth() {
		return weekOfMonth;
	}

	public int getColor() {
		return color;
	}
}
