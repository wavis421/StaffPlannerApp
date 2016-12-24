package gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
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
import javax.swing.tree.TreeSelectionModel;

import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

import model.AssignedTasksModel;
import model.DateRangeModel;
import model.PersonModel;
import model.SingleInstanceTaskModel;
import model.TaskModel;
import utilities.Utilities;

public class PersonDialog extends JDialog {
	// Constants
	private static final int TEXT_FIELD_SIZE = 30;
	private static final int TASK_COMBO_WIDTH = 334;
	private static final int TASK_COMBO_HEIGHT = 30;
	
	private JButton okButton = new JButton("OK");
	private JButton cancelButton = new JButton("Cancel");

	// Private instance variables
	private JTextField personName = new JTextField(TEXT_FIELD_SIZE);
	private JTextField phone = new JTextField(TEXT_FIELD_SIZE);
	private JTextField email = new JTextField(TEXT_FIELD_SIZE);
	private JRadioButton leaderButton = new JRadioButton("Leader");
	private JRadioButton volunteerButton = new JRadioButton("Volunteer");
	private ButtonGroup staffGroup = new ButtonGroup();
	private JPanel staffPanel = new JPanel();
	private JPanel datePanel = new JPanel();
	private JDatePickerImpl startDayPicker, endDayPicker;
	private JTextArea notesArea = new JTextArea(3, TEXT_FIELD_SIZE);
	private LinkedList<AssignedTasksModel> assignedTaskChanges;
	private DateRangeModel datesUnavailable;
	private JComboBox<String> singleInstanceTaskCombo;
	private JScrollPane assignedTasksScrollPane;
	private JScrollPane taskTreeScrollPane;

	// Labels
	private JLabel nameLabel = new JLabel("Person's name: ");
	private JLabel phoneLabel = new JLabel("Phone #: ");
	private JLabel emailLabel = new JLabel("Email: ");
	private JLabel staffLabel = new JLabel("Leader or volunteer: ");
	private JLabel notesLabel = new JLabel("Notes: ");
	private JLabel datesLabel = new JLabel("Dates Unavailable: ");
	private JLabel singleTaskLabel = new JLabel("Extra Dates: ");

	// Dialog panels
	private JPanel controlsPanel;
	private JPanel buttonsPanel;
	private PersonEvent dialogResponse;
	private boolean okToSave = false;

	public PersonDialog(JFrame parent, JTree assignedTasksTree, JTree taskTree) {
		// super(parent, "Add person...", true);
		super(parent, "Add person...");
		setModalityType(Dialog.DEFAULT_MODALITY_TYPE.APPLICATION_MODAL);
		createTrees(assignedTasksTree, taskTree);
		this.leaderButton.setSelected(true);
		this.assignedTaskChanges = new LinkedList<AssignedTasksModel>();
		this.datesUnavailable = new DateRangeModel("", "");

		createSingleInstanceTaskCombo(null);
		
		setupPersonDialog();
	}

	// Constructor for updating existing person, PersonModel contains values
	public PersonDialog(JFrame parent, PersonModel person, LinkedList<AssignedTasksModel> assignedTaskChanges,
			JTree assignedTasksTree, JTree taskTree) {
		super(parent, "Edit person...", true);
		setModalityType(Dialog.DEFAULT_MODALITY_TYPE.APPLICATION_MODAL);
		createTrees(assignedTasksTree, taskTree);

		this.personName.setText(person.getName());
		this.phone.setText(person.getPhone());
		this.email.setText(person.getEmail());
		this.notesArea.setText(person.getNotes());
		if (person.isLeader())
			this.leaderButton.setSelected(true);
		else
			this.volunteerButton.setSelected(true);
		this.assignedTaskChanges = assignedTaskChanges;
		this.datesUnavailable = person.getDatesUnavailable();

		createSingleInstanceTaskCombo(person.getSingleInstanceTasks());
		
		setupPersonDialog();
	}

	public PersonEvent getDialogResponse() {
		return dialogResponse;
	}

	public boolean getOkToSaveStatus() {
		return okToSave;
	}

