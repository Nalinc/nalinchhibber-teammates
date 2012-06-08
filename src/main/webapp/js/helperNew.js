/**
 * Helper functions for Teammates Require: jquery
 */

var debugEnabled = true;

function logSubmission(s) {
	if (debugEnabled) {
		var msg = "SUBMISSION: ";
		msg += s.fromStudent + "|";
		msg += s.toStudent + "|";
		msg += s.points + "|";
		msg += s.pointsBumpRatio + "|";
		msg += s.courseID + "|";
		msg += s.evaluationName + "|";
		msg += s.teamName + "|";
		msg += s.commentsToStudent + "\n";
		msg += s.justification;
		console.log(msg);
	}
}

function logSubmissionList(lst) {
	if (debugEnabled) {
		for ( var i = 0; i < lst.length; i++) {
			logSubmission(lst[i]);
		}
	}
}

function logSummaryList(lst) {
	if (debugEnabled) {
		for ( var i = 0; i < lst.length; i++) {
			var msg = "summary list " + i + " ";
			msg += lst[i].toStudent + "|";
			msg += lst[i].claimedPoints + "|";
			msg += lst[i].average + "|";
			msg += lst[i].difference + "|";
			msg += lst[i].courseID + "|";
			msg += lst[i].evaluationName + "|";
			msg += lst[i].teamName + "|";
			console.log(msg);
		}
	}
}

