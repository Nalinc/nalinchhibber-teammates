package teammates.ui.controller;

import teammates.common.exception.EntityDoesNotExistException;
import teammates.common.util.Assumption;
import teammates.common.util.Const;
import teammates.logic.GateKeeper;

public class InstructorCourseStudentDetailsEditPageAction extends InstructorCoursesPageAction {
	
	
	@Override
	public ActionResult execute() throws EntityDoesNotExistException {

		String courseId = getRequestParam(Const.ParamsNames.COURSE_ID);
		Assumption.assertNotNull(courseId);
		
		String studentEmail = getRequestParam(Const.ParamsNames.STUDENT_EMAIL);
		Assumption.assertNotNull(studentEmail);
		
		new GateKeeper().verifyAccessible(
				logic.getInstructorForGoogleId(courseId, account.googleId),
				logic.getCourse(courseId));
		
		InstructorCourseStudentDetailsEditPageData data = new InstructorCourseStudentDetailsEditPageData(account);
		
		data.student = logic.getStudentForEmail(courseId, studentEmail);
		data.regKey = logic.getKeyForStudent(courseId, studentEmail);
		
		statusToAdmin = "instructorCourseStudentEdit Page Load<br>" + 
				"Editing Student <span class=\"bold\">" + studentEmail +"'s</span> details " +
				"in Course <span class=\"bold\">[" + courseId + "]</span>"; 
		

		return createShowPageResult(Const.ViewURIs.INSTRUCTOR_COURSE_STUDENT_EDIT, data);

	}


}
