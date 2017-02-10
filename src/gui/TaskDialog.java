package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;

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
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import model.TaskModel;
import model.TimeModel;
import utilities.Utilities;

public class TaskDialog extends JDialog {
	private static final int TEXT_FIELD_WIDTH = 30;
	private static final int DEFAULT_HOUR = 8;
	private static final int DEFAULT_MINUTE = 30;
	private JButton okButton = new JButton("OK");
	private JButton cancelButton = new JButton("Cancel");

	// Private instance variables
	private JTextField taskNameField = new JTextField(TEXT_FIELD_WIDTH);
	private JTextField locationTextField = new JTextField(TEXT_FIELD_WIDTH);
	private NumberSpinnerHandler hourSpinner;
	private NumberSpinnerHandler minuteSpinner;

	private JComboBox<String> comboAmPm;
	private JSpinner numLeadersSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 9999, 1));
	private JSpinner totalPersonsSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 9999, 1));
	private JRadioButton[] dayOfWeekButtons = new JRadioButton[7];
	private JRadioButton[] weekOfMonthButtons = new JRadioButton[5];
	private JPanel colorPanel = new JPanel();
	private ButtonGroup colorGroup;

	// Label variables
	private JLabel taskNameLabel = new JLabel("Task Name: ");
	private JLabel timeLabel = new JLabel("Start Time: ");
	private JLabel locationLabel = new JLabel("Location: ");
	private JLabel dayOfWeekLabel = new JLabel("Days of the Week: ");
	private JLabel weekOfMonthLabel = new JLabel("DOW in the Month:");
	private JLabel colorChooserLabel = new JLabel("Select task color: ");

	// Dialog panels
	private JPanel controlsPanel;
	private JPanel buttonsPanel;
	private JPanel dayOfWeekPanel;
	private JPanel weekOfMonthPanel;
	private JPanel timePanel;
	private TaskEvent dialogResponse;

	// Track current program and task
	private String currentProgramName;
	private TaskModel currentTask;
	private String borderTitle;

	// Constructor for creating new task
	public TaskDialog(JFrame parent, String programName) {
		super(parent, programName, true);
		setLocation(new Point(100, 100));

		currentProgramName = new String(programName);
		currentTask = null;
		createTimePanel(null);
		borderTitle = new String("Create new task");
		setupTaskDialog();
	}

	// Constructor for updating existing task, TaskModel contains task values
	public TaskDialog(JFrame parent, String programName, TaskModel task) {
		super(parent, programName, true);
		setLocation(new Point(100, 100));

		currentProgramName = programName;
		currentTask = task;
		taskNameField.setText(currentTask.getTaskName());
		createTimePanel(currentTask.getTime());
		locationTextField.setText(currentTask.getLocation());
		numLeadersSpinner.setValue(currentTask.getNumLeadersReqd());
		totalPersonsSpinner.setValue(currentTask.getTotalPersonsReqd());

		borderTitle = new String("Edit task");
		setupTaskDialog();
	}

	// Constructor for re-try of task create, TaskEvent content re-loaded
	public TaskDialog(JFrame parent, TaskEvent event, int taskID, int programID) {
		super(parent, event.getProgramName(), true);
		setLocation(new Point(100, 100));

		// Set up task, but leave name field empty since it was found to be a
		// duplicate
		currentProgramName = event.getProgramName();
		currentTask = new TaskModel(taskID, programID, event.getTaskName(), event.getLocation(), event.getNumLeadersReqd(),
				event.getTotalPersonsReqd(), event.getDayOfWeek(), event.getWeekOfMonth(), event.getTime(),
				event.getColor());

		createTimePanel(currentTask.getTime());
		locationTextField.setText(currentTask.getLocation());
		numLeadersSpinner.setValue(currentTask.getNumLeadersReqd());
		totalPersonsSpinner.setValue(currentTask.getTotalPersonsReqd());

		borderTitle = new String("Create new task");
		setupTaskDialog();
	}

	private void setupTaskDialog() {
		createDayOfWeekButtons();
		createWeekOfMonthButtons();
		createColorSelector();
		createSpinnerListeners();

		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Make sure that task name has been entered
				if (taskNameField.getText().equals("")) {
					JOptionPane.showMessageDialog(okButton, "Task name field is required");
				} else {
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
					TaskEvent ev = new TaskEvent(this, currentProgramName, taskNameField.getText().trim(),
							locationTextField.getText().trim(), (Integer) numLeadersSpinner.getValue(),
							(Integer) totalPersonsSpinner.getValue(), daysOfWeekSelected, weeksOfMonthSelected,
							getTime(hourSpinner, minuteSpinner),
							Integer.parseInt(colorGroup.getSelection().getActionCommand()));
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
		setTaskLayout();
		setSize(625, 400);
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

		// Add day-of-week buttons
		for (int i = 0; i < dayOfWeekButtons.length; i++) {
			dayOfWeekPanel.add(dayOfWeekButtons[i]);
		}

		// Add weeks in month buttons and the "all" button
		JPanel womSubPanel = new JPanel();
		for (int i = 0; i < weekOfMonthButtons.length; i++) {
			womSubPanel.add(weekOfMonthButtons[i]);
		}
		JRadioButton allWeeksButton = new JRadioButton("All");
		weekOfMonthPanel.add(womSubPanel);
		weekOfMonthPanel.add(allWeeksButton);

		// Add listener to "all" button
		allWeeksButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (allWeeksButton.isSelected()) {
					for (int i = 0; i < weekOfMonthButtons.length; i++) {
						weekOfMonthButtons[i].setSelected(true);
					}
				}
			}
		});

		// controlsPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		Border titleBorder = BorderFactory.createTitledBorder(borderTitle);
		Border spaceBorder = BorderFactory.createEmptyBorder(15, 15, 15, 15);
		controlsPanel.setBorder(BorderFactory.createCompoundBorder(spaceBorder, titleBorder));
		dayOfWeekPanel.setBorder(BorderFactory.createEtchedBorder());
		womSubPanel.setBorder(BorderFactory.createEtchedBorder());

		GridBagConstraints gc = new GridBagConstraints();

		gc.weightx = gc.weighty = 1;
		gc.fill = GridBagConstraints.NONE;

		// Add task name, location, time, DOW/WOM selectors and color picker
		addRowToControlPanel(gc, taskNameLabel, taskNameField, gridY++);
		addRowToControlPanel(gc, locationLabel, locationTextField, gridY++);
		addRowToControlPanel(gc, timeLabel, timePanel, gridY++);
		addRowToControlPanel(gc, new JLabel("Minimum # leaders req'd: "), numLeadersSpinner, gridY++);
		addRowToControlPanel(gc, new JLabel("Total leaders/assistants req'd: "), totalPersonsSpinner, gridY++);
		addRowToControlPanel(gc, dayOfWeekLabel, dayOfWeekPanel, gridY++);
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
		int[] colorSelections = Utilities.getColorSelection();
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
		else
			buttons[0].setSelected(true);
	}

	private void createSpinnerListeners() {
		numLeadersSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				checkSpinnerValues();
			}
		});
		totalPersonsSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				checkSpinnerValues();
			}
		});
	}

	private void checkSpinnerValues() {
		if ((Integer) numLeadersSpinner.getValue() > (Integer) totalPersonsSpinner.getValue())
			totalPersonsSpinner.setValue((Integer) numLeadersSpinner.getValue());
	}

	private void createTimePanel(TimeModel timeModel) {
		if (timeModel == null)
			timeModel = new TimeModel(DEFAULT_HOUR, DEFAULT_MINUTE);

		if (timeModel.getHour() == 0)
			hourSpinner = new NumberSpinnerHandler(12, 1, 12, 1);
		else
			hourSpinner = new NumberSpinnerHandler(timeModel.getHour(), 1, 12, 1);
		minuteSpinner = new NumberSpinnerHandler(timeModel.getMinute(), 0, 59, 5);

		hourSpinner.setPreferredSize(new Dimension(35, hourSpinner.getPreferredSize().height));
		minuteSpinner.setPreferredSize(new Dimension(35, minuteSpinner.getPreferredSize().height));

		DefaultComboBoxModel<String> modelAmPm = new DefaultComboBoxModel<String>();
		modelAmPm.addElement("AM");
		modelAmPm.addElement("PM");
		comboAmPm = new JComboBox<String>(modelAmPm);
		comboAmPm.setSelectedIndex(timeModel.getAmPm());
		comboAmPm.setEditable(false);
		comboAmPm.setBorder(BorderFactory.createEtchedBorder());

		// Add everything to the time panel
		timePanel = new JPanel();
		timePanel.add(hourSpinner);
		timePanel.add(new JLabel(":"));
		timePanel.add(minuteSpinner);
		timePanel.add(comboAmPm);
	}

	private TimeModel getTime(NumberSpinnerHandler hour, NumberSpinnerHandler minute) {
		int newHour = hour.getCurrentValue();

		if (newHour == 12 && comboAmPm.getSelectedIndex() == Calendar.AM)
			newHour = 0; // Back to 0
		else if (comboAmPm.getSelectedIndex() == Calendar.PM && newHour < 12)
			newHour += 12;

		return (new TimeModel(newHour, minute.getCurrentValue()));
	}
}
