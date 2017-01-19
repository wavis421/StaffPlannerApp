package gui;

import java.awt.BorderLayout;

/**
 * File: CalendarPanel.java
 * -----------------------
 * This class uses the GUI table layout mechanism to create 
 * a calendar panel.
 **/

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import acm.gui.TableLayout;
import acm.util.JTFTools;
import model.CalendarDayModel;
import utilities.Utilities;

/*
 ** http://cs.stanford.edu/people/eroberts/jtf/tutorial/GraphicalUserInterfaces.html
 */
public class CalendarPanel extends JPanel {
	// Private constants
	private static final Color EMPTY_BACKGROUND = new Color(0xDDDDDD);
	private static final String TITLE_FONT = "Serif-36";
	private static final String TITLE_BOLD_FONT = "Serif-bold-36";
	private static final String PROGRAM_FONT = "Serif-italic-22";
	private static final String LABEL_FONT = "Serif-bold-14";
	private static final String DATE_FONT = "Serif-11";
	private static final int DAY_PANEL_WIDTH = 300;
	private static final int DAY_PANEL_HEIGHT = 150;

	// Base panel contains CAL panel and Day panel, overlayed
	private JPanel calPanel = new JPanel();
	private JPanel dayPanel = new JPanel();
	private int dayPanelIdx = -1;
	
	// Private instance variables
	private JLabel leftLabel, rightLabel;
	private LinkedList<CalendarDayModel>[] dayBoxTaskList = new LinkedList[31];
	private JLabel programLabel = new JLabel("   ");
	private TableLayout layout = new TableLayout();
	private Locale locale = new Locale("en", "US", "");
	private DateFormatSymbols symbols = new DateFormatSymbols(locale);
	private DayBoxListener dayListener;
	private CalendarUpdateListener updateListener;

	// Private calendar variables
	private Calendar currentCalendar;
	private String[] monthNames = symbols.getMonths();
	private String[] weekdayNames = symbols.getWeekdays();
	private Border innerDayBorder, outerDayBorder;
	private JLabel[] weekdayLabels = new JLabel[7];

	public CalendarPanel(Dimension calPanelSize) {
		// Base panel contains calPanel and dayPanel and has no layout of it's own
		setLayout(null);
		setPreferredSize(calPanelSize);
		calPanel.setBounds(0, 0, (int) calPanelSize.getWidth(), (int) calPanelSize.getHeight());
		
		/***** Set up CALENDAR panel *****/
		// Create borders
		Border innerBorder = BorderFactory.createEtchedBorder();
		Border outerBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);
		calPanel.setBorder(BorderFactory.createCompoundBorder(outerBorder, innerBorder));

		innerDayBorder = BorderFactory.createLineBorder(Color.decode(Integer.toString(0xF00000)), 2);
		outerDayBorder = BorderFactory.createEmptyBorder(1, 1, 1, 1);

		// Set up header label
		programLabel.setHorizontalAlignment(JLabel.CENTER);
		programLabel.setFont(JTFTools.decodeFont(PROGRAM_FONT));

		// Initialize calendar parameters
		currentCalendar = Calendar.getInstance(locale);
		currentCalendar.setFirstDayOfWeek(Calendar.SUNDAY);
		createWeekdayLabels();
		CreateMonthUpdateLabels();

		// Set table layout
		layout.setColumnCount(7);
		calPanel.setLayout(layout);

		// Display this month's calendar
		updateCalendarDisplay(currentCalendar);

