package model;

import java.util.Calendar;

import utilities.Utilities;

public class SingleInstanceTaskModel implements Comparable<SingleInstanceTaskModel> {
	private int taskID;
	private String taskName;
	private String programName;
	private Calendar taskDate;
	private int color;
	private boolean isFocus = false;
	private int elementStatus;

	public SingleInstanceTaskModel(int taskID, String programName, String taskName, Calendar taskDate, int color) {
		this.taskID = taskID;
		this.programName = programName;
		this.taskName = taskName;
		this.elementStatus = ListStatus.elementAssigned();

		taskDate.set(Calendar.SECOND, 0);
		taskDate.set(Calendar.MILLISECOND, 0);
		this.taskDate = taskDate;

		// Note: Color parameter only valid when taskName is blank
		this.color = color;
	}

	public String toString() {
		if (taskName == null || taskName.equals("")) {
			return "Floater on " + Utilities.getDisplayDate(taskDate) + " at " + Utilities.formatTime(taskDate);
		} else {
			return taskName + " on " + Utilities.getDisplayDate(taskDate);
		}
	}

	public int getTaskID() {
		return taskID;
	}

	public String getProgramName() {
		return programName;
	}

	public String getTaskName() {
		return taskName;
	}

	public Calendar getTaskDate() {
		return taskDate;
	}

	public int getColor() {
		return color;
	}

	public int getElementStatus() {
		return elementStatus;
	}

	public void setElementStatus(int elementStatus) {
		this.elementStatus = elementStatus;
	}

	public boolean getIsFocus() {
		return isFocus;
	}

	public void setIsFocus(boolean value) {
		isFocus = value;
	}

	public int compareTo(SingleInstanceTaskModel otherTask) {
		int dateCompare = this.taskDate.compareTo(otherTask.getTaskDate());
		int nameCompare = this.taskName.compareTo(otherTask.getTaskName());

		if (dateCompare != 0)
			return dateCompare;
		else
			return nameCompare;
	}
}
