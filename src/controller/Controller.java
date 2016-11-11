package controller;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.LinkedList;

import javax.swing.JList;

import gui.PersonEvent;
import gui.ProgramEvent;
import gui.TaskEvent;
import model.AssignedTasksModel;
import model.CalendarDayModel;
import model.Database;
import model.PersonModel;
import model.ProgramModel;
import model.TaskModel;

public class Controller {
	Database db = new Database();

	/*
	 * ------- Programs -------
	 */
	public void addProgram(ProgramEvent ev) {
		db.addProgram(ev.getProgramName(), ev.getEndDate());
	}

	public void updateProgram(String programName, String endDate) {
		db.updateProgram(programName, endDate);
	}
	
	public void renameProgram(String oldName, String newName) {
		db.renameProgram(oldName, newName);
	}
	
	public ProgramModel getProgramByName(String programName) {
		return db.getProgramByName(programName);
	}

	public JList<String> getAllProgramsAsString() {
		return db.getAllProgramsAsString();
	}
	
	public LinkedList<ProgramModel> getAllPrograms() {
		return db.getAllPrograms();
	}
	
	public int getNumPrograms () {
		return db.getNumPrograms();
	}

	/*
	 * ------- Task data -------
	 */
	public void addTask(TaskEvent ev) {
		TaskModel task = new TaskModel(ev.getTaskName(), ev.getLocation(), ev.getNumStaffReqd(), ev.getTotalPersonsReqd(),
				ev.getDayOfWeek(), ev.getWeekOfMonth(), ev.getTime(), ev.getColor());
		db.addTask(ev.getProgramName(), task);
	}

	public void updateTask(TaskEvent ev) {
		TaskModel task = new TaskModel(ev.getTaskName(), ev.getLocation(), ev.getNumStaffReqd(), ev.getTotalPersonsReqd(),
				ev.getDayOfWeek(), ev.getWeekOfMonth(), ev.getTime(), ev.getColor());
		db.updateTask(ev.getProgramName(), task);
	}

	public void renameTask(String programName, String oldName, String newName) {
		db.renameTask(programName, oldName, newName);
	}

	public TaskModel getTaskByName(String programName, String taskName) {
		return db.getTaskByName(programName, taskName);
	}

	public LinkedList<CalendarDayModel> getTasksByDayByProgram(Calendar calendar, JList<String> programs) {
		return db.getTasksByDayByProgram(calendar, programs);
	}
	
	public LinkedList<CalendarDayModel> getTasksByDayByPerson(Calendar calendar, JList<String> personList) {
		return db.getTasksByDayByPerson(calendar, personList);
	}
	
	public LinkedList<CalendarDayModel> getTasksByDayByStaffShortage(Calendar calendar) {
		return db.getTasksByDayByStaffShortage(calendar);
	}
	
	public LinkedList<CalendarDayModel> getAllTasksByDay(Calendar calendar) {
		return db.getAllTasksByDay(calendar);
	}

	/*
	 * public List<TaskModel> getAllTasks() { return db.getAllTasks(); }
	 */

	public JList<TaskModel> getAllTasks(String programName) {
		return db.getAllTasks(programName);
	}
	
	/*
	public JList<String> getAllTasksAsString(String programName) {
		return db.getAllTasksAsString(programName);
	}
	*/

	/*
	 * ------- Persons -------
	 */
	public void addPerson(String name, String phone, String email, boolean staff, String notes,
			LinkedList<AssignedTasksModel> assignedTasks) {
		db.addPerson(name, phone, email, staff, notes, assignedTasks);	
	}
	
	public void updatePerson(PersonEvent ev) {
		PersonModel person = new PersonModel(ev.getName(), ev.getPhone(), ev.getEmail(), ev.isStaff(), ev.getNotes(),
				ev.getAssignedTasks());
		db.updatePerson(person);
	}
	
	public void renamePerson(String oldName, String newName) {
		db.renamePerson(oldName, newName);
	}
	
	public PersonModel getPersonByName (String name) {
		return db.getPersonByName(name);
	}
	
	public JList<String> getAllPersonsAsString() {
		return db.getAllPersonsAsString();
	}
	
	public JList<PersonModel> getAllPersons() {
		return db.getAllPersons();
	}
	
	public int getNumPersons() {
		return db.getNumPersons();
	}
	
	/*
	 * ------- File save/restore items -------
	 */
	public void saveProgramToFile(JList<String> programNameList, File file) throws IOException {
		db.saveProgramToFile(programNameList, file);
	}

	public void loadProgramFromFile(File file) throws IOException {
		db.loadProgramFromFile(file);
	}
	
	public void saveStaffToFile(File file) throws IOException {
		db.saveStaffToFile(file);
	}

	public void loadStaffFromFile(File file) throws IOException {
		db.loadStaffFromFile(file);
	}
}
