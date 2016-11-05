package gui;

import java.util.Comparator;

import model.PersonModel;

public class PersonComparator implements Comparator<PersonModel> {
	public int compare(PersonModel o1, PersonModel o2) {
		return (o1.getName().compareTo(o2.getName()));
	}
}
