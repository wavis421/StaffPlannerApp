# Hard-coded to 2013-03-07

# Select tasks by DOW and WOM
SELECT PersonName,
		isLeader AS Leader,
		Tasks.TaskID,
		TaskName,
		Hour,
		Minute,
		Location,
		PhoneNumber,
		EMail,
		NULL AS SingleDate
		
	FROM Tasks, Persons, Programs, AssignedTasks
	
	WHERE (Tasks.ProgramID = Programs.ProgramID   # Check if program expired
			AND ((Programs.StartDate IS NULL) OR ('2017-03-07' >= Programs.StartDate)) 
			AND ((Programs.EndDate IS NULL) OR ('2017-03-07' <= Programs.EndDate)))
		# Check whether task is active today
		AND (Tasks.DaysOfWeek & (1 << 2)) != 0
		AND (Tasks.DowInMonth & (1 << 0)) != 0
		# Check if assigned task is active today
		AND Tasks.TaskID = AssignedTasks.TaskID
		AND (AssignedTasks.DaysOfWeek & (1 << 2)) != 0
		AND (AssignedTasks.DowInMonth & (1 << 0)) != 0
		# Find associated person
		AND Persons.PersonID = AssignedTasks.PersonID
	
UNION

	# Floater tasks determined by unassigned TaskID (NULL) and matching date
	SELECT PersonName,
		isLeader AS Leader,
		SingleInstanceTasks.TaskID,
		TaskName,
		Hour,
		Minute,
		Location,
		PhoneNumber,
		EMail,
		SingleDate

	FROM Tasks, Persons, Programs, SingleInstanceTasks
	
	WHERE (Tasks.ProgramID = Programs.ProgramID   # Check if program expired
			AND ((Programs.StartDate IS NULL) OR ('2017-03-07' >= Programs.StartDate)) 
			AND ((Programs.EndDate IS NULL) OR ('2017-03-07' <= Programs.EndDate)))
		AND (SingleInstanceTasks.TaskID = Tasks.TaskID OR SingleInstanceTasks.TaskID IS NULL)
		AND SingleDate = '2017-03-07'
		AND SingleInstanceTasks.PersonID = Persons.PersonID

	GROUP BY PersonName, TaskID
	
ORDER BY PersonName, TaskName;
		
		
