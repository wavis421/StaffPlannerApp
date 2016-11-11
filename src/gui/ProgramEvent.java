package gui;

import java.util.EventObject;

public class ProgramEvent extends EventObject {
	private String programName;
	private String startDate, endDate;
	private boolean selectAsActive;
	
	public ProgramEvent(Object source, String programName, String startDate, String endDate, boolean selectAsActive) {
		super(source);

		this.programName = programName;
		this.startDate = startDate;
		this.endDate = endDate;
		this.selectAsActive = selectAsActive;
	}

	public String toString () {
		return programName;
	}
	
	public String getProgramName() {
		return programName;
	}

	public String getStartDate() {
		return startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public boolean isSelectedActive() {
		return selectAsActive;
	}
}
