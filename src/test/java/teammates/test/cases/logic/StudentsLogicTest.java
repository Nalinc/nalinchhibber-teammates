package teammates.test.cases.logic;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import teammates.common.datatransfer.AccountAttributes;
import teammates.common.datatransfer.CourseAttributes;
import teammates.common.datatransfer.DataBundle;
import teammates.common.datatransfer.EvaluationAttributes;
import teammates.common.datatransfer.FeedbackParticipantType;
import teammates.common.datatransfer.FeedbackQuestionAttributes;
import teammates.common.datatransfer.FeedbackResponseAttributes;
import teammates.common.datatransfer.StudentAttributes;
import teammates.common.datatransfer.StudentAttributesFactory;
import teammates.common.datatransfer.StudentEnrollDetails;
import teammates.common.datatransfer.SubmissionAttributes;
import teammates.common.exception.EnrollException;
import teammates.common.exception.EntityDoesNotExistException;
import teammates.common.exception.InvalidParametersException;
import teammates.common.util.Const;
import teammates.common.util.FieldValidator;
import teammates.common.util.StringHelper;
import teammates.logic.core.AccountsLogic;
import teammates.logic.core.CoursesLogic;
import teammates.logic.core.EvaluationsLogic;
import teammates.logic.core.FeedbackQuestionsLogic;
import teammates.logic.core.FeedbackResponsesLogic;
import teammates.logic.core.StudentsLogic;
import teammates.logic.core.SubmissionsLogic;
import teammates.storage.api.StudentsDb;
import teammates.storage.entity.Student;
import teammates.test.cases.BaseComponentTestCase;
import teammates.test.cases.storage.EvaluationsDbTest;
import teammates.test.driver.AssertHelper;

import com.google.appengine.api.datastore.KeyFactory;

public class StudentsLogicTest extends BaseComponentTestCase{
	
	//TODO: add missing test cases. Some of the test content can be transferred from LogicTest.
	
	protected static StudentsLogic studentsLogic = StudentsLogic.inst();
	protected static SubmissionsLogic submissionsLogic = SubmissionsLogic.inst();
	protected static AccountsLogic accountsLogic = AccountsLogic.inst();
	protected static CoursesLogic coursesLogic = CoursesLogic.inst();
	protected static EvaluationsLogic evaluationsLogic = EvaluationsLogic.inst();
	private static DataBundle dataBundle = getTypicalDataBundle();
	
