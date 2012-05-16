package teammates;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import teammates.exception.AccountExistsException;
import teammates.exception.CourseDoesNotExistException;
import teammates.exception.CourseExistsException;
import teammates.exception.CourseInputInvalidException;
import teammates.exception.TeamFormingSessionExistsException;
import teammates.exception.TeamProfileExistsException;
import teammates.jdo.Coordinator;
import teammates.jdo.Course;
import teammates.jdo.CourseSummaryForCoordinator;
import teammates.jdo.EnrollmentReport;
import teammates.jdo.Evaluation;
import teammates.jdo.Student;
import teammates.jdo.Submission;
import teammates.jdo.TeamFormingSession;

import com.google.appengine.api.datastore.Text;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.logging.Logger;

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
	public static final String OPERATION_DELETE_COORD_NON_CASCADE = "OPERATION_DELETE_COORD_NON_CASCADE";
	public static final String OPERATION_DELETE_COURSE_BY_ID_NON_CASCADE = "OPERATION_DELETE_COURSE_BY_ID_NON_CASCADE";
	public static final String OPERATION_GET_COORD_BY_ID = "OPERATION_GET_COORD_BY_ID";
	public static final String OPERATION_GET_COURSES_BY_COORD="get_courses_by_coord";
	public static final String OPERATION_GET_COURSE_BY_ID = "OPERATION_GET_COURSE_BY_ID";
	public static final String OPERATION_PERSIST_DATABUNDLE = "OPERATION_PERSIST_DATABUNDLE";
	public static final String OPERATION_SYSTEM_ACTIVATE_AUTOMATED_REMINDER="activate_auto_reminder";

	public static final String PARAMETER_COURSE_ID = "PARAMETER_COURSE_ID";
	public static final String PARAMETER_COORD_EMAIL = "PARAMETER_COORD_EMAIL";
	public static final String PARAMETER_COORD_ID = "PARAMETER_COORD_ID";
	public static final String PARAMETER_COORD_NAME = "PARAMETER_COORD_NAME";
	public static final String PARAMETER_DATABUNDLE_JSON = "PARAMETER_DATABUNDLE_JSON";
	
	private HttpServletRequest req;
	private HttpServletResponse resp;
	private static final Logger log = Logger.getLogger(APIServlet.class.getName());
	
	
	
	

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
        if (action.equals("evaluation_open")) {
                evaluationOpen();
        } else if(action.equals("teamformingsession_open")) {
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
                teamFormingSessionAdd();
        } else if (action.equals("createteamprofiles")) {
                createProfileOfExistingTeams();
        } else if (action.equals("course_add")) {
                courseAdd();
        } else if (action.equals("cleanup")) {
                totalCleanup();
        } else if (action.equals("cleanup_course")) {
                cleanupCourse();
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
        }  else if (action.equals(OPERATION_SYSTEM_ACTIVATE_AUTOMATED_REMINDER)){
                activateAutomatedReminder();
        } else if (action.equals(OPERATION_GET_COURSES_BY_COORD)){
    		String coordID = req.getParameter(PARAMETER_COORD_ID);
    		String courseIDs = getCoursesByCoordID(coordID);
    		resp.getWriter().write(courseIDs);
        } else if (action.equals(OPERATION_DELETE_COURSE_BY_ID_NON_CASCADE)){
    		String courseID = req.getParameter(PARAMETER_COURSE_ID);
    		deleteCourseByIdNonCascade(courseID);
        }else if (action.equals(OPERATION_GET_COORD_BY_ID)){
    		String coordID = req.getParameter(PARAMETER_COORD_ID);
    		String coordJsonString = getCoordByID(coordID);
    		resp.getWriter().write(coordJsonString);
        }else if (action.equals(OPERATION_CREATE_COORD)){
        	String coordID = req.getParameter(PARAMETER_COORD_ID);
        	String coordName = req.getParameter(PARAMETER_COORD_NAME);
    		String coordEmail = req.getParameter(PARAMETER_COORD_EMAIL);
    		createCoordIfNew(coordID, coordName, coordEmail);
        }else if (action.equals(OPERATION_DELETE_COORD_NON_CASCADE)){
    		String coordId = req.getParameter(PARAMETER_COORD_ID);
    		deleteCoordByIdNonCascade(coordId);
        }else if (action.equals(OPERATION_GET_COURSE_BY_ID)){
    		String courseId = req.getParameter(PARAMETER_COURSE_ID);
    		String courseJasonString = getCourseById(courseId);
    		resp.getWriter().write(courseJasonString);
        }else if (action.equals(OPERATION_PERSIST_DATABUNDLE)){
    		String dataBundleJsonString = req.getParameter(PARAMETER_DATABUNDLE_JSON);
    		String status;
			try {
				status = persistDataBundleIfNew(dataBundleJsonString);
			} catch (Exception e) {
				status = Common.BACKEND_STATUS_FAILURE+ e.getMessage();
			}
    		resp.getWriter().write(status);
        } else {
                System.err.println("Unknown command: " + action);
        }

		resp.flushBuffer();
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
		boolean edited = enrollmentReportList.addAll(courses.enrolStudents(studentList, courseId));

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
	//TODO: this method does not do a 'total cleanup'
	protected void totalCleanupByCoordinator() {
		String coordID = req.getParameter("coordinator_id");
		try {
			Courses.inst().deleteCoordinatorCourses(coordID);
		} catch (CourseDoesNotExistException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Clean up everything about a particular course
	 */
	protected void cleanupCourse() {
		
		String courseID = req.getParameter("course_id");
		System.out.println("APIServlet.cleanupCourse() courseID = " + courseID);
		cascadeCleanupCourse(courseID);
	}

	/**
	 * Deletes a course and all data associated with it: students, evaluations, 
	 *    team profiles, team-forming sessions
	 * @param courseID
	 */
	private void cascadeCleanupCourse(String courseID) {
		
			try {
				Courses.inst().cleanUpCourse(courseID);
			} catch (CourseDoesNotExistException e) {
				System.out.println("Course "+courseID+" could not be deleted because " +
						"it does not exist");
				return;
			}
			Evaluations.inst().deleteEvaluations(courseID);
			TeamForming teamForming = TeamForming.inst();
            if (teamForming.getTeamFormingSession(courseID, null) != null)
            	teamForming.deleteTeamFormingSession(courseID);
            if(teamForming.getTeamFormingLogList(courseID)!=null)
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
		
		log.info("Course added: coord: "+ c.getCoordinatorID()
				+ " course id: "+c.getID()
				+ " course name: "+c.getName());

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
	protected void teamFormingSessionAdd() throws IOException {
        String json = req.getParameter("teamformingsession");

        Gson gson = new Gson();
        TeamFormingSession e = gson.fromJson(json, TeamFormingSession.class);

        try{
                TeamForming teamForming = TeamForming.inst();
                teamForming.createTeamFormingSession(e.getCourseID(), e.getStart(), 
                                e.getDeadline(), e.getTimeZone(), e.getGracePeriod(), e.getInstructions(), e.getProfileTemplate());
                resp.getWriter().write("ok");
        }
        catch (TeamFormingSessionExistsException ex){
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
        
        try{
                teamForming.createTeamProfile(courseId, courseName, teamName, teamProfile);
                resp.getWriter().write("ok");
        }
        
        catch (TeamProfileExistsException e){
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
	 * @throws IOException
	 * @throws ServletException
	 */
	protected void activateAutomatedReminder() throws IOException,ServletException{
	        RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/automatedreminders");
	        dispatcher.forward(this.req, this.resp);
		}
	


	private String getCoursesByCoordID(String coordID) {
		String query = "select from " + Course.class.getName() + " where coordinatorID == '" + coordID + "'";

		@SuppressWarnings("unchecked")
		List<Course> courseList = (List<Course>) getPM().newQuery(query).execute();
		String courseIDs = "";
		
		for (Course c : courseList) {
			courseIDs = courseIDs + c.getID()+" ";
		}
		return courseIDs.trim();
	}
	
	private void deleteCourseByIdNonCascade(String courseID) {
		Courses courses = Courses.inst();
		try {
			courses.deleteCoordinatorCourse(courseID);
		} catch (CourseDoesNotExistException e) {
			log.warning("Trying to delete non-existent course "+courseID);
		}
	}
	
	private String getCoordByID(String coordID) {
		Accounts accounts = Accounts.inst();
		Coordinator coord = accounts.getCoordinator(coordID);
		if(coord==null){
			log.warning("Trying to get non-existent coord "+coordID);
		}
		return (new Gson()).toJson(coord);
	}
	
	//TODO: should be named createIfNew
	private void createCoordIfNew(String coordID, String coordName, String coordEmail) {
		Accounts accounts = Accounts.inst();

		try {
			accounts.addCoordinator(coordID, coordName, coordEmail);
		}catch (AccountExistsException e) {
			log.warning("Trying to create a coordinator that already exists: "+coordID);
		}
		
	}
	
	private void deleteCoordByIdNonCascade(String coordId) {
		Accounts accounts = Accounts.inst();
		try {
			accounts.deleteCoordinatorNonCascade(coordId);
		} catch (Exception e) {
			log.warning("problem while trying to delete coordinator"+coordId+"\n error:"+ e.getMessage());
		}
		
	}
	
	private String getCourseById(String courseId) {
		Course course = Courses.inst().getCourse(courseId);
		return new Gson().toJson(course);
	}
	
	//TODO: upgrade this to remove 'IfNew' i.e., overwrite if entity already exists
	private String persistDataBundleIfNew(String dataBundleJsonString) throws CourseInputInvalidException {
		Gson gson = new Gson();
		
		DataBundle data = gson.fromJson(dataBundleJsonString, DataBundle.class);
		HashMap<String, Coordinator> coords = data.coords;
		for (Coordinator coord : coords.values()) {
		    log.info("API Servlet adding coord :"+coord.getGoogleID());
		    createCoordIfNew(coord.getGoogleID(), coord.getName(), coord.getEmail());
		}
		
		HashMap<String, Course> courses = data.courses;
		for (Course course: courses.values()){
			log.info("API Servlet adding course :"+course.getID());
			createCourseIfNew(course.getCoordinatorID(),course.getID(),course.getName());
		}
		return Common.BACKEND_STATUS_SUCCESS;
	}

	private void createCourseIfNew(String coordinatorId, String courseId,
			String courseName) throws CourseInputInvalidException {
		try {
			Courses.inst().addCourse(courseId, courseName, coordinatorId);
		} catch (CourseExistsException e) {
			log.warning("Course already exists :"+courseId);
		} 
		
	}
	

}
