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
		return (this.taskName.compareTo(otherTask.getTaskName()));
	}
}
