package gui;

import javax.swing.JList;
import javax.swing.table.AbstractTableModel;

import model.TaskModel;
import utilities.Utilities;

public class TaskTableModel extends AbstractTableModel {
	private static final int TASK_NAME_COLUMN = 0;
	private static final int LOCATION_COLUMN = 1;
	private static final int TIME_COLUMN = 2;
	private static final int DAY_OF_WEEK_COLUMN = 3;
	private static final int DOW_IN_MONTH_COLUMN = 4;

	private static final long serialVersionUID = 12340001L;
	private JList<TaskModel> taskList;
	private String colNames[] = { "Task", "Location", "Time", "Day of Week", "Week of Month" };

	public TaskTableModel(JList<TaskModel> taskList) {
		this.taskList = taskList;
	}

	public void setData(JList<TaskModel> db) {
		taskList = db;
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
		return taskList.getModel().getSize();
	}

	@Override
	public Object getValueAt(int row, int col) {
		TaskModel task = (TaskModel) taskList.getModel().getElementAt(row);

		switch (col) {
		case 0: // task name
			return task.getTaskName();
		case 1: // location
			return task.getLocation();
		case 2: // time
			return Utilities.formatTime(task.getTime());
		case 3: // day of week
			return getDayOfWeekString(task.getDayOfWeek());
		case 4: // DOW in month
			return getWeekOfMonthString(task.getWeekOfMonth());
		}
		return null;
	}

	public int getColumnForTaskName() {
		return TASK_NAME_COLUMN;
	}

	public int getColumnForLocation() {
		return LOCATION_COLUMN;
	}

	public int getColumnForTime() {
		return TIME_COLUMN;
	}

	public int getColumnForDayOfWeek() {
		return DAY_OF_WEEK_COLUMN;
	}

	public int getColumnForDowInMonth() {
		return DOW_IN_MONTH_COLUMN;
	}
	
	private String getDayOfWeekString(boolean[] dow) {
		String[] dayName = { "Sun", "Mon", "Tue", "Wed", "Th", "Fri", "Sat" };
		String dowString = "";
		for (int i = 0; i < dow.length; i++) {
			if (dow[i]) {
				if (!dowString.equals(""))
					dowString += "/"; 
				dowString += dayName[i];
			}
		}
		return dowString;
	}
	
	private String getWeekOfMonthString(boolean[] wom) {
		String[] weekName = { "1", "2", "3", "4", "5" };
		String womString = "";
		for (int i = 0; i < wom.length; i++) {
			if (wom[i]) {
				if (!womString.equals(""))
					womString += ", "; 
				womString += weekName[i];
			}
		}
		return womString;
	}
}
