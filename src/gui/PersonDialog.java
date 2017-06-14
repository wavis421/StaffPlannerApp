package gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.border.Border;
import javax.swing.tree.DefaultMutableTreeNode;

import model.AssignedTasksModel;
import model.DateRangeModel;
import model.ListStatus;
import model.PersonModel;
import model.ProgramModel;
import model.SingleInstanceTaskModel;
import model.TaskModel;
import utilities.Utilities;

public class PersonDialog extends JDialog {
	// Constants
	private static final int TEXT_FIELD_SIZE = 30;
	private static final int COMBO_BOX_WIDTH = 334;
	private static final int COMBO_BOX_HEIGHT = 30;

	private JButton okButton = new JButton("OK");
	private JButton cancelButton = new JButton("Cancel");
	private JButton unavailDatesButton = new JButton("Unavail Dates");
	private JButton extraDatesButton = new JButton("Extra Dates");

	// Private instance variables
	private int personID;
	private String currentProgramName;
	private JTextField personName = new JTextField(TEXT_FIELD_SIZE);
	private JTextField phone = new JTextField(TEXT_FIELD_SIZE);
	private JTextField email = new JTextField(TEXT_FIELD_SIZE);
	private JTextArea notesArea = new JTextArea(3, TEXT_FIELD_SIZE);
	private JRadioButton leaderButton = new JRadioButton("Leader");
	private JRadioButton assistantButton = new JRadioButton("Assistant");
	private ButtonGroup staffGroup = new ButtonGroup();
	private JComboBox<String> singleInstanceTaskCombo;
	private JComboBox<String> dateUnavailCombo;
	private JScrollPane assignedTasksScrollPane;
	private JScrollPane taskTreeScrollPane;
	private DefaultMutableTreeNode selectedNode;

	// Lists
	private AssignTaskCreateTree trees;
	private ArrayList<AssignedTasksModel> assignedTasksList;
	private ArrayList<SingleInstanceTaskModel> singleInstanceTaskList;
	private ArrayList<DateRangeModel> datesUnavailableList;
	private ArrayList<TaskModel> allTasks;
	private ArrayList<ProgramModel> programList;
	private ArrayList<JList<TaskModel>> taskListByProgram;
	private ArrayList<ArrayList<AssignedTasksModel>> assignedTaskListByProgram;

	// Labels
	private JLabel nameLabel = new JLabel("Person's name: ");
	private JLabel phoneLabel = new JLabel("Phone #: ");
	private JLabel emailLabel = new JLabel("Email: ");
	private JLabel staffLabel = new JLabel("Leader or assistant: ");
	private JLabel notesLabel = new JLabel("Notes: ");
	private JLabel datesLabel = new JLabel("Dates Unavailable: ");
	private JLabel singleTaskLabel = new JLabel("Extra Dates: ");

	// Dialog panels
	private JPanel controlsPanel;
	private JPanel buttonsPanel;
	private JPanel staffPanel = new JPanel();
	private PersonEvent dialogResponse;

	public PersonDialog(JFrame parent, String currentProgram, ArrayList<TaskModel> allTasks,
			ArrayList<ProgramModel> programList, ArrayList<JList<TaskModel>> taskListByProgram,
			ArrayList<ArrayList<AssignedTasksModel>> assignedTaskListByProgram) {
		// super(parent, "Add person...", true);
		super(parent, "Add person...");
		setLocation(new Point(100, 100));
		setModalityType(Dialog.DEFAULT_MODALITY_TYPE.APPLICATION_MODAL);
		this.currentProgramName = currentProgram;

		// TODO: find better names!!
		trees = new AssignTaskCreateTree(currentProgram, programList, taskListByProgram, assignedTaskListByProgram);
		createTrees(trees.getAssignedTaskTree(), trees.getTaskTree());

		// Save copy of lists used to build tree
		this.allTasks = allTasks;
		this.programList = programList;
		this.taskListByProgram = taskListByProgram;
		this.assignedTaskListByProgram = assignedTaskListByProgram;

		// Create empty lists for new person
		this.leaderButton.setSelected(true);
		this.assignedTasksList = new ArrayList<AssignedTasksModel>();
		this.singleInstanceTaskList = new ArrayList<SingleInstanceTaskModel>();
		this.datesUnavailableList = new ArrayList<DateRangeModel>();

		createUnavailDateCombo();
		createSingleInstanceTaskCombo();

		setupPersonDialog();
		setVisible(true);
	}

