package model;

import java.io.Serializable;

public class DateRangeModel implements Serializable{
	private static final long serialVersionUID = 12340001L;	
	private String startDate;
	private String endDate;
	
	public DateRangeModel (String startDate, String endDate) {
		this.startDate = startDate;
		this.endDate = endDate;
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
