package gui;

import java.awt.Color;
import java.awt.Component;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import acm.util.JTFTools;

public class AssignTaskTreeRenderer extends DefaultTreeCellRenderer {
	private static ImageIcon calIcon;
	private static ImageIcon calPlusIcon;
	private static final String BASIC_FONT = "Arial-12";
	private static final String BOLD_FONT = "Arial-bold-12";
	private static final String ITALIC_FONT = "Arial-italic-12";

	public AssignTaskTreeRenderer() {
		// TODO: Combine with other renderers!!
		super();

		setOpaque(true);

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
			AssignTaskEvent taskEvent = (AssignTaskEvent) (((DefaultMutableTreeNode) value).getUserObject());
			setText(taskEvent.getTask().getTaskName());

			setFont(JTFTools.decodeFont(BOLD_FONT));
			textSelectionColor = new Color(taskEvent.getTask().getColor());
			textNonSelectionColor = new Color(taskEvent.getTask().getColor());
			if (taskEvent.getIsFocus() || hasFocus)
				setBackground(new Color(0xDDDDDD));
		}
		super.getTreeCellRendererComponent(tree, value, isSelected, isExpanded, isLeaf, row, hasFocus);
		return this;
	}
}
