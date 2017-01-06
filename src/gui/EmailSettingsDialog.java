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
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;

public class EmailSettingsDialog extends JDialog {
	private JButton okButton = new JButton("OK");
	private JButton cancelButton = new JButton("Cancel");

	// Private instance variables
	private JSpinner portSpinner;
	private JTextField userName;
	private JPasswordField password;

	// Label variables
	private JLabel userNameLabel = new JLabel("Username: ");
	private JLabel passwordLabel = new JLabel("Password: ");
	private JLabel portLabel = new JLabel("Port: ");

	// Dialog panels
	private JPanel controlsPanel;
	private JPanel buttonsPanel;
	private EmailSettingsEvent dialogResponse;

	// Constructor for Email Settings
	public EmailSettingsDialog(JDialog parent) {
		super(parent, "Configure Email SENDER...", true);
		
		portSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 9999, 1));
		userName = new JTextField(30);
		password = new JPasswordField(30);
		password.setEchoChar('*');

		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String user = userName.getText();
				char[] pw = password.getPassword();
				Integer port = (Integer) portSpinner.getValue();
				
				// Create event and set response
				EmailSettingsEvent ev = new EmailSettingsEvent(this, user, new String(pw), port);
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
		setLayout();
		setSize(500, 180);
		setVisible(true);
	}

	public EmailSettingsEvent getDialogResponse() {
		return dialogResponse;
	}

	private void setLayout() {
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
		addRowToControlPanel(gc, userNameLabel, userName, gridY++);
		addRowToControlPanel(gc, passwordLabel, password, gridY++);
		addRowToControlPanel(gc, portLabel, portSpinner, gridY++);

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
}
