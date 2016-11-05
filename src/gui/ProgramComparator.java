package gui;

import java.util.Comparator;

import model.ProgramModel;

public class ProgramComparator implements Comparator<ProgramModel> {
	public int compare(ProgramModel o1, ProgramModel o2) {
		return (o1.getProgramName().compareTo(o2.getProgramName()));
	}
}
