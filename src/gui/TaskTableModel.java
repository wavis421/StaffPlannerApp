package gui;

import javax.swing.JList;
import javax.swing.table.AbstractTableModel;

import model.TaskModel;
import model.TimeModel;
import utilities.Utilities;

public class TaskTableModel extends AbstractTableModel {
	private static final int TASK_NAME_COLUMN = 0;
	private static final int TIME_COLUMN = 1;
	private static final int LOCATION_COLUMN = 2;
	private static final int DAY_OF_WEEK_COLUMN = 3;
	private static final int DOW_IN_MONTH_COLUMN = 4;

	private JList<TaskModel> taskList;
	private String colNames[] = { "Task", "Time", "Location", "Day of Week", "Week of Month" };

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

	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case 1: // Time
			return TimeModel.class;

		default:
			return String.class;
		}
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		TaskModel task = (TaskModel) taskList.getModel().getElementAt(row);

		switch (col) {
		case 0: // task name
			return task.getTaskName();
		case 1: // time
			return task.getTime();
		case 2: // location
			return task.getLocation();
		case 3: // day of week
			return Utilities.getDayOfWeekString(task.getDayOfWeek());
		case 4: // DOW in month
			return Utilities.getWeekOfMonthString(task.getWeekOfMonth());
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
}
