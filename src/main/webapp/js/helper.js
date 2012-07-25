/*
 * Helper functions for Teammates Require: jquery
 */

/**
 * Format a number to be two digits
 */
function formatDigit(num){
	return (num<10?"0":"")+num;
}

/**
 * Format a date object into DD/MM/YYYY format
 * @param date
 * @returns {String}
 */
function convertDateToDDMMYYYY(date) {
	return formatDigit(date.getDate()) + "/" +
			formatDigit(date.getMonth()+1) + "/" +
			date.getFullYear();
}

/**
 * Format a date object into HHMM format
 * @param date
 * @returns {String}
 */
function convertDateToHHMM(date) {
	return formatDigit(date.getHours()) + formatDigit(date.getMinutes());
}

/**
 * Returns Date object that shows the current time at specific timeZone
 * @param timeZone
 * @returns {Date}
 */
function getDateWithTimeZoneOffset(timeZone) {
	var now = new Date();

	// Convert local time zone to ms
	var nowTime = now.getTime();

	// Obtain local time zone offset
	var localOffset = now.getTimezoneOffset() * 60000;

	// Obtain UTC time
	var UTC = nowTime + localOffset;

	// Add the time zone of evaluation
	var nowMilliS = UTC + (timeZone * 60 * 60 * 1000);

	now.setTime(nowMilliS);

	return now;
}

/**---------------------------- Sorting Functions --------------------------**/
/**
 * jQuery.fn.sortElements
 * --------------
 * @author James Padolsey (http://james.padolsey.com)
 * @version 0.11
 * @updated 18-MAR-2010
 * --------------
 * @param Function comparator:
 *   Exactly the same behaviour as [1,2,3].sort(comparator)
 *   
 * @param Function getSortable
 *   A function that should return the element that is
 *   to be sorted. The comparator will run on the
 *   current collection, but you may want the actual
 *   resulting sort to occur on a parent or another
 *   associated element.
 *   
 *   E.g. $('td').sortElements(comparator, function(){
 *      return this.parentNode; 
 *   })
 *   
 *   The <td>'s parent (<tr>) will be sorted instead
 *   of the <td> itself.
 */
$.fn.sortElements = (function(){
	var sort = [].sort;

	return function(comparator, getSortable) {
		getSortable = getSortable || function(){return this;};
		var placements = this.map(function(){
			var sortElement = getSortable.call(this),
			parentNode = sortElement.parentNode,

			// Since the element itself will change position, we have
			// to have some way of storing it's original position in
			// the DOM. The easiest way is to have a 'flag' node:
			nextSibling = parentNode.insertBefore(
					document.createTextNode(''),
					sortElement.nextSibling
			);

			return function() {
				if (parentNode === this) {
					throw new Error(
							"You can't sort elements if any one is a descendant of another."
					);
				}
				// Insert before flag:
				parentNode.insertBefore(this, nextSibling);
				// Remove flag:
				parentNode.removeChild(nextSibling);
			};
		});
		return sort.call(this, comparator).each(function(i){
			placements[i].call(getSortable.call(this));
		});
	};
})();

/**
 * Sorts a table
 * @param divElement
 * 		The sort button
 * @param colIdx
 * 		The column index (1-based) as key for the sort
 */
function toggleSort(divElement,colIdx,comparator) {
	sortTable(divElement,colIdx,comparator);
	$(".buttonSortAscending").attr("class","buttonSortNone");
	$(divElement).attr("class","buttonSortAscending");
}

/**
 * Sorts a table based on certain column and comparator
 * @param oneOfTableCell
 * 		One of the table cell
 * @param colIdx
 * 		The column index (1-based) as key for the sort
 * @param comparator
 * 		This will be used to compare two cells. If not specified,
 * 		sortBaseCell will be used
 */
function sortTable(oneOfTableCell, colIdx, comparator){
	if(!comparator) comparator = sortBaseCell;
	var table = $(oneOfTableCell);
	if(!table.is("table")){
		table = $(oneOfTableCell).parentsUntil("table");
		table = $(table[table.length-1].parentNode);
	}
	keys = $("td:nth-child("+colIdx+")",table);
	keys.sortElements( comparator, function(){return this.parentNode;} );
}

/**
 * The base comparator for a cell
 * @param cell1
 * @param cell2
 * @returns
 */
function sortBaseCell(cell1, cell2){
	cell1 = cell1.innerHTML;
	cell2 = cell2.innerHTML;
	return sortBase(cell1,cell2);
}

