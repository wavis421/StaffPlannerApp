package model;

import java.io.Serializable;
import java.util.LinkedList;

public class PersonModel implements Serializable, Comparable<PersonModel> {
	private static final long serialVersionUID = 12340003L;
	private String name;
	private String phone;
	private String email;
	private boolean leader; // Leader or volunteer
	private String notes;
	private LinkedList<AssignedTasksModel> assignedTasks;
	private DateRangeModel datesUnavailable;
	private LinkedList<SingleInstanceTaskModel> singleInstanceTasks;

	public PersonModel(String name, String phone, String email, boolean leader, String notes,
			LinkedList<AssignedTasksModel> assignedTasks, DateRangeModel datesUnavailable,
			LinkedList<SingleInstanceTaskModel> singleInstanceTaskAssignment) {
		this.name = name;
		this.phone = phone;
		this.email = email;
		this.leader = leader;
		this.notes = notes;
		this.assignedTasks = assignedTasks;
		this.datesUnavailable = datesUnavailable;
		this.singleInstanceTasks = singleInstanceTaskAssignment;
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

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public boolean isLeader() {
		return leader;
	}

	public void setLeader(boolean leader) {
		this.leader = leader;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public LinkedList<AssignedTasksModel> getAssignedTasks() {
		return assignedTasks;
	}

	public DateRangeModel getDatesUnavailable() {
		return datesUnavailable;
	}

	public void setDatesUnavailable(DateRangeModel datesUnavailable) {
		this.datesUnavailable = datesUnavailable;
	}

	public LinkedList<SingleInstanceTaskModel> getSingleInstanceTasks() {
		return singleInstanceTasks;
	}

	@Override
	public int compareTo(PersonModel otherPerson) {
		return (this.getName().compareTo(otherPerson.getName()));
	}
}
