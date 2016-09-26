package gui;

import java.awt.Point;
import java.util.Calendar;
import java.util.LinkedList;

import acm.gui.VPanel;
import model.TaskModel;

public interface DayBoxListener {
	public void dayBoxClicked (Calendar calendar, Point point, TaskModel task);
}
