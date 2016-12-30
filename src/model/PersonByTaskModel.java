package model;

import java.util.Calendar;

public class PersonByTaskModel implements Comparable<PersonByTaskModel> {
	private PersonModel person;
	private TaskModel task;
	private boolean isSubstitute;
	private int taskColor;
	private Calendar taskDate;
	
	public PersonByTaskModel(PersonModel person, TaskModel task, boolean isSubstitute, int color, Calendar taskDate) {
		this.person = person;
		this.task = task;
		this.isSubstitute = isSubstitute;
		this.taskColor = color;
		this.taskDate = (Calendar) taskDate.clone();
	}

	public String toString () {
		return person.getName();
	}
	
	public PersonModel getPerson() {
		return person;
	}

	public TaskModel getTask() {
		return task;
	}

	public boolean isSubstitute() {
		return isSubstitute;
	}

	public int getTaskColor() {
		return taskColor;
	}

	public Calendar getTaskDate() {
		return taskDate;
	}

	@Override
	public int compareTo(PersonByTaskModel otherPerson) {
		return (this.getPerson().getName().compareTo(otherPerson.getPerson().getName()));
	}
}
