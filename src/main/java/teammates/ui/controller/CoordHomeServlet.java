package teammates.ui.controller;

import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import teammates.common.Common;
import teammates.common.datatransfer.CourseData;
import teammates.common.exception.EntityDoesNotExistException;

@SuppressWarnings("serial")
/**
 * Servlet to handle Home actions
 */
public class CoordHomeServlet extends ActionServlet<CoordHomeHelper> {

	@Override
	protected CoordHomeHelper instantiateHelper() {
		return new CoordHomeHelper();
	}


	@Override
	protected void doAction(HttpServletRequest req, CoordHomeHelper helper) throws EntityDoesNotExistException{
		HashMap<String, CourseData> courses = helper.server.getCourseDetailsListForCoord(helper.userId);
		helper.courses = new ArrayList<CourseData>(courses.values());
		sortCourses(helper.courses);
		for(CourseData course: helper.courses){
			sortEvaluationsByDeadline(course.evaluations);
		}
	}

	@Override
	protected String getDefaultForwardUrl() {
		return Common.JSP_COORD_HOME;
	}
}
