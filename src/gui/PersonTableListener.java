package gui;

public interface PersonTableListener {
	public void addPerson ();
	public void rowDeleted (int row);
	public void editRow (String personName);
	public void refresh();
}
