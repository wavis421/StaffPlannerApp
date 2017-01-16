package model;

import java.io.Serializable;
import java.sql.Time;
import java.util.Calendar;
import java.util.Date;

public class CalendarDayModel implements Serializable, Comparable<CalendarDayModel> {
	private static final long serialVersionUID = 12340002L;
	private TaskModel task;
	private int personCount;
	private int leaderCount;
	private int textColor;
	private Calendar floaterTime;
	private String floaterTaskName;

	public CalendarDayModel(TaskModel task, int personCount, int leaderCount, int textColor, Calendar floaterTime,
			String floaterTaskName) {
		this.task = task;
		this.personCount = personCount;
		this.leaderCount = leaderCount;
		this.textColor = textColor;
		this.floaterTime = floaterTime;
		this.floaterTaskName = floaterTaskName;
	}

	public String toString() {
		if (task == null)
			return floaterTaskName;
		else
			return task.getTaskName();
	}

	public TaskModel getTask() {
		return task;
	}

	public int getPersonCount() {
		return personCount;
	}

	public int getLeaderCount() {
		return leaderCount;
	}

	public int getTextColor() {
		return textColor;
	}

	public Calendar getFloaterTime() {
		return floaterTime;
	}

	public String getFloaterTaskName() {
		return floaterTaskName;
	}

	public void setFloaterTaskName(String floaterTaskName) {
		this.floaterTaskName = floaterTaskName;
	}

	@Override
	public int compareTo(CalendarDayModel otherTask) {
		Time otherTaskTime, thisTaskTime;

		if (otherTask.getTask() == null) {
			Calendar cal = otherTask.getFloaterTime();
			if (cal.get(Calendar.AM_PM) == Calendar.PM)
				otherTaskTime = Time.valueOf((cal.get(Calendar.HOUR) + 12) + ":" + cal.get(Calendar.MINUTE) + ":00");
			else
				otherTaskTime = Time.valueOf(cal.get(Calendar.HOUR) + ":" + cal.get(Calendar.MINUTE) + ":00");
		} else {
			otherTaskTime = (Time) otherTask.getTask().getTime();
		}

		if (task == null) {
			Calendar cal = floaterTime;
			if (cal.get(Calendar.AM_PM) == Calendar.PM)
				thisTaskTime = Time.valueOf((cal.get(Calendar.HOUR) + 12) + ":" + cal.get(Calendar.MINUTE) + ":00");
			else
				thisTaskTime = Time.valueOf(cal.get(Calendar.HOUR) + ":" + cal.get(Calendar.MINUTE) + ":00");
		} else {
			thisTaskTime = (Time) task.getTime();
		}

		return (thisTaskTime.compareTo(otherTaskTime));
	}
}
