package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Time;
import java.util.Calendar;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import model.PersonByTaskModel;

public class PersonTableDialog extends JDialog {
	private static final int PREF_DIALOG_WIDTH = 800;
	private static final int PREF_DIALOG_HEIGHT = 300;

	private static final int ADD_PERSON_BUTTON = 0;
	private static final int EMAIL_BUTTON = 1;
	private static final int CLOSE_BUTTON = 2;
	private static final int EDIT_ROW_BUTTON = 3;
	private static final int DELETE_ROW_BUTTON = 4;

	private static final int ROW_GAP = 5;

	private boolean isColumnExpanded;
	private JPanel tablePanel;
	private JTable table;
	private PersonTableModel tableModel;
	private JPopupMenu popup;
	private JMenuItem removeItem;
	private JMenuItem editItem;
	private LinkedList<PersonByTaskModel> personList;

	private String addButtonText;

	private JList<String> allPersons;
	private JList<Time> allTimes;
	private Calendar calendar;
	private PersonTableEvent dialogResponse;

	public PersonTableDialog(JFrame parent, String title, boolean isColumnExpanded,
			LinkedList<PersonByTaskModel> personList, String addButtonText, Calendar calendar, JList<String> allPersons,
			JList<Time> allTimes) {
		super(parent, true);
		setTitle(title);
		this.addButtonText = addButtonText;
		this.isColumnExpanded = isColumnExpanded;
		this.personList = personList;
		this.allPersons = allPersons;
		this.allTimes = allTimes;
		this.calendar = calendar;

		tablePanel = createPersonTablePanel();

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

	private JPanel createButtonPanel() {
		JPanel panel = new JPanel();

		if (!addButtonText.equals("")) {
			// Button used for both adding task sub and floater
			JButton addPersonButton = new JButton(addButtonText);
			panel.add(addPersonButton);

			addPersonButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (isColumnExpanded) {
						// Adding floater
						FloaterDialog floaterEvent = new FloaterDialog(PersonTableDialog.this, calendar, allPersons,
								allTimes);
						FloaterEvent floaterResponse = floaterEvent.getDialogResponse();

						if (floaterResponse != null) {
							PersonTableEvent ev = new PersonTableEvent(this, ADD_PERSON_BUTTON, 0,
									floaterResponse.getPersonName(), floaterResponse.getCalendar(),
									floaterResponse.getColor());
							dialogResponse = ev;
						}
					} else {
						// Adding task substitute
						PersonTableEvent ev = new PersonTableEvent(this, ADD_PERSON_BUTTON, 0, null, null, 0);
						dialogResponse = ev;
					}
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
				PersonTableEvent ev = new PersonTableEvent(this, CLOSE_BUTTON, 0, null, null, 0);
				dialogResponse = ev;
				setVisible(false);
				dispose();
			}
		});

		return panel;
	}

	private JPanel createPersonTablePanel() {
		JPanel panel = new JPanel();
		tableModel = new PersonTableModel(isColumnExpanded, personList);
		table = new JTable(tableModel);

		table.setFont(new Font("Serif", Font.PLAIN, 14));
		table.getTableHeader().setFont(new Font("Serif", Font.BOLD, 16));
		table.setRowHeight(table.getRowHeight() + ROW_GAP);
		table.getColumnModel().getColumn(tableModel.getColumnForLeader()).setMaxWidth(35);
		table.getColumnModel().getColumn(tableModel.getColumnForPhone()).setMaxWidth(100);
		table.getColumnModel().getColumn(tableModel.getColumnForPhone()).setPreferredWidth(95);
		if (isColumnExpanded) {
			table.getColumnModel().getColumn(tableModel.getColumnForSub()).setMaxWidth(35);
			table.getColumnModel().getColumn(tableModel.getColumnForTime()).setMaxWidth(75);
		}
		table.setDefaultRenderer(Object.class, new PersonTableRenderer());
		table.setAutoCreateRowSorter(true);

		popup = new JPopupMenu();
		removeItem = new JMenuItem("Delete row");
		editItem = new JMenuItem("Edit row");
		popup.add(removeItem);
		popup.add(editItem);

		// Detect right mouse click on table, then pop-up "Delete/Edit row"
		// and select row
		table.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3) {
					popup.show(table, e.getX(), e.getY());
					int row = table.rowAtPoint(e.getPoint());
					table.getSelectionModel().setSelectionInterval(row, row);
				}
			}
		});

		// When "Delete row" selected, then trigger PersonTableListener action
		// for this row
		removeItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				PersonTableEvent ev = new PersonTableEvent(this, DELETE_ROW_BUTTON, table.getSelectedRow(), null, null,
						0);
				dialogResponse = ev;
				setVisible(false);
				dispose();
			}
		});

		// When "Edit row" selected, then trigger PersonTableListener action
		// for this row
		editItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				PersonTableEvent ev = new PersonTableEvent(this, EDIT_ROW_BUTTON, 0,
						(String) tableModel.getValueAt(table.getSelectedRow(), tableModel.getColumnForPersonName()),
						null, 0);
				dialogResponse = ev;
				setVisible(false);
				dispose();
			}
		});

		panel.setLayout(new BorderLayout());
		panel.add(new JScrollPane(table), BorderLayout.CENTER);
		return panel;
	}

	public void setData(LinkedList<PersonByTaskModel> db) {
		personList = db;
		tableModel.setData(db);
		tableModel.fireTableDataChanged();
	}

	public class PersonTableRenderer extends JLabel implements TableCellRenderer {
		private PersonTableRenderer() {
			super();
			super.setOpaque(true);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {

			if (value instanceof String)
				setText((String) value);
			else if (value instanceof Time)
				setText((String) value.toString());

			if (column != -1) {
				Color textColor = Color.black;
				if (column == tableModel.getColumnForTaskName()) {
					PersonByTaskModel person = personList.get(row);
					if (person.getTask() != null)
						textColor = new Color(person.getTask().getColor());
					else
						textColor = new Color(person.getTaskColor());
				}
				super.setForeground(textColor);

				if (isSelected)
					super.setBackground(new Color(0xDDDDDD));
				else
					super.setBackground(Color.WHITE);

				if (column == tableModel.getColumnForPersonName())
					super.setHorizontalAlignment(LEFT);
				else
					super.setHorizontalAlignment(CENTER);
			}
			return this;
		}
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
