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

	public PersonTableFrame (PersonTableModel model) {
		setTitle("Staff/Volunteers");
		this.tableModel = model;
		this.tablePanel = new PersonTablePanel(tableModel);
	
		setLayout (new BorderLayout());
		JPanel buttonPanel = createButtonPanel ();
		add(tablePanel, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);

		pack();
		setVisible(true);
		setSize(PREF_FRAME_WIDTH, PREF_FRAME_HEIGHT);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}
	
	public void setTableListener (PersonTableListener listener)
	{
		this.tableListener = listener;
	}
	
	public void setData(LinkedList<PersonModel> db) {
		tablePanel.setData(db);
	}
	
	public void tableRefresh() {
		tablePanel.refresh();
	}
	
	private JPanel createButtonPanel() {
		JPanel panel = new JPanel();
		JButton closeButton = new JButton("Close");
		JButton refreshButton = new JButton("Refresh");
		
		panel.add(refreshButton);
		panel.add(closeButton);
		
		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		
		refreshButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (tableListener != null) {
					tableListener.refresh();
				}
			}
		});
		
		return panel;
	}
}
