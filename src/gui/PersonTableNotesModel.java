package gui;

public class PersonTableNotesModel implements Comparable<PersonTableNotesModel> {
	private String person;
	private String notes;

	public PersonTableNotesModel(String person) {
		this.person = person;
		this.notes = null;
	}

	public String toString() {
		return person;
	}

	public String getPersonName() {
		return person;
	}

	public String getPersonNotes() {
		return notes;
	}

	public void setPersonNotes(String notes) {
		this.notes = notes;
	}

	@Override
	public int compareTo(PersonTableNotesModel otherNotes) {
		return (person.compareTo(otherNotes.getPersonName()));
	}
}
