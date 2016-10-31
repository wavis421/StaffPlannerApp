package gui;

import java.sql.Time;
import java.util.EventObject;

public class TaskEvent extends EventObject {

	private String programName;
	private String taskName;
	private String location;
	private boolean[] dayOfWeek;
	private boolean[] weekOfMonth;
	private Time time;
	private int color;

	public TaskEvent(Object source) {
		super(source);
	}

	public TaskEvent(Object source, String programName, String taskName, String location, boolean[] dayOfWeek,
			boolean[] weekOfMonth, Time time, int color) {
		super(source);

		this.programName = programName;
		this.taskName = taskName;
		this.location = location;
		this.dayOfWeek = dayOfWeek;
		this.weekOfMonth = weekOfMonth;
		this.time = time;
		this.color = color;
	}

	public String toString() {
		return taskName;
	}
	
	public String getProgramName() {
		return programName;
	}

	public String getTaskName() {
		return taskName;
	}

	public String getLocation() {
		return location;
	}

	public boolean[] getDayOfWeek() {
		return dayOfWeek;
	}

	public boolean[] getWeekOfMonth() {
		return weekOfMonth;
	}

	public Time getTime() {
		return time;
	}

	public int getColor() {
		return color;
	}
}
