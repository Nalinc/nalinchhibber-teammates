package teammates.ui.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import teammates.common.Common;
import teammates.common.datatransfer.AccountAttributes;
import teammates.common.datatransfer.CourseDetailsBundle;
import teammates.common.datatransfer.EvaluationAttributes;
import teammates.common.datatransfer.FeedbackSessionAttributes;

public class StudentHomePageData extends PageData {
	
	public StudentHomePageData(AccountAttributes account) {
		super(account);
	}
	
	public List<CourseDetailsBundle> courses = new ArrayList<CourseDetailsBundle>();
	public Map<String, String> evalSubmissionStatusMap = new HashMap<String, String>();
	public Map<String, Boolean> sessionSubmissionStatusMap = new HashMap<String, Boolean>();
	
	/**
	 * Returns the submission status of the student for a given evaluation.
	 * The possible status are:
	 * <ul>
	 * <li>PENDING - The student has not submitted any submission</li>
	 * <li>SUBMITTED - The student has submitted a submission, and the evaluation is still open</li>
	 * <li>CLOSED - The evaluation has been closed (passed the deadline), and the result has not been published</li>
	 * <li>PUBLISHED - The evaluation is closed and the result is available for viewing</li>
	 * </ul>
	 */
	public String getStudentStatusForEval(EvaluationAttributes evaluation){
		return evalSubmissionStatusMap.get(evaluation.courseId+"%"+evaluation.name);
	}
	
	/**
	 * Returns the submission status of the student for a given feedback session.
	 */
	public String getStudentStatusForSession(FeedbackSessionAttributes session){
		String status = PageData.getInstructorStatusForFeedbackSession(session);
		if (status == "Open") {
			Boolean hasSubmitted = sessionSubmissionStatusMap.get(session.courseId+"%"+session.feedbackSessionName);
			status = hasSubmitted ? "Submitted" : "Pending";
		}
		return status;
	}
	
	/**
	 * @param submissionStatus Submission status of a student for a particular evaluation. 
	 * 
	 * @return The hover message to explain evaluation submission status.
	 */
	public String getStudentHoverMessageForSession(FeedbackSessionAttributes session){
		String msg = "";
		Boolean hasSubmitted = sessionSubmissionStatusMap.get(session.courseId+"%"+session.feedbackSessionName);
		
		if (hasSubmitted){
			msg += Common.HOVER_MESSAGE_STUDENT_FEEDBACK_SESSION_STATUS_SUBMITTED;
		} else {
			msg += Common.HOVER_MESSAGE_STUDENT_FEEDBACK_SESSION_STATUS_PENDING;
		}		
		if (session.isClosed()){
			msg += Common.HOVER_MESSAGE_STUDENT_FEEDBACK_SESSION_STATUS_CLOSED;
		}
		if (session.isPublished()) {
			msg += Common.HOVER_MESSAGE_STUDENT_FEEDBACK_SESSION_STATUS_PUBLISHED;
		}
		return msg;
	}
	
	/**
	 * @param submissionStatus Submission status of a student for a particular evaluation. 
	 * Can be: PENDING, SUBMITTED, ClOSED, PUBLISHED.
	 * 
	 * @return The hover message to explain evaluation submission status.
	 */
	public String getStudentHoverMessageForEval(String submissionStatus){
		if(submissionStatus.equals(Common.STUDENT_EVALUATION_STATUS_PENDING)){
			return Common.HOVER_MESSAGE_STUDENT_EVALUATION_STATUS_PENDING;
		} else if(submissionStatus.equals(Common.STUDENT_EVALUATION_STATUS_SUBMITTED)){
			return Common.HOVER_MESSAGE_STUDENT_EVALUATION_STATUS_SUBMITTED;
		} else if(submissionStatus.equals(Common.STUDENT_EVALUATION_STATUS_CLOSED)){
			return Common.HOVER_MESSAGE_STUDENT_EVALUATION_STATUS_CLOSED;
		} else if(submissionStatus.equals(Common.STUDENT_EVALUATION_STATUS_PUBLISHED)){
			return Common.HOVER_MESSAGE_STUDENT_EVALUATION_STATUS_PUBLISHED;
		} else {
			return Common.HOVER_MESSAGE_STUDENT_EVALUATION_STATUS_ERROR;
		}
	}
	
	
	
	public String getStudentCourseDetailsLink(String courseId){
		String link = Common.PAGE_STUDENT_COURSE_DETAILS;
		link = Common.addParamToUrl(link,Common.PARAM_COURSE_ID,courseId);
		return link;
	}
	
	public String getStudentFeedbackResponseEditLink(String courseId, String feedbackSessionName){
		String link = Common.PAGE_STUDENT_FEEDBACK_SUBMIT;
		link = Common.addParamToUrl(link,Common.PARAM_COURSE_ID,courseId);
		link = Common.addParamToUrl(link,Common.PARAM_FEEDBACK_SESSION_NAME,feedbackSessionName);
		link = addUserIdToUrl(link);
		return link;
	}
	
	public String getStudentFeedbackResultsLink(String courseId, String feedbackSessionName){
		String link = Common.PAGE_STUDENT_FEEDBACK_RESULTS;
		link = Common.addParamToUrl(link,Common.PARAM_COURSE_ID,courseId);
		link = Common.addParamToUrl(link,Common.PARAM_FEEDBACK_SESSION_NAME,feedbackSessionName);
		link = addUserIdToUrl(link);
		return link;
	}
	
