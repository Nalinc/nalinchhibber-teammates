var COURSE_ID_MAX_LENGTH = 30;
var COURSE_NAME_MAX_LENGTH = 38;
var EVAL_NAME_MAX_LENGTH = 38;

// Field names
var COURSE_ID = "courseid"; // Used in instructorCourse.js
var COURSE_NAME = "coursename"; // Used in instructorCourse.js
var COURSE_INSTRUCTOR_LIST = "instructorlist"; // Used in instructorCourse.js
var COURSE_INSTRUCTOR_ID = "instructorid"; // Used in instructorCourse.js

var EVALUATION_START = "start"; // Used in instructorEval.js
var EVALUATION_STARTTIME = "starttime"; // Used in instructorEval.js
var EVALUATION_TIMEZONE = "timezone"; // Used in instructorEval.js

// Display messages
// Used for validating input
var DISPLAY_INPUT_FIELDS_EXTRA = "There are too many fields.";
var DISPLAY_INPUT_FIELDS_MISSING = "There are missing fields.";
var DISPLAY_GOOGLEID_INVALID = "GoogleID should only consist of alphanumerics, fullstops, dashes or underscores.";
var DISPLAY_EMAIL_INVALID = "The e-mail address is invalid.";
var DISPLAY_NAME_INVALID = "Name should only consist of alphanumerics or hyphens, apostrophes, fullstops, commas, slashes, round brackets<br> and not more than 40 characters.";
var DISPLAY_STUDENT_TEAMNAME_INVALID = "Team name should contain less than 25 characters.";

// Used in instructorCourse.js only
var DISPLAY_COURSE_MISSING_FIELD = "Course ID and Course Name are compulsory fields.";
var DISPLAY_COURSE_LONG_ID = "Course ID should not exceed "
		+ COURSE_ID_MAX_LENGTH + " characters.";
var DISPLAY_COURSE_LONG_NAME = "Course name should not exceed "
		+ COURSE_NAME_MAX_LENGTH + " characters.";
var DISPLAY_COURSE_INVALID_ID = "Please use only alphabets, numbers, dots, hyphens, underscores and dollar signs in course ID.";
var DISPLAY_COURSE_INSTRUCTOR_LIST_EMPTY = "You must add at least 1 instructor in the course.";
var MESSAGE_INSTRUCTOR_NOT_WHTHIN_INSTRUCTOR_LIST = "You are NOT in the list of instructors for the new course. You will NOT be able to access the new course once it is created. Do you wish to continue?";

// Used in instructorCourseEnroll.js only
var DISPLAY_ENROLLMENT_INPUT_EMPTY = "Please input at least one student detail.";

// Used in instructorEval.js only
var DISPLAY_EVALUATION_NAMEINVALID = "Please use only alphabets, numbers and whitespace in evaluation name.";
var DISPLAY_EVALUATION_NAME_LENGTHINVALID = "Evaluation name should not exceed 38 characters.";
var DISPLAY_EVALUATION_SCHEDULEINVALID = "The evaluation schedule (start/deadline) is not valid.<br />"
		+ "The start time should be in the future, and the deadline should be after start time.";
var DISPLAY_FIELDS_EMPTY = "Please fill in all the relevant fields.";
var DISPLAY_INVALID_INPUT = "Unexpected error. Invalid Input";

// Max length for input
var TEAMNAME_MAX_LENGTH = 24;
var NAME_MAX_LENGTH = 40;

/** ---------------------------- Sorting Functions --------------------------* */
/**
 * jQuery.fn.sortElements --------------
 * 
 * @author James Padolsey (http://james.padolsey.com)
 * @version 0.11
 * @updated 18-MAR-2010 --------------
 * @param Function
 *            comparator: Exactly the same behaviour as [1,2,3].sort(comparator)
 * 
 * @param Function
 *            getSortable A function that should return the element that is to
 *            be sorted. The comparator will run on the current collection, but
 *            you may want the actual resulting sort to occur on a parent or
 *            another associated element.
 * 
 * E.g. $('td').sortElements(comparator, function(){ return this.parentNode; })
 * 
 * The
 * <td>'s parent (
 * <tr>) will be sorted instead of the
 * <td> itself.
 */
