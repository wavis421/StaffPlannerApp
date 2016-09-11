package gui;

/*
 * File: MainFrame.java
 * -----------------------
 * This program creates the GUI for the Staff Planner App.
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JToolBar;

public class MainFrame extends JFrame {

	/* Private instance variables */
	private JToolBar toolBar;
	private JButton leftButton;
	private JButton rightButton;
	private CalendarPanel calPanel;

	/**
	 * Initialize the graphical user interface
	 **/
	public MainFrame(String name) {
		super("App");
		setLayout (new BorderLayout());
		setBackground(Color.WHITE);
		
		/* Create components */
		toolBar = new JToolBar();
		calPanel = new CalendarPanel();
		
		/* Set up toolbar */
		toolBar.setLayout(new FlowLayout());
		toolBar.setBorder(BorderFactory.createEtchedBorder());
		leftButton = new JButton("<-");
		rightButton = new JButton("->");
		toolBar.add(leftButton);
		toolBar.add(rightButton);
		
		/* Set up Calendar Panel */
		calPanel.setPreferredSize(new Dimension(585, 200));

		// ADD button listeners
		leftButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				calPanel.updateMonth(-1);
			}
		});
		rightButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				calPanel.updateMonth(1);
			}
		});
		
		// ADD all components to frame
		add(toolBar, BorderLayout.PAGE_START);
		add(calPanel, BorderLayout.WEST);
		pack();
		
		// Make form visible
		setSize (600, 500);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}
}
