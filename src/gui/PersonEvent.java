package gui;

import java.util.EventObject;
import java.util.LinkedList;

import model.AssignedTasksModel;

public class PersonEvent extends EventObject {
	private String name;
	private String phone;
	private String email;
	private boolean staff;
	private String notes;
	private LinkedList<AssignedTasksModel> assignedTasks;
	private AssignedTasksModel lastTaskAdded;
	
	public PersonEvent(Object source) {
		super(source);
	}

	public PersonEvent(Object source, String name, String phone, String email, boolean staff, String notes,
			LinkedList<AssignedTasksModel> assignedTasks, AssignedTasksModel lastTaskAdded) {
		super(source);

		this.name = name;
		this.phone = phone;
		this.email = email;
		this.staff = staff;
		this.notes = notes;
		this.assignedTasks = assignedTasks;
		this.lastTaskAdded = lastTaskAdded;
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

	public boolean isStaff() {
		return staff;
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
}
