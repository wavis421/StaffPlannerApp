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
import java.util.ArrayList;
import java.util.Calendar;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import controller.Controller;
import model.AssignedTasksModel;
import model.CalendarDayModel;
import model.PersonByTaskModel;
import model.PersonModel;
import model.ProgramModel;
import model.TaskModel;
import model.TaskTimeModel;
import model.TimeModel;
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
	private ArrayList<String> filteredList = null;

	private final String[] filterNames = { "", "Program", "Persons", "Incomplete Roster", "Location", "Time" };

	private JMenu taskMenu;
	private JMenuItem filterByProgramMenuItem;
	private JMenuItem filterByPersonMenuItem;

	public MainFrame() {
		super("Program Planner");
		setLayout(new BorderLayout());
		setBackground(Color.WHITE);

		ImageIcon img = new ImageIcon("PPicon24.png");
		setIconImage(img.getImage());

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

		// Initialize program and roster
		initializeProgram();
		initializeRoster();

		updateMonth((Calendar) calPanel.getCurrentCalendar());
	}

	private JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();

		// Set up top level menus and add to menu bar
		JMenu fileMenu = new JMenu("File");
		JMenu programMenu = new JMenu("Program");
		taskMenu = new JMenu("Task");
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
		JMenuItem exitItem = new JMenuItem("Exit ");
		fileMenu.add(importProgramItem);
		fileMenu.add(exportProgramItem);
		fileMenu.add(importRosterItem);
		fileMenu.add(exportRosterItem);
		fileMenu.addSeparator();
		fileMenu.add(exitItem);

		// Add program sub-menus
		JMenuItem programCreateItem = new JMenuItem("Create program ");
		JMenu programEditMenu = new JMenu("Edit program ");
		JMenu programDeleteMenu = new JMenu("Delete program ");
		JMenu programSelectMenu = new JMenu("Select active program ");
		programMenu.add(programCreateItem);
		programMenu.add(programEditMenu);
		programMenu.add(programDeleteMenu);
		programMenu.add(programSelectMenu);

		// Add task sub-menus
		JMenuItem taskCreateItem = new JMenuItem("Create task ");
		JMenu taskEditMenu = new JMenu("Edit task ");
		JMenu taskDeleteMenu = new JMenu("Delete task ");
		JMenu taskCloneMenu = new JMenu("Clone task ");
		JMenu taskRosterMenu = new JMenu("Task roster ");
		JMenuItem taskViewAllItem = new JMenuItem("View all tasks ");
		taskMenu.add(taskCreateItem);
		taskMenu.add(taskEditMenu);
		taskMenu.add(taskDeleteMenu);
		taskMenu.add(taskCloneMenu);
		taskMenu.add(taskRosterMenu);
		taskMenu.add(taskViewAllItem);

		// Add persons sub-menus
		JMenuItem personViewNotesItem = new JMenuItem("View roster notes ");
		JMenuItem personAddItem = new JMenuItem("Add person ");
		JMenu personEditMenu = new JMenu("Edit person ");
		JMenu personRemoveMenu = new JMenu("Remove person ");
		JMenuItem personViewAllItem = new JMenuItem("View complete roster ");
		JMenuItem personViewActiveItem = new JMenuItem("View active roster ");
		personMenu.add(personViewNotesItem);
		personMenu.add(personAddItem);
		personMenu.add(personEditMenu);
		personMenu.add(personRemoveMenu);
		personMenu.add(personViewAllItem);
		personMenu.add(personViewActiveItem);

		// Add calendar sub-menus
		JMenu calendarFilterMenu = new JMenu("Filter ");
		calendarMenu.add(calendarFilterMenu);
		JMenuItem filterNoneItem = new JMenuItem("None ");
		filterByProgramMenuItem = new JMenuItem("by Program ");
		filterByPersonMenuItem = new JMenuItem("by Person ");
		JMenuItem filterByIncompleteRosterItem = new JMenuItem("by Incomplete Roster ");
		JMenuItem filterByLocationItem = new JMenuItem("by Location ");
		JMenuItem filterByTimeItem = new JMenuItem("by Time ");
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
		createProgramMenuListeners(taskMenu, programCreateItem, programEditMenu, programDeleteMenu, programSelectMenu);
		createTaskMenuListeners(taskCreateItem, taskEditMenu, taskDeleteMenu, taskCloneMenu, taskRosterMenu,
				taskViewAllItem);
		createPersonMenuListeners(personViewNotesItem, personAddItem, personEditMenu, personRemoveMenu,
				personViewAllItem, personViewActiveItem);
		createCalendarMenuListeners(filterNoneItem, filterByProgramMenuItem, filterByPersonMenuItem,
				filterByIncompleteRosterItem, filterByLocationItem, filterByTimeItem);

		return menuBar;
	}

	private void createFileMenuListeners(JMenu taskMenu, JMenuItem exportProgramItem, JMenuItem exportRosterItem,
			JMenuItem importProgramItem, JMenuItem importRosterItem, JMenuItem exitItem) {
		// Set up listeners for FILE menu
		exportProgramItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ArrayList<String> programList = controller.getAllProgramsAsString();
				FilterListDialog ev = new FilterListDialog(MainFrame.this, "Select Program(s) to export", programList);
				ArrayList<String> dialogResponse = ev.getDialogResponse();

				if (dialogResponse != null) {
					if (fileChooser.showSaveDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION) {
						try {
							controller.saveProgramToFile(dialogResponse, fileChooser.getSelectedFile());

						} catch (IOException e1) {
							System.out.println("Export program error: " + e1.getMessage());
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
						initializeProgram();

					} catch (IOException e1) {
						System.out.println("Import Program error: " + e1.getMessage());
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
						System.out.println("Export Roster error: " + e1.getMessage());
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

						// Process import roster
						initializeRoster();

					} catch (IOException e1) {
						System.out.println("Import Roster error: " + e1.getMessage());
					}
				}
			}
		});
		exitItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				controller.disconnectDatabase();
				dispose();
				System.gc();
			}
		});
	}

	private void initializeProgram() {
		// Select active program and enable program filter menu
		int numPrograms = controller.getNumPrograms();
		if (numPrograms > 1)
			filterByProgramMenuItem.setEnabled(true);
		if (numPrograms == 1) {
			ArrayList<String> programList = controller.getAllProgramsAsString();
			setProgramName(programList.get(0));
			taskMenu.setEnabled(true);

		} else if (numPrograms > 1 && selectedProgramName == null) {
			ArrayList<String> programList = controller.getAllProgramsAsString();
			SelectActiveProgramDialog ev = new SelectActiveProgramDialog(MainFrame.this, programList);
			String dialogResponse = ev.getDialogResponse();
			if (dialogResponse != null) {
				setProgramName(dialogResponse);
				taskMenu.setEnabled(true);
			}
		}
	}

	private void initializeRoster() {
		// Enable person filter menu
		if (controller.getNumPersons() > 1)
			filterByPersonMenuItem.setEnabled(true);
	}

	private void createProgramMenuListeners(JMenu taskMenu, JMenuItem programCreateItem, JMenu programEditMenu,
			JMenu programDeleteMenu, JMenu programSelectMenu) {
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
				ArrayList<String> programList = controller.getAllProgramsAsString();

				for (int i = 0; i < programList.size(); i++) {
					JMenuItem programItem = new JMenuItem(programList.get(i).toString());
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

							programList.clear();
							programEditMenu.removeAll();
						}
					});
				}
			}
		});
		programDeleteMenu.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				programDeleteMenu.removeAll();
				ArrayList<String> programList = controller.getAllProgramsAsString();

				for (int i = 0; i < programList.size(); i++) {
					JMenuItem programItem = new JMenuItem(programList.get(i).toString());
					programDeleteMenu.add(programItem);

					programItem.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ev) {
							// Delete program
							String programName = programItem.getText();
							if (JOptionPane.showConfirmDialog(MainFrame.this,
									"Are you sure you want to delete " + programName + " program"
											+ "\n and all corresponding tasks? ",
									"Program Delete", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
								controller.deleteProgram(programName);
								updateMonth((Calendar) calPanel.getCurrentCalendar());
							}

							// Clean up lists
							programList.clear();
							programDeleteMenu.removeAll();
						}
					});
				}
			}
		});
		programSelectMenu.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				programSelectMenu.removeAll();
				ArrayList<String> programList = controller.getAllProgramsAsString();

				for (int i = 0; i < programList.size(); i++) {
					JMenuItem programItem = new JMenuItem(programList.get(i).toString());
					programSelectMenu.add(programItem);

					programItem.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ev) {
							setProgramName(programItem.getText());
							taskMenu.setEnabled(true);

							programList.clear();
							programSelectMenu.removeAll();
						}
					});
				}
			}
		});
	}

	private void createTaskMenuListeners(JMenuItem taskCreateItem, JMenu taskEditMenu, JMenu taskDeleteMenu,
			JMenu taskCloneMenu, JMenuItem taskRosterMenu, JMenuItem taskViewAllItem) {
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
		taskDeleteMenu.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				taskDeleteMenu.removeAll();

				JList<TaskModel> taskList = controller.getAllTasksByProgram(selectedProgramName);
				taskList.setCellRenderer(new TaskRenderer());

				for (int i = 0; i < taskList.getModel().getSize(); i++) {
					TaskModel task = taskList.getModel().getElementAt(i);
					JMenuItem taskItem = new JMenuItem(task.toString());
					taskItem.setForeground(new Color(task.getColor()));
					taskDeleteMenu.add(taskItem);

					taskItem.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							// Delete task
							String taskName = taskItem.getText();
							if (deleteTask(taskName))
								updateMonth((Calendar) calPanel.getCurrentCalendar());

							// Clean up lists
							taskList.removeAll();
							taskDeleteMenu.removeAll();
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
		taskRosterMenu.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				taskRosterMenu.removeAll();

				JList<TaskModel> taskList = controller.getAllTasksByProgram(selectedProgramName);
				taskList.setCellRenderer(new TaskRenderer());

				for (int i = 0; i < taskList.getModel().getSize(); i++) {
					TaskModel task = taskList.getModel().getElementAt(i);
					JMenuItem taskItem = new JMenuItem(task.toString());
					taskItem.setForeground(new Color(task.getColor()));
					taskRosterMenu.add(taskItem);

					taskItem.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							ArrayList<PersonByTaskModel> personsByTask = controller.getPersonsByTask(task);
							ArrayList<String> personsAvail = controller.getAllPersonsAsString();
							PersonTableDialog ev = new PersonTableDialog(MainFrame.this,
									"Complete Roster for " + task.getTaskName(), PersonTableModel.getExpansionByTask(),
									task.getTaskName(), personsByTask, "Add person", null, personsAvail, null, null);

							do {
								ev = processViewCompleteRosterByTaskDialog(ev.getDialogResponse(), task);
							} while (ev != null);

							taskList.removeAll();
							taskRosterMenu.removeAll();
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

			} else if (event.getButtonId() == TaskTableDialog.getDeleteRowButton()) {
				if (deleteTask(event.getTaskName()))
					updateMonth((Calendar) calPanel.getCurrentCalendar());
			}

			// Refresh data and re-open Task Table dialog
			JList<TaskModel> allTasks = controller.getAllTasksByProgram(selectedProgramName);
			TaskTableDialog ev = new TaskTableDialog(MainFrame.this, "All Tasks for " + selectedProgramName, allTasks);
			return (ev);
		}
		return null;
	}

	private void createPersonMenuListeners(JMenuItem viewNotesItem, JMenuItem addPersonItem, JMenu editPersonMenu,
			JMenu removePersonMenu, JMenuItem viewAllPersonsItem, JMenuItem viewActivePersonsItem) {
		// Set up listeners for PERSONS menu
		viewNotesItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ArrayList<PersonByTaskModel> allPersons = controller.getAllPersonsWithNotes();
				PersonTableDialog ev = new PersonTableDialog(MainFrame.this, "Roster Notes",
						PersonTableModel.getExpansionWithNotes(), null, allPersons, "", null, null, null, null);
				if (ev != null && ev.getDialogNotesResponse() != null) {
					controller.updatePersonNotes(ev.getDialogNotesResponse().getPersonNotesList());
				}
			}
		});
		addPersonItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ArrayList<AssignedTasksModel> assignedList = new ArrayList<AssignedTasksModel>();
				ArrayList<ProgramModel> progList = controller.getAllPrograms();
				ArrayList<JList<TaskModel>> taskListByProgram = new ArrayList<JList<TaskModel>>();
				ArrayList<ArrayList<AssignedTasksModel>> assignedTaskListByProgram = new ArrayList<ArrayList<AssignedTasksModel>>();
				getAssignedTaskLists(assignedList, progList, taskListByProgram, assignedTaskListByProgram);
				PersonDialog personEvent = new PersonDialog(MainFrame.this, selectedProgramName,
						controller.getAllTasks(), progList, taskListByProgram, assignedTaskListByProgram);

				do {
					personEvent = processAddPersonDialog(personEvent);
				} while (personEvent != null);
			}
		});
		editPersonMenu.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				editPersonMenu.removeAll();
				ArrayList<String> personList = controller.getAllPersonsAsString();

				for (int i = 0; i < personList.size(); i++) {
					JMenuItem personItem = new JMenuItem(personList.get(i));
					editPersonMenu.add(personItem);

					personItem.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ev) {
							String origName = personItem.getText();
							editPerson(origName);

							personList.clear();
							editPersonMenu.removeAll();
							updateMonth((Calendar) calPanel.getCurrentCalendar());
						}
					});
				}
			}
		});
		removePersonMenu.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				removePersonMenu.removeAll();
				ArrayList<String> personList = controller.getAllPersonsAsString();

				for (int i = 0; i < personList.size(); i++) {
					JMenuItem personItem = new JMenuItem(personList.get(i));
					removePersonMenu.add(personItem);

					personItem.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ev) {
							// Remove person from roster
							boolean changed = removePerson(personItem.getText());

							// Clean up lists
							personList.clear();
							removePersonMenu.removeAll();

							if (changed)
								updateMonth((Calendar) calPanel.getCurrentCalendar());
						}
					});
				}
			}
		});
		viewAllPersonsItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (controller.getNumPersons() > 0) {
					ArrayList<PersonByTaskModel> allPersons = controller.getAllPersons();
					PersonTableDialog ev = new PersonTableDialog(MainFrame.this, "Complete Roster",
							PersonTableModel.getMinimumExpansion(), null, allPersons, "Add person", null, null, null,
							null);

					do {
						ev = processViewAllPersonsDialog(ev.getDialogResponse());
					} while (ev != null);
				}
			}
		});
		viewActivePersonsItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (controller.getNumPersons() > 0) {
					ArrayList<PersonByTaskModel> activePersons = controller.getActivePersons();
					PersonTableDialog ev = new PersonTableDialog(MainFrame.this, "Active Roster",
							PersonTableModel.getMinimumExpansion(), null, activePersons, "", null, null, null, null);

					do {
						ev = processViewActivePersonsDialog(ev.getDialogResponse());
					} while (ev != null);
				}
			}
		});
	}

	private PersonTableDialog processViewAllPersonsDialog(PersonTableEvent event) {
		if (event != null && event.getButtonId() != PersonTableDialog.getCloseButtonId()) {
			if (event.getButtonId() == PersonTableDialog.getAddPersonButtonId()) {
				// Add new person
				ArrayList<AssignedTasksModel> assignedList = new ArrayList<AssignedTasksModel>();
				ArrayList<ProgramModel> progList = controller.getAllPrograms();
				ArrayList<JList<TaskModel>> taskListByProgram = new ArrayList<JList<TaskModel>>();
				ArrayList<ArrayList<AssignedTasksModel>> assignedTaskListByProgram = new ArrayList<ArrayList<AssignedTasksModel>>();
				getAssignedTaskLists(assignedList, progList, taskListByProgram, assignedTaskListByProgram);

				PersonDialog personEvent = new PersonDialog(MainFrame.this, selectedProgramName,
						controller.getAllTasks(), progList, taskListByProgram, assignedTaskListByProgram);

				do {
					personEvent = processAddPersonDialog(personEvent);
				} while (personEvent != null);

			} else if (event.getButtonId() == PersonTableDialog.getEditRowButtonId()) {
				// Edit person
				editPerson(event.getPersonName());
				updateMonth((Calendar) calPanel.getCurrentCalendar());
			}

			else if (event.getButtonId() == PersonTableDialog.getRemovePersonRowButtonId()) {
				// Remove person from roster
				if (removePerson(event.getPersonName()))
					updateMonth((Calendar) calPanel.getCurrentCalendar());
			}

			// Refresh data and re-open Person Table dialog
			ArrayList<PersonByTaskModel> allPersons = controller.getAllPersons();
			PersonTableDialog ev = new PersonTableDialog(MainFrame.this, "Complete Roster",
					PersonTableModel.getMinimumExpansion(), null, allPersons, "Add person", null, null, null, null);
			return ev;
		}
		return null;
	}

	private PersonTableDialog processViewActivePersonsDialog(PersonTableEvent event) {
		if (event != null && event.getButtonId() != PersonTableDialog.getCloseButtonId()) {
			if (event.getButtonId() == PersonTableDialog.getEditRowButtonId()) {
				// Edit person
				editPerson(event.getPersonName());
				updateMonth((Calendar) calPanel.getCurrentCalendar());
			}
			else if (event.getButtonId() == PersonTableDialog.getRemovePersonRowButtonId()) {
				// Remove person from roster
				if (removePerson(event.getPersonName()))
					updateMonth((Calendar) calPanel.getCurrentCalendar());
			}

			// Refresh data and re-open Person Table dialog
			ArrayList<PersonByTaskModel> activePersons = controller.getActivePersons();
			PersonTableDialog ev = new PersonTableDialog(MainFrame.this, "Active Roster",
					PersonTableModel.getMinimumExpansion(), null, activePersons, "", null, null, null, null);
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
				ArrayList<String> programList = controller.getAllProgramsAsString();
				FilterListDialog ev = new FilterListDialog(MainFrame.this, "Filter Calendar by program", programList);
				ArrayList<String> dialogResponse = ev.getDialogResponse();

				// Only one filter can be active
				setCalendarFilter(PROGRAM_FILTER, dialogResponse);
				updateMonth((Calendar) calPanel.getCurrentCalendar());
			}
		});
		filterByPersonItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ArrayList<String> personList = controller.getAllPersonsAsString();
				FilterListDialog ev = new FilterListDialog(MainFrame.this, "Filter Calendar by person", personList);
				ArrayList<String> dialogResponse = ev.getDialogResponse();

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
				ArrayList<String> locationList = controller.getAllLocationsAsString();
				FilterListDialog ev = new FilterListDialog(MainFrame.this, "Filter Calendar by location", locationList);
				ArrayList<String> dialogResponse = ev.getDialogResponse();

				// Only one filter can be active
				setCalendarFilter(LOCATION_FILTER, dialogResponse);
				updateMonth((Calendar) calPanel.getCurrentCalendar());
			}
		});
		filterByTimeItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ArrayList<String> timeList = controller.getAllTimesAsString();
				FilterListDialog ev = new FilterListDialog(MainFrame.this, "Filter Calendar by time", timeList);
				ArrayList<String> dialogResponse = ev.getDialogResponse();

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
		processCreateTaskDialog(taskEvent, null, null);
	}

	private void editTask(String programName, String origTaskName) {
		if (programName == null)
			programName = controller.findProgramByTaskName(origTaskName);

		TaskModel task = controller.getTaskByName(origTaskName);
		TimeModel origTaskTime = task.getTime();
		TaskDialog taskEvent = new TaskDialog(MainFrame.this, programName, task);
		processCreateTaskDialog(taskEvent, origTaskName, origTaskTime);
	}

	private void cloneTask(TaskModel task) {
		TimeModel origTaskTime = task.getTime();
		TaskEvent ev = new TaskEvent(MainFrame.this, selectedProgramName, null, task.getLocation(),
				task.getNumLeadersReqd(), task.getTotalPersonsReqd(), task.getDayOfWeek(), task.getWeekOfMonth(),
				task.getTime(), task.getColor());

		TaskDialog taskEvent = new TaskDialog(MainFrame.this, ev, task.getTaskID(), task.getProgramID());
		processCreateTaskDialog(taskEvent, null, origTaskTime);
	}

	private boolean deleteTask(String taskName) {
		if (JOptionPane.showConfirmDialog(MainFrame.this,
				"Are you sure you want to delete " + taskName + " task" + "\n and all corresponding task assignments? ",
				"Task Delete", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
			controller.deleteTask(taskName);
			return true;
		}
		return false;
	}

	private void processCreateTaskDialog(TaskDialog taskEvent, String origTaskName, TimeModel origTaskTime) {
		// Loop until user enters valid and unique task name OR cancels
		while (taskEvent.getDialogResponse() != null) {
			TaskEvent dialogResponse = taskEvent.getDialogResponse();
			TaskModel task = controller.getTaskByName(dialogResponse.getTaskName());

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
					taskEvent = new TaskDialog(MainFrame.this, dialogResponse, task.getTaskID(), task.getProgramID());

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
					task = controller.renameTask(dialogResponse.getProgramName(), origTaskName,
							dialogResponse.getTaskName());

				if (task != null) {
					controller.updateTask(task.getTaskID(), dialogResponse, origTaskTime);
					updateMonth((Calendar) calPanel.getCurrentCalendar());
				}
				break;
			}
		}
	}

	private PersonDialog processAddPersonDialog(PersonDialog personEvent) {
		PersonEvent dialogResponse = personEvent.getDialogResponse();

		if (dialogResponse != null) {
			if (controller.checkPersonExists(dialogResponse.getName())) {
				JOptionPane.showMessageDialog(MainFrame.this,
						"'" + dialogResponse.getName() + "' already exists. Please use a different name.");

				// Do not save; go back and edit person
				personEvent = new PersonDialog(MainFrame.this, selectedProgramName, dialogResponse.getAllTasks(),
						new PersonModel(dialogResponse.getPersonID(), dialogResponse.getName(),
								dialogResponse.getPhone(), dialogResponse.getEmail(), dialogResponse.isLeader(),
								dialogResponse.getNotes(), dialogResponse.getAssignedTaskChanges(),
								dialogResponse.getDatesUnavailable(), dialogResponse.getExtraDates()),
						dialogResponse.getProgramList(), dialogResponse.getTaskListByProgram(),
						dialogResponse.getAssignedTaskListByProgram());
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
			if (!origName.equals(dialogResponse.getName()) && controller.checkPersonExists(dialogResponse.getName())) {
				// Renaming to an existing person name not allowed
				JOptionPane.showMessageDialog(MainFrame.this,
						"'" + dialogResponse.getName() + "' already exists. Please use a different name.");

				// Go back and edit person
				personEvent = new PersonDialog(MainFrame.this, selectedProgramName, dialogResponse.getAllTasks(),
						new PersonModel(dialogResponse.getPersonID(), dialogResponse.getName(),
								dialogResponse.getPhone(), dialogResponse.getEmail(), dialogResponse.isLeader(),
								dialogResponse.getNotes(), dialogResponse.getAssignedTaskChanges(),
								dialogResponse.getDatesUnavailable(), dialogResponse.getExtraDates()),
						dialogResponse.getProgramList(), dialogResponse.getTaskListByProgram(),
						dialogResponse.getAssignedTaskListByProgram());
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
			JOptionPane.showMessageDialog(MainFrame.this, "Person does not exist");
		else {
			// TODO: Must be a cleaner way to do this
			ArrayList<ProgramModel> progList = controller.getAllPrograms();
			ArrayList<JList<TaskModel>> taskListByProgram = new ArrayList<JList<TaskModel>>();
			ArrayList<ArrayList<AssignedTasksModel>> assignedTaskListByProgram = new ArrayList<ArrayList<AssignedTasksModel>>();
			getAssignedTaskLists(person.getAssignedTasks(), progList, taskListByProgram, assignedTaskListByProgram);

			PersonDialog personEvent = new PersonDialog(MainFrame.this, selectedProgramName, controller.getAllTasks(),
					person, progList, taskListByProgram, assignedTaskListByProgram);
			do {
				personEvent = processEditPersonDialog(personEvent, origName);
			} while (personEvent != null);
		}
	}

	private boolean removePerson(String personName) {
		int answer = JOptionPane.showConfirmDialog(MainFrame.this,
				"Are you sure you want to permanently remove\n" + personName + " from the roster?", "Remove person",
				JOptionPane.YES_NO_OPTION);
		if (answer == JOptionPane.YES_OPTION) {
			controller.removePerson(personName);
			return true;
		} else
			return false;
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
					selectedTask = controller.getTaskByName(task.getTask().getTaskName());
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
					ArrayList<PersonByTaskModel> personsToday = controller.getPersonsByDay(selectedCalendar);
					ArrayList<String> personsAvail = controller.getAvailPersonsAsString(selectedCalendar);
					Calendar calendar = (Calendar) selectedCalendar.clone();
					PersonTableDialog ev = new PersonTableDialog(MainFrame.this,
							"Roster for " + selectedTask.getTaskName() + " on "
									+ Utilities.getDisplayDate(selectedCalendar),
							PersonTableModel.getExpansionByDay(), selectedTask.getTaskName(), personsToday, "Add sub",
							calendar, personsAvail, null, null);

					do {
						ev = processViewRosterByTaskDialog(ev.getDialogResponse());
					} while (ev != null);
				}
			}
		});
		viewRosterByTimeItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Create time list with single time element
				ArrayList<TimeModel> timeList = new ArrayList<TimeModel>();
				timeList.add(new TimeModel(selectedCalendar));

				// View assigned persons by time
				ArrayList<PersonByTaskModel> personsByTime = controller.getPersonsByDayByTime(selectedCalendar);
				ArrayList<String> personsAvail = controller.getAvailPersonsAsString(selectedCalendar);
				Calendar calendar = (Calendar) selectedCalendar.clone();

				PersonTableDialog ev = new PersonTableDialog(MainFrame.this,
						"Roster for " + Utilities.getDisplayDate(selectedCalendar) + " at "
								+ Utilities.formatTime(selectedCalendar),
						PersonTableModel.getExpansionByDay(), null, personsByTime, "Add floater", calendar,
						personsAvail, timeList, null);

				do {
					ev = processViewRosterByTimeDialog(ev.getDialogResponse());
				} while (ev != null);
			}
		});
		viewRosterByLocationItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (selectedTask != null) {
					if (selectedTask.getLocation() != null && !selectedTask.getLocation().equals("")) {
						// View assigned persons by location
						ArrayList<PersonByTaskModel> personsByLoc = controller
								.getPersonsByDayByLocation(selectedCalendar, selectedTask.getLocation());
						Calendar calendar = (Calendar) selectedCalendar.clone();

						ArrayList<String> personsAvail = controller.getAvailPersonsAsString(selectedCalendar);
						PersonTableDialog ev = new PersonTableDialog(MainFrame.this,
								"Roster at " + selectedTask.getLocation() + " for "
										+ Utilities.getDisplayDate(selectedCalendar),
								PersonTableModel.getExpansionByDay(), null, personsByLoc, "", calendar, personsAvail,
								null, null);

						do {
							ev = processViewRosterByLocationDialog(ev.getDialogResponse());
						} while (ev != null);

					} else {
						JOptionPane.showMessageDialog(MainFrame.this, "Location not provided for this task");
					}
				}
			}
		});
		viewCompleteRosterForToday.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// View all persons
				ArrayList<PersonByTaskModel> personsByTask = controller.getPersonsByDay(selectedCalendar);
				ArrayList<String> personsAvail = controller.getAvailPersonsAsString(selectedCalendar);
				ArrayList<TimeModel> timesToday = controller.getAllTimesByDay(selectedCalendar);
				ArrayList<TaskTimeModel> tasksToday = controller.getAllTasksWithTimeByDay(selectedCalendar);
				Calendar calendar = (Calendar) selectedCalendar.clone();

				PersonTableDialog ev = new PersonTableDialog(MainFrame.this,
						"Roster for " + Utilities.getDisplayDate(selectedCalendar),
						PersonTableModel.getExpansionByDay(), null, personsByTask, "Add floater", calendar,
						personsAvail, timesToday, tasksToday);

				do {
					ev = processViewCompleteRosterDialog(ev.getDialogResponse());
				} while (ev != null);
			}
		});
	}

	private PersonTableDialog processViewRosterByTaskDialog(PersonTableEvent event) {
		if (event != null && event.getButtonId() != PersonTableDialog.getCloseButtonId()) {
			if (event.getButtonId() == PersonTableDialog.getAddPersonButtonId()) {
				controller.addSingleInstanceTask(event.getPersonList(), selectedProgramName, selectedCalendar,
						selectedTask, selectedTask.getColor());
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
			ArrayList<PersonByTaskModel> personsToday = controller.getPersonsByDay(selectedCalendar);
			ArrayList<String> personsAvail = controller.getAvailPersonsAsString(selectedCalendar);
			Calendar calendar = (Calendar) selectedCalendar.clone();

			PersonTableDialog ev = new PersonTableDialog(MainFrame.this,
					"Roster for " + selectedTask.getTaskName() + " on " + Utilities.getDisplayDate(selectedCalendar),
					PersonTableModel.getExpansionByDay(), selectedTask.getTaskName(), personsToday, "Add sub", calendar,
					personsAvail, null, null);

			return ev;
		}
		return null;
	}

	private PersonTableDialog processViewCompleteRosterByTaskDialog(PersonTableEvent event, TaskModel task) {
		if (event != null && event.getButtonId() != PersonTableDialog.getCloseButtonId()) {
			if (event.getButtonId() == PersonTableDialog.getAddPersonButtonId()) {
				for (int i = 0; i < event.getPersonList().size(); i++)
					editPerson(event.getPersonList().get(i));
				updateMonth((Calendar) calPanel.getCurrentCalendar());
			}

			else if (event.getButtonId() == PersonTableDialog.getEditRowButtonId()) {
				editPerson(event.getPersonName());
				updateMonth((Calendar) calPanel.getCurrentCalendar());
			}

			// Refresh data and re-open Person Table Dialog
			ArrayList<PersonByTaskModel> personsByTask = controller.getPersonsByTask(task);
			ArrayList<String> personsAvail = controller.getAllPersonsAsString();
			PersonTableDialog ev = new PersonTableDialog(MainFrame.this, "Monthly Roster for " + task.getTaskName(),
					PersonTableModel.getExpansionByTask(), task.getTaskName(), personsByTask, "Add person", null,
					personsAvail, null, null);

			return ev;
		}
		return null;
	}

	private PersonTableDialog processViewRosterByTimeDialog(PersonTableEvent event) {
		if (event != null && event.getButtonId() != PersonTableDialog.getCloseButtonId()) {
			if (event.getButtonId() == PersonTableDialog.getAddPersonButtonId()) {
				// Adding floater
				controller.addSingleInstanceTask(event.getPersonList(), selectedProgramName, event.getCalendar(), null,
						event.getColor());
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
			ArrayList<TimeModel> timeList = new ArrayList<TimeModel>();
			timeList.add(new TimeModel(event.getCalendar()));

			// Refresh data and re-open Person Table dialog
			ArrayList<PersonByTaskModel> personsByTime = controller.getPersonsByDayByTime(event.getCalendar());
			ArrayList<String> personsAvail = controller.getAvailPersonsAsString(event.getCalendar());
			Calendar calendar = (Calendar) selectedCalendar.clone();

			PersonTableDialog ev = new PersonTableDialog(MainFrame.this,
					"Roster for " + Utilities.getDisplayDate(event.getCalendar()) + " at "
							+ Utilities.formatTime(event.getCalendar()),
					PersonTableModel.getExpansionByDay(), null, personsByTime, "Add floater", calendar, personsAvail,
					timeList, null);

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
			ArrayList<PersonByTaskModel> personsByLoc = controller.getPersonsByDayByLocation(selectedCalendar,
					selectedTask.getLocation());
			Calendar calendar = (Calendar) selectedCalendar.clone();

			PersonTableDialog ev = new PersonTableDialog(MainFrame.this,
					"Roster at " + selectedTask.getLocation() + " for " + Utilities.getDisplayDate(selectedCalendar),
					PersonTableModel.getExpansionByDay(), null, personsByLoc, "", calendar, null, null, null);

			return ev;
		}
		return null;
	}

	private PersonTableDialog processViewCompleteRosterDialog(PersonTableEvent event) {
		if (event != null && event.getButtonId() != PersonTableDialog.getCloseButtonId()) {
			if (event.getButtonId() == PersonTableDialog.getAddPersonButtonId()) {
				controller.addSingleInstanceTask(event.getPersonList(), selectedProgramName, event.getCalendar(), null,
						event.getColor());
				updateMonth((Calendar) calPanel.getCurrentCalendar());

			} else if (event.getButtonId() == PersonTableDialog.getEditRowButtonId()) {
				editPerson(event.getPersonName());
				updateMonth((Calendar) calPanel.getCurrentCalendar());
			}

			else if (event.getButtonId() == PersonTableDialog.getRemovePersonRowButtonId()) {
				if (event.isSingleInstanceTask())
					controller.removeSingleInstanceTask(event.getPersonName(), event.getCalendar());
				else
					controller.markPersonUnavail(event.getPersonName(), selectedCalendar);
				updateMonth((Calendar) calPanel.getCurrentCalendar());
			}

			else if (event.getButtonId() == PersonTableDialog.getSubToFloaterRowButtonId()) {
				controller.updateSingleInstanceTaskId(event.getPersonName(), event.getCalendar(), event.getTaskName());
				updateMonth((Calendar) calPanel.getCurrentCalendar());
			}

			else if (event.getButtonId() == PersonTableDialog.getFloaterToSubRowButtonId()) {
				controller.updateSingleInstanceTaskId(event.getPersonName(), event.getCalendar(), event.getTaskName());
				updateMonth((Calendar) calPanel.getCurrentCalendar());
			}

			// Refresh data and re-open Person Table Dialog
			ArrayList<PersonByTaskModel> personsToday = controller.getPersonsByDay(selectedCalendar);
			ArrayList<String> personsAvail = controller.getAvailPersonsAsString(selectedCalendar);
			ArrayList<TimeModel> timesToday = controller.getAllTimesByDay(selectedCalendar);
			ArrayList<TaskTimeModel> tasksToday = controller.getAllTasksWithTimeByDay(selectedCalendar);
			Calendar calendar = (Calendar) selectedCalendar.clone();

			PersonTableDialog ev = new PersonTableDialog(MainFrame.this,
					"Roster for " + Utilities.getDisplayDate(selectedCalendar), PersonTableModel.getExpansionByDay(),
					null, personsToday, "Add floater", calendar, personsAvail, timesToday, tasksToday);

			return ev;
		}
		return null;
	}

	private void updateMonth(Calendar calendar) {
		Calendar localCalendar = (Calendar) calendar.clone();

		localCalendar.set(Calendar.DAY_OF_MONTH, 1);
		if (selectedFilterId == LOCATION_FILTER)
			calPanel.updateTasksByMonth(controller.getTasksByLocationByMonth(localCalendar, filteredList));
		else if (selectedFilterId == TIME_FILTER)
			calPanel.updateTasksByMonth(controller.getTasksByTimeByMonth(localCalendar, filteredList));
		else if (selectedFilterId == PERSON_FILTER)
			calPanel.updateTasksByMonth(controller.getTasksByPersonsByMonth(localCalendar, filteredList));
		else if (selectedFilterId == PROGRAM_FILTER)
			calPanel.updateTasksByMonth(controller.getTasksByProgramByMonth(localCalendar, filteredList));
		else if (selectedFilterId == ROSTER_FILTER)
			calPanel.updateTasksByMonth(controller.getTasksByIncompleteRosterByMonth(localCalendar));
		else
			calPanel.updateTasksByMonth(controller.getAllTasksAndFloatersByMonth(localCalendar));

		calPanel.refresh();
	}

	// Calendar filters
	private void setCalendarFilter(int filterId, ArrayList<String> list) {
		if (filteredList != null)
			filteredList.clear();
		filteredList = list;

		// Roster filter has a null list
		if (filteredList == null && filterId != ROSTER_FILTER)
			selectedFilterId = NO_FILTER;
		else
			selectedFilterId = filterId;

		calPanel.setCalendarFilter(filterNames[selectedFilterId]);
	}

	private void setProgramName(String progName) {
		selectedProgramName = progName;
		calPanel.setProgramName(selectedProgramName);
	}

	private void getAssignedTaskLists(ArrayList<AssignedTasksModel> assignedList, ArrayList<ProgramModel> progList,
			ArrayList<JList<TaskModel>> taskListByProgram,
			ArrayList<ArrayList<AssignedTasksModel>> assignedTaskListByProgram) {
		for (int i = 0; i < progList.size(); i++) {
			// Get all tasks for each program and add to task list
			String programName = progList.get(i).getProgramName();
			taskListByProgram.add(i, controller.getAllTasksByProgram(programName));

			// Create new assigned task list for this program
			assignedTaskListByProgram.add(i, new ArrayList<AssignedTasksModel>());
			ArrayList<AssignedTasksModel> thisAssignedTaskList = assignedTaskListByProgram.get(i);

			// Add any assigned task to assigned task list for this program
			for (int j = 0; j < assignedList.size(); j++) {
				AssignedTasksModel assignedTask = assignedList.get(j);
				if (assignedTask.getProgramName().equals(programName))
					thisAssignedTaskList.add(assignedTask);
			}
		}
	}
}