	@BeforeClass
	public static void classSetUp() throws Exception {
		printTestClassHeader();
		turnLoggingUp(StudentsLogic.class);
	}
	
	
	@SuppressWarnings("deprecation")
	@Test
	public void testEnrollStudent() throws Exception {

		String instructorId = "instructorForEnrollTesting";
		String instructorCourse = "courseForEnrollTesting";
		
		//delete leftover data, if any
		accountsLogic.deleteAccountCascade(instructorId);
		coursesLogic.deleteCourseCascade(instructorCourse);
		
		//create fresh test data
		accountsLogic.createAccount(
				new AccountAttributes(instructorId, "ICET Instr Name", true,
						"instructor@icet.com", "National University of Singapore"));
		coursesLogic.createCourseAndInstructor(instructorId, instructorCourse, "Course for Enroll Testing");

		______TS("add student into empty course");

		StudentAttributes student1 = new StudentAttributes("t1", "n", "e@g", "c", instructorCourse);

		// check if the course is empty
		assertEquals(0, studentsLogic.getStudentsForCourse(instructorCourse).size());

		// add a new student and verify it is added and treated as a new student
		StudentEnrollDetails enrollmentResult = invokeEnrollStudent(student1);
		assertEquals(1, studentsLogic.getStudentsForCourse(instructorCourse).size());
		LogicTest.verifyEnrollmentDetailsForStudent(student1, null, enrollmentResult,
				StudentAttributes.UpdateStatus.NEW);
		LogicTest.verifyPresentInDatastore(student1);

		______TS("add existing student");

		// Verify it was not added
		enrollmentResult = invokeEnrollStudent(student1);
		LogicTest.verifyEnrollmentDetailsForStudent(student1, null, enrollmentResult,
				StudentAttributes.UpdateStatus.UNMODIFIED);
		assertEquals(1, studentsLogic.getStudentsForCourse(instructorCourse).size());

		______TS("add student into non-empty course");
		StudentAttributes student2 = new StudentAttributes("t1", "n2", "e2@g", "c", instructorCourse);
		enrollmentResult = invokeEnrollStudent(student2);
		LogicTest.verifyEnrollmentDetailsForStudent(student2, null, enrollmentResult,
				StudentAttributes.UpdateStatus.NEW);
		
		//add some more students to the same course (we add more than one 
		//  because we can use them for testing cascade logic later in this test case)
		invokeEnrollStudent(new StudentAttributes("t2", "n3", "e3@g", "c", instructorCourse));
		invokeEnrollStudent(new StudentAttributes("t2", "n4", "e4@g", "", instructorCourse));
		assertEquals(4, studentsLogic.getStudentsForCourse(instructorCourse).size());
		
		______TS("modify info of existing student");
		//add some more details to the student
		student1.googleId = "googleId";
		studentsLogic.updateStudentCascade(student1.email, student1);
		
		______TS("add evaluation and modify team of existing student" +
				"(to check the cascade logic of the SUT)");
		EvaluationAttributes e = EvaluationsDbTest.generateTypicalEvaluation();
		e.courseId = instructorCourse;
		evaluationsLogic.createEvaluationCascadeWithoutSubmissionQueue(e);

		//add some more details to the student
		String oldTeam = student2.team;
		student2.team = "t2";
		
		//Save student structure before changing of teams
		//This allows for verification of existence of old submissions 
		List<StudentAttributes> studentDetailsBeforeModification = studentsLogic
				.getStudentsForCourse(instructorCourse);
		
		enrollmentResult = invokeEnrollStudent(student2);
		LogicTest.verifyPresentInDatastore(student2);
		LogicTest.verifyEnrollmentDetailsForStudent(student2, oldTeam, enrollmentResult,
				StudentAttributes.UpdateStatus.MODIFIED);
		
		//verify that submissions have not been adjusted
		//i.e no new submissions have been added
		List<SubmissionAttributes> student2Submissions = submissionsLogic
				.getSubmissionsForEvaluation(instructorCourse, e.name);
		
		for (SubmissionAttributes submission : student2Submissions) {
			boolean isStudent2Participant = (submission.reviewee == student2.email) ||
										   (submission.reviewer == student2.email);
			boolean isNewTeam = submission.team == student2.team;
			
			assertFalse(isNewTeam && isStudent2Participant);
		}
		
		//also, verify that the datastore still has the old team structure
		LogicTest.verifySubmissionsExistForCurrentTeamStructureInEvaluation(e.name,
				studentDetailsBeforeModification, submissionsLogic.getSubmissionsForCourse(instructorCourse));

		______TS("error during enrollment");

		StudentAttributes student5 = new StudentAttributes("", "n6", "e6@g@", "", instructorCourse);
		enrollmentResult = invokeEnrollStudent(student5);
		assertEquals (StudentAttributes.UpdateStatus.ERROR, enrollmentResult.updateStatus);
		assertEquals(4, studentsLogic.getStudentsForCourse(instructorCourse).size());
		
	}
	
