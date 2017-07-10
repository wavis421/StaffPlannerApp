package model;

public class ListStatus {
	// Define STATUS to show changes to list elements.
	
	// Previously declared as an enum, but this seemed
	// to take a lot more memory!!
	private static final int LIST_ELEMENT_ASSIGNED = 1;
	private static final int LIST_ELEMENT_NEW = 2;
	private static final int LIST_ELEMENT_UPDATE = 3;
	private static final int LIST_ELEMENT_DELETE = 4;
	
	public static int elementAssigned() {
		return LIST_ELEMENT_ASSIGNED;
	}
	public static int elementNew() {
		return LIST_ELEMENT_NEW;
	}
	public static int elementUpdate() {
		return LIST_ELEMENT_UPDATE;
	}
	public static int elementDelete() {
		return LIST_ELEMENT_DELETE;
	}
}
