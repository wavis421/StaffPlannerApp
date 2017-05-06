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
import java.util.Calendar;
import java.util.Date;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.border.Border;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

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

	// Lists
	private AssignTaskCreateTree trees;
	private ArrayList<AssignedTasksModel> assignedTasksList;
	private ArrayList<SingleInstanceTaskModel> singleInstanceTaskList;
	private ArrayList<DateRangeModel> datesUnavailableList;
	private JList<TaskModel> allTasks;

	// Static variables
	private static boolean okToSave;
	private static ArrayList<SingleInstanceTaskModel> newSingleInstanceTasks;
	private static ArrayList<DateRangeModel> newDatesUnavailable;

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

	public PersonDialog(JFrame parent, String currentProgram, JList<TaskModel> allTasks,
			ArrayList<ProgramModel> programList, ArrayList<JList<TaskModel>> taskListByProgram,
			ArrayList<ArrayList<AssignedTasksModel>> assignedTaskListByProgram) {
		// super(parent, "Add person...", true);
		super(parent, "Add person...");
		setLocation(new Point(100, 100));
		setModalityType(Dialog.DEFAULT_MODALITY_TYPE.APPLICATION_MODAL);

		// TODO: find better names!!
		trees = new AssignTaskCreateTree(currentProgram, programList,
				taskListByProgram, assignedTaskListByProgram);
		createTrees(trees.getAssignedTaskTree(), trees.getTaskTree());

		this.allTasks = allTasks;
		this.leaderButton.setSelected(true);
		this.assignedTasksList = new ArrayList<AssignedTasksModel>();

		if (newSingleInstanceTasks != null)
			newSingleInstanceTasks.clear();
		newSingleInstanceTasks = new ArrayList<SingleInstanceTaskModel>();

		if (newDatesUnavailable != null)
			newDatesUnavailable.clear();
		newDatesUnavailable = new ArrayList<DateRangeModel>();

		createUnavailDateCombo();

		okToSave = false;
		createSingleInstanceTaskCombo();

		setupPersonDialog();
		setVisible(true);
	}

	// Constructor for updating existing person, PersonModel contains values
	public PersonDialog(JFrame parent, String currentProgram, JList<TaskModel> allTasks, PersonModel person,
			ArrayList<ProgramModel> programList, ArrayList<JList<TaskModel>> taskListByProgram,
			ArrayList<ArrayList<AssignedTasksModel>> assignedTaskListByProgram) {
		super(parent, "Edit person...", true);
		setLocation(new Point(100, 100));
		setModalityType(Dialog.DEFAULT_MODALITY_TYPE.APPLICATION_MODAL);

		// TODO: find better names!!
		trees = new AssignTaskCreateTree(currentProgram, programList,
				taskListByProgram, assignedTaskListByProgram);
		createTrees(trees.getAssignedTaskTree(), trees.getTaskTree());

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

		if (okToSave) {
			// These lists were processed and can be cleared
			newSingleInstanceTasks.clear();
			newSingleInstanceTasks = new ArrayList<SingleInstanceTaskModel>();
			newDatesUnavailable.clear();
			newDatesUnavailable = new ArrayList<DateRangeModel>();

			okToSave = false;

		} else {
			// Create lists first time after startup
			if (newSingleInstanceTasks == null)
				newSingleInstanceTasks = new ArrayList<SingleInstanceTaskModel>();
			if (newDatesUnavailable == null)
				newDatesUnavailable = new ArrayList<DateRangeModel>();
		}

		createUnavailDateCombo();
		createSingleInstanceTaskCombo();

		this.allTasks = allTasks;

		setupPersonDialog();
		setVisible(true);
	}

	public PersonEvent getDialogResponse() {
		return dialogResponse;
	}

	public boolean getOkToSaveStatus() {
		return okToSave;
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
					newDatesUnavailable.add(dateResponse.getSqlDateRange());
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
							dateResponse.getTask().getTaskID(), dateResponse.getTask().getTaskName(),
							dateResponse.getStartDate(), dateResponse.getTask().getColor());

					// Check for date/time conflicts
					if (!checkConflictsForSingleInstanceTask(singleTask)) {
						// No conflicts; ready to add task
						newSingleInstanceTasks.add(singleTask);

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
							processNotesArea(), assignedTasksList, null, newSingleInstanceTasks, newDatesUnavailable);
					okToSave = true;
					dialogResponse = ev;
					setVisible(false);
					dispose();
				}
			}
		});
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				okToSave = true;
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

		if (singleInstanceTaskList != null) {
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
		}

		for (int i = 0; i < newSingleInstanceTasks.size(); i++) {
			SingleInstanceTaskModel task = newSingleInstanceTasks.get(i);
			taskModel.addElement(task.getTaskName() + " on " + Utilities.getDisplayDate(task.getTaskDate()));
		}

		singleInstanceTaskCombo = new JComboBox<String>(taskModel);
		singleInstanceTaskCombo.setEditable(false);
		singleInstanceTaskCombo.setBorder(BorderFactory.createEtchedBorder());
		singleInstanceTaskCombo.setPreferredSize(new Dimension(COMBO_BOX_WIDTH, COMBO_BOX_HEIGHT));
	}

	private void createUnavailDateCombo() {
		DefaultComboBoxModel<String> dateModel = new DefaultComboBoxModel<String>();

		if (datesUnavailableList != null) {
			for (int i = 0; i < datesUnavailableList.size(); i++) {
				DateRangeModel dateUnavail = datesUnavailableList.get(i);
				addDateUnavail(dateUnavail, dateModel);
			}
		}

		for (int i = 0; i < newDatesUnavailable.size(); i++) {
			DateRangeModel dateUnavail = newDatesUnavailable.get(i);
			addDateUnavail(dateUnavail, dateModel);
		}

		dateUnavailCombo = new JComboBox<String>(dateModel);
		dateUnavailCombo.setEditable(false);
		dateUnavailCombo.setBorder(BorderFactory.createEtchedBorder());
		dateUnavailCombo.setPreferredSize(new Dimension(COMBO_BOX_WIDTH, COMBO_BOX_HEIGHT));
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
		/* Create assigned task tree */
		assignedTasksScrollPane = new JScrollPane(assignedTasksTree);
		assignedTasksScrollPane.setPreferredSize(new Dimension((int) notesArea.getPreferredSize().getWidth() + 4,
				(int) notesArea.getPreferredSize().getHeight() * 3));
		assignedTasksTree.setCellRenderer(new AssignTaskTreeRenderer());

		/* Add tree listener */
		assignedTasksTree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent evt) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) assignedTasksTree.getLastSelectedPathComponent();
				if (node == null)
					return;

				/* retrieve the node that was selected */
				Object nodeInfo = node.getUserObject();

				if (node.getLevel() == 2) {
					setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
					AssignTaskDialog event = new AssignTaskDialog(PersonDialog.this, (AssignTaskEvent) nodeInfo);

					AssignTaskEvent eventResponse = event.getDialogResponse();
					if (eventResponse != null) {
						// Update assigned task with new node info
						AssignedTasksModel lastAssignedTask = new AssignedTasksModel(eventResponse.getAssignedTaskID(),
								personID, eventResponse.getTask().getTaskID(), eventResponse.getProgramName(),
								eventResponse.getTask().getTaskName(), eventResponse.getDaysOfWeek(),
								eventResponse.getWeeksOfMonth());
						ListStatus status = removeNodeFromAssignedTaskList(lastAssignedTask.getTaskName());
						if (status == ListStatus.LIST_ELEMENT_ASSIGNED)
							status = ListStatus.LIST_ELEMENT_UPDATE;
						
						// TODO: process lastAssignedTask (expand tree node)
						lastAssignedTask.setElementStatus(status);
						assignedTasksList.add(lastAssignedTask);
					}
					assignedTasksTree.clearSelection();
				}
			}
		});

		/* Create unassigned task tree */
		taskTreeScrollPane = new JScrollPane(taskTree);
		taskTreeScrollPane.setPreferredSize(new Dimension((int) notesArea.getPreferredSize().getWidth(),
				(int) notesArea.getPreferredSize().getHeight() * 3));
		taskTree.setCellRenderer(new TaskTreeRenderer());

		/* Add tree listener */
		taskTree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent evt) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) taskTree.getLastSelectedPathComponent();
				if (node == null)
					return;

				/* retrieve the node that was selected */
				Object nodeInfo = node.getUserObject();

				if (node.getLevel() == 2) {
					TreePath parentPath = taskTree.getSelectionPath();
					DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) (parentPath.getLastPathComponent());

					// Can't keep this dialog MODAL if the AssignTaskDialog is
					// to be MODAL
					setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);

					AssignTaskDialog event = new AssignTaskDialog(PersonDialog.this, node.getParent().toString(),
							(TaskModel) nodeInfo);

					AssignTaskEvent eventResponse = event.getDialogResponse();
					if (eventResponse != null) {
						AssignedTasksModel lastAssignedTask = new AssignedTasksModel(eventResponse.getAssignedTaskID(),
								personID, eventResponse.getTask().getTaskID(), node.getParent().toString(),
								childNode.toString(), eventResponse.getDaysOfWeek(), eventResponse.getWeeksOfMonth());
						lastAssignedTask.setElementStatus(ListStatus.LIST_ELEMENT_NEW);
						assignedTasksList.add(lastAssignedTask);

						// Remove node from task tree, add it to assigned task tree
						trees.removeNodeFromTree(taskTree, eventResponse.getProgramName(), eventResponse.getTask().getTaskName());
						trees.addNodeToTree(assignedTasksTree, eventResponse.getProgramName(), eventResponse);
					}
					taskTree.clearSelection();
				}
			}
		});
	}

	private ListStatus removeNodeFromAssignedTaskList(String taskName) {
		for (int i = 0; i < assignedTasksList.size(); i++) {
			AssignedTasksModel t = assignedTasksList.get(i);
			if (t.getTaskName().equals(taskName)) {
				int idx = assignedTasksList.indexOf(t);
				assignedTasksList.remove(idx);
				return t.getElementStatus();
			}
		}
		return ListStatus.LIST_ELEMENT_NEW;
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

		if (singleInstanceTaskList != null) {
			if (checkSingleInstanceTaskConflicts(person, singleInstanceTaskList, calDay, calDayIdx, calWeekIdx))
				return true;
		}

		if (checkSingleInstanceTaskConflicts(person, newSingleInstanceTasks, calDay, calDayIdx, calWeekIdx))
			return true;

		return false;
	}

	private boolean isPersonAvailable(String person, Calendar thisDay, Date today) {
		if (datesUnavailableList != null) {
			for (int i = 0; i < datesUnavailableList.size(); i++) {
				DateRangeModel datesUnavail = datesUnavailableList.get(i);
				if (Utilities.isDateWithinDateRange(today, datesUnavail.getStartDate(), datesUnavail.getEndDate(),
						"Unable to parse " + person + "'s Unavailable start/end Dates.")) {
					JOptionPane.showMessageDialog(PersonDialog.this,
							person + " is not available on " + Utilities.getDisplayDate(thisDay),
							"Updating Person Info", JOptionPane.INFORMATION_MESSAGE);
					return false;
				}
			}
		}

		for (int i = 0; i < newDatesUnavailable.size(); i++) {
			DateRangeModel datesUnavail = newDatesUnavailable.get(i);

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
