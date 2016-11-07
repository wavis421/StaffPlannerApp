package model;

import java.io.Serializable;

public class AssignedTasksModel implements Serializable {
	private String programName;
	private String taskName;
	private boolean[] daysOfWeek;
	private boolean[] weeksOfMonth;
	
	public AssignedTasksModel (String programName, String taskName, boolean[] daysOfWeek, boolean[] weeksOfMonth) {
		this.programName = programName;
		this.taskName = taskName;
		this.daysOfWeek = daysOfWeek;
		this.weeksOfMonth = weeksOfMonth;
	}

	public String toString () {
		return taskName;
	}
	
	public String getProgramName() {
		return programName;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public boolean[] getDaysOfWeek() {
		return daysOfWeek;
	}

	public boolean[] getWeeksOfMonth() {
		return weeksOfMonth;
	}
}
