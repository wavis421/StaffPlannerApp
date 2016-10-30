package gui;

import java.util.EventObject;

public class AssignTaskEvent extends EventObject {
	private String programName;
	private String taskName;
	private boolean[] daysOfWeek;
	private boolean[] weeksOfMonth;
	
	public AssignTaskEvent(Object source, String programName, String taskName, boolean[] daysOfWeek,
			boolean[] weeksOfMonth) {
		
		super(source);

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
