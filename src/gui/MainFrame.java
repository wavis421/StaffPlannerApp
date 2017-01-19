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
import java.io.IOException;
import java.sql.Time;
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;

import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import controller.Controller;
import model.AssignedTasksModel;
import model.CalendarDayModel;
import model.PersonByTaskModel;
import model.PersonModel;
import model.ProgramModel;
import model.TaskModel;
import utilities.Utilities;

public class MainFrame extends JFrame {
	/* Private constants */
	private static final int PREF_FRAME_WIDTH = 975;
	private static final int PREF_FRAME_HEIGHT = 700;
	private static final Dimension PREF_FRAME_DIMENSION = new Dimension(PREF_FRAME_WIDTH - 15, PREF_FRAME_HEIGHT - 60);

	/* Private instance variables */
	private static Controller controller;
	private CalendarPanel calPanel;
	private JFileChooser fileChooser;
	private TaskFileFilter fileFilter;

	// Store parameters for selected day box
	private String selectedProgramName;
	private TaskModel selectedTask;
	private Calendar selectedCalendar;

	// Calendar filters
	private final int NO_FILTER = 0;
	private final int PROGRAM_FILTER = 1;
	private final int PERSON_FILTER = 2;
	private final int ROSTER_FILTER = 3;
	private final int LOCATION_FILTER = 4;
	private final int TIME_FILTER = 5;
	private int selectedFilterId = NO_FILTER;
	private JList<String> filteredList = null;

	private JMenuItem filterByProgramMenuItem;
	private JMenuItem filterByPersonMenuItem;

