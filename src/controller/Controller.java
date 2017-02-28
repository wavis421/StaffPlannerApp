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
import model.DateRangeModel;
import model.PersonByTaskModel;
import model.PersonModel;
import model.ProgramModel;
import model.SingleInstanceTaskModel;
import model.TaskModel;
import model.TimeModel;

public class Controller {
	Database db = new Database();

	/*
	 * ------- Programs -------
	 */
	public void addProgram(ProgramEvent ev) throws Exception {
		db.addProgram(ev.getProgramName(), ev.getStartDate(), ev.getEndDate());
	}

	public void updateProgram(String programName, String startDate, String endDate) {
		db.updateProgram(programName, startDate, endDate);
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

	public int getNumPrograms() {
		return db.getNumPrograms();
	}

	/*
	 * ------- Task data -------
	 */
	public void addTask(TaskEvent ev) throws Exception {
		db.addTask(ev.getProgramName(), ev.getTaskName(), ev.getLocation(), ev.getNumLeadersReqd(),
				ev.getTotalPersonsReqd(), ev.getDayOfWeek(), ev.getWeekOfMonth(), ev.getTime(), ev.getColor());
	}

	public void updateTask(TaskEvent ev) {
		db.updateTask(ev.getProgramName(), ev.getTaskName(), ev.getLocation(), ev.getNumLeadersReqd(),
				ev.getTotalPersonsReqd(), ev.getDayOfWeek(), ev.getWeekOfMonth(), ev.getTime(), ev.getColor());
	}

	public void renameTask(String programName, String oldName, String newName) {
		db.renameTask(programName, oldName, newName);
	}

	public TaskModel getTaskByName(String programName, String taskName) {
		return db.getTaskByName(programName, taskName);
	}

	public String findProgramByTaskName(String taskName) {
		return db.findProgramByTaskName(taskName);
	}

	public LinkedList<CalendarDayModel> getTasksByDayByProgram(Calendar calendar, JList<String> programs) {
		return db.getTasksByDayByProgram(calendar, programs);
	}

	public LinkedList<CalendarDayModel> getTasksByDayByPerson(Calendar calendar, JList<String> personList) {
		return db.getTasksByDayByPerson(calendar, personList);
	}

	public LinkedList<CalendarDayModel> getTasksByDayByIncompleteRoster(Calendar calendar) {
		return db.getTasksByDayByIncompleteRoster(calendar);
	}

	public LinkedList<CalendarDayModel> getTasksByDayByLocation(Calendar calendar, JList<String> locations) {
		return db.getTasksByDayByLocation(calendar, locations);
	}

	public LinkedList<CalendarDayModel> getTasksByDayByTime(Calendar calendar, JList<String> times) {
		return db.getTasksByDayByTime(calendar, times);
	}

	public LinkedList<CalendarDayModel> getAllTasksByDay(Calendar calendar) {
		return db.getAllTasksByDay(calendar);
	}

	public LinkedList<CalendarDayModel> getAllTasksAndFloatersByDay(Calendar calendar) {
		return db.getAllTasksAndFloatersByDay(calendar);
	}

	/*
	 * public List<TaskModel> getAllTasks() { return db.getAllTasks(); }
	 */

	public JList<TaskModel> getAllTasksByProgram(String programName) {
		return db.getAllTasksByProgram(programName);
	}

	public JList<TaskModel> getAllTasks() {
		return db.getAllTasks();
	}

	public JList<String> getAllLocationsAsString() {
		return db.getAllLocationsAsString();
	}

	public JList<String> getAllTimesAsString() {
		return db.getAllTimesAsString();
	}

	public JList<TimeModel> getAllTimesByDay(Calendar calendar) {
		return db.getAllTimesByDay(calendar);
	}

	/*
	public JList<String> getAllTasksAsString(String programName) {
		return db.getAllTasksAsString(programName);
	}
	*/

	/*
	 * ------- Persons -------
	 */
	public void addPerson(String name, String phone, String email, boolean leader, String notes,
			LinkedList<AssignedTasksModel> assignedTasks, LinkedList<SingleInstanceTaskModel> extraDates,
			LinkedList<DateRangeModel> datesUnavailable) throws Exception {
		db.addPerson(name, phone, email, leader, notes, assignedTasks, extraDates, datesUnavailable);
	}

	public void updatePerson(PersonEvent ev) throws Exception {
		db.updatePerson(ev.getName(), ev.getPhone(), ev.getEmail(), ev.isLeader(), ev.getNotes(),
				ev.getAssignedTaskChanges(), ev.getExtraDates(), ev.getDatesUnavailable());
	}

	public void addSingleInstanceTask(JList<String> personList, Calendar day, TaskModel task, int color) throws Exception {
		for (int i = 0; i < personList.getModel().getSize(); i++) {
			db.addSingleInstanceTask(personList.getModel().getElementAt(i), day, task, color);
		}
	}

	public void renamePerson(String oldName, String newName) {
		db.renamePerson(oldName, newName);
	}

	public void markPersonUnavail(String personName, Calendar today) throws Exception {
		db.markPersonUnavail(personName, today);
	}

	public PersonModel getPersonByName(String name) {
		return db.getPersonByName(name);
	}

	public JList<String> getAllPersonsAsString() {
		return db.getAllPersonsAsString();
	}

	public JList<String> getAvailPersonsAsString(Calendar today) {
		return db.getAvailPersonsAsString(today);
	}

	public JList<PersonModel> getAllPersons() {
		return db.getAllPersons();
	}

	public LinkedList<PersonByTaskModel> getPersonsByTask(TaskModel task) {
		return db.getPersonsByTask(task);
	}

	public LinkedList<PersonByTaskModel> getPersonsByDayByTask(Calendar calendar, TaskModel task) {
		return db.getPersonsByDayByTask(calendar, task);
	}

	public LinkedList<PersonByTaskModel> getPersonsByDayByTime(Calendar calendar) {
		return db.getPersonsByDayByTime(calendar);
	}

	public LinkedList<PersonByTaskModel> getPersonsByDayByLocation(Calendar calendar, String location) {
		return db.getPersonsByDayByLocation(calendar, location);
	}

	public LinkedList<PersonByTaskModel> getPersonsByDay(Calendar calendar) {
		return db.getPersonsByDay(calendar);
	}

	public LinkedList<PersonByTaskModel> getAllPersonsList() {
		return db.getAllPersonsList();
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

	public void loadProgramFromDatabase() throws Exception {
		db.loadProgramFromDatabase();
	}

	public void loadRosterFromDatabase() throws Exception {
		db.loadRosterFromDatabase();
	}

	public void saveRosterToFile(File file) throws IOException {
		db.saveRosterToFile(file);
	}

	public void loadRosterFromFile(File file) throws IOException {
		db.loadRosterFromFile(file);
	}
}
