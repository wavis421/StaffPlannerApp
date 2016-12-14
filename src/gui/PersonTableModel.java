package gui;

import java.sql.Time;
import java.util.LinkedList;

import javax.swing.table.AbstractTableModel;

import model.PersonByTaskModel;
import model.PersonModel;

public class PersonTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 12340002L;
	private LinkedList<PersonByTaskModel> personList;
	private String colNamesBasic[] = { "Name", "Ldr", "Phone #", "E-Mail", "Unavail Dates" };
	private String colNamesExpanded[] = { "Name", "Ldr", "Sub", "Task", "Location", "Time", "Phone #", "E-Mail",
			"Unavail Dates" };
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
			case 0:
				return person.getPerson().getName();
			case 1:
				return (String) (Character.toString(person.getPerson().isLeader() ? '\u2713' : ' '));
			case 2:
				return (String) (Character.toString(person.isSubstitute() ? '\u2713' : ' '));
			case 3:
				if (person.getTask() == null)
					return "";
				else
					return person.getTask().getTaskName();
			case 4:
				if (person.getTask() == null)
					return "Floater";
				else
					return person.getTask().getLocation();
			case 5:
				if (person.getTask() == null)
					return Time.valueOf("12:00:00");
				else
					return person.getTask().getTime();
			case 6:
				return person.getPerson().getPhone();
			case 7:
				return person.getPerson().getEmail();
			case 8:
				return getDatesUnavail(person.getPerson());
			}
		} else {
			switch (col) {
			case 0:
				return person.getPerson().getName();
			case 1:
				return (String) (Character.toString(person.getPerson().isLeader() ? '\u2713' : ' '));
			case 2:
				return person.getPerson().getPhone();
			case 3:
				return person.getPerson().getEmail();
			case 4:
				return getDatesUnavail(person.getPerson());
			}
		}
		return null;
	}

	public int getColumnForPersonName() {
		return 0;
	}

	private String getDatesUnavail(PersonModel person) {
		if (!person.getDatesUnavailable().getStartDate().equals("")
				&& !person.getDatesUnavailable().getEndDate().equals(""))
			return (person.getDatesUnavailable().getStartDate() + " to " + person.getDatesUnavailable().getEndDate());
		else
			return null;
	}
}
