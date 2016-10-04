package model;

import java.io.Serializable;
import java.util.LinkedList;

public class ProgramModel implements Serializable {
	private String programName;
	private int defaultColor;
	private LinkedList<TaskModel> taskList;
	
	// Create new program with null task list
	public ProgramModel(String programName, int defaultColor, LinkedList<TaskModel> taskList) {
		this.programName = programName;
		this.defaultColor = defaultColor;
		this.taskList = taskList;
	}

	public String getProgramName() {
		return programName;
	}

	public int getDefaultColor () {
		return defaultColor;
	}
	
	public LinkedList<TaskModel> getTaskList() {
		return taskList;
	}
}
