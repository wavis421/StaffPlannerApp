package gui;

import java.sql.Time;
import java.util.Calendar;
import java.util.LinkedList;

import javax.swing.table.AbstractTableModel;

import model.PersonByTaskModel;

public class PersonTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 12340002L;
	private LinkedList<PersonByTaskModel> personList;
	private String colNamesBasic[] = { "Name", "Ldr", "Phone #", "E-Mail" };
	private String colNamesExpanded[] = { "Name", "Ldr", "Sub", "Task", "Location", "Time", "Phone #", "E-Mail" };
	private String colNames[];
	private boolean expanded;

	public PersonTableModel(boolean expanded) {
		this.expanded = expanded;
		if (expanded) {
			colNames = colNamesExpanded;
		} else {
			colNames = colNamesBasic;
		}
	}

	public void setData(LinkedList<PersonByTaskModel> db) {
		this.personList = db;
	}

	@Override
	public int getColumnCount() {
		return colNames.length;
	}

	@Override
	public String getColumnName(int column) {
		return colNames[column];
	}

	@Override
	public int getRowCount() {
		return personList.size();
	}

	@Override
	public Object getValueAt(int row, int col) {
		PersonByTaskModel person = personList.get(row);
		if (expanded) {
			switch (col) {
			case 0: // person name
				return person.getPerson().getName();
			case 1: // is leader?
				return (String) (Character.toString(person.getPerson().isLeader() ? '\u2713' : ' '));
			case 2: // is substitute?
				return (String) (Character.toString(person.isSubstitute() ? '\u2713' : ' '));
			case 3: // Task Name
				if (person.getTask() == null)
					return "Floater";
				else
					return person.getTask().getTaskName();
			case 4: // Location
				if (person.getTask() == null)
					return "";
				else
					return person.getTask().getLocation();
			case 5: // Time
				Calendar cal;
				if (person.getTask() == null) {
					cal = person.getPerson().getSingleInstanceTaskAssignment().getTaskDate();
				} else {
					cal = Calendar.getInstance();
					cal.setTime(person.getTask().getTime());
				}
				return Time.valueOf((cal.get(Calendar.HOUR) + 1) + ":" + cal.get(Calendar.MINUTE) + ":00");
			case 6: // Phone number
				return person.getPerson().getPhone();
			case 7: // email
				return person.getPerson().getEmail();
			}
		} else {
			switch (col) {
			case 0: // Person name
				return person.getPerson().getName();
			case 1: // is leader?
				return (String) (Character.toString(person.getPerson().isLeader() ? '\u2713' : ' '));
			case 2: // phone number
				return person.getPerson().getPhone();
			case 3: // email
				return person.getPerson().getEmail();
			}
		}
		return null;
	}

	public int getColumnForPersonName() {
		return 0;
	}
}