	public MainFrame() {
		super("Staff Planner");
		setLayout(new BorderLayout());
		setBackground(Color.WHITE);

		// Create components
		controller = new Controller();
		calPanel = new CalendarPanel(PREF_FRAME_DIMENSION);
		fileChooser = new JFileChooser();
		fileFilter = new TaskFileFilter();

		setJMenuBar(createMenuBar());
		setCalendarPopupMenu();
		fileChooser.addChoosableFileFilter(fileFilter);
		fileChooser.setFileFilter(fileFilter);

		// Create update month listener
		calPanel.setUpdateCalendarListener(new CalendarUpdateListener() {
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
		JMenu personMenu = new JMenu("Roster");
		JMenu calendarMenu = new JMenu("Calendar");
		JMenu settingsMenu = new JMenu("Settings");
		menuBar.add(fileMenu);
		menuBar.add(programMenu);
		menuBar.add(taskMenu);
		menuBar.add(personMenu);
		menuBar.add(calendarMenu);
		menuBar.add(settingsMenu);

		taskMenu.setEnabled(false);

		// Add file sub-menus
		JMenuItem exportProgramItem = new JMenuItem("Export program...  ");
		JMenuItem exportRosterItem = new JMenuItem("Export roster...  ");
		JMenuItem importProgramItem = new JMenuItem("Import program...  ");
		JMenuItem importRosterItem = new JMenuItem("Import roster...  ");
		JMenuItem exitItem = new JMenuItem("Exit");
		fileMenu.add(importProgramItem);
		fileMenu.add(exportProgramItem);
		fileMenu.add(importRosterItem);
		fileMenu.add(exportRosterItem);
		fileMenu.addSeparator();
		fileMenu.add(exitItem);

		// Add program sub-menus
		JMenuItem programCreateItem = new JMenuItem("New program");
		JMenu programEditMenu = new JMenu("Edit program");
		JMenu programSelectMenu = new JMenu("Select active program");
		programMenu.add(programCreateItem);
		programMenu.add(programEditMenu);
		programMenu.add(programSelectMenu);

		// Add task sub-menus
		JMenuItem taskCreateItem = new JMenuItem("Create task");
		JMenu taskEditMenu = new JMenu("Edit task");
		JMenu taskCloneMenu = new JMenu("Clone task");
		JMenuItem taskViewAllItem = new JMenuItem("View All");
		taskMenu.add(taskCreateItem);
		taskMenu.add(taskEditMenu);
		taskMenu.add(taskCloneMenu);
		taskMenu.add(taskViewAllItem);

		// Add persons sub-menus
		JMenuItem personAddItem = new JMenuItem("Add person");
		JMenu personEditMenu = new JMenu("Edit person");
		JMenuItem personViewAllItem = new JMenuItem("View All");
		personMenu.add(personAddItem);
		personMenu.add(personEditMenu);
		personMenu.add(personViewAllItem);

		// Add calendar sub-menus
		JMenu calendarFilterMenu = new JMenu("Filter");
		calendarMenu.add(calendarFilterMenu);
		JMenuItem filterNoneItem = new JMenuItem("None");
		filterByProgramMenuItem = new JMenuItem("by Program");
		filterByPersonMenuItem = new JMenuItem("by Person");
		JMenuItem filterByIncompleteRosterItem = new JMenuItem("by Incomplete Roster");
		JMenuItem filterByLocationItem = new JMenuItem("by Location");
		JMenuItem filterByTimeItem = new JMenuItem("by Time");
		calendarFilterMenu.add(filterNoneItem);
		calendarFilterMenu.add(filterByProgramMenuItem);
		calendarFilterMenu.add(filterByPersonMenuItem);
		calendarFilterMenu.add(filterByIncompleteRosterItem);
		calendarFilterMenu.add(filterByLocationItem);
		calendarFilterMenu.add(filterByTimeItem);
		if (controller.getNumPrograms() <= 1)
			filterByProgramMenuItem.setEnabled(false);
		if (controller.getNumPersons() <= 1)
			filterByPersonMenuItem.setEnabled(false);

		// Create listeners
		createFileMenuListeners(taskMenu, exportProgramItem, exportRosterItem, importProgramItem, importRosterItem,
				exitItem);
		createProgramMenuListeners(taskMenu, programCreateItem, programEditMenu, programSelectMenu);
		createTaskMenuListeners(taskCreateItem, taskEditMenu, taskCloneMenu, taskViewAllItem);
		createPersonMenuListeners(personAddItem, personEditMenu, personViewAllItem);
		createCalendarMenuListeners(filterNoneItem, filterByProgramMenuItem, filterByPersonMenuItem,
				filterByIncompleteRosterItem, filterByLocationItem, filterByTimeItem);

		return menuBar;
	}

	private void createFileMenuListeners(JMenu taskMenu, JMenuItem exportProgramItem, JMenuItem exportRosterItem,
			JMenuItem importProgramItem, JMenuItem importRosterItem, JMenuItem exitItem) {
		// Set up listeners for FILE menu
		exportProgramItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JList<String> programList = controller.getAllProgramsAsString();
				FilterListDialog ev = new FilterListDialog(MainFrame.this, "Select Program(s) to export", programList);
				JList<String> dialogResponse = ev.getDialogResponse();

				if (dialogResponse != null) {
					if (fileChooser.showSaveDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION) {
						try {
							controller.saveProgramToFile(dialogResponse, fileChooser.getSelectedFile());

						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				}
			}
		});
		importProgramItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (fileChooser.showOpenDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION) {
					try {
						// Load program and clear program filter
						controller.loadProgramFromFile(fileChooser.getSelectedFile());
						if (selectedFilterId == PROGRAM_FILTER)
							setCalendarFilter(NO_FILTER, null);

						updateMonth((Calendar) calPanel.getCurrentCalendar());

						// Select active program and enable program filter menu
						int numPrograms = controller.getNumPrograms();
						if (numPrograms > 1)
							filterByProgramMenuItem.setEnabled(true);
						if (numPrograms == 1) {
							JList<String> programList = controller.getAllProgramsAsString();
							setProgramName(programList.getModel().getElementAt(0));
							taskMenu.setEnabled(true);

						} else if (numPrograms > 1 && selectedProgramName == null) {
							JList<String> programList = controller.getAllProgramsAsString();
							SelectActiveProgramDialog ev = new SelectActiveProgramDialog(MainFrame.this, programList);
							String dialogResponse = ev.getDialogResponse();
							if (dialogResponse != null) {
								setProgramName(dialogResponse);
								taskMenu.setEnabled(true);
							}
						}

					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		});
		exportRosterItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (fileChooser.showSaveDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION) {
					try {
						controller.saveRosterToFile(fileChooser.getSelectedFile());

					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		});
		importRosterItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (fileChooser.showOpenDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION) {
					try {
						// Load roster and clear person filter
						controller.loadRosterFromFile(fileChooser.getSelectedFile());
						if (selectedFilterId == PERSON_FILTER)
							setCalendarFilter(NO_FILTER, null);

						updateMonth((Calendar) calPanel.getCurrentCalendar());

						// Enable person filter menu
						if (controller.getNumPersons() > 1)
							filterByPersonMenuItem.setEnabled(true);

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
	}

	private void createProgramMenuListeners(JMenu taskMenu, JMenuItem programCreateItem, JMenu programEditMenu,
			JMenu programSelectMenu) {
		// Set up listeners for PROGRAM menu
		programCreateItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ProgramDialog programEvent = new ProgramDialog(MainFrame.this, controller.getNumPrograms());
				ProgramEvent dialogResponse = programEvent.getDialogResponse();

				if (dialogResponse != null) {
					ProgramModel program = controller.getProgramByName(dialogResponse.getProgramName());
					if (program != null) {
						// Program already exists!
						JOptionPane.showMessageDialog(MainFrame.this,
								"Program " + dialogResponse.getProgramName() + " already exists.");

					} else {
						// Add program to database
						controller.addProgram(dialogResponse);
						if (controller.getNumPrograms() > 1)
							filterByProgramMenuItem.setEnabled(true);

						if (dialogResponse.isSelectedActive()) {
							setProgramName(dialogResponse.getProgramName());
							taskMenu.setEnabled(true);
						}
					}
				}
			}
		});
		programEditMenu.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				programEditMenu.removeAll();
				JList<String> programList = controller.getAllProgramsAsString();

				for (int i = 0; i < programList.getModel().getSize(); i++) {
					JMenuItem programItem = new JMenuItem(programList.getModel().getElementAt(i).toString());
					programEditMenu.add(programItem);

					programItem.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ev) {
							ProgramModel program = controller.getProgramByName(programItem.getText());
							ProgramDialog programEvent = new ProgramDialog(MainFrame.this, controller.getNumPrograms(),
									program);
							ProgramEvent dialogResponse = programEvent.getDialogResponse();

							if (dialogResponse != null && dialogResponse.getProgramName() != null) {
								if (!programItem.getText().equals(dialogResponse.getProgramName())) {
									// First rename program and updated selected
									// program if needed
									controller.renameProgram(programItem.getText(), dialogResponse.getProgramName());
									if (selectedProgramName.equals(programItem.getText())) {
										setProgramName(dialogResponse.getProgramName());
									}
								}
								controller.updateProgram(dialogResponse.getProgramName(), dialogResponse.getStartDate(),
										dialogResponse.getEndDate());
								
								if (dialogResponse.isSelectedActive())
									setProgramName(dialogResponse.getProgramName());

								updateMonth((Calendar) calPanel.getCurrentCalendar());
							}

							programList.removeAll();
							programEditMenu.removeAll();
						}
					});
				}
			}
		});
		programSelectMenu.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				programSelectMenu.removeAll();
				JList<String> programList = controller.getAllProgramsAsString();

				for (int i = 0; i < programList.getModel().getSize(); i++) {
					JMenuItem programItem = new JMenuItem(programList.getModel().getElementAt(i).toString());
					programSelectMenu.add(programItem);

					programItem.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ev) {
							setProgramName(programItem.getText());
							taskMenu.setEnabled(true);

							programList.removeAll();
							programSelectMenu.removeAll();
						}
					});
				}
			}
		});
	}

	private void createTaskMenuListeners(JMenuItem taskCreateItem, JMenu taskEditMenu, JMenu taskCloneMenu,
			JMenuItem taskViewAllItem) {
		// Set up listeners for TASK menu
		taskCreateItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				createTask();
			}
		});
		taskEditMenu.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				taskEditMenu.removeAll();

				JList<TaskModel> taskList = controller.getAllTasksByProgram(selectedProgramName);
				taskList.setCellRenderer(new TaskRenderer());

				for (int i = 0; i < taskList.getModel().getSize(); i++) {
					TaskModel task = taskList.getModel().getElementAt(i);
					JMenuItem taskItem = new JMenuItem(task.toString());
					taskItem.setForeground(new Color(task.getColor()));
					taskEditMenu.add(taskItem);

					taskItem.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							String origName = taskItem.getText();
							editTask(selectedProgramName, origName);

							taskList.removeAll();
							taskEditMenu.removeAll();
						}
					});
				}
			}
		});
		taskCloneMenu.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				taskCloneMenu.removeAll();

				JList<TaskModel> taskList = controller.getAllTasksByProgram(selectedProgramName);
				taskList.setCellRenderer(new TaskRenderer());

				for (int i = 0; i < taskList.getModel().getSize(); i++) {
					TaskModel task = taskList.getModel().getElementAt(i);
					JMenuItem taskItem = new JMenuItem(task.toString());
					taskItem.setForeground(new Color(task.getColor()));
					taskCloneMenu.add(taskItem);

					taskItem.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							cloneTask(task);
							taskList.removeAll();
							taskCloneMenu.removeAll();
						}
					});
				}
			}
		});
		taskViewAllItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JList<TaskModel> allTasks = controller.getAllTasksByProgram(selectedProgramName);
				TaskTableDialog taskEvent = new TaskTableDialog(MainFrame.this, "All Tasks for " + selectedProgramName,
						allTasks);
				do {
					taskEvent = processViewAllTasksDialog(taskEvent.getDialogResponse());
				} while (taskEvent != null);
			}
		});
	}

	private TaskTableDialog processViewAllTasksDialog(TaskTableEvent event) {
		if (event != null && event.getButtonId() != TaskTableDialog.getCloseButton()) {
			if (event.getButtonId() == TaskTableDialog.getAddTaskButton()) {
				createTask();
				updateMonth((Calendar) calPanel.getCurrentCalendar());
			} else if (event.getButtonId() == TaskTableDialog.getEditRowButton()) {
				editTask(selectedProgramName, event.getTaskName());
				updateMonth((Calendar) calPanel.getCurrentCalendar());
			}

			// Refresh data and re-open Task Table dialog
			JList<TaskModel> allTasks = controller.getAllTasksByProgram(selectedProgramName);
			TaskTableDialog ev = new TaskTableDialog(MainFrame.this, "All Tasks for " + selectedProgramName, allTasks);
			return (ev);
		}
		return null;
	}

	private void createPersonMenuListeners(JMenuItem addPersonItem, JMenu editPersonMenu,
			JMenuItem viewAllPersonsItem) {
		// Set up listeners for PERSONS menu
		addPersonItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				LinkedList<AssignedTasksModel> assignedList = new LinkedList<AssignedTasksModel>();
				JTree taskTree = createTaskTree(assignedList);
				PersonDialog personEvent = new PersonDialog(MainFrame.this, controller.getAllTasks(),
						createAssignedTasksTree(null, taskTree, assignedList), taskTree);
				do {
					personEvent = processAddPersonDialog(personEvent);
				} while (personEvent != null);
			}
		});
		editPersonMenu.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				editPersonMenu.removeAll();
				JList<PersonModel> personList = controller.getAllPersons();

				for (int i = 0; i < personList.getModel().getSize(); i++) {
					JMenuItem personItem = new JMenuItem(personList.getModel().getElementAt(i).toString());
					editPersonMenu.add(personItem);

					personItem.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ev) {
							String origName = personItem.getText();
							editPerson(origName);

							personList.removeAll();
							editPersonMenu.removeAll();
						}
					});
				}
			}
		});
		viewAllPersonsItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (controller.getNumPersons() > 0) {
					LinkedList<PersonByTaskModel> allPersons = controller.getAllPersonsList();
					PersonTableDialog ev = new PersonTableDialog(MainFrame.this, "Complete Roster", false, null,
							allPersons, "", null, null, null);
					do {
						ev = processViewAllPersonsDialog(ev.getDialogResponse());
					} while (ev != null);
				}
			}
		});
	}

	private PersonTableDialog processViewAllPersonsDialog(PersonTableEvent event) {
		if (event != null && event.getButtonId() != PersonTableDialog.getCloseButtonId()) {
			if (event.getButtonId() == PersonTableDialog.getEditRowButtonId()) {
				// Edit person
				editPerson(event.getPersonName());
				updateMonth((Calendar) calPanel.getCurrentCalendar());
			}

			// Refresh data and re-open Person Table dialog
			LinkedList<PersonByTaskModel> allPersons = controller.getAllPersonsList();
			PersonTableDialog ev = new PersonTableDialog(MainFrame.this, "Complete Roster", false, null, allPersons, "",
					null, null, null);
			return ev;
		}
		return null;
	}

	private void createCalendarMenuListeners(JMenuItem filterNoneItem, JMenuItem filterByProgramItem,
			JMenuItem filterByPersonItem, JMenuItem filterByIncompleteRosterItem, JMenuItem filterByLocationItem,
			JMenuItem filterByTimeItem) {
		// Set up listeners for CALENDAR menu
		filterByProgramItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JList<String> programList = controller.getAllProgramsAsString();
				FilterListDialog ev = new FilterListDialog(MainFrame.this, "Filter Calendar by program", programList);
				JList<String> dialogResponse = ev.getDialogResponse();

				// Only one filter can be active
				setCalendarFilter(PROGRAM_FILTER, dialogResponse);
				updateMonth((Calendar) calPanel.getCurrentCalendar());
			}
		});
		filterByPersonItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JList<String> personList = controller.getAllPersonsAsString();
				FilterListDialog ev = new FilterListDialog(MainFrame.this, "Filter Calendar by person", personList);
				JList<String> dialogResponse = ev.getDialogResponse();

				// Only one filter can be active
				setCalendarFilter(PERSON_FILTER, dialogResponse);
				updateMonth((Calendar) calPanel.getCurrentCalendar());
			}
		});
		filterByIncompleteRosterItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Set calendar filter
				setCalendarFilter(ROSTER_FILTER, null);
				updateMonth((Calendar) calPanel.getCurrentCalendar());
			}
		});
		filterByLocationItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JList<String> locationList = controller.getAllLocationsAsString();
				FilterListDialog ev = new FilterListDialog(MainFrame.this, "Filter Calendar by location", locationList);
				JList<String> dialogResponse = ev.getDialogResponse();

				// Only one filter can be active
				setCalendarFilter(LOCATION_FILTER, dialogResponse);
				updateMonth((Calendar) calPanel.getCurrentCalendar());
			}
		});
		filterByTimeItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JList<String> timeList = controller.getAllTimesAsString();
				FilterListDialog ev = new FilterListDialog(MainFrame.this, "Filter Calendar by time", timeList);
				JList<String> dialogResponse = ev.getDialogResponse();

				// Only one filter can be active
				setCalendarFilter(TIME_FILTER, dialogResponse);
				updateMonth((Calendar) calPanel.getCurrentCalendar());
			}
		});
		filterNoneItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setCalendarFilter(NO_FILTER, null);
				updateMonth((Calendar) calPanel.getCurrentCalendar());
			}
		});
	}

	private void createTask() {
		TaskDialog taskEvent = new TaskDialog(MainFrame.this, selectedProgramName);
		processCreateTaskDialog(taskEvent, null);
	}

	private void editTask(String programName, String origTaskName) {
		if (programName == null)
			programName = controller.findProgramByTaskName(origTaskName);

		TaskModel task = controller.getTaskByName(programName, origTaskName);
		TaskDialog taskEvent = new TaskDialog(MainFrame.this, programName, task);
		processCreateTaskDialog(taskEvent, origTaskName);
	}

	private void cloneTask(TaskModel task) {
		TaskEvent ev = new TaskEvent(MainFrame.this, selectedProgramName, null, task.getLocation(),
				task.getNumLeadersReqd(), task.getTotalPersonsReqd(), task.getDayOfWeek(), task.getWeekOfMonth(),
				task.getTime(), task.getColor());

		TaskDialog taskEvent = new TaskDialog(MainFrame.this, ev);
		processCreateTaskDialog(taskEvent, null);
	}

	private void processCreateTaskDialog(TaskDialog taskEvent, String origTaskName) {
		// Loop until user enters valid and unique task name OR cancels
		while (taskEvent.getDialogResponse() != null) {
			TaskEvent dialogResponse = taskEvent.getDialogResponse();
			TaskModel task = controller.getTaskByName(dialogResponse.getProgramName(), dialogResponse.getTaskName());

			if (task != null && (origTaskName == null || !origTaskName.equals(dialogResponse.getTaskName()))) {
				// Task already exists!
				int confirm = JOptionPane.showConfirmDialog(MainFrame.this,
						"Task '" + dialogResponse.getTaskName() + "' already Exists.\n"
								+ "Do you want to switch to editing " + dialogResponse.getTaskName() + "?");

				if (confirm == JOptionPane.OK_OPTION) {
					// Edit existing task with this name
					origTaskName = dialogResponse.getTaskName();
					taskEvent = new TaskDialog(MainFrame.this, dialogResponse.getProgramName(), task);

				} else if (confirm == JOptionPane.NO_OPTION) {
					// Re-try creating this task
					taskEvent = new TaskDialog(MainFrame.this, dialogResponse);

				} else { // Cancel
					break;
				}

			} else if (origTaskName == null) {
				// Add new task and refresh calendar
				controller.addTask(dialogResponse);
				updateMonth((Calendar) calPanel.getCurrentCalendar());
				break;

			} else {
				// Editing existing task, so update task and refresh calendar
				if (!origTaskName.equals(dialogResponse.getTaskName()))
					controller.renameTask(dialogResponse.getProgramName(), origTaskName, dialogResponse.getTaskName());

				controller.updateTask(dialogResponse);
				updateMonth((Calendar) calPanel.getCurrentCalendar());
				break;
			}
		}
	}

	private PersonDialog processAddPersonDialog(PersonDialog personEvent) {
		PersonEvent dialogResponse = personEvent.getDialogResponse();

		if (dialogResponse != null) {
			boolean okToSave = personEvent.getOkToSaveStatus();
			PersonModel person = controller.getPersonByName(dialogResponse.getName());
			if (!okToSave || person != null) {
				if (okToSave)
					// Person already exists
					JOptionPane.showMessageDialog(MainFrame.this,
							"Person " + dialogResponse.getName() + " already exists. Please use a different name.");

				// Do not save; go back and edit person
				LinkedList<AssignedTasksModel> assignedTaskList = dialogResponse.getAssignedTaskChanges();
				JTree taskTree = createTaskTree(assignedTaskList);
				personEvent = new PersonDialog(MainFrame.this, controller.getAllTasks(),
						new PersonModel(dialogResponse.getName(), dialogResponse.getPhone(), dialogResponse.getEmail(),
								dialogResponse.isLeader(), dialogResponse.getNotes(), assignedTaskList,
								dialogResponse.getDatesUnavailable(), null),
						assignedTaskList,
						createAssignedTasksTree(dialogResponse.getLastTaskAdded(), taskTree, assignedTaskList),
						taskTree);
				return personEvent;

			} else {
				// Add person to database
				controller.addPerson(dialogResponse.getName(), dialogResponse.getPhone(), dialogResponse.getEmail(),
						dialogResponse.isLeader(), dialogResponse.getNotes(), dialogResponse.getAssignedTaskChanges(),
						dialogResponse.getExtraDates(), dialogResponse.getDatesUnavailable());
				if (controller.getNumPersons() > 1)
					filterByPersonMenuItem.setEnabled(true);
				updateMonth((Calendar) calPanel.getCurrentCalendar());
			}
		}
		return null;
	}

	private PersonDialog processEditPersonDialog(PersonDialog personEvent, String origName) {
		PersonEvent dialogResponse = personEvent.getDialogResponse();

		if (dialogResponse != null) {
			if (!personEvent.getOkToSaveStatus()) { // is OK to save?
				PersonModel thisPerson = controller.getPersonByName(origName);
				LinkedList<AssignedTasksModel> assignedTasks = (LinkedList<AssignedTasksModel>) thisPerson
						.getAssignedTasks().clone();
				LinkedList<AssignedTasksModel> assignedListMerged = mergeAssignedTaskList(assignedTasks,
						dialogResponse.getAssignedTaskChanges());
				JTree taskTree = createTaskTree(assignedListMerged);
				personEvent = new PersonDialog(MainFrame.this, controller.getAllTasks(),
						new PersonModel(dialogResponse.getName(), dialogResponse.getPhone(), dialogResponse.getEmail(),
								dialogResponse.isLeader(), dialogResponse.getNotes(),
								dialogResponse.getAssignedTaskChanges(), dialogResponse.getDatesUnavailable(),
								thisPerson.getSingleInstanceTasks()),
						dialogResponse.getAssignedTaskChanges(),
						createAssignedTasksTree(dialogResponse.getLastTaskAdded(), taskTree, assignedListMerged),
						taskTree);
				return personEvent;
			} else {
				// Update task list and refresh calendar
				if (!origName.equals(dialogResponse.getName()))
					controller.renamePerson(origName, dialogResponse.getName());
				controller.updatePerson(dialogResponse);
				updateMonth((Calendar) calPanel.getCurrentCalendar());
			}
		}
		return null;
	}

	private void editPerson(String origName) {
		PersonModel person = controller.getPersonByName(origName);
		if (person == null)
			JOptionPane.showMessageDialog(null, "Person does not exist");
		else {
			LinkedList<AssignedTasksModel> assignedList = person.getAssignedTasks();
			JTree taskTree = createTaskTree(assignedList);
			PersonDialog personEvent = new PersonDialog(MainFrame.this, controller.getAllTasks(), person,
					new LinkedList<AssignedTasksModel>(), createAssignedTasksTree(null, taskTree, assignedList),
					taskTree);
			do {
				personEvent = processEditPersonDialog(personEvent, origName);
			} while (personEvent != null);
		}
	}

	private void setCalendarPopupMenu() {
		JPopupMenu popupMenu = new JPopupMenu();
		JMenuItem viewRosterByTaskItem = new JMenuItem("View roster by task");
		JMenuItem viewRosterByTimeItem = new JMenuItem("View roster by time");
		JMenuItem viewRosterByLocationItem = new JMenuItem("View roster by location");
		JMenuItem viewCompleteRosterForToday = new JMenuItem("View complete roster for today");
		popupMenu.add(viewRosterByTaskItem);
		popupMenu.add(viewRosterByTimeItem);
		popupMenu.add(viewRosterByLocationItem);
		popupMenu.add(viewCompleteRosterForToday);

		// Day Box listener
		calPanel.setDayBoxListener(new DayBoxListener() {
			public void dayBoxClicked(Calendar calendar, Point point, CalendarDayModel task) {
				selectedCalendar = calendar;

				// Handle floater with null task
				if (task.getTask() != null) {
					String programName = controller.findProgramByTaskName(task.getTask().getTaskName());
					selectedTask = controller.getTaskByName(programName, task.getTask().getTaskName());
				} else {
					selectedTask = null;
				}
				// Display pop-up menu
				popupMenu.show(calPanel, point.x, point.y);
			}
		});
		// Day Box pop-up sub-menus
		viewRosterByTaskItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (selectedTask != null) {
					// View assigned persons
					LinkedList<PersonByTaskModel> personsToday = controller.getPersonsByDay(selectedCalendar);
					JList<String> personsAvail = controller.getAvailPersonsAsString(selectedCalendar);
					Calendar calendar = (Calendar) selectedCalendar.clone();
					PersonTableDialog ev = new PersonTableDialog(MainFrame.this,
							"Roster for " + selectedTask.getTaskName() + " on "
									+ Utilities.getDisplayDate(selectedCalendar),
							true, selectedTask.getTaskName(), personsToday, "Add sub", calendar, personsAvail, null);
					do {
						ev = processViewRosterByTaskDialog(ev.getDialogResponse());
					} while (ev != null);
				}
			}
		});
		viewRosterByTimeItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Create time list with single time element
				DefaultListModel<Time> timeModel = new DefaultListModel<Time>();
				Time thisTime = Utilities.getTimeFromCalendar(selectedCalendar);
				timeModel.addElement(thisTime);
				JList<Time> timeList = new JList<Time>(timeModel);

				// View assigned persons by time
				LinkedList<PersonByTaskModel> personsByTime = controller.getPersonsByDayByTime(selectedCalendar);
				JList<String> personsAvail = controller.getAvailPersonsAsString(selectedCalendar);
				Calendar calendar = (Calendar) selectedCalendar.clone();

				PersonTableDialog ev = new PersonTableDialog(MainFrame.this,
						"Roster for " + Utilities.getDisplayDate(selectedCalendar) + " at "
								+ Utilities.formatTime(selectedCalendar),
						true, null, personsByTime, "Add floater", calendar, personsAvail, timeList);
				do {
					ev = processViewRosterByTimeDialog(ev.getDialogResponse());
				} while (ev != null);
			}
		});
		viewRosterByLocationItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (selectedTask != null && selectedTask.getLocation() != null
						&& !selectedTask.getLocation().equals("")) {
					// View assigned persons by location
					LinkedList<PersonByTaskModel> personsByLoc = controller.getPersonsByDayByLocation(selectedCalendar,
							selectedTask.getLocation());
					Calendar calendar = (Calendar) selectedCalendar.clone();

					JList<String> personsAvail = controller.getAvailPersonsAsString(selectedCalendar);
					PersonTableDialog ev = new PersonTableDialog(MainFrame.this,
							"Roster at " + selectedTask.getLocation() + " for "
									+ Utilities.getDisplayDate(selectedCalendar),
							true, null, personsByLoc, "", calendar, personsAvail, null);
					do {
						ev = processViewRosterByLocationDialog(ev.getDialogResponse());
					} while (ev != null);
				}
			}
		});
		viewCompleteRosterForToday.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// View all persons
				LinkedList<PersonByTaskModel> personsByTask = controller.getPersonsByDay(selectedCalendar);
				JList<String> personsAvail = controller.getAvailPersonsAsString(selectedCalendar);
				JList<Time> timesToday = controller.getAllTimesByDay(selectedCalendar);
				Calendar calendar = (Calendar) selectedCalendar.clone();

				PersonTableDialog ev = new PersonTableDialog(MainFrame.this,
						"Roster for " + Utilities.getDisplayDate(selectedCalendar), true, null, personsByTask,
						"Add floater", calendar, personsAvail, timesToday);
				do {
					ev = processViewCompleteRosterDialog(ev.getDialogResponse());
				} while (ev != null);
			}
		});
	}

	private PersonTableDialog processViewRosterByTaskDialog(PersonTableEvent event) {
		if (event != null && event.getButtonId() != PersonTableDialog.getCloseButtonId()) {
			if (event.getButtonId() == PersonTableDialog.getAddPersonButtonId()) {
				controller.addSingleInstanceTask(event.getPersonList(), selectedCalendar, selectedTask.getTaskName(),
						selectedTask.getColor());
				updateMonth((Calendar) calPanel.getCurrentCalendar());
			}

			else if (event.getButtonId() == PersonTableDialog.getEditRowButtonId()) {
				editPerson(event.getPersonName());
				updateMonth((Calendar) calPanel.getCurrentCalendar());
			}

			else if (event.getButtonId() == PersonTableDialog.getRemovePersonRowButtonId()) {
				controller.markPersonUnavail(event.getPersonName(), selectedCalendar);
				updateMonth((Calendar) calPanel.getCurrentCalendar());
			}

			// Refresh data and re-open Person Table Dialog
			LinkedList<PersonByTaskModel> personsToday = controller.getPersonsByDay(selectedCalendar);
			JList<String> personsAvail = controller.getAvailPersonsAsString(selectedCalendar);
			Calendar calendar = (Calendar) selectedCalendar.clone();

			PersonTableDialog ev = new PersonTableDialog(MainFrame.this,
					"Roster for " + selectedTask.getTaskName() + " on " + Utilities.getDisplayDate(selectedCalendar),
					true, selectedTask.getTaskName(), personsToday, "Add sub", calendar, personsAvail, null);

			return ev;
		}
		return null;
	}

	private PersonTableDialog processViewRosterByTimeDialog(PersonTableEvent event) {
		if (event != null && event.getButtonId() != PersonTableDialog.getCloseButtonId()) {
			if (event.getButtonId() == PersonTableDialog.getAddPersonButtonId()) {
				// Adding floater
				controller.addSingleInstanceTask(event.getPersonList(), event.getCalendar(), "", event.getColor());
				updateMonth((Calendar) calPanel.getCurrentCalendar());

			} else if (event.getButtonId() == PersonTableDialog.getEditRowButtonId()) {
				editPerson(event.getPersonName());
				updateMonth((Calendar) calPanel.getCurrentCalendar());
			}

			else if (event.getButtonId() == PersonTableDialog.getRemovePersonRowButtonId()) {
				controller.markPersonUnavail(event.getPersonName(), selectedCalendar);
				updateMonth((Calendar) calPanel.getCurrentCalendar());
			}

			// Create time list with single time element
			DefaultListModel<Time> timeModel = new DefaultListModel<Time>();
			Time thisTime = Utilities.getTimeFromCalendar(event.getCalendar());
			timeModel.addElement(thisTime);
			JList<Time> timeList = new JList<Time>(timeModel);

			// Refresh data and re-open Person Table dialog
			LinkedList<PersonByTaskModel> personsByTime = controller.getPersonsByDayByTime(event.getCalendar());
			JList<String> personsAvail = controller.getAvailPersonsAsString(event.getCalendar());
			Calendar calendar = (Calendar) selectedCalendar.clone();

			PersonTableDialog ev = new PersonTableDialog(MainFrame.this,
					"Roster for " + Utilities.getDisplayDate(event.getCalendar()) + " at "
							+ Utilities.formatTime(event.getCalendar()),
					true, null, personsByTime, "Add floater", calendar, personsAvail, timeList);

			return ev;
		}
		return null;
	}

	private PersonTableDialog processViewRosterByLocationDialog(PersonTableEvent event) {
		if (event != null && event.getButtonId() != PersonTableDialog.getCloseButtonId()) {
			if (event.getButtonId() == PersonTableDialog.getEditRowButtonId()) {
				editPerson(event.getPersonName());
				updateMonth((Calendar) calPanel.getCurrentCalendar());
			}

			else if (event.getButtonId() == PersonTableDialog.getRemovePersonRowButtonId()) {
				controller.markPersonUnavail(event.getPersonName(), selectedCalendar);
				updateMonth((Calendar) calPanel.getCurrentCalendar());
			}

			// Remaining events not implemented. Re-open Person Table dialog.
			LinkedList<PersonByTaskModel> personsByLoc = controller.getPersonsByDayByLocation(selectedCalendar,
					selectedTask.getLocation());
			Calendar calendar = (Calendar) selectedCalendar.clone();

			PersonTableDialog ev = new PersonTableDialog(MainFrame.this,
					"Roster at " + selectedTask.getLocation() + " for " + Utilities.getDisplayDate(selectedCalendar),
					true, null, personsByLoc, "", calendar, null, null);

			return ev;
		}
		return null;
	}

	private PersonTableDialog processViewCompleteRosterDialog(PersonTableEvent event) {
		if (event != null && event.getButtonId() != PersonTableDialog.getCloseButtonId()) {
			if (event.getButtonId() == PersonTableDialog.getAddPersonButtonId()) {
				controller.addSingleInstanceTask(event.getPersonList(), event.getCalendar(), "", event.getColor());
				updateMonth((Calendar) calPanel.getCurrentCalendar());

			} else if (event.getButtonId() == PersonTableDialog.getEditRowButtonId()) {
				editPerson(event.getPersonName());
				updateMonth((Calendar) calPanel.getCurrentCalendar());
			}

			else if (event.getButtonId() == PersonTableDialog.getRemovePersonRowButtonId()) {
				controller.markPersonUnavail(event.getPersonName(), selectedCalendar);
				updateMonth((Calendar) calPanel.getCurrentCalendar());
			}

			// Refresh data and re-open Person Table Dialog
			LinkedList<PersonByTaskModel> personsToday = controller.getPersonsByDay(selectedCalendar);
			JList<String> personsAvail = controller.getAvailPersonsAsString(selectedCalendar);
			JList<Time> timesToday = controller.getAllTimesByDay(selectedCalendar);
			Calendar calendar = (Calendar) selectedCalendar.clone();

			PersonTableDialog ev = new PersonTableDialog(MainFrame.this,
					"Roster for " + Utilities.getDisplayDate(selectedCalendar), true, null, personsToday, "Add floater",
					calendar, personsAvail, timesToday);

			return ev;
		}
		return null;
	}

	private void updateMonth(Calendar calendar) {
		Calendar localCalendar = (Calendar) calendar.clone();
		LinkedList<CalendarDayModel> tasks;
		for (int i = 0; i < 31; i++) {
			localCalendar.set(Calendar.DAY_OF_MONTH, i + 1);
			if (selectedFilterId == PROGRAM_FILTER)
				tasks = controller.getTasksByDayByProgram(localCalendar, filteredList);
			else if (selectedFilterId == PERSON_FILTER)
				tasks = controller.getTasksByDayByPerson(localCalendar, filteredList);
			else if (selectedFilterId == ROSTER_FILTER)
				tasks = controller.getTasksByDayByIncompleteRoster(localCalendar);
			else if (selectedFilterId == LOCATION_FILTER)
				tasks = controller.getTasksByDayByLocation(localCalendar, filteredList);
			else if (selectedFilterId == TIME_FILTER)
				tasks = controller.getTasksByDayByTime(localCalendar, filteredList);
			else
				tasks = controller.getAllTasksAndFloatersByDay(localCalendar);

			calPanel.updateTasksByDay(i, tasks);
		}
		calPanel.refresh();
	}

	// Calendar filters
	private void setCalendarFilter(int filterId, JList<String> list) {
		if (filteredList != null)
			filteredList.removeAll();
		filteredList = list;

		// Roster filter has a null list
		if (filteredList == null && filterId != ROSTER_FILTER)
			selectedFilterId = NO_FILTER;
		else
			selectedFilterId = filterId;
	}

	private void setProgramName(String progName) {
		selectedProgramName = progName;
		calPanel.setProgramName(selectedProgramName);
	}

	// TODO: Make the tree handling methods (below) a separate class
	private JTree createTaskTree(LinkedList<AssignedTasksModel> assignedTaskList) {
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Select task to assign  >>>");
		DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);
		LinkedList<ProgramModel> programList = controller.getAllPrograms();

		for (int i = 0; i < programList.size(); i++) {
			ProgramModel p = programList.get(i);
			DefaultMutableTreeNode pNode = new DefaultMutableTreeNode(p);
			rootNode.add(pNode);

			JList<TaskModel> taskList = controller.getAllTasksByProgram(p.getProgramName());

			// For each task in this program, add to program only if not yet
			// assigned
			for (int j = 0; j < taskList.getModel().getSize(); j++) {
				TaskModel task = taskList.getModel().getElementAt(j);
				if (findNodeInAssignedTaskList(assignedTaskList, task.getTaskName()) == -1)
					pNode.add(new DefaultMutableTreeNode(task));
			}
		}
		JTree tree = new JTree(treeModel);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setShowsRootHandles(true);
		tree.setCellRenderer(new TaskTreeRenderer());
		return (tree);
	}

	private JTree createAssignedTasksTree(AssignedTasksModel lastTaskAdded, JTree taskTree,
			LinkedList<AssignedTasksModel> taskList) {
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Assigned tasks");
		DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);
		JTree assignedTree = new JTree(treeModel);
		TreePath path;

		for (AssignedTasksModel item : taskList) {
			// Check to see if this task is currently valid; if not then leave
			// disabled
			TaskModel thisTask = controller.getTaskByName(item.getProgramName(), item.getTaskName());
			if (thisTask == null) {
				System.out.println("Dropping program " + item.getProgramName() + ", task " + item.getTaskName());
				continue;
			}

			// Create the event to be added to the tree
			AssignTaskEvent taskEvent = new AssignTaskEvent(MainFrame.this, item.getProgramName(), thisTask,
					item.getDaysOfWeek(), item.getWeeksOfMonth());

			// Find if the associated Program is already in tree
			path = findNodeInTree((DefaultMutableTreeNode) assignedTree.getModel().getRoot(), item.getProgramName());
			if (path != null) {
				// Program node already exists
				assignedTree.setSelectionPath(path);
				assignedTree.expandRow(assignedTree.getRowForPath(path));
				int childCount = treeModel.getChildCount(assignedTree.getSelectionPath().getLastPathComponent());

				// AssignedTree is already sorted, so add to end
				treeModel.insertNodeInto(new DefaultMutableTreeNode(taskEvent),
						(DefaultMutableTreeNode) assignedTree.getSelectionPath().getLastPathComponent(), childCount);

			} else {
				// Create program node, then add task event
				DefaultMutableTreeNode pNode = new DefaultMutableTreeNode(item.getProgramName());
				assignedTree.setSelectionPath(assignedTree.getPathForRow(0));
				treeModel.insertNodeInto(pNode,
						(DefaultMutableTreeNode) assignedTree.getSelectionPath().getLastPathComponent(), 0);

				pNode.add(new DefaultMutableTreeNode(taskEvent));
			}
		}

		// Collapse all program nodes except last inserted task
		String programName = null;
		if (lastTaskAdded != null)
			programName = lastTaskAdded.getProgramName();
		collapseTree(assignedTree, programName);
		collapseTree(taskTree, programName);

		assignedTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		assignedTree.setShowsRootHandles(true);
		assignedTree.setCellRenderer(new AssignTaskTreeRenderer());
		return (assignedTree);
	}

	private void collapseTree(JTree tree, String s) {
		tree.expandRow(0);
		int row = tree.getRowCount() - 1;

		// Collapse child nodes of root
		while (row > 0) {
			tree.collapseRow(row);
			row--;
		}

		if (s != null) {
			TreePath path = findNodeInTree((DefaultMutableTreeNode) tree.getModel().getRoot(), s);
			tree.expandPath(path);
		}
	}

	private TreePath findNodeInTree(DefaultMutableTreeNode root, String s) {
		Enumeration<DefaultMutableTreeNode> e = root.depthFirstEnumeration();
		while (e.hasMoreElements()) {
			DefaultMutableTreeNode node = e.nextElement();
			if (node.toString().equals(s)) {
				return new TreePath(node.getPath());
			}
		}
		return null;
	}

	private int findNodeInAssignedTaskList(LinkedList<AssignedTasksModel> list, String taskName) {
		for (int idx = 0; idx < list.size(); idx++) {
			AssignedTasksModel t = list.get(idx);
			if (t.getTaskName().equals(taskName)) {
				return idx;
			}
		}
		return -1;
	}

	private LinkedList<AssignedTasksModel> mergeAssignedTaskList(LinkedList<AssignedTasksModel> mergeTaskList,
			LinkedList<AssignedTasksModel> assignedTaskChangeList) {
		// Merge tasks changed list into merge list
		for (AssignedTasksModel task : assignedTaskChangeList) {
			int foundIdx = findNodeInAssignedTaskList(mergeTaskList, task.getTaskName());
			if (foundIdx == -1) { // Not in list
				mergeTaskList.add(task);
			} else {
				mergeTaskList.set(foundIdx, task);
			}
		}
		Collections.sort(mergeTaskList);
		return mergeTaskList;
	}
}
