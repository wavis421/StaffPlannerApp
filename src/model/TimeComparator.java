package model;

import java.util.Comparator;

public class TimeComparator implements Comparator<CalendarDayModel> {
	public int compare(CalendarDayModel calDay1, CalendarDayModel calDay2) {
		return calDay1.getTask().getTime().compareTo(calDay2.getTask().getTime());
	}
}
