package gui;

import java.util.EventObject;

public class PersonEvent extends EventObject {
	private String name;
	private String phone;
	private String email;
	private boolean staff;
	private String notes;
	
	public PersonEvent(Object source) {
		super(source);
	}

	public PersonEvent(Object source, String name, String phone, String email, boolean staff, String notes) {
		super(source);

		this.name = name;
		this.phone = phone;
		this.email = email;
		this.staff = staff;
		this.notes = notes;
	}

	public String getName() {
		return name;
	}

	public String getPhone() {
		return phone;
	}

	public String getEmail() {
		return email;
	}

	public boolean isStaff() {
		return staff;
	}

	public String getNotes() {
		return notes;
	}
}
