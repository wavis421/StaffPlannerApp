package model;

import java.io.Serializable;
import java.util.Calendar;

public class SingleInstanceTaskModel implements Serializable, Comparable<SingleInstanceTaskModel> {
	private static final long serialVersionUID = 12340003L;
	private int singleTaskID, personID, taskID;
	private String taskName;
	private Calendar taskDate;
	private int color;

	public SingleInstanceTaskModel (int singleTaskID, int personID, int taskID, String taskName, Calendar taskDate, int color) {
		this.singleTaskID = singleTaskID;
		this.personID = personID;
		this.taskID = taskID;
		this.taskName = taskName;

		taskDate.set(Calendar.SECOND, 0);
		taskDate.set(Calendar.MILLISECOND, 0);
		this.taskDate = taskDate;

		// Note: Color parameter only valid when taskName is blank
		this.color = color;
	}

	public void setSingleTaskID(int id) {
		singleTaskID = id;
	}

	public void setPersonID(int id) {
		personID = id;
	}

	public int getTaskID() {
		return taskID;
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

	public int compareTo(SingleInstanceTaskModel otherTask) {
		int dateCompare = this.taskDate.compareTo(otherTask.getTaskDate());
		int nameCompare = this.taskName.compareTo(otherTask.getTaskName());

		if (dateCompare != 0)
			return dateCompare;
		else
			return nameCompare;
	}
}
