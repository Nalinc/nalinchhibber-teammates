package teammates.ui.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import teammates.common.Assumption;
import teammates.common.Common;
import teammates.common.datatransfer.CourseDetailsBundle;
import teammates.common.datatransfer.EvaluationAttributes;
import teammates.common.datatransfer.EvaluationDetailsBundle;
import teammates.common.datatransfer.StudentAttributes;
import teammates.common.exception.EntityDoesNotExistException;
import teammates.common.exception.InvalidParametersException;
import teammates.logic.GateKeeper;

public class StudentHomePageAction extends Action {
	
	private StudentHomePageData data;
	

	@Override
	public ActionResult execute() throws EntityDoesNotExistException {
		
		new GateKeeper().verifyOwnerOfId(account.googleId);
		
		data = new StudentHomePageData(account);
		
		try{
			data.courses = logic.getCourseDetailsListForStudent(account.googleId);
			data.evalSubmissionStatusMap = generateEvalSubmissionStatusMap(data.courses, account.googleId);
			CourseDetailsBundle.sortDetailedCourses(data.courses);
			for(CourseDetailsBundle course: data.courses){
				EvaluationDetailsBundle.sortEvaluationsByDeadline(course.evaluations);
			}
			
			statusToAdmin = "studentHome Page Load<br>" + "Total courses: " + data.courses.size();
			
		} catch (EntityDoesNotExistException e){
			statusToUser.add(Common.MESSAGE_STUDENT_FIRST_TIME);
			statusToAdmin = Common.LOG_SERVLET_ACTION_FAILURE + " :" + e.getMessage();
		}
		
		ShowPageResult response = createShowPageResult(Common.JSP_STUDENT_HOME, data);
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


	private String getStudentStatusForEval(EvaluationAttributes eval, String googleId){
		
		StudentAttributes student = logic.getStudentForGoogleId(eval.courseId, googleId);
		Assumption.assertNotNull(student);

		String studentEmail = student.email;
		
		switch (eval.getStatus()) {
		case PUBLISHED:
			return Common.STUDENT_EVALUATION_STATUS_PUBLISHED;
		case CLOSED:
			return Common.STUDENT_EVALUATION_STATUS_CLOSED;
		default:
			break; // continue processing.
		}
		
		boolean submitted = false;
		
		try {
			submitted = logic.hasStudentSubmittedEvaluation(eval.courseId, eval.name, studentEmail);
		} catch (InvalidParametersException e) {
			Assumption.fail("Parameters are expected to be valid at this point :" + Common.stackTraceToString(e));
		}
		
		return submitted ? 
				Common.STUDENT_EVALUATION_STATUS_SUBMITTED 
				: Common.STUDENT_EVALUATION_STATUS_PENDING;
	}

}
