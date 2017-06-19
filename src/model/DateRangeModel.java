package model;

import java.io.Serializable;

public class DateRangeModel implements Serializable, Comparable<DateRangeModel> {
	private static final long serialVersionUID = 12340002L;
	private int personID;
	private String startDate;
	private String endDate;
	private ListStatus elementStatus;

	public DateRangeModel(int personID, String startDate, String endDate) {
		this.personID = personID;
		this.startDate = startDate;
		this.endDate = endDate;
		this.elementStatus = ListStatus.LIST_ELEMENT_ASSIGNED;
	}

	public int getPersonID() {
		return personID;
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

	public ListStatus getElementStatus() {
		return elementStatus;
	}

	public void setElementStatus(ListStatus elementStatus) {
		this.elementStatus = elementStatus;
	}

	public int compareTo(DateRangeModel otherDate) {
		int dateCompareStart = this.startDate.compareTo(otherDate.getStartDate());
		int dateCompareEnd = this.endDate.compareTo(otherDate.getEndDate());

		if (dateCompareStart != 0)
			return dateCompareStart;
		else
			return dateCompareEnd;
	}
}
