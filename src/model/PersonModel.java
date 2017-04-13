package model;

import java.io.Serializable;
import java.util.ArrayList;

public class PersonModel implements Serializable, Comparable<PersonModel> {
	private static final long serialVersionUID = 12340004L;
	private int personID;
	private String name;
	private String phone;
	private String email;
	private boolean leader; // Leader or assistant
	private String notes;
	private ArrayList<AssignedTasksModel> assignedTasks;
	private ArrayList<DateRangeModel> datesUnavailable;
	private ArrayList<SingleInstanceTaskModel> singleInstanceTasks;

	public PersonModel(int personID, String name, String phone, String email, boolean leader, String notes,
			ArrayList<AssignedTasksModel> assignedTasks, ArrayList<DateRangeModel> datesUnavailable,
			ArrayList<SingleInstanceTaskModel> singleInstanceTaskAssignment) {
		this.personID = personID;
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

	public int getPersonID() {
		return personID;
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

	public ArrayList<AssignedTasksModel> getAssignedTasks() {
		return assignedTasks;
	}

	public ArrayList<DateRangeModel> getDatesUnavailable() {
		return datesUnavailable;
	}

	public void setDatesUnavailable(ArrayList<DateRangeModel> datesUnavail) {
		this.datesUnavailable.clear();
		this.datesUnavailable = datesUnavail;
	}

	public ArrayList<SingleInstanceTaskModel> getSingleInstanceTasks() {
		return singleInstanceTasks;
	}

	@Override
	public int compareTo(PersonModel otherPerson) {
		return (this.getName().compareTo(otherPerson.getName()));
	}
}
