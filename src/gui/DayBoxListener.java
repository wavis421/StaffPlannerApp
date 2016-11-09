package gui;

import java.awt.Point;
import java.util.Calendar;

import model.CalendarDayModel;

public interface DayBoxListener {
	public void dayBoxClicked (Calendar calendar, Point point, CalendarDayModel task);
}
