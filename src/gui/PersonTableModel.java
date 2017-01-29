package gui;

import java.util.LinkedList;

import javax.swing.table.AbstractTableModel;

import model.PersonByTaskModel;
import model.TimeModel;
import utilities.Utilities;

public class PersonTableModel extends AbstractTableModel {
	private static final int PERSON_NAME_COLUMN = 0;
	private static final int LEADER_COLUMN = 1;
	private static final int SUB_COLUMN = 2;
	private static final int TASK_COLUMN = 3;
	private static final int TIME_COLUMN = 5;
	private static final int PHONE_COLUMN_EXPANDED = 6;
	private static final int PHONE_COLUMN_NOT_EXPANDED = 2;
	private static final int EMAIL_COLUMN_EXPANDED = 7;
	private static final int EMAIL_COLUMN_NOT_EXPANDED = 3;

	private static final long serialVersionUID = 12340002L;
	private LinkedList<PersonByTaskModel> personList;
	private String colNamesBasic[] = { "Name", "Ldr", "Phone #", "E-Mail" };
	private String colNamesExpanded[] = { "Name", "Ldr", "Sub", "Task", "Location", "Time", "Phone #", "E-Mail" };
	private String colNames[];
	private boolean expanded;

	public PersonTableModel(boolean isColumnExpanded, LinkedList<PersonByTaskModel> personList) {
		this.personList = personList;
		this.expanded = isColumnExpanded;
		if (expanded) {
			colNames = colNamesExpanded;
		} else {
			colNames = colNamesBasic;
		}
	}

	public void setData(LinkedList<PersonByTaskModel> db) {
		personList = db;
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
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case 5: // Time
			return TimeModel.class;

		default:
			return String.class;
		}
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
				if (person.getTask() == null) {
					return new TimeModel(person.getTaskDate());
				} else {
					return person.getTask().getTime();
				}
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
		return PERSON_NAME_COLUMN;
	}

	public int getColumnForLeader() {
		return LEADER_COLUMN;
	}

	public int getColumnForSub() {
		if (expanded)
			return SUB_COLUMN;
		else
			return -1;
	}

	public int getColumnForTaskName() {
		if (expanded)
			return TASK_COLUMN;
		else
			return -1;
	}

	public int getColumnForTime() {
		if (expanded)
			return TIME_COLUMN;
		else
			return -1;
	}

	public int getColumnForPhone() {
		if (expanded)
			return PHONE_COLUMN_EXPANDED;
		else
			return PHONE_COLUMN_NOT_EXPANDED;
	}

	public int getColumnForEmail() {
		if (expanded)
			return EMAIL_COLUMN_EXPANDED;
		else
			return EMAIL_COLUMN_NOT_EXPANDED;
	}
}
