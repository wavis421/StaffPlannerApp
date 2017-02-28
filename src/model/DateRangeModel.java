package model;

import java.io.Serializable;

public class DateRangeModel implements Serializable{
	private static final long serialVersionUID = 12340002L;
	private int unavailDatesID, personID;
	private String startDate;
	private String endDate;
	
	public DateRangeModel (int unavailDatesID, int personID, String startDate, String endDate) {
		this.unavailDatesID = unavailDatesID;
		this.personID = personID;
		this.startDate = startDate;
		this.endDate = endDate;
	}

	public void setUnavailDatesID(int id) {
		unavailDatesID = id;
	}
	
	public void setPersonID(int id) {
		personID = id;
	}
	
	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}
}
