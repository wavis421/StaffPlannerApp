package gui;

import java.util.Calendar;
import java.util.EventObject;

import model.DateRangeModel;
import model.TaskModel;
import utilities.Utilities;

public class DateRangeEvent extends EventObject {
	private TaskModel task;
	private Calendar startDate;
	private Calendar endDate;

	public DateRangeEvent(Object source, TaskModel task, Calendar startDate, Calendar endDate) {
		super(source);

		this.task = task;
		this.startDate = startDate;
		this.endDate = endDate;
	}

	public String toString() {
		if (startDate != null && endDate != null)
			return (Utilities.getDisplayDate(startDate) + "  to  " + Utilities.getDisplayDate(endDate));
		else
			return "";
	}
	
	public TaskModel getTask() {
		return task;
	}

	public Calendar getStartDate() {
		return startDate;
	}

	public DateRangeModel getDateRange() {
		return new DateRangeModel(0, 0, Utilities.getDisplayDate(startDate), Utilities.getDisplayDate(endDate));
	}

	public DateRangeModel getSqlDateRange() {
		return new DateRangeModel(0, 0, Utilities.getSqlDate(startDate), Utilities.getSqlDate(endDate));
	}
}
