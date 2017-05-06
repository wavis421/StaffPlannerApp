package gui;

import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import model.AssignedTasksModel;
import model.ProgramModel;
import model.TaskModel;

public class AssignTaskCreateTree {
	private ArrayList<ProgramModel> programList;
	private ArrayList<JList<TaskModel>> taskListByProgram;
	private ArrayList<ArrayList<AssignedTasksModel>> assignedTasksByProgram;

	private JTree taskTree;
	private JTree assignedTaskTree;

	public AssignTaskCreateTree(String currentProgram, ArrayList<ProgramModel> programList,
			ArrayList<JList<TaskModel>> taskListByProgram,
			ArrayList<ArrayList<AssignedTasksModel>> assignedTaskListByProgram) {
		this.programList = programList;
		this.taskListByProgram = taskListByProgram;
		this.assignedTasksByProgram = assignedTaskListByProgram;

		taskTree = createTaskTree();
		assignedTaskTree = createAssignedTasksTree(currentProgram);
	}

	public JTree getTaskTree() {
		return taskTree;
	}

	public JTree getAssignedTaskTree() {
		return assignedTaskTree;
	}

	private JTree createTaskTree() {
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Select task to assign  >>>");
		DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);

		for (int i = 0; i < programList.size(); i++) {
			ProgramModel p = programList.get(i);
			DefaultMutableTreeNode pNode = new DefaultMutableTreeNode(p);
			rootNode.add(pNode);

			JList<TaskModel> taskList = taskListByProgram.get(i);

			// For each task in program, add only if not yet assigned
			for (int j = 0; j < taskList.getModel().getSize(); j++) {
				TaskModel task = taskList.getModel().getElementAt(j);
				if (findNodeInAssignedTaskList(assignedTasksByProgram.get(i), task.getTaskName()) == -1)
					pNode.add(new DefaultMutableTreeNode(task));
			}
		}
		JTree tree = new JTree(treeModel);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setShowsRootHandles(true);
		return (tree);
	}

	private JTree createAssignedTasksTree(String currProgram) {
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Assigned tasks");
		DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);
		JTree assignedTree = new JTree(treeModel);
		TreePath path;

		for (int i = 0; i < programList.size(); i++) {
			// Create program node
			String progName = programList.get(i).getProgramName();
			DefaultMutableTreeNode pNode = new DefaultMutableTreeNode(progName);
			assignedTree.setSelectionPath(assignedTree.getPathForRow(0));
			treeModel.insertNodeInto(pNode,
					(DefaultMutableTreeNode) assignedTree.getSelectionPath().getLastPathComponent(), 0);

			// Add tasks for this program
			ArrayList<AssignedTasksModel> assignedTaskList = assignedTasksByProgram.get(i);
			for (int j = 0; j < assignedTaskList.size(); j++) {
				AssignedTasksModel item = assignedTaskList.get(j);

				// Create the event to be added to the tree
				AssignTaskEvent taskEvent = new AssignTaskEvent(this, item.getProgramName(),
						findNodeInTaskList(getTaskListByProgram(progName), item.getTaskName()),
						item.getAssignedTaskID(), item.getDaysOfWeek(), item.getWeeksOfMonth());

				pNode.add(new DefaultMutableTreeNode(taskEvent));
			}
		}

		// Collapse all program nodes except last inserted task
		collapseTree(assignedTree, currProgram);
		collapseTree(taskTree, currProgram);

		assignedTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		assignedTree.setShowsRootHandles(true);
		return (assignedTree);
	}

	private void collapseTree(JTree tree, String s) {
		tree.expandRow(0);
		int row = tree.getRowCount() - 1;

		// Collapse child nodes of root
		while (row > 0) {
			tree.collapseRow(row);
			row--;
		}

		if (s != null) {
			TreePath path = findNodeInTree((DefaultMutableTreeNode) tree.getModel().getRoot(), s);
			tree.expandPath(path);
		}
	}

	private TreePath findNodeInTree(DefaultMutableTreeNode root, String s) {
		Enumeration<DefaultMutableTreeNode> e = root.depthFirstEnumeration();
		while (e.hasMoreElements()) {
			DefaultMutableTreeNode node = e.nextElement();
			if (node.toString().equals(s)) {
				return new TreePath(node.getPath());
			}
		}
		return null;
	}

	private int findNodeInAssignedTaskList(ArrayList<AssignedTasksModel> list, String taskName) {
		for (int idx = 0; idx < list.size(); idx++) {
			AssignedTasksModel t = list.get(idx);
			if (t.getTaskName().equals(taskName)) {
				return idx;
			}
		}
		return -1;
	}

	private TaskModel findNodeInTaskList(JList<TaskModel> list, String taskName) {
		for (int idx = 0; idx < list.getModel().getSize(); idx++) {
			TaskModel t = list.getModel().getElementAt(idx);
			if (t.getTaskName().equals(taskName)) {
				return t;
			}
		}
		return null;
	}

	private JList<TaskModel> getTaskListByProgram(String progName) {
		for (int i = 0; i < programList.size(); i++) {
			ProgramModel p = programList.get(i);
			if (p.getProgramName().equals(progName)) {
				return taskListByProgram.get(i);
			}
		}
		return null;
	}
}
