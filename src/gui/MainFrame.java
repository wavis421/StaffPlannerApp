package gui;

/**
 * File: MainFrame.java
 * -----------------------
 * This class creates the GUI for the Staff Planner App.
 **/

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import acm.gui.VPanel;

public class MainFrame extends JFrame {
	/* Private constants */
	private static final int PREF_FRAME_WIDTH = 600;
	private static final int PREF_FRAME_HEIGHT = 500;

	/* Private instance variables */
	private CalendarPanel calPanel;
	private JPopupMenu popupMenu;
	private JMenuItem taskItem;
	
	/* Other misc variables */
	private String selectedDay;

	public MainFrame() {
		super("Staff Planner");
		setLayout(new BorderLayout());
		setBackground(Color.WHITE);

		// Create components 
		calPanel = new CalendarPanel();
		popupMenu = new JPopupMenu();
		taskItem = new JMenuItem("Tasks");
		popupMenu.add(taskItem);

		// Set up Calendar Panel and day Listener 
		calPanel.setPreferredSize(new Dimension(PREF_FRAME_WIDTH - 15, PREF_FRAME_HEIGHT - 50));
		calPanel.setDayBoxListener(new DayBoxListener() {
			public void dayBoxClicked(VPanel dayPanel, Calendar calendar, int day, Point point) {
				selectedDay = calPanel.getMonthName(calendar.get(Calendar.MONTH)) + " " 
						+ day + ", " + calendar.get(Calendar.YEAR);
				
				// Create pop-up menu
				popupMenu.show(calPanel, dayPanel.getX() + point.x, dayPanel.getY() + point.y);
			}
		});
		
		// Set up listener for taskItem 
		taskItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Task Item action for " + selectedDay + "!");
			}
		});

		// ADD all components to frame
		add(calPanel, BorderLayout.PAGE_START);
		pack();

		// Make form visible
		setSize(PREF_FRAME_WIDTH, PREF_FRAME_HEIGHT);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}
}
