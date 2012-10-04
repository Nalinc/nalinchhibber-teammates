<%@ page import="teammates.common.Common" %>
<%@ page import="teammates.common.datatransfer.CourseData"%>
<%@ page import="teammates.common.datatransfer.EvaluationData"%>
<%@ page import="teammates.ui.controller.CoordCourseStudentDetailsHelper"%>
<%	CoordCourseStudentDetailsHelper helper = (CoordCourseStudentDetailsHelper)request.getAttribute("helper"); %>
<!DOCTYPE html>
<html>
<head>
	<link rel="shortcut icon" href="/favicon.png">
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>Teammates - Coordinator</title>
	<link rel="stylesheet" href="/stylesheets/main.css" type="text/css">
	<link rel="stylesheet" href="/stylesheets/evaluation.css" type="text/css">
	
	<script type="text/javascript" src="/js/jquery-1.6.2.min.js"></script>
	<script type="text/javascript" src="/js/tooltip.js"></script>
	<script type="text/javascript" src="/js/date.js"></script>
	<script type="text/javascript" src="/js/CalendarPopup.js"></script>
	<script type="text/javascript" src="/js/AnchorPosition.js"></script>
	<script type="text/javascript" src="/js/common.js"></script>
	
	<script language="JavaScript" src="/js/coordinator.js"></script>

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
			 			<td id="<%= Common.PARAM_STUDENT_NAME %>"><%= helper.student.name %></td>
			 		</tr>
				 	<tr>
				 		<td class="fieldname">Team Name:</td>
				 		<td id="<%= Common.PARAM_TEAM_NAME %>"><%=CoordCourseStudentDetailsHelper.escapeForHTML(helper.student.team)%></td>
				 	</tr>
				 	<tr>
				 		<td class="fieldname">E-mail Address:</td>
				 		<td id="<%=Common.PARAM_STUDENT_EMAIL%>"><%=CoordCourseStudentDetailsHelper.escapeForHTML(helper.student.email)%></td>
				 	</tr>
				 	<tr>
						<td class="fieldname">Google ID:</td>
						<td id="<%=Common.PARAM_USER_ID%>"><%=(helper.student.id!= null ? CoordCourseStudentDetailsHelper.escapeForHTML(helper.student.id) : "")%></td>
					</tr>
					<tr>
						<td class="fieldname">Registration Key:</td>
						<td id="<%=Common.PARAM_REGKEY%>"><%=CoordCourseStudentDetailsHelper.escapeForHTML(helper.regKey)%></td>
					</tr>
				 	<tr>
				 		<td class="fieldname">Comments:</td>
				 		<td id="<%=Common.PARAM_COMMENTS%>"><%=CoordCourseStudentDetailsHelper.escapeForHTML(helper.student.comments)%></td>
				 	</tr>
				 </table>
				 <br><br>
			</div>
		</div>
	</div>

	<div id="frameBottom">
		<jsp:include page="<%= Common.JSP_FOOTER %>" />
	</div>
</body>
</html>