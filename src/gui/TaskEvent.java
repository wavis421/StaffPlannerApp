package gui;

import java.sql.Time;
import java.util.EventObject;

public class TaskEvent extends EventObject {

	private String taskName;
	private String location;
	private int dayOfWeek;
	private boolean[] weekOfMonth;
	private Time time;
	
	public TaskEvent(Object source) {
		super(source);
	}
	
	public TaskEvent(Object source, String taskName, String location, int dayOfWeek, boolean[] weekOfMonth, Time time) {
		super(source);
		
		this.taskName = taskName;
		this.location = location;
		this.dayOfWeek = dayOfWeek;
		this.weekOfMonth = weekOfMonth;
		this.time = time;
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
}
