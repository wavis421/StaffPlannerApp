package model;

import java.io.Serializable;
import java.util.Calendar;

public class SingleInstanceTaskModel implements Serializable, Comparable<SingleInstanceTaskModel> {
	private static final long serialVersionUID = 12340002L;
	private String taskName;
	private Calendar taskDate;
	private int color;
	
	public SingleInstanceTaskModel (String taskName, Calendar taskDate, int color) {
		this.taskName = taskName;
		this.taskDate = taskDate;
		
		// Note: Color parameter only valid when taskName is blank
		this.color = color;
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
