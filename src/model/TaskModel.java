package model;

import java.io.Serializable;

public class TaskModel implements Serializable, Comparable<TaskModel> {
	private static final long serialVersionUID = 12340001L;
	private String taskName;
	private TimeModel time;
	private String location;
	private int numLeadersReqd;
	private int totalPersonsReqd;
	private boolean[] dayOfWeek;
	private boolean[] weekOfMonth;
	private int color;

	public TaskModel(String taskName, String location, int numLeadersReqd, int totalPersonsReqd, boolean[] dayOfWeek,
			boolean[] weekOfMonth, TimeModel time, int color) {
		this.taskName = taskName;
		this.location = location;
		this.numLeadersReqd = numLeadersReqd;
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

	public TimeModel getTime() {
		return time;
	}

	public String getLocation() {
		return location;
	}

	public int getNumLeadersReqd() {
		return numLeadersReqd;
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

	@Override
	public int compareTo(TaskModel otherTask) {
		return (this.getTime().compareTo(otherTask.getTime()));
	}
}
