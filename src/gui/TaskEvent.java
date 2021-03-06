package gui;

import java.util.EventObject;

import model.TimeModel;

public class TaskEvent extends EventObject {

	private String programName;
	private String taskName;
	private String location;
	private int numLeadersReqd;
	private int totalPersonsReqd;
	private boolean[] dayOfWeek;
	private boolean[] weekOfMonth;
	private TimeModel time;
	private int color;

	public TaskEvent(Object source) {
		super(source);
	}

	public TaskEvent(Object source, String programName, String taskName, String location, int numLeadersReqd,
			int totalPersonsReqd, boolean[] dayOfWeek, boolean[] weekOfMonth, TimeModel time, int color) {
		super(source);

		this.programName = programName;
		this.taskName = taskName;
		this.location = location;
		this.numLeadersReqd = numLeadersReqd;
		this.totalPersonsReqd = totalPersonsReqd;
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

	public int getNumLeadersReqd() {
		return numLeadersReqd;
	}

	public int getTotalPersonsReqd() {
		return totalPersonsReqd;
	}

	public boolean[] getDayOfWeek() {
		return dayOfWeek;
	}

	public boolean[] getWeekOfMonth() {
		return weekOfMonth;
	}

	public TimeModel getTime() {
		return time;
	}

	public int getColor() {
		return color;
	}
}
