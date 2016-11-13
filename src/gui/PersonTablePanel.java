package gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import model.PersonModel;

public class PersonTablePanel extends JPanel {
	private JTable table;
	private PersonTableModel tableModel;
	private JPopupMenu popup;
	private JMenuItem removeItem;
	private JMenuItem editItem;
	private PersonTableListener personTableListener;

	public PersonTablePanel(PersonTableModel tableModel) {
		this.tableModel = tableModel;
		table = new JTable(tableModel);

		popup = new JPopupMenu();
		removeItem = new JMenuItem("Delete row");
		editItem = new JMenuItem("Edit row");
		popup.add(removeItem);
		popup.add(editItem);

		// Detect right mouse click on table, then pop-up "Delete row" and select
		// row
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
					personTableListener.editRow((String) tableModel.getValueAt(row, tableModel.getColumnForPersonName()));
					tableModel.fireTableRowsUpdated(row,  row);
				}
			}
		});

		setLayout(new BorderLayout());
		add(new JScrollPane(table), BorderLayout.CENTER);
	}

	public void setData(LinkedList<PersonModel> db) {
		tableModel.setData(db);
		refresh();
	}

	public void setPersonTableListener(PersonTableListener listener) {
		this.personTableListener = listener;
	}

	public void refresh() {
		tableModel.fireTableDataChanged();
	}
}
