package teammates.test.cases.ui;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import teammates.common.datatransfer.DataBundle;
import teammates.common.datatransfer.EvaluationAttributes;
import teammates.common.datatransfer.EvaluationAttributes.EvalStatus;
import teammates.common.datatransfer.InstructorAttributes;
import teammates.common.datatransfer.StudentAttributes;
import teammates.common.util.Config;
import teammates.common.util.TimeHelper;
import teammates.storage.api.EvaluationsDb;
import teammates.ui.controller.ControllerServlet;

public class StudentEvalResultsPageActionTest extends BaseActionTest {

	DataBundle dataBundle;
	EvaluationsDb evaluationsDb = new EvaluationsDb();
	
	@BeforeClass
	public static void classSetUp() throws Exception {
		printTestClassHeader();
		URI = Config.PAGE_STUDENT_EVAL_RESULTS;
		sr.registerServlet(URI, ControllerServlet.class.getName());
	}

	@BeforeMethod
	public void methodSetUp() throws Exception {
		dataBundle = getTypicalDataBundle();
		restoreTypicalDataInDatastore();
	}
	
	@Test
	public void testAccessControl() throws Exception{
		
		EvaluationAttributes eval = dataBundle.evaluations.get("evaluation1InCourse1");
		eval.endTime = TimeHelper.getDateOffsetToCurrentTime(-1);
		eval.published = true;
		assertEquals(EvalStatus.PUBLISHED, eval.getStatus());
		evaluationsDb.updateEvaluation(eval);
		
		InstructorAttributes instructor1OfCourse1 = dataBundle.instructors.get("instructor1OfCourse1");
		String instructorId = instructor1OfCourse1.googleId;
		StudentAttributes student1InCourse1 = dataBundle.students.get("student1InCourse1");
		String studentId = student1InCourse1.googleId;
		
		String[] submissionParams = new String[]{
				Config.PARAM_COURSE_ID, eval.courseId,
				Config.PARAM_EVALUATION_NAME, eval.name
				};
		
		verifyUnaccessibleWithoutLogin(submissionParams);
		
		//if the user is not a student of the course, we redirect to home page.
		loginUser("unreg.user");
		verifyRedirectTo(Config.PAGE_STUDENT_HOME, submissionParams);
		verifyCannotMasquerade(addUserIdToParams(studentId,submissionParams));
		
		verifyAccessibleForStudentsOfTheSameCourse(submissionParams);
		
		//if the user is not a student of the course, we redirect to home page.
		loginAsInstructor(instructorId);
		verifyRedirectTo(Config.PAGE_STUDENT_HOME, submissionParams);
		verifyCannotMasquerade(addUserIdToParams(studentId,submissionParams));
		
		verifyAccessibleForAdminToMasqueradeAsStudent(submissionParams);
		
	}

	@Test
	public void testExecuteAndPostProcess() throws Exception{
		
		//TODO: implement this
		//TODO: ensure results are not viewable when not PUBLISHED
	}

	
}
