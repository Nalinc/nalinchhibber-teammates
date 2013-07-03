<%@ page import="teammates.common.util.Constants"%>
<%@ page import="teammates.common.datatransfer.CourseAttributes"%>
<%@ page import="teammates.common.datatransfer.EvaluationDetailsBundle"%>
<%@ page import="teammates.common.datatransfer.SubmissionAttributes"%>
<%@ page import="teammates.ui.controller.InstructorEvalSubmissionViewPageData"%>
<%
	InstructorEvalSubmissionViewPageData data = (InstructorEvalSubmissionViewPageData)request.getAttribute("data");
%>
<!DOCTYPE html>
<html>
<head>
	<link rel="shortcut icon" href="/favicon.png">
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>Teammates - Instructor</title>
	<link rel="stylesheet" href="/stylesheets/common.css" type="text/css" media="screen">
	<link rel="stylesheet" href="/stylesheets/instructorEvalSubmissionView.css" type="text/css" media="screen">
	<link rel="stylesheet" href="/stylesheets/common-print.css" type="text/css" media="print">
    <link rel="stylesheet" href="/stylesheets/instructorEvalSubmissionView-print.css" type="text/css" media="print">
	
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
				<h1>View Student's Evaluation</h1>
			</div>
			
			<table class="inputTable" id="studentEvaluationInfo">
				<tr>
					<td class="label rightalign bold" width="30%">Course ID:</td>
					<td class="leftalign"><%=data.evaluation.courseId%></td>
				</tr>
				<tr>
					<td class="label rightalign bold" width="30%">Evaluation Name:</td>
					<td class="leftalign"><%=InstructorEvalSubmissionViewPageData.escapeForHTML(data.evaluation.name)%></td>
				</tr>
			</table>
			

			<%
							for(boolean byReviewee = true, repeat=true; repeat; repeat = byReviewee, byReviewee=false){
						%>
			<h2 class="centeralign"><%=InstructorEvalSubmissionViewPageData.escapeForHTML(data.student.name) + (byReviewee ? "'s Result" : "'s Submission")%></h2>
			<table class="resultTable">
				<thead><tr>
					<th colspan="2" width="10%" class="bold leftalign">
						<span class="resultHeader"><%=byReviewee ? "Reviewee" : "Reviewer"%>: </span><%=data.student.name%></th>
					<th class="bold leftalign"><span class="resultHeader"
							onmouseover="ddrivetip('<%=Constants.TOOLTIP_CLAIMED%>')"
							onmouseout="hideddrivetip()">
						Claimed Contribution: </span><%=InstructorEvalSubmissionViewPageData.getPointsInEqualShareFormatAsHtml(data.studentResult.summary.claimedToInstructor,true)%></th>
					<th class="bold leftalign"><span class="resultHeader"
							onmouseover="ddrivetip('<%=Constants.TOOLTIP_PERCEIVED%>')"
							onmouseout="hideddrivetip()">
						Perceived Contribution: </span><%=InstructorEvalSubmissionViewPageData.getPointsInEqualShareFormatAsHtml(data.studentResult.summary.perceivedToInstructor,true)%></th>
				</tr></thead>
				<tr>
					<td colspan="4"><span class="bold">Self evaluation:</span><br>
							<%=InstructorEvalSubmissionViewPageData.getJustificationAsSanitizedHtml(data.studentResult.getSelfEvaluation())%></td>
					</tr>
				<tr>
					<td colspan="4"><span class="bold">Comments about team:</span><br>
							<%=InstructorEvalSubmissionViewPageData.escapeForHTML(data.studentResult.getSelfEvaluation().p2pFeedback.getValue())%></td>
					</tr>
				<tr class="resultSubheader">
					<td width="15%" class="bold"><%=byReviewee ? "From" : "To"%> Student</td>
					<td width="5%" class="bold">Contribution</td>
					<td width="40%" class="bold">Confidential comments</td>
					<td width="40%" class="bold">Feedback to peer</td>
				</tr>
				<%
					for(SubmissionAttributes sub: (byReviewee ? data.studentResult.incoming : data.studentResult.outgoing)){
														if(sub.reviewer.equals(sub.reviewee)) continue;
				%>
					<tr>
						<td><b><%=InstructorEvalSubmissionViewPageData.escapeForHTML(byReviewee ? sub.details.reviewerName : sub.details.revieweeName)%></b></td>
						<td><%=InstructorEvalSubmissionViewPageData.getPointsInEqualShareFormatAsHtml(sub.details.normalizedToInstructor,false)%></td>
						<td><%=InstructorEvalSubmissionViewPageData.getJustificationAsSanitizedHtml(sub)%></td>
						<td><%=InstructorEvalSubmissionViewPageData.getP2pFeedbackAsHtml(InstructorEvalSubmissionViewPageData.escapeForHTML(sub.p2pFeedback.getValue()), data.evaluation.p2pEnabled)%></td>
					</tr>
				<%
					}
				%>
			</table>
			<br><br>
			<%
				}
			%>
			<div class="centeralign">
				<input type="button" class="button" id="button_edit" value="Edit Submission"
						onclick="window.location.href='<%=data.getInstructorEvaluationSubmissionEditLink(data.evaluation.courseId, data.evaluation.name, data.student.email)%>'">
			</div>
			<br>
			<br>
			<br>
	
		</div>
	</div>

	<div id="frameBottom">
		<jsp:include page="<%=Constants.VIEW_FOOTER%>" />
	</div>
</body>
</html>