package gui;

import java.util.ArrayList;
import java.util.EventObject;

import model.AssignedTasksModel;
import model.DateRangeModel;
import model.SingleInstanceTaskModel;

public class PersonEvent extends EventObject {
	private int personID;
	private String name;
	private String phone;
	private String email;
	private boolean leader;
	private String notes;
	private ArrayList<AssignedTasksModel> assignedTaskChanges;
	private AssignedTasksModel lastTaskAdded;
	private ArrayList<SingleInstanceTaskModel> extraDates;
	private ArrayList<DateRangeModel> datesUnavailable;

	public PersonEvent(Object source) {
		super(source);
	}

	public PersonEvent(Object source, int personID, String name, String phone, String email, boolean leader, String notes,
			ArrayList<AssignedTasksModel> assignedTaskChanges, AssignedTasksModel lastTaskAdded,
			ArrayList<SingleInstanceTaskModel> extraDates, ArrayList<DateRangeModel> datesUnavailable) {
		super(source);

		this.personID = personID;
		this.name = name;
		this.phone = phone;
		this.email = email;
		this.leader = leader;
		this.notes = notes;
		this.assignedTaskChanges = assignedTaskChanges;
		this.lastTaskAdded = lastTaskAdded;
		this.extraDates = extraDates;
		this.datesUnavailable = datesUnavailable;
	}

	public int getPersonID() {
		return personID;
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

	public ArrayList<AssignedTasksModel> getAssignedTaskChanges() {
		return assignedTaskChanges;
	}

	public AssignedTasksModel getLastTaskAdded() {
		return lastTaskAdded;
	}

	public ArrayList<SingleInstanceTaskModel> getExtraDates() {
		return extraDates;
	}
	
	public ArrayList<DateRangeModel> getDatesUnavailable() {
		return datesUnavailable;
	}
}
