package gui;

import java.util.Comparator;

import model.AssignedTasksModel;

public class AssignedTaskComparator implements Comparator<AssignedTasksModel> {
	public int compare(AssignedTasksModel o1, AssignedTasksModel o2) {
		// Later get this sorted by task time
		return (o1.getTaskName().compareTo(o2.getTaskName()));
	}
}
