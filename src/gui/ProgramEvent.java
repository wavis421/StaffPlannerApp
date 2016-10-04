package gui;

import java.util.EventObject;

public class ProgramEvent extends EventObject {
	private String programName;
	private int defaultColor;
	
	public ProgramEvent(Object source) {
		super(source);
	}

	public ProgramEvent(Object source, String programName, int defaultColor) {
		super(source);

		this.programName = programName;
		this.defaultColor = defaultColor;
	}

	public String getProgramName() {
		return programName;
	}

	public int getDefaultColor() {
		return defaultColor;
	}
}