		/***** Add Cal and Day panels to base panel *****/
		dayPanel.setVisible(false);
		add(dayPanel, 0);
		add(calPanel, 1);

	}

	public void setProgramName(String name) {
		programLabel.setText(name);
	}

	// Set listener for mouse action on calendar day boxes
	public void setDayBoxListener(DayBoxListener listener) {
		this.dayListener = listener;
	}

	// Set listener for updating calendar month
	public void setUpdateCalendarListener(CalendarUpdateListener listener) {
		this.updateListener = listener;
	}

	// Update the tasks for the indicated calendar day
	public void updateTasksByDay(int dayIdx, LinkedList<CalendarDayModel> tasks) {
		if (dayBoxTaskList[dayIdx] != null)
			dayBoxTaskList[dayIdx].clear();
		dayBoxTaskList[dayIdx] = tasks;
	}

	// Refresh calendar
	public void refresh() {
		// Remove expanded day panel
		dayPanel.removeAll();
		dayPanelIdx = -1;
		
		// Update calendar
		updateCalendarDisplay(currentCalendar);
	}

	// Get current calendar
	public Calendar getCurrentCalendar() {
		return currentCalendar;
	}

	// Update the calendar display for the indicated month
	private void updateCalendarDisplay(Calendar calendar) {
		// Remove components from the calendar table
		calPanel.removeAll();

		// Add month labels
		calPanel.add(leftLabel);
		calPanel.add(createMonthLabel(calendar), "gridwidth=5 bottom=0");
		calPanel.add(rightLabel);

		calPanel.add(programLabel, "gridwidth=7 weightx=1 bottom=12");

		// Add weekday labels
		for (int i = 0; i < 7; i++) {
			calPanel.add(weekdayLabels[i], "weightx=1 width=1 bottom=2");
		}

		// Add null boxes for first week until first day
		int weekday = getFirstWeekdayIndex(calendar);
		for (int i = 0; i < weekday; i++) {
			calPanel.add(createDayBox(null), "weighty=1");
		}

		// Add day box for each day of the month
		int nDays = getDaysInMonth(calendar);
		for (int day = 1; day <= nDays; day++) {
			calPanel.add(createDayBox("" + day), "weighty=1");
			weekday = (weekday + 1) % 7;
		}

		// Add null boxes for the rest of the last week
		while (weekday != 0) {
			calPanel.add(createDayBox(null), "weighty=1");
			weekday = (weekday + 1) % 7;
		}
		validate();
	}

	// Create right/left month update labels and listeners
	private void CreateMonthUpdateLabels() {
		leftLabel = new JLabel("<<", SwingConstants.RIGHT);
		rightLabel = new JLabel(">>");

		leftLabel.setFont(JTFTools.decodeFont(TITLE_BOLD_FONT));
		rightLabel.setFont(JTFTools.decodeFont(TITLE_BOLD_FONT));

		// ADD listeners
		leftLabel.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				currentCalendar.add(Calendar.MONTH, -1);
				if (updateListener != null)
					updateListener.updateCalendar((Calendar) currentCalendar.clone());
			}
		});
		rightLabel.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				currentCalendar.add(Calendar.MONTH, 1);
				if (updateListener != null)
					updateListener.updateCalendar((Calendar) currentCalendar.clone());
			}
		});
	}

	// Generate the header label for a particular month
	private JLabel createMonthLabel(Calendar calendar) {
		int month = calendar.get(Calendar.MONTH);
		int year = calendar.get(Calendar.YEAR);
		String monthName = capitalize(monthNames[month]);

		JLabel label = new JLabel(monthName + " " + year);
		label.setFont(JTFTools.decodeFont(TITLE_FONT));
		label.setHorizontalAlignment(JLabel.CENTER);
		return label;
	}

	// Create a label for the weekday header at the specified index
	private void createWeekdayLabels() {
		for (int i = 0; i < 7; i++) {
			int weekday = (Calendar.SUNDAY + i + 6) % 7 + 1;
			weekdayLabels[i] = new JLabel(capitalize(weekdayNames[weekday]));

			weekdayLabels[i].setFont(JTFTools.decodeFont(LABEL_FONT));
			weekdayLabels[i].setHorizontalAlignment(JLabel.CENTER);
		}
	}

	// Compute the number of days in the current month
	private int getDaysInMonth(Calendar calendar) {
		calendar = (Calendar) calendar.clone();
		int current = calendar.get(Calendar.DAY_OF_MONTH);
		int next = current;
		while (next >= current) {
			current = next;
			calendar.add(Calendar.DAY_OF_MONTH, 1);
			next = calendar.get(Calendar.DAY_OF_MONTH);
		}
		return current;
	}

	// Compute the index of the first weekday for the current Locale
	private int getFirstWeekdayIndex(Calendar calendar) {
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int weekday = calendar.get(Calendar.DAY_OF_WEEK);
		int weekdayIndex = (weekday + 7 - Calendar.SUNDAY) % 7;
		return ((5 * 7 + 1) + weekdayIndex - day) % 7;
	}

	// Create a box for a calendar day containing the specified text
	private JPanel createDayBox(String text) {
		// Single table panel
		JPanel dayBox = new JPanel(new BorderLayout());
		JScrollPane scrollPane;
		boolean highlight;

		if (text == null) {
			dayBox.setBackground(EMPTY_BACKGROUND);
		} else {
			JLabel label = new JLabel(text);
			int dayIdx = Integer.parseInt(text) - 1;
			highlight = false;

			// Add day of month
			label.setFont(JTFTools.decodeFont(DATE_FONT));
			label.setOpaque(true);
			dayBox.setBackground(Color.WHITE);
			dayBox.add(label, BorderLayout.BEFORE_FIRST_LINE);

			// Create a list of tasks assigned to this day
			DefaultListModel<CalendarDayModel> taskListModel = new DefaultListModel<CalendarDayModel>();
			if (dayBoxTaskList[dayIdx] != null && !dayBoxTaskList[dayIdx].isEmpty()) {
				for (int idx = 0; idx < dayBoxTaskList[dayIdx].size(); idx++) {
					CalendarDayModel day = dayBoxTaskList[dayIdx].get(idx);
					taskListModel.addElement(day);
					if (day.getTask() != null && ((day.getPersonCount() < day.getTask().getTotalPersonsReqd())
							|| (day.getLeaderCount() < day.getTask().getNumLeadersReqd())))
						highlight = true;
				}
			}
			if (highlight)
				label.setBorder(BorderFactory.createCompoundBorder(outerDayBorder, innerDayBorder));

			JList<CalendarDayModel> taskList = new JList<>(taskListModel);
			taskList.setCellRenderer(new CalendarDayRenderer());
			taskList.setName(text);
			scrollPane = new JScrollPane(taskList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
					ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

			scrollPane.setPreferredSize(
					new Dimension((int) dayBox.getMinimumSize().getWidth(), (int) dayBox.getMinimumSize().getHeight()));
			scrollPane.setBackground(Color.WHITE);
			dayBox.add(scrollPane, BorderLayout.CENTER);

			label.addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent e) {
					// Select calendar day to enlarge
					Point point = new Point();
					point.setLocation(dayBox.getX(), dayBox.getY());
					configureDayPanel(scrollPane, label, dayIdx, point);
				}
			});

			// Add listener to task list
			taskList.addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent e) {
					if (e.getButton() == MouseEvent.BUTTON3) {
						// Right mouse button event
						if (dayListener != null) {
							// Check whether a list item has been selected
							int listIdx = ((JList<String>) e.getComponent().getComponentAt(e.getPoint()))
									.getSelectedIndex();
							if (listIdx != -1) {
								// Clone the calendar, update with selected day
								Calendar calendar = (Calendar) currentCalendar.clone();
								calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(e.getComponent().getName()));

								// Compute location for pop-up menu
								Point point = new Point();
								point.setLocation(dayBox.getX() + e.getPoint().getX(),
										dayBox.getY() + e.getPoint().getY());

								// Get task model and invoke listener
								int dayIdx = Integer.parseInt(e.getComponent().getName()) - 1;
								CalendarDayModel task = dayBoxTaskList[dayIdx].get(listIdx);
								if (task.getTask() != null)
									Utilities.addTimeToCalendar(calendar, task.getTask().getTime());
								else
									calendar = task.getFloaterTime();

								dayListener.dayBoxClicked(calendar, point, task);
							}
						}
					}
				}
			});
		}
		dayBox.setOpaque(true);
		dayBox.setBorder(new LineBorder(Color.BLACK));
		return dayBox;
	}
	
	private void configureDayPanel(JScrollPane scrollPane, JLabel label, int dayIdx, Point point) {
		dayPanel.setVisible(false);
		dayPanel.removeAll();
		JScrollPane localScrollPane = scrollPane;
		
		if (dayPanelIdx == dayIdx) {
			// Close the expanded day panel
			dayPanelIdx = -1;
		}
		else {			
			// Expand selected day
			dayPanelIdx = dayIdx;
			dayPanel.setLayout(null);
			dayPanel.setBorder(new LineBorder(Color.BLACK));
			dayPanel.setBounds((int) point.getX(), (int) point.getY(), DAY_PANEL_WIDTH, DAY_PANEL_HEIGHT);

			// Configure scroll pane
			localScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			localScrollPane.setPreferredSize(
					new Dimension((int) dayPanel.getMinimumSize().getWidth(), (int) dayPanel.getMinimumSize().getHeight()));
			
			// Add calendar day components
			dayPanel.add(label);
			dayPanel.add(localScrollPane);
			dayPanel.setVisible(true);
		}
		updateCalendarDisplay(currentCalendar);
	}

	// Capitalize the first letter of a word
	private String capitalize(String word) {
		return word.substring(0, 1).toUpperCase() + word.substring(1);
	}
}
