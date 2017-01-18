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

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.Border;

import model.TaskModel;

public class AssignTaskDialog extends JDialog {
	private JButton okButton = new JButton("OK");
	private JButton cancelButton = new JButton("Cancel");

	// Private instance variables
	private JRadioButton[] dayOfWeekButtons = new JRadioButton[7];
	private JRadioButton[] weekOfMonthButtons = new JRadioButton[5];

	// Label variables
	private JLabel dayOfWeekLabel = new JLabel("Days of the Week: ");
	private JLabel weekOfMonthLabel = new JLabel("Weeks of the Month:");

	// Dialog panels
	private JPanel controlsPanel;
	private JPanel buttonsPanel;
	private JPanel dayOfWeekPanel;
	private JPanel weekOfMonthPanel;
	private AssignTaskEvent dialogResponse;

	// Track current program and task
	private String programName;
	private TaskModel task;

	public AssignTaskDialog(JDialog parent, AssignTaskEvent taskEvent) {
		super(parent, "Assign task...", true);
		// setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
		// setModalityType(Dialog.DEFAULT_MODALITY_TYPE.APPLICATION_MODAL);

		initAssignTaskDialog(taskEvent.getProgramName(), taskEvent.getTask(), taskEvent.getDaysOfWeek(),
				taskEvent.getWeeksOfMonth());
	}

	public AssignTaskDialog(JDialog parent, String programName, TaskModel task) {
		super(parent, "Assign task...", true);
		// setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
		// setModalityType(Dialog.DEFAULT_MODALITY_TYPE.APPLICATION_MODAL);

		boolean[] daysOfWeek = { false, false, false, false, false, false, false };
		boolean[] weeksOfMonth = { false, false, false, false, false };

		initAssignTaskDialog(programName, task, daysOfWeek, weeksOfMonth);
	}

	public AssignTaskEvent getDialogResponse() {
		return dialogResponse;
	}

	private void initAssignTaskDialog(String programName, TaskModel task, boolean[] daysOfWeek,
			boolean[] weeksOfMonth) {
		this.programName = programName;
		this.task = task;

		createDayOfWeekButtons(daysOfWeek);
		createWeekOfMonthButtons(weeksOfMonth);

		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
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
					AssignTaskEvent ev = new AssignTaskEvent(this, programName, task, daysOfWeekSelected,
							weeksOfMonthSelected);
					dialogResponse = ev;
					setVisible(false);
					dispose();

				} catch (IllegalArgumentException ev) {
					JOptionPane.showMessageDialog(okButton, "TBD Exception");
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
		setLayout();
		setSize(580, 250);
		setVisible(true);
	}

	private void setLayout() {
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
		Border bevelBorder = BorderFactory.createTitledBorder(programName + ": " + task.getTaskName());
		Border spaceBorder = BorderFactory.createEmptyBorder(15, 15, 15, 15);
		controlsPanel.setBorder(BorderFactory.createCompoundBorder(spaceBorder, bevelBorder));
		dayOfWeekPanel.setBorder(BorderFactory.createEtchedBorder());
		womSubPanel.setBorder(BorderFactory.createEtchedBorder());

		GridBagConstraints gc = new GridBagConstraints();

		gc.weightx = gc.weighty = 1;
		gc.fill = GridBagConstraints.NONE;

		// Add DOW/WOM selectors
		addRowToControlPanel(gc, dayOfWeekLabel, dayOfWeekPanel, gridY++);
		addRowToControlPanel(gc, weekOfMonthLabel, weekOfMonthPanel, gridY++);

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

	private void createDayOfWeekButtons(boolean[] currentDayOfWeek) {
		String[] dayName = { "Sun", "Mon", "Tue", "Wed", "Th", "Fri", "Sat" };
		int selectedCount = 0, selectedIdx = 0;

		for (int i = 0; i < dayOfWeekButtons.length; i++) {
			dayOfWeekButtons[i] = new JRadioButton(dayName[i]);
			if (task.getDayOfWeek()[i] == false)
				dayOfWeekButtons[i].setEnabled(false);
			else {
				selectedCount++;
				selectedIdx = i;
				dayOfWeekButtons[i].setSelected(currentDayOfWeek[i]);
			}
		}

		// If only 1 button is enabled, then select it
		if (selectedCount == 1) {
			dayOfWeekButtons[selectedIdx].doClick();
		}
	}

	private void createWeekOfMonthButtons(boolean[] currentWeekOfMonth) {
		String[] weekName = { "1st", "2nd", "3rd", "4th", "5th" };
		int selectedCount = 0, selectedIdx = 0;

		for (int i = 0; i < weekOfMonthButtons.length; i++) {
			weekOfMonthButtons[i] = new JRadioButton(weekName[i]);
			if (task.getWeekOfMonth()[i] == false)
				weekOfMonthButtons[i].setEnabled(false);
			else {
				selectedCount++;
				selectedIdx = i;
				weekOfMonthButtons[i].setSelected(currentWeekOfMonth[i]);
			}
		}
		
		// If only 1 button is enabled, then select it
		if (selectedCount == 1) {
			weekOfMonthButtons[selectedIdx].doClick();
		}
	}
}
