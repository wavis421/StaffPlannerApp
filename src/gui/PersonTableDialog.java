package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Calendar;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import model.PersonByTaskModel;
import model.TimeModel;
import utilities.Utilities;

public class PersonTableDialog extends JDialog {
	private static final int PREF_DIALOG_WIDTH = 800;
	private static final int PREF_DIALOG_HEIGHT = 300;
	private static final int NOTES_MAX_TEXT_LENGTH = 140;

	private static final int ADD_PERSON_BUTTON = 0;
	private static final int EMAIL_BUTTON = 1;
	private static final int CLOSE_BUTTON = 2;
	private static final int EDIT_PERSON_ROW_BUTTON = 3;
	private static final int REMOVE_PERSON_ROW_BUTTON = 4;
	private static final int FLOATER_TO_SUB_ROW_BUTTON = 5;
	private static final int SUB_TO_FLOATER_ROW_BUTTON = 6;

	private static final int ROW_GAP = 5;
	private static final int POPUP_WIDTH = 240;

	private int columnExpansionLevel;
	private JPanel tablePanel;
	private JTable table;
	private PersonTableModel tableModel;
	private JPopupMenu popup;
	private JMenuItem removeItem;
	private JMenuItem editItem;
	private JMenuItem floaterToSubItem;
	private JMenuItem subToFloaterItem;
	private ArrayList<PersonByTaskModel> personList;
	private ArrayList<PersonByTaskModel> fullList = null;
	private ArrayList<PersonTableNotesModel> notesList;
	private String taskName;
	private int activeRow = -1;
	private int popupHeightCurr = 30, popupHeightAdjust = 20;

	private String addButtonText;

	private ArrayList<String> allPersons;
	private ArrayList<TimeModel> allTimes;
	private Calendar calendar;
	private String conflictingTask = null;
	private PersonTableEvent dialogResponse;
	private PersonTableNotesEvent dialogNotesResponse;
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

		if (columnExpansionLevel == PersonTableModel.getExpansionWithNotes())
			initializeNotesList(personList);

		tablePanel = createPersonTablePanel();

