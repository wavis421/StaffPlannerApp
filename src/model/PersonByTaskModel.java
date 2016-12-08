package model;

public class PersonByTaskModel implements Comparable<PersonByTaskModel> {
	private PersonModel person;
	private TaskModel task;
	private boolean isSubstitute;
	
	public PersonByTaskModel(PersonModel person, TaskModel task, boolean isSubstitute) {
		this.person = person;
		this.task = task;
		this.isSubstitute = isSubstitute;
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

	@Override
	public int compareTo(PersonByTaskModel otherPerson) {
		return (this.getPerson().getName().compareTo(otherPerson.getPerson().getName()));
	}
}
