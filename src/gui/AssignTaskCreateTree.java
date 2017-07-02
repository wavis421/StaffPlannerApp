package gui;

import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.JList;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import model.AssignedTasksModel;
import model.ListStatus;
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

	public void addNodeToTree(JTree tree, String programName, AssignTaskEvent taskEvent) {
		DefaultMutableTreeNode assignedTasksNode = (DefaultMutableTreeNode) tree.getModel()
				.getChild(tree.getModel().getRoot(), 0);

		for (int i = 0; i < tree.getModel().getChildCount(assignedTasksNode); i++) {
			DefaultMutableTreeNode progNode = (DefaultMutableTreeNode) tree.getModel().getChild(assignedTasksNode, i);

			if (progNode.toString().equals(programName)) {
				progNode.add(new DefaultMutableTreeNode(taskEvent));

				((DefaultTreeModel) tree.getModel()).reload(progNode);
				collapseTree(tree, programName);
				return;
			}
		}
	}

	public void addNodeToTree(JTree tree, String programName, TaskModel taskEvent) {
		DefaultMutableTreeNode tasksNode = (DefaultMutableTreeNode) tree.getModel().getChild(tree.getModel().getRoot(),
				0);

		for (int i = 0; i < tree.getModel().getChildCount(tasksNode); i++) {
			DefaultMutableTreeNode progNode = (DefaultMutableTreeNode) tree.getModel().getChild(tasksNode, i);

			if (progNode.toString().equals(programName)) {
				progNode.add(new DefaultMutableTreeNode(taskEvent));

				((DefaultTreeModel) tree.getModel()).reload(progNode);
				collapseTree(tree, programName);
				return;
			}
		}
	}

	public void removeNodeFromTree(JTree tree, String programName, String taskName) {
		DefaultMutableTreeNode tasksNode = (DefaultMutableTreeNode) tree.getModel().getChild(tree.getModel().getRoot(),
				0);

		for (int i = 0; i < tree.getModel().getChildCount(tasksNode); i++) {
			DefaultMutableTreeNode progNode = (DefaultMutableTreeNode) tree.getModel().getChild(tasksNode, i);
			if (progNode.toString().equals(programName)) {
				for (int j = 0; j < progNode.getChildCount(); j++) {
					if (progNode.getChildAt(j).toString().equals(taskName)) {
						progNode.remove(j);

						((DefaultTreeModel) tree.getModel()).reload(progNode);
						collapseTree(tree, programName);
						return;
					}
				}
			}
		}
	}

	private JTree createTaskTree() {
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Available Tasks");
		DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);
		JTree tree = new JTree(treeModel);

		DefaultMutableTreeNode tasksNode = new DefaultMutableTreeNode("Select task to assign  >>>");
		tree.setSelectionPath(tree.getPathForRow(0));
		treeModel.insertNodeInto(tasksNode, (DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent(), 0);

		for (int i = 0; i < programList.size(); i++) {
			// Create program node
			ProgramModel p = programList.get(i);
			DefaultMutableTreeNode pNode = new DefaultMutableTreeNode(p);
			tasksNode.add(pNode);

			JList<TaskModel> taskList = taskListByProgram.get(i);

			// For each task in program, add only if not yet assigned
			for (int j = 0; j < taskList.getModel().getSize(); j++) {
				TaskModel task = taskList.getModel().getElementAt(j);
				if (findNodeInAssignedTaskList(assignedTasksByProgram.get(i), task.getTaskName()) == -1)
					pNode.add(new DefaultMutableTreeNode(task));
			}
		}
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setShowsRootHandles(false);
		return (tree);
	}

	private JTree createAssignedTasksTree(String currProgram) {
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Assigned Tasks");
		DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);
		JTree activeTasksTree = new JTree(treeModel);

		DefaultMutableTreeNode assignedTasksNode = new DefaultMutableTreeNode("Regularly Scheduled Tasks");
		DefaultMutableTreeNode extraTasksNode = new DefaultMutableTreeNode("Extra Tasks");
		activeTasksTree.setSelectionPath(activeTasksTree.getPathForRow(0));
		treeModel.insertNodeInto(assignedTasksNode,
				(DefaultMutableTreeNode) activeTasksTree.getSelectionPath().getLastPathComponent(), 0);
		treeModel.insertNodeInto(extraTasksNode,
				(DefaultMutableTreeNode) activeTasksTree.getSelectionPath().getLastPathComponent(), 1);

		for (int i = 0; i < programList.size(); i++) {
			// Create program node
			String progName = programList.get(i).getProgramName();
			DefaultMutableTreeNode pNode = new DefaultMutableTreeNode(progName);
			assignedTasksNode.add(pNode);

			// Add tasks for this program
			ArrayList<AssignedTasksModel> assignedTaskList = assignedTasksByProgram.get(i);
			for (int j = 0; j < assignedTaskList.size(); j++) {
				AssignedTasksModel item = assignedTaskList.get(j);

				if (item.getElementStatus() != ListStatus.LIST_ELEMENT_DELETE) {
					// Create the event to be added to the tree
					AssignTaskEvent taskEvent = new AssignTaskEvent(this, item.getProgramName(),
							findNodeInTaskList(getTaskListByProgram(progName), item.getTaskName()),
							item.getAssignedTaskID(), item.getDaysOfWeek(), item.getWeeksOfMonth());

					pNode.add(new DefaultMutableTreeNode(taskEvent));
				}
			}
		}

		// Collapse all program nodes except last inserted task
		collapseTree(activeTasksTree, currProgram);
		collapseTree(taskTree, currProgram);

		activeTasksTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		activeTasksTree.setShowsRootHandles(false);
		return (activeTasksTree);
	}

	private void collapseTree(JTree tree, String s) {
		tree.expandRow(0);
		tree.expandRow(1);
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
