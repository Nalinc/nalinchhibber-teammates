<%@ page import="java.util.Map"%>
<%@ page import="java.util.List"%>
<%@ page import="teammates.common.util.Const"%>
<%@ page import="teammates.common.datatransfer.FeedbackResponseAttributes"%>
<%@ page import="teammates.common.datatransfer.FeedbackSessionResultsBundle"%>
<%@ page import="teammates.common.datatransfer.StudentResultBundle"%>
<%@ page import="teammates.common.datatransfer.EvaluationDetailsBundle"%>
<%@ page import="teammates.common.datatransfer.EvaluationAttributes"%>
<%@ page import="teammates.common.datatransfer.SubmissionAttributes"%>
<%@ page import="teammates.ui.controller.InstructorEvalSubmissionPageData"%>
<%@ page import="teammates.ui.controller.InstructorStudentRecordsPageData"%>
<%
	InstructorStudentRecordsPageData data = (InstructorStudentRecordsPageData)request.getAttribute("data");
%>
<!DOCTYPE html>
<html>
<head>
	<link rel="shortcut icon" href="/favicon.png">
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>TEAMMATES - Instructor</title>
	<link rel="stylesheet" href="/stylesheets/common.css" type="text/css" media="screen">
	<link rel="stylesheet" href="/stylesheets/instructorStudentRecords.css" type="text/css" media="screen">
	<link rel="stylesheet" href="/stylesheets/common-print.css" type="text/css" media="print">
	
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
		<jsp:include page="<%=Const.ViewURIs.INSTRUCTOR_HEADER%>" />
	</div>

	<div id="frameBody">
		<div id="frameBodyWrapper">
			<div id="topOfPage"></div>
			<div id="headerOperation">
				<h1><%=data.courseId %> - <%=data.student.name %>'s Records</h1>
				<hr>
			</div>
			<jsp:include page="<%=Const.ViewURIs.STATUS_MESSAGE%>" />
			<%
				int evalIndex = -1;
				for(StudentResultBundle studentResult: data.studentEvaluationResults){
					evalIndex++;
			%>
					<div class="student_eval" id="studentEval<%=evalIndex%>">
					<table class="inputTable" id="studentEvaluationInfo">
						<tr>
							<td class="label rightalign bold" width="250px">Evaluation Name:</td>
							<td class="leftalign" id="eval_name<%=evalIndex%>"width="250px"><%=InstructorStudentRecordsPageData.sanitizeForHtml(data.evaluations.get(evalIndex).name)%></td>
						</tr>
					</table>
				<%
					for(boolean byReviewee = true, repeat=true; repeat; repeat = byReviewee, byReviewee=false){
				%>
						<h2 class="centeralign"><%=InstructorStudentRecordsPageData.sanitizeForHtml(data.student.name) + (byReviewee ? "'s Result" : "'s Submission")%></h2>
						<table class="resultTable">
							<thead><tr>
								<th colspan="2" width="10%" class="bold leftalign">
									<span class="resultHeader"><%=byReviewee ? "Reviewee" : "Reviewer"%>: </span><%=data.student.name%></th>
								<th class="bold leftalign"><span class="resultHeader"
									onmouseover="ddrivetip('<%=Const.Tooltips.CLAIMED%>')"
									onmouseout="hideddrivetip()">
									Claimed Contribution: </span><%=InstructorEvalSubmissionPageData.getPointsInEqualShareFormatAsHtml(studentResult.summary.claimedToInstructor,true)%></th>
								<th class="bold leftalign"><span class="resultHeader"
									onmouseover="ddrivetip('<%=Const.Tooltips.PERCEIVED%>')"
									onmouseout="hideddrivetip()">
									Perceived Contribution: </span><%=InstructorEvalSubmissionPageData.getPointsInEqualShareFormatAsHtml(studentResult.summary.perceivedToInstructor,true)%></th>
								</tr></thead>
								<tr>
								<td colspan="4"><span class="bold">Self evaluation:</span><br>
										<%=InstructorEvalSubmissionPageData.getJustificationAsSanitizedHtml(studentResult.getSelfEvaluation())%></td>
								</tr>
								<tr>
									<td colspan="4"><span class="bold">Comments about team:</span><br>
										<%=InstructorEvalSubmissionPageData.sanitizeForHtml(studentResult.getSelfEvaluation().p2pFeedback.getValue())%></td>
								</tr>
								<tr class="resultSubheader">
									<td width="15%" class="bold"><%=byReviewee ? "From" : "To"%> Student</td>
									<td width="5%" class="bold">Contribution</td>
									<td width="40%" class="bold">Confidential comments</td>
									<td width="40%" class="bold">Feedback to peer</td>
								</tr>
					<%
						for(SubmissionAttributes sub: (byReviewee ? studentResult.incoming : studentResult.outgoing)){
							if(sub.reviewer.equals(sub.reviewee)) continue;
					%>
							<tr>
								<td><b><%=InstructorEvalSubmissionPageData.sanitizeForHtml(byReviewee ? sub.details.reviewerName : sub.details.revieweeName)%></b></td>
								<td><%=InstructorEvalSubmissionPageData.getPointsInEqualShareFormatAsHtml(sub.details.normalizedToInstructor,false)%></td>
								<td><%=InstructorEvalSubmissionPageData.getJustificationAsSanitizedHtml(sub)%></td>
								<td><%=InstructorEvalSubmissionPageData.getP2pFeedbackAsHtml(InstructorEvalSubmissionPageData.sanitizeForHtml(sub.p2pFeedback.getValue()), data.evaluations.get(evalIndex).p2pEnabled)%></td>
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
						<input type="button" class="button" id="button_edit<%=evalIndex %>" value="Edit Submission"
							onclick="window.location.href='<%=data.getInstructorEvaluationSubmissionEditLink(data.evaluations.get(evalIndex).courseId, data.evaluations.get(evalIndex).name, data.student.email)%>'">
					</div>
					</div>
					<br>
					<hr>
					<br>
			<%
				}
			%>
			<%
				int fbIndex = -1;
				for (FeedbackSessionResultsBundle feedback : data.studentFeedbackResults) {
					fbIndex++;
					Map<String, List<FeedbackResponseAttributes>> received = feedback 
							.getResponsesSortedByRecipient().get(data.student.name);
					Map<String, List<FeedbackResponseAttributes>> given = feedback
							.getResponsesSortedByGiver().get(data.student.name);
			%>
					<div class="student_feedback" id="studentFeedback<%=fbIndex %>">
					<table class="inputTable" id="studentEvaluationInfo">
						<tr>
							<td class="label rightalign bold" width="250px">Feedback Session Name:</td>
							<td class="leftalign" id="feedback_name<%=fbIndex%>"width="250px"><%=InstructorStudentRecordsPageData.sanitizeForHtml(feedback.feedbackSession.feedbackSessionName)%></td>
						</tr>
					</table>
					<br><br>
			<%
					if(received != null){
			%>
						<div class="backgroundBlock">
							<h2 class="color_white">To: <%=data.student.name%></h2>
					<%
						for (Map.Entry<String, List<FeedbackResponseAttributes>> responsesReceived : received.entrySet()) {
					%>
							<table class="resultTable" style="width: 100%">
								<thead>
									<tr>
										<th class="leftalign"><span class="bold">From: </span><%=responsesReceived.getKey()%></th>
									</tr>
								</thead>
						<%
							int qnIndx = 1;
							for (FeedbackResponseAttributes singleResponse : responsesReceived.getValue()) {
						%>
								<tr class="resultSubheader">
									<td class="multiline"><span class="bold">Question <%=feedback.questions
										.get(singleResponse.feedbackQuestionId).questionNumber%>: </span><%=
										feedback.getQuestionText(singleResponse.feedbackQuestionId)%>
									</td>
								</tr>
								<tr>
									<td class="multiline"><span class="bold">Response: </span><%=
										InstructorStudentRecordsPageData.sanitizeForHtml(singleResponse.answer.getValue())%></td>
								</tr>
							<%
								qnIndx++;
							}
							if (responsesReceived.getValue().isEmpty()) {
							%>
								<tr>
									<td class="bold color_red">No feedback from this user.</td>
								</tr>
						<%
							}
						%>
							</table>
					<%
						}
					%>
					</div>
					<br>
					<br>
				<%
					} else{
				%>
					<div class="backgroundBlock">
							<h4 class="centeralign color_white">No feedback for <%=data.student.name%> found</h4>
					</div>
					<br>
					<br>
				<%
					}
					if(given != null){
				%>
						<div class="backgroundBlock">
							<h2 class="color_white">From: <%=data.student.name%></h2>
					<%
						for (Map.Entry<String, List<FeedbackResponseAttributes>> responsesGiven : given.entrySet()) {
					%>
							<table class="resultTable" style="width: 100%">
								<thead>
									<tr>
										<th class="leftalign"><span class="bold">To: </span><%=responsesGiven.getKey()%></th>
									</tr>
								</thead>
						<%
							int qnIndx = 1;
							for (FeedbackResponseAttributes singleResponse : responsesGiven.getValue()) {
						%>
								<tr class="resultSubheader">
									<td class="multiline"><span class="bold">Question <%=feedback.questions
										.get(singleResponse.feedbackQuestionId).questionNumber%>: </span><%=
										feedback.getQuestionText(singleResponse.feedbackQuestionId)%>
									</td>
								</tr>
								<tr>
									<td class="multiline"><span class="bold">Response: </span><%=
										InstructorStudentRecordsPageData.sanitizeForHtml(singleResponse.answer.getValue())%></td>
								</tr>
							<%
								qnIndx++;
							}
							if (responsesGiven.getValue().isEmpty()) {
							%>
								<tr>
									<td class="bold color_red">No feedback from this user.</td>
								</tr>
						<%
							}
						%>
							</table>
							<br>
						
					<%
						}
					%>
					</div>
				<%
					} else{
				%>
						<div class="backgroundBlock">
							<h4 class="centeralign color_white">No feedback by <%=data.student.name%> found</h4>
						</div>
				<%
					}
				%>	
					</div>
					<br>
					<hr>
					<br>
			<%
				}
			%>
			
			<br>
			<br>
			<br>

		</div>
	</div>

	<div id="frameBottom">
		<jsp:include page="<%=Const.ViewURIs.FOOTER%>" />
	</div>
</body>
</html>