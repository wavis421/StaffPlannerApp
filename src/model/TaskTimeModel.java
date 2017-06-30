package model;

public class TaskTimeModel {
	private TimeModel time;
	private String taskName;

	public TaskTimeModel(String taskName, TimeModel time) {
		this.time = time;
		this.taskName = taskName;
	}

	public String getTaskName() {
		return taskName;
	}

	public TimeModel getTime () {
		return time;
	}
}
