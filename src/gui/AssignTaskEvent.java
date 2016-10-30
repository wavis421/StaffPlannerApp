package gui;

import java.util.EventObject;

import model.TaskModel;

public class AssignTaskEvent extends EventObject {
	private String programName;
	private TaskModel task;
	private boolean[] daysOfWeek;
	private boolean[] weeksOfMonth;
	
	public AssignTaskEvent(Object source, String programName, TaskModel task, boolean[] daysOfWeek,
			boolean[] weeksOfMonth) {
		
		super(source);

		this.programName = programName;
		this.task = task;
		this.daysOfWeek = daysOfWeek;
		this.weeksOfMonth = weeksOfMonth;
	}

	public String toString () {
		return task.getTaskName();
	}
	
	public String getProgramName() {
		return programName;
	}

	public TaskModel getTask() {
		return task;
	}

	public boolean[] getDaysOfWeek() {
		return daysOfWeek;
	}

	public boolean[] getWeeksOfMonth() {
		return weeksOfMonth;
	}
}
