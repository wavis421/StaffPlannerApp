package gui;

import java.sql.Time;
import java.util.EventObject;

public class TaskEvent extends EventObject {

	private String programName;
	private String taskName;
	private String location;
	private int dayOfWeek;
	private boolean[] weekOfMonth;
	private Time time;
	private String endDate;
	private int color;

	public TaskEvent(Object source) {
		super(source);
	}

	public TaskEvent(Object source, String programName, String taskName, String location, int dayOfWeek,
			boolean[] weekOfMonth, Time time, String endDate, int color) {
		super(source);

		this.programName = programName;
		this.taskName = taskName;
		this.location = location;
		this.dayOfWeek = dayOfWeek;
		this.weekOfMonth = weekOfMonth;
		this.time = time;
		this.endDate = endDate;
		this.color = color;
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

	public int getDayOfWeek() {
		return dayOfWeek;
	}

	public boolean[] getWeekOfMonth() {
		return weekOfMonth;
	}

	public Time getTime() {
		return time;
	}

	public String getEndDate() {
		return endDate;
	}

	public int getColor() {
		return color;
	}
}
