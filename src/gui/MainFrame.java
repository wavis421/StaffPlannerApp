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
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;
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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import controller.Controller;
import model.AssignedTasksModel;
import model.CalendarDayModel;
import model.PersonModel;
import model.ProgramModel;
import model.TaskModel;

public class MainFrame extends JFrame {
	/* Private constants */
	private static final int PREF_FRAME_WIDTH = 975;
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
		JMenu personMenu = new JMenu("Leaders/Volunteers");
		JMenu calendarMenu = new JMenu("Calendar");
		menuBar.add(fileMenu);
		menuBar.add(programMenu);
		menuBar.add(taskMenu);
		menuBar.add(personMenu);
		menuBar.add(calendarMenu);

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
		taskMenu.add(taskCreateItem);
		taskMenu.add(taskEditMenu);
		taskMenu.add(taskCloneMenu);

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
		createTaskMenuListeners(taskCreateItem, taskEditMenu, taskCloneMenu);
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
							selectActiveProgramDialog ev = new selectActiveProgramDialog(MainFrame.this, programList);
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
				CreateUpdateProgramDialog programEvent = new CreateUpdateProgramDialog(MainFrame.this,
						controller.getNumPrograms());
				ProgramEvent dialogResponse = programEvent.getDialogResponse();

				if (dialogResponse != null) {
					if (controller.getProgramByName(dialogResponse.getProgramName()) != null) {
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
							CreateUpdateProgramDialog programEvent = new CreateUpdateProgramDialog(MainFrame.this,
									controller.getNumPrograms(), controller.getProgramByName(programItem.getText()));
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

	private void createTaskMenuListeners(JMenuItem taskCreateItem, JMenu taskEditMenu, JMenu taskCloneMenu) {
		// Set up listeners for TASK menu
		taskCreateItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				createTask();
			}
		});
		taskEditMenu.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				taskEditMenu.removeAll();

				JList<TaskModel> taskList = controller.getAllTasks(selectedProgramName);
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