	@Test
	public void testEnrollStudents() throws Exception {
		
		CourseAttributes course1 = dataBundle.courses.get("typicalCourse1");
		dataBundle = getTypicalDataBundle();
		
		List<SubmissionAttributes> allSubmissions = submissionsLogic
				.getSubmissionsForCourse(course1.id);
		
		restoreTypicalDataInDatastore();
		
		allSubmissions = submissionsLogic
				.getSubmissionsForCourse(course1.id);
		
		//Verify that all submissions have been added. 
		//Expected number : 2 * (4 * 4 + 1 * 1) = 34 
		assertEquals(34, allSubmissions.size());
		
		______TS("enrolling students to a non-existent course");
		String newStudentLine = "Team 1.1|n|s@g|c";
		String nonExistentCourseId = "courseDoesNotExist";
		String enrollLines = newStudentLine + Const.EOL;
		
		List<StudentAttributes> studentsInfo;
		try {
			studentsInfo = studentsLogic
					.enrollStudents(enrollLines, nonExistentCourseId);
			assertTrue(false);
		} catch (EntityDoesNotExistException e) {
			assertTrue(true);
		}
		
		______TS("try to enroll with empty input enroll lines");
		enrollLines = "";
		
		try {
			studentsInfo = studentsLogic
					.enrollStudents(enrollLines, course1.id);
			assertTrue(false);
		} catch (EnrollException e) {
			String errorMessage = e.getLocalizedMessage();
			assertEquals(Const.StatusMessages.ENROLL_LINE_EMPTY, errorMessage);
		}
		
		______TS("enroll new students to existing course" +
				"(to check the cascade logic of the SUT)");

		//enroll string can aslo contain whitespace lines
		enrollLines = newStudentLine + Const.EOL + "\t";
		
		studentsInfo = studentsLogic
				.enrollStudents(enrollLines, course1.id);
		
		//Check whether students are present in database
		LogicTest.verifyPresentInDatastore(studentsInfo.get(0));
		
		//Check whether required submissions were created/adjusted 
		verifyCascasedToSubmissions(course1.id);
		allSubmissions = submissionsLogic
				.getSubmissionsForCourse(course1.id);
		
		//Verify that all submissions have been added. 
		//Expected number : 2 * (5 * 5 + 1 * 1) = 52 
		assertEquals(52, allSubmissions.size());
		
		______TS("change an existing students email and verify submissions after update");
		String oldEmail = studentsInfo.get(0).email;
		StudentAttributes updatedAttributes = new StudentAttributes();
		updatedAttributes.email = "newEmail@g";
		updatedAttributes.course = course1.id;

		studentsLogic.updateStudentCascade(oldEmail, updatedAttributes);

		StudentAttributes updatedStudent = studentsLogic
				.getStudentForEmail(course1.id, updatedAttributes.email);
		
		LogicTest.verifyPresentInDatastore(updatedStudent);
		verifySubmissionsDoNotExistForEmailInCourse(oldEmail, course1.id);
		verifyCascasedToSubmissions(course1.id);
		
		______TS("change the case of existing students email and verify submissions after update");
		oldEmail = updatedAttributes.email;
		updatedAttributes = new StudentAttributes();
		updatedAttributes.email = "newEMAIL@g";
		updatedAttributes.course = course1.id;

		assertTrue(oldEmail.equalsIgnoreCase(updatedAttributes.email));
		studentsLogic.updateStudentCascade(oldEmail, updatedAttributes);

		updatedStudent = studentsLogic
				.getStudentForEmail(course1.id, updatedAttributes.email);
		
		LogicTest.verifyPresentInDatastore(updatedStudent);
		verifySubmissionsDoNotExistForEmailInCourse(oldEmail, course1.id);
		verifyCascasedToSubmissions(course1.id);
		
		______TS("change team of existing student and verify deletion of all his responses");
		StudentAttributes studentInTeam1 = dataBundle.students.get("student1InCourse1");
		
		//verify he has existing team feedback responses in the system
		List<FeedbackResponseAttributes> student1responses = getAllTeamResponsesForStudent(studentInTeam1);
		assertTrue(student1responses.size() != 0);
		
		studentInTeam1.team = "Team 1.2";
		String student1enrollString = studentInTeam1.toEnrollmentString();
		studentsInfo = studentsLogic.enrollStudents(student1enrollString, studentInTeam1.course);
		
		//verify all previous team responses for student have been deleted
		student1responses = getAllTeamResponsesForStudent(studentInTeam1);
		assertTrue(student1responses.size() == 0);
		
		//Verify that new submissions have been added and old deleted
		//Expected number : 2 * (4 * 4 + 2 * 2) = 40 
		allSubmissions = submissionsLogic
				.getSubmissionsForCourse(course1.id);
		assertEquals(40, allSubmissions.size());
		
		verifyCascasedToSubmissions(studentInTeam1.course);

		______TS("error during enrollment");
		String invalidStudentId = "t1|n6|e6@g@";
		String invalidEnrollLine = invalidStudentId + Const.EOL;
		try {
			studentsInfo = studentsLogic
					.enrollStudents(invalidEnrollLine, course1.id);
			assertTrue(false);
		} catch (EnrollException e) {
			String actualErrorMessage = e.getLocalizedMessage();

			String errorReason = String.format(FieldValidator.EMAIL_ERROR_MESSAGE, "e6@g@",
					FieldValidator.REASON_INCORRECT_FORMAT);
			String expectedMessage = String.format(Const.StatusMessages.ENROLL_LINES_PROBLEM,
					invalidStudentId, errorReason);
			
			assertEquals(expectedMessage, actualErrorMessage);
		}
		
	}

