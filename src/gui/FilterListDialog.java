package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.Border;

public class FilterListDialog extends JDialog {
	private JButton okButton = new JButton("OK");
	private JButton cancelButton = new JButton("Cancel");
	private JScrollPane scrollPane;

	// Dialog panels
	private JPanel controlsPanel = new JPanel();
	private JPanel buttonsPanel = new JPanel();
	private JList<JCheckBox> cbList = new JList<JCheckBox>();
	private ArrayList<String> dialogResponse = null;

	public FilterListDialog(JFrame parent, String filterName, ArrayList<String> filterList) {
		super(parent, filterName + "...", true);
		DefaultListModel<JCheckBox> filterModel = new DefaultListModel<JCheckBox>();

		// Set layout for control and button panels
		int numRows = filterList.size();
		controlsPanel.setLayout(new GridLayout(numRows, 1));
		buttonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

		// Create check boxes for control panel
		for (int i = 0; i < numRows; i++) {
			JCheckBox c = new JCheckBox(filterList.get(i), false);
			filterModel.addElement(c);
			controlsPanel.add(c);
		}
		cbList = new JList<JCheckBox>(filterModel);
		scrollPane = new JScrollPane(controlsPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);

		// Create buttons panel
		buttonsPanel.add(okButton);
		buttonsPanel.add(cancelButton);
		setupButtonsListener();

		// Layout
		setFilterLayout();
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		if (numRows <= 16)
			setSize(350, 120 + (25 * numRows));
		else
			setSize(350, 545); // Maximum size
		setVisible(true);
	}

	private void setupButtonsListener() {
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				int numItems = cbList.getModel().getSize();
				if (numItems > 0) {
					ArrayList<String> responseList = new ArrayList<String>();
					for (int i = 0; i < numItems; i++) {
						if (cbList.getModel().getElementAt(i).isSelected())
							responseList.add(new String(cbList.getModel().getElementAt(i).getText()));
					}
					if (responseList.size() > 0)
						dialogResponse = responseList;
				}
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
	}

	private void setFilterLayout() {
		Border etchedBorder = BorderFactory.createEtchedBorder();
		Border spaceBorder = BorderFactory.createEmptyBorder(15, 15, 15, 15);
		scrollPane.setBorder(BorderFactory.createCompoundBorder(spaceBorder, etchedBorder));

		// Add to dialog
		setLayout(new BorderLayout());
		add(scrollPane, BorderLayout.CENTER);
		add(buttonsPanel, BorderLayout.SOUTH);

		// make OK & cancel buttons the same size
		Dimension btnSize = cancelButton.getPreferredSize();
		okButton.setPreferredSize(btnSize);
	}

	public ArrayList<String> getDialogResponse() {
		return dialogResponse;
	}
}