				JList<TaskModel> taskList = controller.getAllTasks(selectedProgramName);
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
	}

	private void createPersonMenuListeners(JMenuItem addPerson, JMenu editPerson, JMenuItem viewAllPersons) {
		// Set up listeners for PERSONS menu
		addPerson.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				LinkedList<AssignedTasksModel> assignedList = new LinkedList<AssignedTasksModel>();
				JTree taskTree = createTaskTree(assignedList);
				CreateUpdatePersonDialog personEvent = new CreateUpdatePersonDialog(MainFrame.this,
						createAssignedTasksTree(null, taskTree, assignedList), taskTree);
				processAddPersonDialog(personEvent);
			}
		});
		editPerson.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				editPerson.removeAll();
				JList<PersonModel> personList = controller.getAllPersons();

				for (int i = 0; i < personList.getModel().getSize(); i++) {
					JMenuItem personItem = new JMenuItem(personList.getModel().getElementAt(i).toString());
					editPerson.add(personItem);

					personItem.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ev) {
							String origName = personItem.getText();
							editPerson(origName);

							personList.removeAll();
							editPerson.removeAll();
						}
					});
				}
			}
		});
		viewAllPersons.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (controller.getNumPersons() > 0) {
					PersonTableModel tableModel = new PersonTableModel();
					tableModel.setData(controller.getAllPersonsList());
					PersonTableFrame frame = new PersonTableFrame("Leaders/Volunteers", tableModel);

					PersonTableListener tableListener = new PersonTableListener() {
						public void rowDeleted(int row) {
						}

						public void editRow(String personName) {
							editPerson(personName);
							frame.setData(controller.getAllPersonsList());
						}

						public void refresh() {
							frame.setData(controller.getAllPersonsList());
						}
					};

					frame.setTableListener(tableListener);
					frame.setVisible(true);
				}
			}
		});
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
		CreateUpdateTaskDialog taskEvent = new CreateUpdateTaskDialog(MainFrame.this, selectedProgramName);
		processCreateTaskDialog(taskEvent);
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
					editTask(dialogResponse.getProgramName(), dialogResponse.getTaskName());
				else if (confirm == JOptionPane.NO_OPTION)
					createTaskRetry(dialogResponse);

			} else {
				// Add task and refresh calendar
				controller.addTask(dialogResponse);
				updateMonth((Calendar) calPanel.getCurrentCalendar());
			}
		}
	}

	private void editTask(String programName, String origTaskName) {
		if (programName == null)
			programName = controller.findProgramByTaskName(origTaskName);

		CreateUpdateTaskDialog taskEvent = new CreateUpdateTaskDialog(MainFrame.this, programName,
				controller.getTaskByName(programName, origTaskName));
		TaskEvent dialogResponse = taskEvent.getDialogResponse();

		if (dialogResponse != null) {
			// Update task list and refresh calendar
			if (!origTaskName.equals(dialogResponse.getTaskName()))
				controller.renameTask(programName, origTaskName, dialogResponse.getTaskName());
			controller.updateTask(dialogResponse);
			updateMonth((Calendar) calPanel.getCurrentCalendar());
		}
	}

	private void cloneTask(TaskModel task) {
		TaskEvent ev = new TaskEvent(MainFrame.this, selectedProgramName, null, task.getLocation(),
				task.getNumLeadersReqd(), task.getTotalPersonsReqd(), task.getDayOfWeek(), task.getWeekOfMonth(),
				task.getTime(), task.getColor());

		CreateUpdateTaskDialog taskEvent = new CreateUpdateTaskDialog(MainFrame.this, ev);
		processCreateTaskDialog(taskEvent);
	}

	private void processAddPersonDialog(CreateUpdatePersonDialog personEvent) {
		PersonEvent dialogResponse = personEvent.getDialogResponse();
		boolean okToSave = personEvent.getOkToSaveStatus();

		if (dialogResponse != null) {
			if (!okToSave || controller.getPersonByName(dialogResponse.getName()) != null) {
				if (okToSave)
					// Person already exists
					JOptionPane.showMessageDialog(MainFrame.this,
							"Person " + dialogResponse.getName() + " already exists. Please use a different name.");

				// Do not save; go back and edit person
				LinkedList<AssignedTasksModel> assignedTaskList = dialogResponse.getAssignedTaskChanges();
				JTree taskTree = createTaskTree(assignedTaskList);
				personEvent = new CreateUpdatePersonDialog(MainFrame.this,
						new PersonModel(dialogResponse.getName(), dialogResponse.getPhone(), dialogResponse.getEmail(),
								dialogResponse.isLeader(), dialogResponse.getNotes(), assignedTaskList,
								dialogResponse.getDatesUnavailable()),
						assignedTaskList,
						createAssignedTasksTree(dialogResponse.getLastTaskAdded(), taskTree, assignedTaskList),
						taskTree);
				processAddPersonDialog(personEvent);

			} else {
				// Add person to database
				controller.addPerson(dialogResponse.getName(), dialogResponse.getPhone(), dialogResponse.getEmail(),
						dialogResponse.isLeader(), dialogResponse.getNotes(), dialogResponse.getAssignedTaskChanges(),
						dialogResponse.getDatesUnavailable());
				if (controller.getNumPersons() > 1)
					filterByPersonMenuItem.setEnabled(true);
				updateMonth((Calendar) calPanel.getCurrentCalendar());
			}
		}
	}

	private void processEditPersonDialog(CreateUpdatePersonDialog personEvent, String origName) {
		PersonEvent dialogResponse = personEvent.getDialogResponse();
		boolean isOkToSave = personEvent.getOkToSaveStatus();

		if (dialogResponse != null) {
			if (!isOkToSave) {
				LinkedList<AssignedTasksModel> assignedList = mergeAssignedTaskList(origName,
						dialogResponse.getAssignedTaskChanges());
				JTree taskTree = createTaskTree(assignedList);
				personEvent = new CreateUpdatePersonDialog(MainFrame.this,
						new PersonModel(dialogResponse.getName(), dialogResponse.getPhone(), dialogResponse.getEmail(),
								dialogResponse.isLeader(), dialogResponse.getNotes(),
								dialogResponse.getAssignedTaskChanges(), dialogResponse.getDatesUnavailable()),
						dialogResponse.getAssignedTaskChanges(),
						createAssignedTasksTree(dialogResponse.getLastTaskAdded(), taskTree, assignedList), taskTree);
				processEditPersonDialog(personEvent, origName);
			} else {
				// Update task list and refresh calendar
				if (!origName.equals(dialogResponse.getName()))
					controller.renamePerson(origName, dialogResponse.getName());
				controller.updatePerson(dialogResponse);
				updateMonth((Calendar) calPanel.getCurrentCalendar());
			}
		}
	}

	private void editPerson(String origName) {
		PersonModel person = controller.getPersonByName(origName);
		if (person == null)
			JOptionPane.showMessageDialog(null, "Person does not exist");
		else {
			LinkedList<AssignedTasksModel> assignedList = person.getAssignedTasks();
			JTree taskTree = createTaskTree(assignedList);
			CreateUpdatePersonDialog personEvent = new CreateUpdatePersonDialog(MainFrame.this, person,
					new LinkedList<AssignedTasksModel>(), createAssignedTasksTree(null, taskTree, assignedList),
					taskTree);
			processEditPersonDialog(personEvent, origName);
		}
	}

	private void setCalendarPopupMenu() {
		JPopupMenu popupMenu = new JPopupMenu();
		JMenuItem editTaskItem = new JMenuItem("Edit task");
		JMenuItem viewRosterByTaskItem = new JMenuItem("View roster by task");
		JMenuItem viewCompleteRosterForToday = new JMenuItem("View complete roster for today");
		popupMenu.add(editTaskItem);
		popupMenu.add(viewRosterByTaskItem);
		popupMenu.add(viewCompleteRosterForToday);

		// Day Box listener
		calPanel.setDayBoxListener(new DayBoxListener() {
			public void dayBoxClicked(Calendar calendar, Point point, CalendarDayModel task) {
				selectedCalendar = calendar;
				String programName = controller.findProgramByTaskName(task.getTask().getTaskName());
				selectedTask = controller.getTaskByName(programName, task.getTask().getTaskName());
				System.out.println("day box clicked: day = " + calendar.get(Calendar.DAY_OF_MONTH) + ", room = "
						+ task.getTask().getLocation() + ", task name = " + task.getTask().getTaskName());

				// Display pop-up menu
				popupMenu.show(calPanel, point.x, point.y);
			}
		});
		// Day Box pop-up sub-menus
		editTaskItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Edit task, then update calendar using filters
				LinkedList<CalendarDayModel> tasksByDay = controller.getAllTasksByDay(selectedCalendar);
				editTask(null, selectedTask.getTaskName());
				updateMonth(selectedCalendar);
			}
		});
		viewRosterByTaskItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// View assigned persons
				LinkedList<PersonModel> personByTask = controller.getPersonsByDayByTask(selectedCalendar,
						selectedTask.getTaskName());

				PersonTableModel tableModel = new PersonTableModel();
				tableModel.setData(personByTask);
				PersonTableFrame frame = new PersonTableFrame("Leaders/Volunteers for " + selectedTask.getTaskName()
						+ " on " + getDisplayDate(selectedCalendar), tableModel);

				PersonTableListener tableListener = new PersonTableListener() {
					public void rowDeleted(int row) {
					}

					public void editRow(String personName) {
						editPerson(personName);
						frame.setData(controller.getPersonsByDayByTask(selectedCalendar, selectedTask.getTaskName()));
					}

					public void refresh() {
						frame.setData(controller.getPersonsByDayByTask(selectedCalendar, selectedTask.getTaskName()));
					}
				};

				frame.setTableListener(tableListener);
				frame.setVisible(true);
			}
		});
		viewCompleteRosterForToday.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// View all persons
				LinkedList<PersonModel> personList = controller.getPersonsByDay(selectedCalendar);

				PersonTableModel tableModel = new PersonTableModel();
				tableModel.setData(personList);
				PersonTableFrame frame = new PersonTableFrame(
						"Leaders/Volunteers for " + getDisplayDate(selectedCalendar), tableModel);

				PersonTableListener tableListener = new PersonTableListener() {
					public void rowDeleted(int row) {
					}

					public void editRow(String personName) {
						editPerson(personName);
						frame.setData(controller.getPersonsByDay(selectedCalendar));
					}

					public void refresh() {
						frame.setData(controller.getPersonsByDay(selectedCalendar));
					}
				};

				frame.setTableListener(tableListener);
				frame.setVisible(true);
			}
		});
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
				tasks = controller.getAllTasksByDay(localCalendar);

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

	private String getDisplayDate(Calendar calendar) {
		return ((calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.DAY_OF_MONTH) + "/"
				+ calendar.get(Calendar.YEAR));
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

			JList<TaskModel> taskList = controller.getAllTasks(p.getProgramName());

			// For each task in this program, add to program only if not yet
			// assigned
			for (int j = 0; j < taskList.getModel().getSize(); j++) {
				TaskModel task = taskList.getModel().getElementAt(j);
				if (!findNodeInAssignedTaskList(assignedTaskList, task.getTaskName()))
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
			if (controller.getTaskByName(item.getProgramName(), item.getTaskName()) == null) {
				System.out.println("Dropping program " + item.getProgramName() + ", task " + item.getTaskName());
				continue;
			}

			// Create the event to be added to the tree
			AssignTaskEvent taskEvent = new AssignTaskEvent(MainFrame.this, item.getProgramName(),
					controller.getTaskByName(item.getProgramName(), item.getTaskName()), item.getDaysOfWeek(),
					item.getWeeksOfMonth());

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

	private boolean findNodeInAssignedTaskList(LinkedList<AssignedTasksModel> list, String taskName) {
		for (AssignedTasksModel t : list) {
			if (t.getTaskName().equals(taskName)) {
				return true;
			}
		}
		return false;
	}

	private LinkedList<AssignedTasksModel> mergeAssignedTaskList(String personName,
			LinkedList<AssignedTasksModel> assignedTaskChangeList) {
		PersonModel person = controller.getPersonByName(personName);
		LinkedList<AssignedTasksModel> masterTaskList = person.getAssignedTasks();

		// Merge master task list into task changed list
		for (AssignedTasksModel task : masterTaskList) {
			if (!findNodeInAssignedTaskList(assignedTaskChangeList, task.getTaskName())) {
				assignedTaskChangeList.add(task);
			}
		}
		Collections.sort(assignedTaskChangeList);
		return assignedTaskChangeList;
	}
}
