package teammates.ui.controller;

import java.util.ArrayList;
import java.util.logging.Logger;

import teammates.common.datatransfer.CourseDetailsBundle;
import teammates.common.exception.EntityDoesNotExistException;
import teammates.common.util.Const;
import teammates.common.util.Utils;
import teammates.logic.api.GateKeeper;

/**
 * Action: loading of the 'Courses' page for an instructor.
 */
public class InstructorCoursesPageAction extends Action {
	/* Explanation: Get a logger to be used for any logging */
	protected static final Logger log = Utils.getLogger();
	
	
	@Override
	public ActionResult execute() 
			throws EntityDoesNotExistException {
		/* Explanation: First, we extract any parameters from the request object.
		 * e.g., idOfCourseToDelete = getRequestParam(Const.ParamsNames.COURSE_ID);
		 * In this Action, there are no parameters.*/
		
		/* Explanation: Next, check if the user has rights to execute the action.*/
		new GateKeeper().verifyInstructorPrivileges(account);
		
		/* Explanation: This is a 'show page' type action. Therefore, we 
		 * prepare the matching PageData object, accessing the Logic 
		 * component if necessary.*/
		InstructorCoursesPageData data = new InstructorCoursesPageData(account);
		data.newCourse = null;
		data.instructorListToShow = account.googleId + "|" + account.name + "|" + account.email;
		data.courseIdToShow = "";
		data.courseNameToShow = "";
		
		data.currentCourses = new ArrayList<CourseDetailsBundle>(
				logic.getCourseSummariesForInstructor(account.googleId).values());
		CourseDetailsBundle.sortDetailedCoursesByCourseId(data.currentCourses);
		
		/* Explanation: Set any status messages that should be shown to the user.*/
		if (data.currentCourses.size() == 0 ){
			statusToUser.add(Const.StatusMessages.COURSE_EMPTY);
		}
		
		/* Explanation: We must set this variable. It is the text that will 
		 * represent this particular execution of this action in the
		 * 'adming activity log' page.*/
		statusToAdmin = "instructorCourse Page Load<br>" 
				+ "Total courses: " + data.currentCourses.size();
		
		/* Explanation: Create the appropriate result object and return it.*/
		ShowPageResult response = createShowPageResult(Const.ViewURIs.INSTRUCTOR_COURSES, data);
		return response;
	}


}
