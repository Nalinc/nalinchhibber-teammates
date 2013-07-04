package teammates.ui.controller;

import teammates.common.datatransfer.StudentAttributes;
import teammates.common.exception.EntityDoesNotExistException;
import teammates.common.exception.InvalidParametersException;
import teammates.common.util.Const;
import teammates.logic.GateKeeper;

public class AdminAccountDeleteAction extends Action {

	@Override
	protected ActionResult execute() throws EntityDoesNotExistException,
			InvalidParametersException {
		
		new GateKeeper().verifyAdminPrivileges(account);
		
		String instructorId = getRequestParam(Const.ParamsNames.INSTRUCTOR_ID);
		String studentId = getRequestParam(Const.ParamsNames.STUDENT_ID);
		String courseId = getRequestParam(Const.ParamsNames.COURSE_ID);
		String account = getRequestParam("account");
		
		ActionResult result = null;
		
		//TODO: We should extract these into separate actions e.g., AdminInstructorDowngradeAction
		if(courseId == null && account == null){	
			//delete instructor status
			logic.downgradeInstructorToStudentCascade(instructorId);
			statusToUser.add(Const.StatusMessages.INSTRUCTOR_STATUS_DELETED);
			statusToAdmin = "Instructor Status for <span class=\"bold\">" + instructorId + "</span> has been deleted.";
			result = createRedirectResult(Const.ActionURIs.ADMIN_ACCOUNT_MANAGEMENT_PAGE);
			
		} else if (courseId == null && account != null){
			//delete entire account
			logic.deleteAccount(instructorId);
			statusToUser.add(Const.StatusMessages.INSTRUCTOR_ACCOUNT_DELETED);
			statusToAdmin = "Instructor Account for <span class=\"bold\">" + instructorId + "</span> has been deleted.";
			result = createRedirectResult(Const.ActionURIs.ADMIN_ACCOUNT_MANAGEMENT_PAGE);
			
		} else if (courseId != null && instructorId != null){
			//remove instructor from course
			logic.deleteInstructor(courseId, instructorId);
			statusToUser.add(Const.StatusMessages.INSTRUCTOR_REMOVED_FROM_COURSE);
			statusToAdmin = "Instructor <span class=\"bold\">" + instructorId + 
					"</span> has been deleted from Course<span class=\"bold\">[" + courseId + "]</span>"; 
			result = createRedirectResult(Const.ActionURIs.ADMIN_ACCOUNT_DETAILS_PAGE + "?instructorid=" + instructorId);
			
		} else if (courseId != null && studentId != null) {
			//remove student from course
			StudentAttributes student = logic.getStudentForGoogleId(courseId, studentId);
			logic.deleteStudent(courseId, student.email);
			statusToUser.add(Const.StatusMessages.INSTRUCTOR_REMOVED_FROM_COURSE);
			statusToAdmin = "Instructor <span class=\"bold\">" + instructorId + 
					"</span>'s student status in Course<span class=\"bold\">[" + courseId + "]</span> has been deleted"; 
			result = createRedirectResult(Const.ActionURIs.ADMIN_ACCOUNT_DETAILS_PAGE + "?instructorid=" + studentId);
		}		
		
		return result;
	}

}