	// Constructor for updating existing person, PersonModel contains values
	public PersonDialog(JFrame parent, String currentProgram, ArrayList<TaskModel> allTasks, PersonModel person,
			ArrayList<ProgramModel> programList, ArrayList<JList<TaskModel>> taskListByProgram,
			ArrayList<ArrayList<AssignedTasksModel>> assignedTaskListByProgram) {
		super(parent, "Edit person...", true);
		setLocation(new Point(100, 100));
		setModalityType(Dialog.DEFAULT_MODALITY_TYPE.APPLICATION_MODAL);
		this.currentProgramName = currentProgram;

		// TODO: find better names!!
		trees = new AssignTaskCreateTree(currentProgram, programList, taskListByProgram, assignedTaskListByProgram);
		createTrees(trees.getAssignedTaskTree(), trees.getTaskTree());

		// Save copy of lists used to build tree
		this.allTasks = allTasks;
		this.programList = programList;
		this.taskListByProgram = taskListByProgram;
		this.assignedTaskListByProgram = assignedTaskListByProgram;

		// Save person fields
		this.personID = person.getPersonID();
		this.personName.setText(person.getName());
		this.phone.setText(person.getPhone());
		this.email.setText(person.getEmail());
		this.notesArea.setText(person.getNotes());
		this.assignedTasksList = person.getAssignedTasks();
		this.singleInstanceTaskList = person.getSingleInstanceTasks();
		this.datesUnavailableList = person.getDatesUnavailable();

		if (person.isLeader())
			this.leaderButton.setSelected(true);
		else
			this.assistantButton.setSelected(true);

		createUnavailDateCombo();
		createSingleInstanceTaskCombo();

		this.allTasks = allTasks;

		setupPersonDialog();
		setVisible(true);
	}

	public PersonEvent getDialogResponse() {
		return dialogResponse;
	}

