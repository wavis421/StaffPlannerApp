package gui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import model.CalendarDayModel;
import utilities.Utilities;

public class CalendarDayRenderer extends JLabel implements ListCellRenderer<CalendarDayModel> {

	public CalendarDayRenderer() {
		setOpaque(true);
	}

	public Component getListCellRendererComponent(JList<? extends CalendarDayModel> list, CalendarDayModel calendarDay,
			int index, boolean isSelected, boolean cellHasFocus) {

		setFont(CustomFonts.CAL_BOLD_DEFAULT_FONT);
		setForeground(new Color(calendarDay.getTextColor()));
		if (isSelected)
			setBackground(new Color(0xDDDDDD));
		else
			setBackground(Color.WHITE);

		if (calendarDay.getTask() == null) {
			// Floater task
			setText(calendarDay.getFloaterTaskName() + " " + Utilities.formatTime(calendarDay.getFloaterTime()));

		} else if (!calendarDay.getShowCounts()) {
			setText(calendarDay.getTask().getTaskName());

		} else {
			// Assigned task
			if (calendarDay.getLeaderCount() >= calendarDay.getTask().getNumLeadersReqd())
				setText(calendarDay.getTask().getTaskName() + " (" + calendarDay.getPersonCount() + "/"
						+ calendarDay.getTask().getTotalPersonsReqd() + ")");
			else
				setText("*" + calendarDay.getTask().getTaskName() + " (" + calendarDay.getPersonCount() + "/"
						+ calendarDay.getTask().getTotalPersonsReqd() + ")");
			if (calendarDay.getPersonCount() < calendarDay.getTask().getTotalPersonsReqd()) {
				setFont(CustomFonts.CAL_ITALIC_DEFAULT_FONT);
			}
		}
		return this;
	}
}
