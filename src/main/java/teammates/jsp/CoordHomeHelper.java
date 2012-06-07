package teammates.jsp;

import java.util.ArrayList;

import teammates.api.Common;
import teammates.datatransfer.CourseData;
import teammates.datatransfer.EvaluationData;

public class CoordHomeHelper extends Helper {
	public String coordID;
	
	public CourseData[] summary;
	
	public CoordHomeHelper(Helper helper){
		super(helper);
	}
	
	public static EvaluationData[] getEvaluationsForCourse(CourseData course){
		ArrayList<EvaluationData> evaluations = course.evaluations;
		EvaluationData[] evaluationsArr = evaluations.toArray(new EvaluationData[]{});
		return evaluationsArr;
	}
	
	public String getCourseEnrollLink(String courseID){
		String link = Common.JSP_COORD_COURSE_ENROLL;
		link = addParam(link,Common.PARAM_COURSE_ID,courseID);
		if(isMasqueradeMode()){
			link = addParam(link,Common.PARAM_USER_ID,requestedUser);
		}
		return link;
	}

	public String getCourseViewLink(String courseID){
		String link = Common.JSP_COORD_COURSE_DETAILS;
		link = addParam(link,Common.PARAM_COURSE_ID,courseID); 
		if(isMasqueradeMode()){
			link = addParam(link,Common.PARAM_USER_ID,requestedUser);
		}
		return link;
	}
	
	public String getCourseDeleteLink(String courseID){
		String link = Common.JSP_COORD_COURSE_DELETE;
		link = addParam(link,Common.PARAM_COURSE_ID,courseID);
		link = addParam(link,Common.PARAM_NEXT_URL,Common.JSP_COORD_HOME);
		if(isMasqueradeMode()){
			link = addParam(link,Common.PARAM_USER_ID,requestedUser);
		}
		return link;
	}
}
