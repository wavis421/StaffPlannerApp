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
import javax.swing.text.Position;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

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

	// Calendar filters
	JList<String> programFilter = null;
	JList<String> personFilter = null;

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
		JMenu programSelectMenu = new JMenu("Select active program");
		programMenu.add(programCreateItem);
		programMenu.add(programEditItem);
		programMenu.add(programSelectMenu);

		// Add task sub-menus
		JMenuItem taskCreateItem = new JMenuItem("Create task");
		JMenu taskEditMenu = new JMenu("Edit task");
		taskMenu.add(taskCreateItem);
		taskMenu.add(taskEditMenu);

		// Add persons sub-menus
		JMenuItem personAddItem = new JMenuItem("Add person");
		JMenu personEditMenu = new JMenu("Edit person");
		personMenu.add(personAddItem);
		personMenu.add(personEditMenu);

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

						int numPrograms = controller.getNumPrograms();
						if (numPrograms == 1) {
							JList<String> programList = controller.getAllProgramsAsString();
							selectedProgramName = programList.getModel().getElementAt(0);
							calPanel.setProgramName(selectedProgramName);
						} else if (numPrograms > 1) {
							JList<String> programList = controller.getAllProgramsAsString();
							selectActiveProgramDialog ev = new selectActiveProgramDialog(MainFrame.this, programList);
							String dialogResponse = ev.getDialogResponse();
							if (dialogResponse != null) {
								System.out.println("Select active program: " + dialogResponse);
								selectedProgramName = dialogResponse;
								calPanel.setProgramName(selectedProgramName);
							}
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
						if (dialogResponse.isSelectedActive()) {
							selectedProgramName = dialogResponse.getProgramName();
							calPanel.setProgramName(selectedProgramName);
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

							programList.removeAll();
							programSelectMenu.removeAll();
						}
					});
				}
			}
		});

		// Set up listeners for TASK menu
		taskCreateItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				createTask();
			}
		});
		taskEditMenu.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				taskEditMenu.removeAll();

				if (selectedProgramName == null) {
					JOptionPane.showMessageDialog(MainFrame.this, "Select Program first!");

				} else {
					JList<TaskModel> taskList = controller.getAllTasks(selectedProgramName);
					taskList.setCellRenderer(new TaskRenderer());

					for (int i = 0; i < taskList.getModel().getSize(); i++) {
						JMenuItem taskItem = new JMenuItem(taskList.getModel().getElementAt(i).toString());
						taskEditMenu.add(taskItem);

						taskItem.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								String origName = taskItem.getText();
								System.out.println("Task Update listener: name = " + origName);

								editTask(origName);

								taskList.removeAll();
								taskEditMenu.removeAll();
							}
						});
					}
				}
			}
		});

		// Set up listeners for PERSONS menu
		personAddItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CreateUpdatePersonDialog personEvent = new CreateUpdatePersonDialog(MainFrame.this,
						createAssignedTasksTree(new LinkedList<AssignedTasksModel>()), createTaskTree());
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
							System.out.println("Person Edit listener: name = " + origName);

							editPerson(origName);

							personList.removeAll();
							personEditMenu.removeAll();
						}
					});
				}
			}
		});

		// Set up listeners for CALENDAR menu
		filterByProgramItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JList<String> programList = controller.getAllProgramsAsString();
				FilterCalendarDialog ev = new FilterCalendarDialog(MainFrame.this, "program", programList);
				JList<String> dialogResponse = ev.getDialogResponse();

				// Only one filter can be active
				programFilter = dialogResponse;
				if (programFilter != null)
					personFilter = null;
				updateMonth((Calendar) calPanel.getCurrentCalendar().clone());
			}
		});
		// TBD: filter by person, by Staff/volunteer, by tasks that require more
		// people
		filterByPersonItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JList<String> personList = controller.getAllPersonsAsString();
				FilterCalendarDialog ev = new FilterCalendarDialog(MainFrame.this, "person", personList);
				JList<String> dialogResponse = ev.getDialogResponse();

				// Only one filter can be active
				personFilter = dialogResponse;
				if (personFilter != null)
					programFilter = null;
				updateMonth((Calendar) calPanel.getCurrentCalendar().clone());
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
				LinkedList<AssignedTasksModel> taskList = dialogResponse.getAssignedTasks();
				personEvent = new CreateUpdatePersonDialog(MainFrame.this,
						new PersonModel(dialogResponse.getName(), dialogResponse.getPhone(), dialogResponse.getEmail(),
								dialogResponse.isStaff(), dialogResponse.getNotes(), taskList),
						createAssignedTasksTree(taskList), createTaskTree());
				processAddPersonDialog(personEvent);

			} else {
				// Add person to database
				controller.addPerson(dialogResponse.getName(), dialogResponse.getPhone(), dialogResponse.getEmail(),
						dialogResponse.isStaff(), dialogResponse.getNotes(), dialogResponse.getAssignedTasks());
			}
		}
	}

	private void processEditPersonDialog(CreateUpdatePersonDialog personEvent, String origName) {
		PersonEvent dialogResponse = personEvent.getDialogResponse();
		boolean isOkToSave = personEvent.getOkToSaveStatus();

		if (dialogResponse != null) {
			if (!isOkToSave) {
				personEvent = new CreateUpdatePersonDialog(MainFrame.this,
						new PersonModel(dialogResponse.getName(), dialogResponse.getPhone(), dialogResponse.getEmail(),
								dialogResponse.isStaff(), dialogResponse.getNotes(), dialogResponse.getAssignedTasks()),
						createAssignedTasksTree(dialogResponse.getAssignedTasks()), createTaskTree());
				processEditPersonDialog(personEvent, origName);
			} else {
				// Update task list and refresh calendar
				if (!origName.equals(dialogResponse.getName()))
					controller.renamePerson(origName, dialogResponse.getName());
				controller.updatePerson(dialogResponse);
			}
		}
	}

	private void editPerson(String origName) {
		System.out.println("Person EDIT: name = " + origName);

		PersonModel person = controller.getPersonByName(origName);
		if (person == null)
			JOptionPane.showMessageDialog(null, "Person does not exist");
		else {
			CreateUpdatePersonDialog personEvent = new CreateUpdatePersonDialog(MainFrame.this, person,
					createAssignedTasksTree(person.getAssignedTasks()), createTaskTree());
			processEditPersonDialog(personEvent, origName);
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
			if (programFilter != null)
				tasks = controller.getTasksByDayByProgram(calendar, programFilter);
			else if (personFilter != null)
				tasks = controller.getTasksByDayByPerson(calendar, personFilter);
			else
				tasks = controller.getAllTasksByDay(calendar);
				
			calPanel.updateTasksByDay(i, tasks);
		}
		calPanel.refresh();
	}

	private JTree createTaskTree() {
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Select task to assign  >>>");
		DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);
		LinkedList<ProgramModel> programList = controller.getAllPrograms();

		for (int i = 0; i < programList.size(); i++) {
			ProgramModel p = programList.get(i);
			DefaultMutableTreeNode pNode = new DefaultMutableTreeNode(p);
			rootNode.add(pNode);

			JList<TaskModel> taskList = controller.getAllTasks(p.getProgramName());

			for (int j = 0; j < taskList.getModel().getSize(); j++) {
				TaskModel task = taskList.getModel().getElementAt(j);
				pNode.add(new DefaultMutableTreeNode(task));
			}
		}
		JTree tree = new JTree(treeModel);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setShowsRootHandles(true);
		tree.setCellRenderer(new TaskTreeRenderer());
		return (tree);
	}

	private JTree createAssignedTasksTree(LinkedList<AssignedTasksModel> taskList) {
		boolean pathFound;
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Assigned tasks");
		DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);
		JTree tree = new JTree(treeModel);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setShowsRootHandles(true);
		tree.expandRow(0);

		for (AssignedTasksModel item : taskList) {
			pathFound = false;
			for (int row = 1; row < tree.getRowCount(); row++) {
				TreePath path = tree.getNextMatch(item.getProgramName(), row, Position.Bias.Forward);
				if (path != null) {
					pathFound = true;
					tree.setSelectionPath(path);
					AssignTaskEvent taskEvent = new AssignTaskEvent(MainFrame.this, item.getProgramName(),
							controller.getTaskByName(item.getProgramName(), item.getTaskName()), item.getDaysOfWeek(),
							item.getWeeksOfMonth());
					treeModel.insertNodeInto(new DefaultMutableTreeNode(taskEvent),
							(DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent(), row);
					break;
				}
			}
			if (pathFound == false) {
				DefaultMutableTreeNode pNode = new DefaultMutableTreeNode(item.getProgramName());
				tree.setSelectionPath(tree.getPathForRow(0));
				treeModel.insertNodeInto(pNode, (DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent(),
						0);

				AssignTaskEvent taskEvent = new AssignTaskEvent(MainFrame.this, item.getProgramName(),
						controller.getTaskByName(item.getProgramName(), item.getTaskName()), item.getDaysOfWeek(),
						item.getWeeksOfMonth());

				pNode.add(new DefaultMutableTreeNode(taskEvent));
				tree.expandRow(0);
			}
		}

		tree.setCellRenderer(new AssignTaskTreeRenderer());
		return (tree);
	}
}
