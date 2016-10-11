package gui;

import java.awt.BorderLayout;
import java.awt.Color;
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
import javax.swing.ButtonGroup;
import javax.swing.JButton;
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

	// Private instance variables
	private JTextField taskName = new JTextField(20);
	private JTextField timeTextField = new JTextField(10);
	private JTextField locationTextField = new JTextField(10);
	private JRadioButton[] dayOfWeekButtons = new JRadioButton[7];
	private JRadioButton[] weekOfMonthButtons = new JRadioButton[5];
	private JPanel colorPanel = new JPanel();
	private ButtonGroup colorGroup;

	// Label variables
	private JLabel taskNameLabel = new JLabel("Task Name: ");
	private JLabel timeLabel = new JLabel("Time: ");
	private JLabel locationLabel = new JLabel("Location: ");
	private JLabel dayOfWeekLabel = new JLabel("Days of the Week: ");
	private JLabel weekOfMonthLabel = new JLabel("Weeks of the Month:");
	private JLabel colorChooserLabel = new JLabel("Select task color: ");

	// Dialog panels
	private JPanel controlsPanel;
	private JPanel buttonsPanel;
	private JPanel dayOfWeekPanel;
	private JPanel weekOfMonthPanel;
	private TaskEvent dialogResponse;

	// Track current program and task
	private String currentProgramName;
	private TaskModel currentTask;
	private String borderTitle;

	// Constructor for creating new task
	public CreateUpdateTaskDialog(JFrame parent, String programName) {
		super(parent, programName, true);
		currentProgramName = new String(programName);
		currentTask = null;
		borderTitle = new String("Create new task");
		setupTaskDialog();
	}

	// Constructor for updating existing task, TaskModel contains task values
	public CreateUpdateTaskDialog(JFrame parent, String programName, TaskModel task) {
		super(parent, programName, true);

		currentProgramName = programName;
		currentTask = task;
		taskName.setText(currentTask.getTaskName());
		timeTextField.setText(getTimeString(currentTask.getTime().toString()));
		locationTextField.setText(currentTask.getLocation());

		borderTitle = new String("Edit task");
		setupTaskDialog();
	}

	// Constructor for re-try of task create, TaskEvent content re-loaded
	public CreateUpdateTaskDialog(JFrame parent, TaskEvent event) {
		super(parent, event.getProgramName(), true);

		// Set up task, but leave name field empty since it was found to be a duplicate
		currentProgramName = event.getProgramName();
		currentTask = new TaskModel(event.getTaskName(), event.getLocation(),
				event.getDayOfWeek(), event.getWeekOfMonth(), event.getTime(), event.getColor());
		timeTextField.setText(getTimeString(currentTask.getTime().toString()));
		locationTextField.setText(currentTask.getLocation());

		borderTitle = new String("Create new task");
		setupTaskDialog();
	}

	private String getTimeString(String timeString) {
		int firstColon = timeString.indexOf(":");
		return timeString.substring(0, firstColon + 3);
	}

	private void setupTaskDialog() {
		createDayOfWeekButtons();
		createWeekOfMonthButtons();
		createColorSelector();

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

						// Create days of week array
						int numDaysInWeek = dayOfWeekButtons.length;
						boolean[] daysOfWeekSelected = new boolean[numDaysInWeek];
						for (int i = 0; i < numDaysInWeek; i++) {
							if (dayOfWeekButtons[i].isSelected())
								daysOfWeekSelected[i] = true;
						}

						// Create weeks of month array
						int numWeeksInMonth = weekOfMonthButtons.length;
						boolean[] weeksOfMonthSelected = new boolean[numWeeksInMonth];
						for (int i = 0; i < numWeeksInMonth; i++) {
							if (weekOfMonthButtons[i].isSelected())
								weeksOfMonthSelected[i] = true;
						}

						// Create TaskEvent and set response
						TaskEvent ev = new TaskEvent(this, currentProgramName, taskName.getText(),
								locationTextField.getText(), daysOfWeekSelected,
								weeksOfMonthSelected, time, 
								Integer.parseInt(colorGroup.getSelection().getActionCommand()));
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
		setSize(580, 430);
		setVisible(true);
	}

	public TaskEvent getDialogResponse() {
		return dialogResponse;
	}

	private void setTaskLayout() {
		int gridY = 0;
		controlsPanel = new JPanel();
		buttonsPanel = new JPanel();
		dayOfWeekPanel = new JPanel();
		weekOfMonthPanel = new JPanel();

		controlsPanel.setLayout(new GridBagLayout());
		buttonsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		for (int i = 0; i < dayOfWeekButtons.length; i++) {
			dayOfWeekPanel.add(dayOfWeekButtons[i]);
		}
		for (int i = 0; i < weekOfMonthButtons.length; i++) {
			weekOfMonthPanel.add(weekOfMonthButtons[i]);
		}

		// controlsPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		Border titleBorder = BorderFactory.createTitledBorder(borderTitle);
		Border spaceBorder = BorderFactory.createEmptyBorder(15, 15, 15, 15);
		controlsPanel.setBorder(BorderFactory.createCompoundBorder(spaceBorder, titleBorder));

		GridBagConstraints gc = new GridBagConstraints();

		gc.weightx = gc.weighty = 1;
		gc.fill = GridBagConstraints.NONE;
		
		// Add task name, location, time, DOW/WOM selectors and color picker
		addRowToControlPanel(gc, taskNameLabel, taskName, gridY++);
		addRowToControlPanel(gc, locationLabel, locationTextField, gridY++);
		addRowToControlPanel(gc, timeLabel, timeTextField, gridY++);
		gc.weighty = 0.1;
		addRowToControlPanel(gc, dayOfWeekLabel, dayOfWeekPanel, gridY++);
		gc.weighty = 1;
		addRowToControlPanel(gc, weekOfMonthLabel, weekOfMonthPanel, gridY++);
		addRowToControlPanel(gc, colorChooserLabel, colorPanel, gridY++);

		// Buttons row
		gc.gridx = 0;
		gc.gridy = gridY++;
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

	private void createDayOfWeekButtons() {
		String[] dayName = { "Sun", "Mon", "Tue", "Wed", "Th", "Fri", "Sat" };

		if (currentTask == null) {
			for (int i = 0; i < dayOfWeekButtons.length; i++)
				dayOfWeekButtons[i] = new JRadioButton(dayName[i]);
		} else {
			boolean[] currentDayOfWeek = currentTask.getDayOfWeek();
			for (int i = 0; i < dayOfWeekButtons.length; i++) {
				dayOfWeekButtons[i] = new JRadioButton(dayName[i]);
				dayOfWeekButtons[i].setSelected(currentDayOfWeek[i]);
			}
		}
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

	private void createColorSelector() {
		colorGroup = new ButtonGroup();
		int[] colorSelections = { 
				0x000000, // Black 
				0xDC143C, // Crimson
				0xF28500, // Tangerine
				0x008000, // Green
				0x7CFC00, // Lawn green
				0x003399, // Dark powder blue
				0x5082B6, // Moderate blue
				0x8B008B, // Dark magenta
				0x966FD6, // Dark Pastel purple
				0x988344, // Dark Khaki
				0x7D7D7D  // Dark gray
				};
		JRadioButton[] buttons = new JRadioButton[colorSelections.length];

		int taskColor, colorMatchIdx = -1;
		if (currentTask != null)
			taskColor = currentTask.getColor();
		else
			taskColor = colorSelections[0];

		for (int idx = 0; idx < colorSelections.length; idx++) {
			buttons[idx] = new JRadioButton();

			buttons[idx].setBackground(Color.decode(Integer.toString(colorSelections[idx])));
			buttons[idx].setActionCommand(Integer.toString(colorSelections[idx]));

			colorGroup.add(buttons[idx]);
			colorPanel.add(buttons[idx]);

			if (taskColor == colorSelections[idx])
				colorMatchIdx = idx;
		}

		// If color match found in color table, than highlight the selection
		if (colorMatchIdx != -1)
			buttons[colorMatchIdx].setSelected(true);
	}
}
