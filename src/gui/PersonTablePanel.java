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
import java.util.LinkedList;

import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import model.PersonByTaskModel;

public class PersonTablePanel extends JPanel {
	private final int ROW_GAP = 5;
	private JTable table;
	private PersonTableModel tableModel;
	private JPopupMenu popup;
	private JMenuItem removeItem;
	private JMenuItem editItem;
	private PersonTableListener personTableListener;
	private LinkedList<PersonByTaskModel> personList;

	public PersonTablePanel(boolean isColumnExpanded, LinkedList<PersonByTaskModel> personList) {
		this.personList = personList;

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

		// Detect right mouse click on table, then pop-up "Delete row" and
		// select row
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
				int row = table.getSelectedRow();
				if (personTableListener != null) {
					personTableListener.rowDeleted(row);
					tableModel.fireTableRowsDeleted(row, row);
				}
			}
		});

		// When "Edit row" selected, then trigger PersonTableListener action
		// for this row
		editItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int row = table.getSelectedRow();
				if (personTableListener != null) {
					personTableListener
							.editRow((String) tableModel.getValueAt(row, tableModel.getColumnForPersonName()));
					tableModel.fireTableRowsUpdated(row, row);
				}
			}
		});

		setLayout(new BorderLayout());
		add(new JScrollPane(table), BorderLayout.CENTER);
	}

	public void setData(LinkedList<PersonByTaskModel> db) {
		personList = db;
		tableModel.setData(db);
		refresh();
	}

	public void setPersonTableListener(PersonTableListener listener) {
		this.personTableListener = listener;
	}

	public void refresh() {
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
}
