package model;

import java.io.Serializable;
import java.util.LinkedList;

public class ProgramModel implements Serializable, Comparable<ProgramModel> {
	private static final long serialVersionUID = 12340001L;
	private String programName;
	private String startDate, endDate;
	private LinkedList<TaskModel> taskList;
	
	// Create new program with null task list
	public ProgramModel(String programName, String startDate, String endDate, LinkedList<TaskModel> taskList) {
		this.programName = programName;
		this.startDate = startDate;
		this.endDate = endDate;
		this.taskList = taskList;
	}

	public String toString () {
		return programName;
	}
	
	public String getProgramName() {
		return programName;
	}

	public void setProgramName(String programName) {
		this.programName = programName;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public LinkedList<TaskModel> getTaskList() {
		return taskList;
	}

	@Override
	public int compareTo(ProgramModel otherProgram) {
		return (this.getProgramName().compareTo(otherProgram.getProgramName()));
	}
}
