package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Calendar;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import model.PersonByTaskModel;
import model.TimeModel;
import utilities.Utilities;

public class PersonTableDialog extends JDialog {
	private static final int PREF_DIALOG_WIDTH = 800;
	private static final int PREF_DIALOG_HEIGHT = 300;

	private static final int ADD_PERSON_BUTTON = 0;
	private static final int EMAIL_BUTTON = 1;
	private static final int CLOSE_BUTTON = 2;
	private static final int EDIT_PERSON_ROW_BUTTON = 3;
	private static final int REMOVE_PERSON_ROW_BUTTON = 4;

	private static final int ROW_GAP = 5;

	private int columnExpansionLevel;
	private JPanel tablePanel;
	private JTable table;
	private PersonTableModel tableModel;
	private JPopupMenu popup;
	private JMenuItem removeItem;
	private JMenuItem editItem;
	private ArrayList<PersonByTaskModel> personList;
	private ArrayList<PersonByTaskModel> fullList = null;
	private String taskName;

	private String addButtonText;

	private ArrayList<String> allPersons;
	private ArrayList<TimeModel> allTimes;
	private Calendar calendar;
	private String conflictingTask = null;
	private PersonTableEvent dialogResponse;
	private JFrame parent;
	private JDialog child;

	public PersonTableDialog(JFrame parent, String title, int columnExpansionLevel, String taskName,
			ArrayList<PersonByTaskModel> personList, String addButtonText, Calendar calendar,
			ArrayList<String> allPersons, ArrayList<TimeModel> allTimes) {
		super(parent, true);
		setLocation(new Point(100, 100));

		setTitle(title);
		this.parent = parent;
		this.child = this;
		this.addButtonText = addButtonText;
		this.columnExpansionLevel = columnExpansionLevel;
		this.taskName = taskName;

		// Floaters have null task; if task defined, create a sub-list with
		// matching task
		if (taskName == null)
			this.personList = personList;
		else {
			this.fullList = personList;
			this.personList = createPersonListByTask(taskName, fullList);
		}

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
					if (addButtonText.equals("Add floater")) {
						// Adding floater
						if (allPersons.size() == 0) {
							// No persons available
							JOptionPane.showMessageDialog(PersonTableDialog.this,
									"No persons are available on " + Utilities.getDisplayDate(calendar));
							return;
						}
						FloaterDialog floaterEvent;
						if (allTimes.size() > 1)
							floaterEvent = new FloaterDialog(PersonTableDialog.this,
									"Add floater for " + Utilities.getDisplayDate(calendar), calendar, allPersons,
									allTimes);
						else
							floaterEvent = new FloaterDialog(PersonTableDialog.this, "Add floater for "
									+ Utilities.getDisplayDate(calendar) + " at " + Utilities.formatTime(calendar),
									calendar, allPersons, allTimes);
						FloaterEvent floaterResponse = floaterEvent.getDialogResponse();

						if (floaterResponse != null) {
							if (!isTimeAlreadyAssigned(floaterResponse.getPersonName(),
									floaterResponse.getCalendar())) {
								// New time for this person, create event
								PersonTableEvent ev = new PersonTableEvent(this, ADD_PERSON_BUTTON,
										floaterResponse.getPersonName(), floaterResponse.getCalendar(),
										floaterResponse.getColor());
								dialogResponse = ev;
								setVisible(false);
								dispose();
							}
						}
					} else if (addButtonText.equals("Add sub")) {
						// Adding task substitute
						FilterListDialog ev1 = new FilterListDialog(parent,
								"Assign person(s) to " + taskName + " on " + Utilities.getDisplayDate(calendar),
								allPersons);
						ArrayList<String> filterListResponse = ev1.getDialogResponse();

						if (filterListResponse != null && filterListResponse.size() > 0) {
							if (!isPersonAlreadyAssigned(filterListResponse, calendar, taskName)) {
								// New time for this person, create event
								PersonTableEvent ev = new PersonTableEvent(this, ADD_PERSON_BUTTON, filterListResponse,
										calendar, 0);
								dialogResponse = ev;
								setVisible(false);
								dispose();
							}
						}
					} else if (columnExpansionLevel == PersonTableModel.getExpansionByTask()) {
						// Adding person to task
						FilterListDialog ev1 = new FilterListDialog(parent, "Assign person(s) to " + taskName,
								allPersons);
						ArrayList<String> filterListResponse = ev1.getDialogResponse();

						if (filterListResponse != null && filterListResponse.size() > 0) {
							PersonTableEvent ev = new PersonTableEvent(this, ADD_PERSON_BUTTON, filterListResponse,
									null, 0);
							dialogResponse = ev;
							setVisible(false);
							dispose();
						}
					} else {
						// Adding new person
						PersonTableEvent ev = new PersonTableEvent(this, ADD_PERSON_BUTTON, (String) null, null, 0);
						dialogResponse = ev;
						setVisible(false);
						dispose();
					}
				}
			});
		}

		JButton selectionButton = new JButton("Select all");
		JButton sendEmailButton = new JButton("Send email");
		JButton closeButton = new JButton("Close");

		panel.add(selectionButton);
		panel.add(sendEmailButton);
		panel.add(closeButton);

		selectionButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (selectionButton.getText().equals("Select all")) {
					selectionButton.setText("Select none");
					table.selectAll();
				} else {
					selectionButton.setText("Select all");
					table.clearSelection();
				}
			}
		});

		sendEmailButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (table.getSelectedRowCount() == 0)
					JOptionPane.showMessageDialog(child, "Please first select email recipients.");
				else {
					DefaultListModel<String> emailModel = new DefaultListModel<String>();
					int[] selectedRows = table.getSelectedRows();

					for (int i = 0; i < selectedRows.length; i++) {
						int row = table.convertRowIndexToModel(selectedRows[i]);
						String emailElement = (String) tableModel.getValueAt(row, tableModel.getColumnForEmail());
						if (emailElement != null && !emailElement.equals(""))
							emailModel.addElement(emailElement);
					}
					if (emailModel.getSize() > 0) {
						JList<String> parsedEmailList = Utilities
								.removeDuplicateEntriesInJlist(new JList<String>(emailModel));
						EmailDialog emailDialog = new EmailDialog(child, parsedEmailList);
					} else
						JOptionPane.showMessageDialog(child,
								"Please first select email recipients\n      with email field filled in.");
				}
			}
		});

		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				PersonTableEvent ev = new PersonTableEvent(this, CLOSE_BUTTON, (String) null, null, 0);
				dialogResponse = ev;
				setVisible(false);
				dispose();
			}
		});

		return panel;
	}

	private JPanel createPersonTablePanel() {
		JPanel panel = new JPanel();
		tableModel = new PersonTableModel(columnExpansionLevel, personList);
		table = new JTable(tableModel);

		table.setFont(new Font("Serif", Font.PLAIN, 14));
		table.getTableHeader().setFont(new Font("Serif", Font.BOLD, 16));
		table.setRowHeight(table.getRowHeight() + ROW_GAP);
		table.getColumnModel().getColumn(tableModel.getColumnForLeader()).setMaxWidth(35);
		table.getColumnModel().getColumn(tableModel.getColumnForPhone()).setMaxWidth(100);
		table.getColumnModel().getColumn(tableModel.getColumnForPhone()).setPreferredWidth(95);
		if (columnExpansionLevel == PersonTableModel.getExpansionByDay()) {
			table.getColumnModel().getColumn(tableModel.getColumnForSub()).setMaxWidth(35);
			table.getColumnModel().getColumn(tableModel.getColumnForTime()).setMaxWidth(75);
		} else if (columnExpansionLevel == PersonTableModel.getExpansionByTask()) {
			table.getColumnModel().getColumn(tableModel.getColumnForDow()).setMaxWidth(140);
			table.getColumnModel().getColumn(tableModel.getColumnForDow()).setPreferredWidth(140);
			table.getColumnModel().getColumn(tableModel.getColumnForWom()).setMaxWidth(90);
			table.getColumnModel().getColumn(tableModel.getColumnForWom()).setPreferredWidth(90);
		}
		table.setDefaultRenderer(Object.class, new PersonTableRenderer());
		table.setAutoCreateRowSorter(true);

		// *** POP UP CONTAINS "Edit row" and "Remove person'
		popup = new JPopupMenu();

		// When "Edit row" selected, then trigger PersonTableListener action
		// for this row
		editItem = new JMenuItem("Edit person");
		popup.add(editItem);
		editItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				int row = table.convertRowIndexToModel(table.getSelectedRow());
				PersonTableEvent ev = new PersonTableEvent(this, EDIT_PERSON_ROW_BUTTON,
						(String) tableModel.getValueAt(row, tableModel.getColumnForPersonName()), calendar, 0);
				dialogResponse = ev;
				setVisible(false);
				dispose();
			}
		});
		// When "Remove person" selected, then trigger PersonTableListener
		// action for this row
		if (calendar != null || allPersons == null) {
			removeItem = new JMenuItem("");
			popup.add(removeItem);
			removeItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					int row = table.convertRowIndexToModel(table.getSelectedRow());
					PersonTableEvent ev = new PersonTableEvent(this, REMOVE_PERSON_ROW_BUTTON,
							(String) tableModel.getValueAt(row, tableModel.getColumnForPersonName()), calendar, 0);
					dialogResponse = ev;
					setVisible(false);
					dispose();
				}
			});
			popup.setPreferredSize(new Dimension(240, 50));
		} else
			popup.setPreferredSize(new Dimension(240, 30));

		// Detect right mouse click on table and show pop up menu
		table.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3) {
					popup.show(table, e.getX(), e.getY());
					int row = table.rowAtPoint(e.getPoint());
					if (calendar != null)
						removeItem.setText("Mark person unavailable for " + Utilities.getDisplayDate(calendar));
					else if (removeItem != null)
						removeItem.setText("Remove person from roster");
					table.getSelectionModel().setSelectionInterval(row, row);
				}
			}
		});

		panel.setLayout(new BorderLayout());
		panel.add(new JScrollPane(table), BorderLayout.CENTER);
		return panel;
	}

	public void setData(ArrayList<PersonByTaskModel> db) {
		// Floaters have null task; if task defined, create a sub-list with
		// matching task
		if (taskName == null)
			personList = db;
		else {
			fullList = db;
			personList = createPersonListByTask(taskName, fullList);
		}

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
			else if (value instanceof TimeModel)
				setText(((TimeModel) value).toString());

			if (column != -1) {
				Color textColor = Color.black;
				if (column == tableModel.getColumnForTaskName()) {
					int modelRow = table.convertRowIndexToModel(row);
					PersonByTaskModel person = personList.get(modelRow);
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

				if (column == tableModel.getColumnForPersonName()) {
					super.setText(" " + super.getText());
					super.setHorizontalAlignment(LEFT);
				} else
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
		return EDIT_PERSON_ROW_BUTTON;
	}

	public static int getRemovePersonRowButtonId() {
		return REMOVE_PERSON_ROW_BUTTON;
	}

	private boolean isTimeAlreadyAssigned(String thisPerson, Calendar thisTime) {
		for (int i = 0; i < personList.size(); i++) {
			PersonByTaskModel personModel = personList.get(i);
			String personListValue = personModel.getPerson().getName();
			Calendar timeListValue = personModel.getTaskDate();

			if (personListValue.equals(thisPerson) && Utilities.checkForTimeMatch(timeListValue, thisTime)) {
				if (personModel.getTask() == null) {
					JOptionPane.showMessageDialog(this,
							thisPerson + " is already assigned as Floater at " + Utilities.formatTime(thisTime),
							"Failed to add " + thisPerson + " as Floater", JOptionPane.WARNING_MESSAGE);
					return true;
				} else {
					conflictingTask = personModel.getTask().getTaskName();

					JOptionPane.showMessageDialog(this,
							thisPerson + " is already assigned to " + conflictingTask + " at "
									+ Utilities.formatTime(thisTime),
							"Failed to add " + thisPerson + " as Floater", JOptionPane.WARNING_MESSAGE);
					return true;
				}
			}
		}
		return false;
	}

	private boolean isPersonAlreadyAssigned(ArrayList<String> newPersonList, Calendar calendar, String taskName) {

		for (int i = 0; i < newPersonList.size(); i++) {
			String newPersonName = newPersonList.get(i);

			for (int j = 0; j < fullList.size(); j++) {
				PersonByTaskModel personByDay = fullList.get(j);
				String personByDayName = personByDay.getPerson().getName();

				if (personByDayName.equals(newPersonName) && personByDay.getTaskDate().compareTo(calendar) == 0) {
					// Person already assigned at this time
					if (personByDay.getTask() == null) // Floater
						JOptionPane.showMessageDialog(this,
								personByDayName + " is already assigned as Floater at "
										+ Utilities.formatTime(calendar),
								"Failed to add " + personByDayName + " to " + taskName, JOptionPane.WARNING_MESSAGE);
					else
						JOptionPane.showMessageDialog(this,
								personByDayName + " is already assigned to " + personByDay.getTask().getTaskName()
										+ " at " + Utilities.formatTime(calendar),
								"Failed to add " + personByDayName + " to " + taskName, JOptionPane.WARNING_MESSAGE);

					// Remove from list
					newPersonList.remove(i);
					i--;
					break;
				}
			}
		}

		if (newPersonList.size() > 0)
			// List still contains at least 1 person, then not all were assigned
			return false;
		else
			// List is empty, all persons on original list were assigned
			return true;
	}

	private ArrayList<PersonByTaskModel> createPersonListByTask(String taskName,
			ArrayList<PersonByTaskModel> fullPersonList) {
		ArrayList<PersonByTaskModel> newPersonList = new ArrayList<PersonByTaskModel>();
		for (int i = 0; i < fullPersonList.size(); i++) {
			PersonByTaskModel person = fullPersonList.get(i);
			if (person.getTask() != null && person.getTask().getTaskName().equals(taskName))
				newPersonList.add(person);
		}
		return newPersonList;
	}
}
