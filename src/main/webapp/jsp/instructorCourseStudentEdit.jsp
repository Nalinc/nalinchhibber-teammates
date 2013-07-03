<%@ page import="teammates.common.util.Constants" %>
<%@ page import="teammates.common.datatransfer.CourseAttributes"%>
<%@ page import="teammates.common.datatransfer.EvaluationAttributes"%>
<%@ page import="static teammates.ui.controller.PageData.sanitizeForHtml"%>
<%@ page import="teammates.ui.controller.InstructorCourseStudentDetailsEditPageData"%>
<%
	InstructorCourseStudentDetailsEditPageData data = (InstructorCourseStudentDetailsEditPageData)request.getAttribute("data");
%>
<!DOCTYPE html>
<html>
<head>
	<link rel="shortcut icon" href="/favicon.png">
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>Teammates - Instructor</title>
    <link rel="stylesheet" href="/stylesheets/common.css" type="text/css" media="screen">
    <link rel="stylesheet" href="/stylesheets/instructorCourseStudentEdit.css" type="text/css" media="screen">
    <link rel="stylesheet" href="/stylesheets/common-print.css" type="text/css" media="print">
    <link rel="stylesheet" href="/stylesheets/instructorCourseStudentEdit-print.css" type="text/css" media="print">
	
	<script type="text/javascript" src="/js/googleAnalytics.js"></script>
	<script type="text/javascript" src="/js/jquery-minified.js"></script>
	<script type="text/javascript" src="/js/tooltip.js"></script>
	<script type="text/javascript" src="/js/date.js"></script>
	<script type="text/javascript" src="/js/CalendarPopup.js"></script>
	<script type="text/javascript" src="/js/AnchorPosition.js"></script>
	<script type="text/javascript" src="/js/common.js"></script>
	
	<script type="text/javascript" src="/js/instructor.js"></script>
    <jsp:include page="../enableJS.jsp"></jsp:include>
</head>

<body>
	<div id="dhtmltooltip"></div>
	<div id="frameTop">
		<jsp:include page="<%=Constants.VIEW_INSTRUCTOR_HEADER%>" />
	</div>

	<div id="frameBody">
		<div id="frameBodyWrapper">
			<div id="topOfPage"></div>
			<div id="headerOperation">
				<h1>Edit Student Details</h1>
			</div>
				
			<form action="<%=Constants.ACTION_INSTRUCTOR_COURSE_STUDENT_DETAILS_EDIT_SAVE%>" method="post">
				<input type="hidden" name="<%=Constants.PARAM_COURSE_ID%>" value="<%=data.student.course%>">
				<table class="inputTable" id="studentEditForm">
					<tr>
			 			<td class="label bold">Student Name:</td>
			 			<td>
			 				<input class="fieldvalue" name="<%=Constants.PARAM_STUDENT_NAME%>" 
			 						id="<%=Constants.PARAM_STUDENT_NAME%>"
			 						value="<%=sanitizeForHtml(data.student.name)%>">
			 			</td>
			 		</tr>
				 	<tr>
				 		<td class="label bold">Team Name:</td>
				 		<td>
				 			<input class="fieldvalue" name="<%=Constants.PARAM_TEAM_NAME%>" 
				 					id="<%=Constants.PARAM_TEAM_NAME%>"
				 					value="<%=sanitizeForHtml(data.student.team)%>">
				 		</td>
				 	</tr>
				 	<tr>
				 		<td class="label bold">E-mail Address:
				 			<input type="hidden" name="<%=Constants.PARAM_STUDENT_EMAIL%>" 
				 					id="<%=Constants.PARAM_STUDENT_EMAIL%>"
				 					value="<%=sanitizeForHtml(data.student.email)%>">
				 		</td>
				 		<td>
				 			<input class="fieldvalue" name="<%=Constants.PARAM_NEW_STUDENT_EMAIL%>" 
				 					id="<%=Constants.PARAM_NEW_STUDENT_EMAIL%>"
				 					value="<%=sanitizeForHtml(data.student.email)%>">
				 		</td>
				 	</tr>
				 	<tr>
						<td class="label bold">Google ID:</td>
						<td id="<%=Constants.PARAM_USER_ID%>"><%=(data.student.googleId!= null ? sanitizeForHtml(data.student.googleId) : "")%></td>
					</tr>
					<tr>
						<td class="label bold">Registration Key:</td>
						<td id="<%=Constants.PARAM_REGKEY%>"><%=sanitizeForHtml(data.regKey)%></td>
					</tr>
				 	<tr>
				 		<td class="label bold middlealign">Comments:</td>
				 		<td>
				 			<textarea class="textvalue" rows="6" cols="80" 
				 				name="<%=Constants.PARAM_COMMENTS%>" 
				 				id="<%=Constants.PARAM_COMMENTS%>"><%=sanitizeForHtml(data.student.comments)%></textarea>
				 		</td>
				 	</tr>
				</table>
				
				<jsp:include page="<%=Constants.VIEW_STATUS_MESSAGE%>" />
				<br>
				<div class="centeralign">
					<input type="submit" class="button centeralign" id="button_submit" name="submit" value="Save Changes"
						onclick="return isStudentInputValid(this.form.<%=Constants.PARAM_STUDENT_NAME%>.value,this.form.<%=Constants.PARAM_TEAM_NAME%>.value,this.form.<%=Constants.PARAM_NEW_STUDENT_EMAIL%>.value)">
				</div>
				<br>
				<br>
				<input type="hidden" name="<%=Constants.PARAM_USER_ID%>" value="<%=data.account.googleId%>">
			</form>
			
		</div>
	</div>

	<div id="frameBottom">
		<jsp:include page="<%=Constants.VIEW_FOOTER%>" />
	</div>
</body>
</html>