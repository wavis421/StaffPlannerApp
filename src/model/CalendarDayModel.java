package model;

import java.io.Serializable;
import java.sql.Time;
import java.util.Calendar;
import java.util.Date;

public class CalendarDayModel implements Serializable, Comparable<CalendarDayModel> {
	private static final long serialVersionUID = 12340001L;
	private TaskModel task;
	private int personCount;
	private int textColor;
	private Calendar floaterTime;

	public CalendarDayModel(TaskModel task, int personCount, int textColor, Calendar floaterTime) {
		this.task = task;
		this.personCount = personCount;
		this.textColor = textColor;
		this.floaterTime = floaterTime;
	}

	public String toString() {
		if (task == null)
			return "";
		else
			return task.getTaskName();
	}

	public TaskModel getTask() {
		return task;
	}

	public int getPersonCount() {
		return personCount;
	}

	public int getTextColor() {
		return textColor;
	}

	public Calendar getFloaterTime() {
		return floaterTime;
	}

	@Override
	public int compareTo(CalendarDayModel otherTask) {
		Time otherTaskTime, thisTaskTime;

		if (otherTask.getTask() == null) {
			Calendar cal = otherTask.getFloaterTime();
			otherTaskTime = Time.valueOf((cal.get(Calendar.HOUR) + 1) + ":" + cal.get(Calendar.MINUTE) + ":00");;
		} else {
			otherTaskTime = (Time) otherTask.getTask().getTime();
		}

		if (task == null) {
			Calendar cal = floaterTime;
			thisTaskTime = Time.valueOf((cal.get(Calendar.HOUR) + 1) + ":" + cal.get(Calendar.MINUTE) + ":00");
		} else {
			thisTaskTime = (Time) task.getTime();
		}

		return (thisTaskTime.compareTo(otherTaskTime));
	}
}
