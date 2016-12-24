package gui;

import java.util.EventObject;

public class PersonTableEvent extends EventObject {
	
	private int buttonId;
	private int row;
	private String personName;
	
	public PersonTableEvent(Object source, int buttonId, int row, String personName) {
		super(source);

		this.buttonId = buttonId;
		this.row = row;
		this.personName = personName;
	}

	public int getButtonId() {
		return buttonId;
	}

	public int getRow() {
		return row;
	}

	public String getPersonName() {
		return personName;
	}
}
