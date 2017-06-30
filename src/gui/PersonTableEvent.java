package gui;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.EventObject;

public class PersonTableEvent extends EventObject {

	private int buttonId;
	private String personName = "";
	private ArrayList<String> personList;
	private Calendar calendar;
	private String taskName;
	private int color;

	public PersonTableEvent(Object source, int buttonId, String personName, Calendar calendar, String taskName,
			int color) {
		super(source);

		this.buttonId = buttonId;
		this.calendar = calendar;
		this.taskName = taskName;
		this.color = color;
		this.personName = personName;

		// Create list with person name
		if (personName != null && !personName.equals("")) {
			ArrayList<String> pList = new ArrayList<String>();
			pList.add(new String(personName));
			this.personList = pList;
		}
	}

	public PersonTableEvent(Object source, int buttonId, ArrayList<String> personList, Calendar calendar,
			String taskName, int color) {
		super(source);

		this.buttonId = buttonId;
		this.calendar = calendar;
		this.taskName = taskName;
		this.color = color;
		this.personName = "";
		this.personList = personList;
	}

	public int getButtonId() {
		return buttonId;
	}

	public String getPersonName() {
		return personName;
	}

	public ArrayList<String> getPersonList() {
		return personList;
	}

	public Calendar getCalendar() {
		return calendar;
	}

	public String getTaskName() {
		return taskName;
	}

	public int getColor() {
		return color;
	}
}