	private void setupPersonDialog() {
		createStaffSelector();
		createDateSelectors();
		singleInstanceTaskCombo.setEditable(false);

		// Force the text area not to expand when user types more than 3 lines!!
		notesArea.setBorder(BorderFactory.createEtchedBorder());
		notesArea.setLineWrap(true);
		notesArea.setRows(3);
		notesArea.setMargin(new Insets(20, 20, 20, 20));
		notesArea.setAutoscrolls(false);
		notesArea.setPreferredSize(notesArea.getPreferredSize());

		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					// Make sure that person name has been entered
					if (personName.getText().equals("")) {
						JOptionPane.showMessageDialog(okButton, "Person's name field is required");
					} else {
						datesUnavailable.setStartDate(startDayPicker.getJFormattedTextField().getText());
						datesUnavailable.setEndDate(endDayPicker.getJFormattedTextField().getText());
						PersonEvent ev = new PersonEvent(this, personName.getText().trim(), phone.getText().trim(),
								email.getText().trim(), leaderButton.isSelected() ? true : false, processNotesArea(),
								assignedTaskChanges, null, datesUnavailable);
						okToSave = true;
						dialogResponse = ev;
						setVisible(false);
						dispose();
					}

				} catch (IllegalArgumentException ev) {
					JOptionPane.showMessageDialog(okButton, "TBD exception message");
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
		setVisible(true);
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
		addRowToControlPanel(gc, datesLabel, datePanel, gridY++);
		addRowToControlPanel(gc, singleTaskLabel, singleInstanceTaskCombo, gridY++);
		addRowToControlPanel(gc, notesLabel, notesArea, gridY++);
		addRowToControlPanel(gc, taskTreeScrollPane, assignedTasksScrollPane, gridY++);

		// Buttons row
		gc.gridy++;
		gc.gridx = 0;
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
		volunteerButton.setActionCommand("volunteer");

		staffPanel.add(leaderButton);
		staffPanel.add(volunteerButton);

		staffGroup.add(leaderButton);
		staffGroup.add(volunteerButton);
	}

	private void createSingleInstanceTaskCombo(LinkedList<SingleInstanceTaskModel> taskList) {
		DefaultComboBoxModel<String> taskModel = new DefaultComboBoxModel<String>();

		if (taskList != null) {
			for (SingleInstanceTaskModel task : taskList) {
				Calendar date = task.getTaskDate();
				String taskName = task.getTaskName();
				if (taskName.equals("")) {
					taskName = "Floater";
					taskModel.addElement(taskName + " on " + (date.get(Calendar.MONTH) + 1) + "/"
							+ date.get(Calendar.DAY_OF_MONTH) + "/" + date.get(Calendar.YEAR) + " at "
							+ Utilities.formatTime(date));
				} else {
					taskModel.addElement(taskName + " on " + (date.get(Calendar.MONTH) + 1) + "/"
							+ date.get(Calendar.DAY_OF_MONTH) + "/" + date.get(Calendar.YEAR));
				}
			}
		}

		singleInstanceTaskCombo = new JComboBox<String>(taskModel);
		if (taskModel.getSize() > 0)
			singleInstanceTaskCombo.setSelectedIndex(0);
		singleInstanceTaskCombo.setBorder(BorderFactory.createEtchedBorder());
		singleInstanceTaskCombo.setPreferredSize(new Dimension(TASK_COMBO_WIDTH, TASK_COMBO_HEIGHT));
	}

	private void createDateSelectors() {
		startDayPicker = createDatePicker(datesUnavailable.getStartDate(), "start");
		endDayPicker = createDatePicker(datesUnavailable.getEndDate(), "end");

		datePanel.add(startDayPicker);
		datePanel.add(new JLabel(" to "));
		datePanel.add(endDayPicker);
	}

	private JDatePickerImpl createDatePicker(String lastDate, String name) {
		UtilDateModel dateModel = new UtilDateModel();
		Properties prop = new Properties();
		JDatePanelImpl datePanel;
		SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy");

		prop.put("text.today", "today");
		prop.put("text.month", "month");
		prop.put("text.year", "year");

		datePanel = new JDatePanelImpl(dateModel, prop);
		JDatePickerImpl datePicker = new JDatePickerImpl(datePanel, new DateLabelFormatter());

		if (lastDate != null && !lastDate.equals("")) {
			try {
				// Initialize date picker using database date string
				Date date = dateFormatter.parse(lastDate);
				Calendar cal = Calendar.getInstance();
				cal.setTime(date);
				datePicker.getModel().setDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
						cal.get(Calendar.DAY_OF_MONTH));

				datePicker.getModel().setSelected(true);

			} catch (ParseException ex) {
				JOptionPane.showMessageDialog(this, "Invalid date, expecting MM/dd/yyyy", "Parsing Exception",
						JOptionPane.WARNING_MESSAGE);
			}
		}

