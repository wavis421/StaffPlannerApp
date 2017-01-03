package gui;

import java.util.EventObject;

public class TaskTableEvent extends EventObject {

	private int buttonId;
	private String taskName = "";

	public TaskTableEvent(Object source, int buttonId, String taskName) {
		super(source);
		this.buttonId = buttonId;
		this.taskName = taskName;
	}

	public int getButtonId() {
		return buttonId;
	}

	public String getTaskName() {
		return taskName;
	}
}