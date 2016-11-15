package gui;

import java.util.EventObject;
import java.util.LinkedList;

import model.AssignedTasksModel;
import model.DateRangeModel;

public class PersonEvent extends EventObject {
	private String name;
	private String phone;
	private String email;
	private boolean leader;
	private String notes;
	private LinkedList<AssignedTasksModel> assignedTasks;
	private AssignedTasksModel lastTaskAdded;
	private DateRangeModel datesUnavailable;

	public PersonEvent(Object source) {
		super(source);
	}

	public PersonEvent(Object source, String name, String phone, String email, boolean leader, String notes,
			LinkedList<AssignedTasksModel> assignedTasks, AssignedTasksModel lastTaskAdded,
			DateRangeModel datesUnavailable) {
		super(source);

		this.name = name;
		this.phone = phone;
		this.email = email;
		this.leader = leader;
		this.notes = notes;
		this.assignedTasks = assignedTasks;
		this.lastTaskAdded = lastTaskAdded;
		this.datesUnavailable = datesUnavailable;
	}

	public String getName() {
		return name;
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

	public AssignedTasksModel getLastTaskAdded() {
		return lastTaskAdded;
	}

	public DateRangeModel getDatesUnavailable() {
		return datesUnavailable;
	}
}
