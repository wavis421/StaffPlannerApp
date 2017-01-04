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
import java.util.Calendar;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.Border;

import utilities.Utilities;

public class FloaterDialog extends JDialog {
	private static final int TEXT_FIELD_WIDTH = 30;
	private static final int TEXT_FIELD_HEIGHT = 20;
	private JButton okButton = new JButton("OK");
	private JButton cancelButton = new JButton("Cancel");

	// Private instance variables
	private JList<String> personsList;
	private JComboBox<String> personCombo;
	private JList<Time> timesList;
	private JComboBox<String> timeCombo;
	private Calendar calendar;
	private JPanel colorPanel = new JPanel();
	private ButtonGroup colorGroup;

	// Label variables
	private JLabel personLabel = new JLabel("Select Floater: ");
	private JLabel timeLabel = new JLabel("Select Time: ");
	private JLabel colorChooserLabel = new JLabel("Select Color: ");

	// Dialog panels
	private JPanel controlsPanel;
	private JPanel buttonsPanel;
	private FloaterEvent dialogResponse;

	// Constructor for adding floater
	public FloaterDialog(JDialog parent, String title, Calendar date, JList<String> personsList, JList<Time> timesList) {
		super(parent, title, true);

		this.calendar = date;
		this.personsList = personsList;
		this.timesList = timesList;

		setupFloaterDialog();
	}

	private void setupFloaterDialog() {
		createColorSelector();
		createPersonsComboBox(personsList);
		createTimesComboBox(timesList);

		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Add selected time to calendar
				int idx = timeCombo.getSelectedIndex();
				Utilities.addTimeToCalendar(calendar, timesList.getModel().getElementAt(idx));

				// Create FloaterEvent and set response
				FloaterEvent ev = new FloaterEvent(this, personCombo.getSelectedItem().toString(), calendar,
						Integer.parseInt(colorGroup.getSelection().getActionCommand()));
				dialogResponse = ev;

				setVisible(false);
				dispose();
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
		setSize(550, 250);
		setVisible(true);
	}

	public FloaterEvent getDialogResponse() {
		return dialogResponse;
	}

	private void setTaskLayout() {
		int gridY = 0;
		controlsPanel = new JPanel();
		buttonsPanel = new JPanel();

		controlsPanel.setLayout(new GridBagLayout());
		buttonsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

		Border etchedBorder = BorderFactory.createEtchedBorder();
		Border spaceBorder = BorderFactory.createEmptyBorder(15, 15, 15, 15);
		controlsPanel.setBorder(BorderFactory.createCompoundBorder(spaceBorder, etchedBorder));

		GridBagConstraints gc = new GridBagConstraints();

		gc.weightx = gc.weighty = 1;
		gc.fill = GridBagConstraints.NONE;

		// Add task name, location, time, DOW/WOM selectors and color picker
		addRowToControlPanel(gc, personLabel, personCombo, gridY++);
		addRowToControlPanel(gc, timeLabel, timeCombo, gridY++);
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

	private void createPersonsComboBox(JList<String> persons) {
		DefaultComboBoxModel<String> personModel = new DefaultComboBoxModel<String>();

		for (int i = 0; i < persons.getModel().getSize(); i++)
			personModel.addElement(persons.getModel().getElementAt(i));

		personCombo = new JComboBox<String>(personModel);
		personCombo.setSelectedIndex(0);
		personCombo.setBorder(BorderFactory.createEtchedBorder());
		personCombo.setMaximumSize(new Dimension(TEXT_FIELD_WIDTH, TEXT_FIELD_HEIGHT));
	}

	private void createTimesComboBox(JList<Time> times) {
		DefaultComboBoxModel<String> timeModel = new DefaultComboBoxModel<String>();

		for (int i = 0; i < times.getModel().getSize(); i++) {
			String timeString = Utilities.formatTime(times.getModel().getElementAt(i));
			timeModel.addElement(timeString);
		}

		timeCombo = new JComboBox<String>(timeModel);
		timeCombo.setSelectedIndex(0);
		timeCombo.setBorder(BorderFactory.createEtchedBorder());
		timeCombo.setMaximumSize(new Dimension(TEXT_FIELD_WIDTH, TEXT_FIELD_HEIGHT));
	}

	private void createColorSelector() {
		colorGroup = new ButtonGroup();
		int[] colorSelections = { 0x000000, // Black
				0xDC143C, // Crimson
				0xF28500, // Tangerine
				0x008000, // Green
				0x7CFC00, // Lawn green
				0x003399, // Dark powder blue
				0x5082B6, // Moderate blue
				0x8B008B, // Dark magenta
				0x966FD6, // Dark Pastel purple
				0x988344, // Dark Khaki
				0x7D7D7D // Dark gray
		};
		JRadioButton[] buttons = new JRadioButton[colorSelections.length];

		for (int idx = 0; idx < colorSelections.length; idx++) {
			buttons[idx] = new JRadioButton();

			buttons[idx].setBackground(Color.decode(Integer.toString(colorSelections[idx])));
			buttons[idx].setActionCommand(Integer.toString(colorSelections[idx]));

			colorGroup.add(buttons[idx]);
			colorPanel.add(buttons[idx]);
		}

		// Highlight default color BLACK
		buttons[0].setSelected(true);
	}
}
