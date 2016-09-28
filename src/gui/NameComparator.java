package gui;

import java.util.Comparator;

import model.TaskModel;

public class NameComparator implements Comparator<TaskModel> {
	public int compare(TaskModel o1, TaskModel o2) {
		return (o1.getTaskName().compareTo(o2.getTaskName()));
	}
}
