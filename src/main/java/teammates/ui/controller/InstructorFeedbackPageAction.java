package teammates.ui.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import teammates.common.Common;
import teammates.common.datatransfer.CourseDetailsBundle;
import teammates.common.datatransfer.EvaluationDetailsBundle;
import teammates.common.datatransfer.FeedbackSessionDetailsBundle;
import teammates.common.exception.EntityDoesNotExistException;
import teammates.common.exception.InvalidParametersException;
import teammates.logic.GateKeeper;

public class InstructorFeedbackPageAction extends Action {

	@Override
	protected ActionResult execute() throws EntityDoesNotExistException,
			InvalidParametersException {
		//This can be null. Non-null value indicates the page is being loaded 
		//   to add a feedback to the specified course
		String courseIdForNewSession = getRequestParam(Common.PARAM_COURSE_ID);
		
		new GateKeeper().verifyInstructorUsingOwnIdOrAbove(account.googleId);
		
		if(courseIdForNewSession!=null){
			new GateKeeper().verifyCourseInstructorOrAbove(courseIdForNewSession);
		}

		InstructorFeedbackPageData data = new InstructorFeedbackPageData(account);
		data.courseIdForNewSession = courseIdForNewSession;
		// This indicates that an empty form to be shown (except possibly the course value filled in)
		data.newFeedbackSession = null; 

		data.courses = loadCoursesList(account.googleId);
		if (data.courses.size() == 0) {
			statusToUser.add(Common.MESSAGE_COURSE_EMPTY_IN_EVALUATION.replace("${user}", "?user="+account.googleId));
			data.existingEvals = new ArrayList<EvaluationDetailsBundle>();
		
		} else {
			data.existingEvals = loadEvaluationsList(account.googleId);			
			data.existingSessions = loadFeedbackSessionsList(account.googleId);
			if (data.existingSessions.size() == 0) {
				statusToUser.add(Common.MESSAGE_FEEDBACK_SESSION_EMPTY);
			} else {
				statusToAdmin = "Number of feedback sessions :"+data.existingSessions.size();
			}
		}	
		
		return createShowPageResult(Common.JSP_INSTRUCTOR_FEEDBACK, data);
	}
	
	protected List<FeedbackSessionDetailsBundle> loadFeedbackSessionsList(
			String googleId) throws EntityDoesNotExistException {
		List<FeedbackSessionDetailsBundle> sessions =
				logic.getFeedbackSessionDetailsForInstructor(googleId);
		
		return sessions;
	}

	protected List<EvaluationDetailsBundle> loadEvaluationsList(String userId)
			throws EntityDoesNotExistException {
		List<EvaluationDetailsBundle> evaluations =
				logic.getEvaluationsDetailsForInstructor(userId);
		EvaluationDetailsBundle.sortEvaluationsByDeadline(evaluations);

		return evaluations;
	}
	
	protected List<CourseDetailsBundle> loadCoursesList(String userId)
			throws EntityDoesNotExistException {
		HashMap<String, CourseDetailsBundle> summary = 
				logic.getCourseSummariesForInstructor(userId);
		List<CourseDetailsBundle>courses = new ArrayList<CourseDetailsBundle>(summary.values());
		CourseDetailsBundle.sortDetailedCoursesByCourseId(courses);
		
		return courses;
	}

}
