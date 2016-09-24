package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Time;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.Border;

public class CreateTaskDialog extends JDialog {
	private JButton okButton;
	private JButton cancelButton;
	private JTextField taskName;
	private JComboBox<String> dayOfWeekCombo;
	private JTextField timeTextField;
	private JTextField locationTextField;
	private JRadioButton[] weekOfMonthButtons;
	
	private TaskEvent dialogResponse;

	public CreateTaskDialog(JFrame parent) {
		super(parent, "Create task...", true);

		taskName = new JTextField(20);
		dayOfWeekCombo = new JComboBox<String>();
		timeTextField = new JTextField(10);
		locationTextField = new JTextField(10);

		okButton = new JButton("OK");
		cancelButton = new JButton("Cancel");

		createDayOfWeekCombo();
		createWeekOfMonthButtons();
		
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					// Validate that time field is correct format
					Time time = Time.valueOf(timeTextField.getText() + ":00");

					// Create weeks of month array
					int numWeeksInMonth = weekOfMonthButtons.length;
					boolean[] weeksOfMonthSelected = new boolean[numWeeksInMonth];
					for (int i = 0; i < numWeeksInMonth; i++) {
						if (weekOfMonthButtons[i].isSelected())
							weeksOfMonthSelected[i] = true;
					}
					
					// Create TaskEvent and set response
					TaskEvent ev = new TaskEvent(this, taskName.getText(), locationTextField.getText(),
							dayOfWeekCombo.getSelectedIndex() + 1, weeksOfMonthSelected, time);
					dialogResponse = ev;
					setVisible(false);
					dispose();

				} catch (IllegalArgumentException ev) {
					JOptionPane.showMessageDialog(okButton, "Please enter Time as hh:mm");
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

		setTaskLayout();
		setSize(550, 375);
		setVisible(true);
	}

	public TaskEvent getDialogResponse () {
		return dialogResponse; 	
	}
	
	private void setTaskLayout() {
		JPanel controlsPanel = new JPanel();
		JPanel buttonsPanel = new JPanel();

		controlsPanel.setLayout(new GridBagLayout());
		buttonsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

		// controlsPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		Border titleBorder = BorderFactory.createTitledBorder("Create/Edit Task");
		Border spaceBorder = BorderFactory.createEmptyBorder(15, 15, 15, 15);
		controlsPanel.setBorder(BorderFactory.createCompoundBorder(spaceBorder, titleBorder));

		GridBagConstraints gc = new GridBagConstraints();

		gc.weightx = gc.weighty = 1;
		gc.fill = GridBagConstraints.NONE;

		// Task name row
		gc.gridx = gc.gridy = 0;
		gc.anchor = GridBagConstraints.EAST;
		gc.insets = new Insets(0, 0, 0, 15);
		controlsPanel.add(new JLabel("Task Name: "), gc);
		gc.gridx++;
		gc.anchor = GridBagConstraints.WEST;
		gc.insets = new Insets(0, 0, 0, 0);
		controlsPanel.add(taskName, gc);

		// Day of week row
		gc.gridy++;
		gc.gridx = 0;
		gc.anchor = GridBagConstraints.EAST;
		gc.insets = new Insets(0, 0, 0, 15);
		controlsPanel.add(new JLabel("Day of Week: "), gc);
		gc.gridx++;
		gc.anchor = GridBagConstraints.WEST;
		gc.insets = new Insets(0, 0, 0, 0);
		controlsPanel.add(dayOfWeekCombo, gc);

		// Time row
		gc.gridy++;
		gc.gridx = 0;
		gc.anchor = GridBagConstraints.EAST;
		gc.insets = new Insets(0, 0, 0, 15);
		controlsPanel.add(new JLabel("Time: "), gc);
		gc.gridx++;
		gc.anchor = GridBagConstraints.WEST;
		gc.insets = new Insets(0, 0, 0, 0);
		controlsPanel.add(timeTextField, gc);

		// Weeks of the month row
		gc.gridy++;
		gc.gridx = 0;
		gc.anchor = GridBagConstraints.EAST;
		gc.insets = new Insets(0, 0, 0, 15);
		controlsPanel.add(new JLabel("Weeks of the Month: "), gc);
		gc.anchor = GridBagConstraints.WEST;
		gc.insets = new Insets(0, 0, 0, 0);
		for (int i = 0; i < weekOfMonthButtons.length; i++) {
			gc.gridx++;
			controlsPanel.add(weekOfMonthButtons[i], gc);
		}

		// Location row
		gc.gridy++;
		gc.gridx = 0;
		gc.anchor = GridBagConstraints.EAST;
		gc.insets = new Insets(0, 0, 0, 15);
		controlsPanel.add(new JLabel("Location: "), gc);
		gc.gridx++;
		gc.anchor = GridBagConstraints.WEST;
		gc.insets = new Insets(0, 0, 0, 0);
		controlsPanel.add(locationTextField, gc);

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

	private void createDayOfWeekCombo() {
		DefaultComboBoxModel<String> dayOfWeekModel = new DefaultComboBoxModel<String>();

		dayOfWeekModel.addElement(new String("Sunday"));
		dayOfWeekModel.addElement(new String("Monday"));
		dayOfWeekModel.addElement(new String("Tuesday"));
		dayOfWeekModel.addElement(new String("Wednesday"));
		dayOfWeekModel.addElement(new String("Thursday"));
		dayOfWeekModel.addElement(new String("Friday"));
		dayOfWeekModel.addElement(new String("Saturday"));

		dayOfWeekCombo = new JComboBox<String>(dayOfWeekModel);
		dayOfWeekCombo.setSelectedIndex(1);
		dayOfWeekCombo.setBorder(BorderFactory.createEtchedBorder());
	}
	
	private void createWeekOfMonthButtons() {
		weekOfMonthButtons = new JRadioButton[6];
		for (int i = 0; i < 6; i++)
			weekOfMonthButtons[i] = new JRadioButton();
	}
}
