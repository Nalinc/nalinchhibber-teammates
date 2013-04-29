<%@ page import="teammates.common.Common"%>
<%@ page import="teammates.common.datatransfer.CourseData"%>
<%@ page import="teammates.common.datatransfer.EvaluationData"%>
<%@ page import="teammates.common.datatransfer.SubmissionData"%>
<%@ page import="teammates.ui.controller.InstructorEvalSubmissionEditHelper"%>
<%	InstructorEvalSubmissionEditHelper helper = (InstructorEvalSubmissionEditHelper)request.getAttribute("helper"); %>
<!DOCTYPE html>
<html>
<head>
	<link rel="shortcut icon" href="/favicon.png">
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>Teammates - Instructor</title>
	<link rel="stylesheet" href="/stylesheets/common.css" type="text/css" media="screen">
	<link rel="stylesheet" href="/stylesheets/instructorEvalSubmissionEdit.css" type="text/css" media="screen">
	<link rel="stylesheet" href="/stylesheets/common-print.css" type="text/css" media="print">
    <link rel="stylesheet" href="/stylesheets/instructorEvalSubmissionEdit-print.css" type="text/css" media="print">
	
	
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
		<jsp:include page="<%= Common.JSP_INSTRUCTOR_HEADER %>" />
	</div>

	<div id="frameBody">
		<div id="frameBodyWrapper">
			<div id="topOfPage"></div>
			<div id="headerOperation">
				<h1>Edit Student's Submission</h1>
			</div>
			
			<table class="inputTable" id="studentEvaluationInfo">
				<tr>
					<td class="label rightalign bold" width="30%">Course ID:</td>
					<td class="leftalign"><%= helper.eval.course %></td>
				</tr>
				<tr>
					<td class="label rightalign bold" width="30%">Evaluation Name:</td>
					<td class="leftalign"><%=InstructorEvalSubmissionEditHelper.escapeForHTML(helper.eval.name)%></td>
				</tr>
			</table>
			
			<br>
			<div id="studentEvaluationSubmissions">
				<form name="form_submitevaluation" id="form_submitevaluation" method="post"
						action="<%= Common.PAGE_INSTRUCTOR_EVAL_SUBMISSION_EDIT_HANDLER %>">
					<jsp:include page="<%= Common.JSP_EVAL_SUBMISSION_EDIT %>">
					<jsp:param name="isStudent" value="false" />
					</jsp:include>
					<br>
					<jsp:include page="<%= Common.JSP_STATUS_MESSAGE %>" />
					<br>
					<div class="centeralign">
						<input type="button" class="button" id="button_back"
								onclick="window.close(); opener.setStatusMessage('')" value="Cancel">
						<input type="submit" class="button" name="submitEvaluation"
								onclick="return checkEvaluationForm(this.form)"
								id="button_submit" value="Save Changes">
					</div>
					<% if(helper.isMasqueradeMode()){ %>
						<input type="hidden" name="<%= Common.PARAM_USER_ID %>" value="<%= helper.requestedUser %>">
					<% } %>
				</form>
		 		<br><br>
			</div>
		</div>
	</div>

	<div id="frameBottom">
		<jsp:include page="<%= Common.JSP_FOOTER %>" />
	</div>
</body>
</html>