package model;

import java.io.Serializable;

public class AssignedTasksModel implements Serializable {
	private String programName;
	private String taskName;
	private boolean[] daysOfWeek;
	private boolean[] weeksOfMonth;
	
	public void AssignedTasksModel (String programName, String taskName, boolean[] daysOfWeek, boolean[] weeksOfMonth) {
		this.programName = programName;
		this.taskName = taskName;
		this.daysOfWeek = daysOfWeek;
		this.weeksOfMonth = weeksOfMonth;
	}

	public String getProgramName() {
		return programName;
	}

	public String getTaskName() {
		return taskName;
	}

	public boolean[] getDaysOfWeek() {
		return daysOfWeek;
	}

	public boolean[] getWeeksOfMonth() {
		return weeksOfMonth;
	}
}