	@Test
	public void testUpdateStudentCascade() throws Exception {
			
		______TS("typical edit");

		restoreTypicalDataInDatastore();
		dataBundle = getTypicalDataBundle();

		StudentAttributes student1InCourse1 = dataBundle.students.get("student1InCourse1");
		LogicTest.verifyPresentInDatastore(student1InCourse1);
		String originalEmail = student1InCourse1.email;
		student1InCourse1.name = student1InCourse1.name + "x";
		student1InCourse1.googleId = student1InCourse1.googleId + "x";
		student1InCourse1.comments = student1InCourse1.comments + "x";
		student1InCourse1.email = student1InCourse1.email + "x";
		student1InCourse1.team = "Team 1.2"; // move to a different team

		// take a snapshot of submissions before
		List<SubmissionAttributes> submissionsBeforeEdit = submissionsLogic.getSubmissionsForCourse(student1InCourse1.course);

		// verify student details changed correctly
		studentsLogic.updateStudentCascade(originalEmail, student1InCourse1);
		LogicTest.verifyPresentInDatastore(student1InCourse1);

		// take a snapshot of submissions after the edit
		List<SubmissionAttributes> submissionsAfterEdit = submissionsLogic.getSubmissionsForCourse(student1InCourse1.course);
		
		// We moved a student from a 4-person team to an existing 1-person team.
		// We have 2 evaluations in the course.
		// Therefore, submissions that will be deleted = 7*2 = 14
		//              submissions that will be added = 3*2
		assertEquals(submissionsBeforeEdit.size() - 14  + 6,
				submissionsAfterEdit.size()); 
		
		// verify new submissions were created to match new team structure
		LogicTest.verifySubmissionsExistForCurrentTeamStructureInAllExistingEvaluations(submissionsAfterEdit,
				student1InCourse1.course);

		______TS("check for KeepExistingPolicy : change email only");
		
		// create an empty student and then copy course and email attributes
		StudentAttributes copyOfStudent1 = new StudentAttributes();
		copyOfStudent1.course = student1InCourse1.course;
		originalEmail = student1InCourse1.email;

		String newEmail = student1InCourse1.email + "y";
		student1InCourse1.email = newEmail;
		copyOfStudent1.email = newEmail;

		studentsLogic.updateStudentCascade(originalEmail, copyOfStudent1);
		LogicTest.verifyPresentInDatastore(student1InCourse1);

		______TS("check for KeepExistingPolicy : change nothing");	
		
		originalEmail = student1InCourse1.email;
		copyOfStudent1.email = null;
		studentsLogic.updateStudentCascade(originalEmail, copyOfStudent1);
		LogicTest.verifyPresentInDatastore(copyOfStudent1);
		
		______TS("non-existent student");
		
		try {
			studentsLogic.updateStudentCascade("non-existent@email", student1InCourse1);
			signalFailureToDetectException();
		} catch (EntityDoesNotExistException e) {
			assertEquals(StudentsDb.ERROR_UPDATE_NON_EXISTENT_STUDENT
					+ student1InCourse1.course + "/" + "non-existent@email",
					e.getMessage());
		}

		______TS("check for InvalidParameters");
		try {
			copyOfStudent1.email = "invalid email";
			studentsLogic.updateStudentCascade(originalEmail, copyOfStudent1);
			signalFailureToDetectException();
		} catch (InvalidParametersException e) {
			AssertHelper.assertContains(FieldValidator.REASON_INCORRECT_FORMAT,
					e.getMessage());
		}
	}
	
	@Test
	public void testKeyGeneration() {
		long key = 5;
		String longKey = KeyFactory.createKeyString(
				Student.class.getSimpleName(), key);
		long reverseKey = KeyFactory.stringToKey(longKey).getId();
		assertEquals(key, reverseKey);
		assertEquals("Student", KeyFactory.stringToKey(longKey).getKind());
	}
	