function escape(str) {
	str = str.replace(/'/g, "\\'");
	return str;
}

/**
 * Function that encodes ASCII printable characters for function arguments
 * Character code 32-127
 * 
 * Omitting characters 128-255 as these generally do not interfere with our normal functions
 * 
 * @param str
 */
function encodeChar(str) {
	str = str.replace(/&/g, "&amp;");
	str = str.replace(/#/g, "&#35;");
	str = str.replace(/\\/g, "&#92;");

	// Skipping character 32 (space)
	str = str.replace(/!/g, "&#33;");
	str = str.replace(/"/g, "&quot;");
	// Replace # second (see above) since it appears in ASCII equivalent of characters
	str = str.replace(/\$/g, "&#36;");
	str = str.replace(/%/g, "&#37;");
	// Replace & first (see above) since it appears in ASCII equivalent of characters
	str = str.replace(/'/g, "\\'");
	str = str.replace(/\(/g, "&#40;");
	str = str.replace(/\)/g, "&#41;");
	str = str.replace(/\*/g, "&#42;");
	str = str.replace(/\+/g, "&#43;");
	str = str.replace(/,/g, "&#44;");
	str = str.replace(/-/g, "&#45;");
	str = str.replace(/\./g, "&#46;");
	str = str.replace(/\//g, "&#47;");
	// Skipping characters 48-57 (digits 0-9)
	str = str.replace(/:/g, "&#58;");
	// Skip # since it doesn't interfere with any of our processes
	str = str.replace(/</g, "&lt;");
	str = str.replace(/=/g, "&#61;");
	str = str.replace(/>/g, "&gt;");
	str = str.replace(/\?/g, "&#63;");
	str = str.replace(/@/g, "&#64;");
	// Skipping characters 65-90 (alphabets A-Z)
	str = str.replace(/\[/g, "&#91;");
	// Replace \ third so that so that any existing \ in the string is converted
	// and the \ for ' remains intact in the string
	str = str.replace(/\]/g, "&#93;");
	str = str.replace(/\^/g, "&#94;");
	str = str.replace(/_/g, "&#95;");
	str = str.replace(/`/g, "&#96;");
	// Skipping characters 97-122 (alphabets a-z)
	str = str.replace(/\{/g, "&#123;");
	str = str.replace(/\|/g, "&#124;");
	str = str.replace(/\}/g, "&#125;");
	str = str.replace(/~/g, "&#126;");
	// Skipping character 127 (command DEL)

	return str;
}

/**
 * Function that encodes ASCII printable characters for printing purposes
 * Character code 32-127
 *
 * Omitting characters 128-255 as these generally do not interfere with our normal functions
 * 
 * @param str
 */
function encodeCharForPrint(str) {
	str = str.replace(/&/g, "&amp;");
	str = str.replace(/#/g, "&#35;");

	// Skipping character 32 (space)
	str = str.replace(/!/g, "&#33;");
	str = str.replace(/"/g, "&quot;");
	// Replace # second since it appears in ASCII equivalent of characters
	str = str.replace(/\$/g, "&#36;");
	str = str.replace(/%/g, "&#37;");
	// Replace & first since it appears in ASCII equivalent of characters
	str = str.replace(/\\'/g, "&#39;");
	str = str.replace(/\'/g, "&#39;");
	str = str.replace(/\(/g, "&#40;");
	str = str.replace(/\)/g, "&#41;");
	str = str.replace(/\*/g, "&#42;");
	str = str.replace(/\+/g, "&#43;");
	str = str.replace(/,/g, "&#44;");
	str = str.replace(/-/g, "&#45;");
	str = str.replace(/\./g, "&#46;");
	str = str.replace(/\//g, "&#47;");
	// Skipping characters 48-57 (digits 0-9)
	str = str.replace(/:/g, "&#58;");
	// Skip ; since it doesn't interfere with any of our processes
	str = str.replace(/</g, "&lt;");
	str = str.replace(/=/g, "&#61;");
	str = str.replace(/>/g, "&gt;");
	str = str.replace(/\?/g, "&#63;");
	str = str.replace(/@/g, "&#64;");
	// Skipping characters 65-90 (alphabets A-Z)
	str = str.replace(/\[/g, "&#91;");
	str = str.replace(/\\/g, "&#92;");
	str = str.replace(/\]/g, "&#93;");
	str = str.replace(/\^/g, "&#94;");
	str = str.replace(/_/g, "&#95;");
	str = str.replace(/`/g, "&#96;");
	// Skipping characters 97-122 (alphabets a-z)
	str = str.replace(/\{/g, "&#123;");
	str = str.replace(/\|/g, "&#124;");
	str = str.replace(/\}/g, "&#125;");
	str = str.replace(/~/g, "&#126;");
	// Skipping character 127 (command DEL)

	return str;
}

function setEditEvaluationResultsStatusMessage(message) {
	if (message == "") {
		clearEditEvaluationResultsStatusMessage();
		return;
	}

	$("#coordinatorEditEvaluationResultsStatusMessage").html(message).show();
}

function clearEditEvaluationResultsStatusMessage() {
	$("#coordinatorEditEvaluationResultsStatusMessage").html("").hide();
}

function toggleEditEvaluationResultsStatusMessage(statusMsg) {
	setEditEvaluationResultsStatusMessage(statusMsg);
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
function toggleSort(divElement,colIdx) {
	sortTable(divElement,colIdx);
	$(".buttonSortAscending").attr("class","buttonSortNone");
	$(divElement).attr("class","buttonSortAscending");
}

/**
 * Sorts a table ascending based on certain column
 * @param oneOfTableCell
 * 		One of the table cell
 * @param colIdx
 * 		The column index (1-based) as key for the sort
 */
function sortTable(oneOfTableCell, colIdx){
	var table = $(oneOfTableCell);
	if(!table.is("table")){
		table = $(oneOfTableCell).parentsUntil("table");
		table = $(table[table.length-1].parentNode);
	}
	keys = $("td:nth-child("+colIdx+")",table);
	keys.sortElements( sortBaseCell, function(){return this.parentNode;} );
}

function sortBaseCell(cell1, cell2){
	cell1 = cell1.innerHTML;
	cell2 = cell2.innerHTML;
	return sortBase(cell1,cell2);
}

function sortBase(x, y) {
	return ((x < y) ? -1 : ((x > y) ? 1 : 0));
}

function sortByAverage(a, b) {
	var x = a.average;
	var y = b.average;

	if (x == "N/A") {
		x = 1000;
	}

	if (y == "N/A") {
		y = 1000;
	}

	return sortBase(x, y);
}

function sortByCourseID(a, b) {
	var x = a.courseID.toLowerCase();
	var y = b.courseID.toLowerCase();

	return sortBase(x, y);
}

function sortByDiff(a, b) {
	var x = a.difference;
	var y = b.difference;

	if (x == "N/A") {
		x = 1000;
	}

	if (y == "N/A") {
		y = 1000;
	}

	return sortBase(x, y);
}

function sortByFromStudentName(a, b) {
	var x = a.fromStudentName;
	var y = b.fromStudentName;

	return sortBase(x, y);
}

function sortByID(a, b) {
	var x = a.ID.toLowerCase();
	var y = b.ID.toLowerCase();

	return sortBase(x, y);
}

function sortByName(a, b) {
	var x = a.name.toLowerCase();
	var y = b.name.toLowerCase();

	return sortBase(x, y);
}

function sortByGoogleID(a, b) {
	var x = a.googleID;
	var y = b.googleID;

	return sortBase(x, y);
}

function sortBySubmitted(a, b) {
	var x = a.submitted;
	var y = b.submitted;

	return sortBase(x, y);
}

function sortByTeamName(a, b) {
	var x = a.teamName;
	var y = b.teamName;

	return sortBase(x, y);
}

function sortByToStudentName(a, b) {
	var x = a.toStudentName;
	var y = b.toStudentName;

	return sortBase(x, y);
}

/**-----------------------UI Related Helper Functions-----------------------**/
/**
 * Scrolls the screen to top
 */
function scrollToTop() {
	document.getElementById(DIV_TOPOFPAGE).scrollIntoView(true);
}

//Selector for status message div tag
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

function setStatusMessageToLoading() {
	$(DIV_STATUS_MESSAGE).html(DISPLAY_LOADING).css("background","").show();
}

function clearStatusMessage() {
	$(DIV_STATUS_MESSAGE).html("").css("background","").hide();
}

function alertServerError() {
	alert(DISPLAY_SERVERERROR);
}