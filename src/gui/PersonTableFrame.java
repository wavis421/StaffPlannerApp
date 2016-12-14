package gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import model.PersonByTaskModel;

public class PersonTableFrame extends JFrame {
	private static final int PREF_FRAME_WIDTH = 800;
	private static final int PREF_FRAME_HEIGHT = 300;

	private PersonTableListener tableListener;
	private PersonTableModel tableModel;
	private PersonTablePanel tablePanel;
	private String addButtonText;

	public PersonTableFrame(String title, PersonTableModel model, String addButtonText) {
		setTitle(title);
		this.tableModel = model;
		this.tablePanel = new PersonTablePanel(tableModel);
		this.addButtonText = addButtonText;

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

	public void setData(LinkedList<PersonByTaskModel> db) {
		tablePanel.setData(db);
	}

	public void tableRefresh() {
		tablePanel.refresh();
	}

	private JPanel createButtonPanel() {
		JPanel panel = new JPanel();

		if (!addButtonText.equals("")) {
			JButton addPersonButton = new JButton(addButtonText);
			panel.add(addPersonButton);

			addPersonButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (tableListener != null) {
						tableListener.addPerson();
					}
				}
			});
		}

		JButton sendEmailButton = new JButton("Send email");
		JButton refreshButton = new JButton("Refresh");
		JButton closeButton = new JButton("Close");

		panel.add(sendEmailButton);
		panel.add(refreshButton);
		panel.add(closeButton);

		sendEmailButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (tableListener != null) {
					tableListener.addPerson();
				}
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
