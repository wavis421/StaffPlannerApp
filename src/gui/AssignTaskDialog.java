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
	private JDialog parent;

	public AssignTaskDialog(JDialog parent, String programName, TaskModel task) {
		super(parent, "Assign task...", true);
		//setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
		//setModalityType(Dialog.DEFAULT_MODALITY_TYPE.APPLICATION_MODAL);
		
		this.programName = programName;
		this.task = task;
		this.parent = parent;

		createDayOfWeekButtons();
		createWeekOfMonthButtons();
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
						AssignTaskEvent ev = new AssignTaskEvent(this, programName, task.getTaskName(),
								daysOfWeekSelected, weeksOfMonthSelected);
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

	public AssignTaskEvent getDialogResponse() {
		return dialogResponse;
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
		for (int i = 0; i < weekOfMonthButtons.length; i++) {
			weekOfMonthPanel.add(weekOfMonthButtons[i]);
		}

		// controlsPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		Border bevelBorder = BorderFactory.createTitledBorder(programName + ": " + task.getTaskName());
		Border spaceBorder = BorderFactory.createEmptyBorder(15, 15, 15, 15);
		controlsPanel.setBorder(BorderFactory.createCompoundBorder(spaceBorder, bevelBorder));

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

	private void createDayOfWeekButtons() {
		String[] dayName = { "Sun", "Mon", "Tue", "Wed", "Th", "Fri", "Sat" };

		boolean[] currentDayOfWeek = task.getDayOfWeek();
		for (int i = 0; i < dayOfWeekButtons.length; i++) {
			dayOfWeekButtons[i] = new JRadioButton(dayName[i]);
			if (currentDayOfWeek[i] == false)
				dayOfWeekButtons[i].setEnabled(false);
			//dayOfWeekButtons[i].setSelected(currentDayOfWeek[i]);
		}
	}

	private void createWeekOfMonthButtons() {
		String[] weekName = { "1st", "2nd", "3rd", "4th", "5th" };

		boolean[] currentWeekOfMonth = task.getWeekOfMonth();
		for (int i = 0; i < weekOfMonthButtons.length; i++) {
			weekOfMonthButtons[i] = new JRadioButton(weekName[i]);
			if (currentWeekOfMonth[i] == false)
				weekOfMonthButtons[i].setEnabled(false);
			//weekOfMonthButtons[i].setSelected(currentWeekOfMonth[i]);
		}
	}
}
