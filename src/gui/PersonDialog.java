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
import java.util.LinkedList;

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
import model.PersonModel;
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
	private JTextField personName = new JTextField(TEXT_FIELD_SIZE);
	private JTextField phone = new JTextField(TEXT_FIELD_SIZE);
	private JTextField email = new JTextField(TEXT_FIELD_SIZE);
	private JRadioButton leaderButton = new JRadioButton("Leader");
	private JRadioButton assistantButton = new JRadioButton("Assistant");
	private ButtonGroup staffGroup = new ButtonGroup();
	private JPanel staffPanel = new JPanel();
	private JComboBox<String> dateUnavailCombo;
	private JTextArea notesArea = new JTextArea(3, TEXT_FIELD_SIZE);
	private LinkedList<AssignedTasksModel> assignedTaskChanges;
	private LinkedList<DateRangeModel> datesUnavailable;
	private JComboBox<String> singleInstanceTaskCombo;
	private JScrollPane assignedTasksScrollPane;
	private JScrollPane taskTreeScrollPane;
	private JList<TaskModel> allTasks;

	private static boolean okToSave;
	private static LinkedList<SingleInstanceTaskModel> newSingleInstanceTasks;

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
	private PersonEvent dialogResponse;

	public PersonDialog(JFrame parent, JList<TaskModel> allTasks, JTree assignedTasksTree, JTree taskTree) {
		// super(parent, "Add person...", true);
		super(parent, "Add person...");
		setLocation(new Point(100, 100));
		setModalityType(Dialog.DEFAULT_MODALITY_TYPE.APPLICATION_MODAL);

		createTrees(assignedTasksTree, taskTree);

		this.allTasks = allTasks;
		this.leaderButton.setSelected(true);
		this.assignedTaskChanges = new LinkedList<AssignedTasksModel>();

		if (newSingleInstanceTasks != null)
			newSingleInstanceTasks.clear();
		newSingleInstanceTasks = new LinkedList<SingleInstanceTaskModel>();

		this.datesUnavailable = new LinkedList<DateRangeModel>();

		okToSave = false;
		createSingleInstanceTaskCombo(null);

		setupPersonDialog();
		setVisible(true);
	}

	// Constructor for updating existing person, PersonModel contains values
	public PersonDialog(JFrame parent, JList<TaskModel> allTasks, PersonModel person,
			LinkedList<AssignedTasksModel> assignedTaskChanges, JTree assignedTasksTree, JTree taskTree) {
		super(parent, "Edit person...", true);
		setLocation(new Point(100, 100));
		setModalityType(Dialog.DEFAULT_MODALITY_TYPE.APPLICATION_MODAL);
		createTrees(assignedTasksTree, taskTree);

		this.personName.setText(person.getName());
		this.phone.setText(person.getPhone());
		this.email.setText(person.getEmail());
		this.notesArea.setText(person.getNotes());

		if (person.isLeader())
			this.leaderButton.setSelected(true);
		else
			this.assistantButton.setSelected(true);
		this.assignedTaskChanges = assignedTaskChanges;

		if (okToSave) {
			// This list was processed and can be cleared
			newSingleInstanceTasks.clear();
			newSingleInstanceTasks = new LinkedList<SingleInstanceTaskModel>();
			okToSave = false;
		} else if (newSingleInstanceTasks == null) {
			// Create list first time after startup
			newSingleInstanceTasks = new LinkedList<SingleInstanceTaskModel>();
		}

		this.datesUnavailable = (LinkedList<DateRangeModel>) person.getDatesUnavailable().clone();
		this.allTasks = allTasks;

		createSingleInstanceTaskCombo(person.getSingleInstanceTasks());

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
		createUnavailDateCombo();

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
				DateRangeEvent dialogResponse = ev.getDialogResponse();
				if (dialogResponse != null) {
					// Date range valid. Add to Linked List and Combo Box.
					datesUnavailable.add(dialogResponse.getDateRange());
					DefaultComboBoxModel<String> dateModel = (DefaultComboBoxModel<String>) dateUnavailCombo.getModel();
					if (dialogResponse.getDateRange().getStartDate().equals(dialogResponse.getDateRange().getEndDate()))
						// Add single date to list
						dateModel.addElement(dialogResponse.getDateRange().getStartDate());
					else
						// Add date range to list
						dateModel.addElement(dialogResponse.toString());
				}
			}
		});
		extraDatesButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DateRangeDialog ev = new DateRangeDialog(PersonDialog.this, "Select extra dates...", "Select Date: ",
						allTasks);
				DateRangeEvent dialogResponse = ev.getDialogResponse();
				if (dialogResponse != null && dialogResponse.getTask() != null) {
					// Date and task valid. Add single instance task.
					Utilities.addTimeToCalendar(dialogResponse.getStartDate(), dialogResponse.getTask().getTime());
					newSingleInstanceTasks.add(new SingleInstanceTaskModel(0, 0, dialogResponse.getTask().getTaskID(),
							dialogResponse.getTask().getTaskName(),
							dialogResponse.getStartDate(), dialogResponse.getTask().getColor()));
					DefaultComboBoxModel<String> extraDateModel = (DefaultComboBoxModel<String>) singleInstanceTaskCombo
							.getModel();
					extraDateModel.addElement(dialogResponse.getTask().getTaskName() + " on "
							+ Utilities.getDisplayDate(dialogResponse.getStartDate()));
				}
			}
		});
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Make sure that person name has been entered
				if (personName.getText().trim().equals("")) {
					JOptionPane.showMessageDialog(okButton, "Person's name field is required");
				} else {
					PersonEvent ev = new PersonEvent(this, personName.getText().trim(), phone.getText().trim(),
							email.getText().trim(), leaderButton.isSelected() ? true : false, processNotesArea(),
							assignedTaskChanges, null, newSingleInstanceTasks, datesUnavailable);
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

	private void createSingleInstanceTaskCombo(LinkedList<SingleInstanceTaskModel> taskList) {
		DefaultComboBoxModel<String> taskModel = new DefaultComboBoxModel<String>();

		if (taskList != null) {
			for (int i = 0; i < taskList.size(); i++) {
				SingleInstanceTaskModel task = taskList.get(i);
				Calendar date = task.getTaskDate();
				String taskName = task.getTaskName();
				if (taskName.equals("")) {
					taskName = "Floater";
					taskModel.addElement(
							taskName + " on " + Utilities.getDisplayDate(date) + " at " + Utilities.formatTime(date));
				} else {
					taskModel.addElement(taskName + " on " + Utilities.getDisplayDate(date));
				}
			}
		}
		if (newSingleInstanceTasks != null) {
			for (int i = 0; i < newSingleInstanceTasks.size(); i++) {
				SingleInstanceTaskModel task = newSingleInstanceTasks.get(i);
				taskModel.addElement(task.getTaskName() + " on " + Utilities.getDisplayDate(task.getTaskDate()));
			}
		}

		singleInstanceTaskCombo = new JComboBox<String>(taskModel);
		singleInstanceTaskCombo.setEditable(false);
		singleInstanceTaskCombo.setBorder(BorderFactory.createEtchedBorder());
		singleInstanceTaskCombo.setPreferredSize(new Dimension(COMBO_BOX_WIDTH, COMBO_BOX_HEIGHT));
	}

	private void createUnavailDateCombo() {
		DefaultComboBoxModel<String> dateModel = new DefaultComboBoxModel<String>();

		for (int i = 0; i < datesUnavailable.size(); i++) {
			DateRangeModel dateUnavail = datesUnavailable.get(i);
			if (Utilities.isDateInThePast(dateUnavail.getEndDate(), "Error parsing Unavailable Date(s)"))
				// Date range has passed; remove from list
				datesUnavailable.remove(dateUnavail);
			else if (dateUnavail.getStartDate().equals(dateUnavail.getEndDate()))
				// Add single date to list
				dateModel.addElement(dateUnavail.getStartDate());
			else
				// Add date range to list
				dateModel.addElement(dateUnavail.getStartDate() + "  to  " + dateUnavail.getEndDate());
		}

		dateUnavailCombo = new JComboBox<String>(dateModel);
		dateUnavailCombo.setEditable(false);
		dateUnavailCombo.setBorder(BorderFactory.createEtchedBorder());
		dateUnavailCombo.setPreferredSize(new Dimension(COMBO_BOX_WIDTH, COMBO_BOX_HEIGHT));
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
						AssignedTasksModel lastAssignedTask = new AssignedTasksModel(0, 0, eventResponse.getTask().getTaskID(),
								eventResponse.getProgramName(),
								eventResponse.getTask().getTaskName(), eventResponse.getDaysOfWeek(),
								eventResponse.getWeeksOfMonth());
						removeNodeFromAssignedTaskList(lastAssignedTask.getTaskName());
						assignedTaskChanges.add(lastAssignedTask);

						PersonEvent ev = new PersonEvent(this, personName.getText(), phone.getText(), email.getText(),
								leaderButton.isSelected() ? true : false, processNotesArea(), assignedTaskChanges,
								lastAssignedTask, newSingleInstanceTasks, datesUnavailable);
						dialogResponse = ev;
						setVisible(false);
						dispose();
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
						AssignedTasksModel lastAssignedTask = new AssignedTasksModel(0, 0, eventResponse.getTask().getTaskID(),
								node.getParent().toString(),
								childNode.toString(), eventResponse.getDaysOfWeek(), eventResponse.getWeeksOfMonth());
						assignedTaskChanges.add(lastAssignedTask);

						PersonEvent ev = new PersonEvent(this, personName.getText(), phone.getText(), email.getText(),
								leaderButton.isSelected() ? true : false, processNotesArea(), assignedTaskChanges,
								lastAssignedTask, newSingleInstanceTasks, datesUnavailable);
						dialogResponse = ev;
						setVisible(false);
						dispose();
					}
					taskTree.clearSelection();
				}
			}
		});
	}

	private void removeNodeFromAssignedTaskList(String taskName) {
		for (int i = 0; i < assignedTaskChanges.size(); i++) {
			AssignedTasksModel t = assignedTaskChanges.get(i);
			if (t.getTaskName().equals(taskName)) {
				int idx = assignedTaskChanges.indexOf(t);
				assignedTaskChanges.remove(idx);
				return;
			}
		}
	}
}
