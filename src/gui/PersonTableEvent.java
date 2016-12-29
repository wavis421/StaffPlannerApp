package gui;

import java.util.Calendar;
import java.util.EventObject;

public class PersonTableEvent extends EventObject {
	
	private int buttonId;
	private int row;
	private String personName;
	private Calendar calendar;
	private int color;
	
	public PersonTableEvent(Object source, int buttonId, int row, String personName, Calendar calendar, int color) {
		super(source);

		this.buttonId = buttonId;
		this.row = row;
		this.personName = personName;
		this.calendar = calendar;
		this.color = color;
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

	public Calendar getCalendar() {
		return calendar;
	}

	public int getColor() {
		return color;
	}
}
