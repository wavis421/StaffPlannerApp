package gui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import model.TaskModel;

public class TaskRenderer extends JLabel implements ListCellRenderer<TaskModel> {
	// TODO: Combine with other renderers!!
	public TaskRenderer() {
		setOpaque(true);
	}

	public Component getListCellRendererComponent(JList<? extends TaskModel> list, TaskModel task, int index,
			boolean isSelected, boolean cellHasFocus) 
	{
		setText(task.getTaskName());
		if (isSelected)
			setBackground(new Color(0xDDDDDD));
		else
			setBackground(Color.WHITE);
		setForeground(new Color(task.getColor()));
		
		return this;
	}
}
