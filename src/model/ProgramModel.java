package model;

import java.io.Serializable;
import java.util.ArrayList;

public class ProgramModel implements Serializable, Comparable<ProgramModel> {
	private static final long serialVersionUID = 12340002L;
	private int programID;
	private String programName;
	private String startDate, endDate;
	private ArrayList<TaskModel> taskList;
	
	// Create new program with null task list
	public ProgramModel(int programID, String programName, String startDate, String endDate, ArrayList<TaskModel> taskList) {
		this.programID = programID;
		this.programName = programName;
		this.startDate = startDate;
		this.endDate = endDate;
		this.taskList = taskList;
	}

	public String toString () {
		return programName;
	}
	
	public int getProgramID() {
		return programID;
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

	public ArrayList<TaskModel> getTaskList() {
		return taskList;
	}

	@Override
	public int compareTo(ProgramModel otherProgram) {
		return (this.getProgramName().compareTo(otherProgram.getProgramName()));
	}
}
