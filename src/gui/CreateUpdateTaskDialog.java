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

import model.TaskModel;

public class CreateUpdateTaskDialog extends JDialog {
	private JButton okButton = new JButton("OK");
	private JButton cancelButton = new JButton("Cancel");

	private JTextField taskName = new JTextField(20);
	private JTextField timeTextField = new JTextField(10);
	private JTextField locationTextField = new JTextField(10);
	private JComboBox<String> dayOfWeekCombo = new JComboBox<String>();
	private JRadioButton[] weekOfMonthButtons = new JRadioButton[5];

	private JLabel taskNameLabel = new JLabel("Task Name: ");
	private JLabel timeLabel = new JLabel("Time: ");
	private JLabel locationLabel = new JLabel("Location: ");
	private JLabel dayOfWeekLabel = new JLabel("Day of Week: ");
	private JLabel weekOfMonthLabel = new JLabel();

	private JPanel controlsPanel;
	private JPanel buttonsPanel;

	private TaskEvent dialogResponse;
	private TaskModel currentTask;

	public CreateUpdateTaskDialog(JFrame parent) {
		super(parent, "Create task...", true);
		currentTask = null;
		setupTaskDialog();
	}

	public CreateUpdateTaskDialog(JFrame parent, TaskModel task) {
		super(parent, "Update task...", true);

		currentTask = task;
		taskName.setText(task.getTaskName());
		timeTextField.setText(getTimeString(task.getTime().toString()));
		locationTextField.setText(task.getLocation());

		setupTaskDialog();
	}

	public CreateUpdateTaskDialog(JFrame parent, TaskEvent event) {
		super(parent, "Create task...", true);
		
		currentTask = new TaskModel(event.getTaskName(), event.getLocation(), event.getDayOfWeek(),
				event.getWeekOfMonth(), event.getTime());
		timeTextField.setText(getTimeString(currentTask.getTime().toString()));
		locationTextField.setText(currentTask.getLocation());

		setupTaskDialog();
	}

	private String getTimeString(String timeString) {
		int firstColon = timeString.indexOf(":");
		return timeString.substring(0, firstColon + 3);
	}

	private void setupTaskDialog() {
		createDayOfWeekCombo();
		createWeekOfMonthButtons();
		timeTextField.setToolTipText("Enter start time as hh:mm");

		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					// Make sure that task name has been entered
					if (taskName.getText().equals("")) {
						JOptionPane.showMessageDialog(okButton, "Task name field is required");
					} else {
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
					}

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

		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setTaskLayout();
		setSize(550, 375);
		setVisible(true);
	}

	public TaskEvent getDialogResponse() {
		return dialogResponse;
	}

	private void setTaskLayout() {
		int gridY = 0;
		controlsPanel = new JPanel();
		buttonsPanel = new JPanel();

		controlsPanel.setLayout(new GridBagLayout());
		buttonsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

		// controlsPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		Border titleBorder = BorderFactory.createTitledBorder("Create/Update Task");
		Border spaceBorder = BorderFactory.createEmptyBorder(15, 15, 15, 15);
		controlsPanel.setBorder(BorderFactory.createCompoundBorder(spaceBorder, titleBorder));

		GridBagConstraints gc = new GridBagConstraints();

		gc.weightx = gc.weighty = 1;
		gc.fill = GridBagConstraints.NONE;

		addRowToControlPanel(gc, taskNameLabel, taskName, gridY++);
		addRowToControlPanel(gc, locationLabel, locationTextField, gridY++);
		addRowToControlPanel(gc, dayOfWeekLabel, dayOfWeekCombo, gridY++);
		addRowToControlPanel(gc, timeLabel, timeTextField, gridY++);

		// Weeks of the month row: requires special handling
		gc.weighty = 0.1;
		gc.gridx = 0;
		gc.gridy = gridY;
		gc.anchor = GridBagConstraints.EAST;
		gc.insets = new Insets(0, 0, 0, 15);
		controlsPanel.add(weekOfMonthLabel, gc);
		gc.anchor = GridBagConstraints.WEST;
		gc.insets = new Insets(0, 0, 0, 0);
		gc.gridx = 1;
		for (int i = 0; i < weekOfMonthButtons.length; i++) {
			gc.gridy = gridY++;
			controlsPanel.add(weekOfMonthButtons[i], gc);
		}

		// Buttons row
		gc.weightx = gc.weighty = 1;
		gc.gridy = gridY++;
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

	private void addRowToControlPanel(GridBagConstraints gcon, JLabel lbl, Component value, int gridY) {
		gcon.gridx = 0;
		gcon.gridy = gridY;
		gcon.anchor = GridBagConstraints.EAST;
		gcon.insets = new Insets(0, 0, 0, 15);
		controlsPanel.add(lbl, gcon);
		gcon.gridx++;
		gcon.anchor = GridBagConstraints.WEST;
		gcon.insets = new Insets(0, 0, 0, 0);
		controlsPanel.add(value, gcon);
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

		dayOfWeekCombo.setModel(dayOfWeekModel);
		if (currentTask != null)
			dayOfWeekCombo.setSelectedIndex(currentTask.getDayOfWeek() - 1);
		else
			dayOfWeekCombo.setSelectedIndex(0);
		weekOfMonthLabel.setText(dayOfWeekCombo.getSelectedItem().toString() + "s of the Month:");
		dayOfWeekCombo.setBorder(BorderFactory.createEtchedBorder());

		// Add listener
		dayOfWeekCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				weekOfMonthLabel.setText(dayOfWeekCombo.getSelectedItem().toString() + "s of the Month:");
			}
		});
	}

	private void createWeekOfMonthButtons() {
		String[] weekName = { "1st", "2nd", "3rd", "4th", "5th" };

		if (currentTask == null) {
			for (int i = 0; i < weekOfMonthButtons.length; i++)
				weekOfMonthButtons[i] = new JRadioButton(weekName[i]);
		} else {
			boolean[] currentWeekOfMonth = currentTask.getWeekOfMonth();
			for (int i = 0; i < weekOfMonthButtons.length; i++) {
				weekOfMonthButtons[i] = new JRadioButton(weekName[i]);
				weekOfMonthButtons[i].setSelected(currentWeekOfMonth[i]);
			}
		}
	}
}
