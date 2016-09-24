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
import java.util.LinkedList;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import controller.Controller;
import model.TaskModel;

public class MainFrame extends JFrame {
	/* Private constants */
	private static final int PREF_FRAME_WIDTH = 600;
	private static final int PREF_FRAME_HEIGHT = 550;

	/* Private instance variables */
	private CalendarPanel calPanel;
	private static Controller controller;
	private JPopupMenu popupMenu;
	private JMenuItem selectTaskItem;

	/* Other miscellaneous variables */
	private Calendar selectedCalendar;

	public MainFrame() {
		super("Staff Planner");
		setLayout(new BorderLayout());
		setBackground(Color.WHITE);

		// Create components
		controller = new Controller();
		calPanel = new CalendarPanel();
		popupMenu = new JPopupMenu();
		selectTaskItem = new JMenuItem("Select task");

		setJMenuBar(createMenuBar());
		popupMenu.add(selectTaskItem);

		// Set up Calendar Panel, update listener and day Listener
		calPanel.setPreferredSize(new Dimension(PREF_FRAME_WIDTH - 15, PREF_FRAME_HEIGHT - 60));
		calPanel.setUpdateCalendarListener(new UpdateCalendarListener() {
			public void updateCalendar(Calendar calendar) {
				updateMonth(calendar);
			}
		});
		calPanel.setDayBoxListener(new DayBoxListener() {
			public void dayBoxClicked(Calendar calendar, Point point) {
				selectedCalendar = calendar;

				// Display pop-up menu
				popupMenu.show(calPanel, point.x, point.y);
			}
		});
		selectTaskItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Select tasks for: " + selectedCalendar.get(Calendar.MONTH) + "/"
						+ selectedCalendar.get(Calendar.DAY_OF_MONTH) + "/" + selectedCalendar.get(Calendar.YEAR)
						+ ", DOW = " + selectedCalendar.get(Calendar.DAY_OF_WEEK) + ", WOM = "
						+ selectedCalendar.get(Calendar.WEEK_OF_MONTH));

				// TBD
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
				dispose();
				System.gc();
			}
		});
		taskCreateItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CreateTaskDialog taskEvent = new CreateTaskDialog(MainFrame.this);
				TaskEvent dialogResponse = taskEvent.getDialogResponse();
				System.out.println("Task Create listener: name = " + dialogResponse.getTaskName());

				// Update task list and refresh calendar
				if (dialogResponse != null) {
					controller.addTask(dialogResponse);
					updateMonth ((Calendar) calPanel.getCurrentCalendar().clone());
				}
			}
		});

		return menuBar;
	}
	
	private void updateMonth(Calendar calendar) {
		LinkedList<TaskModel> tasks;
		for (int i = 0; i < 31; i++) {
			calendar.set(Calendar.DAY_OF_MONTH, i + 1);
			tasks = controller.findTasksByDay(calendar);
			calPanel.updateTasksByDay(i, tasks);
		}
		calPanel.refresh();
	}
}
