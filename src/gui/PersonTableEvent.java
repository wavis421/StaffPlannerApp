package gui;

import java.util.Calendar;
import java.util.EventObject;

import javax.swing.DefaultListModel;
import javax.swing.JList;

public class PersonTableEvent extends EventObject {

	private int buttonId;
	private int row;
	private String personName = "";
	private JList<String> personList;
	private Calendar calendar;
	private int color;

	public PersonTableEvent(Object source, int buttonId, int row, String personName, Calendar calendar, int color) {
		super(source);

		this.buttonId = buttonId;
		this.row = row;
		this.calendar = calendar;
		this.color = color;
		this.personName = personName;

		// Create JList with person name
		if (personName != null && !personName.equals("")) {
			DefaultListModel<String> pModel = new DefaultListModel<String>();
			pModel.addElement(new String(personName));
			this.personList = new JList<String>(pModel);
		}
	}

	public PersonTableEvent(Object source, int buttonId, int row, JList<String> personList, Calendar calendar,
			int color) {
		super(source);

		this.buttonId = buttonId;
		this.row = row;
		this.calendar = calendar;
		this.color = color;
		this.personName = "";
		this.personList = personList;
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

	public JList<String> getPersonList() {
		return personList;
	}

	public Calendar getCalendar() {
		return calendar;
	}

	public int getColor() {
		return color;
	}
}
