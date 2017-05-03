package controller;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.ArrayList;

import javax.swing.JList;

import gui.PersonEvent;
import gui.ProgramEvent;
import gui.TaskEvent;
import model.AssignedTasksModel;
import model.CalendarDayModel;
import model.Database;
import model.DateRangeModel;
import model.MySqlDatabase;
import model.PersonByTaskModel;
import model.PersonModel;
import model.ProgramModel;
import model.SingleInstanceTaskModel;
import model.TaskModel;
import model.TimeModel;

public class Controller {
	Database db = new Database();
	MySqlDatabase sqlDb = new MySqlDatabase();

	/*
	 * ------- Database Connections -------
	 */
	public void disconnectDatabase() {
		sqlDb.disconnectDatabase();
	}

	/*
	 * ------- Programs -------
	 */
	public void addProgram(ProgramEvent ev) {
		sqlDb.addProgram(ev.getProgramName(), ev.getStartDate(), ev.getEndDate());
	}

	public void updateProgram(String programName, String startDate, String endDate) {
		sqlDb.updateProgram(programName, startDate, endDate);
	}

	public void renameProgram(String oldName, String newName) {
		sqlDb.renameProgram(oldName, newName);
	}

	public ProgramModel getProgramByName(String programName) {
		return sqlDb.getProgramByName(programName);
	}

	public JList<String> getAllProgramsAsString() {
		return sqlDb.getAllProgramsAsString();
	}

	public ArrayList<ProgramModel> getAllPrograms() {
		return sqlDb.getAllPrograms();
	}

	public int getNumPrograms() {
		return sqlDb.getNumPrograms();
	}

	/*
	 * ------- Task data -------
	 */
	public void addTask(TaskEvent ev) {
		sqlDb.addTask(ev.getProgramName(), ev.getTaskName(), ev.getLocation(), ev.getNumLeadersReqd(),
				ev.getTotalPersonsReqd(), ev.getDayOfWeek(), ev.getWeekOfMonth(), ev.getTime(), ev.getColor());
	}

	public void updateTask(TaskEvent ev) {
		sqlDb.updateTask(ev.getProgramName(), ev.getTaskName(), ev.getLocation(), ev.getNumLeadersReqd(),
				ev.getTotalPersonsReqd(), ev.getDayOfWeek(), ev.getWeekOfMonth(), ev.getTime(), ev.getColor());
	}

	public void renameTask(String programName, String oldName, String newName) {
		sqlDb.renameTask(programName, oldName, newName);
	}

	public TaskModel getTaskByName(String programName, String taskName) {
		return sqlDb.getTaskByName(programName, taskName);
	}

	public String findProgramByTaskName(String taskName) {
		return sqlDb.findProgramByTaskName(taskName);
	}

	public ArrayList<CalendarDayModel> getTasksByDayByProgram(Calendar calendar, JList<String> programs) {
		System.out.println("getTasksByDayByProgram needs update");
		return db.getTasksByDayByProgram(calendar, programs);
	}

	public ArrayList<CalendarDayModel> getTasksByDayByPerson(Calendar calendar, JList<String> personList) {
		System.out.println("getTasksByDayByPerson needs update");
		return db.getTasksByDayByPerson(calendar, personList);
	}

	public ArrayList<CalendarDayModel> getTasksByDayByIncompleteRoster(Calendar calendar) {
		System.out.println("getTasksByDayByIncompleteRoster needs update");
		return db.getTasksByDayByIncompleteRoster(calendar);
	}

	public ArrayList<CalendarDayModel> getTasksByDayByLocation(Calendar calendar, JList<String> locations) {
		System.out.println("getTasksByDayByLocation needs update");
		return db.getTasksByDayByLocation(calendar, locations);
	}

	public ArrayList<CalendarDayModel> getTasksByDayByTime(Calendar calendar, JList<String> times) {
		System.out.println("getTasksByDayByTime needs update");
		return db.getTasksByDayByTime(calendar, times);
	}

	public ArrayList<CalendarDayModel> getAllTasksAndFloatersByDay(Calendar calendar) {
		System.out.println("getAllTasksAndFloatersByDay needs update");
		return db.getAllTasksAndFloatersByDay(calendar);
	}

	public ArrayList<ArrayList<CalendarDayModel>> getAllTasksAndFloatersByMonth(Calendar calendar) {
		return sqlDb.getAllTasksAndFloatersByMonth(calendar);
	}

	public ArrayList<ArrayList<CalendarDayModel>> getTasksByLocationByMonth(Calendar calendar, JList<String> locations) {
		return sqlDb.getTasksByLocationByMonth(calendar, locations);
	}
	
