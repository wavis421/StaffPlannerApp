package gui;

import java.util.EventObject;

import model.TaskModel;

public class AssignTaskEvent extends EventObject {
	private String programName;
	private TaskModel task;
	private int assignedTaskID;
	private boolean[] daysOfWeek;
	private boolean[] weeksOfMonth;
	private int hour, minute;
	private boolean isFocus = false;

	public AssignTaskEvent(Object source, String programName, TaskModel task, int assignedTaskID, boolean[] daysOfWeek,
			boolean[] weeksOfMonth, int hour, int minute) {

		super(source);

		this.programName = programName;
		this.task = task;
		this.assignedTaskID = assignedTaskID;
		this.daysOfWeek = daysOfWeek;
		this.weeksOfMonth = weeksOfMonth;
		this.hour = hour;
		this.minute = minute;
	}

	public String toString() {
		if (task != null)
			return task.getTaskName();
		else
			return null;
	}

	public String getProgramName() {
		return programName;
	}

	public TaskModel getTask() {
		return task;
	}

	public int getAssignedTaskID() {
		return assignedTaskID;
	}

	public boolean[] getDaysOfWeek() {
		return daysOfWeek;
	}

	public boolean[] getWeeksOfMonth() {
		return weeksOfMonth;
	}

	public int getHour() {
		return hour;
	}

	public int getMinute() {
		return minute;
	}

	public boolean getIsFocus() {
		return isFocus;
	}

	public void setIsFocus(boolean value) {
		isFocus = value;
	}
}
