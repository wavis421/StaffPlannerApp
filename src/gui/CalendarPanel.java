package gui;

/**
 * File: CalendarPanel.java
 * -----------------------
 * This class uses the GUI table layout mechanism to create 
 * a calendar panel.
 **/

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import acm.gui.TableLayout;
import acm.gui.VPanel;
import acm.util.JTFTools;

/*
 ** http://cs.stanford.edu/people/eroberts/jtf/tutorial/GraphicalUserInterfaces.html
 */
public class CalendarPanel extends JPanel {
	/* Private constants */
	private static final Color EMPTY_BACKGROUND = new Color(0xDDDDDD);
	private static final String TITLE_FONT = "Serif-36";
	private static final String LABEL_FONT = "Serif-bold-14";
	private static final String DATE_FONT = "Serif-18";

	/* Private instance variables */
	private Calendar currentCalendar;
	private Locale locale;
	private DateFormatSymbols symbols;
	private String[] monthNames;
	private String[] weekdayNames;
	private int firstDayOfWeek;
	private JButton leftButton, rightButton;
	private DayBoxListener dayListener;

	public CalendarPanel() {
		// Create borders
		Border innerBorder = BorderFactory.createTitledBorder("Calendar");
		Border outerBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);
		setBorder(BorderFactory.createCompoundBorder(outerBorder, innerBorder));

		// Initialize calendar parameters
		locale = new Locale("en", "US", "");
		currentCalendar = Calendar.getInstance(locale);
		symbols = new DateFormatSymbols(locale);
		weekdayNames = symbols.getWeekdays();
		monthNames = symbols.getMonths();
		firstDayOfWeek = currentCalendar.getFirstDayOfWeek();
		updateCalendarDisplay(currentCalendar);
	}

	/* Update the calendar display when a new month is selected */
	private void updateCalendarDisplay(Calendar calendar) {
		// Remove components from the calendar table
		removeAll();

		// Set up new table layout
		TableLayout layout = new TableLayout();
		layout.setColumnCount(7);
		setLayout(layout);

		// Add month set buttons and month label
		CreateMonthButtons();
		add(leftButton, "weightx=0.1 weighty=0.1");
		add(createMonthLabel(calendar), "gridwidth=5 bottom=3");
		add(rightButton, "weightx=0.1 weighty=0.1");

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

	/* Create right/left month update buttons */
	private void CreateMonthButtons() {
		leftButton = new JButton();
		rightButton = new JButton();

		leftButton.setIcon(createIcon("/images/arrow-left-16.png"));
		rightButton.setIcon(createIcon("/images/arrow-right-16.png"));

		// ADD button listeners
		leftButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				currentCalendar.add(Calendar.MONTH, -1);
				updateCalendarDisplay(currentCalendar);
			}
		});
		rightButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				currentCalendar.add(Calendar.MONTH, 1);
				updateCalendarDisplay(currentCalendar);
			}
		});
	}

	/* Generate the header label for a particular month */
	private JLabel createMonthLabel(Calendar calendar) {
		int month = calendar.get(Calendar.MONTH);
		int year = calendar.get(Calendar.YEAR);
		String monthName = capitalize(monthNames[month]);
		JLabel label = new JLabel(monthName + " " + year);
		label.setFont(JTFTools.decodeFont(TITLE_FONT));
		label.setHorizontalAlignment(JLabel.CENTER);
		return label;
	}

	/* Create a label for the weekday header at the specified index */
	private JLabel createWeekdayLabel(int index) {
		int weekday = (firstDayOfWeek + index + 6) % 7 + 1;
		JLabel label = new JLabel(capitalize(weekdayNames[weekday]));
		label.setFont(JTFTools.decodeFont(LABEL_FONT));
		label.setHorizontalAlignment(JLabel.CENTER);
		return label;
	}

	/* Compute the number of days in the current month */
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

	/* Compute the index of the first weekday for the current Locale */
	private int getFirstWeekdayIndex(Calendar calendar) {
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int weekday = calendar.get(Calendar.DAY_OF_WEEK);
		int weekdayIndex = (weekday + 7 - firstDayOfWeek) % 7;
		return ((5 * 7 + 1) + weekdayIndex - day) % 7;
	}

	/* Create a box for a calendar day containing the specified text */
	private Component createDayBox(String text) {
		VPanel vbox = new VPanel(); // Single table panel
		if (text == null) {
			vbox.setBackground(EMPTY_BACKGROUND);
		} else {
			JLabel label = new JLabel(text);
			label.setFont(JTFTools.decodeFont(DATE_FONT));
			vbox.add(label, "anchor=NORTHEAST top=2 right=2");
			vbox.setBackground(Color.WHITE);
			vbox.setName(text);

			vbox.addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent e) {
					if (e.getButton() == MouseEvent.BUTTON3) { 
						// Right mouse button event
						if (dayListener != null) 
							dayListener.dayBoxClicked(vbox, (Calendar)currentCalendar.clone(), 
									Integer.parseInt(e.getComponent().getName()), e.getPoint());
					}
				}
			});
		}
		vbox.setOpaque(true);
		vbox.setBorder(new LineBorder(Color.BLACK));
		return vbox;
	}

	/* Capitalize the first letter of a word */
	private String capitalize(String word) {
		return word.substring(0, 1).toUpperCase() + word.substring(1);
	}

	private ImageIcon createIcon(String path) {
		URL url = getClass().getResource(path);
		ImageIcon icon = new ImageIcon(url);
		return icon;
	}

	public String getMonthName (int month)
	{
		return capitalize(monthNames[month]);	
	}
	
	public void setDayBoxListener(DayBoxListener listener) {
		this.dayListener = listener;
	}
}
