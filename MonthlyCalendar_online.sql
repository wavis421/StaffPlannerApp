BEGIN
	DECLARE firstDow INT Default (DAYOFWEEK(startDate) - 1);
	DECLARE numDaysInMonth INT Default (DAYOFMONTH(LAST_DAY(startDate)));
	DECLARE thisDay INT Default DAYOFMONTH(startDate);
	DECLARE currDate DATE Default startDate;
	DECLARE currDow INT Default firstDow;
	DECLARE currDowInMonth INT Default 0;

	# Create MonthTable 
	CREATE TEMPORARY TABLE MonthTable (
		Today INT(11),
		TaskName VARCHAR(30),
		TaskID INT(11),
		TaskHour INT(11),
		TaskMinute INT(11),
		PersonCount INT(11),
		LeaderCount INT(11),
		NumPersonsReqd INT(11),
		NumLdrsReqd INT(11)
	);

	# Insert tasks and floaters into table for each day of month
	WHILE (thisDay <= numDaysInMonth) DO
		INSERT INTO MonthTable
		   (Today, TaskName, TaskID, TaskHour, TaskMinute, PersonCount, LeaderCount, NumPersonsReqd, NumLdrsReqd)

		   # Select Tasks by DOW and WOM
		   (SELECT thisDay AS Today, Tasks.TaskName AS TaskName, Tasks.TaskID AS TaskID,
				Tasks.Hour AS TaskHour, Tasks.Minute AS TaskMinute, 
				# *** this select should also increment LEADER count for each personCnt that is a LEADER.
				# *** ie. if (Persons.PersonID = AssignedTasks.PersonID AND Persons.isLeader = 1)
				# ***     then also increment LEADER count for each non-zero personCnt!!
				(SELECT COUNT(*) FROM AssignedTasks WHERE AssignedTasks.TaskID = Tasks.TaskID
					AND (AssignedTasks.DaysOfWeek & (1 << currDow)) != 0
					AND (AssignedTasks.DowInMonth & (1 << currDowInMonth)) != 0) AS PersonCount, 
				0 AS LeaderCount,
				Tasks.TotalPersonsReqd AS NumPersonsReqd, Tasks.NumLeadersReqd AS NumLdrsReqd
			FROM Tasks, Programs
			WHERE (Tasks.ProgramID = Programs.ProgramID   # Check if program expired
					AND ((Programs.StartDate IS NULL) OR (currDate >= Programs.StartDate)) 
					AND ((Programs.EndDate IS NULL) OR (currDate <= Programs.EndDate)))
				AND ((Tasks.DaysOfWeek & (1 << currDow)) != 0)
				AND ((Tasks.DowInMonth & (1 << currDowInMonth)) != 0)

		   ) UNION

		   # Floater tasks determined by unassigned TaskID (NULL) and matching date
		   (SELECT thisDay, NULL AS TaskName, SingleInstanceTasks.TaskID AS TaskID,
				HOUR(SingleTime), MINUTE(SingleTime), COUNT(*) AS PersonCount, 0, 0, 0
			FROM SingleInstanceTasks, Programs
			WHERE (SingleInstanceTasks.ProgramID = Programs.ProgramID 
					AND ((Programs.StartDate IS NULL) OR (currDate >= Programs.StartDate)) 
					AND ((Programs.EndDate IS NULL) OR (currDate <= Programs.EndDate)))
				AND TaskID IS NULL 
				AND SingleDate = currDate
			GROUP BY SingleTime
   		   )

		   ORDER BY TaskHour, TaskMinute, TaskName;

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
	
	DROP TABLE MonthTable;
END