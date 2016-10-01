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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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
import javax.swing.JTextField;
import javax.swing.border.Border;

import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

import model.TaskModel;

public class CreateUpdateTaskDialog extends JDialog {
	private JButton okButton = new JButton("OK");
	private JButton cancelButton = new JButton("Cancel");

	// Private instance variables
	private JTextField taskName = new JTextField(20);
	private JTextField timeTextField = new JTextField(10);
	private JTextField locationTextField = new JTextField(10);
	private JComboBox<String> dayOfWeekCombo = new JComboBox<String>();
	private JRadioButton[] weekOfMonthButtons = new JRadioButton[5];
	private JRadioButton enableEndDateButton = new JRadioButton("Set end-date ");
	private JDatePickerImpl endDatePicker;
	private JPanel colorPanel = new JPanel();
	private ButtonGroup colorGroup;

	// Label variables
	private JLabel taskNameLabel = new JLabel("Task Name: ");
	private JLabel timeLabel = new JLabel("Time: ");
	private JLabel locationLabel = new JLabel("Location: ");
	private JLabel dayOfWeekLabel = new JLabel("Day of Week: ");
	private JLabel weekOfMonthLabel = new JLabel();
	private JLabel endDateLabel = new JLabel("End-date: ");
	private JLabel colorChooserLabel = new JLabel("Select task color: ");

	// Dialog panels
	private JPanel controlsPanel;
	private JPanel buttonsPanel;
	private TaskEvent dialogResponse;

	// Track current task
	private TaskModel currentTask;

	// Constructor for creating new task
	public CreateUpdateTaskDialog(JFrame parent) {
		super(parent, "Create task...", true);
		currentTask = null;
		setupTaskDialog();
	}

	// Constructor for updating existing task, TaskModel contains task values
	public CreateUpdateTaskDialog(JFrame parent, TaskModel task) {
		super(parent, "Update task...", true);

		currentTask = task;
		taskName.setText(currentTask.getTaskName());
		timeTextField.setText(getTimeString(currentTask.getTime().toString()));
		locationTextField.setText(currentTask.getLocation());

		setupTaskDialog();
	}

	// Constructor for re-try of task create, TaskEvent content re-loaded
	public CreateUpdateTaskDialog(JFrame parent, TaskEvent event) {
		super(parent, "Create task...", true);

		// Set up task, but leave name field empty since it was found to be a
		// duplicate
		currentTask = new TaskModel(event.getTaskName(), event.getLocation(), event.getDayOfWeek(),
				event.getWeekOfMonth(), event.getTime(), event.getEndDate(), event.getColor());
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
		createEndDatePicker();
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

						// Create weeks of month array
						int numWeeksInMonth = weekOfMonthButtons.length;
						boolean[] weeksOfMonthSelected = new boolean[numWeeksInMonth];
						for (int i = 0; i < numWeeksInMonth; i++) {
							if (weekOfMonthButtons[i].isSelected())
								weeksOfMonthSelected[i] = true;
						}

						// Create TaskEvent and set response
						TaskEvent ev = new TaskEvent(this, taskName.getText(), locationTextField.getText(),
								dayOfWeekCombo.getSelectedIndex() + 1, weeksOfMonthSelected, time,
								endDatePicker.getJFormattedTextField().getText(),
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
		setSize(575, 450);
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

		// End-date row: requires special handling
		gc.weightx = gc.weighty = 1;
		gc.gridx = 0;
		gc.gridy = gridY++;
		gc.anchor = GridBagConstraints.EAST;
		controlsPanel.add(enableEndDateButton, gc);
		addRowToControlPanel(gc, endDateLabel, endDatePicker, gridY++);

		// Color chooser row
		addRowToControlPanel(gc, colorChooserLabel, colorPanel, gridY++);

		// Buttons row
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

	private void createEndDatePicker() {
		UtilDateModel dateModel = new UtilDateModel();
		Properties prop = new Properties();
		JDatePanelImpl datePanel;
		SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy");

		prop.put("text.today", "today");
		prop.put("text.month", "month");
		prop.put("text.year", "year");

		datePanel = new JDatePanelImpl(dateModel, prop);
		endDatePicker = new JDatePickerImpl(datePanel, new DateLabelFormatter());

		if (currentTask != null && currentTask.getEndDate() != null) {
			try {
				// Initialize date picker using database date string
				Date date = dateFormatter.parse(currentTask.getEndDate());
				Calendar cal = Calendar.getInstance();
				cal.setTime(date);
				endDatePicker.getModel().setDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
						cal.get(Calendar.DAY_OF_MONTH));

				// Select button and date picker
				enableEndDateButton.setSelected(true);
				endDatePicker.getModel().setSelected(true);
			} catch (ParseException ex) {
				System.out.println("Exception " + ex);
			}
		}

		// Add listener to enable end-date button
		enableEndDateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (enableEndDateButton.isSelected()) {
					endDatePicker.setTextEditable(true);
				} else {
					endDatePicker.setTextEditable(false);
				}
			}
		});
	}

	private void createColorSelector() {
		colorGroup = new ButtonGroup();
		int[] colorSelections = { 0x000000, 0xFF0000, 0x00FF00, 0x0000FF };
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
