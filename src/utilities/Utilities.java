package utilities;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;

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
}
