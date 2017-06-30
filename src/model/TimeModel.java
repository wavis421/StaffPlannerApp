package model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TimeModel implements Comparable<TimeModel>, Serializable {
	private static final long serialVersionUID = 12340001L;
	private Calendar calTime = Calendar.getInstance();
	private static final SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");

	public TimeModel(int hour, int minute) {
		this.calTime.set(0, 0, 0);  // Clear year/month/day
		
		if (hour >= 12) {
			this.calTime.set(Calendar.HOUR, hour - 12);
			this.calTime.set(Calendar.AM_PM, Calendar.PM);
		}
		else {
			this.calTime.set(Calendar.HOUR, hour);
			this.calTime.set(Calendar.AM_PM, Calendar.AM);
		}
		this.calTime.set(Calendar.MINUTE, minute);
	}
	
	public TimeModel(Calendar calendar) {
		this.calTime = (Calendar) calendar.clone();
		this.calTime.set(0, 0, 0);  // Clear year/month/day
	}

	public String toString() {
		return timeFormat.format(calTime.getTime());
	}
	
	public Calendar getCalTime() {
		return calTime;
	}
	
	public int getHour() {
		return calTime.get(Calendar.HOUR);
	}
	
	public int get24Hour() {
		if (getAmPm() == Calendar.PM)
			return getHour() + 12;
		else
			return getHour();
	}
	
	public int getMinute() {
		return calTime.get(Calendar.MINUTE);
	}
	
	public int getAmPm() {
		return calTime.get(Calendar.AM_PM);
	}

	@Override
	public int compareTo(TimeModel otherTime) {
		if (this.get24Hour() == otherTime.get24Hour() && this.getMinute() == otherTime.getMinute())
			return 0;
		else if (this.get24Hour() < otherTime.get24Hour()) 
			return -1; 
		else if (this.get24Hour() > otherTime.get24Hour())
			return 1;
		else if (this.getMinute() < otherTime.getMinute())
			return -1;
		else
			return 1;
	}
}
