package gui;

import java.util.LinkedList;

import javax.swing.table.AbstractTableModel;

import model.PersonModel;

public class PersonTableModel extends AbstractTableModel {
	private LinkedList<PersonModel> personList;
	private String colNames[] = { "Name", "Phone #", "E-Mail", "Notes", "Unavail Dates" };

	public void setData(LinkedList<PersonModel> db) {
		this.personList = db;
	}

	@Override
	public int getColumnCount() {
		return 5;
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
		PersonModel person = personList.get(row);
		switch (col) {
		case 0:
			return person.getName();
		case 1:
			return person.getPhone();
		case 2:
			return person.getEmail();
		case 3:
			return person.getNotes();
		case 4:
			return getDatesUnavail(person);
		}
		return null;
	}

	private String getDatesUnavail(PersonModel person) {
		if (!person.getDatesUnavailable().getStartDate().equals("")
				&& !person.getDatesUnavailable().getEndDate().equals(""))
			return (person.getDatesUnavailable().getStartDate() + " to " + person.getDatesUnavailable().getEndDate());
		else
			return null;
	}
}
