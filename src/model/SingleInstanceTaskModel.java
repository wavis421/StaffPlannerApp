package model;

import java.io.Serializable;
import java.util.Calendar;

public class SingleInstanceTaskModel implements Serializable, Comparable<SingleInstanceTaskModel> {
	private static final long serialVersionUID = 12340003L;
	private int taskID;
	private String taskName;
	private String programName;
	private Calendar taskDate;
	private int color;
	private ListStatus elementStatus;

	public SingleInstanceTaskModel (int taskID, String programName, String taskName, Calendar taskDate, int color) {
		this.taskID = taskID;
		this.programName = programName;
		this.taskName = taskName;
		this.elementStatus = ListStatus.LIST_ELEMENT_ASSIGNED;

		taskDate.set(Calendar.SECOND, 0);
		taskDate.set(Calendar.MILLISECOND, 0);
		this.taskDate = taskDate;

		// Note: Color parameter only valid when taskName is blank
		this.color = color;
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

	public ListStatus getElementStatus() {
		return elementStatus;
	}

	public void setElementStatus(ListStatus elementStatus) {
		this.elementStatus = elementStatus;
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
