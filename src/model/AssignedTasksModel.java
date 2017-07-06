package model;

public class AssignedTasksModel implements Comparable<AssignedTasksModel> {
	private int assignedTaskID, personID, taskID;
	private String programName;
	private String taskName;
	private boolean[] daysOfWeek;
	private boolean[] weeksOfMonth;
	private ListStatus elementStatus;

	public AssignedTasksModel(int assignedTasksID, int personID, int taskID, String programName, String taskName,
			boolean[] daysOfWeek, boolean[] weeksOfMonth) {
		this.assignedTaskID = assignedTasksID;
		this.personID = personID;
		this.taskID = taskID;
		this.programName = programName;
		this.taskName = taskName;
		this.daysOfWeek = daysOfWeek;
		this.weeksOfMonth = weeksOfMonth;
		this.elementStatus = ListStatus.LIST_ELEMENT_ASSIGNED;
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

	public int getPersonID() {
		return personID;
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

	public ListStatus getElementStatus() {
		return elementStatus;
	}

	public void setElementStatus(ListStatus elementStatus) {
		this.elementStatus = elementStatus;
	}

	@Override
	public int compareTo(AssignedTasksModel otherTask) {
		// TODO: Sort by task time
		return (this.getTaskName().compareTo(otherTask.getTaskName()));
	}
}