$.fn.sortElements = (function() {
	var sort = [].sort;

	return function(comparator, getSortable) {
		getSortable = getSortable || function() {
			return this;
		};
		var placements = this
				.map(function() {
					var sortElement = getSortable.call(this), parentNode = sortElement.parentNode,

					// Since the element itself will change position, we have
					// to have some way of storing it's original position in
					// the DOM. The easiest way is to have a 'flag' node:
					nextSibling = parentNode.insertBefore(document
							.createTextNode(''), sortElement.nextSibling);

					return function() {
						if (parentNode === this) {
							throw new Error(
									"You can't sort elements if any one is a descendant of another.");
						}
						// Insert before flag:
						parentNode.insertBefore(this, nextSibling);
						// Remove flag:
						parentNode.removeChild(nextSibling);
					};
				});
		return sort.call(this, comparator).each(function(i) {
			placements[i].call(getSortable.call(this));
		});
	};
})();

/**
 * Sorts a table
 * 
 * @param divElement
 *            The sort button
 * @param colIdx
 *            The column index (1-based) as key for the sort
 */
function toggleSort(divElement, colIdx, comparator) {
	if ($(divElement).attr("class") == "buttonSortNone") {
		sortTable(divElement, colIdx, comparator);
		$(".buttonSortAscending").attr("class", "buttonSortNone");
		$(".buttonSortDescending").attr("class", "buttonSortNone");
		$(divElement).attr("class", "buttonSortAscending");
	} else if ($(divElement).attr("class") == "buttonSortAscending") {
		sortTable(divElement, colIdx, sortBaseCellDescending);
		$(divElement).attr("class", "buttonSortDescending");
	} else {
		sortTable(divElement, colIdx, comparator);
		$(divElement).attr("class", "buttonSortAscending");
	}
}

/**
 * Sorts a table based on certain column and comparator
 * 
 * @param oneOfTableCell
 *            One of the table cell
 * @param colIdx
 *            The column index (1-based) as key for the sort
 * @param comparator
 *            This will be used to compare two cells. If not specified,
 *            sortBaseCell will be used
 */
function sortTable(oneOfTableCell, colIdx, comparator) {
	if (!comparator)
		comparator = sortBaseCellAscending;
	var table = $(oneOfTableCell);
	if (!table.is("table")) {
		table = $(oneOfTableCell).parentsUntil("table");
		table = $(table[table.length - 1].parentNode);
	}
	keys = $("td:nth-child(" + colIdx + ")", table);
	keys.sortElements(comparator, function() {
		return this.parentNode;
	});
}

/**
 * The base comparator for a cell (ascending)
 * 
 * @param cell1
 * @param cell2
 * @returns returns if cell1.innerHTML is larger than cell2.innerHTML
 */
function sortBaseCellAscending(cell1, cell2) {
	return sortBase(cell1.innerHTML, cell2.innerHTML);
}

/**
 * The base comparator for a cell (descending)
 * 
 * @param cell1
 * @param cell2
 * @returns returns the opposite of sortBaseAscending
 */
function sortBaseCellDescending(cell1, cell2) {
	return sortBaseCellAscending(cell2, cell1);
}

/**
 * The base comparator (ascending)
 * 
 * @param x
 * @param y
 * @returns
 */
function sortBase(x, y) {
	return ((x < y) ? -1 : ((x > y) ? 1 : 0));
}

/**
 * Comparator to sort strings in format: E([+-]x%) | N/A | N/S | 0% with
 * possibly a tag that surrounds it.
 * 
 * @param a
 * @param b
 */
function sortByPoint(a, b) {
	a = getPointValue(a, true);
	b = getPointValue(b, true);

	return sortBase(a, b);
}

/**
 * Comparator to sort strings in format: [+-]x% | N/A with possibly a tag that
 * surrounds it.
 * 
 * @param a
 * @param b
 */
function sortByDiff(a, b) {
	a = getPointValue(a, false);
	b = getPointValue(b, false);

	return sortBase(a, b);
}

/**
 * To get point value from a formatted string
 * 
 * @param s
 *            A table cell (td tag) that contains the formatted string
 * @param ditchZero
 *            Whether 0% should be treated as lower than -90 or not
 * @returns
 */
function getPointValue(s, ditchZero) {
	s = s.innerHTML;
	if (s.lastIndexOf("<") != -1) {
		s = s.substring(0, s.lastIndexOf("<"));
		s = s.substring(s.lastIndexOf(">") + 1);
	}
	if (s.indexOf("/") != -1) {
		if (s.indexOf("S") != -1)
			return 999; // Case N/S
		return 1000; // Case N/A
	}
	if (s == "0%") { // Case 0%
		if (ditchZero)
			return 0;
		else
			return 100;
	}
	s = s.replace("E", "");
	s = s.replace("%", "");
	if (s == "")
		return 100; // Case E
	return 100 + eval(s); // Other typical cases
}

