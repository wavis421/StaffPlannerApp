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

	// Calendar filters
	private final int NO_FILTER = 0;
	private final int PROGRAM_FILTER = 1;
	private final int PERSON_FILTER = 2;
	private final int STAFF_FILTER = 3;
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
		JMenu personMenu = new JMenu("Staff/Volunteers");
		JMenu calendarMenu = new JMenu("Calendar");
		menuBar.add(fileMenu);
		menuBar.add(programMenu);
		menuBar.add(taskMenu);
		menuBar.add(personMenu);
		menuBar.add(calendarMenu);

		taskMenu.setEnabled(false);

		// Add file sub-menus
		JMenuItem exportProgramItem = new JMenuItem("Export program...  ");
		JMenuItem exportStaffItem = new JMenuItem("Export staff...  ");
		JMenuItem importProgramItem = new JMenuItem("Import program...  ");
		JMenuItem importStaffItem = new JMenuItem("Import staff...  ");
		JMenuItem exitItem = new JMenuItem("Exit");
		fileMenu.add(importProgramItem);
		fileMenu.add(exportProgramItem);
		fileMenu.add(importStaffItem);
		fileMenu.add(exportStaffItem);
		fileMenu.addSeparator();
		fileMenu.add(exitItem);

		// Add program sub-menus
		JMenuItem programCreateItem = new JMenuItem("New program");
		JMenuItem programEditItem = new JMenuItem("Edit program");
		JMenu programSelectMenu = new JMenu("Select active program");
		programMenu.add(programCreateItem);
		programMenu.add(programEditItem);
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
		personMenu.add(personAddItem);
		personMenu.add(personEditMenu);

		// Add calendar sub-menus
		JMenu calendarFilterMenu = new JMenu("Filter");
		calendarMenu.add(calendarFilterMenu);
		JMenuItem filterNoneItem = new JMenuItem("None");
		filterByProgramMenuItem = new JMenuItem("by Program");
		filterByPersonMenuItem = new JMenuItem("by Person");
		JMenuItem filterByStaffShortageItem = new JMenuItem("by Staff Shortage");
		calendarFilterMenu.add(filterNoneItem);
		calendarFilterMenu.add(filterByProgramMenuItem);
		calendarFilterMenu.add(filterByPersonMenuItem);
		calendarFilterMenu.add(filterByStaffShortageItem);
		if (controller.getNumPrograms() <= 1)
			filterByProgramMenuItem.setEnabled(false);
		if (controller.getNumPersons() <= 1)
			filterByPersonMenuItem.setEnabled(false);

		// Create listeners
		createFileMenuListeners(taskMenu, exportProgramItem, exportStaffItem, importProgramItem, importStaffItem,
				exitItem);
		createProgramMenuListeners(taskMenu, programCreateItem, programEditItem, programSelectMenu);
		createTaskMenuListeners(taskCreateItem, taskEditMenu, taskCloneMenu);
		createPersonMenuListeners(personAddItem, personEditMenu);
		createCalendarMenuListeners(filterNoneItem, filterByProgramMenuItem, filterByPersonMenuItem,
				filterByStaffShortageItem);

		return menuBar;
	}

	private void createFileMenuListeners(JMenu taskMenu, JMenuItem exportProgramItem, JMenuItem exportStaffItem,
			JMenuItem importProgramItem, JMenuItem importStaffItem, JMenuItem exitItem) {
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
						controller.loadProgramFromFile(fileChooser.getSelectedFile());
						updateMonth((Calendar) calPanel.getCurrentCalendar().clone());

						int numPrograms = controller.getNumPrograms();
						if (numPrograms == 1) {
							JList<String> programList = controller.getAllProgramsAsString();
							selectedProgramName = programList.getModel().getElementAt(0);
							calPanel.setProgramName(selectedProgramName);
							taskMenu.setEnabled(true);

						} else if (numPrograms > 1 && selectedProgramName == null) {
							JList<String> programList = controller.getAllProgramsAsString();
							selectActiveProgramDialog ev = new selectActiveProgramDialog(MainFrame.this, programList);
							String dialogResponse = ev.getDialogResponse();
							if (dialogResponse != null) {
								selectedProgramName = dialogResponse;
								calPanel.setProgramName(selectedProgramName);
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
		exportStaffItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (fileChooser.showSaveDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION) {
					try {
						controller.saveStaffToFile(fileChooser.getSelectedFile());

					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		});
		importStaffItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (fileChooser.showOpenDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION) {
					try {
						controller.loadStaffFromFile(fileChooser.getSelectedFile());
						updateMonth((Calendar) calPanel.getCurrentCalendar().clone());

						// Clear person filter if selected
						if (selectedFilterId == PERSON_FILTER) {
							setCalendarFilter(NO_FILTER, null);
							updateMonth((Calendar) calPanel.getCurrentCalendar().clone());
						}

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

	private void createProgramMenuListeners(JMenu taskMenu, JMenuItem programCreateItem, JMenuItem programEditItem,
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
							selectedProgramName = dialogResponse.getProgramName();
							calPanel.setProgramName(selectedProgramName);
							taskMenu.setEnabled(true);
						}
					}
				}
			}
		});
		programEditItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

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
							selectedProgramName = programItem.getText();
							calPanel.setProgramName(selectedProgramName);
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
							editTask(origName);

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

	private void createPersonMenuListeners(JMenuItem personAddItem, JMenu personEditMenu) {
		// Set up listeners for PERSONS menu
		personAddItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				LinkedList<AssignedTasksModel> assignedList = new LinkedList<AssignedTasksModel>();
				JTree taskTree = createTaskTree(assignedList);
				CreateUpdatePersonDialog personEvent = new CreateUpdatePersonDialog(MainFrame.this,
						createAssignedTasksTree(null, taskTree, assignedList), taskTree);
				processAddPersonDialog(personEvent);
			}
		});
		personEditMenu.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				personEditMenu.removeAll();
				JList<PersonModel> personList = controller.getAllPersons();

				for (int i = 0; i < personList.getModel().getSize(); i++) {
					JMenuItem personItem = new JMenuItem(personList.getModel().getElementAt(i).toString());
					personEditMenu.add(personItem);

					personItem.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ev) {
							String origName = personItem.getText();
							editPerson(origName);

							personList.removeAll();
							personEditMenu.removeAll();
						}
					});
				}
			}
		});
	}

	private void createCalendarMenuListeners(JMenuItem filterNoneItem, JMenuItem filterByProgramItem,
			JMenuItem filterByPersonItem, JMenuItem filterByShortStaffItem) {
		// Set up listeners for CALENDAR menu
		filterByProgramItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JList<String> programList = controller.getAllProgramsAsString();
				FilterListDialog ev = new FilterListDialog(MainFrame.this, "Filter Calendar by program", programList);
				JList<String> dialogResponse = ev.getDialogResponse();

				// Only one filter can be active
				setCalendarFilter(PROGRAM_FILTER, dialogResponse);
				updateMonth((Calendar) calPanel.getCurrentCalendar().clone());
			}
		});
		filterByPersonItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JList<String> personList = controller.getAllPersonsAsString();
				FilterListDialog ev = new FilterListDialog(MainFrame.this, "Filter Calendar by person", personList);
				JList<String> dialogResponse = ev.getDialogResponse();

				// Only one filter can be active
				setCalendarFilter(PERSON_FILTER, dialogResponse);
				updateMonth((Calendar) calPanel.getCurrentCalendar().clone());
			}
		});
		filterByShortStaffItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Set calendar filter
				setCalendarFilter(STAFF_FILTER, null);
				updateMonth((Calendar) calPanel.getCurrentCalendar().clone());
			}
		});
		filterNoneItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setCalendarFilter(NO_FILTER, null);
				updateMonth((Calendar) calPanel.getCurrentCalendar().clone());
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
					editTask(dialogResponse.getTaskName());
				else if (confirm == JOptionPane.NO_OPTION)
					createTaskRetry(dialogResponse);

			} else {
				// Add task and refresh calendar
				controller.addTask(dialogResponse);
				updateMonth((Calendar) calPanel.getCurrentCalendar().clone());
			}
		}
	}

	private void editTask(String origName) {
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

	private void cloneTask(TaskModel task) {
		TaskEvent ev = new TaskEvent(MainFrame.this, selectedProgramName, null, task.getLocation(),
				task.getNumStaffReqd(), task.getTotalPersonsReqd(), task.getDayOfWeek(), task.getWeekOfMonth(),
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
				LinkedList<AssignedTasksModel> assignedTaskList = dialogResponse.getAssignedTasks();
				JTree taskTree = createTaskTree(assignedTaskList);
				personEvent = new CreateUpdatePersonDialog(MainFrame.this,
						new PersonModel(dialogResponse.getName(), dialogResponse.getPhone(), dialogResponse.getEmail(),
								dialogResponse.isStaff(), dialogResponse.getNotes(), assignedTaskList),
						createAssignedTasksTree(dialogResponse.getLastTaskAdded(), taskTree, assignedTaskList),
						taskTree);
				processAddPersonDialog(personEvent);

			} else {
				// Add person to database
				controller.addPerson(dialogResponse.getName(), dialogResponse.getPhone(), dialogResponse.getEmail(),
						dialogResponse.isStaff(), dialogResponse.getNotes(), dialogResponse.getAssignedTasks());
				if (controller.getNumPersons() > 1)
					filterByPersonMenuItem.setEnabled(true);
				updateMonth((Calendar) calPanel.getCurrentCalendar().clone());
			}
		}
	}

	private void processEditPersonDialog(CreateUpdatePersonDialog personEvent, String origName) {
		PersonEvent dialogResponse = personEvent.getDialogResponse();
		boolean isOkToSave = personEvent.getOkToSaveStatus();

		if (dialogResponse != null) {
			if (!isOkToSave) {
				LinkedList<AssignedTasksModel> assignedList = dialogResponse.getAssignedTasks();
				JTree taskTree = createTaskTree(assignedList);
				personEvent = new CreateUpdatePersonDialog(MainFrame.this,
						new PersonModel(dialogResponse.getName(), dialogResponse.getPhone(), dialogResponse.getEmail(),
								dialogResponse.isStaff(), dialogResponse.getNotes(), dialogResponse.getAssignedTasks()),
						createAssignedTasksTree(dialogResponse.getLastTaskAdded(), taskTree, assignedList), taskTree);
				processEditPersonDialog(personEvent, origName);
			} else {
				// Update task list and refresh calendar
				if (!origName.equals(dialogResponse.getName()))
					controller.renamePerson(origName, dialogResponse.getName());
				controller.updatePerson(dialogResponse);
				updateMonth((Calendar) calPanel.getCurrentCalendar().clone());
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
					createAssignedTasksTree(null, taskTree, assignedList), taskTree);
			processEditPersonDialog(personEvent, origName);
		}
	}

	private void setCalendarPopupMenu() {
		JPopupMenu popupMenu = new JPopupMenu();
		JMenuItem editTaskItem = new JMenuItem("Edit task");
		popupMenu.add(editTaskItem);

		// Day Box listener
		calPanel.setDayBoxListener(new DayBoxListener() {
			public void dayBoxClicked(Calendar calendar, Point point, CalendarDayModel task) {
				selectedCalendar = calendar;
				selectedTask = controller.getTaskByName(selectedProgramName, task.getTask().getTaskName());
				System.out.println("day box clicked: day = " + calendar.get(Calendar.DAY_OF_MONTH) + ", room = "
						+ task.getTask().getLocation() + ", task name = " + task.getTask().getTaskName());

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
				LinkedList<CalendarDayModel> tasksByDay = controller.getAllTasksByDay(selectedCalendar);
				calPanel.updateTasksByDay(selectedCalendar.get(Calendar.DAY_OF_MONTH) - 1, tasksByDay);
				calPanel.refresh();
			}
		});
	}

	private void updateMonth(Calendar calendar) {
		LinkedList<CalendarDayModel> tasks;
		for (int i = 0; i < 31; i++) {
			calendar.set(Calendar.DAY_OF_MONTH, i + 1);
			if (selectedFilterId == PROGRAM_FILTER)
				tasks = controller.getTasksByDayByProgram(calendar, filteredList);
			else if (selectedFilterId == PERSON_FILTER)
				tasks = controller.getTasksByDayByPerson(calendar, filteredList);
			else if (selectedFilterId == STAFF_FILTER)
				tasks = controller.getTasksByDayByStaffShortage(calendar);
			else
				tasks = controller.getAllTasksByDay(calendar);

			calPanel.updateTasksByDay(i, tasks);
		}
		calPanel.refresh();
	}

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

	private void setCalendarFilter(int filterId, JList<String> list) {
		if (filteredList != null)
			filteredList.removeAll();

		filteredList = list;
		if ((filterId == PROGRAM_FILTER || filterId == PERSON_FILTER) && filteredList == null)
			selectedFilterId = NO_FILTER;
		else
			selectedFilterId = filterId;
	}
}
