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
import model.SingleInstanceTaskModel;
import model.TaskModel;

public class AssignTaskCreateTree {
	private ArrayList<ProgramModel> programList;
	private ArrayList<JList<TaskModel>> taskListByProgram;
	private ArrayList<ArrayList<AssignedTasksModel>> assignedTasksByProgram;

	private JTree taskTree;
	private JTree assignedTaskTree;
	private DefaultMutableTreeNode regularTaskRootNode = new DefaultMutableTreeNode("Regularly Scheduled Tasks");
	private DefaultMutableTreeNode substituteRootNode = new DefaultMutableTreeNode("Substitute");
	private DefaultMutableTreeNode floaterRootNode = new DefaultMutableTreeNode("Floater");

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

	public void addNodeToTree(JTree tree, String programName, Object taskEvent) {
		DefaultMutableTreeNode taskNode = (DefaultMutableTreeNode) tree.getModel().getChild(tree.getModel().getRoot(),
				0);

		for (int i = 0; i < tree.getModel().getChildCount(taskNode); i++) {
			DefaultMutableTreeNode progNode = (DefaultMutableTreeNode) tree.getModel().getChild(taskNode, i);

			if (progNode.toString().equals(programName)) {
				progNode.add(new DefaultMutableTreeNode(taskEvent));

				((DefaultTreeModel) tree.getModel()).reload(progNode);
				collapseTree(tree, programName);
				return;
			}
		}
	}

	public void addExtraTaskNodeToTree(JTree tree, SingleInstanceTaskModel extraTaskEvent) {
		// Get 'extra tasks' node
		DefaultMutableTreeNode extraTaskNode = (DefaultMutableTreeNode) tree.getModel()
				.getChild(tree.getModel().getRoot(), 1);

		// TODO: Initialize subs/floaters all at once for optimization
		if (extraTaskEvent.getTaskID() == 0)
			floaterRootNode.add(new DefaultMutableTreeNode(extraTaskEvent));
		else
			substituteRootNode.add(new DefaultMutableTreeNode(extraTaskEvent));

		((DefaultTreeModel) tree.getModel()).reload(extraTaskNode);
		expandExtraTaskNode(tree);
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

	public void removeExtraTaskNodeFromTree(SingleInstanceTaskModel task) {
		DefaultMutableTreeNode rootNode;

		// Select whether substitute or floater
		if (task.getTaskID() == 0)
			rootNode = floaterRootNode;
		else
			rootNode = substituteRootNode;

		// Remove matching task
		for (int i = 0; i < rootNode.getChildCount(); i++) {
			if (rootNode.getChildAt(i).toString().equals(task.toString())) {
				rootNode.remove(i);
				((DefaultTreeModel) assignedTaskTree.getModel()).reload(rootNode);
				assignedTaskTree.expandPath(new TreePath(rootNode.getPath()));
				break;
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
		DefaultTreeModel treeModel = new DefaultTreeModel(new DefaultMutableTreeNode("Assigned Tasks"));
		DefaultMutableTreeNode extraTaskNode = new DefaultMutableTreeNode("Extra Tasks");
		JTree activeTasksTree = new JTree(treeModel);

		activeTasksTree.setSelectionPath(activeTasksTree.getPathForRow(0));
		treeModel.insertNodeInto(regularTaskRootNode,
				(DefaultMutableTreeNode) activeTasksTree.getSelectionPath().getLastPathComponent(), 0);
		treeModel.insertNodeInto(extraTaskNode,
				(DefaultMutableTreeNode) activeTasksTree.getSelectionPath().getLastPathComponent(), 1);

		extraTaskNode.add(substituteRootNode);
		extraTaskNode.add(floaterRootNode);

		for (int i = 0; i < programList.size(); i++) {
			// Create program node
			String progName = programList.get(i).getProgramName();
			DefaultMutableTreeNode pNode = new DefaultMutableTreeNode(progName);
			regularTaskRootNode.add(pNode);

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

		// Get row count for # programs, add 1 for root node
		int row = regularTaskRootNode.getChildCount() + 1;

		// Collapse child nodes of 'Regularly Scheduled Tasks'
		while (row > 0) {
			tree.collapseRow(row);
			row--;
		}

		// Expand current program node if it exists
		if (s != null) {
			TreePath path = findNodeInTree((DefaultMutableTreeNode) tree.getModel().getRoot(), s);
			tree.expandPath(path);
		}
	}

	private void expandExtraTaskNode(JTree tree) {
		DefaultMutableTreeNode extraTaskNode = (DefaultMutableTreeNode) tree.getModel()
				.getChild(tree.getModel().getRoot(), 1);

		// Expand 'extra tasks' row
		tree.expandPath(new TreePath(extraTaskNode.getPath()));
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
