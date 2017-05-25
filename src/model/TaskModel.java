package model;

import java.io.Serializable;

public class TaskModel implements Serializable, Comparable<TaskModel> {
	private static final long serialVersionUID = 12340002L;
	private int taskID, programID;
	private String taskName;
	private TimeModel time;
	private String location;
	private int numLeadersReqd;
	private int totalPersonsReqd;
	private boolean[] dayOfWeek;
	private boolean[] weekOfMonth;
	private int color;
	private boolean isFocus = false;

	public TaskModel(int taskID, int programID, String taskName, String location, int numLeadersReqd,
			int totalPersonsReqd, boolean[] dayOfWeek, boolean[] weekOfMonth, TimeModel time, int color) {
		this.taskID = taskID;
		this.programID = programID;
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

	public int getTaskID() {
		return taskID;
	}

	public int getProgramID() {
		return programID;
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

	public boolean getIsFocus() {
		return isFocus;
	}

	public void setIsFocus(boolean value) {
		isFocus = value;
	}

	@Override
	public int compareTo(TaskModel otherTask) {
		return (this.getTime().compareTo(otherTask.getTime()));
	}
}
