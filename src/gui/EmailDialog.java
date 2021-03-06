package gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.border.Border;

public class EmailDialog extends JDialog {
	private static final int TEXT_FIELD_WIDTH = 54;
	private static final int TEXT_PANE_WIDTH = 600;
	private static final int TO_PANE_HEIGHT = 60;
	private static final int MESSAGE_PANE_HEIGHT = 200;

	private JButton copyButton = new JButton("Copy address list");
	private JButton sendButton = new JButton("Send");
	private JButton cancelButton = new JButton("Cancel");
	private JTextPane toField = new JTextPane();
	private JTextField fromField = new JTextField("", TEXT_FIELD_WIDTH);
	private JTextField subjectField = new JTextField("", TEXT_FIELD_WIDTH);
	private JTextPane messageText = new JTextPane();

	// Private instance variables
	private static String userName = "";
	private static char[] password;
	private static Integer port;
	private ArrayList<String> emailRecipients;

	// Dialog panels
	private JPanel controlsPanel;
	private JPanel buttonsPanel;

	public EmailDialog(JDialog parent, ArrayList<String> emailRecipients) {
		super(parent, "Send email...", true);
		this.emailRecipients = emailRecipients;

		if (userName.equals("") || password.length == 0) {
			// Get username & password
			EmailSettingsDialog ev = new EmailSettingsDialog(this);
			EmailSettingsEvent dialogResponse = ev.getDialogResponse();

			if (dialogResponse != null) {
				userName = dialogResponse.getUserName();
				password = dialogResponse.getPassword();
				port = dialogResponse.getPortNumber();
			}
		}

		copyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String delims = "[, ]+";
				String[] elements = toField.getText().split(delims);

				if (elements.length == 0)
					JOptionPane.showMessageDialog(getParent(), "Address list is empty");
				else {
					String addressString = "";
					for (int i = 0; i < elements.length; i++) {
						if (!addressString.equals(""))
							addressString += ", ";
						addressString += elements[i];
					}

					StringSelection stringSelection = new StringSelection(addressString);
					Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
					clip.setContents(stringSelection, null);
				}
			}
		});
		sendButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				generateAndSendEmail();
				setVisible(false);
				dispose();
			}
		});
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				dispose();
			}
		});

		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setLayout();
		setSize(740, 430);
		setVisible(true);
	}

	private void setLayout() {
		int gridY = 0;
		controlsPanel = new JPanel();
		buttonsPanel = new JPanel();

		String toText = "";
		for (int i = 0; i < emailRecipients.size(); i++) {
			if (!toText.equals(""))
				toText += ", ";
			toText += emailRecipients.get(i);
		}
		toField.setText(toText);
		JScrollPane toPane = new JScrollPane(toField);
		toPane.setPreferredSize(new Dimension(TEXT_PANE_WIDTH, TO_PANE_HEIGHT));

		fromField.setText(userName);

		JScrollPane messagePane = new JScrollPane(messageText);
		messagePane.setPreferredSize(new Dimension(TEXT_PANE_WIDTH, MESSAGE_PANE_HEIGHT));

		controlsPanel.setLayout(new GridBagLayout());
		buttonsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

		Border etchedBorder = BorderFactory.createEtchedBorder();
		Border spaceBorder = BorderFactory.createEmptyBorder(15, 15, 15, 15);
		controlsPanel.setBorder(BorderFactory.createCompoundBorder(spaceBorder, etchedBorder));

		GridBagConstraints gc = new GridBagConstraints();

		gc.weightx = gc.weighty = 1;
		gc.fill = GridBagConstraints.NONE;

		addRowToControlPanel(gc, new JLabel("To: "), toPane, gridY++);
		addRowToControlPanel(gc, new JLabel("From: "), fromField, gridY++);
		addRowToControlPanel(gc, new JLabel("Subject: "), subjectField, gridY++);
		addRowToControlPanel(gc, new JLabel("Message: "), messagePane, gridY++);

		// Buttons row
		gc.gridy++;
		gc.gridx = 0;
		buttonsPanel.add(copyButton);
		gc.gridx++;
		buttonsPanel.add(sendButton);
		gc.gridx++;
		buttonsPanel.add(cancelButton);

		// Add to panel
		setLayout(new BorderLayout());
		add(controlsPanel, BorderLayout.CENTER);
		add(buttonsPanel, BorderLayout.SOUTH);

		// make send & cancel buttons the same size
		Dimension btnSize = cancelButton.getPreferredSize();
		sendButton.setPreferredSize(btnSize);
	}

	// Generic method to add row with label and component
	private void addRowToControlPanel(GridBagConstraints gcon, Component value1, Component value2, int gridY) {
		gcon.gridx = 0;
		gcon.gridy = gridY;
		gcon.anchor = GridBagConstraints.EAST;
		gcon.insets = new Insets(0, 0, 0, 15);
		controlsPanel.add(value1, gcon);
		gcon.gridx++;
		gcon.anchor = GridBagConstraints.WEST;
		gcon.insets = new Insets(0, 0, 0, 0);
		controlsPanel.add(value2, gcon);
	}

	private void generateAndSendEmail() {
		// Currently hard-coded to send using gmail SMTP
		Properties properties = System.getProperties();
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.host", "smtp.gmail.com");
		properties.put("mail.smtp.socketFactory.port", port.toString());
		properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		properties.put("mail.smtp.port", port.toString());

		// Session session = Session.getDefaultInstance(properties, null);
		Session session = Session.getDefaultInstance(properties, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(userName, new String(password));
			}
		});

		// Set cursor to "wait" cursor
		this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		try {
			// Set message fields
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(userName));
			message.setSubject(subjectField.getText());
			message.setText(messageText.getText());

			// Set email recipients
			String delims = "[, ]+";
			String[] elements = toField.getText().split(delims);
			for (int i = 0; i < elements.length; i++) {
				message.addRecipient(Message.RecipientType.TO, new InternetAddress(elements[i]));
			}

			// Send email (commented out line is if not authenticated above)
			Transport.send(message);

		} catch (MessagingException ex) {
			JOptionPane.showMessageDialog(getParent(), "ERROR sending email: " + ex.getMessage());
		}

		// Set cursor back to default
		this.setCursor(Cursor.getDefaultCursor());
	}
}
