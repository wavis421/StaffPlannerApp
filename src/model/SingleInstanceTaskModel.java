package model;

import java.io.Serializable;
import java.util.Calendar;

public class SingleInstanceTaskModel implements Serializable, Comparable<SingleInstanceTaskModel> {
	private static final long serialVersionUID = 12340001L;
	private String taskName;
	private Calendar taskDate;
	
	public SingleInstanceTaskModel (String taskName, Calendar taskDate) {
		this.taskName = taskName;
		this.taskDate = taskDate;
	}
	
	public String getTaskName() {
		return taskName;
	}

	public Calendar getTaskDate() {
		return taskDate;
	}

	public int compareTo(SingleInstanceTaskModel arg0) {
		// TODO Auto-generated method stub
		return 0;
	}
}
