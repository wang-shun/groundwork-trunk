function updateClock ( )
	{
	var months = new Array(13);
	months[0]  = "January";
	months[1]  = "February";
	months[2]  = "March";
	months[3]  = "April";
	months[4]  = "May";
	months[5]  = "June";
	months[6]  = "July";
	months[7]  = "August";
	months[8]  = "September";
	months[9]  = "October";
	months[10] = "November";
	months[11] = "December";

	var days = new Array(7);
	days[0]  = "Sunday";
	days[1]  = "Monday";
	days[2]  = "Tuesday";
	days[3]  = "Wednesday";
	days[4]  = "Thursday";
	days[5]  = "Friday";
	days[6]  = "Saturday";

	var currentTime = new Date ( );

	var currentHours = currentTime.getHours ( );
	var currentMinutes = currentTime.getMinutes ( );
	// var currentSeconds = currentTime.getSeconds ( );
	var currentMonth = currentTime.getMonth ( );
	var currentYear = currentTime.getFullYear ( );
	var currentDate = currentTime.getDate( );
	var currentDay = currentTime.getDay( );
	var monthName = months[currentMonth];
	var dayName = days[currentDay];

	// Pad the hours, minutes, and seconds with leading zeros, if required
	currentHours = ( currentHours < 10 ? "0" : "" ) + currentHours;
	currentMinutes = ( currentMinutes < 10 ? "0" : "" ) + currentMinutes;
	// currentSeconds = ( currentSeconds < 10 ? "0" : "" ) + currentSeconds;

	// Choose either "AM" or "PM" as appropriate (but a 24-hour clock is more sensible)
	// var timeOfDay = ( currentHours < 12 ) ? "AM" : "PM";

	// Convert the hours component to 12-hour format if needed
	// currentHours = ( currentHours > 12 ) ? currentHours - 12 : currentHours;

	// Convert an hours component of "0" to "12"
	// currentHours = ( currentHours == 0 ) ? 12 : currentHours;

	// Compose the string for display
	var currentTimeString = dayName + ", " + monthName + " " + currentDate + ", " + currentYear +
		" at " + currentHours + ":" + currentMinutes
		// + ":" + currentSeconds + " " + timeOfDay
		;

	// Update the time display
	document.getElementById("clock").firstChild.nodeValue = currentTimeString;
	}
