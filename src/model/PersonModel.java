package model;

import java.io.Serializable;
import java.util.LinkedList;

public class PersonModel implements Serializable {
	private static final long serialVersionUID = 12340001L;
	private String name;
	private String phone;
	private String email;
	private boolean staff; // Staff or volunteer
	private String notes;
	private LinkedList<AssignedTasksModel> assignedTasks;

	public PersonModel(String name, String phone, String email, boolean staff, String notes,
			LinkedList<AssignedTasksModel> assignedTasks) {
		this.name = name;
		this.phone = phone;
		this.email = email;
		this.staff = staff;
		this.notes = notes;
		this.assignedTasks = assignedTasks;
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

	public boolean isStaff() {
		return staff;
	}

	public String getNotes() {
		return notes;
	}

	public LinkedList<AssignedTasksModel> getAssignedTasks() {
		return assignedTasks;
	}
}
