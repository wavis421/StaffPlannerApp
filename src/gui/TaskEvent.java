package gui;

import java.sql.Time;
import java.util.EventObject;

import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

public class TaskEvent extends EventObject {

	private String taskName;
	private String location;
	private int dayOfWeek;
	private boolean[] weekOfMonth;
	private Time time;
	private String endDate;

	public TaskEvent(Object source) {
		super(source);
	}

	public TaskEvent(Object source, String taskName, String location, int dayOfWeek, boolean[] weekOfMonth, Time time,
			String endDate) {
		super(source);

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
}
