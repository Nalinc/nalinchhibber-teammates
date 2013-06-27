<%@ page import="teammates.common.Common" %>
<%@ page import="teammates.common.datatransfer.CourseAttributes"%>
<%@ page import="teammates.common.FieldValidator"%>
<%@ page import="teammates.common.datatransfer.InstructorAttributes"%>
<%@ page import="static teammates.ui.controller.PageData.escapeForHTML"%>
<%@ page import="teammates.ui.controller.InstructorCourseEditPageData"%>
<%
InstructorCourseEditPageData data = (InstructorCourseEditPageData)request.getAttribute("data");
%>
<!DOCTYPE html>
<html>
<head>
	<link rel="shortcut icon" href="/favicon.png">
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>Teammates - Instructor</title>
	<link rel="stylesheet" href="/stylesheets/common.css" type="text/css" media="screen">
	<link rel="stylesheet" href="/stylesheets/instructorCourseEdit.css" type="text/css" media="screen">
	<link rel="stylesheet" href="/stylesheets/common-print.css" type="text/css" media="print">
    <link rel="stylesheet" href="/stylesheets/instructorCourseEdit-print.css" type="text/css" media="print">
	
	<script type="text/javascript" src="/js/googleAnalytics.js"></script>
	<script type="text/javascript" src="/js/jquery-minified.js"></script>
	<script type="text/javascript" src="/js/tooltip.js"></script>
	<script type="text/javascript" src="/js/date.js"></script>
	<script type="text/javascript" src="/js/CalendarPopup.js"></script>
	<script type="text/javascript" src="/js/AnchorPosition.js"></script>
	<script type="text/javascript" src="/js/common.js"></script>
	
	<script type="text/javascript" src="/js/instructor.js"></script>
	<script type="text/javascript" src="/js/instructorCourse.js"></script>
    <jsp:include page="../enableJS.jsp"></jsp:include>
</head>

<body>
	<div id="dhtmltooltip"></div>
	<div id="frameTop">
		<jsp:include page="<%=Common.JSP_INSTRUCTOR_HEADER%>" />
	</div>

	<div id="frameBody">
		<div id="frameBodyWrapper">
			<div id="topOfPage"></div>
			<div id="headerOperation">
				<h1>Edit Course Details</h1>
			</div>
				
			<form action="<%=Common.PAGE_INSTRUCTOR_COURSE_EDIT_SAVE%>" method="post">
				<input type="hidden" name="<%=Common.PARAM_COURSE_ID%>" value="<%=data.course.id%>">
				<input type="hidden" id="<%=Common.PARAM_INSTRUCTOR_ID%>" name="<%=Common.PARAM_INSTRUCTOR_ID%>" value="<%=data.account.googleId%>">
				<table id="addform" class="inputTable">
					<tr>
						<td class="label bold">Course ID:</td>
					</tr>
					<tr>
						<td><input class="addinput" type="text"
							name="<%=Common.PARAM_COURSE_ID%>" id="<%=Common.PARAM_COURSE_ID%>"
							value="<%=(data.course.id==null ? "" : escapeForHTML(data.course.id))%>"
							onmouseover="ddrivetip('Identifier of the course, e.g.CS3215-Sem1.')"
							onmouseout="hideddrivetip()"
							maxlength=<%=FieldValidator.COURSE_ID_MAX_LENGTH%> tabindex="1" disabled="disabled"></td>
					</tr>
					<tr>
						<td class="label bold">Course Name:</td>
					</tr>
					<tr>
						<td><input class="addinput" type="text"
							name="<%=Common.PARAM_COURSE_NAME%>" id="<%=Common.PARAM_COURSE_NAME%>"
							value="<%=(data.course.name==null ? "" : escapeForHTML(data.course.name))%>"
							onmouseover="ddrivetip('Enter the name of the course, e.g. Software Engineering.')"
							onmouseout="hideddrivetip()"
							maxlength=<%=FieldValidator.COURSE_NAME_MAX_LENGTH%> tabindex=2 disabled="disabled"/></td>
					</tr>
					<tr>
						<td class="label bold">Instructors:</td>
					</tr>
					<tr>
						<td colspan=2>
							<span id="instructorformat" class="bold">Format: Google ID | Instructor Name | Instructor Email</span>
							<%
								String instructorInfo = "";
								for (int i = 0; i < data.instructorList.size(); i++){
									InstructorAttributes instructor = data.instructorList.get(i);
									instructorInfo += instructor.googleId + "|" + instructor.name + "|" + instructor.email + "\n";
								}
							%>
							<textarea rows="6" cols="110" class ="textvalue" name="<%=Common.PARAM_COURSE_INSTRUCTOR_LIST%>" id="<%=Common.PARAM_COURSE_INSTRUCTOR_LIST%>"><%= escapeForHTML(instructorInfo.trim())%></textarea>
						</td>
					</tr>
					<tr>
						<td colspan=2 class="centeralign">
							<input type="submit" class="button" id="button_submit" name="submit" value="Save Changes" onclick="return verifyCourseData();">
						</td>
					</tr>
				</table>
				
				<jsp:include page="<%=Common.JSP_STATUS_MESSAGE%>" />
				<br>
				
				<br>
				<br>
				<input type="hidden" name="<%= Common.PARAM_USER_ID %>" value="<%= data.account.googleId %>">
			</form>
			
		</div>
	</div>

	<div id="frameBottom">
		<jsp:include page="<%=Common.JSP_FOOTER%>" />
	</div>
</body>
</html>