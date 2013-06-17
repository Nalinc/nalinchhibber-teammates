package teammates.test.cases.logic;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.HashMap;
import java.util.List;

import javax.mail.internet.MimeMessage;
import javax.servlet.ServletException;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import teammates.common.Common;
import teammates.common.datatransfer.CourseAttributes;
import teammates.common.datatransfer.DataBundle;
import teammates.common.datatransfer.EvaluationAttributes;
import teammates.common.datatransfer.EvaluationAttributes.EvalStatus;
import teammates.common.datatransfer.InstructorAttributes;
import teammates.common.datatransfer.StudentAttributes;
import teammates.common.datatransfer.SubmissionAttributes;
import teammates.common.exception.EntityAlreadyExistsException;
import teammates.common.exception.InvalidParametersException;
import teammates.logic.Emails;
import teammates.logic.backdoor.BackDoorLogic;
import teammates.storage.api.CoursesDb;
import teammates.storage.datastore.Datastore;
import teammates.test.cases.BaseComponentTest;
import teammates.test.cases.common.CourseAttributesTest;

import com.google.gson.Gson;

public class BackDoorLogicTest extends BaseComponentTest {
	Gson gson = Common.getTeammatesGson();
	private static DataBundle dataBundle = getTypicalDataBundle();

	@BeforeClass
	public static void classSetUp() throws Exception {
		printTestClassHeader();
		turnLoggingUp(BackDoorLogic.class);
	}

	@BeforeMethod
	public void caseSetUp() throws ServletException {
		dataBundle = getTypicalDataBundle();
		resetDatastore();
	}

	@Test
	public void testPersistDataBundle() throws Exception {

		BackDoorLogic logic = new BackDoorLogic();
		String jsonString = "";
		jsonString = Common.readFile(Common.TEST_DATA_FOLDER + "/typicalDataBundle.json");
		loginAsAdmin("admin.user");
		
		DataBundle dataBundle = gson.fromJson(jsonString, DataBundle.class);
		// clean up the datastore first, to avoid clashes with existing data
		HashMap<String, InstructorAttributes> instructors = dataBundle.instructors;
		for (InstructorAttributes instructor : instructors.values()) {
			logic.deleteInstructor(instructor.courseId, instructor.googleId);
		}
		______TS("empty data bundle");
		String status = logic.persistDataBundle(new DataBundle());
		assertEquals(Common.BACKEND_STATUS_SUCCESS, status);

		logic.persistDataBundle(dataBundle);
		verifyPresentInDatastore(jsonString);

		______TS("try to persist while entities exist");
		try {
			logic.persistDataBundle(dataBundle);
			Assert.fail();
		} catch (EntityAlreadyExistsException e) {
			ignoreExpectedException();
		}

		______TS("null parameter");
		DataBundle nullDataBundle = null;
		try {
			logic.persistDataBundle(nullDataBundle);
			Assert.fail();
		} catch (InvalidParametersException e) {
			assertEquals(Common.ERRORCODE_NULL_PARAMETER, e.errorCode);
		}

		______TS("invalid parameters in an entity");
		CourseAttributes invalidCourse = CourseAttributesTest.generateValidCourseAttributesObject();
		invalidCourse.id = "invalid id";
		dataBundle = new DataBundle();
		dataBundle.courses.put("invalid", invalidCourse);
		try {
			logic.persistDataBundle(dataBundle);
			Assert.fail();
		} catch (InvalidParametersException e) {
			assertTrue(e.getMessage().contains("not acceptable to TEAMMATES as a Course ID because it is not in the correct format"));
		}

		// Not checking for invalid values in other entities because they
		// should be checked at lower level methods
	}

		@Test
	public void testGetSubmission() throws Exception {

		restoreTypicalDataInDatastore();

		______TS("typical case");
		SubmissionAttributes expected = dataBundle.submissions
				.get("submissionFromS1C1ToS1C1");
		LogicTest.verifyPresentInDatastore(expected);

		______TS("null parameters");
		// no need to check for null as this is a private method

		______TS("non-existent");

		assertEquals(
				null,
				LogicTest.invokeGetSubmission("non-existent", expected.evaluation,
						expected.reviewer, expected.reviewee));
		assertEquals(
				null,
				LogicTest.invokeGetSubmission(expected.course, "non-existent",
						expected.reviewer, expected.reviewee));
		assertEquals(
				null,
				LogicTest.invokeGetSubmission(expected.course, expected.evaluation,
						"non-existent", expected.reviewee));
		assertEquals(
				null,
				LogicTest.invokeGetSubmission(expected.course, expected.evaluation,
						expected.reviewer, "non-existent"));
	}
	
	private void verifyPresentInDatastore(String dataBundleJsonString)
			throws Exception {
	
		DataBundle data = gson.fromJson(dataBundleJsonString, DataBundle.class);
		HashMap<String, InstructorAttributes> instructors = data.instructors;
		for (InstructorAttributes expectedInstructor : instructors.values()) {
			LogicTest.verifyPresentInDatastore(expectedInstructor);
		}
	
		HashMap<String, CourseAttributes> courses = data.courses;
		for (CourseAttributes expectedCourse : courses.values()) {
			LogicTest.verifyPresentInDatastore(expectedCourse);
		}
	
		HashMap<String, StudentAttributes> students = data.students;
		for (StudentAttributes expectedStudent : students.values()) {
			LogicTest.verifyPresentInDatastore(expectedStudent);
		}
	
		HashMap<String, EvaluationAttributes> evaluations = data.evaluations;
		for (EvaluationAttributes expectedEvaluation : evaluations.values()) {
			LogicTest.verifyPresentInDatastore(expectedEvaluation);
		}
	
		HashMap<String, SubmissionAttributes> submissions = data.submissions;
		for (SubmissionAttributes expectedSubmission : submissions.values()) {
			LogicTest.verifyPresentInDatastore(expectedSubmission);
		}
	
	}

	/*
	 * Following methods are tested by the testPersistDataBundle method
		getAccountAsJson(String)
		getInstructorAsJson(String, String)
		getCourseAsJson(String)
		getStudentAsJson(String, String)
		getEvaluationAsJson(String, String)
		getSubmissionAsJson(String, String, String, String)
		editAccountAsJson(String)
		editStudentAsJson(String, String)
		editEvaluationAsJson(String)
		editSubmissionAsJson(String)
		editEvaluation(EvaluationAttributes)
		createCourse(String, String)
	*/
	


	@AfterClass
	public static void classTearDown() throws Exception {
		printTestClassFooter();
		turnLoggingDown(BackDoorLogic.class);
	}

}
