package gui;

import java.awt.BorderLayout;
import java.awt.Component;
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
import javax.swing.tree.TreeSelectionModel;

import model.AssignedTasksModel;
import model.PersonModel;

public class CreateUpdatePersonDialog extends JDialog {
	private JButton okButton = new JButton("OK");
	private JButton cancelButton = new JButton("Cancel");
	
	// Private instance variables
	private JTextField personName = new JTextField(20);
	private JTextField phone = new JTextField(20);
	private JTextField email = new JTextField(20);
	private JRadioButton staffButton = new JRadioButton("Staff");
	private JRadioButton volunteerButton = new JRadioButton("Volunteer");
	private ButtonGroup staffGroup = new ButtonGroup ();
	private JPanel staffPanel = new JPanel();
	private JTextArea notesArea = new JTextArea(3, 20);
	private LinkedList<AssignedTasksModel> assignedTasks;
	private JScrollPane assignedTasksScrollPane;
	private JScrollPane taskTreeScrollPane;
	
	// Labels
	private JLabel nameLabel = new JLabel ("Person's name: ");
	private JLabel phoneLabel = new JLabel ("Phone #: ");
	private JLabel emailLabel = new JLabel ("Email: ");
	private JLabel staffLabel = new JLabel ("Staff or volunteer: ");
	private JLabel notesLabel = new JLabel ("Notes: ");
	
	// Dialog panels
	private JPanel controlsPanel;
	private JPanel buttonsPanel;
	private PersonEvent dialogResponse;
	
	public CreateUpdatePersonDialog (JFrame parent, JTree assignedTasksTree, JTree taskTree) {
		super(parent, "Add person...", true);
		createTrees (assignedTasksTree, taskTree);
		staffButton.setSelected(true);
		setupPersonDialog();
	}
	
	// Constructor for updating existing person, PersonModel contains values
	public CreateUpdatePersonDialog(JFrame parent, PersonModel person, JTree assignedTasksTree, JTree taskTree) {
		super(parent, "Edit person...", true);
		createTrees(assignedTasksTree, taskTree);

		personName.setText(person.getName());
		phone.setText(person.getPhone());
		email.setText(person.getEmail());
		notesArea.setText(person.getNotes());
		if (person.isStaff())
			staffButton.setSelected(true);
		else
			volunteerButton.setSelected(true);
		assignedTasks = person.getAssignedTasks();

		setupPersonDialog();
	}

	public PersonEvent getDialogResponse() {
		return dialogResponse;
	}
	
	private void setupPersonDialog () {
		createStaffSelector ();
		
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
					// Make sure that program name has been entered
					if (personName.getText().equals("")) {
						JOptionPane.showMessageDialog(okButton, "Person's name field is required");
					} else {
						PersonEvent ev = new PersonEvent(this, personName.getText(), phone.getText(), 
								email.getText(), staffButton.isSelected() ? true : false, 
								processNotesArea(), assignedTasks);
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
		setSize(550, 450);
		setVisible(true);	
	}
	
	private void setPersonLayout() {
		int gridY = 0;
		controlsPanel = new JPanel();
		buttonsPanel = new JPanel();

		controlsPanel.setLayout(new GridBagLayout());
		buttonsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

		// controlsPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		Border titleBorder = BorderFactory.createRaisedSoftBevelBorder();
		Border spaceBorder = BorderFactory.createEmptyBorder(15, 15, 15, 15);
		controlsPanel.setBorder(BorderFactory.createCompoundBorder(spaceBorder, titleBorder));

		GridBagConstraints gc = new GridBagConstraints();

		gc.weightx = gc.weighty = 1;
		gc.fill = GridBagConstraints.NONE;

		// Add name, phone, email and notes fields
		addRowToControlPanel(gc, nameLabel, personName, gridY++);
		addRowToControlPanel(gc, phoneLabel, phone, gridY++);
		addRowToControlPanel(gc, emailLabel, email, gridY++);
		addRowToControlPanel(gc, staffLabel, staffPanel, gridY++);
		addRowToControlPanel(gc, notesLabel, notesArea, gridY++);
		addRowToControlPanel(gc, assignedTasksScrollPane, taskTreeScrollPane, gridY++);

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

	private void createStaffSelector () {
		staffButton.setActionCommand("staff");
		volunteerButton.setActionCommand("volunteer");
		
		staffPanel.add(staffButton);
		staffPanel.add(volunteerButton);
		
		staffGroup.add(staffButton);
		staffGroup.add(volunteerButton);
	}
	
	private String processNotesArea () {
		int numRows = 0, currIdx = 0, currLength = 0;
		for (int i = 0; i < 3 && notesArea.getText().length() > currLength; i++) {
			currIdx = notesArea.getText().indexOf("\n", currLength);
			if (currIdx != -1) {   // Found new-line
				currLength = currIdx + 1;
				numRows = i + 1;
			}
		}
		int remainLength = notesArea.getText().length() - currLength;
		if (remainLength > (31 * (3 - numRows)))
			remainLength = 31 * (3 - numRows);
		return (notesArea.getText().substring(0,  currLength + remainLength));
	}
	
	private void createTrees (JTree assignedTasksTree, JTree taskTree) {
		assignedTasksScrollPane = new JScrollPane (assignedTasksTree);
		assignedTasksScrollPane.setPreferredSize(new Dimension((int)notesArea.getPreferredSize().getWidth(), 
				(int)notesArea.getPreferredSize().getHeight() * 2));
		assignedTasksTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		
		/* Add tree listener */
		assignedTasksTree.addTreeSelectionListener(new TreeSelectionListener() {
		    public void valueChanged(TreeSelectionEvent evt) {
		        DefaultMutableTreeNode node = (DefaultMutableTreeNode)
		        		assignedTasksTree.getLastSelectedPathComponent();
 
		        if (node == null) return;

		        /* retrieve the node that was selected */ 
		        Object nodeInfo = node.getUserObject();		        
		        System.out.println("Tree Selection Listener: " + nodeInfo + ", path: " + evt.getPath() +
		        		", isLeaf: " + node.isLeaf() + ", root: " + node.isRoot() + 
		        		", path length: " + evt.getPath().getPathCount());   
		    }
		});
		
		taskTreeScrollPane = new JScrollPane(taskTree);
		taskTreeScrollPane.setPreferredSize(new Dimension((int)notesArea.getPreferredSize().getWidth(), 
				(int)notesArea.getPreferredSize().getHeight() * 2));
		taskTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		
		/* Add tree listener */
		taskTree.addTreeSelectionListener(new TreeSelectionListener() {
		    public void valueChanged(TreeSelectionEvent evt) {
		        DefaultMutableTreeNode node = (DefaultMutableTreeNode)
		        		taskTree.getLastSelectedPathComponent();
 
		        if (node == null) return;

		        /* retrieve the node that was selected */ 
		        Object nodeInfo = node.getUserObject();	
		        System.out.println("Tree Selection Listener: " + nodeInfo + ", path: " + evt.getPath() +
		        		", isLeaf: " + node.isLeaf() + ", root: " + node.isRoot() + 
		        		", path length: " + evt.getPath().getPathCount()); 
		    }
		});
	}
}
