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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.Border;

import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilCalendarModel;

import model.TaskModel;
import utilities.Utilities;

public class DateRangeDialog extends JDialog {
	private JButton okButton = new JButton("OK");
	private JButton cancelButton = new JButton("Cancel");

	// Private instance variables
	private JDatePickerImpl startDatePicker;
	private JDatePickerImpl endDatePicker;
	private JPanel datePanel = new JPanel();
	private String dateLabelText;
	private ArrayList<TaskModel> allTasks;
	private JComboBox<TaskModel> taskCombo;

	// Dialog panels
	private JPanel controlsPanel;
	private JPanel buttonsPanel;
	private DateRangeEvent dialogResponse;

	public DateRangeDialog(JDialog parent, String title, String dateLabel, ArrayList<TaskModel> allTasks) {
		super(parent, title, true);
		this.dateLabelText = dateLabel;
		this.allTasks = allTasks;
		setupDialog();
	}

	public DateRangeEvent getDialogResponse() {
		return dialogResponse;
	}

	private void setupDialog() {
		startDatePicker = createDatePicker("start");
		endDatePicker = createDatePicker("end");
		datePanel.add(startDatePicker);

		if (allTasks == null) {
			// When all tasks null, then require a date range
			datePanel.add(new JLabel(" to "));
			datePanel.add(endDatePicker);

		} else {
			// Add task combo box
			DefaultComboBoxModel<String> taskModel = new DefaultComboBoxModel<String>();
			for (int i = 0; i < allTasks.size(); i++)
				taskModel.addElement(allTasks.get(i).toString());
			taskCombo = new JComboBox(taskModel);
			taskCombo.setEditable(false);
		}

		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Set up start/end dates
				String startDate = startDatePicker.getJFormattedTextField().getText();
				String endDate = endDatePicker.getJFormattedTextField().getText();

				if (!startDate.equals("") && !endDate.equals("")) {
					if (taskCombo == null) {
						// No task in dialog, so setting Unavailable Date range
						dialogResponse = new DateRangeEvent(DateRangeDialog.this, null,
								(Calendar) startDatePicker.getModel().getValue(),
								(Calendar) endDatePicker.getModel().getValue());
						setVisible(false);
						dispose();

					} else if (taskCombo.getSelectedItem() == null) {
						// Substitute needs a task, none selected
						JOptionPane.showMessageDialog(DateRangeDialog.this,
								"Please select which task to assign a substitute");

					} else {
						// Assigning substitute, task selected
						int idx = taskCombo.getSelectedIndex();
						dialogResponse = new DateRangeEvent(DateRangeDialog.this, (TaskModel) allTasks.get(idx),
								(Calendar) startDatePicker.getModel().getValue(), null);
						setVisible(false);
						dispose();
					}
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
		setSize(560, 180);
		setVisible(true);
	}

	private void setLayout() {
		int gridY = 0;
		controlsPanel = new JPanel();
		buttonsPanel = new JPanel();

		controlsPanel.setLayout(new GridBagLayout());
		buttonsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

		// controlsPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		Border etchedBorder = BorderFactory.createEtchedBorder();
		Border spaceBorder = BorderFactory.createEmptyBorder(15, 15, 15, 15);
		controlsPanel.setBorder(BorderFactory.createCompoundBorder(spaceBorder, etchedBorder));

		GridBagConstraints gc = new GridBagConstraints();

		gc.weightx = gc.weighty = 1;
		gc.fill = GridBagConstraints.NONE;

		// Add unavailable date row
		if (taskCombo != null)
			addRowToControlPanel(gc, new JLabel("Select task: "), taskCombo, gridY++);
		addRowToControlPanel(gc, new JLabel(dateLabelText), datePanel, gridY++);

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

	private JDatePickerImpl createDatePicker(String name) {
		UtilCalendarModel dateModel = new UtilCalendarModel();
		Properties prop = new Properties();
		JDatePanelImpl datePickerPanel;

		prop.put("text.today", "today");
		prop.put("text.month", "month");
		prop.put("text.year", "year");

		datePickerPanel = new JDatePanelImpl(dateModel, prop);
		JDatePickerImpl datePicker = new JDatePickerImpl(datePickerPanel, new DateLabelFormatter());

		// Add action listener
		datePicker.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Utilities.checkStartEndDatePicker(name, startDatePicker, endDatePicker);
			}
		});

		datePicker.setPreferredSize(new Dimension(150, 26));
		datePicker.setName(name);
		return datePicker;
	}
}
