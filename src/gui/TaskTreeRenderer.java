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
import model.SingleInstanceTaskModel;
import model.TaskModel;

public class TaskTreeRenderer extends DefaultTreeCellRenderer {
	// Task list path count = 1, Assign tasks path count = 2
	private static final int PROGRAM_PATH_COUNT = 3;
	private static final int LEAF_PATH_COUNT = 4;

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
		} else if (tree.getPathForRow(row) != null && tree.getPathForRow(row).getPathCount() <= PROGRAM_PATH_COUNT) {
			setClosedIcon(calPlusIcon);
			setOpenIcon(calPlusIcon);
			setLeafIcon(calPlusIcon);
		} else {
			setLeafIcon(null);
		}

		textSelectionColor = Color.black;
		textNonSelectionColor = Color.black;
		setBackground(Color.WHITE);

		if (value != null && tree.getPathForRow(row) != null
				&& tree.getPathForRow(row).getPathCount() == LEAF_PATH_COUNT) {
			Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
			String taskName;
			int taskColor;
			boolean taskIsFocus;

			// This renderer is used for task, assigned task, single task
			if (userObject instanceof TaskModel) {
				taskName = ((TaskModel) userObject).getTaskName();
				taskColor = ((TaskModel) userObject).getColor();
				taskIsFocus = ((TaskModel) userObject).getIsFocus();
			} else if (userObject instanceof AssignTaskEvent) {
				taskName = ((AssignTaskEvent) userObject).getTask().getTaskName();
				taskColor = ((AssignTaskEvent) userObject).getTask().getColor();
				taskIsFocus = ((AssignTaskEvent) userObject).getIsFocus();
			} else { // SingleInstanceTaskModel
				taskName = ((SingleInstanceTaskModel) userObject).getTaskName();
				taskColor = ((SingleInstanceTaskModel) userObject).getColor();
				taskIsFocus = ((SingleInstanceTaskModel) userObject).getIsFocus();
			}

			setText(taskName);
			setFont(JTFTools.decodeFont(BOLD_FONT));
			textSelectionColor = new Color(taskColor);
			textNonSelectionColor = new Color(taskColor);
			if (taskIsFocus || hasFocus)
				setBackground(new Color(0xDDDDDD));
		}
		super.getTreeCellRendererComponent(tree, value, isSelected, isExpanded, isLeaf, row, hasFocus);
		return this;
	}

	public static int getProgramPathCount() {
		return PROGRAM_PATH_COUNT;
	}
}
