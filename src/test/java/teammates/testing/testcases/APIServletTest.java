package teammates.testing.testcases;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import javax.servlet.ServletException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import teammates.APIServlet;
import teammates.Common;
import teammates.DataBundle;
import teammates.Datastore;
import teammates.exception.EntityDoesNotExistException;
import teammates.jdo.Coordinator;
import teammates.jdo.Course;
import teammates.jdo.Evaluation;
import teammates.jdo.EvaluationDetailsForCoordinator;
import teammates.jdo.Student;
import teammates.jdo.Submission;
import teammates.jdo.TeamFormingLog;
import teammates.jdo.TeamFormingSession;
import teammates.jdo.TeamProfile;
import teammates.testing.lib.SharedLib;

import com.google.appengine.api.datastore.Text;
import com.google.appengine.tools.development.testing.*;
import com.google.gson.Gson;

public class APIServletTest extends BaseTestCase {

	private static LocalServiceTestHelper helper;
	private final static APIServlet apiServlet = new APIServlet();
	private static String TEST_DATA_FOLDER = "src/test/resources/data/";
	private static Gson gson = Common.getTeammatesGson();
	String jsonString = SharedLib.getFileContents(TEST_DATA_FOLDER
			+ "typicalDataBundle.json");
	private DataBundle dataBundle;

	private static String queueXmlFilePath = System.getProperty("user.dir")
			+ File.separator + "src" + File.separator + "main" + File.separator
			+ "webapp" + File.separator + "WEB-INF" + File.separator
			+ "queue.xml";

	@BeforeClass
	public static void setUp() throws ServletException, IOException {
		printTestClassHeader(getNameOfThisClass());
		/*
		 * We have to explicitly set the path of queue.xml because the test 
		 * environment cannot find it. Apparently, this is a bug in the test
		 * environment (as mentioned in http://turbomanage.wordpress.com/2010/03/03/a-recipe-for-unit-testing-appengine-task-queues/
		 * The bug might get fixed in future SDKs.
		 */
		LocalTaskQueueTestConfig ltqtc = new LocalTaskQueueTestConfig();
		ltqtc.setQueueXmlPath(queueXmlFilePath);
		
		helper = new LocalServiceTestHelper(
				new LocalDatastoreServiceTestConfig(),
				new LocalMailServiceTestConfig(), ltqtc);

		/**
		 * LocalServiceTestHelper is supposed to run in the same timezone as Dev
		 * server and production server i.e. (i.e. UTC timezone), as stated in
		 * https
		 * ://developers.google.com/appengine/docs/java/tools/localunittesting
		 * /javadoc/com/google/appengine/tools/development/testing/
		 * LocalServiceTestHelper#setTimeZone%28java.util.TimeZone%29
		 * 
		 * But it seems Dev server does not run on UTC timezone, but it runs on
		 * "GMT+8:00" (Possibly, a bug). Therefore, I'm changing timeZone of
		 * LocalServiceTestHelper to match the Dev server. But note that tests
		 * that run on Dev server might fail on Production server due to this
		 * problem. We need to find a fix.
		 */
		helper.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
		helper.setUp();
		apiServlet.init();
		Datastore.initialize();

	}

	@Test
	public void testPersistDataBundle() throws Exception {
		dataBundle = gson.fromJson(jsonString, DataBundle.class);
		HashMap<String, Coordinator> coords = dataBundle.coords;
		for (Coordinator coord : coords.values()) {
			apiServlet.deleteCoord(coord.getGoogleID());
		}
		apiServlet.persistNewDataBundle(jsonString);
		verifyPresentInDatastore(jsonString);
	}

