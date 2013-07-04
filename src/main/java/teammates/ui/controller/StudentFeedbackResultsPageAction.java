package teammates.ui.controller;

import teammates.common.exception.EntityDoesNotExistException;
import teammates.common.exception.InvalidParametersException;
import teammates.common.exception.UnauthorizedAccessException;
import teammates.common.util.Const;
import teammates.logic.GateKeeper;

public class StudentFeedbackResultsPageAction extends Action {

	@Override
	protected ActionResult execute() throws EntityDoesNotExistException,
			InvalidParametersException {
		
		String courseId = getRequestParam(Const.ParamsNames.COURSE_ID);
		String feedbackSessionName = getRequestParam(Const.ParamsNames.FEEDBACK_SESSION_NAME);
		
		if(courseId==null || feedbackSessionName == null) {
			return createRedirectResult(Const.ActionURIs.STUDENT_HOME);
		}
		
		if(notYetJoinedCourse(courseId, account.googleId)){
			return createPleaseJoinCourseResponse(courseId);
		}
		
		new GateKeeper().verifyAccessible(
				logic.getStudentForGoogleId(courseId, account.googleId), 
				logic.getFeedbackSession(feedbackSessionName, courseId));
		
		StudentFeedbackResultsPageData data = new StudentFeedbackResultsPageData(account);
		
		data.student = logic.getStudentForGoogleId(courseId, account.googleId);
		data.bundle = logic.getFeedbackSessionResultsForUser(feedbackSessionName, courseId, data.student.email);
		
		if(data.bundle == null) {
			throw new EntityDoesNotExistException("Feedback session "+feedbackSessionName+" does not exist in "+courseId+".");
		}
		if (data.bundle.feedbackSession.isPublished() == false) {
			throw new UnauthorizedAccessException(
					"This feedback session is not yet visible.");
		}
		
		return createShowPageResult(Const.ViewURIs.STUDENT_FEEDBACK_RESULTS, data);
	}
}