/** -----------------------UI Related Helper Functions-----------------------* */

var DIV_TOPOFPAGE = "topOfPage";
/**
 * Scrolls the screen to top
 */
function scrollToTop() {
	document.getElementById(DIV_TOPOFPAGE).scrollIntoView(true);
}

/** Selector for status message div tag (to be used in JQuery) */
var DIV_STATUS_MESSAGE = "#statusMessage";

/**
 * Sets a status message. Change the background color to red if it's an error
 * 
 * @param message
 * @param error
 */
function setStatusMessage(message, error) {
	if (message == "") {
		clearStatusMessage();
		return;
	}
	$(DIV_STATUS_MESSAGE).html(message);
	$(DIV_STATUS_MESSAGE).show();
	if (error === true) {
		$(DIV_STATUS_MESSAGE).attr("style",
				"display: block; background-color: rgb(255, 153, 153);");
	} else {
		$(DIV_STATUS_MESSAGE).attr("style",
				"display: block; background-color: transparent;");
	}
}

/**
 * Clears the status message div tag and hides it
 */
function clearStatusMessage() {
	$(DIV_STATUS_MESSAGE).html("");
	$(DIV_STATUS_MESSAGE).css("background", "");
	$(DIV_STATUS_MESSAGE).attr("style", "display: none;");
}

/**
 * Function to check whether the evaluation submission edit/submit form has been
 * fully filled (no unfilled textarea and dropdown box)
 * 
 * @returns {Boolean}
 */
function checkEvaluationForm() {
	points = $("select");
	comments = $("textarea");
	for ( var i = 0; i < points.length; i++) {
		if (points[i].value == '') {
			setStatusMessage("Please give contribution scale to everyone", true);
			return false;
		}
	}
	return true;
}

/**
 * Sanitize GoogleID by trimming space and '@gmail.com'
 * Used in instructorCourse, instructorCourseEdit, adminHome
 * 
 * @param googleId
 * @returns sanitizedGoolgeId
 */
function sanitizeGoogleId(googleId) {
	googleId = googleId.trim();
	var loc = googleId.toLowerCase().indexOf("@gmail.com");
	if (loc > -1) {
		googleId = googleId.substring(0, loc);   
	}
	return googleId.trim();
}

/**
 * Check if the GoogleID is valid
 * GoogleID allow only alphanumeric, full stops, dashes, underscores or valid email
 * 
 * @param googleId
 * @return {Boolean}
 */
function isValidGoogleId(googleId) {
	var isValidNonEmailGoogleId = false;
	googleId = googleId.trim();
	
	// match() retrieve the matches when matching a string against a regular expression.
	var matches = googleId.match(/^([\w-]+(?:\.[\w-]+)*)/);
	
	isValidNonEmailGoogleId = (matches != null && matches[0] == googleId);
	
	var isValidEmailGoogleId = isEmailValid(googleId);
	if (googleId.indexOf("@gmail.com") > -1) {
		isValidEmailGoogleId = false;
	}
	
	// email addresses are valid google IDs too
	return isValidNonEmailGoogleId || isValidEmailGoogleId;
}

/**
 * Checks whether an e-mail is valid. Used in instructorCourseEnroll page
 * (through instructorCourseEnroll.js)
 * 
 * @param email
 * @returns {Boolean}
 */
function isEmailValid(email) {
	return email
			.match(/^([\w-]+(?:\.[\w-]+)*)@((?:[\w-]+\.)*\w[\w-]{0,66})\.([a-z]{2,6}(?:\.[a-z]{2})?)$/i) != null;
}

/**
 * Checks whether a student's name is valid Used in instructorCourseEnroll page
 * (through instructorCourseEnroll.js)
 * 
 * @param name
 * @returns {Boolean}
 */
function isNameValid(name) {
	name = name.trim();

	if (name == "") {
		return false;
	}
	if (name.match(/[^\/\\,.'\-\(\)0-9a-zA-Z \t]/)) {
		// Returns true if a character NOT belonging to the following set
		// appears in the name: slash(/), backslash(\), fullstop(.), comma(,),
		// apostrophe('), hyphen(-), round brackets(()), alpha numeric
		// characters, space, tab
		return false;
	} else if (name.length > NAME_MAX_LENGTH) {
		return false;
	} else {
		return true;
	}
}