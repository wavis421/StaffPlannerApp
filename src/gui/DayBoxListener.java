package gui;

import java.awt.Point;
import java.util.Calendar;

import acm.gui.VPanel;

public interface DayBoxListener {
	public void dayBoxClicked (Calendar calendar, Point point);
}
