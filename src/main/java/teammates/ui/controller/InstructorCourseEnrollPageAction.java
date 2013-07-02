package teammates.ui.controller;

import java.util.logging.Logger;

import teammates.common.util.Assumption;
import teammates.common.util.Config;
import teammates.logic.GateKeeper;

public class InstructorCourseEnrollPageAction extends Action {
	protected static final Logger log = Config.getLogger();
	
	
	@Override
	public ActionResult execute() {
		String courseId = getRequestParam(Config.PARAM_COURSE_ID);
		Assumption.assertNotNull(courseId);
		
		new GateKeeper().verifyAccessible(
				logic.getInstructorForGoogleId(courseId, account.googleId),
				logic.getCourse(courseId));
		
		InstructorCourseEnrollPageData data = new InstructorCourseEnrollPageData(account);
		data.courseId = courseId;
		
		statusToAdmin = "instructorCourseEnroll Page Load<br>"
				+ "Enrollment for Course <span class=\"bold\">[" + courseId + "]</span>"; 
		
		return createShowPageResult(Config.JSP_INSTRUCTOR_COURSE_ENROLL, data);
	}


}
