package gui;

import java.awt.Point;
import java.util.Calendar;

import acm.gui.VPanel;

public interface DayBoxListener {
	public void dayBoxClicked (VPanel dayPanel, Calendar calendar, int day, Point point);
}
