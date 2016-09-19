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
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import acm.gui.VPanel;
import controller.Controller;
import model.Task;

public class MainFrame extends JFrame {
	/* Private constants */
	private static final int PREF_FRAME_WIDTH = 600;
	private static final int PREF_FRAME_HEIGHT = 550;

	/* Private instance variables */
	private CalendarPanel calPanel;
	private Controller controller;
	private JPopupMenu popupMenu;
	private JMenuItem selectTaskItem;

	/* Other miscellaneous variables */
	private Calendar selectedCalendar;

	public MainFrame() {
		super("Staff Planner");
		setLayout(new BorderLayout());
		setBackground(Color.WHITE);

		// Create components
		calPanel = new CalendarPanel();
		controller = new Controller();
		popupMenu = new JPopupMenu();
		selectTaskItem = new JMenuItem("Select task");

		setJMenuBar(createMenuBar());
		popupMenu.add(selectTaskItem);

		// Set up Calendar Panel and day Listener
		calPanel.setPreferredSize(new Dimension(PREF_FRAME_WIDTH - 15, PREF_FRAME_HEIGHT - 60));
		calPanel.setDayBoxListener(new DayBoxListener() {
			public void dayBoxClicked(VPanel dayPanel, Calendar calendar, int day, Point point) {
				selectedCalendar = calendar;
				System.out.println(calPanel.getMonthName(calendar.get(Calendar.MONTH)) + " " + day + ", "
						+ calendar.get(Calendar.YEAR));

				// Create pop-up menu
				popupMenu.show(calPanel, dayPanel.getX() + point.x, dayPanel.getY() + point.y);
			}
		});
		selectTaskItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Task Menu Item action for " + selectedCalendar.get(Calendar.MONTH) + "/"
						+ selectedCalendar.get(Calendar.DAY_OF_MONTH) + "/" + selectedCalendar.get(Calendar.YEAR)
						+ "!");
				Task task = controller.findTask(selectedCalendar);
				if (task == null)
					System.out.println("Task not found");
				else
					System.out.println("Task found: " + task.getTaskName());
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

	private JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();

		// Set up top level menus and add to menu bar
		JMenu fileMenu = new JMenu("File");
		JMenu taskMenu = new JMenu("Task");
		menuBar.add(fileMenu);
		menuBar.add(taskMenu);

		// Add file sub-menus
		JMenuItem exitItem = new JMenuItem("Exit");
		fileMenu.add(exitItem);

		// Add task sub-menus
		JMenuItem taskCreateItem = new JMenuItem("Create task");
		taskMenu.add(taskCreateItem);

		// Set up listeners
		exitItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Exit Menu Item clicked!");
			}
		});
		taskCreateItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Create Task Menu Item clicked!");
				new CreateTaskDialog(MainFrame.this, controller);
			}
		});

		return menuBar;
	}
}
