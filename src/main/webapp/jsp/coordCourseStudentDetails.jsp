<%@ page import="teammates.api.Common" %>
<%@ page import="teammates.datatransfer.CourseData"%>
<%@ page import="teammates.datatransfer.EvaluationData"%>
<%@ page import="teammates.jsp.CoordCourseStudentDetailsHelper"%>
<%	CoordCourseStudentDetailsHelper helper = (CoordCourseStudentDetailsHelper)request.getAttribute("helper"); %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
	"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
	<link rel="shortcut icon" href="/favicon.png" />
	<meta http-equiv="X-UA-Compatible" content="IE=8" />
	<title>Teammates - Coordinator</title>
	<link rel=stylesheet href="/stylesheets/main.css" type="text/css" />
	<link rel=stylesheet href="/stylesheets/evaluation.css" type="text/css" />
	
	<script language="JavaScript" src="/js/jquery-1.6.2.min.js"></script>
	<script language="JavaScript" src="/js/tooltip.js"></script>
	<script language="JavaScript" src="/js/date.js"></script>
	<script language="JavaScript" src="/js/CalendarPopup.js"></script>
	<script language="JavaScript" src="/js/AnchorPosition.js"></script>
	<script language="JavaScript" src="/js/helperNew.js"></script>
	<script language="JavaScript" src="/js/constants.js"></script>
	<script language="JavaScript" src="/js/commonNew.js"></script>
	
	<script language="JavaScript" src="/js/coordinatorNew.js"></script>

</head>

<body>
	<div id="dhtmltooltip"></div>
	<div id="frameTop">
		<jsp:include page="<%= Common.JSP_COORD_HEADER %>" />
	</div>

	<div id="frameBody">
		<div id="frameBodyWrapper">
			<div id="topOfPage"></div>
			<div id="headerOperation">
				<h1>Student Details</h1>
			</div>
			<jsp:include page="<%= Common.JSP_STATUS_MESSAGE %>" />
			<div id="coordinatorStudentInformation">
				<table class="detailform">
					<tr>
			 			<td class="fieldname">Student Name:</td>
			 			<td><%= helper.student.name %></td>
			 		</tr>
				 	<tr>
				 		<td class="fieldname">Team Name:</td>
				 		<td><%= CoordCourseStudentDetailsHelper.escapeHTML(helper.student.team) %></td>
				 	</tr>
				 	<tr>
				 		<td class="fieldname">E-mail Address:</td>
				 		<td><%= CoordCourseStudentDetailsHelper.escapeHTML(helper.student.email) %></td>
				 	</tr>
				 	<tr>
						<td class="fieldname">Google ID:</td>
						<td><%= (helper.student.id!= null ? CoordCourseStudentDetailsHelper.escapeHTML(helper.student.id) : "-") %></td>
					</tr>
					<tr>
						<td class="fieldname">Registration Key:</td>
						<td id="t_courseKey"><%= CoordCourseStudentDetailsHelper.escapeHTML(helper.regKey) %></td>
					</tr>
				 	<tr>
				 		<td class="fieldname">Comments:</td>
				 		<td><%= CoordCourseStudentDetailsHelper.escapeHTML(helper.student.comments) %></td>
				 	</tr>
				 </table>
				 <br /><br />
				 <input type="button" class="button" id="button_back" value="Back"
				 		onclick="window.location.href='<%= helper.getCoordCourseDetailsLink(helper.student.course) %>'" />
			</div>
		</div>
	</div>

	<div id="frameBottom">
		<jsp:include page="<%= Common.JSP_FOOTER %>" />
	</div>
</body>
</html>