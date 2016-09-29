package model;

import java.io.Serializable;
import java.sql.Time;

import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

public class TaskModel implements Serializable {
	private String taskName;
	private Time time;
	private String location;
	private int dayOfWeek;
	private boolean[] weekOfMonth;
	private String endDate;

	public TaskModel(String taskName, String location, int dayOfWeek, boolean[] weekOfMonth, Time time,
			String endDate) {
		this.taskName = taskName;
		this.location = location;
		this.dayOfWeek = dayOfWeek;
		this.weekOfMonth = weekOfMonth;
		this.time = time;
		this.endDate = endDate;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public Time getTime() {
		return time;
	}

	public String getLocation() {
		return location;
	}

	public String getEndDate() {
		return endDate;
	}

	public int getDayOfWeek() {
		return dayOfWeek;
	}

	public boolean[] getWeekOfMonth() {
		return weekOfMonth;
	}
}