	public ArrayList<ArrayList<CalendarDayModel>> getTasksByTimeByMonth(Calendar calendar,
			JList<String> times) {
		return sqlDb.getTasksByTimeByMonth(calendar, times);
	}
	
	public ArrayList<ArrayList<CalendarDayModel>> getTasksByPersonsByMonth(Calendar calendar,
			JList<String> personList) {
		return sqlDb.getTasksByPersonsByMonth(calendar, personList);
	}
	
	public JList<TaskModel> getAllTasksByProgram(String programName) {
		return sqlDb.getAllTasksByProgram(programName);
	}

	public JList<TaskModel> getAllTasks() {
		return sqlDb.getAllTasks();
	}

	public JList<String> getAllLocationsAsString() {
		return sqlDb.getAllLocationsAsString();
	}

	public JList<String> getAllTimesAsString() {
		return sqlDb.getAllTimesAsString();
	}

	public JList<TimeModel> getAllTimesByDay(Calendar calendar) {
		return sqlDb.getAllTimesByDay(calendar);
	}

	/*
	 * ------- Persons -------
	 */
	public void addPerson(String name, String phone, String email, boolean leader, String notes,
			ArrayList<AssignedTasksModel> assignedTasks, ArrayList<SingleInstanceTaskModel> extraDates,
			ArrayList<DateRangeModel> datesUnavailable) {
		sqlDb.addPerson(name, phone, email, leader, notes, assignedTasks, extraDates, datesUnavailable);
	}

	public void updatePerson(PersonEvent ev) {
		sqlDb.updatePerson(ev.getName(), ev.getPhone(), ev.getEmail(), ev.isLeader(), ev.getNotes(),
				ev.getAssignedTaskChanges(), ev.getExtraDates(), ev.getDatesUnavailable());
	}

	public void addSingleInstanceTask(JList<String> personList, Calendar day, TaskModel task, int color) {
		for (int i = 0; i < personList.getModel().getSize(); i++) {
			sqlDb.addSingleInstanceTask(personList.getModel().getElementAt(i), day, task, color);
		}
	}

	public void renamePerson(String oldName, String newName) {
		sqlDb.renamePerson(oldName, newName);
	}

	public void markPersonUnavail(String personName, Calendar today) {
		sqlDb.markPersonUnavail(personName, today);
	}

	public boolean checkPersonExists(String personName) {
		return sqlDb.checkPersonExists(personName);
	}
	
	public PersonModel getPersonByName(String name) {
		return sqlDb.getPersonByName(name);
	}

	public JList<String> getAllPersonsAsString() {
		return sqlDb.getAllPersonsAsString();
	}

	public JList<String> getAvailPersonsAsString(Calendar today) {
		return sqlDb.getAvailPersonsAsString(today);
	}

	public ArrayList<PersonByTaskModel> getPersonsByTask(TaskModel task) {
		return sqlDb.getPersonsByTask(task);
	}

	public ArrayList<PersonByTaskModel> getPersonsByDayByTime(Calendar calendar) {
		return sqlDb.getPersonsByDayByTime(calendar);
	}

	public ArrayList<PersonByTaskModel> getPersonsByDayByLocation(Calendar calendar, String location) {
		return sqlDb.getPersonsByDayByLocation(calendar, location);
	}

	public ArrayList<PersonByTaskModel> getPersonsByDay(Calendar calendar) {
		return sqlDb.getPersonsByDay(calendar);
	}

	public ArrayList<PersonByTaskModel> getAllPersons() {
		System.out.println("getAllPersonsList needs update");
		return sqlDb.getAllPersons();
	}

	public int getNumPersons() {
		return sqlDb.getNumPersons();
	}

	public ArrayList<AssignedTasksModel> getAssignedTasks(String personName) {
		return sqlDb.getAssignedTasks(personName);
	}

	public ArrayList<DateRangeModel> getUnavailDates(String personName) {
		return sqlDb.getUnavailDates(personName);
	}

	/*
	 * ------- File save/restore items -------
	 */
	// TODO: Implement any required save/restore features
	public void saveProgramToFile(JList<String> programNameList, File file) throws IOException {
		db.saveProgramToFile(programNameList, file);
	}

	public void loadProgramFromFile(File file) throws IOException {
		db.loadProgramFromFile(file);
	}

	public void loadProgramFromDatabase() {
		db.loadProgramFromDatabase();
	}

	public void loadRosterFromDatabase() {
		db.loadRosterFromDatabase();
	}

	public void saveRosterToFile(File file) throws IOException {
		db.saveRosterToFile(file);
	}

	public void loadRosterFromFile(File file) throws IOException {
		db.loadRosterFromFile(file);
	}
}
