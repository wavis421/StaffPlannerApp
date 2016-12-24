package gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import model.PersonByTaskModel;

public class PersonTableDialog extends JDialog {
	private static final int PREF_DIALOG_WIDTH = 800;
	private static final int PREF_DIALOG_HEIGHT = 300;
	
	private static final int ADD_PERSON_BUTTON = 0;
	private static final int EMAIL_BUTTON = 1;
	private static final int CLOSE_BUTTON = 2;
	private static final int EDIT_ROW_BUTTON = 3;
	private static final int DELETE_ROW_BUTTON = 4;

	private PersonTablePanel tablePanel;
	private String addButtonText;

	private PersonTableEvent dialogResponse;

	public PersonTableDialog(JFrame parent, String title, boolean isColumnExpanded,
			LinkedList<PersonByTaskModel> personList, String addButtonText) {
		super(parent, true);
		setTitle(title);
		this.tablePanel = new PersonTablePanel(isColumnExpanded, personList);
		this.addButtonText = addButtonText;
		
		// Add listeners for edit & delete row
		PersonTableListener tableListener = new PersonTableListener () {
			@Override
			public void rowDeleted(int row) {
				PersonTableEvent ev = new PersonTableEvent(this, DELETE_ROW_BUTTON, row, null);
				dialogResponse = ev;
				setVisible(false);
				dispose();
			}
			
			@Override
			public void editRow(String personName) {
				PersonTableEvent ev = new PersonTableEvent(this, EDIT_ROW_BUTTON, 0, personName);
				dialogResponse = ev;
				setVisible(false);
				dispose();
			}
		};
		this.tablePanel.setPersonTableListener(tableListener);

		setLayout(new BorderLayout());
		JPanel buttonPanel = createButtonPanel();
		add(tablePanel, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);

		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setSize(PREF_DIALOG_WIDTH, PREF_DIALOG_HEIGHT);
		setVisible(true);
	}

	public PersonTableEvent getDialogResponse() {
		return dialogResponse;
	}

	public void setData(LinkedList<PersonByTaskModel> db) {
		tablePanel.setData(db);
	}

	private JPanel createButtonPanel() {
		JPanel panel = new JPanel();

		if (!addButtonText.equals("")) {
			JButton addPersonButton = new JButton(addButtonText);
			panel.add(addPersonButton);

			addPersonButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					PersonTableEvent ev = new PersonTableEvent(this, ADD_PERSON_BUTTON, 0, null);
					dialogResponse = ev;
					setVisible(false);
					dispose();
				}
			});
		}

		JButton sendEmailButton = new JButton("Send email");
		JButton closeButton = new JButton("Close");

		panel.add(sendEmailButton);
		panel.add(closeButton);

		sendEmailButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Send email");
			}
		});

		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				PersonTableEvent ev = new PersonTableEvent (this, CLOSE_BUTTON, 0, null);
				dialogResponse = ev;
				setVisible(false);
				dispose();
			}
		});

		return panel;
	}

	public static int getAddPersonButtonId() {
		return ADD_PERSON_BUTTON;
	}

	public static int getEmailButtonId() {
		return EMAIL_BUTTON;
	}

	public static int getCloseButtonId() {
		return CLOSE_BUTTON;
	}
	
	public static int getEditRowButtonId() {
		return EDIT_ROW_BUTTON;
	}
	
	public static int getDeleteRowButtonId() {
		return DELETE_ROW_BUTTON;
	}
}