	@Test
	public void testEnrollLinesChecking() throws Exception {
		String info;
		String enrollLines;
		String courseId = "CourseID";
		coursesLogic.createCourse(courseId, "CourseName");
		
		List<String> invalidInfo;
		List<String> expectedInvalidInfo = new ArrayList<String>();
		
		______TS("enrollLines with invalid parameters");
		String invalidTeamName = StringHelper.generateStringOfLength(FieldValidator.TEAM_NAME_MAX_LENGTH + 1);
		String invalidStudentName = StringHelper.generateStringOfLength(FieldValidator.PERSON_NAME_MAX_LENGTH + 1);
		String invalidEmail = "invalid_email.com";
		
		String lineWithInvalidTeamName = invalidTeamName + "| John | john@email.com";
		String lineWithInvalidStudentName = "Team 1 |" + invalidStudentName + "| student@email.com";
		String lineWithInvalidEmail = "Team 1 | James |" + invalidEmail;
		String lineWithInvalidStudentNameAndEmail = "Team 2 |" + invalidStudentName + "|" + invalidEmail;
		String lineWithInvalidTeamNameAndEmail = invalidTeamName + "| Paul |" + invalidEmail;
		String lineWithInvalidTeamNameAndStudentNameAndEmail = invalidTeamName + "|" + invalidStudentName + "|" + invalidEmail;;
		
		enrollLines = lineWithInvalidTeamName + Const.EOL + lineWithInvalidStudentName + Const.EOL +
					lineWithInvalidEmail + Const.EOL + lineWithInvalidStudentNameAndEmail + Const.EOL +
					lineWithInvalidTeamNameAndEmail + Const.EOL + lineWithInvalidTeamNameAndStudentNameAndEmail;
		
		invalidInfo = invokeGetInvalidityInfoInEnrollLines(enrollLines, courseId);

		StudentAttributesFactory saf = new StudentAttributesFactory();
		expectedInvalidInfo.clear();
		info = StringHelper.toString(saf.makeStudent(lineWithInvalidTeamName, courseId).getInvalidityInfo(), "<br>" + Const.StatusMessages.ENROLL_LINES_PROBLEM_DETAIL_PREFIX + " ");
		expectedInvalidInfo.add(String.format(Const.StatusMessages.ENROLL_LINES_PROBLEM, lineWithInvalidTeamName, info));
		info = StringHelper.toString(saf.makeStudent(lineWithInvalidStudentName, courseId).getInvalidityInfo(), "<br>" + Const.StatusMessages.ENROLL_LINES_PROBLEM_DETAIL_PREFIX + " ");
		expectedInvalidInfo.add(String.format(Const.StatusMessages.ENROLL_LINES_PROBLEM, lineWithInvalidStudentName, info));
		info = StringHelper.toString(saf.makeStudent(lineWithInvalidEmail, courseId).getInvalidityInfo(), "<br>" + Const.StatusMessages.ENROLL_LINES_PROBLEM_DETAIL_PREFIX + " ");
		expectedInvalidInfo.add(String.format(Const.StatusMessages.ENROLL_LINES_PROBLEM, lineWithInvalidEmail, info));
		info = StringHelper.toString(saf.makeStudent(lineWithInvalidStudentNameAndEmail, courseId).getInvalidityInfo(), "<br>" + Const.StatusMessages.ENROLL_LINES_PROBLEM_DETAIL_PREFIX + " ");
		expectedInvalidInfo.add(String.format(Const.StatusMessages.ENROLL_LINES_PROBLEM, lineWithInvalidStudentNameAndEmail, info));
		info = StringHelper.toString(saf.makeStudent(lineWithInvalidTeamNameAndEmail, courseId).getInvalidityInfo(), "<br>" + Const.StatusMessages.ENROLL_LINES_PROBLEM_DETAIL_PREFIX + " ");
		expectedInvalidInfo.add(String.format(Const.StatusMessages.ENROLL_LINES_PROBLEM, lineWithInvalidTeamNameAndEmail, info));
		info = StringHelper.toString(saf.makeStudent(lineWithInvalidTeamNameAndStudentNameAndEmail, courseId).getInvalidityInfo(), "<br>" + Const.StatusMessages.ENROLL_LINES_PROBLEM_DETAIL_PREFIX + " ");
		expectedInvalidInfo.add(String.format(Const.StatusMessages.ENROLL_LINES_PROBLEM, lineWithInvalidTeamNameAndStudentNameAndEmail, info));
		
		for (int i = 0; i < invalidInfo.size(); i++) {
			assertEquals(expectedInvalidInfo.get(i), invalidInfo.get(i));
		}
		
		______TS("enrollLines with too few");
		String lineWithNoEmailInput = "Team 4 | StudentWithNoEmailInput";
		String lineWithExtraParameters = "Team 4 | StudentWithExtraParameters | student@email.com | comment | extra_parameter";
		
		enrollLines = lineWithNoEmailInput + Const.EOL + lineWithExtraParameters;
		
		invalidInfo = invokeGetInvalidityInfoInEnrollLines(enrollLines, courseId);

		expectedInvalidInfo.clear();
		expectedInvalidInfo.add(String.format(Const.StatusMessages.ENROLL_LINES_PROBLEM, lineWithNoEmailInput, StudentAttributesFactory.ERROR_ENROLL_LINE_TOOFEWPARTS));
		
		for (int i = 0; i < invalidInfo.size(); i++) {
			assertEquals(expectedInvalidInfo.get(i), invalidInfo.get(i));
		}
		
		______TS("enrollLines with some empty fields");
		String lineWithTeamNameEmpty = "    | StudentWithTeamFieldEmpty | student@email.com";
		String lineWithStudentNameEmpty = "Team 5 |  | no_name@email.com";
		String lineWithEmailEmpty = "Team 5 | StudentWithEmailFieldEmpty | |";
		
		enrollLines = lineWithTeamNameEmpty + Const.EOL + lineWithStudentNameEmpty + Const.EOL + lineWithEmailEmpty;

		invalidInfo = invokeGetInvalidityInfoInEnrollLines(enrollLines, courseId);
		expectedInvalidInfo.clear();
		info = StringHelper.toString(saf.makeStudent(lineWithTeamNameEmpty, courseId).getInvalidityInfo(), "<br>" + Const.StatusMessages.ENROLL_LINES_PROBLEM_DETAIL_PREFIX + " ");
		expectedInvalidInfo.add(String.format(Const.StatusMessages.ENROLL_LINES_PROBLEM, lineWithTeamNameEmpty, info));
		info = StringHelper.toString(saf.makeStudent(lineWithStudentNameEmpty, courseId).getInvalidityInfo(), "<br>" + Const.StatusMessages.ENROLL_LINES_PROBLEM_DETAIL_PREFIX + " ");
		expectedInvalidInfo.add(String.format(Const.StatusMessages.ENROLL_LINES_PROBLEM, lineWithStudentNameEmpty, info));
		info = StringHelper.toString(saf.makeStudent(lineWithEmailEmpty, courseId).getInvalidityInfo(), "<br>" + Const.StatusMessages.ENROLL_LINES_PROBLEM_DETAIL_PREFIX + " ");
		expectedInvalidInfo.add(String.format(Const.StatusMessages.ENROLL_LINES_PROBLEM, lineWithEmailEmpty, info));

		for (int i = 0; i < invalidInfo.size(); i++) {
			assertEquals(expectedInvalidInfo.get(i), invalidInfo.get(i));
		}

		______TS("enrollLines with correct input");
		String lineWithCorrectInput = "Team 3 | Mary | mary@email.com";
		String lineWithCorrectInputWithComment = "Team 4 | Benjamin | benjamin@email.com | Foreign student";
		
		enrollLines = lineWithCorrectInput + Const.EOL + lineWithCorrectInputWithComment;
		
		invalidInfo = invokeGetInvalidityInfoInEnrollLines(enrollLines, courseId);

		assertEquals(0, invalidInfo.size());
		
		______TS("enrollLines with only whitespaces");
		// not tested as enroll lines must be trimmed before passing to the method
		
		______TS("enrollLines with a mix of all above cases");
		enrollLines = lineWithInvalidTeamName + Const.EOL + lineWithInvalidTeamNameAndStudentNameAndEmail + Const.EOL + lineWithExtraParameters + Const.EOL +
				lineWithTeamNameEmpty + Const.EOL + lineWithCorrectInput + Const.EOL + "\t";

		invalidInfo = invokeGetInvalidityInfoInEnrollLines(enrollLines, courseId);
		
		expectedInvalidInfo.clear();
		info = StringHelper.toString(saf.makeStudent(lineWithInvalidTeamName, courseId).getInvalidityInfo(), "<br>" + Const.StatusMessages.ENROLL_LINES_PROBLEM_DETAIL_PREFIX + " ");
		expectedInvalidInfo.add(String.format(Const.StatusMessages.ENROLL_LINES_PROBLEM, lineWithInvalidTeamName, info));
		info = StringHelper.toString(saf.makeStudent(lineWithInvalidTeamNameAndStudentNameAndEmail, courseId).getInvalidityInfo(), "<br>" + Const.StatusMessages.ENROLL_LINES_PROBLEM_DETAIL_PREFIX + " ");
		expectedInvalidInfo.add(String.format(Const.StatusMessages.ENROLL_LINES_PROBLEM, lineWithInvalidTeamNameAndStudentNameAndEmail, info));
		info = StringHelper.toString(saf.makeStudent(lineWithTeamNameEmpty, courseId).getInvalidityInfo(), "<br>" + Const.StatusMessages.ENROLL_LINES_PROBLEM_DETAIL_PREFIX + " ");
		expectedInvalidInfo.add(String.format(Const.StatusMessages.ENROLL_LINES_PROBLEM, lineWithTeamNameEmpty, info));
		info = StringHelper.toString(saf.makeStudent(lineWithCorrectInput, courseId).getInvalidityInfo(), "<br>" + Const.StatusMessages.ENROLL_LINES_PROBLEM_DETAIL_PREFIX + " ");
		expectedInvalidInfo.add(String.format(Const.StatusMessages.ENROLL_LINES_PROBLEM, lineWithCorrectInput, info));
		
		for (int i = 0; i < invalidInfo.size(); i++) {
			assertEquals(expectedInvalidInfo.get(i), invalidInfo.get(i));
		}
		
	}

