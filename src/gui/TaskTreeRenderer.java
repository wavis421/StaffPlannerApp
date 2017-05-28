package gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import acm.util.JTFTools;
import model.TaskModel;

public class TaskTreeRenderer extends DefaultTreeCellRenderer {
	private static ImageIcon calIcon;
	private static ImageIcon calPlusIcon;
	private static final String BASIC_FONT = "Arial-12";
	private static final String BOLD_FONT = "Arial-bold-12";
	private static final String ITALIC_FONT = "Arial-italic-12";

	// TODO: Combine with other renderers!!
	public TaskTreeRenderer(int treeWidth) {
		super();

		setOpaque(true);
		setPreferredSize(new Dimension(treeWidth, 16));

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

		setFont(JTFTools.decodeFont(BASIC_FONT));
		if (row == 0) {
			setFont(JTFTools.decodeFont(ITALIC_FONT));
			setClosedIcon(calIcon);
			setOpenIcon(calIcon);
			setLeafIcon(calIcon);
		} else if (tree.getPathForRow(row) != null && tree.getPathForRow(row).getPathCount() == 2) {
			setClosedIcon(calPlusIcon);
			setOpenIcon(calPlusIcon);
			setLeafIcon(calPlusIcon);
		} else {
			setLeafIcon(null);
		}

		textSelectionColor = Color.black;
		textNonSelectionColor = Color.black;
		setBackground(Color.WHITE);

		if (value != null && tree.getPathForRow(row) != null && tree.getPathForRow(row).getPathCount() == 3) {
			Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
			TaskModel task;
			boolean taskIsFocus;

			// This renderer is used for both task and assigned task
			if (userObject instanceof TaskModel) {
				task = (TaskModel) userObject;
				taskIsFocus = task.getIsFocus();
			} else {
				task = ((AssignTaskEvent) userObject).getTask();
				taskIsFocus = ((AssignTaskEvent) userObject).getIsFocus();
			}

			setText(task.getTaskName());
			setFont(JTFTools.decodeFont(BOLD_FONT));
			textSelectionColor = new Color(task.getColor());
			textNonSelectionColor = new Color(task.getColor());
			if (taskIsFocus || hasFocus)
				setBackground(new Color(0xDDDDDD));
		}
		super.getTreeCellRendererComponent(tree, value, isSelected, isExpanded, isLeaf, row, hasFocus);
		return this;
	}
}
