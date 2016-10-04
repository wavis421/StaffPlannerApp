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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Calendar;
import java.util.LinkedList;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import controller.Controller;
import model.ProgramModel;
import model.TaskModel;

public class MainFrame extends JFrame {
	/* Private constants */
	private static final int PREF_FRAME_WIDTH = 900;
	private static final int PREF_FRAME_HEIGHT = 700;

	/* Private instance variables */
	private static Controller controller;
	private CalendarPanel calPanel;
	private JFileChooser fileChooser;
	private TaskFileFilter fileFilter;

	// Store parameters for selected day box
	private String selectedProgramName;
	private TaskModel selectedTask;
	private Calendar selectedCalendar;

	public MainFrame() {
		super("Staff Planner");
		setLayout(new BorderLayout());
		setBackground(Color.WHITE);

		// Create components
		controller = new Controller();
		calPanel = new CalendarPanel();
		fileChooser = new JFileChooser();
		fileFilter = new TaskFileFilter();

		setJMenuBar(createMenuBar());
		setCalendarPopupMenu();
		fileChooser.addChoosableFileFilter(fileFilter);
		fileChooser.setFileFilter(fileFilter);

		// Set up Calendar Panel and update month listener
		calPanel.setPreferredSize(new Dimension(PREF_FRAME_WIDTH - 15, PREF_FRAME_HEIGHT - 60));
		calPanel.setUpdateCalendarListener(new UpdateCalendarListener() {
			public void updateCalendar(Calendar calendar) {
				updateMonth(calendar);
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
		JMenu programMenu = new JMenu("Program");
		JMenu taskMenu = new JMenu("Task");
		menuBar.add(fileMenu);
		menuBar.add(programMenu);
		menuBar.add(taskMenu);

		// Add file sub-menus
		JMenuItem exportDataItem = new JMenuItem("Export...");
		JMenuItem importDataItem = new JMenuItem("Import...");
		JMenuItem exitItem = new JMenuItem("Exit");
		fileMenu.add(exportDataItem);
		fileMenu.add(importDataItem);
		fileMenu.addSeparator();
		fileMenu.add(exitItem);

		// Add program sub-menus
		JMenuItem programCreateItem = new JMenuItem("Create new program");
		JMenuItem programEditItem = new JMenuItem("Edit program properties");
		JMenuItem programSelectItem = new JMenuItem("Select active program");
		programMenu.add(programCreateItem);
		programMenu.add(programEditItem);
		programMenu.add(programSelectItem);

		// Add task sub-menus
		JMenuItem taskCreateItem = new JMenuItem("Create task");
		JMenuItem taskEditItem = new JMenuItem("Edit task");
		taskMenu.add(taskCreateItem);
		taskMenu.add(taskEditItem);

		// Set up listeners for FILE menu
		exportDataItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (fileChooser.showSaveDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION) {
					try {
						controller.saveToFile(fileChooser.getSelectedFile());

					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		});
		importDataItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (fileChooser.showOpenDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION) {
					try {
						controller.loadFromFile(fileChooser.getSelectedFile());
						updateMonth((Calendar) calPanel.getCurrentCalendar().clone());

					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		});
		exitItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
				System.gc();
			}
		});

		// Set up listeners for PROGRAM menu
		programCreateItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CreateUpdateProgramDialog programEvent = new CreateUpdateProgramDialog(MainFrame.this);
				ProgramEvent dialogResponse = programEvent.getDialogResponse();

				if (dialogResponse != null) {
					if (controller.getProgramByName(dialogResponse.getProgramName()) != null) {
						// Program already exists!
						JOptionPane.showMessageDialog(MainFrame.this,
								"Program " + dialogResponse.getProgramName() + " already exists.");

					} else {
						// Add program to database
						controller.addProgram(dialogResponse);
						if (!dialogResponse.getProgramName().equals(selectedProgramName)) {
							if (JOptionPane.showConfirmDialog(MainFrame.this, "Do you want to set active program to "
									+ dialogResponse.getProgramName() + "?") == JOptionPane.OK_OPTION) {
								selectedProgramName = dialogResponse.getProgramName();
								calPanel.setProgramName(selectedProgramName);
							}
						}
						System.out.println("Program Create listener: name = " + dialogResponse.getProgramName());
					}
				}
			}
		});
		programEditItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

			}
		});
		programSelectItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JPopupMenu selectProgramPopup = new JPopupMenu();
				JList<String> programList = controller.getAllProgramsAsString();

				selectProgramPopup.add(programList);
				selectProgramPopup.setSize(300, 200); // TBD
				selectProgramPopup.show(programMenu, programMenu.getX(), programMenu.getY());

				programList.addMouseListener(new MouseAdapter() {
					public void mousePressed(MouseEvent e) {
						selectedProgramName = programList.getSelectedValue();
						calPanel.setProgramName(selectedProgramName);

						selectProgramPopup.setVisible(false);
						programList.removeAll();
						selectProgramPopup.removeAll();
					}

				});
			}
		});

		// Set up listeners for TASK menu
		taskCreateItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				createTask();
			}
		});
		taskEditItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (selectedProgramName == null) {
					JOptionPane.showMessageDialog(MainFrame.this, "Select Program first!");
				} else {
					JPopupMenu editTaskPopup = new JPopupMenu();
					JList<String> nameList = controller.getAllTasksAsString(selectedProgramName);

					editTaskPopup.add(nameList);
					editTaskPopup.setSize(300, 200); // TBD
					editTaskPopup.show(taskMenu, taskMenu.getX(), taskMenu.getY());

					nameList.addMouseListener(new MouseAdapter() {
						public void mousePressed(MouseEvent e) {
							String origName = nameList.getSelectedValue();
							System.out.println("Task Update listener: name = " + origName);

							editTask(origName);

							editTaskPopup.setVisible(false);
							nameList.removeAll();
							editTaskPopup.removeAll();
						}

					});
				}
			}
		});
		return menuBar;
	}

	private void createTask() {
		System.out.println("Create task: program name = " + selectedProgramName);
		if (selectedProgramName == null)
			JOptionPane.showMessageDialog(MainFrame.this, "Select Program before assigning tasks!");
		else {
			CreateUpdateTaskDialog taskEvent = new CreateUpdateTaskDialog(MainFrame.this, selectedProgramName);
			processCreateTaskDialog(taskEvent);
		}
	}

	private void createTaskRetry(TaskEvent ev) {
		CreateUpdateTaskDialog taskEvent = new CreateUpdateTaskDialog(MainFrame.this, ev);
		processCreateTaskDialog(taskEvent);
	}

	private void processCreateTaskDialog(CreateUpdateTaskDialog taskEvent) {
		TaskEvent dialogResponse = taskEvent.getDialogResponse();

		if (dialogResponse != null) {
			if (controller.getTaskByName(dialogResponse.getProgramName(), dialogResponse.getTaskName()) != null) {
				// Task already exists!
				int confirm = JOptionPane.showConfirmDialog(MainFrame.this,
						"Task " + dialogResponse.getTaskName() + " already exists. Do you want to edit existing task?");
				if (confirm == JOptionPane.OK_OPTION)
					editTask(dialogResponse.getTaskName());
				else if (confirm == JOptionPane.NO_OPTION)
					createTaskRetry(dialogResponse);

			} else {
				// Add task and refresh calendar
				System.out.println("Task Create listener: name = " + dialogResponse.getTaskName());
				controller.addTask(dialogResponse);
				updateMonth((Calendar) calPanel.getCurrentCalendar().clone());
			}
		}
	}

	private void editTask(String origName) {
		System.out.println("Task EDIT: name = " + origName);
		CreateUpdateTaskDialog taskEvent = new CreateUpdateTaskDialog(MainFrame.this, selectedProgramName,
				controller.getTaskByName(selectedProgramName, origName));
		TaskEvent dialogResponse = taskEvent.getDialogResponse();

		if (dialogResponse != null) {
			// Update task list and refresh calendar
			if (!origName.equals(dialogResponse.getTaskName()))
				controller.renameTask(selectedProgramName, origName, dialogResponse.getTaskName());
			controller.updateTask(dialogResponse);
			updateMonth((Calendar) calPanel.getCurrentCalendar().clone());
		}
	}

	private void setCalendarPopupMenu() {
		JPopupMenu popupMenu = new JPopupMenu();
		JMenuItem removeTaskItem = new JMenuItem("Remove task");
		popupMenu.add(removeTaskItem);

		// Day Box listener
		calPanel.setDayBoxListener(new DayBoxListener() {
			public void dayBoxClicked(Calendar calendar, Point point, TaskModel task) {
				selectedCalendar = calendar;
				selectedTask = controller.getTaskByName(selectedProgramName, task.getTaskName());
				System.out.println("day box clicked: day = " + calendar.get(Calendar.DAY_OF_MONTH) + ", room = "
						+ task.getLocation() + ", task name = " + task.getTaskName());

				// Display pop-up menu
				popupMenu.show(calPanel, point.x, point.y);
			}
		});
		// Day Box pop-up sub-menus
		removeTaskItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Remove task: " + selectedCalendar.get(Calendar.MONTH) + "/"
						+ selectedCalendar.get(Calendar.DAY_OF_MONTH) + "/" + selectedCalendar.get(Calendar.YEAR)
						+ ", Room = " + selectedTask.getLocation() + ", task name = " + selectedTask.getTaskName());

				// Remove task from selected day box 
				// (TBD: also remove from database)
				controller.removeTaskFromDay(selectedCalendar, selectedTask.getTaskName());
				calPanel.updateTasksByDay(selectedCalendar.get(Calendar.DAY_OF_MONTH) - 1,
						controller.getTasksByDay(selectedCalendar));
				calPanel.refresh();
			}
		});
	}

	private void updateMonth(Calendar calendar) {
		LinkedList<TaskModel> tasks;
		for (int i = 0; i < 31; i++) {
			calendar.set(Calendar.DAY_OF_MONTH, i + 1);
			tasks = controller.getTasksByDay(calendar);
			calPanel.updateTasksByDay(i, tasks);
		}
		calPanel.refresh();
	}
}
