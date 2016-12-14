package gui;

import java.util.Calendar;
import java.util.EventObject;

import javax.swing.JList;

public class FloaterEvent extends EventObject {
	private JList<String> personNames;
	private Calendar calendar;
	private int color;

	public FloaterEvent(Object source) {
		super(source);
	}

	public FloaterEvent(Object source, JList<String> personNames, Calendar calendar, int color) {
		super(source);

		this.personNames = personNames;
		this.calendar = calendar;
		this.color = color;
	}

	public JList<String> getPersonNames() {
		return personNames;
	}

	public Calendar getCalendar() {
		return calendar;
	}

	public int getColor() {
		return color;
	}
}