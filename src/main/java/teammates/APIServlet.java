package teammates;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import teammates.exception.CourseDoesNotExistException;
import teammates.exception.CourseInputInvalidException;
import teammates.exception.EnrollException;
import teammates.exception.EntityAlreadyExistsException;
import teammates.exception.EntityDoesNotExistException;
import teammates.exception.InvalidParametersException;
import teammates.jdo.Coordinator;
import teammates.jdo.Course;
import teammates.jdo.CourseSummaryForCoordinator;
import teammates.jdo.EnrollmentReport;
import teammates.jdo.Evaluation;
import teammates.jdo.EvaluationDetailsForCoordinator;
import teammates.jdo.Student;
import teammates.jdo.StudentInfoForCoord;
import teammates.jdo.StudentInfoForCoord.UpdateStatus;
import teammates.jdo.Submission;
import teammates.jdo.TeamFormingLog;
import teammates.jdo.TeamFormingSession;
import teammates.jdo.TeamProfile;

import com.google.appengine.api.datastore.Text;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * The API Servlet.
 * 
 * This is a hidden (to end user) servlet. It receives REST requests and
 * directly alter the data.
 * 
 * Mainly there for some automated testing purposes
 * 
 * @author nvquanghuy
 * 
 */
@SuppressWarnings("serial")
public class APIServlet extends HttpServlet {
	public static final String OPERATION_CREATE_COORD = "OPERATION_CREATE_COORD";
	public static final String OPERATION_DELETE_COORD = "OPERATION_DELETE_COORD";
	public static final String OPERATION_DELETE_COORD_NON_CASCADE = "OPERATION_DELETE_COORD_NON_CASCADE";
	public static final String OPERATION_DELETE_COURSE = "OPERATION_DELETE_COURSE";
	public static final String OPERATION_DELETE_COURSE_BY_ID_NON_CASCADE = "OPERATION_DELETE_COURSE_BY_ID_NON_CASCADE";
	public static final String OPERATION_DELETE_EVALUATION = "OPERATION_DELETE_EVALUATION";
	public static final String OPERATION_DELETE_STUDENT = "OPERATION_DELETE_STUDENT";
	public static final String OPERATION_DELETE_TEAM_FORMING_LOG = "OPERATION_DELETE_TEAM_FORMING_LOG";
	public static final String OPERATION_DELETE_TEAM_PROFILE = "OPERATION_DELETE_TEAM_PROFILE";
	public static final String OPERATION_DELETE_TFS = "OPERATION_DELETE_TFS";
	public static final String OPERATION_EDIT_EVALUATION = "OPERATION_EDIT_COORD";
	public static final String OPERATION_EDIT_STUDENT = "OPERATION_EDIT_STUDENT";
	public static final String OPERATION_EDIT_SUBMISSION = "OPERATION_EDIT_SUBMISSION";
	public static final String OPERATION_EDIT_TEAM_PROFILE = "OPERATION_EDIT_TEAM_PROFILE";
	public static final String OPERATION_EDIT_TFS = "OPERATION_EDIT_TFS";
	public static final String OPERATION_GET_COORD_AS_JSON = "OPERATION_GET_COORD_AS_JSON";
	public static final String OPERATION_GET_COURSES_BY_COORD = "get_courses_by_coord";
	public static final String OPERATION_GET_COURSE_AS_JSON = "OPERATION_GET_COURSE_AS_JSON";
	public static final String OPERATION_GET_STUDENT_AS_JSON = "OPERATION_GET_STUDENT_AS_JSON";
	public static final String OPERATION_GET_EVALUATION_AS_JSON = "OPERATION_GET_EVALUATION_AS_JSON";
	public static final String OPERATION_GET_SUBMISSION_AS_JSON = "OPERATION_GET_SUBMISSION_AS_JSON";
	public static final String OPERATION_GET_TEAM_FORMING_LOG_AS_JSON = "OPERATION_GET_TEAM_FORMING_LOG_AS_JSON";
	public static final String OPERATION_GET_TEAM_PROFILE_AS_JSON = "OPERATION_GET_TEAM_PROFILE_AS_JSON";
	public static final String OPERATION_GET_TFS_AS_JSON = "OPERATION_GET_TFS_AS_JSON";
	public static final String OPERATION_PERSIST_DATABUNDLE = "OPERATION_PERSIST_DATABUNDLE";
	public static final String OPERATION_SYSTEM_ACTIVATE_AUTOMATED_REMINDER = "activate_auto_reminder";

	public static final String PARAMETER_COURSE_ID = "PARAMETER_COURSE_ID";
	public static final String PARAMETER_COORD_EMAIL = "PARAMETER_COORD_EMAIL";
	public static final String PARAMETER_COORD_ID = "PARAMETER_COORD_ID";
	public static final String PARAMETER_COORD_NAME = "PARAMETER_COORD_NAME";
	public static final String PARAMETER_DATABUNDLE_JSON = "PARAMETER_DATABUNDLE_JSON";
	public static final String PARAMETER_EVALUATION_NAME = "PARAMETER_EVALUATION_NAME";
	public static final String PARAMETER_JASON_STRING = "PARAMETER_JASON_STRING";
	public static final String PARAMETER_REVIEWER_EMAIL = "PARAMETER_REVIEWER_EMAIL";
	public static final String PARAMETER_REVIEWEE_EMAIL = "PARAMETER_REVIEWEE_EMAIL";
	public static final String PARAMETER_STUDENT_EMAIL = "PARAMETER_STUDENT_EMAIL";
	public static final String PARAMETER_STUDENT_ID = "PARAMETER_STUDENT_ID";
	public static final String PARAMETER_TEAM_NAME = "PARAMETER_TEAM_NAME";

	private HttpServletRequest req;
	private HttpServletResponse resp;
	private static final Logger log = Logger.getLogger(APIServlet.class
			.getName());

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {
		doPost(req, resp);
	}

