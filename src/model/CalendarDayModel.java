package model;

import java.io.Serializable;
import java.util.Calendar;

public class CalendarDayModel implements Serializable, Comparable<CalendarDayModel> {
	private static final long serialVersionUID = 12340002L;
	private TaskModel task;
	private int personCount;
	private int leaderCount;
	private int textColor;
	private Calendar floaterTime;
	private String floaterTaskName;
	private boolean showCounts;

	public CalendarDayModel(TaskModel task, int personCount, int leaderCount, int textColor, Calendar floaterTime,
			String floaterTaskName, boolean showCounts) {
		this.task = task;
		this.personCount = personCount;
		this.leaderCount = leaderCount;
		this.textColor = textColor;
		this.floaterTime = floaterTime;
		this.floaterTaskName = floaterTaskName;
		this.showCounts = showCounts;
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

	public boolean getShowCounts() {
		return showCounts;
	}

	@Override
	public int compareTo(CalendarDayModel otherTask) {
		TimeModel otherTaskTime, thisTaskTime;

		if (otherTask.getTask() == null) {
			otherTaskTime = new TimeModel(otherTask.getFloaterTime());
		} else {
			otherTaskTime = otherTask.getTask().getTime();
		}

		if (task == null) {
			thisTaskTime = new TimeModel(floaterTime);

		} else {
			thisTaskTime = task.getTime();
		}

		return (thisTaskTime.compareTo(otherTaskTime));
	}
}