		setLayout(new BorderLayout());
		JPanel buttonPanel = createButtonPanel();
		add(tablePanel, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);

		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		if (columnExpansionLevel == PersonTableModel.getMinimumExpansion())
			setSize(PREF_DIALOG_WIDTH, PREF_DIALOG_HEIGHT);
		else
			setSize(PREF_DIALOG_WIDTH + 100, PREF_DIALOG_HEIGHT);
		setVisible(true);
	}

	public PersonTableEvent getDialogResponse() {
		return dialogResponse;
	}

	public PersonTableNotesEvent getDialogNotesResponse() {
		return dialogNotesResponse;
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
					ArrayList<String> emailList = new ArrayList<String>();
					int[] selectedRows = table.getSelectedRows();

					for (int i = 0; i < selectedRows.length; i++) {
						int row = table.convertRowIndexToModel(selectedRows[i]);
						String emailElement = (String) tableModel.getValueAt(row, tableModel.getColumnForEmail());
						if (emailElement != null && !emailElement.equals(""))
							emailList.add(emailElement);
					}
					if (emailList.size() > 0) {
						Utilities.removeDuplicateEntriesInList(emailList);
						new EmailDialog(child, emailList);
					} else
						JOptionPane.showMessageDialog(child,
								"Please first select email recipients\n      with email field filled in.");
				}
			}
		});

		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (columnExpansionLevel == PersonTableModel.getExpansionWithNotes()) {
					extractNotesListChanges();
					dialogNotesResponse = new PersonTableNotesEvent(this, notesList);
				} else {
					dialogResponse = new PersonTableEvent(this, CLOSE_BUTTON, (String) null, null, 0);
				}
				setVisible(false);
				dispose();
			}
		});

		return panel;
	}

	private void initializeNotesList(ArrayList<PersonByTaskModel> inputList) {
		notesList = new ArrayList<PersonTableNotesModel>();

		for (int i = 0; i < inputList.size(); i++) {
			notesList.add(new PersonTableNotesModel(inputList.get(i).getPerson().getName()));
		}
	}

	private void updateNotesList(String name, String notes) {
		for (int i = 0; i < notesList.size(); i++) {
			if (notesList.get(i).getPersonName().equals(name)) {
				notesList.get(i).setPersonNotes(notes);
				return;
			}
		}
	}

	private void extractNotesListChanges() {
		for (int i = 0; i < notesList.size(); i++) {
			if (notesList.get(i).getPersonNotes() == null) {
				notesList.remove(i);
				i--; // Adjust for removed item
			}
		}
	}

	private JPanel createPersonTablePanel() {
		JPanel panel = new JPanel();
		tableModel = new PersonTableModel(columnExpansionLevel, personList);
		table = new JTable(tableModel);

		table.setFont(CustomFonts.TABLE_TEXT_FONT);
		table.getTableHeader().setFont(CustomFonts.TABLE_HEADER_FONT);
		int origRowHeight = table.getRowHeight();
		table.setRowHeight(origRowHeight + ROW_GAP);
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
		} else if (columnExpansionLevel == PersonTableModel.getExpansionWithNotes()) {
			table.getColumnModel().getColumn(tableModel.getColumnForNotes()).setPreferredWidth(200);
			table.setRowHeight((3 * origRowHeight) + ROW_GAP);
			table.getColumnModel().getColumn(tableModel.getColumnForNotes())
					.setCellRenderer(new PersonTableNotesRenderer());
			table.getColumnModel().getColumn(tableModel.getColumnForNotes()).setCellEditor(new TextAreaEditor());
		}
		table.setDefaultRenderer(Object.class, new PersonTableRenderer());
		table.setAutoCreateRowSorter(true);

		if (columnExpansionLevel != PersonTableModel.getExpansionWithNotes()) {
			// *** POP UP CONTAINS "Edit person", "Remove person",
			// "Floater to Sub" and "Sub to Floater"
			popup = new JPopupMenu();

			// When "Edit person" selected, trigger PersonTableListener for row
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
			// When "Remove person" selected, trigger PersonTableListener
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
				popupHeightCurr += popupHeightAdjust;
			}
			popup.setPreferredSize(new Dimension(POPUP_WIDTH, popupHeightCurr));

			// When "Sub to Floater" selected, trigger PersonTableListener
			subToFloaterItem = new JMenuItem("Change from Sub to Floater");
			subToFloaterItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					int row = table.convertRowIndexToModel(table.getSelectedRow());
					PersonTableEvent ev = new PersonTableEvent(this, SUB_TO_FLOATER_ROW_BUTTON,
							(String) tableModel.getValueAt(row, tableModel.getColumnForPersonName()), calendar, 0);
					dialogResponse = ev;
					setVisible(false);
					dispose();
				}
			});
			// When "Sub to Floater" selected, trigger PersonTableListener
			floaterToSubItem = new JMenuItem("Change from Floater to Sub");
			floaterToSubItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					int row = table.convertRowIndexToModel(table.getSelectedRow());
					PersonTableEvent ev = new PersonTableEvent(this, FLOATER_TO_SUB_ROW_BUTTON,
							(String) tableModel.getValueAt(row, tableModel.getColumnForPersonName()), calendar, 0);
					dialogResponse = ev;
					setVisible(false);
					dispose();
				}
			});

			// Detect right mouse click on table and show pop up menu
			table.addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent e) {
					if (e.getButton() == MouseEvent.BUTTON3) {
						int row = table.rowAtPoint(e.getPoint());
						if (tableModel.isSubstitute(row)) {
							// If SUB row selected, add sub-to-floater menu item
							popup.setPopupSize(POPUP_WIDTH, popupHeightCurr + popupHeightAdjust);
							popup.add(subToFloaterItem);
							popup.remove(floaterToSubItem);
						} else if (tableModel.isFloater(row)) {
							// If floater, add floater-to-sub menu item
							popup.setPopupSize(POPUP_WIDTH, popupHeightCurr + popupHeightAdjust);
							popup.add(floaterToSubItem);
							popup.remove(subToFloaterItem);
						} else {
							// Neither floater or sub
							popup.setPopupSize(POPUP_WIDTH, popupHeightCurr);
							popup.remove(subToFloaterItem);
							popup.remove(floaterToSubItem);
						}

						if (calendar != null)
							removeItem.setText("Mark person unavailable for " + Utilities.getDisplayDate(calendar));
						else if (removeItem != null)
							removeItem.setText("Remove person from roster");

						table.getSelectionModel().setSelectionInterval(row, row);
						popup.show(table, e.getX(), e.getY());
					}
				}
			});
		}

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
				setFont(CustomFonts.TABLE_TEXT_FONT);
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
					super.setBackground(CustomFonts.SELECTED_BACKGROUND_COLOR);
				else
					super.setBackground(CustomFonts.UNSELECTED_BACKGROUND_COLOR);

				super.setVerticalAlignment(TOP);
				if (column == tableModel.getColumnForPersonName()) {
					super.setText(" " + super.getText());
					super.setHorizontalAlignment(LEFT);
				} else {
					super.setHorizontalAlignment(CENTER);
				}
			}
			return this;
		}
	}

	public class PersonTableNotesRenderer extends JTextArea implements TableCellRenderer {
		private PersonTableNotesRenderer() {
			super();
			super.setOpaque(true);

			setLineWrap(true);
			setWrapStyleWord(true);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			// cell margins: top, left, bottom, right
			if (column == tableModel.getColumnForLeader())
				setMargin(new Insets(0, 10, 3, 0));
			else
				setMargin(new Insets(0, 4, 3, 4));

			setText((String) value);

			setFont(CustomFonts.TABLE_TEXT_FONT);
			super.setForeground(CustomFonts.DEFAULT_TEXT_COLOR);

			if (isSelected)
				super.setBackground(CustomFonts.SELECTED_BACKGROUND_COLOR);
			else
				super.setBackground(CustomFonts.UNSELECTED_BACKGROUND_COLOR);

			return this;
		}
	}

	public class TextAreaEditor extends AbstractCellEditor implements TableCellEditor {
		JTextArea textArea;
		boolean textChanged = false;

		public TextAreaEditor() {
			textArea = new JTextArea();
			textArea.setLineWrap(true);
			textArea.setWrapStyleWord(true);

			textArea.addKeyListener(new KeyAdapter() {
				public void keyTyped(KeyEvent evt) {
					// Limit text length
					if (textArea.getText().length() > NOTES_MAX_TEXT_LENGTH)
						textArea.setText(textArea.getText().substring(0, NOTES_MAX_TEXT_LENGTH));
					textChanged = true;
				}
			});
			textArea.addFocusListener(new FocusAdapter() {
				public void focusLost(FocusEvent e) {
					if (textChanged) {
						tableModel.setValueAt(textArea.getText(), activeRow, tableModel.getColumnForNotes());
						updateNotesList((String) tableModel.getValueAt(activeRow, tableModel.getColumnForPersonName()),
								textArea.getText());
						textChanged = false;
					}
				}
			});
		}

		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
				int column) {
			// top, left, bottom, right
			textArea.setMargin(new Insets(0, 4, 3, 4));

			if (textChanged) {
				tableModel.setValueAt(textArea.getText(), activeRow, tableModel.getColumnForNotes());
				updateNotesList((String) tableModel.getValueAt(activeRow, tableModel.getColumnForPersonName()),
						textArea.getText());
				textChanged = false;
			}
			activeRow = row;

			// font and color
			textArea.setFont(CustomFonts.TABLE_TEXT_FONT);
			textArea.setForeground(CustomFonts.EDIT_TEXT_COLOR);
			textArea.setText((String) value);

			return textArea;
		}

		public Object getCellEditorValue() {
			return textArea.getText();
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

	public static int getFloaterToSubRowButtonId() {
		return FLOATER_TO_SUB_ROW_BUTTON;
	}

	public static int getSubToFloaterRowButtonId() {
		return SUB_TO_FLOATER_ROW_BUTTON;
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
