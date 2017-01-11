package utilities;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.swing.DefaultListModel;
import javax.swing.JList;

public class Utilities {
	// Time format for hour 1 - 12 and AM/PM field
	private static final SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");

	public static String formatTime(Time time) {
		// Set time and convert to String
		Calendar cal = Calendar.getInstance();
		cal.setTime(time);
		return formatTime(cal);
	}

	public static String formatTime(Calendar cal) {
		Calendar localCal = (Calendar) cal.clone();

		// Convert HOUR from 0-11 to 1-12
		localCal.add(Calendar.HOUR, 1);

		// If hour transitioned to 12:00 am/pm, then switch the AM/PM
		int hour = localCal.get(Calendar.HOUR);
		if (hour == 0 || hour == 12)
			localCal.set(Calendar.AM_PM, localCal.get(Calendar.AM_PM) == Calendar.AM ? Calendar.PM : Calendar.AM);

		return timeFormat.format(localCal.getTime());
	}

	public static boolean checkForTimeMatch(Time time1, Calendar time2) {
		Calendar time1Cal = Calendar.getInstance();
		time1Cal.setTime(time1);

		return checkForTimeMatch(time1Cal, time2);
	}

	public static boolean checkForTimeMatch(Calendar time1, Calendar time2) {
		if (time1.get(Calendar.HOUR) == time2.get(Calendar.HOUR)
				&& time1.get(Calendar.MINUTE) == time2.get(Calendar.MINUTE)
				&& time1.get(Calendar.AM_PM) == time2.get(Calendar.AM_PM)) {
			return true;
		} else {
			return false;
		}
	}

	public static void addTimeToCalendar(Calendar calendar, Time time) {
		Calendar timeCal = Calendar.getInstance();
		timeCal.setTime(time);

		calendar.set(Calendar.HOUR, timeCal.get(Calendar.HOUR));
		calendar.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
		calendar.set(Calendar.AM_PM, timeCal.get(Calendar.AM_PM));
	}

	public static Time getTimeFromCalendar(Calendar calendar) {
		int newHour = calendar.get(Calendar.HOUR);
		int newMinute = calendar.get(Calendar.MINUTE);
		if (calendar.get(Calendar.AM_PM) == Calendar.PM)
			newHour += 12;

		return (Time.valueOf(newHour + ":" + newMinute + ":00"));
	}

	public static String getDisplayDate(Calendar calendar) {
		return ((calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.DAY_OF_MONTH) + "/"
				+ calendar.get(Calendar.YEAR));
	}

	public static boolean findStringMatchInJList(String findString, JList<String> list) {
		for (int i = 0; i < list.getModel().getSize(); i++) {
			if (findString == null || list.getModel().getElementAt(i) == null)
				return false;
			
			if (list.getModel().getElementAt(i).equals(findString))
				return true;
		}
		return false;
	}

	public static JList<String> removeDuplicateEntriesInJlist(JList<String> inputList) {
		DefaultListModel<String> newListModel = new DefaultListModel<String>();
		JList<String> newJList = new JList<String>(newListModel);

		for (int i = 0; i < inputList.getModel().getSize(); i++) {
			String findString = inputList.getModel().getElementAt(i);
			if (!findStringMatchInJList(findString, newJList)) {
				newListModel.addElement(findString);
			}
		}
		return newJList;
	}
	
	public static void memoryCheck (String codeLocation) {
	    // Get the Java runtime
	    Runtime runtime = Runtime.getRuntime();
	    runtime.gc();
	    
	    long memory = runtime.totalMemory() - runtime.freeMemory();
	    System.out.println("Used memory in bytes (" + codeLocation + "): " + memory);
	    
	}
}
