package model;

import java.io.Serializable;
import java.util.LinkedList;

public class PersonModel implements Serializable {
	private static final long serialVersionUID = 12340001L;
	private String name;
	private String phone;
	private String email;
	private boolean leader; // Leader or volunteer
	private String notes;
	private LinkedList<AssignedTasksModel> assignedTasks;
	private DateRangeModel datesUnavailable;

	public PersonModel(String name, String phone, String email, boolean leader, String notes,
			LinkedList<AssignedTasksModel> assignedTasks, DateRangeModel datesUnavailable) {
		this.name = name;
		this.phone = phone;
		this.email = email;
		this.leader = leader;
		this.notes = notes;
		this.assignedTasks = assignedTasks;
		this.datesUnavailable = datesUnavailable;
	}

	public String toString() {
		return name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPhone() {
		return phone;
	}

	public String getEmail() {
		return email;
	}

	public boolean isLeader() {
		return leader;
	}

	public String getNotes() {
		return notes;
	}

	public LinkedList<AssignedTasksModel> getAssignedTasks() {
		return assignedTasks;
	}

	public DateRangeModel getDatesUnavailable() {
		return datesUnavailable;
	}
}
