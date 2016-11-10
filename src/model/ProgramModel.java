package model;

import java.io.Serializable;
import java.util.LinkedList;

public class ProgramModel implements Serializable {
	private static final long serialVersionUID = 12340001L;
	private String programName;
	private String endDate;
	private LinkedList<TaskModel> taskList;
	
	// Create new program with null task list
	public ProgramModel(String programName, String endDate, LinkedList<TaskModel> taskList) {
		this.programName = programName;
		this.endDate = endDate;
		this.taskList = taskList;
	}

	public String toString () {
		return programName;
	}
	
	public String getProgramName() {
		return programName;
	}

	public String getEndDate() {
		return endDate;
	}

	public LinkedList<TaskModel> getTaskList() {
		return taskList;
	}
}