		// Add action listener
		datePicker.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String startText = startDayPicker.getJFormattedTextField().getText();
				String endText = endDayPicker.getJFormattedTextField().getText();

				// If end date is NULL, set it to start day
				if (name.equals("start") && !startText.equals("")) {
					if (endText.equals("")) {
						endDayPicker = setDate(startText, endDayPicker);
						endDayPicker.getJFormattedTextField().setText(startText);
						endText = startText;
					}
					startDayPicker.getModel().setSelected(true);
				}
				// If start date is NULL, set it to end day
				if (name.equals("end") && !endText.equals("")) {
					if (startText.equals("")) {
						startDayPicker = setDate(endText, startDayPicker);
						startDayPicker.getJFormattedTextField().setText(endText);
						startText = endText;
					}
					endDayPicker.getModel().setSelected(true);
				}

				if (!startText.equals("") && !endText.equals("")) {
					// If end date is before start date, set to start date
					try {
						Date startDate = dateFormatter.parse(startText);
						if (startDate.compareTo(dateFormatter.parse(endText)) > 0) {
							endDayPicker.getJFormattedTextField().setText(startText);
						}

					} catch (ParseException ex) {
						JOptionPane.showMessageDialog(null, "Unable to parse date.", "Parse Exception",
								JOptionPane.WARNING_MESSAGE);
						// e1.printStackTrace();
					}
				}
			}
		});
		datePicker.setPreferredSize(new Dimension(150, 26));
		datePicker.setName(name);
		return datePicker;
	}

	private JDatePickerImpl setDate(String dateText, JDatePickerImpl datePicker) {
		try {
			SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy");
			Date date = dateFormatter.parse(dateText);
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			datePicker.getModel().setDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
					cal.get(Calendar.DAY_OF_MONTH));

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return datePicker;
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
		assignedTasksScrollPane = new JScrollPane(assignedTasksTree);
		assignedTasksScrollPane.setPreferredSize(new Dimension((int) notesArea.getPreferredSize().getWidth() + 4,
				(int) notesArea.getPreferredSize().getHeight() * 3));
		assignedTasksTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

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
						AssignedTasksModel lastAssignedTask = new AssignedTasksModel(eventResponse.getProgramName(),
								eventResponse.getTask().getTaskName(), eventResponse.getDaysOfWeek(),
								eventResponse.getWeeksOfMonth());
						removeNodeFromAssignedTaskList(lastAssignedTask.getTaskName());
						assignedTaskChanges.add(lastAssignedTask);

						datesUnavailable.setStartDate(startDayPicker.getJFormattedTextField().getText());
						datesUnavailable.setEndDate(endDayPicker.getJFormattedTextField().getText());
						PersonEvent ev = new PersonEvent(this, personName.getText(), phone.getText(), email.getText(),
								leaderButton.isSelected() ? true : false, processNotesArea(), assignedTaskChanges,
								lastAssignedTask, datesUnavailable);
						dialogResponse = ev;
						setVisible(false);
						dispose();
					}
					assignedTasksTree.clearSelection();
				}
			}
		});

		taskTreeScrollPane = new JScrollPane(taskTree);
		taskTreeScrollPane.setPreferredSize(new Dimension((int) notesArea.getPreferredSize().getWidth(),
				(int) notesArea.getPreferredSize().getHeight() * 3));
		taskTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

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
						AssignedTasksModel lastAssignedTask = new AssignedTasksModel(node.getParent().toString(),
								childNode.toString(), eventResponse.getDaysOfWeek(), eventResponse.getWeeksOfMonth());
						assignedTaskChanges.add(lastAssignedTask);

						datesUnavailable.setStartDate(startDayPicker.getJFormattedTextField().getText());
						datesUnavailable.setEndDate(endDayPicker.getJFormattedTextField().getText());
						PersonEvent ev = new PersonEvent(this, personName.getText(), phone.getText(), email.getText(),
								leaderButton.isSelected() ? true : false, processNotesArea(), assignedTaskChanges,
								lastAssignedTask, datesUnavailable);
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
		for (AssignedTasksModel t : assignedTaskChanges) {
			if (t.getTaskName().equals(taskName)) {
				int idx = assignedTaskChanges.indexOf(t);
				assignedTaskChanges.remove(idx);
				return;
			}
		}
	}
}
