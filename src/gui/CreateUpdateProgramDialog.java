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

public class CreateUpdateProgramDialog extends JDialog {
	private JButton okButton = new JButton("OK");
	private JButton cancelButton = new JButton("Cancel");
	
	// Private instance variables
	private JTextField programName = new JTextField(20);
	private JLabel programLabel = new JLabel ("Program name: ");
	private JRadioButton enableEndDateButton = new JRadioButton("Set end-date ");
	private JDatePickerImpl endDatePicker;
	
	// Dialog panels
	private JPanel controlsPanel;
	private JPanel buttonsPanel;
	private ProgramEvent dialogResponse;
	
	// Track last program edited
	private static ProgramEvent lastProgram;
	
	public CreateUpdateProgramDialog(JFrame parent) {
		super(parent, "Create program...", true);
		setupProgramDialog();
	}
	
	public ProgramEvent getDialogResponse() {
		return dialogResponse;
	}
	
	private void setupProgramDialog() {
		createEndDatePicker();
		
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					// Make sure that program name has been entered
					if (programName.getText().equals("")) {
						JOptionPane.showMessageDialog(okButton, "Program name field is required");
					} else {
						ProgramEvent ev = new ProgramEvent(this, programName.getText(), endDatePicker.getJFormattedTextField().getText(), 0);
						dialogResponse = lastProgram = ev;
						setVisible(false);
						dispose();
					}

				} catch (IllegalArgumentException ev) {
					JOptionPane.showMessageDialog(okButton, "TBD exception message");
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
		setProgramLayout();
		setSize(450, 180);
		setVisible(true);
	}
	
	private void setProgramLayout() {
		int gridY = 0;
		controlsPanel = new JPanel();
		buttonsPanel = new JPanel();

		controlsPanel.setLayout(new GridBagLayout());
		buttonsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

		// controlsPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		Border titleBorder = BorderFactory.createRaisedSoftBevelBorder();
		Border spaceBorder = BorderFactory.createEmptyBorder(15, 15, 15, 15);
		controlsPanel.setBorder(BorderFactory.createCompoundBorder(spaceBorder, titleBorder));

		GridBagConstraints gc = new GridBagConstraints();

		gc.weightx = gc.weighty = 1;
		gc.fill = GridBagConstraints.NONE;

		// Add program name and end-date rows
		addRowToControlPanel(gc, programLabel, programName, gridY++);
		addRowToControlPanel(gc, enableEndDateButton, endDatePicker, gridY++);

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

		if (lastProgram != null && lastProgram.getEndDate() != null) {
			try {
				// Initialize date picker using database date string
				Date date = dateFormatter.parse(lastProgram.getEndDate());
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
}
