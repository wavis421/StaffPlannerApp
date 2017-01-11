package gui;

import java.util.EventObject;

public class EmailSettingsEvent extends EventObject {
	private String userName;
	private char[] password;
	private int portNumber;

	public EmailSettingsEvent(Object source) {
		super(source);
	}

	public EmailSettingsEvent(Object source, String userName, char[] password, int portNumber) {
		super(source);

		this.userName = userName;
		this.password = password;
		this.portNumber = portNumber;
	}

	public String getUserName() {
		return userName;
	}

	public char[] getPassword() {
		return password;
	}

	public int getPortNumber() {
		return portNumber;
	}
}
