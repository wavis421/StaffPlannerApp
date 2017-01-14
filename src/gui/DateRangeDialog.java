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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.Border;

import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

import model.DateRangeModel;

public class DateRangeDialog extends JDialog {
	private JButton okButton = new JButton("OK");
	private JButton cancelButton = new JButton("Cancel");

	// Private instance variables
	private JDatePickerImpl startDatePicker;
	private JDatePickerImpl endDatePicker;
	private JPanel datePanel = new JPanel();

	// Dialog panels
	private JPanel controlsPanel;
	private JPanel buttonsPanel;
	private DateRangeModel dialogResponse;

	public DateRangeDialog(JDialog parent) {
		super(parent, "Select unavailable date range...", true);
		setupDialog();
	}

	public DateRangeModel getDialogResponse() {
		return dialogResponse;
	}

	private void setupDialog() {
		startDatePicker = createDatePicker("start");
		endDatePicker = createDatePicker("end");

		datePanel.add(startDatePicker);
		datePanel.add(new JLabel(" to "));
		datePanel.add(endDatePicker);

		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					// Set up start/end dates
					String startDate = startDatePicker.getJFormattedTextField().getText();
					String endDate = endDatePicker.getJFormattedTextField().getText();

					if (!startDate.equals("") && !endDate.equals("")) {
						dialogResponse = new DateRangeModel(startDate, endDate);
						setVisible(false);
						dispose();
					}

				} catch (IllegalArgumentException ev) {
					JOptionPane.showMessageDialog(okButton, "Invalid date field: " + ev.getMessage(),
							"Date Parsing Exception", JOptionPane.WARNING_MESSAGE);
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
		setSize(550, 180);
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
		addRowToControlPanel(gc, new JLabel("Dates Unavailable: "), datePanel, gridY++);

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
		UtilDateModel dateModel = new UtilDateModel();
		Properties prop = new Properties();
		JDatePanelImpl datePanel;
		SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy");

		prop.put("text.today", "today");
		prop.put("text.month", "month");
		prop.put("text.year", "year");

		datePanel = new JDatePanelImpl(dateModel, prop);
		JDatePickerImpl datePicker = new JDatePickerImpl(datePanel, new DateLabelFormatter());

		// Add action listener
		datePicker.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String startText = startDatePicker.getJFormattedTextField().getText();
				String endText = endDatePicker.getJFormattedTextField().getText();

				// If end date is NULL, set it to start day
				if (name.equals("start") && !startText.equals("")) {
					if (endText.equals("")) {
						endDatePicker = setDate(startText, endDatePicker);
						endDatePicker.getJFormattedTextField().setText(startText);
						endText = startText;
					}
					startDatePicker.getModel().setSelected(true);
				}
				// If start date is NULL, set it to end day
				if (name.equals("end") && !endText.equals("")) {
					if (startText.equals("")) {
						startDatePicker = setDate(endText, startDatePicker);
						startDatePicker.getJFormattedTextField().setText(endText);
						startText = endText;
					}
					endDatePicker.getModel().setSelected(true);
				}

				if (!startText.equals("") && !endText.equals("")) {
					// If end date is before start date, set to start date
					try {
						Date startDate = dateFormatter.parse(startText);
						if (startDate.compareTo(dateFormatter.parse(endText)) > 0) {
							endDatePicker.getJFormattedTextField().setText(startText);
						}

					} catch (ParseException ex) {
						JOptionPane.showMessageDialog(null, "Error parsing date: " + ex.getMessage(),
								"Date Parsing Exception", JOptionPane.WARNING_MESSAGE);
					}
				}
			}
		});

		datePicker.setPreferredSize(new Dimension(150, 26));
		datePicker.setName(name);
		return datePicker;
	}

	private JDatePickerImpl setDate(String dateText, JDatePickerImpl datePicker) {
		try {
			SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy");
			Date date = dateFormatter.parse(dateText);
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			datePicker.getModel().setDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
					cal.get(Calendar.DAY_OF_MONTH));

		} catch (ParseException e) {
			JOptionPane.showMessageDialog(null, "Error formatting date: " + e.getMessage(), "Formatting Exception",
					JOptionPane.WARNING_MESSAGE);
		}
		return datePicker;
	}
}
