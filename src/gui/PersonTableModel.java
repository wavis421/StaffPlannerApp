/**
 * PersonTableModel: Model for displaying a roster. There are 3 types of field expansion levels
 * 		which display the following:
 * 			Expansion level 0 (MINIMUM expansion): Name, leader, Phone #, EMail
 * 			Expansion level 1 (BY DAY expansion):  Name, leader, Sub, Task, Time, Location, Phone #, EMail
 * 			Expansion Level 2 (BY TASK expansion): Name, leader, DOW, WOM, Phone #, EMail, 
 */

package gui;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import model.AssignedTasksModel;
import model.PersonByTaskModel;
import model.TimeModel;
import utilities.Utilities;

public class PersonTableModel extends AbstractTableModel {
	// Expansion levels
	private static final int PERSON_TABLE_MINIMUM_EXPANSION = 0;
	private static final int PERSON_TABLE_EXPAND_BY_DAY = 1;
	private static final int PERSON_TABLE_EXPAND_BY_TASK = 2;
	private static final int PERSON_TABLE_EXPAND_WITH_NOTES = 3;

	// Columns for PERSON_TABLE_MINIMUM_EXPANSION
	private static final int PERSON_NAME_COLUMN = 0;
	private static final int LEADER_COLUMN = 1;
	private static final int PHONE_COLUMN_MIN_EXPANSION = 2;
	private static final int EMAIL_COLUMN_MIN_EXPANSION = 3;

	// Columns for PERSON_TABLE_EXPAND_BY_DAY
	private static final int SUB_COLUMN = 2;
	private static final int TASK_COLUMN = 3;
	private static final int TIME_COLUMN = 4;
	private static final int LOCATION_COLUMN = 5;
	private static final int PHONE_COLUMN_EXPAND_BY_DAY = 6;
	private static final int EMAIL_COLUMN_EXPAND_BY_DAY = 7;

	// PERSON_TABLE_EXPAND_BY_TASK
	private static final int DOW_COLUMN = 2;
	private static final int WOM_COLUMN = 3;
	private static final int PHONE_COLUMN_EXPAND_BY_TASK = 4;
	private static final int EMAIL_COLUMN_EXPAND_BY_TASK = 5;

	// PERSON_TABLE_EXPAND_WITH_NOTES
	private static final int NOTES_COLUMN = 4;

	private static final long serialVersionUID = 12340002L;
	private ArrayList<PersonByTaskModel> personList;
	private String colNamesBasic[] = { "Name", "Ldr", "Phone #", "E-Mail" };
	private String colNamesExpandByDay[] = { "Name", "Ldr", "Sub", "Task", "Time", "Location", "Phone #", "E-Mail" };
	private String colNamesExpandByTask[] = { "Name", "Ldr", "DOW", "WOM", "Phone #", "E-Mail" };
	private String colNamesExpandWithNotes[] = { "Name", "Ldr", "Phone #", "E-Mail", "Notes" };
	private String colNames[];
	private int expansionLevel;

	public PersonTableModel(int columnExpansionLevel, ArrayList<PersonByTaskModel> personList) {
		this.personList = personList;
		this.expansionLevel = columnExpansionLevel;
		if (columnExpansionLevel == PERSON_TABLE_EXPAND_BY_DAY) {
			colNames = colNamesExpandByDay;
		} else if (columnExpansionLevel == PERSON_TABLE_EXPAND_BY_TASK) {
			colNames = colNamesExpandByTask;
		} else if (columnExpansionLevel == PERSON_TABLE_EXPAND_WITH_NOTES) {
			colNames = colNamesExpandWithNotes;
		} else {
			colNames = colNamesBasic;
		}
	}

	public void setData(ArrayList<PersonByTaskModel> db) {
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
		if (columnIndex == TIME_COLUMN && expansionLevel == PERSON_TABLE_EXPAND_BY_DAY)
			return TimeModel.class;
		else
			return String.class;
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		if (expansionLevel == PERSON_TABLE_EXPAND_WITH_NOTES && col == NOTES_COLUMN)
			return true;
		else
			return false;
	}