	@Test
	public void testDeleteStudent() throws Exception {
		printTestCaseHeader(getNameOfThisMethod());
		refreshDataInDatastore();

		Submission submissionFromS1C1ToS2C1 = dataBundle.submissions
				.get("submissionFromS1C1ToS2C1");
		verifyPresentInDatastore(submissionFromS1C1ToS2C1);
		Submission submissionFromS2C1ToS1C1 = dataBundle.submissions
				.get("submissionFromS2C1ToS1C1");
		verifyPresentInDatastore(submissionFromS2C1ToS1C1);
		Submission submissionFromS1C1ToS1C1 = dataBundle.submissions
				.get("submissionFromS1C1ToS1C1");
		verifyPresentInDatastore(submissionFromS1C1ToS1C1);

		Student student2InCourse1 = dataBundle.students
				.get("student2InCourse1");
		verifyPresentInDatastore(student2InCourse1);

		// verify that the student-to-be-deleted has some log entries
		verifyPresenceOfTfsLogsForStudent(student2InCourse1.getCourseID(),
				student2InCourse1.getEmail());

		apiServlet.deleteStudent(student2InCourse1.getCourseID(),
				student2InCourse1.getEmail());
		verifyAbsentInDatastore(student2InCourse1);

		// verify that other students in the course are intact
		Student student1InCourse1 = dataBundle.students
				.get("student1InCourse1");
		verifyPresentInDatastore(student1InCourse1);

		// try to delete the student again. should succeed.
		apiServlet.deleteStudent(student2InCourse1.getCourseID(),
				student2InCourse1.getEmail());

		verifyAbsentInDatastore(submissionFromS1C1ToS2C1);
		verifyAbsentInDatastore(submissionFromS2C1ToS1C1);
		verifyPresentInDatastore(submissionFromS1C1ToS1C1);

		// verify that log entries belonging to the student was deleted
		verifyAbsenceOfTfsLogsForStudent(student2InCourse1.getCourseID(),
				student2InCourse1.getEmail());
		// verify that log entries belonging to another student remain intact
		verifyPresenceOfTfsLogsForStudent(student1InCourse1.getCourseID(),
				student1InCourse1.getEmail());

		// TODO: test for cascade delete of profiles
	}

	@Test
	public void testEditStudent() throws Exception {
		printTestCaseHeader(getNameOfThisMethod());
		refreshDataInDatastore();

		Student student1InCourse1 = dataBundle.students
				.get("student1InCourse1");
		verifyPresentInDatastore(student1InCourse1);
		String originalEmail = student1InCourse1.getEmail();
		student1InCourse1.setName(student1InCourse1.getName() + "x");
		student1InCourse1.setID(student1InCourse1.getID() + "x");
		student1InCourse1.setComments(student1InCourse1.getComments() + "x");
		student1InCourse1.setEmail(student1InCourse1.getEmail() + "x");
		apiServlet.editStudent(originalEmail, student1InCourse1);
		verifyPresentInDatastore(student1InCourse1);
	}

	@Test
	public void testGetEvalListForCoord() throws Exception {
		printTestCaseHeader(getNameOfThisMethod());
		refreshDataInDatastore();
		Coordinator coord1 = dataBundle.coords.get("typicalCoord1");
		ArrayList<EvaluationDetailsForCoordinator> evalList = apiServlet
				.getEvaluationsListForCoord(coord1.getGoogleID());
		assertEquals(3, evalList.size());
		for (EvaluationDetailsForCoordinator ed : evalList) {
			assertTrue(ed.getCourseID().contains("Coord1"));
		}
		Coordinator coord2 = dataBundle.coords.get("typicalCoord2");
		evalList = apiServlet.getEvaluationsListForCoord(coord2.getGoogleID());
		assertEquals(1, evalList.size());
		for (EvaluationDetailsForCoordinator ed : evalList) {
			assertTrue(ed.getCourseID().contains("Coord2"));
		}
		Coordinator coord3 = dataBundle.coords.get("typicalCoord3");
		evalList = apiServlet.getEvaluationsListForCoord(coord3.getGoogleID());
		assertEquals(0, evalList.size());

		evalList = apiServlet.getEvaluationsListForCoord("nonExistentCoord");
		assertEquals(0, evalList.size());
		// TODO: needs more testing
	}

	@Test
	public void testGetStudentListForCourse() throws Exception {
		printTestCaseHeader(getNameOfThisMethod());
		refreshDataInDatastore();

		Course course1OfCoord1 = dataBundle.courses.get("course1OfCoord1");
		List<Student> studentList = apiServlet
				.getStudentListForCourse(course1OfCoord1.getID());
		assertEquals(3, studentList.size());
		for (Student s : studentList) {
			assertEquals(course1OfCoord1.getID(), s.getCourseID());
		}

		Course course2OfCoord1 = dataBundle.courses.get("course2OfCoord1");
		studentList = apiServlet.getStudentListForCourse(course2OfCoord1
				.getID());
		assertEquals(0, studentList.size());
		for (Student s : studentList) {
			assertEquals(course2OfCoord1.getID(), s.getCourseID());
		}

		// TODO: more testing
	}