	private PersistenceManager getPM() {
		return Datastore.getPersistenceManager();
	}

	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {

		this.req = req;
		this.resp = resp;

		// TODO: Change to JSON/XML
		resp.setContentType("text/plain");

		// Check for auth code(to prevent misuse)
		String auth = req.getParameter("tm_auth");
		if (!auth.equals(Config.inst().API_AUTH_CODE)) {
			resp.getWriter().write("Authentication fails.");
			resp.flushBuffer();
			return;
		}

		String action = req.getParameter("action");
		log.info(action);
		// TODO: reorder in alphabetical order
		if (action.equals("evaluation_open")) {
			evaluationOpen();
		} else if (action.equals("teamformingsession_open")) {
			teamFormingSessionOpen();
		} else if (action.equals("evaluation_close")) {
			evaluationClose();
		} else if (action.equals("evaluation_add")) {
			evaluationAdd();
		} else if (action.equals("evaluation_publish")) {
			evaluationPublish();
		} else if (action.equals("evaluation_unpublish")) {
			evaluationUnpublish();
		} else if (action.equals("teamformingsession_add")) {
			try {
				teamFormingSessionAdd();
			} catch (EntityAlreadyExistsException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (action.equals("createteamprofiles")) {
			createProfileOfExistingTeams();
		} else if (action.equals("course_add")) {
			courseAdd();
		} else if (action.equals("cleanup")) {
			totalCleanup();
		} else if (action.equals("cleanup_course")) {
			try {
				cleanupCourse();
			} catch (EntityDoesNotExistException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (action.equals("cleanup_by_coordinator")) {
			totalCleanupByCoordinator();
		} else if (action.equals("enroll_students")) {
			enrollStudents();
		} else if (action.equals("student_submit_feedbacks")) {
			studentSubmitFeedbacks();
		} else if (action.equals("student_submit_dynamic_feedbacks")) {
			studentSubmitDynamicFeedbacks();
		} else if (action.equals("students_join_course")) {
			studentsJoinCourse();
		} else if (action.equals("email_stress_testing")) {
			emailStressTesting();
		} else if (action.equals("enable_email")) {
			enableEmail();
		} else if (action.equals("disable_email")) {
			disableEmail();
		} else if (action.equals(OPERATION_SYSTEM_ACTIVATE_AUTOMATED_REMINDER)) {
			activateAutomatedReminder();
		} else {
			String returnValue;
			try {
				returnValue = executeBackendAction(req, action);
			} catch (Exception e) {
				returnValue = Common.BACKEND_STATUS_FAILURE + e.getMessage();
			}
			resp.getWriter().write(returnValue);
		}

		resp.flushBuffer();
	}

	private String executeBackendAction(HttpServletRequest req, String action)
			throws Exception {
		if (action.equals(OPERATION_CREATE_COORD)) {
			String coordID = req.getParameter(PARAMETER_COORD_ID);
			String coordName = req.getParameter(PARAMETER_COORD_NAME);
			String coordEmail = req.getParameter(PARAMETER_COORD_EMAIL);
			createCoord(coordID, coordName, coordEmail);
		} else if (action.equals(OPERATION_DELETE_COORD)) {
			String coordID = req.getParameter(PARAMETER_COORD_ID);
			deleteCoord(coordID);
		} else if (action.equals(OPERATION_DELETE_COORD_NON_CASCADE)) {
			String coordId = req.getParameter(PARAMETER_COORD_ID);
			deleteCoordByIdNonCascade(coordId);
		} else if (action.equals(OPERATION_DELETE_COURSE)) {
			String courseId = req.getParameter(PARAMETER_COURSE_ID);
			deleteCourse(courseId);
		} else if (action.equals(OPERATION_DELETE_COURSE_BY_ID_NON_CASCADE)) {
			String courseID = req.getParameter(PARAMETER_COURSE_ID);
			deleteCourseByIdNonCascade(courseID);
		} else if (action.equals(OPERATION_DELETE_EVALUATION)) {
			String courseId = req.getParameter(PARAMETER_COURSE_ID);
			String evaluationName = req.getParameter(PARAMETER_EVALUATION_NAME);
			deleteEvaluation(courseId, evaluationName);
		} else if (action.equals(OPERATION_DELETE_STUDENT)) {
			String courseId = req.getParameter(PARAMETER_COURSE_ID);
			String email = req.getParameter(PARAMETER_STUDENT_EMAIL);
			deleteStudent(courseId, email);
		} else if (action.equals(OPERATION_DELETE_TEAM_FORMING_LOG)) {
			String courseId = req.getParameter(PARAMETER_COURSE_ID);
			deleteTeamFormingLog(courseId);
		} else if (action.equals(OPERATION_DELETE_TEAM_PROFILE)) {
			String courseId = req.getParameter(PARAMETER_COURSE_ID);
			String teamName = req.getParameter(PARAMETER_TEAM_NAME);
			deleteTeamProfile(courseId, teamName);
		} else if (action.equals(OPERATION_DELETE_TFS)) {
			String courseId = req.getParameter(PARAMETER_COURSE_ID);
			deleteTfs(courseId);
		} else if (action.equals(OPERATION_GET_COORD_AS_JSON)) {
			String coordID = req.getParameter(PARAMETER_COORD_ID);
			return getCoordAsJson(coordID);
		} else if (action.equals(OPERATION_GET_COURSE_AS_JSON)) {
			String courseId = req.getParameter(PARAMETER_COURSE_ID);
			return getCourseAsJson(courseId);
		} else if (action.equals(OPERATION_GET_COURSES_BY_COORD)) {
			String coordID = req.getParameter(PARAMETER_COORD_ID);
			return getCoursesByCoordID(coordID);
		} else if (action.equals(OPERATION_GET_STUDENT_AS_JSON)) {
			String courseId = req.getParameter(PARAMETER_COURSE_ID);
			String email = req.getParameter(PARAMETER_STUDENT_EMAIL);
			return getStudentAsJson(courseId, email);
		} else if (action.equals(OPERATION_GET_EVALUATION_AS_JSON)) {
			String courseId = req.getParameter(PARAMETER_COURSE_ID);
			String evaluationName = req.getParameter(PARAMETER_EVALUATION_NAME);
			return getEvaluationAsJson(courseId, evaluationName);
		} else if (action.equals(OPERATION_GET_TFS_AS_JSON)) {
			String courseId = req.getParameter(PARAMETER_COURSE_ID);
			return getTfsAsJson(courseId);
		} else if (action.equals(OPERATION_GET_TEAM_FORMING_LOG_AS_JSON)) {
			String courseId = req.getParameter(PARAMETER_COURSE_ID);
			return getTeamFormingLogAsJson(courseId);
		} else if (action.equals(OPERATION_GET_TEAM_PROFILE_AS_JSON)) {
			String courseId = req.getParameter(PARAMETER_COURSE_ID);
			String teamName = req.getParameter(PARAMETER_TEAM_NAME);
			return getTeamProfileAsJson(courseId, teamName);
		} else if (action.equals(OPERATION_GET_SUBMISSION_AS_JSON)) {
			String courseId = req.getParameter(PARAMETER_COURSE_ID);
			String evaluationName = req.getParameter(PARAMETER_EVALUATION_NAME);
			String reviewerId = req.getParameter(PARAMETER_REVIEWER_EMAIL);
			String revieweeId = req.getParameter(PARAMETER_REVIEWEE_EMAIL);
			return getSubmissionAsJson(courseId, evaluationName, reviewerId,
					revieweeId);
		} else if (action.equals(OPERATION_PERSIST_DATABUNDLE)) {
			String dataBundleJsonString = req
					.getParameter(PARAMETER_DATABUNDLE_JSON);
			persistNewDataBundle(dataBundleJsonString);
		} else if (action.equals(OPERATION_EDIT_EVALUATION)) {
			String newValues = req.getParameter(PARAMETER_JASON_STRING);
			editEvaluationAsJson(newValues);
		} else if (action.equals(OPERATION_EDIT_SUBMISSION)) {
			String newValues = req.getParameter(PARAMETER_JASON_STRING);
			editSubmissionAsJson(newValues);
		} else if (action.equals(OPERATION_EDIT_STUDENT)) {
			String originalEmail = req.getParameter(PARAMETER_STUDENT_EMAIL);
			String newValues = req.getParameter(PARAMETER_JASON_STRING);
			editStudentAsJson(originalEmail,newValues);
		} else if (action.equals(OPERATION_EDIT_TFS)) {
			String newValues = req.getParameter(PARAMETER_JASON_STRING);
			editTfsAsJson(newValues);
		} else if (action.equals(OPERATION_EDIT_TEAM_PROFILE)) {
			String originalTeamName = req.getParameter(PARAMETER_TEAM_NAME);
			String newValues = req.getParameter(PARAMETER_JASON_STRING);
			editTeamProfileAsJson(originalTeamName,newValues);
		} else {
			throw new Exception("Unknown command: " + action);
		}
		return Common.BACKEND_STATUS_SUCCESS;
	}

	/**
	 * 
	 * @author wangsha
	 * @date Dec 19, 2011
	 */
	private void disableEmail() throws IOException {
		Config.inst().emailEnabled = false;
		resp.getWriter().write("ok");
	}

	/**
	 * 
	 * @author wangsha
	 * @date Dec 19, 2011
	 */
	private void enableEmail() throws IOException {
		Config.inst().emailEnabled = true;
		resp.getWriter().write("ok");

	}

	/**
	 * Open an evaluation to students
	 */
	protected void evaluationOpen() throws IOException {
		System.out.println("Opening evaluation.");
		String courseID = req.getParameter("course_id");
		String name = req.getParameter("evaluation_name");

		boolean edited = Evaluations.inst().openEvaluation(courseID, name);

		if (edited) {
			resp.getWriter().write("ok");
		} else {
			resp.getWriter().write("fail");
		}
	}

	/**
	 * Close an evaluation
	 */
	protected void evaluationClose() throws IOException {
		System.out.println("Closing evaluation.");
		String courseID = req.getParameter("course_id");
		String name = req.getParameter("evaluation_name");

		boolean edited = Evaluations.inst().closeEvaluation(courseID, name);

		if (edited) {
			resp.getWriter().write("ok");
		} else {
			resp.getWriter().write("fail");
		}
	}

	/**
	 * Publish an evaluation
	 */
	protected void evaluationPublish() throws IOException {
		String courseID = req.getParameter("course_id");
		String name = req.getParameter("evaluation_name");

		Courses courses = Courses.inst();
		List<Student> studentList = courses.getStudentList(courseID);

		boolean edited = Evaluations.inst().publishEvaluation(courseID, name,
				studentList);

		if (edited) {
			resp.getWriter().write("ok");
		} else {
			resp.getWriter().write("fail");
		}
	}

	/**
	 * Unpublish an evaluation
	 */
	protected void evaluationUnpublish() throws IOException {
		String courseID = req.getParameter("course_id");

		String name = req.getParameter("evaluation_name");
		boolean edited = Evaluations.inst().unpublishEvaluation(courseID, name);
		if (edited) {
			resp.getWriter().write("ok");
		} else {
			resp.getWriter().write("fail");
		}
	}

	protected void evaluationAdd() throws IOException {
		String json = req.getParameter("evaluation");

		Gson gson = new Gson();
		Evaluation e = gson.fromJson(json, Evaluation.class);

		boolean edited = Evaluations.inst().addEvaluation(e);

		// TODO take a snapshot of submissions

		if (edited) {
			resp.getWriter().write("ok");
		} else {
			resp.getWriter().write("fail");
		}
	}

	/**
	 * Enroll students to course. Copied directly from TeammatesServlet.
	 * 
	 * TODO: take a look into the logic again.
	 * 
	 * @param studentList
	 * @param courseId
	 * @throws
	 */
	protected void enrollStudents() throws IOException {
		System.out.println("Enrolling students.");
		String courseId = req.getParameter("course_id");
		String str_json = req.getParameter("students");

		Gson gson = new Gson();
		Type listType = new TypeToken<List<Student>>() {
		}.getType();
		List<Student> studentList = gson.fromJson(str_json, listType);

		// Remove ID (Google ID) from studentList because if it's present, the
		// student will already be joined the course.
		for (Student s : studentList) {
			s.setID("");
		}

		List<EnrollmentReport> enrollmentReportList = new ArrayList<EnrollmentReport>();

		// Check to see if there is an ongoing evaluation. If there is, do not
		// edit
		// students' teams.
		Courses courses = Courses.inst();
		List<Student> currentStudentList = courses.getStudentList(courseId);
		Evaluations evaluations = Evaluations.inst();

		if (evaluations.isEvaluationOngoing(courseId)) {
			for (Student s : studentList) {
				for (Student cs : currentStudentList) {
					if (s.getEmail().equals(cs.getEmail())
							&& !s.getTeamName().equals(cs.getTeamName())) {
						s.setTeamName(cs.getTeamName());
					}
				}
			}
		}
		// Add and edit Student objects in the datastore
		boolean edited = enrollmentReportList.addAll(courses.enrollStudents(
				studentList, courseId));

		if (edited) {
			resp.getWriter().write("ok");
		} else {
			resp.getWriter().write("fail");
		}
	}

	/**
	 * Delete all courses, evaluations, students, submissions. Except
	 * Coordinator
	 */
	@SuppressWarnings("unchecked")
	protected void totalCleanup() throws IOException {
		if (!Config.inst().APP_PRODUCTION_MOLD) {
			System.out.println("Cleaning up.");

			// Delete all courses
			getPM().deletePersistentAll(Courses.inst().getAllCourses());

			// Delete all evaluations
			List<Evaluation> evals = Evaluations.inst().getAllEvaluations();
			getPM().deletePersistentAll(evals);

			// Delete all submissions
			List<Submission> submissions = (List<Submission>) getPM().newQuery(
					Submission.class).execute();
			getPM().deletePersistentAll(submissions);

			// Delete all students
			List<Student> students = (List<Student>) getPM().newQuery(
					Student.class).execute();
			getPM().deletePersistentAll(students);

			resp.getWriter().write("ok");
		} else {
			resp.getWriter().write(
					"production mode, total cleaning up disabled");
		}

	}

	/**
	 * Clean up course, evaluation, submission related to the coordinator
	 * 
	 * @author wangsha
	 * @date Sep 8, 2011
	 */
	// TODO: this method does not do a 'total cleanup'
	protected void totalCleanupByCoordinator() {
		String coordID = req.getParameter("coordinator_id");
		Courses.inst().deleteCoordinatorCourses(coordID);

	}

	/**
	 * Clean up everything about a particular course
	 * 
	 * @throws EntityDoesNotExistException
	 */
	protected void cleanupCourse() throws EntityDoesNotExistException {

		String courseID = req.getParameter("course_id");
		System.out.println("APIServlet.cleanupCourse() courseID = " + courseID);
		cascadeCleanupCourse(courseID);
	}

	/**
	 * Deletes a course and all data associated with it: students, evaluations,
	 * team profiles, team-forming sessions
	 * 
	 * @param courseID
	 * @throws EntityDoesNotExistException
	 */
	private void cascadeCleanupCourse(String courseID)
			throws EntityDoesNotExistException {

		try {
			Courses.inst().cleanUpCourse(courseID);
		} catch (CourseDoesNotExistException e) {
			System.out.println("Course " + courseID
					+ " could not be deleted because " + "it does not exist");
			return;
		}
		Evaluations.inst().deleteEvaluations(courseID);
		TeamForming teamForming = TeamForming.inst();
		if (teamForming.getTeamFormingSession(courseID, null) != null)
			teamForming.deleteTeamFormingSession(courseID);
		if (teamForming.getTeamFormingLogList(courseID) != null)
			teamForming.deleteTeamFormingLog(courseID);

	}

	protected void courseAdd() throws IOException {
		log.info("APIServlet adding new course: ");
		String googleID = req.getParameter("google_id");
		String courseJson = req.getParameter("course");

		Gson gson = new Gson();
		Course c = gson.fromJson(courseJson, Course.class);
		c.setCoordinatorID(googleID);
		getPM().makePersistent(c);

		log.info("Course added: coord: " + c.getCoordinatorID()
				+ " course id: " + c.getID() + " course name: " + c.getName());

		resp.getWriter().write("ok");
	}

	protected void studentSubmitFeedbacks() throws IOException {

		String course_id = req.getParameter("course_id");
		String evaluation_name = req.getParameter("evaluation_name");
		String student_email = req.getParameter("student_email");
		System.out.println("Submitting feedback for student." + student_email);

		/*
		 * huy- Unable to use Transaction here. It says transaction batch
		 * operation must be on the same entity group (and must not be root
		 * entity). However it works for studentsJoinCourse below. ??? Aug 17 -
		 * It doesn't work for Join Course below either.
		 * http://code.google.com/appengine
		 * /docs/java/datastore/transactions.html
		 * #What_Can_Be_Done_In_a_Transaction
		 */

		Query query = getPM().newQuery(Submission.class);
		query.setFilter("courseID == course_id");
		query.setFilter("evaluationName == evaluation_name");
		query.setFilter("fromStudent == student_email");
		query.declareParameters("String course_id, String evaluation_name, String student_email");
		@SuppressWarnings("unchecked")
		List<Submission> submissions = (List<Submission>) query.execute(
				course_id, evaluation_name, student_email);

		for (Submission submission : submissions) {
			submission.setPoints(100);
			submission.setCommentsToStudent(new Text(String.format(
					"This is a public comment from %s to %s.", student_email,
					submission.getToStudent())));
			submission.setJustification(new Text(String.format(
					"This is a justification from %s to %s", student_email,
					submission.getToStudent())));
		}

		// Store back to datastore
		System.out.println(getPM().makePersistentAll(submissions));

		resp.getWriter().write("ok");
	}

	/**
	 * Special Submission Function for Testing Evaluation Points
	 * 
	 * @param points
	 *            defined in scenario.json
	 * @author xialin
	 **/
	protected void studentSubmitDynamicFeedbacks() throws IOException {

		String course_id = req.getParameter("course_id");
		String evaluation_name = req.getParameter("evaluation_name");
		String student_email = req.getParameter("student_email");
		String team_name = req.getParameter("team_name");
		String submission_points = req.getParameter("submission_points");
		String[] pointsArray = submission_points.split(", ");

		Query studentQuery = getPM().newQuery(Student.class);
		studentQuery.setFilter("courseID == course_id");
		studentQuery.setFilter("teamName == team_name");
		studentQuery.declareParameters("String course_id, String team_name");
		@SuppressWarnings("unchecked")
		List<Student> students = (List<Student>) studentQuery.execute(
				course_id, team_name);

		Query query = getPM().newQuery(Submission.class);
		query.setFilter("courseID == course_id");
		query.setFilter("evaluationName == evaluation_name");
		query.setFilter("fromStudent == student_email");
		query.declareParameters("String course_id, String evaluation_name, String student_email");
		@SuppressWarnings("unchecked")
		List<Submission> submissions = (List<Submission>) query.execute(
				course_id, evaluation_name, student_email);

		int position = 0;
		for (Submission submission : submissions) {
			for (int i = 0; i < students.size(); i++) {
				if (submission.getToStudent().equalsIgnoreCase(
						students.get(i).getEmail()))
					position = i;
				int point = Integer.valueOf(pointsArray[position]);
				submission.setPoints(point);
				submission.setCommentsToStudent(new Text(String.format(
						"This is a public comment from %s to %s.",
						student_email, submission.getToStudent())));
				submission.setJustification(new Text(String.format(
						"This is a justification from %s to %s", student_email,
						submission.getToStudent())));
			}
		}

		getPM().makePersistentAll(submissions);
		resp.getWriter().write("ok");

	}

	protected void teamFormingSessionAdd() throws IOException,
			EntityAlreadyExistsException {
		String json = req.getParameter("teamformingsession");

		Gson gson = new Gson();
		TeamFormingSession e = gson.fromJson(json, TeamFormingSession.class);

		try {
			TeamForming teamForming = TeamForming.inst();
			teamForming.createTeamFormingSession(e.getCourseID(), e.getStart(),
					e.getDeadline(), e.getTimeZone(), e.getGracePeriod(),
					e.getInstructions(), e.getProfileTemplate());
			resp.getWriter().write("ok");
		} catch (EntityAlreadyExistsException ex) {
			resp.getWriter().write("fail");
		}
	}

	protected void teamFormingSessionOpen() throws IOException {
		System.out.println("Opening team forming session.");
		String courseID = req.getParameter("course_id");

		boolean edited = TeamForming.inst().openTeamFormingSession(courseID);

		if (edited) {
			resp.getWriter().write("ok");
		} else {
			resp.getWriter().write("fail");
		}
	}

	protected void createProfileOfExistingTeams() throws IOException {
		System.out.println("Creating profiles of existing teams.");
		String courseId = req.getParameter("course_id");
		String courseName = req.getParameter("course_name");
		String teamName = req.getParameter("team_name");
		Text teamProfile = new Text(req.getParameter("team_profile"));

		// Add the team forming session
		TeamForming teamForming = TeamForming.inst();

		try {
			teamForming.createTeamProfile(courseId, courseName, teamName,
					teamProfile);
			resp.getWriter().write("ok");
		}

		catch (EntityAlreadyExistsException e) {
			resp.getWriter().write("fail");
		}
	}

	protected void studentsJoinCourse() throws IOException {
		System.out.println("Joining course for students.");

		// Set the Student.ID to emails.
		String course_id = req.getParameter("course_id");
		String str_json_students = req.getParameter("students");
		Type listType = new TypeToken<List<Student>>() {
		}.getType();
		Gson gson = new Gson();
		List<Student> students = gson.fromJson(str_json_students, listType);

		// Construct a Map< Email --> Student>
		HashMap<String, Student> mapStudents = new HashMap<String, Student>();
		for (Student s : students) {
			mapStudents.put(s.getEmail(), s);
		}

		// Query all Datastore's Student objects with CourseID received

		Query query = getPM().newQuery(Student.class);
		query.setFilter("courseID == course_id");
		query.declareParameters("String course_id");
		@SuppressWarnings("unchecked")
		List<Student> datastoreStudents = (List<Student>) query
				.execute(course_id);

		for (Student dsStudent : datastoreStudents) {
			Student jsStudent = mapStudents.get(dsStudent.getEmail());
			if (jsStudent != null) {
				dsStudent.setID(jsStudent.getID());
			}
		}
		// Store back to datastore
		getPM().makePersistentAll(datastoreStudents);

		resp.getWriter().write("Fail: something wrong");
	}

	protected void emailStressTesting() throws IOException {
		Emails emails = new Emails();
		String account = req.getParameter("account");
		int size = Integer.parseInt(req.getParameter("size"));

		emails.mailStressTesting(account, size);
		resp.getWriter().write("ok");

	}

	/**
	 * request to automatedReminders servlet
	 * 
	 * @throws IOException
	 * @throws ServletException
	 */
	protected void activateAutomatedReminder() throws IOException,
			ServletException {
		RequestDispatcher dispatcher = getServletContext()
				.getRequestDispatcher("/automatedreminders");
		dispatcher.forward(this.req, this.resp);
	}

	private String getCoursesByCoordID(String coordID) {
		String query = "select from " + Course.class.getName()
				+ " where coordinatorID == '" + coordID + "'";

		@SuppressWarnings("unchecked")
		List<Course> courseList = (List<Course>) getPM().newQuery(query)
				.execute();
		String courseIDs = "";

		for (Course c : courseList) {
			courseIDs = courseIDs + c.getID() + " ";
		}
		return courseIDs.trim();
	}

	private void deleteCourseByIdNonCascade(String courseID) {
		Courses courses = Courses.inst();
		courses.deleteCoordinatorCourse(courseID);
	}

	private void deleteCoordByIdNonCascade(String coordId) {
		Accounts accounts = Accounts.inst();
		try {
			accounts.deleteCoordinatorNonCascade(coordId);
		} catch (Exception e) {
			log.warning("problem while trying to delete coordinator" + coordId
					+ "\n error:" + e.getMessage());
		}
	}

	// --------------------- revised API ------------------------------------

	/*
	 * Get methods should throw an exception if the entity was not found. It is
	 * "not expected" that UI will try to access a non-existent entity.
	 */

	private String getCoordAsJson(String coordID) {
		Coordinator coord = getCoord(coordID);
		return Common.getTeammatesGson().toJson(coord);
	}

	private String getCourseAsJson(String courseId) {
		Course course = getCourse(courseId);
		return Common.getTeammatesGson().toJson(course);
	}

	private String getStudentAsJson(String courseId, String email) {
		Student student = getStudent(courseId, email);
		return Common.getTeammatesGson().toJson(student);
	}

	private String getEvaluationAsJson(String courseId, String evaluationName) {
		Evaluation evaluation = getEvaluation(courseId, evaluationName);
		return Common.getTeammatesGson().toJson(evaluation);
	}

	private String getSubmissionAsJson(String courseId, String evaluationName,
			String reviewerEmail, String revieweeEmail) {
		Submission target = getSubmission(courseId, evaluationName,
				reviewerEmail, revieweeEmail);
		return Common.getTeammatesGson().toJson(target);
	}

	private String getTfsAsJson(String courseId) {
		TeamFormingSession tfs = getTfs(courseId);
		return Common.getTeammatesGson().toJson(tfs);
	}

	private String getTeamProfileAsJson(String courseId, String teamName) {
		TeamProfile teamProfile = getTeamProfile(courseId, teamName);
		return Common.getTeammatesGson().toJson(teamProfile);
	}

	private String getTeamFormingLogAsJson(String courseId) {
		List<TeamFormingLog> teamFormingLogList = getTeamFormingLog(courseId);
		return Common.getTeammatesGson().toJson(teamFormingLogList);
	}

	private void createSubmissions(List<Submission> submissionsList) {
		Evaluations.inst().editSubmissions(submissionsList);
	}
	
	private void editStudentAsJson(String originalEmail, String newValues) throws InvalidParametersException {
		Student student = Common.getTeammatesGson().fromJson(newValues, Student.class);
		editStudent(originalEmail, student);
	}
	
	private void editEvaluationAsJson(String evaluationJson) throws InvalidParametersException, EntityDoesNotExistException {
		Evaluation evaluation = Common.getTeammatesGson().fromJson(evaluationJson, Evaluation.class);
		editEvaluation(evaluation);
	}
	
	private void editSubmissionAsJson(String submissionJson) {
		Submission submission = Common.getTeammatesGson().fromJson(submissionJson, Submission.class);
		ArrayList<Submission> submissionList = new ArrayList<Submission>();
		submissionList.add(submission);
		editSubmission(submissionList);
	}
	
	private void editTfsAsJson(String tfsJson) throws EntityDoesNotExistException {
		TeamFormingSession tfs = Common.getTeammatesGson().fromJson(tfsJson,
				TeamFormingSession.class);
		TeamForming.inst().editTeamFormingSession(tfs.getCourseID(),
				tfs.getStart(), tfs.getDeadline(), tfs.getGracePeriod(),
				tfs.getInstructions(), tfs.getProfileTemplate(),
				tfs.isActivated(), tfs.getTimeZone());
	}
	
	private void editTeamProfileAsJson(String originalTeamName, String teamProfileJson) throws EntityDoesNotExistException {
		TeamProfile teamProfile = Common.getTeammatesGson().fromJson(teamProfileJson,
				TeamProfile.class);
		TeamForming.inst().editTeamProfile(teamProfile.getCourseID(), teamProfile.getCourseName(), originalTeamName, teamProfile.getTeamName(), teamProfile.getTeamProfile());
	}
	

	// =======================API for JSP======================================

	public String coordGetLoginUrl(String redirectUrl) {
		Accounts accounts = Accounts.inst();
		return accounts.getLoginPage(redirectUrl);
	}

	public String coordGetLogoutUrl(String redirectUrl) {
		Accounts accounts = Accounts.inst();
		return accounts.getLogoutPage(redirectUrl);
	}

	/**
	 * Persists given data in the datastore Works ONLY if the data is correct
	 * and new (i.e. these entities do not already exist in the datastore). The
	 * behavior is undefined if incorrect or not new.
	 * 
	 * @param dataBundleJsonString
	 * @return status of the request in the form 'status meassage'+'additional
	 *         info (if any)' e.g., "[BACKEND_STATUS_SUCCESS]" e.g.,
	 *         "[BACKEND_STATUS_FAILURE]NullPointerException at ..."
	 * @throws CourseInputInvalidException
	 */
	public String persistNewDataBundle(String dataBundleJsonString)
			throws Exception {
		Gson gson = Common.getTeammatesGson();

		DataBundle data = gson.fromJson(dataBundleJsonString, DataBundle.class);
		HashMap<String, Coordinator> coords = data.coords;
		for (Coordinator coord : coords.values()) {
			log.info("API Servlet adding coord :" + coord.getGoogleID());
			createCoord(coord.getGoogleID(), coord.getName(), coord.getEmail());
		}

		HashMap<String, Course> courses = data.courses;
		for (Course course : courses.values()) {
			log.info("API Servlet adding course :" + course.getID());
			createCourse(course.getCoordinatorID(), course.getID(),
					course.getName());
		}

		HashMap<String, Student> students = data.students;
		for (Student student : students.values()) {
			log.info("API Servlet adding student :" + student.getEmail()
					+ " to course " + student.getCourseID());
			createStudent(student);
		}

		HashMap<String, Evaluation> evaluations = data.evaluations;
		for (Evaluation evaluation : evaluations.values()) {
			log.info("API Servlet adding evaluation :" + evaluation.getName()
					+ " to course " + evaluation.getCourseID());
			createEvalution(evaluation);
		}

		// processing is slightly different for submissions because we are
		// adding all submissions in one go
		HashMap<String, Submission> submissionsMap = data.submissions;
		List<Submission> submissionsList = new ArrayList<Submission>();
		for (Submission submission : submissionsMap.values()) {
			log.info("API Servlet adding submission for "
					+ submission.getEvaluationName() + " from "
					+ submission.getFromStudent() + " to "
					+ submission.getToStudent());
			submissionsList.add(submission);
		}
		createSubmissions(submissionsList);
		log.info("API Servlet added " + submissionsList.size() + " submissions");

		HashMap<String, TeamFormingSession> tfsMap = data.teamFormingSessions;
		for (TeamFormingSession tfs : tfsMap.values()) {
			log.info("API Servlet adding TeamFormingSession to course "
					+ tfs.getCourseID());
			createTfs(tfs);
		}

		HashMap<String, TeamProfile> teamProfiles = data.teamProfiles;
		for (TeamProfile teamProfile : teamProfiles.values()) {
			log.info("API Servlet adding TeamProfile of "
					+ teamProfile.getTeamName() + " in course "
					+ teamProfile.getCourseID());
			createTeamProfile(teamProfile);
		}

		HashMap<String, TeamFormingLog> teamFormingLogs = data.teamFormingLogs;
		for (TeamFormingLog teamFormingLog : teamFormingLogs.values()) {
			log.info("API Servlet adding TeamFormingLog in course "
					+ teamFormingLog.getCourseID() + " : "
					+ teamFormingLog.getMessage().getValue());
			createTeamFormingLogEntry(teamFormingLog);
		}

		return Common.BACKEND_STATUS_SUCCESS;
	}

	// -----------------------Coordinators------------------------------------------

	public void createCoord(String coordID, String coordName, String coordEmail)
			throws EntityAlreadyExistsException, InvalidParametersException {
		Accounts.inst().addCoordinator(coordID, coordName, coordEmail);
	}

	public Coordinator getCoord(String coordID) {
		return Accounts.inst().getCoordinator(coordID);
	}

	public void deleteCoord(String coordId) {
		List<Course> coordCourseList = Courses.inst().getCoordinatorCourseList(
				coordId);
		for (Course course : coordCourseList) {
			deleteCourse(course.getID());
		}
		Accounts.inst().deleteCoord(coordId);
	}
	
	public HashMap<String, CourseSummaryForCoordinator> getCourseListForCoord(
			String coordId) {
		return Courses.inst().getCourseSummaryListForCoord(coordId);
	}
	
	public HashMap<String, CourseSummaryForCoordinator> getCourseDetailsListForCoord(
			String coordId) {
		//TODO: using this method here may not be efficient as it retrieves info not required
		HashMap<String, CourseSummaryForCoordinator> courseList = getCourseListForCoord(coordId);
		ArrayList<EvaluationDetailsForCoordinator> evaluationList = getEvaluationsListForCoord(coordId);
		for(EvaluationDetailsForCoordinator edfc: evaluationList){
			CourseSummaryForCoordinator courseSummary = courseList.get(edfc.courseID);
			courseSummary.evaluations.add(edfc);
		}
		return courseList;
	}
	
	public ArrayList<EvaluationDetailsForCoordinator> getEvaluationsListForCoord(
			String coordId) {

		List<Course> courseList = Courses.inst().getCoordinatorCourseList(coordId);
		ArrayList<EvaluationDetailsForCoordinator> evaluationDetailsList = new ArrayList<EvaluationDetailsForCoordinator>();
		
		for(Course c: courseList){
			evaluationDetailsList.addAll(Evaluations.inst().getEvaluationsSummaryForCourse(c.getID()));
		}
		return evaluationDetailsList;
	}
	

	public List<TeamFormingSession> getTfsListForCoord(String coordId) {
		List<Course> courseList = Courses.inst().getCoordinatorCourseList(coordId);
		List<TeamFormingSession> teamFormingSessionList = TeamForming.inst()
				.getTeamFormingSessionList(courseList);	
		return teamFormingSessionList;
	}

	// -----------------------Courses------------------------------------------

	public void createCourse(String coordinatorId, String courseId,
			String courseName) throws EntityAlreadyExistsException,
			InvalidParametersException {
		Courses.inst().addCourse(courseId, courseName, coordinatorId);
	}

	public Course getCourse(String courseId) {
		return Courses.inst().getCourse(courseId);
	}

	public void deleteCourse(String courseId) {
		Evaluations.inst().deleteEvaluations(courseId);
		TeamForming.inst().deleteTeamFormingSession(courseId);
		Courses.inst().deleteCourse(courseId);
	}

	public List<Student> getStudentListForCourse(String courseId) {
		return Courses.inst().getStudentList(courseId);
	}
	
	public void sendRegistrationInviteForCourse(String courseId) {
		List<Student> studentList = Courses.inst().getUnregisteredStudentList(courseId);

		for(Student s: studentList){
			sendRegistrationInviteToStudent(courseId, s.getEmail());
		}
	}
	
	public List<StudentInfoForCoord> enrollStudents(String enrollLines, String courseId) throws EnrollException   {
		ArrayList<StudentInfoForCoord> returnList = new ArrayList<StudentInfoForCoord>();
		String[] linesArray = enrollLines.split(Common.EOL); 
		ArrayList<Student> studentList = new ArrayList<Student>();
		
		//check if all non-empty lines are formatted correctly
		for (int i = 0; i < linesArray.length; i++) {
			String line = linesArray[i];
			try {
				if(Common.isWhiteSpace(line))continue;
				studentList.add(new Student(line, courseId));
			} catch (InvalidParametersException e) {
				throw new EnrollException(e.errorCode, "Problem in line : "+line+Common.EOL+e.getMessage());
			}
		}
		
		//enroll all students
		for(Student student: studentList){
			StudentInfoForCoord studentInfo;
			studentInfo = enrollStudent(student);
			returnList.add(studentInfo);
		}
		
		//add to return list students not included in the enroll list.
		List<Student> studentsInCourse = getStudentListForCourse(courseId);
		for(Student student: studentsInCourse){
			if(!isInEnrollList(student,returnList)){
				StudentInfoForCoord studentInfo = convertToStudentInfoForCoord(student);
				studentInfo.updateStatus = StudentInfoForCoord.UpdateStatus.NOT_IN_ENROLL_LIST;
				returnList.add(studentInfo);
			}
		}
		return returnList;
	}
		
	private boolean isInEnrollList(Student student,
			ArrayList<StudentInfoForCoord> studentInfoList) {
		for(StudentInfoForCoord studentInfo: studentInfoList){
			if(studentInfo.email.equalsIgnoreCase(student.getEmail()))
				return true;
		}
		return false;
	}

	public StudentInfoForCoord enrollStudent(Student student){
		StudentInfoForCoord.UpdateStatus updateStatus = UpdateStatus.UNMODIFIED;
		try {
			if (isSameAsExistingStudent(student)) {
				updateStatus = UpdateStatus.UNMODIFIED;
			} else if (isModificationToExistingStudent(student)) {
				editStudent(student.getEmail(), student);
				updateStatus = UpdateStatus.MODIFIED;
			} else {
				createStudent(student);
				updateStatus = UpdateStatus.NEW;
			}
		} catch (Exception e) {
			updateStatus = UpdateStatus.ERROR;
			log.severe("EntityExistsExcpetion thrown unexpectedly");
			e.printStackTrace();
		}
		StudentInfoForCoord studentInfo = convertToStudentInfoForCoord(student);
		studentInfo.updateStatus = updateStatus;
		return studentInfo;
	}

	private boolean isSameAsExistingStudent(Student student) {
		Student existingStudent = getStudent(student.getCourseID(), student.getEmail());
		if(existingStudent==null) return false;
		return student.isEnrollInfoSameAs(existingStudent);
	}

	private StudentInfoForCoord convertToStudentInfoForCoord(Student student) {
		StudentInfoForCoord studentInfoForCoord = new StudentInfoForCoord(student);
		return studentInfoForCoord;
	}

	private boolean isModificationToExistingStudent(Student student) {
		return getStudent(student.getCourseID(), student.getEmail())!=null;
	}

	// ----------------------Students------------------------------------------
	public void createStudent(Student student)
			throws EntityAlreadyExistsException, InvalidParametersException {
		Accounts.inst().createStudent(student);
	}

	public void deleteStudent(String courseId, String studentEmail) {
		Courses.inst().deleteStudent(courseId, studentEmail);
		Evaluations.inst().deleteSubmissionsForStudent(courseId, studentEmail);
		TeamForming.inst().deleteLogsForStudent(courseId,studentEmail);
		// TODO:delete team profile, if the last member
	}

	public Student getStudent(String courseId, String email) {
		return Accounts.inst().getStudent(courseId, email);
	}

	public void editStudent(String originalEmail, Student student)
			throws InvalidParametersException {
		// TODO: make the implementation more defensive
		Courses.inst().editStudent(student.getCourseID(), originalEmail,
				student.getName(), student.getTeamName(), student.getEmail(), student.getID(),
				student.getComments(), student.getProfileDetail());
	}
	
	public void sendRegistrationInviteToStudent(String courseId, String studentEmail) {
		
		Course course = Courses.inst().getCourse(courseId);
		Student student = Courses.inst().getStudentWithEmail(courseId, studentEmail);
		Coordinator coord = Accounts.inst().getCoordinator(course.getCoordinatorID());
		List<Student> studentList = new ArrayList<Student>();
		studentList.add(student);

		//TODO: this need not be a batch processing method
		Courses.inst().sendRegistrationKeys(studentList, courseId, course.getName(), coord.getName(), coord.getEmail());
		
	}

	// ------------------------Evaluations-----------------------------------

	public void createEvalution(Evaluation evaluation)
			throws EntityAlreadyExistsException, InvalidParametersException {
		Evaluations.inst().addEvaluation(evaluation);
	}

	public Evaluation getEvaluation(String courseId, String evaluationName) {
		return Evaluations.inst().getEvaluation(courseId, evaluationName);
	}

	public void deleteEvaluation(String courseId, String evaluationName) {
		Evaluations.inst().deleteEvaluation(courseId, evaluationName);
	}
	
	public void editEvaluation(Evaluation evaluation) throws EntityDoesNotExistException, InvalidParametersException{
		Evaluations.inst().editEvaluation(evaluation.getCourseID(),
				evaluation.getName(), evaluation.getInstructions(),
				evaluation.isCommentsEnabled(), evaluation.getStart(),
				evaluation.getDeadline(), evaluation.getGracePeriod(),
				evaluation.isActivated(), evaluation.isPublished(),
				evaluation.getTimeZone());
	}
	
	public void publishEvaluation(String courseId, String evaluationName) throws EntityDoesNotExistException{
		Courses courses = Courses.inst();
		List<Student> studentList = courses.getStudentList(courseId);

		Evaluations.inst().publishEvaluation(courseId, evaluationName, studentList);
	}
	
	public void unpublishEvaluation(String courseId, String evaluationName) {
		Evaluations.inst().unpublishEvaluation(courseId, evaluationName);
	}
	
	public Submission getSubmission(String courseId, String evaluationName,
			String reviewerEmail, String revieweeEmail) {
		return Evaluations.inst().getSubmission(courseId, evaluationName,
				reviewerEmail, revieweeEmail);
	}

	public void editSubmission(List<Submission> submissions) {
		Evaluations.inst().editSubmissions(submissions);
	}

	// ------------------------teamForming----------------------------------

	public void createTfs(TeamFormingSession tfs)
			throws EntityAlreadyExistsException, InvalidParametersException {
		TeamForming.inst().createTeamFormingSession(tfs);
	}

	public void deleteTfs(String courseId) {
		TeamForming.inst().deleteTeamFormingSession(courseId);
	}

	public TeamFormingSession getTfs(String courseId) {
		return TeamForming.inst().getTeamFormingSession(courseId);
	}

	public void getTfs(TeamFormingSession tfs) {
		TeamForming.inst().editTeamFormingSession(tfs.getCourseID(),
				tfs.getStart(), tfs.getDeadline(), tfs.getGracePeriod(),
				tfs.getInstructions(), tfs.getProfileTemplate());
	}


	public List<TeamFormingLog> getTeamFormingLog(String courseId) {
		return TeamForming.inst().getTeamFormingLogList(courseId);
	}

	public void createTeamFormingLogEntry(TeamFormingLog teamFormingLog)
			throws InvalidParametersException {
		TeamForming.inst().createTeamFormingLogEntry(teamFormingLog);
	}

	public void deleteTeamFormingLog(String courseId) {
		TeamForming.inst().deleteTeamFormingLog(courseId);
	}

	
	
	public void createTeamProfile(TeamProfile teamProfile)
			throws EntityAlreadyExistsException, InvalidParametersException {
		TeamForming.inst().createTeamProfile(teamProfile);
	}
	
	public TeamProfile getTeamProfile(String courseId, String teamName) {
		return TeamForming.inst().getTeamProfile(courseId, teamName);
	}
	
	public void deleteTeamProfile(String courseId, String teamName) {
		TeamForming.inst().deleteTeamProfile(courseId, teamName);
	}

	public void editTeamProfile(String originalTeamName, TeamProfile modifieldTeamProfile) throws EntityDoesNotExistException {
		TeamForming.inst().editTeamProfile(modifieldTeamProfile.getCourseID(),
				modifieldTeamProfile.getCourseName(),
				originalTeamName,
				modifieldTeamProfile.getTeamName(),
				modifieldTeamProfile.getTeamProfile());
	}
	
	public void renameTeam(String courseId, String originalTeamName,
			String newTeamName) {
		TeamForming.inst().editStudentsTeam(courseId, originalTeamName, newTeamName);
	}



}
