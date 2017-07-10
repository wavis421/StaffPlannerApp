package model;

public class DateRangeModel implements Comparable<DateRangeModel> {
	private int personID;
	private String startDate;
	private String endDate;
	private int elementStatus;

	public DateRangeModel(int personID, String startDate, String endDate) {
		this.personID = personID;
		this.startDate = startDate;
		this.endDate = endDate;
		this.elementStatus = ListStatus.elementAssigned();
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

	public int getElementStatus() {
		return elementStatus;
	}

	public void setElementStatus(int elementStatus) {
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
