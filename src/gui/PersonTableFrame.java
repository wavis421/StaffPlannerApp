package gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import model.PersonModel;

public class PersonTableFrame extends JFrame {
	private static final int PREF_FRAME_WIDTH = 800;
	private static final int PREF_FRAME_HEIGHT = 300;

	private PersonTableListener tableListener;
	private PersonTableModel tableModel;
	private PersonTablePanel tablePanel;

	public PersonTableFrame(String title, PersonTableModel model) {
		setTitle(title);
		this.tableModel = model;
		this.tablePanel = new PersonTablePanel(tableModel);

		setLayout(new BorderLayout());
		JPanel buttonPanel = createButtonPanel();
		add(tablePanel, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);

		pack();
		setVisible(true);
		setSize(PREF_FRAME_WIDTH, PREF_FRAME_HEIGHT);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}

	public void setTableListener(PersonTableListener listener) {
		this.tableListener = listener;
		tablePanel.setPersonTableListener(listener);
	}

	public void setData(LinkedList<PersonModel> db) {
		tablePanel.setData(db);
	}

	public void tableRefresh() {
		tablePanel.refresh();
	}

	private JPanel createButtonPanel() {
		JPanel panel = new JPanel();
		JButton addPersonButton = new JButton("Add person");
		JButton sendEmailButton = new JButton("Send email");
		JButton refreshButton = new JButton("Refresh");
		JButton closeButton = new JButton("Close");

		panel.add(addPersonButton);
		panel.add(sendEmailButton);
		panel.add(refreshButton);
		panel.add(closeButton);

		addPersonButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

			}
		});

		sendEmailButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

			}
		});

		refreshButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (tableListener != null) {
					tableListener.refresh();
				}
			}
		});

		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});

		return panel;
	}
}
