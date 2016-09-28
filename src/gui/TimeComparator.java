package gui;

import java.util.Comparator;

import model.TaskModel;

public class TimeComparator implements Comparator<TaskModel> {
	public int compare(TaskModel o1, TaskModel o2) {
		return (o1.getTime().compareTo(o2.getTime()));
	}
}