	private void setupPersonDialog() {
		createStaffSelector();

		// Force the text area not to expand when user types more than 3 lines!!
		notesArea.setBorder(BorderFactory.createEtchedBorder());
		notesArea.setLineWrap(true);
		notesArea.setRows(3);
		notesArea.setMargin(new Insets(20, 20, 20, 20));
		notesArea.setAutoscrolls(false);
		notesArea.setPreferredSize(notesArea.getPreferredSize());

		unavailDatesButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DateRangeDialog ev = new DateRangeDialog(PersonDialog.this, "Select unavailable date range...",
						"Dates Unavailable: ", null);
				DateRangeEvent dateResponse = ev.getDialogResponse();
				if (dateResponse != null) {
					// Date range valid. Add to Linked List and Combo Box.
					DateRangeModel dateRangeModel = dateResponse.getSqlDateRange();
					dateRangeModel.setElementStatus(ListStatus.LIST_ELEMENT_NEW);
					datesUnavailableList.add(dateRangeModel);

					DefaultComboBoxModel<String> dateModel = (DefaultComboBoxModel<String>) dateUnavailCombo.getModel();
					if (dateResponse.getDateRange().getStartDate().equals(dateResponse.getDateRange().getEndDate()))
						// Add single date to list
						dateModel.addElement(dateResponse.getDateRange().getStartDate());
					else
						// Add date range to list
						dateModel.addElement(dateResponse.toString());
				}
			}
		});
		extraDatesButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DateRangeDialog ev = new DateRangeDialog(PersonDialog.this, "Select extra dates...", "Select Date: ",
						allTasks);
				DateRangeEvent dateResponse = ev.getDialogResponse();
				if (dateResponse != null && dateResponse.getTask() != null) {
					// Date and task valid
					Utilities.addTimeToCalendar(dateResponse.getStartDate(), dateResponse.getTask().getTime());
					SingleInstanceTaskModel singleTask = new SingleInstanceTaskModel(0, 0,
							dateResponse.getTask().getTaskID(), currentProgramName,
							dateResponse.getTask().getTaskName(), dateResponse.getStartDate(),
							dateResponse.getTask().getColor());

					// Check for date/time conflicts
					if (!checkConflictsForSingleInstanceTask(singleTask)) {
						// No conflicts; ready to add task
						singleTask.setElementStatus(ListStatus.LIST_ELEMENT_NEW);
						singleInstanceTaskList.add(singleTask);

						DefaultComboBoxModel<String> extraDateModel = (DefaultComboBoxModel<String>) singleInstanceTaskCombo
								.getModel();
						extraDateModel.addElement(dateResponse.getTask().getTaskName() + " on "
								+ Utilities.getDisplayDate(dateResponse.getStartDate()));
					}
				}
			}
		});
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Make sure that person name has been entered
				if (personName.getText().trim().equals("")) {
					JOptionPane.showMessageDialog(okButton, "Person's name field is required");
				} else {
					PersonEvent ev = new PersonEvent(this, personID, personName.getText().trim(),
							phone.getText().trim(), email.getText().trim(), leaderButton.isSelected() ? true : false,
							processNotesArea(), assignedTasksList, singleInstanceTaskList, datesUnavailableList,
							allTasks, programList, taskListByProgram, assignedTaskListByProgram);
					dialogResponse = ev;
					setVisible(false);
					dispose();
				}
			}
		});
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dialogResponse = null;
				setVisible(false);
				dispose();
			}
		});

		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setPersonLayout();
		setSize(750, 500);
	}

	private void setPersonLayout() {
		int gridY = 0;
		controlsPanel = new JPanel();
		buttonsPanel = new JPanel();

		controlsPanel.setLayout(new GridBagLayout());
		buttonsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

		// controlsPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		// Border titleBorder = BorderFactory.createRaisedSoftBevelBorder();
		Border etchedBorder = BorderFactory.createEtchedBorder();
		Border spaceBorder = BorderFactory.createEmptyBorder(15, 15, 15, 15);
		controlsPanel.setBorder(BorderFactory.createCompoundBorder(spaceBorder, etchedBorder));

		GridBagConstraints gc = new GridBagConstraints();

		gc.weightx = gc.weighty = 1;
		gc.fill = GridBagConstraints.NONE;

		// Add name, phone, email and notes fields
		addRowToControlPanel(gc, nameLabel, personName, gridY++);
		addRowToControlPanel(gc, phoneLabel, phone, gridY++);
		addRowToControlPanel(gc, emailLabel, email, gridY++);
		addRowToControlPanel(gc, staffLabel, staffPanel, gridY++);
		addRowToControlPanel(gc, datesLabel, dateUnavailCombo, gridY++);
		addRowToControlPanel(gc, singleTaskLabel, singleInstanceTaskCombo, gridY++);
		addRowToControlPanel(gc, notesLabel, notesArea, gridY++);
		addRowToControlPanel(gc, taskTreeScrollPane, assignedTasksScrollPane, gridY++);

		// Buttons row
		gc.gridy++;
		gc.gridx = 0;
		buttonsPanel.add(unavailDatesButton);
		gc.gridx++;
		buttonsPanel.add(extraDatesButton);
		gc.gridx++;
		buttonsPanel.add(okButton);
		gc.gridx++;
		buttonsPanel.add(cancelButton);

		// Add to panel
		setLayout(new BorderLayout());
		add(controlsPanel, BorderLayout.CENTER);
		add(buttonsPanel, BorderLayout.SOUTH);

		// make OK & cancel buttons the same size
		Dimension btnSize = cancelButton.getPreferredSize();
		okButton.setPreferredSize(btnSize);
	}

	// Generic method to add row with label and component
	private void addRowToControlPanel(GridBagConstraints gcon, Component value1, Component value2, int gridY) {
		gcon.gridx = 0;
		gcon.gridy = gridY;
		gcon.anchor = GridBagConstraints.EAST;
		gcon.insets = new Insets(0, 0, 0, 15);
		controlsPanel.add(value1, gcon);
		gcon.gridx++;
		gcon.anchor = GridBagConstraints.WEST;
		gcon.insets = new Insets(0, 0, 0, 0);
		controlsPanel.add(value2, gcon);
	}

	private void createStaffSelector() {
		leaderButton.setActionCommand("leader");
		assistantButton.setActionCommand("assistant");

		staffPanel.add(leaderButton);
		staffPanel.add(assistantButton);

		staffGroup.add(leaderButton);
		staffGroup.add(assistantButton);
	}

	private void createSingleInstanceTaskCombo() {
		DefaultComboBoxModel<String> taskModel = new DefaultComboBoxModel<String>();

		for (int i = 0; i < singleInstanceTaskList.size(); i++) {
			SingleInstanceTaskModel task = singleInstanceTaskList.get(i);
			Calendar date = task.getTaskDate();
			String taskName = task.getTaskName();
			if (taskName == null || taskName.equals("")) {
				taskName = "Floater";
				taskModel.addElement(
						taskName + " on " + Utilities.getDisplayDate(date) + " at " + Utilities.formatTime(date));
			} else {
				taskModel.addElement(taskName + " on " + Utilities.getDisplayDate(date));
			}
		}

		singleInstanceTaskCombo = new JComboBox<String>(taskModel);
		singleInstanceTaskCombo.setEditable(false);
		singleInstanceTaskCombo.setBorder(BorderFactory.createEtchedBorder());
		singleInstanceTaskCombo.setPreferredSize(new Dimension(COMBO_BOX_WIDTH, COMBO_BOX_HEIGHT));

		// Single Instance Task Combo POP UP menu
		JPopupMenu singleTaskComboPopup = new JPopupMenu();
		JMenuItem removeItem = new JMenuItem("Remove");
		singleTaskComboPopup.add(removeItem);
		singleTaskComboPopup.setPreferredSize(new Dimension(240, 25));

		// Single Instance Task Combo POP UP action listeners
		removeItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				String selectedItem = (String) singleInstanceTaskCombo.getSelectedItem();
				SingleInstanceTaskModel task = findSingleInstanceTaskMatch(selectedItem);
				if (task == null) {
					// This SHOULD NOT happen!!
					System.out.println("Error removing single instance task: " + selectedItem);
					return;
				}

				// Remove item from combo box and task list
				((DefaultComboBoxModel<String>) singleInstanceTaskCombo.getModel()).removeElement(selectedItem);
				if (task.getElementStatus() == ListStatus.LIST_ELEMENT_NEW)
					// Was just added, so remove from list
					singleInstanceTaskList.remove(task);
				else
					// Mark for deletion
					task.setElementStatus(ListStatus.LIST_ELEMENT_DELETE);
			}
		});

		singleInstanceTaskCombo.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3 && singleInstanceTaskCombo.getModel().getSize() > 0) {
					singleTaskComboPopup.show(singleInstanceTaskCombo, e.getX(), e.getY());
				}
			}
		});
	}

	private SingleInstanceTaskModel findSingleInstanceTaskMatch(String text) {
		for (int i = 0; i < singleInstanceTaskList.size(); i++) {
			SingleInstanceTaskModel task = singleInstanceTaskList.get(i);
			if (text.startsWith(task.getTaskName()) && text.contains(Utilities.getDisplayDate(task.getTaskDate()))) {
				return task;
			}
		}
		return null;
	}

	private void createUnavailDateCombo() {
		DefaultComboBoxModel<String> dateModel = new DefaultComboBoxModel<String>();

		for (int i = 0; i < datesUnavailableList.size(); i++) {
			DateRangeModel dateUnavail = datesUnavailableList.get(i);
			addDateUnavail(dateUnavail, dateModel);
		}

		dateUnavailCombo = new JComboBox<String>(dateModel);
		dateUnavailCombo.setEditable(false);
		dateUnavailCombo.setBorder(BorderFactory.createEtchedBorder());
		dateUnavailCombo.setPreferredSize(new Dimension(COMBO_BOX_WIDTH, COMBO_BOX_HEIGHT));

		// Date Unavail Combo POP UP menu
		JPopupMenu dateUnavailComboPopup = new JPopupMenu();
		JMenuItem removeItem = new JMenuItem("Remove");
		dateUnavailComboPopup.add(removeItem);
		dateUnavailComboPopup.setPreferredSize(new Dimension(240, 25));

		// Date Unavail Combo POP UP action listeners
		removeItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				String selectedItem = (String) dateUnavailCombo.getSelectedItem();
				DateRangeModel date = findDatesUnavailMatch(selectedItem);
				if (date == null) {
					// This SHOULD NOT happen!!
					System.out.println("Error removing Unavailable Dates: " + selectedItem);
					return;
				}

				// Remove item from combo box and unavail dates list
				((DefaultComboBoxModel<String>) dateUnavailCombo.getModel()).removeElement(selectedItem);
				if (date.getElementStatus() == ListStatus.LIST_ELEMENT_NEW)
					// Was just added, so remove from list
					datesUnavailableList.remove(date);
				else
					// Mark for deletion
					date.setElementStatus(ListStatus.LIST_ELEMENT_DELETE);
			}
		});

		dateUnavailCombo.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3 && dateUnavailCombo.getModel().getSize() > 0) {
					dateUnavailComboPopup.show(dateUnavailCombo, e.getX(), e.getY());
				}
			}
		});
	}

	private DateRangeModel findDatesUnavailMatch(String text) {
		for (int i = 0; i < datesUnavailableList.size(); i++) {
			DateRangeModel date = datesUnavailableList.get(i);
			if (date.getStartDate().equals(date.getEndDate())) {
				if (text.equals(Utilities.convertSqlDateToString(date.getStartDate()))) {
					return date;
				}
			} else if (text.equals((Utilities.convertSqlDateToString(date.getStartDate())) + "  to  "
						+ Utilities.convertSqlDateToString(date.getEndDate()))) {
				return date;
			}
		}
		return null;
	}

	private void addDateUnavail(DateRangeModel dateUnavail, DefaultComboBoxModel<String> dateModel) {

		// if (Utilities.isDateInThePast(dateUnavail.getEndDate(), "Error
		// parsing Unavailable Date(s)"))
		// TODO: Date range has passed; remove from list
		// datesUnavailable.remove(dateUnavail);

		if (dateUnavail.getStartDate().equals(dateUnavail.getEndDate()))
			// Add single date to list
			dateModel.addElement(Utilities.convertSqlDateToString(dateUnavail.getStartDate()));
		else
			// Add date range to list
			dateModel.addElement(Utilities.convertSqlDateToString(dateUnavail.getStartDate()) + "  to  "
					+ Utilities.convertSqlDateToString(dateUnavail.getEndDate()));
	}

	private String processNotesArea() {
		int numRows = 0, currIdx = 0, currLength = 0;
		for (int i = 0; i < 3 && notesArea.getText().length() > currLength; i++) {
			currIdx = notesArea.getText().indexOf("\n", currLength);
			if (currIdx != -1) { // Found new-line
				currLength = currIdx + 1;
				numRows = i + 1;
			}
		}
		int remainLength = notesArea.getText().length() - currLength;
		if (remainLength > (31 * (3 - numRows)))
			remainLength = 31 * (3 - numRows);
		return (notesArea.getText().substring(0, currLength + remainLength));
	}

	private void createTrees(JTree assignedTasksTree, JTree taskTree) {
		/* === Create assigned task tree === */
		Dimension assignDimension = new Dimension((int) notesArea.getPreferredSize().getWidth() + 4,
				(int) notesArea.getPreferredSize().getHeight() * 3);
		assignedTasksScrollPane = new JScrollPane(assignedTasksTree);
		assignedTasksScrollPane.setPreferredSize(assignDimension);
		assignedTasksScrollPane.getVerticalScrollBar().setUnitIncrement(16);
		assignedTasksTree.setCellRenderer(new TaskTreeRenderer((int) assignDimension.getWidth()));

		// Assigned Task Tree POP UP menu
		JPopupMenu assignPopup = new JPopupMenu();
		JMenuItem editItem = new JMenuItem("Edit");
		JMenuItem removeItem = new JMenuItem("Remove");
		assignPopup.add(editItem);
		assignPopup.add(removeItem);
		assignPopup.setPreferredSize(new Dimension(240, 50));

		// Assigned Task Tree POP UP action listeners
		editItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				AssignTaskDialog ev = new AssignTaskDialog(PersonDialog.this,
						(AssignTaskEvent) selectedNode.getUserObject());

				AssignTaskEvent eventResponse = ev.getDialogResponse();
				if (eventResponse != null) {
					// Update assigned task with new node info
					AssignedTasksModel lastAssignedTask = new AssignedTasksModel(eventResponse.getAssignedTaskID(),
							personID, eventResponse.getTask().getTaskID(), eventResponse.getProgramName(),
							eventResponse.getTask().getTaskName(), eventResponse.getDaysOfWeek(),
							eventResponse.getWeeksOfMonth());
					updateNodeInAssignedTaskList(lastAssignedTask);
					selectedNode.setUserObject(eventResponse);
				}
				assignedTasksTree.clearSelection();
				((AssignTaskEvent) (selectedNode.getUserObject())).setIsFocus(false);
			}
		});
		removeItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				AssignTaskEvent ev = (AssignTaskEvent) selectedNode.getUserObject();
				String programName = selectedNode.getParent().toString();

				// Remove task model from assigned tasks tree
				trees.removeNodeFromTree(assignedTasksTree, programName, ev.getTask().getTaskName());

				// Add task model to unassigned tasks tree
				TaskModel taskModel = new TaskModel(ev.getTask().getTaskID(), ev.getTask().getProgramID(),
						ev.getTask().getTaskName(), ev.getTask().getLocation(), ev.getTask().getNumLeadersReqd(),
						ev.getTask().getTotalPersonsReqd(), ev.getTask().getDayOfWeek(), ev.getTask().getWeekOfMonth(),
						ev.getTask().getTime(), ev.getTask().getColor());
				trees.addNodeToTree(taskTree, programName, taskModel);

				// Remove from assigned tasks lists
				deleteNodeInAssignedTaskList(ev.getTask().getTaskName());
				deleteNodeInAssignedTaskByProgList(ev.getTask().getTaskName());

				assignedTasksTree.clearSelection();
				((AssignTaskEvent) (selectedNode.getUserObject())).setIsFocus(false);
			}
		});

		// Assigned Task Tree mouse listener
		assignedTasksTree.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				int row = assignedTasksTree.getClosestRowForLocation(e.getX(), e.getY());
				assignedTasksTree.setSelectionRow(row);

				if (e.getButton() == MouseEvent.BUTTON3) {
					selectedNode = (DefaultMutableTreeNode) assignedTasksTree.getPathForRow(row).getLastPathComponent();
					if (selectedNode != null) {
						if (selectedNode.getLevel() == 2) {
							((AssignTaskEvent) (selectedNode.getUserObject())).setIsFocus(true);
							assignPopup.show(e.getComponent(), e.getX(), e.getY());
						}
					}
				}
			}
		});

		/* === Create unassigned task tree === */
		Dimension unassignDimension = new Dimension((int) notesArea.getPreferredSize().getWidth(),
				(int) notesArea.getPreferredSize().getHeight() * 3);
		taskTreeScrollPane = new JScrollPane(taskTree);
		taskTreeScrollPane.setPreferredSize(unassignDimension);
		taskTreeScrollPane.getVerticalScrollBar().setUnitIncrement(16);
		taskTree.setCellRenderer(new TaskTreeRenderer((int) unassignDimension.getWidth()));

		// Unassigned Task Tree POP UP menu
		JPopupMenu unassignPopup = new JPopupMenu();
		JMenuItem assignItem = new JMenuItem("Assign");
		unassignPopup.add(assignItem);
		unassignPopup.setPreferredSize(new Dimension(240, 25));

		// Unassigned task tree POP UP Action Listeners
		assignItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				TaskModel task = (TaskModel) selectedNode.getUserObject();
				AssignTaskDialog ev = new AssignTaskDialog(PersonDialog.this, selectedNode.getParent().toString(),
						task);

				AssignTaskEvent eventResponse = ev.getDialogResponse();
				if (eventResponse != null) {
					AssignedTasksModel lastAssignedTask = new AssignedTasksModel(eventResponse.getAssignedTaskID(),
							personID, eventResponse.getTask().getTaskID(), selectedNode.getParent().toString(),
							selectedNode.toString(), eventResponse.getDaysOfWeek(), eventResponse.getWeeksOfMonth());
					lastAssignedTask.setElementStatus(ListStatus.LIST_ELEMENT_NEW);
					assignedTasksList.add(lastAssignedTask);

					// Add to assigned-by-program list
					addNodeToAssignedTaskByProgList(eventResponse.getProgramName(), lastAssignedTask);

					// Remove node from task tree, add to assigned task tree
					trees.removeNodeFromTree(taskTree, eventResponse.getProgramName(),
							eventResponse.getTask().getTaskName());
					trees.addNodeToTree(assignedTasksTree, eventResponse.getProgramName(), eventResponse);
				}
				taskTree.clearSelection();
				task.setIsFocus(false);
			}
		});

		// Unassigned Task Tree mouse listener
		taskTree.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				int row = taskTree.getClosestRowForLocation(e.getX(), e.getY());
				taskTree.setSelectionRow(row);

				if (e.getButton() == MouseEvent.BUTTON3) {
					selectedNode = (DefaultMutableTreeNode) taskTree.getPathForRow(row).getLastPathComponent();
					if (selectedNode != null) {
						if (selectedNode.getLevel() == 2) {
							((TaskModel) (selectedNode.getUserObject())).setIsFocus(true);
							unassignPopup.show(e.getComponent(), e.getX(), e.getY());
						}
					}
				}
			}
		});
	}

	private void addNodeToAssignedTaskByProgList(String programName, AssignedTasksModel assignedTask) {
		for (int i = 0; i < programList.size(); i++) {
			ProgramModel prog = programList.get(i);
			if (prog.getProgramName().equals(programName)) {
				assignedTaskListByProgram.get(i).add(assignedTask);
				return;
			}
		}
	}

	private void deleteNodeInAssignedTaskByProgList(String taskName) {
		for (int i = 0; i < assignedTaskListByProgram.size(); i++) {
			ArrayList<AssignedTasksModel> tList = assignedTaskListByProgram.get(i);
			for (int j = 0; j < tList.size(); j++) {
				if (tList.get(j).getTaskName().equals(taskName)) {
					tList.remove(j);
					return;
				}
			}
		}
	}

	private void updateNodeInAssignedTaskList(AssignedTasksModel newTaskModel) {
		for (int i = 0; i < assignedTasksList.size(); i++) {
			AssignedTasksModel t = assignedTasksList.get(i);
			if (t.getTaskName().equals(newTaskModel.getTaskName())) {
				ListStatus status = t.getElementStatus();
				if (status == ListStatus.LIST_ELEMENT_ASSIGNED)
					status = ListStatus.LIST_ELEMENT_UPDATE;
				newTaskModel.setElementStatus(status);

				assignedTasksList.set(i, newTaskModel);
			}
		}
	}

	private void deleteNodeInAssignedTaskList(String taskName) {
		for (int i = 0; i < assignedTasksList.size(); i++) {
			AssignedTasksModel t = assignedTasksList.get(i);
			if (t.getTaskName().equals(taskName)) {
				if (t.getElementStatus() == ListStatus.LIST_ELEMENT_NEW)
					// Deleting a node that has just been added, so remove
					assignedTasksList.remove(t);
				else
					// Mark element for deletion
					t.setElementStatus(ListStatus.LIST_ELEMENT_DELETE);
			}
		}
	}

	private boolean checkConflictsForSingleInstanceTask(SingleInstanceTaskModel newSingleTask) {
		// Returns TRUE if there is a date/time conflict for this person
		Calendar thisDay = newSingleTask.getTaskDate();
		Date calDay = Utilities.getDateFromCalendar(thisDay);
		int calDayIdx = thisDay.get(Calendar.DAY_OF_WEEK) - 1;
		int calWeekIdx = thisDay.get(Calendar.DAY_OF_WEEK_IN_MONTH) - 1;

		String person = personName.getText().trim();
		if (person.equals(""))
			person = "Person";

		if (!isPersonAvailable(person, thisDay, calDay))
			return true;

		if (checkAssignedTaskConflicts(assignedTasksList, person, newSingleTask.getTaskName(), thisDay, calDay,
				calDayIdx, calWeekIdx))
			return true;

		if (checkSingleInstanceTaskConflicts(person, singleInstanceTaskList, calDay, calDayIdx, calWeekIdx))
			return true;

		return false;
	}

	private boolean isPersonAvailable(String person, Calendar thisDay, Date today) {
		for (int i = 0; i < datesUnavailableList.size(); i++) {
			DateRangeModel datesUnavail = datesUnavailableList.get(i);
			if (Utilities.isDateWithinDateRange(today, datesUnavail.getStartDate(), datesUnavail.getEndDate(),
					"Unable to parse " + person + "'s Unavailable start/end Dates.")) {
				JOptionPane.showMessageDialog(PersonDialog.this,
						person + " is not available on " + Utilities.getDisplayDate(thisDay), "Updating Person Info",
						JOptionPane.INFORMATION_MESSAGE);
				return false;
			}
		}
		return true;
	}

	private boolean checkAssignedTaskConflicts(ArrayList<AssignedTasksModel> taskList, String person, String taskName,
			Calendar thisDay, Date calDay, int calDayIdx, int calWeekIdx) {

		// Check if task is in person's assigned task list for today
		for (int i = 0; i < taskList.size(); i++) {
			AssignedTasksModel assignedTask = taskList.get(i);
			if (assignedTask.getTaskName().equals(taskName) && assignedTask.getDaysOfWeek()[calDayIdx]
					&& assignedTask.getWeeksOfMonth()[calWeekIdx]) {
				JOptionPane.showMessageDialog(PersonDialog.this,
						person + " is already assigned to " + assignedTask.getTaskName() + " on "
								+ Utilities.getDisplayDate(thisDay),
						"Updating Person Info", JOptionPane.INFORMATION_MESSAGE);
				return true;
			}
		}
		return false;
	}

	private boolean checkSingleInstanceTaskConflicts(String person, ArrayList<SingleInstanceTaskModel> taskList,
			Date thisDay, int dayOfWeekIdx, int dayOfWeekInMonthIdx) {

		for (int i = 0; i < taskList.size(); i++) {
			SingleInstanceTaskModel singleInstanceTask = taskList.get(i);
			if (Utilities.checkForDateAndTimeMatch(thisDay, dayOfWeekIdx, dayOfWeekInMonthIdx,
					singleInstanceTask.getTaskDate())) {
				if (singleInstanceTask.getTaskName().equals(""))
					// Date/time conflict with floater
					JOptionPane.showMessageDialog(PersonDialog.this,
							person + " is already assigned as a Floater on "
									+ Utilities.getDisplayDate(singleInstanceTask.getTaskDate()) + " at "
									+ Utilities.formatTime(singleInstanceTask.getTaskDate()),
							"Updating Person Info", JOptionPane.INFORMATION_MESSAGE);
				else
					// Date/time conflict with substitute
					JOptionPane.showMessageDialog(PersonDialog.this,
							person + " is already assigned to " + singleInstanceTask.getTaskName() + " on "
									+ Utilities.getDisplayDate(singleInstanceTask.getTaskDate()) + " at "
									+ Utilities.formatTime(singleInstanceTask.getTaskDate()),
							"Updating Person Info", JOptionPane.INFORMATION_MESSAGE);

				return true; // Conflict
			}
		}
		return false;
	}
}
