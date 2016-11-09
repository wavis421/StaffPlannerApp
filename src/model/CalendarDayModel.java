package model;

import java.io.Serializable;

public class CalendarDayModel implements Serializable {
	private TaskModel task;
	private int personCount;
	
	public CalendarDayModel (TaskModel task, int personCount) {
		this.task = task;
		this.personCount = personCount;
	}

	public String toString () {
		return task.getTaskName();
	}
	
	public TaskModel getTask() {
		return task;
	}

	public int getPersonCount() {
		return personCount;
	}
}
