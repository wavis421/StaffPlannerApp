package gui;

import java.util.EventObject;

public class ProgramEvent extends EventObject {
	private String programName;
	private String endDate;
	private int defaultColor;
	
	public ProgramEvent(Object source) {
		super(source);
	}

	public ProgramEvent(Object source, String programName, String endDate, int defaultColor) {
		super(source);

		this.programName = programName;
		this.endDate = endDate;
		this.defaultColor = defaultColor;
	}

	public String getProgramName() {
		return programName;
	}

	public String getEndDate() {
		return endDate;
	}

	public int getDefaultColor() {
		return defaultColor;
	}
}