	private static StudentEnrollDetails invokeEnrollStudent(StudentAttributes student)
			throws Exception {
		Method privateMethod = StudentsLogic.class.getDeclaredMethod("enrollStudent",
				new Class[] { StudentAttributes.class });
		privateMethod.setAccessible(true);
		Object[] params = new Object[] { student };
		return (StudentEnrollDetails) privateMethod.invoke(StudentsLogic.inst(), params);
	}
	
	@SuppressWarnings("unchecked")
	private static List<String> invokeGetInvalidityInfoInEnrollLines(String lines, String courseID)
			throws Exception {
		Method privateMethod = StudentsLogic.class.getDeclaredMethod("getInvalidityInfoInEnrollLines",
									new Class[] { String.class, String.class });
		privateMethod.setAccessible(true);
		Object[] params = new Object[] { lines, courseID };
		return (List<String>) privateMethod.invoke(StudentsLogic.inst(), params);
	}
		
	private void verifyCascasedToSubmissions(String instructorCourse)
			throws EntityDoesNotExistException {
		LogicTest.verifySubmissionsExistForCurrentTeamStructureInAllExistingEvaluations(
				submissionsLogic.getSubmissionsForCourse(instructorCourse),
				instructorCourse);
	}
	
	private List<FeedbackResponseAttributes> getAllTeamResponsesForStudent(StudentAttributes student) {
		List<FeedbackResponseAttributes> returnList = new ArrayList<FeedbackResponseAttributes>();
		
		List<FeedbackResponseAttributes> studentReceiverResponses = FeedbackResponsesLogic.inst()
				.getFeedbackResponsesForReceiverForCourse(student.course, student.email);
		
		for (FeedbackResponseAttributes response : studentReceiverResponses) {
			FeedbackQuestionAttributes question = FeedbackQuestionsLogic.inst()
					.getFeedbackQuestion(response.feedbackQuestionId);
			if (question.recipientType == FeedbackParticipantType.OWN_TEAM_MEMBERS) {
				returnList.add(response);
			}
		}
		
		List<FeedbackResponseAttributes> studentGiverResponses = FeedbackResponsesLogic.inst()
				.getFeedbackResponsesFromGiverForCourse(student.course, student.email);
		
		for (FeedbackResponseAttributes response : studentGiverResponses) {
			FeedbackQuestionAttributes question = FeedbackQuestionsLogic.inst()
					.getFeedbackQuestion(response.feedbackQuestionId);
			if (question.giverType == FeedbackParticipantType.TEAMS || 
				question.recipientType == FeedbackParticipantType.OWN_TEAM_MEMBERS) {
				returnList.add(response);
			}
		}
		
		return returnList;
	}
	
	private void verifySubmissionsDoNotExistForEmailInCourse(String email,
			String courseId) {
		List<SubmissionAttributes> allSubmissions = submissionsLogic.getSubmissionsForCourse(courseId);
		
		for (SubmissionAttributes currentSubmission : allSubmissions) {
			if (currentSubmission.reviewee.equals(email) ||
				currentSubmission.reviewer.equals(email)) {
				fail("Cause : Submission for " + email +
						" found on system");
			}
		}
	}
	@AfterClass()
	public static void classTearDown() throws Exception {
		printTestClassFooter();
		turnLoggingDown(StudentsLogic.class);
	}

}
