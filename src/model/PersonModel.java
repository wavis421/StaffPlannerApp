package model;

import java.io.Serializable;

public class PersonModel implements Serializable {
	private String name;
	private String phone;
	private String email;
	private boolean staff;  // Staff or volunteer
	private String notes;
	
	public PersonModel (String name, String phone, String email, boolean staff, String notes) {
		this.name = name;
		this.phone = phone;
		this.email = email;
		this.staff = staff;
		this.notes = notes;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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