	@Test
	public void testEditEvaluation() throws Exception {
		printTestCaseHeader(getNameOfThisMethod());
		refreshDataInDatastore();

		Evaluation eval1 = dataBundle.evaluations
				.get("evaluation1InCourse1OfCoord1");
		eval1.setGracePeriod(eval1.getGracePeriod() + 1);
		eval1.setInstructions(eval1.getInstructions() + "x");
		eval1.setCommentsEnabled(!eval1.isCommentsEnabled());
		eval1.setStart(getDateOffsetToCurrentTime(1));
		eval1.setDeadline(getDateOffsetToCurrentTime(2));
		apiServlet.editEvaluation(eval1);
		verifyPresentInDatastore(eval1);

		// TODO: more testing

	}

	@Test
	public void testPublishAndUnpublishEvaluation() throws Exception {


		printTestCaseHeader(getNameOfThisMethod());
		refreshDataInDatastore();
		Evaluation eval1 = dataBundle.evaluations
				.get("evaluation1InCourse1OfCoord1");
		assertEquals(false,
				apiServlet.getEvaluation(eval1.getCourseID(), eval1.getName())
						.isPublished());
		apiServlet.publishEvaluation(eval1.getCourseID(), eval1.getName());
		assertEquals(true,
				apiServlet.getEvaluation(eval1.getCourseID(), eval1.getName())
						.isPublished());
		apiServlet.unpublishEvaluation(eval1.getCourseID(), eval1.getName());
		assertEquals(false, apiServlet.getEvaluation(eval1.getCourseID(), eval1.getName()).isPublished());
		//TODO: more testing
	}


	@Test
	public void testEditSubmission() throws Exception {
		printTestCaseHeader(getNameOfThisMethod());
		refreshDataInDatastore();

		ArrayList<Submission> submissionContainer = new ArrayList<Submission>();

		// try without empty list. Nothing should happen
		apiServlet.editSubmission(submissionContainer);

		Submission sub1 = dataBundle.submissions
				.get("submissionFromS1C1ToS2C1");

		Submission sub2 = dataBundle.submissions
				.get("submissionFromS2C1ToS1C1");

		// checking editing of one of the submissions
		alterSubmission(sub1);

		submissionContainer.add(sub1);
		apiServlet.editSubmission(submissionContainer);

		verifyPresentInDatastore(sub1);
		verifyPresentInDatastore(sub2);

		// check editing both submissions
		alterSubmission(sub1);
		alterSubmission(sub2);

		submissionContainer = new ArrayList<Submission>();
		submissionContainer.add(sub1);
		submissionContainer.add(sub2);
		apiServlet.editSubmission(submissionContainer);

		verifyPresentInDatastore(sub1);
		verifyPresentInDatastore(sub2);

		// TODO: more testing

	}

	@Test
	public void testEditTfs() throws Exception {
		printTestCaseHeader(getNameOfThisMethod());
		refreshDataInDatastore();

		TeamFormingSession tfs1 = dataBundle.teamFormingSessions
				.get("tfsInCourse1");
		tfs1.setGracePeriod(tfs1.getGracePeriod() + 1);
		tfs1.setInstructions(tfs1.getInstructions() + "x");
		tfs1.setProfileTemplate(tfs1.getProfileTemplate() + "y");
		tfs1.setStart(getDateOffsetToCurrentTime(1));
		tfs1.setDeadline(getDateOffsetToCurrentTime(2));
		apiServlet.getTfs(tfs1);
		verifyPresentInDatastore(tfs1);

		// TODO: more testing
	}

	@Test
	public void testEditTeamProfile() throws Exception {
		printTestCaseHeader(getNameOfThisMethod());
		refreshDataInDatastore();

		TeamProfile teamProfile1 = dataBundle.teamProfiles
				.get("profileOfTeam1.1");
		String originalTeamName = teamProfile1.getTeamName();
		teamProfile1.setTeamName(teamProfile1.getTeamName() + "new");
		teamProfile1.setTeamProfile(new Text(teamProfile1.getTeamProfile()
				.getValue() + "x"));
		apiServlet.editTeamProfile(originalTeamName, teamProfile1);
		verifyPresentInDatastore(teamProfile1);

	}

