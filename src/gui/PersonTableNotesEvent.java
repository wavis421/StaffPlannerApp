package gui;

import java.util.ArrayList;
import java.util.EventObject;

public class PersonTableNotesEvent extends EventObject {
	private ArrayList<PersonTableNotesModel> personNotesList;

	public PersonTableNotesEvent(Object source, ArrayList<PersonTableNotesModel> personNotesList) {
		super(source);
		this.personNotesList = personNotesList;
	}

	public ArrayList<PersonTableNotesModel> getPersonNotesList() {
		return personNotesList;
	}
}
