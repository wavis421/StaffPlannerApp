
DELIMITER $$
DROP PROCEDURE IF EXISTS CreateTables$$
CREATE PROCEDURE CreateTables()
BEGIN
	
	CREATE TABLE IF NOT EXISTS Programs (
		ProgramID int(11) NOT NULL AUTO_INCREMENT,
		PRIMARY KEY (ProgramID),
		
		ProgramName varchar(60) DEFAULT NULL,
		StartDate date DEFAULT NULL,
		EndDate date DEFAULT NULL
	);
	
	CREATE TABLE IF NOT EXISTS Persons (
		PersonID int(11) NOT NULL AUTO_INCREMENT,
		PRIMARY KEY (PersonID),
		
		PersonName varchar(50) DEFAULT NULL,
		PhoneNumber varchar(12) DEFAULT NULL,
		EMail varchar(50) DEFAULT NULL,
		isLeader tinyint(1) DEFAULT NULL,
		Notes varchar(140) DEFAULT NULL
	);
	
	CREATE TABLE IF NOT EXISTS Tasks (
		TaskID int(11) NOT NULL AUTO_INCREMENT,
		PRIMARY KEY (TaskID),
		
		ProgramID int(11),
		INDEX prog_idx (ProgramID),
		FOREIGN KEY (ProgramID) REFERENCES Programs(ProgramID) 
			ON DELETE CASCADE
			ON UPDATE CASCADE,
		
		# Task data
		TaskName varchar(30) DEFAULT NULL,
		Location varchar(30) DEFAULT NULL,
		Color int(11) DEFAULT 0,
		NumLeadersReqd int(11) DEFAULT NULL,
		TotalPersonsReqd int(11) DEFAULT NULL,
		DaysOfWeek int(11) DEFAULT NULL,
		DowInMonth int(11) DEFAULT NULL,
		Hour int(11) DEFAULT NULL,
		Minute int(11) DEFAULT NULL
	);
	
	CREATE TABLE IF NOT EXISTS SingleInstanceTasks (
		SingleInstanceID int(11) NOT NULL AUTO_INCREMENT,
		PRIMARY KEY (SingleInstanceID),
		
		ProgramID int(11),
		INDEX prog_idx (ProgramID),
		FOREIGN KEY (ProgramID) REFERENCES Programs(ProgramID) 
			ON DELETE CASCADE
			ON UPDATE CASCADE,
		
		TaskID int(11),
		INDEX task_idx (TaskID),
		FOREIGN KEY (TaskID) REFERENCES Tasks(TaskID) 
			ON DELETE CASCADE
			ON UPDATE CASCADE,
		
		PersonID int(11),
		INDEX person_idx (PersonID),
		FOREIGN KEY (PersonID) REFERENCES Persons(PersonID) 
			ON DELETE CASCADE
			ON UPDATE CASCADE,
		
		SingleDate date DEFAULT NULL,
		SingleTime time DEFAULT NULL,
		Color int(11) DEFAULT 0
	);
	
	CREATE TABLE IF NOT EXISTS AssignedTasks (
		AssignedTaskID int(11) NOT NULL AUTO_INCREMENT,
		PRIMARY KEY (AssignedTaskID),
		
		PersonID int(11),
		INDEX person_idx (PersonID),
		FOREIGN KEY (PersonID) REFERENCES Persons(PersonID) 
			ON DELETE CASCADE
			ON UPDATE CASCADE,
		
		TaskID int(11),
		INDEX task_idx (TaskID),
		FOREIGN KEY (TaskID) REFERENCES Tasks(TaskID) 
			ON DELETE CASCADE
			ON UPDATE CASCADE,
		
		DaysOfWeek int(11) DEFAULT NULL,
		DowInMonth int(11) DEFAULT NULL
	);
	
	CREATE TABLE IF NOT EXISTS UnavailDates (
		UnavailDatesID int(11) NOT NULL AUTO_INCREMENT,
		PRIMARY KEY (UnavailDatesID),
		
		PersonID int(11),
		INDEX person_idx (PersonID),
		FOREIGN KEY (PersonID) REFERENCES Persons(PersonID) 
			ON DELETE CASCADE
			ON UPDATE CASCADE,
		
		StartDate date DEFAULT NULL,
		EndDate date DEFAULT NULL
	);

END$$
DELIMITER ;
