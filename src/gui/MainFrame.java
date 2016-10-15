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
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import controller.Controller;
import model.AssignedTasksModel;
import model.PersonModel;
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

	// Calendar filter
	JList<String> programFilter = null;

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
		JMenu personMenu = new JMenu("Staff/Volunteers");
		JMenu calendarMenu = new JMenu("Calendar");
		menuBar.add(fileMenu);
		menuBar.add(programMenu);
		menuBar.add(taskMenu);
		menuBar.add(personMenu);
		menuBar.add(calendarMenu);

		// Add file sub-menus
		JMenuItem exportDataItem = new JMenuItem("Export...");
		JMenuItem importDataItem = new JMenuItem("Import...");
		JMenuItem exitItem = new JMenuItem("Exit");
		fileMenu.add(exportDataItem);
		fileMenu.add(importDataItem);
		fileMenu.addSeparator();
		fileMenu.add(exitItem);

		// Add program sub-menus
		JMenuItem programCreateItem = new JMenuItem("New program");
		JMenuItem programEditItem = new JMenuItem("Edit program");
		JMenuItem programSelectItem = new JMenuItem("Select active program");
		programMenu.add(programCreateItem);
		programMenu.add(programEditItem);
		programMenu.add(programSelectItem);

		// Add task sub-menus
		JMenuItem taskCreateItem = new JMenuItem("Create task");
		JMenuItem taskEditItem = new JMenuItem("Edit task");
		taskMenu.add(taskCreateItem);
		taskMenu.add(taskEditItem);

		// Add persons sub-menus
		JMenuItem personAddItem = new JMenuItem("Add person");
		JMenuItem personEditItem = new JMenuItem("Edit person");
		personMenu.add(personAddItem);
		personMenu.add(personEditItem);

		// Add calendar sub-menus
		JMenu calendarFilterMenu = new JMenu("Filter");
		calendarMenu.add(calendarFilterMenu);
		JMenuItem filterByProgramItem = new JMenuItem("by Program");
		JMenuItem filterByPersonItem = new JMenuItem("by Person");
		calendarFilterMenu.add(filterByProgramItem);
		calendarFilterMenu.add(filterByPersonItem);

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
				selectProgramPopup.show(programMenu, 0, 0);
				System.out.println("menuSelected: programList = " + programList);
				selectProgramPopup.setOpaque(true);

				programList.addMouseListener(new MouseAdapter() {
					public void mousePressed(MouseEvent ev) {
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
					JList<TaskModel> taskList = controller.getAllTasks(selectedProgramName);
					taskList.setCellRenderer(new TaskRenderer());

					editTaskPopup.add(taskList);
					editTaskPopup.setSize(300, 200); // TBD
					editTaskPopup.show(taskMenu, taskMenu.getX(), taskMenu.getY());

					taskList.addMouseListener(new MouseAdapter() {
						public void mousePressed(MouseEvent e) {
							String origName = taskList.getSelectedValue().getTaskName();
							System.out.println("Task Update listener: name = " + origName);

							editTask(origName);

							editTaskPopup.setVisible(false);
							taskList.removeAll();
							editTaskPopup.removeAll();
						}

					});
				}
			}
		});

		// Set up listeners for PERSONS menu
		personAddItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CreateUpdatePersonDialog personEvent = new CreateUpdatePersonDialog(MainFrame.this,
						createAssignedTasksTree(null), createTaskTree());
				PersonEvent dialogResponse = personEvent.getDialogResponse();

				if (dialogResponse != null) {
					if (controller.getPersonByName(dialogResponse.getName()) != null) {
						// Person already exists!
						int confirm = JOptionPane.showConfirmDialog(MainFrame.this, "Person " + dialogResponse.getName()
								+ " already exists. Do you want to edit existing person?");
						/*
						 * if (confirm == JOptionPane.OK_OPTION)
						 * editTask(dialogResponse.getName()); else if (confirm
						 * == JOptionPane.NO_OPTION)
						 * createTaskRetry(dialogResponse);
						 */

					} else {
						// Add person to database
						controller.addPerson(dialogResponse.getName(), dialogResponse.getPhone(),
								dialogResponse.getEmail(), dialogResponse.isStaff(), dialogResponse.getNotes(),
								dialogResponse.getAssignedTasks());
					}
				} else
					System.out.println("Add person is null");
			}
		});
		personEditItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JPopupMenu editPersonPopup = new JPopupMenu();
				JList<PersonModel> personList = controller.getAllPersons();

				editPersonPopup.add(personList);
				editPersonPopup.setSize(300, 200); // TBD
				editPersonPopup.show(personMenu, personMenu.getX(), personMenu.getY());

				personList.addMouseListener(new MouseAdapter() {
					public void mousePressed(MouseEvent e) {
						String origName = personList.getSelectedValue().getName();
						System.out.println("Person Edit listener: name = " + origName);

						editPerson(origName);

						editPersonPopup.setVisible(false);
						personList.removeAll();
						editPersonPopup.removeAll();
					}

				});
			}
		});

		// Set up listeners for CALENDAR menu
		filterByProgramItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JList<String> programList = controller.getAllProgramsAsString();
				FilterCalendarDialog ev = new FilterCalendarDialog(MainFrame.this, "program", programList);
				JList<String> dialogResponse = ev.getDialogResponse();

				programFilter = dialogResponse;
				updateMonth((Calendar) calPanel.getCurrentCalendar().clone());
			}
		});
		// TBD: filter by person, by Staff/volunteer, by tasks that require more
		// people
		filterByPersonItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

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

	private void editPerson(String origName) {
		System.out.println("Person EDIT: name = " + origName);

		PersonModel person = controller.getPersonByName(origName);
		CreateUpdatePersonDialog personEvent = new CreateUpdatePersonDialog(MainFrame.this, person,
				createAssignedTasksTree(person), createTaskTree());
		PersonEvent dialogResponse = personEvent.getDialogResponse();

		if (dialogResponse != null) {
			// Update task list and refresh calendar
			if (!origName.equals(dialogResponse.getName()))
				controller.renamePerson(origName, dialogResponse.getName());
			controller.updatePerson(dialogResponse);
		}
	}

	private void setCalendarPopupMenu() {
		JPopupMenu popupMenu = new JPopupMenu();
		JMenuItem editTaskItem = new JMenuItem("Edit task");
		popupMenu.add(editTaskItem);

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
		editTaskItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Edit task: " + selectedCalendar.get(Calendar.MONTH) + "/"
						+ selectedCalendar.get(Calendar.DAY_OF_MONTH) + "/" + selectedCalendar.get(Calendar.YEAR)
						+ ", Room = " + selectedTask.getLocation() + ", task name = " + selectedTask.getTaskName());

				// Edit task (TBD), then update calendar using filters
				calPanel.updateTasksByDay(selectedCalendar.get(Calendar.DAY_OF_MONTH) - 1,
						controller.getAllTasksByDay(selectedCalendar));
				calPanel.refresh();
			}
		});
	}

	private void updateMonth(Calendar calendar) {
		LinkedList<TaskModel> tasks;
		for (int i = 0; i < 31; i++) {
			calendar.set(Calendar.DAY_OF_MONTH, i + 1);
			if (programFilter == null)
				tasks = controller.getAllTasksByDay(calendar);
			else
				tasks = controller.getTasksByDayByProgram(calendar, programFilter);
			calPanel.updateTasksByDay(i, tasks);
		}
		calPanel.refresh();
	}

	private JTree createTaskTree() {
		DefaultMutableTreeNode top = new DefaultMutableTreeNode("Add new task");
		LinkedList<ProgramModel> programList = controller.getAllPrograms();

		for (int i = 0; i < programList.size(); i++) {
			ProgramModel p = programList.get(i);
			DefaultMutableTreeNode pNode = new DefaultMutableTreeNode(p);
			top.add(pNode);

			JList<TaskModel> taskList = controller.getAllTasks(p.getProgramName());

			for (int j = 0; j < taskList.getModel().getSize(); j++) {
				TaskModel task = taskList.getModel().getElementAt(j);
				pNode.add(new DefaultMutableTreeNode(task));
			}
		}
		JTree tree = new JTree(top);
		tree.setCellRenderer(new TaskTreeRenderer());		
		return (tree);
	}

	private JTree createAssignedTasksTree(PersonModel person) {
		DefaultMutableTreeNode top = new DefaultMutableTreeNode("Assigned tasks");

		if (person != null) {
			for (AssignedTasksModel item : person.getAssignedTasks()) {
				ProgramModel p = controller.getProgramByName(item.getProgramName());
				DefaultMutableTreeNode pNode = new DefaultMutableTreeNode(p);
				top.add(pNode);
				pNode.add(new DefaultMutableTreeNode(
						controller.getTaskByName(item.getProgramName(), item.getTaskName())));
			}
		}
		JTree tree = new JTree(top);
		tree.setCellRenderer(new TaskTreeRenderer());
		return (tree);
	}
}
