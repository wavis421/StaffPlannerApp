
DELIMITER $$
DROP PROCEDURE IF EXISTS MonthlyCalendarByProgram$$
CREATE PROCEDURE MonthlyCalendarByProgram(IN startDate DATE, IN programFilter VARCHAR(150))
BEGIN
	DECLARE firstDow INT Default (DAYOFWEEK(startDate) - 1);
	DECLARE numDaysInMonth INT Default (DAYOFMONTH(LAST_DAY(startDate)));
	DECLARE thisDay INT Default DAYOFMONTH(startDate);
	DECLARE currDate DATE Default startDate;
	DECLARE currDow INT Default firstDow;
	DECLARE currDowInMonth INT Default 0;

	# Create MonthTable 
	# TODO: Name table based on user to make unique
	CREATE TABLE IF NOT EXISTS MonthTable (
		Today INT(11),
		TaskName VARCHAR(30),
		TaskID INT(11),
		ProgramID INT(11),
		TaskHour INT(11),
		TaskMinute INT(11),
		PersonCount INT(11),
		LeaderCount INT(11),
		SubCount INT(11),
		UnavailCount INT(11),
		UnavailLdrCount INT(11),
		NumPersonsReqd INT(11),
		NumLdrsReqd INT(11),
		TaskColor INT(11),
		Location VARCHAR(30)
	);
	TRUNCATE MonthTable;

	# Insert tasks and floaters into table for each day of month
	WHILE (thisDay <= numDaysInMonth) DO
		INSERT INTO MonthTable
		   (Today, TaskName, TaskID, ProgramID, TaskHour, TaskMinute, PersonCount, LeaderCount, SubCount,
				UnavailCount, UnavailLdrCount, NumPersonsReqd, NumLdrsReqd, TaskColor, Location)

		   # Select Tasks by DOW and WOM
		   # TODO: Optimize, counting assigned tasks does not need to be done for each assigned task
		   (SELECT thisDay AS Today, Tasks.TaskName AS TaskName, Tasks.TaskID AS TaskID, Tasks.ProgramID AS ProgramID, 
				Tasks.Hour AS TaskHour, Tasks.Minute AS TaskMinute, 
				# Count all assigned persons who are available today
				(SELECT COUNT(*) FROM AssignedTasks WHERE AssignedTasks.TaskID = Tasks.TaskID
					AND (AssignedTasks.DaysOfWeek & (1 << currDow)) != 0
					AND (AssignedTasks.DowInMonth & (1 << currDowInMonth)) != 0) AS PersonCount, 
				# Count assigned persons who are leaders and are available today
				(SELECT COUNT(*) FROM AssignedTasks, Persons WHERE AssignedTasks.TaskID = Tasks.TaskID
					AND (AssignedTasks.DaysOfWeek & (1 << currDow)) != 0
					AND (AssignedTasks.DowInMonth & (1 << currDowInMonth)) != 0
					AND AssignedTasks.PersonID = Persons.PersonID
					AND Persons.isLeader = 1) AS LeaderCount,
				# Count number of substitutes
				(SELECT COUNT(*) FROM SingleInstanceTasks WHERE SingleInstanceTasks.TaskID IS NOT NULL
					AND SingleInstanceTasks.TaskID = Tasks.TaskID
					AND SingleDate = currDate) AS SubCount, 
				# Count number of persons unavailable
				(SELECT COUNT(*) FROM AssignedTasks, Persons, UnavailDates 
					WHERE Tasks.TaskID = AssignedTasks.TaskID 
					AND (AssignedTasks.DaysOfWeek & (1 << currDow)) != 0
					AND (AssignedTasks.DowInMonth & (1 << currDowInMonth)) != 0
					AND AssignedTasks.PersonID = Persons.PersonID
					AND ((SELECT COUNT(*) FROM Persons, UnavailDates WHERE Persons.PersonID = UnavailDates.PersonID) > 0
						AND Persons.PersonID = UnavailDates.PersonID
						AND currDate BETWEEN UnavailDates.StartDate AND UnavailDates.EndDate) 
				) AS UnavailCount, 
				# Count number of leaders unavailable
				(SELECT COUNT(*) FROM AssignedTasks, Persons, UnavailDates 
					WHERE Tasks.TaskID = AssignedTasks.TaskID 
					AND (AssignedTasks.DaysOfWeek & (1 << currDow)) != 0
					AND (AssignedTasks.DowInMonth & (1 << currDowInMonth)) != 0
					AND AssignedTasks.PersonID = Persons.PersonID
					AND Persons.isLeader = 1
					AND ((SELECT COUNT(*) FROM Persons, UnavailDates WHERE Persons.PersonID = UnavailDates.PersonID) > 0
						AND Persons.PersonID = UnavailDates.PersonID
						AND currDate BETWEEN UnavailDates.StartDate AND UnavailDates.EndDate) 
				) AS UnavailLdrCount, 
				Tasks.TotalPersonsReqd AS NumPersonsReqd, Tasks.NumLeadersReqd AS NumLdrsReqd, Tasks.Color AS TaskColor,
				Tasks.Location AS Location
			FROM Tasks, Programs, Persons
			WHERE ((Tasks.DaysOfWeek & (1 << currDow)) != 0)
				AND ((Tasks.DowInMonth & (1 << currDowInMonth)) != 0)
				AND FIND_IN_SET(Programs.ProgramName, programFilter)
				AND (Tasks.ProgramID = Programs.ProgramID   # Check if program expired
					AND ((Programs.StartDate IS NULL) OR (currDate >= Programs.StartDate)) 
					AND ((Programs.EndDate IS NULL) OR (currDate <= Programs.EndDate)))
			GROUP BY Tasks.TaskID
		   );
		
		# Increment by day
		SET currDate = DATE_ADD(currDate, INTERVAL 1 DAY); 
		SET thisDay = thisDay + 1;

		# Wrap day-of-week
		SET currDow = currDow + 1;
		IF (currDow > 6) THEN
			SET currDow = 0;
		END IF;

		# Wrap day-of-week in month
		IF (currDow = firstDow) THEN
			SET currDowInMonth = currDowInMonth + 1;
		END IF;
	END WHILE;
					
	# Select ALL from this month's table
	SELECT * FROM MonthTable ORDER BY Today, TaskHour, TaskMinute, TaskName;
END$$
DELIMITER ;
