package gui;

import java.awt.Color;
import java.awt.Component;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import model.TaskModel;

public class TaskTreeRenderer extends DefaultTreeCellRenderer {
	private static ImageIcon calIcon;
	private static ImageIcon calPlusIcon;

	public TaskTreeRenderer() {
		super();

		URL url = getClass().getResource("../images/calendar_16x16.png");
		calIcon = new ImageIcon(url);
		url = getClass().getResource("../images/calendar_plus_16x16.png");
		calPlusIcon = new ImageIcon(url);

		setClosedIcon(calPlusIcon);
		setOpenIcon(calPlusIcon);
		setLeafIcon(null);
	}

	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean isSelected, boolean isExpanded,
			boolean isLeaf, int row, boolean hasFocus) {
		if (row == 0) {
			setClosedIcon(calIcon);
			setOpenIcon(calIcon);
			setLeafIcon(calIcon);
		} else {
			setClosedIcon(calPlusIcon);
			setOpenIcon(calPlusIcon);
			setLeafIcon(null);
		}

		textSelectionColor = Color.black;
		textNonSelectionColor = Color.black;
		if (value != null && row != 0 && isLeaf) {
			TaskModel task = (TaskModel) (((DefaultMutableTreeNode) value).getUserObject());
			textSelectionColor = new Color(task.getColor());
			textNonSelectionColor = new Color(task.getColor());
		}
		super.getTreeCellRendererComponent(tree, value, isSelected, isExpanded, isLeaf, row, hasFocus);
		return this;
	}
}
