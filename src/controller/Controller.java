package controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import javax.swing.JList;

import gui.PersonEvent;
import gui.PersonTableNotesModel;
import gui.ProgramEvent;
import gui.TaskEvent;
import model.AssignedTasksModel;
import model.CalendarDayModel;
import model.DateRangeModel;
import model.MySqlDatabase;
import model.PersonByTaskModel;
import model.PersonModel;
import model.ProgramModel;
import model.SingleInstanceTaskModel;
import model.TaskModel;
import model.TaskTimeModel;
import model.TimeModel;

public class Controller {
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

	public void deleteProgram(String programName) {
		sqlDb.deleteProgram(programName);
	}

	public ProgramModel getProgramByName(String programName) {
		return sqlDb.getProgramByName(programName);
	}

	public ArrayList<String> getAllProgramsAsString() {
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

	public void updateTask(int taskID, TaskEvent ev, TimeModel origTaskTime) {
		sqlDb.updateTask(taskID, ev.getProgramName(), ev.getTaskName(), ev.getLocation(), ev.getNumLeadersReqd(),
				ev.getTotalPersonsReqd(), ev.getDayOfWeek(), ev.getWeekOfMonth(), ev.getTime(), ev.getColor(),
				origTaskTime);
	}

	public void updateSingleInstanceTaskId(String personName, Calendar calendar, String taskName) {
		sqlDb.updateSingleInstanceTaskId(personName, calendar, taskName);
	}

	public TaskModel renameTask(String programName, String oldName, String newName) {
		return sqlDb.renameTask(programName, oldName, newName);
	}

	public void deleteTask(String taskName) {
		sqlDb.deleteTask(taskName);
	}

	public TaskModel getTaskByName(String taskName) {
		return sqlDb.getTaskByName(taskName);
	}

	public String findProgramByTaskName(String taskName) {
		return sqlDb.findProgramByTaskName(taskName);
	}

	public ArrayList<ArrayList<CalendarDayModel>> getAllTasksAndFloatersByMonth(Calendar calendar) {
		return sqlDb.getAllTasksAndFloatersByMonth(calendar);
	}

	public ArrayList<ArrayList<CalendarDayModel>> getTasksByLocationByMonth(Calendar calendar,
			ArrayList<String> locations) {
		return sqlDb.getTasksByLocationByMonth(calendar, locations);
	}

	public ArrayList<ArrayList<CalendarDayModel>> getTasksByTimeByMonth(Calendar calendar, ArrayList<String> times) {
		return sqlDb.getTasksByTimeByMonth(calendar, times);
	}

	public ArrayList<ArrayList<CalendarDayModel>> getTasksByPersonsByMonth(Calendar calendar,
			ArrayList<String> personList) {
		return sqlDb.getTasksByPersonsByMonth(calendar, personList);
	}

	public ArrayList<ArrayList<CalendarDayModel>> getTasksByProgramByMonth(Calendar calendar,
			ArrayList<String> programList) {
		return sqlDb.getTasksByProgramByMonth(calendar, programList);
	}

	public ArrayList<ArrayList<CalendarDayModel>> getTasksByIncompleteRosterByMonth(Calendar calendar) {
		return sqlDb.getTasksByIncompleteRosterByMonth(calendar);
	}

	public JList<TaskModel> getAllTasksByProgram(String programName) {
		return sqlDb.getAllTasksByProgram(programName);
	}

	public ArrayList<TaskModel> getAllTasks() {
		return sqlDb.getAllTasks();
	}

	public ArrayList<TaskTimeModel> getAllTasksWithTimeByDay(Calendar calendar) {
		return sqlDb.getAllTasksWithTimeByDay(calendar);
	}

	public ArrayList<String> getAllLocationsAsString() {
		return sqlDb.getAllLocationsAsString();
	}

	public ArrayList<String> getAllTimesAsString() {
		return sqlDb.getAllTimesAsString();
	}

	public ArrayList<TimeModel> getAllTimesByDay(Calendar calendar) {
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

	public void updatePersonNotes(ArrayList<PersonTableNotesModel> personNotesList) {
		for (int i = 0; i < personNotesList.size(); i++) {
			PersonTableNotesModel item = personNotesList.get(i);
			sqlDb.updatePersonNotes(item.getPersonName(), item.getPersonNotes());
		}
	}

	public void removePerson(String personName) {
		sqlDb.removePerson(personName);
	}

	public void addSingleInstanceTask(ArrayList<String> personList, String programName, Calendar day, TaskModel task,
			int color) {
		for (int i = 0; i < personList.size(); i++) {
			sqlDb.addSingleInstanceTask(personList.get(i), programName, day, task, color);
		}
	}

	public void removeSingleInstanceTask(String personName, Calendar singleDate) {
		sqlDb.removeSingleInstanceTask(personName, singleDate);
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

	public ArrayList<String> getAllPersonsAsString() {
		return sqlDb.getAllPersonsAsString();
	}

	public ArrayList<String> getAvailPersonsAsString(Calendar today) {
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
		return sqlDb.getAllPersons();
	}

	public ArrayList<PersonByTaskModel> getActivePersons() {
		return sqlDb.getActivePersons();
	}

	public ArrayList<PersonByTaskModel> getAllPersonsWithNotes() {
		return sqlDb.getAllPersonsWithNotes();
	}

	public int getNumPersons() {
		return sqlDb.getNumPersons();
	}

	public ArrayList<DateRangeModel> getUnavailDates(String personName) {
		return sqlDb.getUnavailDates(personName);
	}

	/*
	 * ------- File save/restore items -------
	 */
	// TODO: Implement any required save/restore features
	public void saveProgramToFile(ArrayList<String> programNameList, File file) throws IOException {
		sqlDb.saveProgramToFile(programNameList, file);
	}

	public void loadProgramFromFile(File file) throws IOException {
		sqlDb.loadProgramFromFile(file);
	}

	public void loadProgramFromDatabase() {
		sqlDb.loadProgramFromDatabase();
	}

	public void loadRosterFromDatabase() {
		sqlDb.loadRosterFromDatabase();
	}

	public void saveRosterToFile(File file) throws IOException {
		sqlDb.saveRosterToFile(file);
	}

	public void loadRosterFromFile(File file) throws IOException {
		sqlDb.loadRosterFromFile(file);
	}
}
