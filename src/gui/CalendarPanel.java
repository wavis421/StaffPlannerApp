package gui;

import java.awt.BorderLayout;

/**
 * File: CalendarPanel.java
 * -----------------------
 * This class uses the GUI table layout mechanism to create 
 * a calendar panel.
 **/

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import acm.gui.TableLayout;
import acm.util.JTFTools;
import model.TaskModel;

/*
 ** http://cs.stanford.edu/people/eroberts/jtf/tutorial/GraphicalUserInterfaces.html
 */
public class CalendarPanel extends JPanel {
	// Private constants
	private static final Color EMPTY_BACKGROUND = new Color(0xDDDDDD);
	private static final String TITLE_FONT = "Serif-36";
	private static final String PROGRAM_FONT = "Serif-italic-22";
	private static final String LABEL_FONT = "Serif-bold-14";
	private static final String DATE_FONT = "Serif-11";

	// Private instance variables
	private JButton leftButton, rightButton;
	private LinkedList<TaskModel>[] dayBoxTaskList;
	private JLabel programLabel = new JLabel("   ");
	private static TableLayout layout = new TableLayout();
	private static Locale locale = new Locale("en", "US", "");
	private static DateFormatSymbols symbols = new DateFormatSymbols(locale);
	private DayBoxListener dayListener;
	private UpdateCalendarListener updateListener;

	// Private calendar variables
	private Calendar currentCalendar;
	private int firstDayOfWeek;
	private static String[] monthNames = symbols.getMonths();
	private static String[] weekdayNames = symbols.getWeekdays();

	public CalendarPanel() {
		// Create borders
		Border innerBorder = BorderFactory.createEtchedBorder();
		Border outerBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);
		setBorder(BorderFactory.createCompoundBorder(outerBorder, innerBorder));

		// Initialize calendar parameters and display this month's calendar
		currentCalendar = Calendar.getInstance(locale);
		firstDayOfWeek = currentCalendar.getFirstDayOfWeek();
		layout.setColumnCount(7);
		dayBoxTaskList = new LinkedList[31];
		updateCalendarDisplay(currentCalendar);
	}

	public void setProgramName (String name) {
		programLabel.setText(name);;
	}
	
	// Set listener for mouse action on calendar day boxes
	public void setDayBoxListener(DayBoxListener listener) {
		this.dayListener = listener;
	}

	// Set listener for updating calendar month
	public void setUpdateCalendarListener(UpdateCalendarListener listener) {
		this.updateListener = listener;
	}

	// Update the tasks for the indicated calendar day
	public void updateTasksByDay(int dayIdx, LinkedList<TaskModel> tasks) {
		dayBoxTaskList[dayIdx] = tasks;
	}

	// Refresh calendar
	public void refresh() {
		updateCalendarDisplay(currentCalendar);
	}

	// Get current calendar
	public Calendar getCurrentCalendar() {
		return currentCalendar;
	}

	// Update the calendar display for the indicated month
	private void updateCalendarDisplay(Calendar calendar) {
		// Remove components from the calendar table
		removeAll();

		// Set table layout
		setLayout(layout);

		// Add month set buttons and month label
		CreateMonthButtons();
		add(leftButton, "weightx=0.1 weighty=0.1");
		//add(createMonthLabel(calendar), "gridwidth=5 bottom=3");
		add(createMonthLabel(calendar), "gridwidth=5 bottom=0");
		add(rightButton, "weightx=0.1 weighty=0.1");

		programLabel.setHorizontalAlignment(JLabel.CENTER);
		programLabel.setFont(JTFTools.decodeFont(PROGRAM_FONT));
		add(programLabel, "gridwidth=7 weightx=1 bottom=12");
		
		// Add weekday labels
		for (int i = 0; i < 7; i++) {
			add(createWeekdayLabel(i), "weightx=1 width=1 bottom=2");
		}

		// Add null boxes for first week until first day
		int weekday = getFirstWeekdayIndex(calendar);
		for (int i = 0; i < weekday; i++) {
			add(createDayBox(null), "weighty=1");
		}

		// Add day box for each day of the month
		int nDays = getDaysInMonth(calendar);
		for (int day = 1; day <= nDays; day++) {
			add(createDayBox("" + day), "weighty=1");
			weekday = (weekday + 1) % 7;
		}

		// Add null boxes for the rest of the last week
		while (weekday != 0) {
			add(createDayBox(null), "weighty=1");
			weekday = (weekday + 1) % 7;
		}
		validate();
	}

	// Create right/left month update buttons and listeners
	private void CreateMonthButtons() {
		leftButton = new JButton();
		rightButton = new JButton();

		leftButton.setIcon(createIcon("/images/arrow-left-16.png"));
		rightButton.setIcon(createIcon("/images/arrow-right-16.png"));

		// ADD button listeners
		leftButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				currentCalendar.add(Calendar.MONTH, -1);
				if (updateListener != null)
					updateListener.updateCalendar((Calendar) currentCalendar.clone());
			}
		});
		rightButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
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
	private JLabel createWeekdayLabel(int index) {
		int weekday = (firstDayOfWeek + index + 6) % 7 + 1;
		JLabel label = new JLabel(capitalize(weekdayNames[weekday]));
		label.setFont(JTFTools.decodeFont(LABEL_FONT));
		label.setHorizontalAlignment(JLabel.CENTER);
		return label;
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
		int weekdayIndex = (weekday + 7 - firstDayOfWeek) % 7;
		return ((5 * 7 + 1) + weekdayIndex - day) % 7;
	}

	// Create a box for a calendar day containing the specified text
	private Component createDayBox(String text) {
		// Single table panel
		JPanel dayBox = new JPanel(new BorderLayout());
		JScrollPane scrollPane;

		if (text == null) {
			dayBox.setBackground(EMPTY_BACKGROUND);
		} else {
			JLabel label = new JLabel(text);
			int dayIdx = Integer.parseInt(text) - 1;

			// Add day of month
			label.setFont(JTFTools.decodeFont(DATE_FONT));
			dayBox.setBackground(Color.WHITE);
			dayBox.add(label, BorderLayout.BEFORE_FIRST_LINE);
			
			// Create a list of task names assigned to this day
			DefaultListModel<String> taskListModel = new DefaultListModel<String>();
			if (dayBoxTaskList[dayIdx] != null && !dayBoxTaskList[dayIdx].isEmpty()) {
				for (TaskModel t : dayBoxTaskList[dayIdx]) {
					taskListModel.addElement(new String(t.getTaskName()));
				}
			}
			
			JList<String> taskList = new JList<String>(taskListModel);
			taskList.setName(text);
			scrollPane = new JScrollPane(taskList);
			scrollPane.setPreferredSize(
					new Dimension((int) dayBox.getMinimumSize().getWidth(), (int) dayBox.getMinimumSize().getHeight()));
			scrollPane.setBackground(Color.WHITE);
			dayBox.add(scrollPane, BorderLayout.CENTER);

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
								dayListener.dayBoxClicked(calendar, point, dayBoxTaskList[dayIdx].get(listIdx));
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

	// Capitalize the first letter of a word
	private String capitalize(String word) {
		return word.substring(0, 1).toUpperCase() + word.substring(1);
	}

	// Create icon from indicated image file
	private ImageIcon createIcon(String path) {
		URL url = getClass().getResource(path);
		ImageIcon icon = new ImageIcon(url);
		return icon;
	}
}