	@Test
	public void testSendRegistrationInviteForCourse() throws Exception {
		printTestCaseHeader(getNameOfThisMethod());
		refreshDataInDatastore();
		// TODO: to be implemented
	}

	@Test
	public void testSendRegistrationInviteToStudent() throws Exception {
		printTestCaseHeader(getNameOfThisMethod());
		refreshDataInDatastore();
		// TODO: to be implemented
	}

	@Test
	public void testEnrolStudents() throws Exception {
		printTestCaseHeader(getNameOfThisMethod());
		refreshDataInDatastore();
		// TODO: to be implemented
	}

	@Test
	public void testGetEvaluationResult() throws Exception {
		printTestCaseHeader(getNameOfThisMethod());
		refreshDataInDatastore();
		// TODO: to be implemented
	}

	@Test
	public void testTfsListForCoord() throws Exception {
		printTestCaseHeader(getNameOfThisMethod());
		refreshDataInDatastore();
		// TODO: to be implemented
	}

	@Test
	public void testRenameTeam() throws Exception {
		printTestCaseHeader(getNameOfThisMethod());
		refreshDataInDatastore();
		// TODO: to be implemented
	}

	// ------------------------------------------------------------------------

	private void alterSubmission(Submission submission) {
		submission.setPoints(submission.getPoints() + 10);
		submission.setCommentsToStudent(new Text(submission
				.getCommentsToStudent().getValue() + "x"));
		submission.setJustification(new Text(submission.getJustification()
				.getValue() + "y"));
	}

