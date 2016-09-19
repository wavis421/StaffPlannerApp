package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Time;
import java.time.DayOfWeek;
import java.util.Calendar;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;

import controller.Controller;

public class CreateTaskDialog extends JDialog {
	private JButton okButton;
	private JButton cancelButton;
	private JTextField taskName;
	private JComboBox<String> dayOfWeekCombo;
	private JTextField timeTextField;
	private JTextField locationTextField;
	private JCheckBox weekOfMonth;
	private Color taskColor;

	public CreateTaskDialog(JFrame parent, Controller controller) {
		super(parent, "Create task...", true);

		taskName = new JTextField(20);
		dayOfWeekCombo = new JComboBox<String>();
		timeTextField = new JTextField(10);
		locationTextField = new JTextField(10);
		weekOfMonth = new JCheckBox ();
		taskColor = Color.BLACK;

		okButton = new JButton("OK");
		cancelButton = new JButton("Cancel");

		createDayOfWeekCombo();

		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					// Validate that time field is correct format
					Time time = Time.valueOf(timeTextField.getText() + ":00");
					
					// Create TaskEvent and send to controller to add to database
					TaskEvent ev = new TaskEvent(this, taskName.getText(),  locationTextField.getText(), dayOfWeekCombo.getSelectedIndex(), Calendar.DAY_OF_MONTH, time);
					controller.addTask(ev);
					setVisible(false);

				} catch (IllegalArgumentException ev) {
					JOptionPane.showMessageDialog(okButton, "Please enter Time as hh:mm");
				}
			}
		});
		cancelButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}

		});

		setTaskLayout();
		setSize(500, 375);
		setVisible(true);
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

		// First row
		gc.gridx = gc.gridy = 0;
		gc.anchor = GridBagConstraints.EAST;
		gc.insets = new Insets(0, 0, 0, 15);
		controlsPanel.add(new JLabel("Task Name: "), gc);
		gc.gridx++;
		gc.anchor = GridBagConstraints.WEST;
		gc.insets = new Insets(0, 0, 0, 0);
		controlsPanel.add(taskName, gc);

		// Next row
		gc.gridy++;
		gc.gridx = 0;
		gc.anchor = GridBagConstraints.EAST;
		gc.insets = new Insets(0, 0, 0, 15);
		controlsPanel.add(new JLabel("Day of Week: "), gc);
		gc.gridx++;
		gc.anchor = GridBagConstraints.WEST;
		gc.insets = new Insets(0, 0, 0, 0);
		controlsPanel.add(dayOfWeekCombo, gc);

		// Next row
		gc.gridy++;
		gc.gridx = 0;
		gc.anchor = GridBagConstraints.EAST;
		gc.insets = new Insets(0, 0, 0, 15);
		controlsPanel.add(new JLabel("Time: "), gc);
		gc.gridx++;
		gc.anchor = GridBagConstraints.WEST;
		gc.insets = new Insets(0, 0, 0, 0);
		controlsPanel.add(timeTextField, gc);

		// Next row
		gc.gridy++;
		gc.gridx = 0;
		gc.anchor = GridBagConstraints.EAST;
		gc.insets = new Insets(0, 0, 0, 15);
		controlsPanel.add(new JLabel("Weeks of the Month: "), gc);
		gc.gridx++;
		gc.anchor = GridBagConstraints.WEST;
		gc.insets = new Insets(0, 0, 0, 0);
		controlsPanel.add(weekOfMonth, gc);
		
		// Next row
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
		dayOfWeekCombo.setSelectedIndex(0);
		dayOfWeekCombo.setBorder(BorderFactory.createEtchedBorder());
	}
}
