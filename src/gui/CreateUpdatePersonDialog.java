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
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
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

import model.AssignedTasksModel;
import model.PersonModel;
import model.TaskModel;

public class CreateUpdatePersonDialog extends JDialog {
	// Constants
	private static final int TEXT_FIELD_SIZE = 30;
	private JButton okButton = new JButton("OK");
	private JButton cancelButton = new JButton("Cancel");

	// Private instance variables
	private JTextField personName = new JTextField(TEXT_FIELD_SIZE);
	private JTextField phone = new JTextField(TEXT_FIELD_SIZE);
	private JTextField email = new JTextField(TEXT_FIELD_SIZE);
	private JRadioButton staffButton = new JRadioButton("Staff");
	private JRadioButton volunteerButton = new JRadioButton("Volunteer");
	private ButtonGroup staffGroup = new ButtonGroup();
	private JPanel staffPanel = new JPanel();
	private JTextArea notesArea = new JTextArea(3, TEXT_FIELD_SIZE);
	private LinkedList<AssignedTasksModel> assignedTasks;
	private JScrollPane assignedTasksScrollPane;
	private JScrollPane taskTreeScrollPane;

	// Labels
	private JLabel nameLabel = new JLabel("Person's name: ");
	private JLabel phoneLabel = new JLabel("Phone #: ");
	private JLabel emailLabel = new JLabel("Email: ");
	private JLabel staffLabel = new JLabel("Staff or volunteer: ");
	private JLabel notesLabel = new JLabel("Notes: ");

	// Dialog panels
	private JPanel controlsPanel;
	private JPanel buttonsPanel;
	private PersonEvent dialogResponse;
	private boolean okToSave = false;

	public CreateUpdatePersonDialog(JFrame parent, JTree assignedTasksTree, JTree taskTree) {
		// super(parent, "Add person...", true);
		super(parent, "Add person...");
		setModalityType(Dialog.DEFAULT_MODALITY_TYPE.APPLICATION_MODAL);
		createTrees(assignedTasksTree, taskTree);
		this.staffButton.setSelected(true);
		this.assignedTasks = new LinkedList<AssignedTasksModel>();

		setupPersonDialog();
	}

	// Constructor for updating existing person, PersonModel contains values
	public CreateUpdatePersonDialog(JFrame parent, PersonModel person, JTree assignedTasksTree, JTree taskTree) {
		super(parent, "Edit person...", true);
		setModalityType(Dialog.DEFAULT_MODALITY_TYPE.APPLICATION_MODAL);
		createTrees(assignedTasksTree, taskTree);

		this.personName.setText(person.getName());
		this.phone.setText(person.getPhone());
		this.email.setText(person.getEmail());
		this.notesArea.setText(person.getNotes());
		if (person.isStaff())
			this.staffButton.setSelected(true);
		else
			this.volunteerButton.setSelected(true);
		this.assignedTasks = person.getAssignedTasks();

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
						PersonEvent ev = new PersonEvent(this, personName.getText(), phone.getText(), email.getText(),
								staffButton.isSelected() ? true : false, processNotesArea(), assignedTasks, null);
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
		setSize(750, 475);
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
		staffButton.setActionCommand("staff");
		volunteerButton.setActionCommand("volunteer");

		staffPanel.add(staffButton);
		staffPanel.add(volunteerButton);

		staffGroup.add(staffButton);
		staffGroup.add(volunteerButton);
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
		assignedTasksScrollPane.setPreferredSize(new Dimension((int) notesArea.getPreferredSize().getWidth(),
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
					AssignTaskDialog event = new AssignTaskDialog(CreateUpdatePersonDialog.this,
							(AssignTaskEvent) nodeInfo);

					AssignTaskEvent eventResponse = event.getDialogResponse();
					if (eventResponse != null) {
						// Update assigned task model with new node info
						AssignedTasksModel taskModel = new AssignedTasksModel(eventResponse.getProgramName(), 
								eventResponse.getTask().getTaskName(), eventResponse.getDaysOfWeek(),
								eventResponse.getWeeksOfMonth());
						assignedTasks.removeLast();
						assignedTasks.add(taskModel);

						PersonEvent ev = new PersonEvent(this, personName.getText(), phone.getText(), email.getText(),
								staffButton.isSelected() ? true : false, processNotesArea(), assignedTasks, taskModel);
						dialogResponse = ev;
						setVisible(false);
						dispose();
					}
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
					setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);

					AssignTaskDialog event = new AssignTaskDialog(CreateUpdatePersonDialog.this,
							node.getParent().toString(), (TaskModel) nodeInfo);

					AssignTaskEvent eventResponse = event.getDialogResponse();
					if (eventResponse != null) {
						AssignedTasksModel taskModel = new AssignedTasksModel(node.getParent().toString(),
								childNode.toString(), eventResponse.getDaysOfWeek(), eventResponse.getWeeksOfMonth());
						assignedTasks.add(taskModel);

						PersonEvent ev = new PersonEvent(this, personName.getText(), phone.getText(), email.getText(),
								staffButton.isSelected() ? true : false, processNotesArea(), assignedTasks, taskModel);
						dialogResponse = ev;
						setVisible(false);
						dispose();
					}
				}
			}
		});
	}
}
