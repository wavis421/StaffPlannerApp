package gui;

import java.awt.Color;
import java.awt.Component;
import java.util.Calendar;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import acm.util.JTFTools;
import model.CalendarDayModel;
import utilities.Utilities;

public class CalendarDayRenderer extends JLabel implements ListCellRenderer<CalendarDayModel> {

	private static final String DEFAULT_FONT = "Serif-bold-14";
	private static final String ITALIC_FONT = "Serif-italic-14";

	public CalendarDayRenderer() {
		setOpaque(true);
	}

	public Component getListCellRendererComponent(JList<? extends CalendarDayModel> list, CalendarDayModel calendarDay,
			int index, boolean isSelected, boolean cellHasFocus) {

		setFont(JTFTools.decodeFont(DEFAULT_FONT));
		setForeground(new Color(calendarDay.getTextColor()));
		if (isSelected)
			setBackground(new Color(0xDDDDDD));
		else
			setBackground(Color.WHITE);

		if (calendarDay.getTask() == null) {
			// Floater task
			setText(calendarDay.getFloaterTaskName() + " " + Utilities.formatTime((Calendar)calendarDay.getFloaterTime().clone()));
		} else {
			// Assigned task
			setText(calendarDay.getTask().getTaskName() + " (" + calendarDay.getPersonCount() + "/"
					+ calendarDay.getTask().getTotalPersonsReqd() + ")");
			if (calendarDay.getPersonCount() < calendarDay.getTask().getTotalPersonsReqd()) {
				setFont(JTFTools.decodeFont(ITALIC_FONT));
			}
		}
		return this;
	}
}
