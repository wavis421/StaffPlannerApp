package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.Border;

public class SelectActiveProgramDialog extends JDialog {
	private JButton okButton = new JButton("OK");
	private JButton cancelButton = new JButton("Cancel");

	// Dialog panels
	private JPanel controlsPanel = new JPanel();
	private JPanel buttonsPanel = new JPanel();
	private JList<String> programList;
	private JRadioButton[] buttonList;
	private ButtonGroup programGroup = new ButtonGroup();
	private String dialogResponse;

	public SelectActiveProgramDialog(JFrame parent, JList<String> programList) {
		super(parent, "Select active program...", true);
		this.programList = programList;

		// Set layout for control and button panels
		int numRows = programList.getModel().getSize();
		controlsPanel.setLayout(new GridLayout(numRows, 1));
		buttonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

		// Create check boxes for control panel
		buttonList = new JRadioButton[numRows];
		for (int i = 0; i < numRows; i++) {
			buttonList[i] = new JRadioButton(programList.getModel().getElementAt(i));
			programGroup.add(buttonList[i]);
			controlsPanel.add(buttonList[i]);
		}

		// Create buttons panel
		buttonsPanel.add(okButton);
		buttonsPanel.add(cancelButton);

		setupButtonsListener();

		// Layout
		setFilterLayout();

		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setSize(300, 120 + (20 * numRows));
		setVisible(true);
	}

	public String getDialogResponse() {
		return dialogResponse;
	}
	
	private void setupButtonsListener() { 
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				for (int i = 0; i < buttonList.length; i++) {
					if (buttonList[i].isSelected()) {
						dialogResponse = buttonList[i].getText();
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
	}

	private void setFilterLayout() {
		Border etchedBorder = BorderFactory.createEtchedBorder();
		Border spaceBorder = BorderFactory.createEmptyBorder(15, 15, 15, 15);
		controlsPanel.setBorder(BorderFactory.createCompoundBorder(spaceBorder, etchedBorder));

		// Add to dialog
		setLayout(new BorderLayout());
		add(controlsPanel, BorderLayout.CENTER);
		add(buttonsPanel, BorderLayout.SOUTH);

		// make OK & cancel buttons the same size
		Dimension btnSize = cancelButton.getPreferredSize();
		okButton.setPreferredSize(btnSize);
	}
}