	private Date getDateOffsetToCurrentTime(int offsetDays) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(cal.getTime());
		cal.add(Calendar.DATE, +offsetDays);
		return cal.getTime();
	}

	private void verifyPresentInDatastore(String dataBundleJsonString)
			throws EntityDoesNotExistException {

		DataBundle data = gson.fromJson(dataBundleJsonString, DataBundle.class);
		HashMap<String, Coordinator> coords = data.coords;
		for (Coordinator expectedCoord : coords.values()) {
			verifyPresentInDatastore(expectedCoord);
		}

		HashMap<String, Course> courses = data.courses;
		for (Course expectedCourse : courses.values()) {
			verifyPresentInDatastore(expectedCourse);
		}

		HashMap<String, Student> students = data.students;
		for (Student expectedStudent : students.values()) {
			verifyPresentInDatastore(expectedStudent);
		}

		HashMap<String, Evaluation> evaluations = data.evaluations;
		for (Evaluation expectedEvaluation : evaluations.values()) {
			verifyPresentInDatastore(expectedEvaluation);
		}

		HashMap<String, Submission> submissions = data.submissions;
		for (Submission expectedSubmission : submissions.values()) {
			verifyPresentInDatastore(expectedSubmission);
		}

		HashMap<String, TeamFormingSession> teamFormingSessions = data.teamFormingSessions;
		for (TeamFormingSession expectedTeamFormingSession : teamFormingSessions
				.values()) {
			verifyPresentInDatastore(expectedTeamFormingSession);
		}

		HashMap<String, TeamProfile> teamProfiles = data.teamProfiles;
		for (TeamProfile expectedTeamProfile : teamProfiles.values()) {
			verifyPresentInDatastore(expectedTeamProfile);
		}

		HashMap<String, TeamFormingLog> teamFormingLogs = data.teamFormingLogs;
		for (TeamFormingLog expectedTeamFormingLogEntry : teamFormingLogs
				.values()) {
			verifyPresentInDatastore(expectedTeamFormingLogEntry);
		}

	}

	private void verifyAbsentInDatastore(Submission submission) {
		assertEquals(
				null,
				apiServlet.getSubmission(submission.getCourseID(),
						submission.getEvaluationName(),
						submission.getFromStudent(), submission.getToStudent()));
	}

	private void verifyAbsentInDatastore(Student student) {
		assertEquals(null, apiServlet.getStudent(student.getCourseID(),
				student.getEmail()));
	}

	private void verifyAbsenceOfTfsLogsForStudent(String courseId,
			String studentEmail) {
		List<TeamFormingLog> teamFormingLogs = apiServlet
				.getTeamFormingLog(courseId);
		for (TeamFormingLog tfl : teamFormingLogs) {
			String actualEmail = tfl.getStudentEmail();
			assertTrue("unexpected email:" + actualEmail,
					!actualEmail.equals(studentEmail));
		}

	}

	private void verifyPresenceOfTfsLogsForStudent(String courseId,
			String studentEmail) {
		List<TeamFormingLog> teamFormingLogs = apiServlet
				.getTeamFormingLog(courseId);
		for (TeamFormingLog tfl : teamFormingLogs) {
			if (tfl.getStudentEmail().equals(studentEmail))
				return;
		}
		Assert.fail("No log messages found for " + studentEmail + " in "
				+ courseId);
	}

	private void verifyPresentInDatastore(Student expectedStudent) {
		Student actualStudent = apiServlet.getStudent(
				expectedStudent.getCourseID(), expectedStudent.getEmail());
		assertEquals(gson.toJson(expectedStudent), gson.toJson(actualStudent));
	}

	private void verifyPresentInDatastore(Submission expected) {
		Submission actual = apiServlet.getSubmission(expected.getCourseID(),
				expected.getEvaluationName(), expected.getFromStudent(),
				expected.getToStudent());
		expected.id = actual.id;
		assertEquals(gson.toJson(expected), gson.toJson(actual));
	}

	private void verifyPresentInDatastore(TeamFormingLog expected) {
		List<TeamFormingLog> actualList = apiServlet.getTeamFormingLog(expected
				.getCourseID());
		assertTrue(isLogEntryInList(expected, actualList));
	}

	private void verifyPresentInDatastore(TeamProfile expected) {
		TeamProfile actual = apiServlet.getTeamProfile(expected.getCourseID(),
				expected.getTeamName());
		expected.id = actual.id;
		assertEquals(gson.toJson(expected), gson.toJson(actual));
	}

	private void verifyPresentInDatastore(TeamFormingSession expected) {
		TeamFormingSession actual = apiServlet.getTfs(expected.getCourseID());
		expected.id = actual.id;
		assertEquals(gson.toJson(expected), gson.toJson(actual));
	}

	private void verifyPresentInDatastore(Evaluation expected) {
		Evaluation actual = apiServlet.getEvaluation(expected.getCourseID(),
				expected.getName());
		expected.id = actual.id;
		assertEquals(gson.toJson(expected), gson.toJson(actual));
	}

	private void verifyPresentInDatastore(Course expected)
			throws EntityDoesNotExistException {
		Course actual = apiServlet.getCourse(expected.getID());
		assertEquals(gson.toJson(expected), gson.toJson(actual));
	}

	private void verifyPresentInDatastore(Coordinator expected) {
		Coordinator actual = apiServlet.getCoord(expected.getGoogleID());
		assertEquals(gson.toJson(expected), gson.toJson(actual));
	}

	private void refreshDataInDatastore() throws Exception {
		dataBundle = gson.fromJson(jsonString, DataBundle.class);
		HashMap<String, Coordinator> coords = dataBundle.coords;
		for (Coordinator coord : coords.values()) {
			apiServlet.deleteCoord(coord.getGoogleID());
		}
		apiServlet.persistNewDataBundle(jsonString);
	}

	private boolean isLogEntryInList(TeamFormingLog teamFormingLogEntry,
			List<TeamFormingLog> teamFormingLogEntryList) {
		for (TeamFormingLog logEntryInList : teamFormingLogEntryList) {
			if (teamFormingLogEntry.getCourseID().equals(
					logEntryInList.getCourseID())
					&& teamFormingLogEntry.getMessage().getValue()
							.equals(logEntryInList.getMessage().getValue())
					&& teamFormingLogEntry.getStudentEmail().equals(
							logEntryInList.getStudentEmail())
					&& teamFormingLogEntry.getStudentName().equals(
							logEntryInList.getStudentName())
					&& teamFormingLogEntry.getTime().toString()
							.equals(logEntryInList.getTime().toString())) {
				return true;
			}
		}
		return false;
	}

	@AfterClass
	public static void tearDown() {
		apiServlet.destroy();
		helper.tearDown();
		printTestClassFooter("CoordCourseAddApiTest");
	}

}
