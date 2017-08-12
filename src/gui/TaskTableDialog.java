package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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

import model.TaskModel;
import model.TimeModel;
import utilities.Utilities;

public class TaskTableDialog extends JDialog {
	private static final int PREF_DIALOG_WIDTH = 800;
	private static final int PREF_DIALOG_HEIGHT = 300;

	private static final int ADD_TASK_BUTTON = 1;
	private static final int EDIT_ROW_BUTTON = 2;
	private static final int DELETE_ROW_BUTTON = 3;
	private static final int CLOSE_BUTTON = 4;

	private static final int ROW_GAP = 5;

	private JPanel tablePanel;
	private JTable table;
	private TaskTableModel tableModel;
	private JPopupMenu popup;
	private JMenuItem removeItem;
	private JMenuItem editItem;
	private JList<TaskModel> taskList;
	private String title;

	private TaskTableEvent dialogResponse;

	public TaskTableDialog(JFrame parent, String title, JList<TaskModel> taskList) {
		super(parent, true);
		setLocation(new Point(100, 100));
		setTitle(title);
		this.title = title;
		this.taskList = taskList;

		tablePanel = createTaskTablePanel();

		setLayout(new BorderLayout());
		JPanel buttonPanel = createButtonPanel();
		add(tablePanel, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);

		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setSize(PREF_DIALOG_WIDTH, PREF_DIALOG_HEIGHT);
		setVisible(true);
	}

	public TaskTableEvent getDialogResponse() {
		return dialogResponse;
	}

	private JPanel createButtonPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		JPanel buttonPanel = new JPanel();

		// Create buttons and add to panel
		JButton addTaskButton = new JButton("Add task");
		JButton closeButton = new JButton("Close");
		buttonPanel.add(addTaskButton);
		buttonPanel.add(closeButton);
		panel.add(buttonPanel, BorderLayout.CENTER);

		// Create print icon and add to panel
		JLabel iconLabel = Utilities.createPrintTableIcon(getClass().getResource("/images/printIcon_18x18.png"));
		panel.add(iconLabel, BorderLayout.WEST);

		addTaskButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TaskTableEvent ev = new TaskTableEvent(this, ADD_TASK_BUTTON, (String) null);
				dialogResponse = ev;
				setVisible(false);
				dispose();
			}
		});
		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TaskTableEvent ev = new TaskTableEvent(this, CLOSE_BUTTON, (String) null);
				dialogResponse = ev;
				setVisible(false);
				dispose();
			}
		});
		iconLabel.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				Utilities.printTable(TaskTableDialog.this, table, title);
			}
		});

		return panel;
	}

	private JPanel createTaskTablePanel() {
		JPanel panel = new JPanel();
		tableModel = new TaskTableModel(taskList);
		table = new JTable(tableModel);

		table.setFont(CustomFonts.TABLE_TEXT_FONT);
		table.getTableHeader().setFont(CustomFonts.TABLE_HEADER_FONT);
		table.setRowHeight(table.getRowHeight() + ROW_GAP);
		table.getColumnModel().getColumn(tableModel.getColumnForTime()).setMaxWidth(75);

		table.setDefaultRenderer(Object.class, new CustomTableRenderer());
		table.setAutoCreateRowSorter(true);

		popup = new JPopupMenu();
		editItem = new JMenuItem("Edit task ");
		removeItem = new JMenuItem("Delete task ");
		popup.add(editItem);
		popup.add(removeItem);

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

		// "Delete" row selected from popup menu
		removeItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int row = table.convertRowIndexToModel(table.getSelectedRow());
				TaskTableEvent ev = new TaskTableEvent(this, DELETE_ROW_BUTTON,
						taskList.getModel().getElementAt(row).getTaskName());
				dialogResponse = ev;
				setVisible(false);
				dispose();
			}
		});

		// "Edit" row selected from popup menu
		editItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int row = table.convertRowIndexToModel(table.getSelectedRow());
				TaskTableEvent ev = new TaskTableEvent(this, EDIT_ROW_BUTTON,
						taskList.getModel().getElementAt(row).getTaskName());
				dialogResponse = ev;
				setVisible(false);
				dispose();
			}
		});

		// TODO: Add clone menu
		// TODO: Merge 3 listeners into single method

		panel.setLayout(new BorderLayout());
		panel.add(new JScrollPane(table), BorderLayout.CENTER);
		return panel;
	}

	public void setData(JList<TaskModel> db) {
		taskList = db;
		tableModel.setData(db);
		tableModel.fireTableDataChanged();
	}

	public class CustomTableRenderer extends JLabel implements TableCellRenderer {
		private CustomTableRenderer() {
			super();
			super.setOpaque(true);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {

			if (value instanceof String)
				setText((String) value);
			else if (value instanceof TimeModel)
				setText((String) value.toString());

			if (column != -1) {
				Color textColor = Color.black;
				if (column == tableModel.getColumnForTaskName()) {
					int modelRow = table.convertRowIndexToModel(row);
					TaskModel task = (TaskModel) taskList.getModel().getElementAt(modelRow);
					textColor = new Color(task.getColor());
				}
				super.setForeground(textColor);

				if (isSelected)
					super.setBackground(new Color(0xDDDDDD));
				else
					super.setBackground(Color.WHITE);

				if (column == tableModel.getColumnForTaskName()) {
					setText(" " + getText());
					super.setHorizontalAlignment(LEFT);
				} else
					super.setHorizontalAlignment(CENTER);
			}
			return this;
		}
	}

	public static int getAddTaskButton() {
		return ADD_TASK_BUTTON;
	}

	public static int getEditRowButton() {
		return EDIT_ROW_BUTTON;
	}

	public static int getDeleteRowButton() {
		return DELETE_ROW_BUTTON;
	}

	public static int getCloseButton() {
		return CLOSE_BUTTON;
	}
}