	public String getStudentEvaluationResultsLink(String courseID, String evalName){
		String link = Common.PAGE_STUDENT_EVAL_RESULTS;
		link = Common.addParamToUrl(link,Common.PARAM_COURSE_ID,courseID);
		link = Common.addParamToUrl(link,Common.PARAM_EVALUATION_NAME,evalName);
		return link;
	}
	
	/**
	 * Note that the submit is essentially an edit to a blank submission.<br />
	 * @return The link to submit or edit a submission for a specific evaluation.
	 */
	public String getStudentEvaluationSubmissionEditLink(String courseID, String evalName){
		String link = Common.PAGE_STUDENT_EVAL_SUBMISSION_EDIT;
		link = Common.addParamToUrl(link,Common.PARAM_COURSE_ID,courseID);
		link = Common.addParamToUrl(link,Common.PARAM_EVALUATION_NAME,evalName);
		return link;
	}
	
	/**
	 * @return The list of available actions for a specific evaluation.
	 */
	public String getStudentEvaluationActions(EvaluationAttributes eval, int idx) {
		String studentStatus = getStudentStatusForEval(eval);
		
		if (studentStatus.equals(Common.STUDENT_EVALUATION_STATUS_PENDING)) {
			return "<a class=\"color_black\" id=\"submitEvaluation"
					+ idx
					+ "\" "
					+ "href=\""
					+ getStudentEvaluationSubmissionEditLink(eval.courseId,
							eval.name) + "\" " + "onmouseover=\"ddrivetip('"
					+ Common.HOVER_MESSAGE_EVALUATION_SUBMIT + "')\" "
					+ "onmouseout=\"hideddrivetip()\">Submit</a>";
		}
		
		
		boolean hasView = false;
		boolean hasEdit = false;
		if (studentStatus.equals(Common.STUDENT_EVALUATION_STATUS_PUBLISHED)) {
			hasView = true;
		} else if (studentStatus
				.equals(Common.STUDENT_EVALUATION_STATUS_SUBMITTED)) {
			hasEdit = true;
		} else if (studentStatus
				.equals(Common.STUDENT_EVALUATION_STATUS_CLOSED)) {
			hasEdit = true;
		}
		
		// @formatter:off
		String result = "<a class=\"color_black\" href=\""
				+ getStudentEvaluationResultsLink(eval.courseId, eval.name)
				+ "\" " + "name=\"viewEvaluationResults"
				+ idx + "\" " + " id=\"viewEvaluationResults" + idx + "\" "
				+ "onmouseover=\"ddrivetip('" + Common.HOVER_MESSAGE_EVALUATION_RESULTS
				+ "')\" " + "onmouseout=\"hideddrivetip()\" " + (hasView ? "" : DISABLED)
				+ ">" + "View Results</a>" + "<a class=\"color_black\" href=\""
				+ getStudentEvaluationSubmissionEditLink(eval.courseId, eval.name)
				+ "\" " + "name=\"editEvaluationSubmission" + idx
				+ "\" id=\"editEvaluationSubmission" + idx + "\" "
				+ "onmouseover=\"ddrivetip('"
				+ Common.HOVER_MESSAGE_EVALUATION_EDIT_SUBMISSION
				+ "')\" onmouseout=\"hideddrivetip()\" "
				+ (hasEdit ? "" : DISABLED) + ">Edit/View Submission</a>";
		// @formatter:off
		
		return result;
	}

	/**
	 * @return The list of available actions for a specific feedback session.
	 */
	public String getStudentFeedbackSessionActions(FeedbackSessionAttributes fs, int idx) {
		
		Boolean hasSubmitted = sessionSubmissionStatusMap.get(fs.courseId+"%"+fs.feedbackSessionName);
		
		boolean hasView = fs.isPublished();
		boolean hasEdit = fs.isOpened();
		
		// @formatter:off
		String result = "<a class=\"color_black\" href=\""
				+ getStudentFeedbackResultsLink(fs.courseId, fs.feedbackSessionName)
				+ "\" " + "name=\"viewFeedbackResults"
				+ idx + "\" " + " id=\"viewFeedbackResults" + idx + "\" "
				+ "onmouseover=\"ddrivetip('" + Common.HOVER_MESSAGE_FEEDBACK_SESSION_RESULTS
				+ "')\" " + "onmouseout=\"hideddrivetip()\" " + (hasView ? "" : DISABLED)
				+ ">" + "View Responses</a>";
				
		if (hasSubmitted) {
			result += "<a class=\"color_black\" href=\""
					+ getStudentFeedbackResponseEditLink(fs.courseId, fs.feedbackSessionName)
					+ "\" " + "name=\"editFeedbackResponses" + idx
					+ "\" id=\"editFeedbackResponses" + idx + "\" "
					+ "onmouseover=\"ddrivetip('"
					+ Common.HOVER_MESSAGE_FEEDBACK_SESSION_EDIT_RESPONSE
					+ "')\" onmouseout=\"hideddrivetip()\" "
					+ (hasEdit ? "" : DISABLED) + ">Edit/View Submission</a>";
		} else {
			result += "<a class=\"color_black\" id=\"submitEvaluation"
					+ idx
					+ "\" "
					+ "href=\""
					+ getStudentFeedbackResponseEditLink(fs.courseId,
						fs.feedbackSessionName) + "\" " + "onmouseover=\"ddrivetip('"
					+ Common.HOVER_MESSAGE_FEEDBACK_SESSION_SUBMIT + "')\" "
					+ "onmouseout=\"hideddrivetip()\">Submit Feedback</a>";	
		}
		// @formatter:off
		
		return result;
	}
}
