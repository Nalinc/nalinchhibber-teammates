package teammates.testing.testcases;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import javax.servlet.ServletException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import teammates.APIServlet;
import teammates.Common;
import teammates.DataBundle;
import teammates.Datastore;
import teammates.exception.EnrollException;
import teammates.exception.EntityDoesNotExistException;
import teammates.jdo.Coordinator;
import teammates.jdo.Course;
import teammates.jdo.CourseSummaryForCoordinator;
import teammates.jdo.Evaluation;
import teammates.jdo.EvaluationDetailsForCoordinator;
import teammates.jdo.Student;
import teammates.jdo.StudentInfoForCoord;
import teammates.jdo.Submission;
import teammates.jdo.TeamFormingLog;
import teammates.jdo.TeamFormingSession;
import teammates.jdo.TeamProfile;
import teammates.testing.lib.SharedLib;

import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.taskqueue.dev.LocalTaskQueue;
import com.google.appengine.api.taskqueue.dev.QueueStateInfo;
import com.google.appengine.api.taskqueue.dev.QueueStateInfo.TaskStateInfo;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalMailServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig;
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
	public static void setUpDatastore() {
		assertTrue(true);
		Datastore.initialize();
	}

	@Before
	public void setUp() throws ServletException, IOException {
		printTestClassHeader();
		/*
		 * We have to explicitly set the path of queue.xml because the test
		 * environment cannot find it. Apparently, this is a bug in the test
		 * environment (as mentioned in
		 * http://turbomanage.wordpress.com/2010/03/
		 * 03/a-recipe-for-unit-testing-appengine-task-queues/ The bug might get
		 * fixed in future SDKs.
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
		printTestCaseHeader();
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
		printTestCaseHeader();
		refreshDataInDatastore();

		Student student1InCourse1 = dataBundle.students
				.get("student1InCourse1");
		verifyPresentInDatastore(student1InCourse1);
		String originalEmail = student1InCourse1.getEmail();
		student1InCourse1.setName(student1InCourse1.getName() + "x");
		student1InCourse1.setID(student1InCourse1.getID() + "x");
		student1InCourse1.setComments(student1InCourse1.getComments() + "x");
		student1InCourse1.setEmail(student1InCourse1.getEmail() + "x");
		student1InCourse1.setTeamName(student1InCourse1.getTeamName() + "x");
		//TODO: make sure team profiles are deleted if this is the last student in that team
		student1InCourse1.setProfileDetail(new Text("new profile detail abc "));
		apiServlet.editStudent(originalEmail, student1InCourse1);
		verifyPresentInDatastore(student1InCourse1);
		//TODO: more testing
	}

	@Test
	public void testGetEvalListForCoord() throws Exception {
		printTestCaseHeader();
		refreshDataInDatastore();
		Coordinator coord1 = dataBundle.coords.get("typicalCoord1");
		ArrayList<EvaluationDetailsForCoordinator> evalList = apiServlet
				.getEvaluationsListForCoord(coord1.getGoogleID());
		assertEquals(3, evalList.size());
		for (EvaluationDetailsForCoordinator ed : evalList) {
			assertTrue(ed.courseID.contains("Coord1"));
		}
		Coordinator coord2 = dataBundle.coords.get("typicalCoord2");
		evalList = apiServlet.getEvaluationsListForCoord(coord2.getGoogleID());
		assertEquals(1, evalList.size());
		for (EvaluationDetailsForCoordinator ed : evalList) {
			assertTrue(ed.courseID.contains("Coord2"));
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
		printTestCaseHeader();
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
		printTestCaseHeader();
		refreshDataInDatastore();

		Evaluation eval1 = dataBundle.evaluations
				.get("evaluation1InCourse1OfCoord1");
		eval1.setGracePeriod(eval1.getGracePeriod() + 1);
		eval1.setInstructions(eval1.getInstructions() + "x");
		eval1.setCommentsEnabled(!eval1.isCommentsEnabled());
		eval1.setStart(Common.getDateOffsetToCurrentTime(1));
		eval1.setDeadline(Common.getDateOffsetToCurrentTime(2));
		apiServlet.editEvaluation(eval1);
		verifyPresentInDatastore(eval1);

		// TODO: more testing

	}

	@Test
	public void testPublishAndUnpublishEvaluation() throws Exception {

		printTestCaseHeader();
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
		assertEquals(false,
				apiServlet.getEvaluation(eval1.getCourseID(), eval1.getName())
						.isPublished());
		// TODO: more testing
	}

	@Test
	public void testEditSubmission() throws Exception {
		printTestCaseHeader();
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
		printTestCaseHeader();
		refreshDataInDatastore();

		TeamFormingSession tfs1 = dataBundle.teamFormingSessions
				.get("tfsInCourse1");
		tfs1.setGracePeriod(tfs1.getGracePeriod() + 1);
		tfs1.setInstructions(tfs1.getInstructions() + "x");
		tfs1.setProfileTemplate(tfs1.getProfileTemplate() + "y");
		tfs1.setStart(Common.getDateOffsetToCurrentTime(1));
		tfs1.setDeadline(Common.getDateOffsetToCurrentTime(2));
		apiServlet.getTfs(tfs1);
		verifyPresentInDatastore(tfs1);

		// TODO: more testing
	}

	@Test
	public void testEditTeamProfile() throws Exception {
		printTestCaseHeader();
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
		printTestCaseHeader();
		refreshDataInDatastore();
		Course course1 = dataBundle.courses.get("course1OfCoord1");

		// send registration key to a class in which all are registered
		apiServlet.sendRegistrationInviteForCourse(course1.getID());
		assertEquals(0, getNumberOfEmailTasksInQueue());

		// modify two students to make them 'unregistered' and send again
		Student student1InCourse1 = dataBundle.students
				.get("student1InCourse1");
		student1InCourse1.setID("");
		apiServlet.editStudent(student1InCourse1.getEmail(), student1InCourse1);
		Student student2InCourse1 = dataBundle.students
				.get("student2InCourse1");
		student2InCourse1.setID("");
		apiServlet.editStudent(student2InCourse1.getEmail(), student2InCourse1);
		apiServlet.sendRegistrationInviteForCourse(course1.getID());
		assertEquals(2, getNumberOfEmailTasksInQueue());
		verifyRegistrationEmailToStudent(student1InCourse1);
		verifyRegistrationEmailToStudent(student2InCourse1);

		// TODO: more testing

	}

	@Test
	public void testSendRegistrationInviteToStudent() throws Exception {
		printTestCaseHeader();
		refreshDataInDatastore();
		Student student1 = dataBundle.students.get("student1InCourse1");
		apiServlet.sendRegistrationInviteToStudent(student1.getCourseID(),
				student1.getEmail());

		assertEquals(1, getNumberOfEmailTasksInQueue());
		verifyRegistrationEmailToStudent(student1);
		// TODO: more testing
	}
	

	@Test
	public void testEnrollStudent() throws Exception {
		printTestCaseHeader();
		refreshDataInDatastore();

		String coordId = "coordForEnrollTesting";
		apiServlet.deleteCoord(coordId);
		apiServlet.createCoord(coordId, "Coord for Enroll Testing",
				"coordForEnrollTestin@gmail.com");
		String courseId = "courseForEnrollTest";
		apiServlet.createCourse(coordId, courseId, "Course for Enroll Testing");

		Student student1 = new Student("t|n|e@g|c", courseId);

		// check if the course is empty
		assertEquals(0, apiServlet.getStudentListForCourse(courseId).size());

		// add a new student and verify it is added and treated as a new student
		StudentInfoForCoord enrollmentResult = apiServlet
				.enrollStudent(student1);
		assertEquals(1, apiServlet.getStudentListForCourse(courseId).size());
		verifyEnrollmentResultForStudent(student1, enrollmentResult,
				StudentInfoForCoord.UpdateStatus.NEW);

		// add the same student. Verify it was not added
		enrollmentResult = apiServlet.enrollStudent(student1);
		verifyEnrollmentResultForStudent(student1, enrollmentResult,
				StudentInfoForCoord.UpdateStatus.UNMODIFIED);

		// modify info of same student and verify it was treated as modified
		Student student2 = new Student("t|n2|e@g|c", courseId);
		enrollmentResult = apiServlet.enrollStudent(student2);
		verifyEnrollmentResultForStudent(student2, enrollmentResult,
				StudentInfoForCoord.UpdateStatus.MODIFIED);

		// add a new student to non-empty course
		Student student3 = new Student("t3|n3|e3@g|c3", courseId);
		enrollmentResult = apiServlet.enrollStudent(student3);
		assertEquals(2, apiServlet.getStudentListForCourse(courseId).size());
		verifyEnrollmentResultForStudent(student3, enrollmentResult,
				StudentInfoForCoord.UpdateStatus.NEW);
	}

	@Test
	public void testEnrollStudents() throws Exception{
		printTestCaseHeader();
		refreshDataInDatastore();
		
		String coordId = "coordForEnrollTesting";
		apiServlet.createCoord(coordId, "Coord for Enroll Testing",
				"coordForEnrollTestin@gmail.com");
		String courseId = "courseForEnrollTest";
		apiServlet.createCourse(coordId, courseId, "Course for Enroll Testing");
		String EOL = Common.EOL;
		
		// all valid students, but contains blank lines
		String line0 = "t1|n1|e1@g|c1";
		String line1 = " t2|  n2|  e2@g|  c2";
		String line2 = "t3|n3|e3@g|c3  ";
		String line3 = "t4|n4|  e4@g|c4";
		String line4 = "t5|n5|e5@g  |c5";
		String lines = line0+ EOL +
				line1+ EOL +
				line2+ EOL +
				"  \t \t \t \t           "+ EOL +
				line3+ EOL +
				EOL +
				line4+ EOL +
				"    "+ EOL +
				EOL;
		List<StudentInfoForCoord> enrollResults = apiServlet.enrollStudents(lines, courseId);
		
		assertEquals(5, enrollResults.size());
		assertEquals(5, apiServlet.getStudentListForCourse(courseId).size());
		verifyEnrollmentResultForStudent(new Student(line0, courseId),
				enrollResults.get(0), StudentInfoForCoord.UpdateStatus.NEW);
		verifyEnrollmentResultForStudent(new Student(line1, courseId),
				enrollResults.get(1), StudentInfoForCoord.UpdateStatus.NEW);
		verifyEnrollmentResultForStudent(new Student(line4, courseId),
				enrollResults.get(4), StudentInfoForCoord.UpdateStatus.NEW);
		
		//includes a mix of unmodified, modified, and new
		String line0_1 = "t3|modified name|e3@g|c3";
		String line5 = "t6|n6|e6@g|c6";
		lines = line0+EOL+line0_1+ EOL +line1+ EOL + line5;
		enrollResults = apiServlet.enrollStudents(lines, courseId);
		assertEquals(6, enrollResults.size());
		assertEquals(6, apiServlet.getStudentListForCourse(courseId).size());
		verifyEnrollmentResultForStudent(new Student(line0, courseId),
				enrollResults.get(0), StudentInfoForCoord.UpdateStatus.UNMODIFIED);
		verifyEnrollmentResultForStudent(new Student(line0_1, courseId),
				enrollResults.get(1), StudentInfoForCoord.UpdateStatus.MODIFIED);
		verifyEnrollmentResultForStudent(new Student(line1, courseId),
				enrollResults.get(2), StudentInfoForCoord.UpdateStatus.UNMODIFIED);
		verifyEnrollmentResultForStudent(new Student(line5, courseId),
				enrollResults.get(3), StudentInfoForCoord.UpdateStatus.NEW);
		assertEquals(StudentInfoForCoord.UpdateStatus.NOT_IN_ENROLL_LIST, enrollResults.get(4).updateStatus);
		assertEquals(StudentInfoForCoord.UpdateStatus.NOT_IN_ENROLL_LIST, enrollResults.get(5).updateStatus);
		
		//includes an incorrect line, no changes should be done to the database
		String incorrectLine = "incorrectly formatted line";
		lines = "t7|n7|e7@g|c7"+EOL+incorrectLine+ EOL +line2+ EOL + line3;
		try {
			enrollResults = apiServlet.enrollStudents(lines, courseId);
			Assert.fail("Did not throw exception for incorrectly formatted line");
		} catch (EnrollException e) {
			assertTrue(e.getMessage().contains(incorrectLine));
		}
		assertEquals(6, apiServlet.getStudentListForCourse(courseId).size());
		
	}
	
	@Test
	public void testGetCourseDetailsListForCoord() throws Exception{
		printTestCaseHeader();
		refreshDataInDatastore();
		
		HashMap<String,CourseSummaryForCoordinator> courseListForCoord = apiServlet.getCourseDetailsListForCoord("idOfTypicalCoord1");
		assertEquals(2, courseListForCoord.size());
		String course1Id = "idOfCourse1OfCoord1";

		//course with 2 evaluations
		ArrayList<EvaluationDetailsForCoordinator> course1Evals = courseListForCoord.get(course1Id).evaluations;
		assertEquals(2, course1Evals.size());
		assertEquals(course1Id, course1Evals.get(0).courseID);
		verifyEvauationInfoExistsInList(dataBundle.evaluations.get("evaluation1InCourse1OfCoord1"),course1Evals);
		verifyEvauationInfoExistsInList(dataBundle.evaluations.get("evaluation2InCourse1OfCoord1"),course1Evals);
		
		//course with 1 evaluation
		assertEquals(course1Id, course1Evals.get(1).courseID);
		ArrayList<EvaluationDetailsForCoordinator> course2Evals = courseListForCoord.get("idOfCourse2OfCoord1").evaluations;
		assertEquals(1, course2Evals.size());
		verifyEvauationInfoExistsInList(dataBundle.evaluations.get("evaluation1InCourse2OfCoord1"),course2Evals);
		
		//course with 0 evaluations
		courseListForCoord = apiServlet.getCourseDetailsListForCoord("idOfTypicalCoord2");
		assertEquals(2, courseListForCoord.size());
		assertEquals(0, courseListForCoord.get("idOfCourse2OfCoord2").evaluations.size());
		
		//coord with 0 courses
		apiServlet.createCoord("coordWith0course", "Coord with 0 courses", "coordWith0course@gmail.com");
		courseListForCoord = apiServlet.getCourseDetailsListForCoord("coordWith0course");
		assertEquals(0, courseListForCoord.size());
		
		//non existent coord
		courseListForCoord = apiServlet.getCourseDetailsListForCoord("nonexistentcoord");
		assertEquals(0, courseListForCoord.size());
		
	}

	private void verifyEvauationInfoExistsInList(Evaluation evaluation,
			ArrayList<EvaluationDetailsForCoordinator> evalInfoList) {

		for(EvaluationDetailsForCoordinator edfc: evalInfoList){
			if(edfc.name.equals(evaluation.getName()))
				return;
		}
		Assert.fail("Did not find "+evaluation.getName()+" in the evaluation info list");
	}

	@Test
	public void testGetEvaluationResult() throws Exception {
		printTestCaseHeader();
		refreshDataInDatastore();
		// TODO: to be implemented
	}

	@Test
	public void testTfsListForCoord() throws Exception {
		printTestCaseHeader();
		refreshDataInDatastore();

		verifyTfsListForCoord(dataBundle.coords.get("typicalCoord1")
				.getGoogleID(), 1);
		verifyTfsListForCoord(dataBundle.coords.get("typicalCoord2")
				.getGoogleID(), 2);
		verifyTfsListForCoord("idOfNonExistentCoord", 0);
		// TODO: more testing
	}

	@Test
	public void testRenameTeam() throws Exception {
		printTestCaseHeader();
		refreshDataInDatastore();
		Student student1InCourse1 = dataBundle.students
				.get("student1InCourse1");
		String originalTeamName = student1InCourse1.getTeamName();
		String newTeamName = originalTeamName + "x";
		String courseID = student1InCourse1.getCourseID();
		verifyTeamNameChange(courseID, originalTeamName, newTeamName);

		refreshDataInDatastore();
		originalTeamName = student1InCourse1.getTeamName();
		courseID = student1InCourse1.getCourseID();
		verifyTeamNameChange(courseID, "nonExisentTeam", "newTeamName");

		// TODO: more testing

	}

	// ------------------------------------------------------------------------
	
	private void verifyEnrollmentResultForStudent(Student expectedStudent,
			StudentInfoForCoord enrollmentResult,
			StudentInfoForCoord.UpdateStatus status) {
		String errorMessage = "mismatch! \n"
				+ Common.getTeammatesGson().toJson(expectedStudent) + "\n"
				+ Common.getTeammatesGson().toJson(enrollmentResult);
		assertEquals(errorMessage, true,
				enrollmentResult.isEnrollmentInfoMatchingTo(expectedStudent,
						status));
	}
	
	private void verifyRegistrationEmailToStudent(Student student) {
		LocalTaskQueue ltq = LocalTaskQueueTestConfig.getLocalTaskQueue();
		QueueStateInfo qsi = ltq.getQueueStateInfo().get("email-queue");

		List<TaskStateInfo> taskInfoList = qsi.getTaskInfo();
		for (TaskStateInfo tsi : taskInfoList) {
			String emailTaskBody = tsi.getBody();
			if (emailTaskBody.contains("email="
					+ student.getEmail().replace("@", "%40"))
					&& emailTaskBody.contains("courseid="
							+ student.getCourseID())) {
				return;
			}
		}
		Assert.fail();
	}

	private int getNumberOfEmailTasksInQueue() {
		LocalTaskQueue ltq = LocalTaskQueueTestConfig.getLocalTaskQueue();
		QueueStateInfo qsi = ltq.getQueueStateInfo().get("email-queue");
		return qsi.getTaskInfo().size();
	}

	private void verifyTeamNameChange(String courseID, String originalTeamName,
			String newTeamName) {
		List<Student> studentsInClass = apiServlet
				.getStudentListForCourse(courseID);
		List<Student> studentsInTeam = new ArrayList<Student>();
		List<Student> studentsNotInTeam = new ArrayList<Student>();
		for (Student s : studentsInClass) {
			if (s.getTeamName().equals(originalTeamName)) {
				studentsInTeam.add(s);
			} else {
				studentsNotInTeam.add(s);
			}
		}
		apiServlet.renameTeam(courseID, originalTeamName, newTeamName);
		for (Student s : studentsInTeam) {
			assertEquals(newTeamName,
					apiServlet.getStudent(s.getCourseID(), s.getEmail())
							.getTeamName());
		}
		for (Student s : studentsNotInTeam) {
			String teamName = apiServlet.getStudent(s.getCourseID(),
					s.getEmail()).getTeamName();
			assertTrue("unexpected team name: " + teamName,
					!teamName.equals(newTeamName));
		}
		// TODO: check for changes in team profile
	}

	private void verifyTfsListForCoord(String coordId, int noOfTfs) {
		List<TeamFormingSession> tfsList = apiServlet
				.getTfsListForCoord(coordId);
		assertEquals(noOfTfs, tfsList.size());
		for (TeamFormingSession tfs : tfsList) {
			assertEquals(coordId, apiServlet.getCourse(tfs.getCourseID())
					.getCoordinatorID());
		}
	}

	private void alterSubmission(Submission submission) {
		submission.setPoints(submission.getPoints() + 10);
		submission.setCommentsToStudent(new Text(submission
				.getCommentsToStudent().getValue() + "x"));
		submission.setJustification(new Text(submission.getJustification()
				.getValue() + "y"));
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

	@After
	public void tearDown() {
		apiServlet.destroy();
		helper.tearDown();
		printTestClassFooter("CoordCourseAddApiTest");
	}

}