/**
 * The base comparator (ascending)
 * @param x
 * @param y
 * @returns
 */
function sortBase(x, y) {
	return ((x < y) ? -1 : ((x > y) ? 1 : 0));
}

/**
 * Comparator to sort strings in format: E([+-]x%) | N/A | N/S | 0%
 * with possibly a tag that surrounds it.
 * @param a
 * @param b
 */
function sortByPoint(a, b){
	a = getPointValue(a,true);
	b = getPointValue(b,true);
	
	return sortBase(a,b);
}

/**
 * Comparator to sort strings in format: [+-]x% | N/A
 * with possibly a tag that surrounds it.
 * @param a
 * @param b
 */
function sortByDiff(a, b){
	a = getPointValue(a,false);
	b = getPointValue(b,false);
	
	return sortBase(a,b);
}

/**
 * To get point value from a formatted string
 * @param s
 * 		A table cell (td tag) that contains the formatted string
 * @param ditchZero
 * 		Whether 0% should be treated as lower than -90 or not
 * @returns
 */
function getPointValue(s, ditchZero){
	s = s.innerHTML;
	if(s.lastIndexOf("<")!=-1){
		s = s.substring(0,s.lastIndexOf("<"));
		s = s.substring(s.lastIndexOf(">")+1);
	}
	if(s.indexOf("/")!=-1){
		if(s.indexOf("S")!=-1) return 999; // Case N/S
		return 1000; // Case N/A
	}
	if(s=="0%"){ // Case 0%
		if(ditchZero) return 0;
		else return 100;
	}
	s = s.replace("E","");
	s = s.replace("%","");
	if(s=="") return 100; // Case E
	return 100+eval(s); // Other typical cases
}

/**-----------------------UI Related Helper Functions-----------------------**/

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
 * Sets a status message.
 * Change the background color to red if it's an error
 * @param message
 * @param error
 */
function setStatusMessage(message, error) {
	if (message == "") {
		clearStatusMessage();
		return;
	}
	$(DIV_STATUS_MESSAGE).html(message);
	if(error===true){
		$(DIV_STATUS_MESSAGE).css("background","#FF9999");
	} else {
		$(DIV_STATUS_MESSAGE).css("background","");
	}
	$(DIV_STATUS_MESSAGE).show();
}

/**
 * Clears the status message div tag and hides it
 */
function clearStatusMessage() {
	$(DIV_STATUS_MESSAGE).html("").css("background","").hide();
}

/**
 * Checks whether an e-mail is valid.
 * Used in coordCourseEnroll page (through coordCourseEnroll.js)
 * @param email
 * @returns {Boolean}
 */
function isStudentEmailValid(email) {
	return email.match(/^([\w-]+(?:\.[\w-]+)*)@((?:[\w-]+\.)*\w[\w-]{0,66})\.([a-z]{2,6}(?:\.[a-z]{2})?)$/i)!=null;
}

/**
 * Checks whether a student's name is valid
 * Used in coordCourseEnroll page (through coordCourseEnroll.js)
 * @param name
 * @returns {Boolean}
 */
function isStudentNameValid(name) {
	if (name.indexOf("\\") >= 0 || name.indexOf("'") >= 0
			|| name.indexOf("\"") >= 0) {
		return false;
	} else if (name.match(/^.[^\t]*$/) == null) {
		return false;
	} else if (name.length > 40) {
		return false;
	}
	return true;
}

/**
 * Checks whether a team's name is valid
 * Used in coordCourseEnroll page (through coordCourseEnroll.js)
 * @param teamName
 * @returns {Boolean}
 */
function isStudentTeamNameValid(teamName) {
	return teamName.length<=24;
}

/**
 * To check whether a student's name and team name are valid
 * @param editName
 * @param editTeamName
 * @returns {Boolean}
 */
function isStudentInputValid(editName, editTeamName, editEmail) {
	if (editName == "" || editTeamName == "" || editEmail == "") {
		setStatusMessage(DISPLAY_FIELDS_EMPTY,true);
		return false;
	} else if (!isStudentNameValid(editName)) {
		setStatusMessage(DISPLAY_STUDENT_NAME_INVALID,true);
		return false;
	} else if (!isStudentTeamNameValid(editTeamName)) {
		setStatusMessage(DISPLAY_STUDENT_TEAMNAME_INVALID,true);
		return false;
	} else if (!isStudentEmailValid(editEmail)){
		setStatusMessage(DISPLAY_STUDENT_EMAIL_INVALID,true);
		return false;
	}
	return true;
}