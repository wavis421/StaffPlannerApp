
DELIMITER $$
DROP PROCEDURE IF EXISTS CreateTables$$
CREATE PROCEDURE CreateTables()
BEGIN
	
	CREATE TABLE IF NOT EXISTS Programs (
		ProgramID int(11) NOT NULL AUTO_INCREMENT,
		PRIMARY KEY (ProgramID),
		
		# Program data
		ProgramName varchar(60) DEFAULT NULL,
		StartDate date DEFAULT NULL,
		EndDate date DEFAULT NULL,

		UNIQUE KEY (ProgramName)
	) ENGINE=InnoDB;
	
	CREATE TABLE IF NOT EXISTS Persons (
		PersonID int(11) NOT NULL AUTO_INCREMENT,
		PRIMARY KEY (PersonID),
		
		# Person data
		PersonName varchar(50) DEFAULT NULL,
		PhoneNumber varchar(12) DEFAULT NULL,
		EMail varchar(50) DEFAULT NULL,
		isLeader tinyint(1) DEFAULT NULL,
		Notes varchar(140) DEFAULT NULL,
		
		UNIQUE KEY (PersonName)
	) ENGINE=InnoDB;
	
	CREATE TABLE IF NOT EXISTS Tasks (
		TaskID int(11) NOT NULL AUTO_INCREMENT,
		PRIMARY KEY (TaskID),
		ProgramID int(11),
		
		CONSTRAINT fk_tasks_program_id
			FOREIGN KEY (ProgramID) 
			REFERENCES Programs(ProgramID) 
			ON DELETE CASCADE,
		
		# Task data
		TaskName varchar(30) DEFAULT NULL,
		Location varchar(30) DEFAULT NULL,
		Color int(11) DEFAULT 0,
		NumLeadersReqd int(11) DEFAULT NULL,
		TotalPersonsReqd int(11) DEFAULT NULL,
		DaysOfWeek int(11) DEFAULT NULL,
		DowInMonth int(11) DEFAULT NULL,
		Hour int(11) DEFAULT NULL,
		Minute int(11) DEFAULT NULL,
				
		UNIQUE KEY (TaskName)
	) ENGINE=InnoDB;
	
	CREATE TABLE IF NOT EXISTS SingleInstanceTasks (
		SingleInstanceID int(11) NOT NULL AUTO_INCREMENT,
		PRIMARY KEY (SingleInstanceID),
		ProgramID int(11),
		TaskID int(11),
		PersonID int(11),
		
		CONSTRAINT fk_single_program_id
			FOREIGN KEY (ProgramID) 
			REFERENCES Programs(ProgramID) 
			ON DELETE CASCADE,
		
		CONSTRAINT fk_single_task_id
			FOREIGN KEY (TaskID) 
			REFERENCES Tasks(TaskID) 
			ON DELETE CASCADE,
		
		CONSTRAINT fk_single_person_id
			FOREIGN KEY (PersonID) 
			REFERENCES Persons(PersonID) 
			ON DELETE CASCADE,
		
		# Single instance task data
		SingleDate date DEFAULT NULL,
		SingleTime time DEFAULT NULL,
		Color int(11) DEFAULT 0
	) ENGINE=InnoDB;
	
	CREATE TABLE IF NOT EXISTS AssignedTasks (
		AssignedTaskID int(11) NOT NULL AUTO_INCREMENT,
		PRIMARY KEY (AssignedTaskID),
		PersonID int(11),
		TaskID int(11),
		
		CONSTRAINT fk_assigned_person_id
			FOREIGN KEY (PersonID) 
			REFERENCES Persons(PersonID) 
			ON DELETE CASCADE,
		
		CONSTRAINT fk_assigned_task_id
			FOREIGN KEY (TaskID) 
			REFERENCES Tasks(TaskID) 
			ON DELETE CASCADE,
		
		# Assigned task data
		DaysOfWeek int(11) DEFAULT NULL,
		DowInMonth int(11) DEFAULT NULL,
				
		UNIQUE KEY (PersonID, TaskID)
	) ENGINE=InnoDB;
	
	CREATE TABLE IF NOT EXISTS UnavailDates (
		UnavailDatesID int(11) NOT NULL AUTO_INCREMENT,
		PRIMARY KEY (UnavailDatesID),
		PersonID int(11),
		
		CONSTRAINT fk_unavail_person_id
			FOREIGN KEY (PersonID) 
			REFERENCES Persons(PersonID) 
			ON DELETE CASCADE,
		
		# Unavail dates data
		StartDate date DEFAULT NULL,
		EndDate date DEFAULT NULL,
				
		UNIQUE KEY (PersonID, StartDate, EndDate)
	) ENGINE=InnoDB;

END$$
DELIMITER ;
