package teammates.ui.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import teammates.common.datatransfer.CourseDetailsBundle;
import teammates.common.datatransfer.EvaluationAttributes;
import teammates.common.datatransfer.EvaluationDetailsBundle;
import teammates.common.datatransfer.FeedbackSessionAttributes;
import teammates.common.datatransfer.FeedbackSessionDetailsBundle;
import teammates.common.datatransfer.StudentAttributes;
import teammates.common.exception.EntityDoesNotExistException;
import teammates.common.exception.InvalidParametersException;
import teammates.common.exception.TeammatesException;
import teammates.common.util.Assumption;
import teammates.common.util.Constants;
import teammates.logic.GateKeeper;

public class StudentHomePageAction extends Action {
	
	private StudentHomePageData data;
	

	@Override
	public ActionResult execute() throws EntityDoesNotExistException {
		
		new GateKeeper().verifyLoggedInUserPrivileges();
		
		data = new StudentHomePageData(account);
		
		try{
			data.courses = logic.getCourseDetailsListForStudent(account.googleId);
			data.evalSubmissionStatusMap = generateEvalSubmissionStatusMap(data.courses, account.googleId);
			data.sessionSubmissionStatusMap = generateFeedbackSessionSubmissionStatusMap(data.courses, account.googleId);
			CourseDetailsBundle.sortDetailedCourses(data.courses);
			for(CourseDetailsBundle course: data.courses){
				EvaluationDetailsBundle.sortEvaluationsByDeadline(course.evaluations);
				FeedbackSessionDetailsBundle.sortFeedbackSessionsByCreationTime(course.feedbackSessions);
			}
			
			statusToAdmin = "studentHome Page Load<br>" + "Total courses: " + data.courses.size();
			
		} catch (EntityDoesNotExistException e){
			statusToUser.add(Constants.STATUS_STUDENT_FIRST_TIME);
			statusToAdmin = Constants.ACTION_RESULT_FAILURE + " :" + e.getMessage();
		}
		
		ShowPageResult response = createShowPageResult(Constants.VIEW_STUDENT_HOME, data);
		return response;

	}
	
	
	private Map<String, String> generateEvalSubmissionStatusMap(
			List<CourseDetailsBundle> courses, String googleId) {
		Map<String, String> returnValue = new HashMap<String, String>();

		for(CourseDetailsBundle c: courses){
			for(EvaluationDetailsBundle edb: c.evaluations){
				EvaluationAttributes e = edb.evaluation;
				returnValue.put(e.courseId+"%"+e.name, getStudentStatusForEval(e, googleId));
			}
		}
		return returnValue;
	}

	private Map<String, Boolean> generateFeedbackSessionSubmissionStatusMap(
			List<CourseDetailsBundle> courses, String googleId) {
		Map<String, Boolean> returnValue = new HashMap<String, Boolean>();

		for(CourseDetailsBundle c: courses){
			for(FeedbackSessionDetailsBundle fsb: c.feedbackSessions){
				FeedbackSessionAttributes f = fsb.feedbackSession;
				returnValue.put(f.courseId+"%"+f.feedbackSessionName, getStudentStatusForSession(f, googleId));
			}
		}
		return returnValue;
	}

	private String getStudentStatusForEval(EvaluationAttributes eval, String googleId){
		
		StudentAttributes student = logic.getStudentForGoogleId(eval.courseId, googleId);
		Assumption.assertNotNull(student);

		String studentEmail = student.email;
		
		switch (eval.getStatus()) {
		case PUBLISHED:
			return Constants.STUDENT_EVALUATION_STATUS_PUBLISHED;
		case CLOSED:
			return Constants.STUDENT_EVALUATION_STATUS_CLOSED;
		default:
			break; // continue processing.
		}
		
		boolean submitted = false;
		
		try {
			submitted = logic.hasStudentSubmittedEvaluation(eval.courseId, eval.name, studentEmail);
		} catch (InvalidParametersException e) {
			Assumption.fail("Parameters are expected to be valid at this point :" + TeammatesException.toStringWithStackTrace(e));
		}
		
		return submitted ? 
				Constants.STUDENT_EVALUATION_STATUS_SUBMITTED 
				: Constants.STUDENT_EVALUATION_STATUS_PENDING;
	}
	
	private boolean getStudentStatusForSession(FeedbackSessionAttributes fs, String googleId){
		
		StudentAttributes student = logic.getStudentForGoogleId(fs.courseId, googleId);
		Assumption.assertNotNull(student);

		String studentEmail = student.email;
		
		try {
			return logic.hasStudentSubmittedFeedback(
					fs.courseId, fs.feedbackSessionName, studentEmail);
		} catch (InvalidParametersException | EntityDoesNotExistException e) {
			Assumption.fail("Parameters are expected to be valid at this point :" + TeammatesException.toStringWithStackTrace(e));
			return false;
		}
	}

}