	@Override
	public Object getValueAt(int row, int col) {
		PersonByTaskModel person = personList.get(row);
		if (expansionLevel == PERSON_TABLE_EXPAND_BY_DAY) {
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
			case 4: // Time
				if (person.getTask() == null) {
					return new TimeModel(person.getTaskDate());
				} else {
					return person.getTask().getTime();
				}
			case 5: // Location
				if (person.getTask() == null)
					return "";
				else
					return person.getTask().getLocation();
			case 6: // Phone number
				return person.getPerson().getPhone();
			case 7: // email
				return person.getPerson().getEmail();
			}
		} else if (expansionLevel == PERSON_TABLE_EXPAND_BY_TASK) {
			switch (col) {
			case 0: // person name
				return person.getPerson().getName();
			case 1: // is leader?
				return (String) (Character.toString(person.getPerson().isLeader() ? '\u2713' : ' '));
			case 2: // DOW
				AssignedTasksModel taskDOW = getTaskMatchInAssignedTaskList(person.getTask().getTaskName(),
						person.getPerson().getAssignedTasks());
				return Utilities.getDayOfWeekString(taskDOW.getDaysOfWeek());
			case 3: // WOM
				AssignedTasksModel taskWOM = getTaskMatchInAssignedTaskList(person.getTask().getTaskName(),
						person.getPerson().getAssignedTasks());
				return Utilities.getWeekOfMonthString(taskWOM.getWeeksOfMonth());
			case 4: // Phone number
				return person.getPerson().getPhone();
			case 5: // email
				return person.getPerson().getEmail();
			}
		} else if (expansionLevel == PERSON_TABLE_EXPAND_WITH_NOTES) {
			switch (col) {
			case 0: // Person name
				return person.getPerson().getName();
			case 1: // is leader?
				return (String) (Character.toString(person.getPerson().isLeader() ? '\u2713' : ' '));
			case 2: // phone number
				return person.getPerson().getPhone();
			case 3: // email
				return person.getPerson().getEmail();
			case 4: // notes
				return person.getPerson().getNotes();
			}
		} else { // Minimum expansion
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

	public boolean isSubstitute(int row) {
		PersonByTaskModel person = personList.get(row);
		return person.isSubstitute();
	}

	public boolean isFloater(int row) {
		PersonByTaskModel person = personList.get(row);
		return (person.getTask() == null);
	}

	public TimeModel getTaskTime(int row) {
		PersonByTaskModel person = personList.get(row);
		if (person.getTask() == null) {
			return new TimeModel(person.getTaskDate());
		} else {
			return person.getTask().getTime();
		}
	}

	public String getTaskName(int row) {
		PersonByTaskModel person = personList.get(row);
		if (person.getTask() == null) {
			return null;
		} else {
			return person.getTask().getTaskName();
		}
	}

	@Override
	public void setValueAt(Object value, int row, int column) {
		if (expansionLevel == PERSON_TABLE_EXPAND_WITH_NOTES) {
			// The only editable field is the notes column
			PersonByTaskModel person = personList.get(row);
			person.getPerson().setNotes((String) value);
		}
	}

	public int getColumnForPersonName() {
		return PERSON_NAME_COLUMN;
	}

	public int getColumnForLeader() {
		return LEADER_COLUMN;
	}

	public int getColumnForSub() {
		if (expansionLevel == PERSON_TABLE_EXPAND_BY_DAY)
			return SUB_COLUMN;
		else
			return -1;
	}

	public int getColumnForTaskName() {
		if (expansionLevel == PERSON_TABLE_EXPAND_BY_DAY)
			return TASK_COLUMN;
		else
			return -1;
	}

	public int getColumnForTime() {
		if (expansionLevel == PERSON_TABLE_EXPAND_BY_DAY)
			return TIME_COLUMN;
		else
			return -1;
	}

	public int getColumnForLocation() {
		if (expansionLevel == PERSON_TABLE_EXPAND_BY_DAY)
			return LOCATION_COLUMN;
		else
			return -1;
	}

	public int getColumnForPhone() {
		if (expansionLevel == PERSON_TABLE_EXPAND_BY_DAY)
			return PHONE_COLUMN_EXPAND_BY_DAY;
		else if (expansionLevel == PERSON_TABLE_EXPAND_BY_TASK)
			return PHONE_COLUMN_EXPAND_BY_TASK;
		else
			return PHONE_COLUMN_MIN_EXPANSION;
	}

	public int getColumnForEmail() {
		if (expansionLevel == PERSON_TABLE_EXPAND_BY_DAY)
			return EMAIL_COLUMN_EXPAND_BY_DAY;
		else if (expansionLevel == PERSON_TABLE_EXPAND_BY_TASK)
			return EMAIL_COLUMN_EXPAND_BY_TASK;
		else
			return EMAIL_COLUMN_MIN_EXPANSION;
	}

	public int getColumnForDow() {
		if (expansionLevel == PERSON_TABLE_EXPAND_BY_TASK)
			return DOW_COLUMN;
		else
			return -1;
	}

	public int getColumnForWom() {
		if (expansionLevel == PERSON_TABLE_EXPAND_BY_TASK)
			return WOM_COLUMN;
		else
			return -1;
	}

	public int getColumnForNotes() {
		if (expansionLevel == PERSON_TABLE_EXPAND_WITH_NOTES)
			return NOTES_COLUMN;
		else
			return -1;
	}

	public static int getMinimumExpansion() {
		return PERSON_TABLE_MINIMUM_EXPANSION;
	}

	public static int getExpansionByDay() {
		return PERSON_TABLE_EXPAND_BY_DAY;
	}

	public static int getExpansionByTask() {
		return PERSON_TABLE_EXPAND_BY_TASK;
	}

	public static int getExpansionWithNotes() {
		return PERSON_TABLE_EXPAND_WITH_NOTES;
	}

	private AssignedTasksModel getTaskMatchInAssignedTaskList(String taskName, ArrayList<AssignedTasksModel> list) {
		// TODO: should always return item 0 in list, since list has only 1
		// entry(???)
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getTaskName().equals(taskName))
				return list.get(i);
		}
		return null;
	}
}
