package teammates.ui.controller;

import teammates.common.Common;
import teammates.common.exception.EntityDoesNotExistException;
import teammates.common.exception.InvalidParametersException;
import teammates.logic.GateKeeper;

public class InstructorEvalPublishAction extends InstructorEvalPageAction {
	
	@Override
	protected ActionResult execute() 
			throws EntityDoesNotExistException,	InvalidParametersException {
		
		String courseId = getRequestParam(Common.PARAM_COURSE_ID);
		String evalName = getRequestParam(Common.PARAM_EVALUATION_NAME);
		
		new GateKeeper().verifyCourseInstructorOrAbove(courseId);
		
		logic.publishEvaluation(courseId,evalName);
		
		statusToUser.add(Common.MESSAGE_EVALUATION_PUBLISHED);
		statusToAdmin = "Evaluation <span class=\"bold\">(" + evalName + ")</span> " +
				"for Course <span class=\"bold\">[" + courseId + "]</span> published.";
		
		return createRedirectResult(Common.PAGE_INSTRUCTOR_EVAL);
	}
	
}
