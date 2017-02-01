package utilities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JOptionPane;

import org.jdatepicker.impl.JDatePickerImpl;

import model.TimeModel;

public class Utilities {
	// Time format for hour 1 - 12 and AM/PM field
	private static final SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");
	private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy");

	/* <<<<<<<<<< Calendar & Time Utilities >>>>>>>>>> */
	public static String formatTime(Calendar cal) {
		return getFormattedTime((Calendar) cal.clone());
	}

	public static String formatTime(TimeModel time) {
		Calendar localCal = Calendar.getInstance();
		addTimeToCalendar(localCal, time);
		return getFormattedTime(localCal);
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

	public static void addTimeToCalendar(Calendar calendar, TimeModel time) {
		calendar.set(Calendar.HOUR, time.getHour());
		calendar.set(Calendar.MINUTE, time.getMinute());
		calendar.set(Calendar.AM_PM, time.getAmPm());
	}

	public static String getDisplayDate(Calendar calendar) {
		String month = String.format("%02d", calendar.get(Calendar.MONTH) + 1);
		return (month + "/" + calendar.get(Calendar.DAY_OF_MONTH) + "/" + calendar.get(Calendar.YEAR));
	}

	public static boolean isDateInThePast(String dateString, String errorString) {
		Calendar today = Calendar.getInstance();
		Calendar date = Calendar.getInstance();

		try {
			date.setTime(dateFormatter.parse(dateString));

		} catch (ParseException e) {
			JOptionPane.showMessageDialog(null, "Unable to parse date '" + dateString + "': " + e.getMessage(),
					errorString, JOptionPane.ERROR_MESSAGE);
			return true;
		}

		// Ignore time
		today.set(Calendar.HOUR_OF_DAY, 0);
		today.set(Calendar.MINUTE, 0);
		today.set(Calendar.SECOND, 0);
		today.set(Calendar.MILLISECOND, 0);

		if (today.compareTo(date) > 0)
			return true;
		else
			return false;
	}

	public static void checkStartEndDatePicker(String name, JDatePickerImpl startDatePicker,
			JDatePickerImpl endDatePicker) {
		String startText = startDatePicker.getJFormattedTextField().getText();
		String endText = endDatePicker.getJFormattedTextField().getText();

		// If end date is NULL, set it to start day
		if (name.equals("start") && !startText.equals("")) {
			if (endText.equals("")) {
				endDatePicker = setDate(startDatePicker, endDatePicker);
				endDatePicker.getJFormattedTextField().setText(startText);
				endText = startText;
			}
			startDatePicker.getModel().setSelected(true);
			endDatePicker.getModel().setSelected(true);
		}
		// If start date is NULL, set it to end day
		if (name.equals("end") && !endText.equals("")) {
			if (startText.equals("")) {
				startDatePicker = setDate(endDatePicker, startDatePicker);
				startDatePicker.getJFormattedTextField().setText(endText);
				startText = endText;
			}
			startDatePicker.getModel().setSelected(true);
			endDatePicker.getModel().setSelected(true);
		}

		if (!startText.equals("") && !endText.equals("")) {
			// If end date is before start date, set to start date
			try {
				Date startDate = dateFormatter.parse(startText);
				if (startDate.compareTo(dateFormatter.parse(endText)) > 0) {
					endDatePicker = setDate(startDatePicker, endDatePicker);
					endDatePicker.getJFormattedTextField().setText(startText);
				}

			} catch (ParseException ex) {
				JOptionPane.showMessageDialog(null, "Error parsing date: " + ex.getMessage(), "Date Parsing Exception",
						JOptionPane.WARNING_MESSAGE);
			}
		}
	}

	public static String getDayOfWeekString(boolean[] dow) {
		String[] dayName = { "Sun", "Mon", "Tue", "Wed", "Th", "Fri", "Sat" };
		String dowString = "";
		for (int i = 0; i < dow.length; i++) {
			if (dow[i]) {
				if (!dowString.equals(""))
					dowString += "/";
				dowString += dayName[i];
			}
		}
		return dowString;
	}

	public static String getWeekOfMonthString(boolean[] wom) {
		String[] weekName = { "1", "2", "3", "4", "5" };
		String womString = "";
		for (int i = 0; i < wom.length; i++) {
			if (wom[i]) {
				if (!womString.equals(""))
					womString += ", ";
				womString += weekName[i];
			}
		}
		return womString;
	}

	private static String getFormattedTime(Calendar cal) {
		// This method assumes that the calendar is already cloned...
		int hour = cal.get(Calendar.HOUR);
		if (hour == 0) {
			cal.set(Calendar.HOUR, 12);
			cal.set(Calendar.AM_PM, Calendar.AM);
		}
		return timeFormat.format(cal.getTime());
	}

	private static JDatePickerImpl setDate(JDatePickerImpl datePickerFrom, JDatePickerImpl datePickerTo) {
		Calendar calFrom = (Calendar) datePickerFrom.getModel().getValue();
		datePickerTo.getModel().setDate(calFrom.get(Calendar.YEAR), calFrom.get(Calendar.MONTH),
				calFrom.get(Calendar.DAY_OF_MONTH));

		return datePickerTo;
	}

	/* <<<<<<<<<< List Utilities >>>>>>>>>> */
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

	/* <<<<<<<<<< Color Utilities >>>>>>>>>> */
	public static int[] getColorSelection() {
		int[] colorSelections = { 0x000000, // Black
				0xE60000, // Red
				0x109010, // Forest green
				0x10E010, // Light green
				0x0000FF, // Blue
				0x20D0F0, // Lighter blue
				0xFF00FF, // Pink
				0xB030B0, // Purple
				0xF28500, // Orange
				0xAA7000, // Brown
				0x909090 // Grey
		};
		return colorSelections;
	}

	/* <<<<<<<<<< Memory Utilities >>>>>>>>>> */
	public static void memoryCheck(String codeLocation) {
		// Get the Java runtime
		Runtime runtime = Runtime.getRuntime();
		runtime.gc();

		long memory = runtime.totalMemory() - runtime.freeMemory();
		System.out.println("Used memory in bytes (" + codeLocation + "): " + memory);

	}
}
