package TestDatabase;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;

import model.Database;

public class TestDatabase {
	public static void main(String[] args) {
		Database db = new Database();
		boolean connected = false;

		try {
			connected = db.connect();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		JFrame frame = new JFrame("Database connector");
		JButton connect = new JButton();
		if (connected)
			connect.setText("Connected");
		else
			connect.setText("NOT connected");
		connect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (db.isConnected())
					connect.setText("Connected");
				else
					connect.setText("NOT connected");
			}
		});
		frame.add(connect);
		frame.pack();
		frame.setVisible(true);

		// db.disconnect();
	}
}
