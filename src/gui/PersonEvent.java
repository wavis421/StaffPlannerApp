package gui;

import java.util.ArrayList;
import java.util.EventObject;

import javax.swing.JList;

import model.AssignedTasksModel;
import model.DateRangeModel;
import model.ProgramModel;
import model.SingleInstanceTaskModel;
import model.TaskModel;

public class PersonEvent extends EventObject {
	private int personID;
	private String name;
	private String phone;
	private String email;
	private boolean leader;
	private String notes;
	private ArrayList<AssignedTasksModel> assignedTaskChanges;
	private ArrayList<SingleInstanceTaskModel> extraDates;
	private ArrayList<DateRangeModel> datesUnavailable;

	// Lists used to create task trees
	private JList<TaskModel> allTasks;
	private ArrayList<ProgramModel> programList;
	private ArrayList<JList<TaskModel>> taskListByProgram;
	private ArrayList<ArrayList<AssignedTasksModel>> assignedTaskListByProgram;

	public PersonEvent(Object source) {
		super(source);
	}

	public PersonEvent(Object source, int personID, String name, String phone, String email, boolean leader,
			String notes, ArrayList<AssignedTasksModel> assignedTaskChanges,
			ArrayList<SingleInstanceTaskModel> extraDates, ArrayList<DateRangeModel> datesUnavailable,
			JList<TaskModel> allTasks, ArrayList<ProgramModel> programList,
			ArrayList<JList<TaskModel>> taskListByProgram,
			ArrayList<ArrayList<AssignedTasksModel>> assignedTaskListByProgram) {
		super(source);

		this.personID = personID;
		this.name = name;
		this.phone = phone;
		this.email = email;
		this.leader = leader;
		this.notes = notes;
		this.assignedTaskChanges = assignedTaskChanges;
		this.extraDates = extraDates;
		this.datesUnavailable = datesUnavailable;

		this.allTasks = allTasks;
		this.programList = programList;
		this.taskListByProgram = taskListByProgram;
		this.assignedTaskListByProgram = assignedTaskListByProgram;
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

	public ArrayList<SingleInstanceTaskModel> getExtraDates() {
		return extraDates;
	}

	public ArrayList<DateRangeModel> getDatesUnavailable() {
		return datesUnavailable;
	}

	public JList<TaskModel> getAllTasks() {
		return allTasks;
	}

	public ArrayList<ProgramModel> getProgramList() {
		return programList;
	}

	public ArrayList<JList<TaskModel>> getTaskListByProgram() {
		return taskListByProgram;
	}

	public ArrayList<ArrayList<AssignedTasksModel>> getAssignedTaskListByProgram() {
		return assignedTaskListByProgram;
	}
}
