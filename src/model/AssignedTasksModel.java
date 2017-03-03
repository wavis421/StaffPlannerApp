package model;

import java.io.Serializable;

public class AssignedTasksModel implements Serializable, Comparable<AssignedTasksModel> {
	private static final long serialVersionUID = 12340001L;
	private int assignedTaskID, personID, taskID;
	private String programName;
	private String taskName;
	private boolean[] daysOfWeek;
	private boolean[] weeksOfMonth;

	public AssignedTasksModel(int assignedTasksID, int personID, int taskID, String programName, String taskName,
			boolean[] daysOfWeek, boolean[] weeksOfMonth) {
		this.assignedTaskID = assignedTasksID;
		this.personID = personID;
		this.taskID = taskID;
		this.programName = programName;
		this.taskName = taskName;
		this.daysOfWeek = daysOfWeek;
		this.weeksOfMonth = weeksOfMonth;
	}

	public int getTaskID() {
		return taskID;
	}

	public int getAssignedTaskID() {
		return assignedTaskID;
	}
	
	public void setAssignedTaskID(int id) {
		assignedTaskID = id;
	}
	
	public void setPersonID(int id) {
		personID = id;
	}
	
	public String toString() {
		return taskName;
	}

	public String getProgramName() {
		return programName;
	}

	public void setProgramName(String programName) {
		this.programName = programName;
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

	@Override
	public int compareTo(AssignedTasksModel otherTask) {
		// TODO: Sort by task time
		return (this.getTaskName().compareTo(otherTask.getTaskName()));
	}
}
