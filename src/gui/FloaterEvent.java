package gui;

import java.util.Calendar;
import java.util.EventObject;

public class FloaterEvent extends EventObject {
	private String personName;
	private Calendar calendar;
	private int color;

	public FloaterEvent(Object source) {
		super(source);
	}

	public FloaterEvent(Object source, String personName, Calendar calendar, int color) {
		super(source);

		this.personName = personName;
		this.calendar = calendar;
		this.color = color;
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