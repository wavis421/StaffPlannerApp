package gui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import acm.util.JTFTools;
import model.CalendarDayModel;

public class CalendarDayRenderer extends JLabel implements ListCellRenderer<CalendarDayModel> {

	private static final String DEFAULT_FONT = "Serif-bold-14";
	private static final String ITALIC_FONT = "Serif-italic-14";

	public CalendarDayRenderer() {
		setOpaque(true);
	}

	public Component getListCellRendererComponent(JList<? extends CalendarDayModel> list, CalendarDayModel calendarDay,
			int index, boolean isSelected, boolean cellHasFocus) {
		setText(calendarDay.getTask().getTaskName());
		if (isSelected)
			setBackground(new Color(0xDDDDDD));
		else
			setBackground(Color.WHITE);
		setForeground(new Color(calendarDay.getTask().getColor()));

		if (calendarDay.getPersonCount() < calendarDay.getTask().getTotalPersonsReqd())
			setFont(JTFTools.decodeFont(ITALIC_FONT));
		else
			setFont(JTFTools.decodeFont(DEFAULT_FONT));

		return this;
	}
}
