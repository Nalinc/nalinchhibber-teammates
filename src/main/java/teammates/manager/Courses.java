package teammates.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import org.mortbay.log.Log;

import teammates.Datastore;
import teammates.api.EntityAlreadyExistsException;
import teammates.api.EntityDoesNotExistException;
import teammates.api.InvalidParametersException;
import teammates.datatransfer.StudentData;
import teammates.exception.CourseDoesNotExistException;
import teammates.exception.GoogleIDExistsInCourseException;
import teammates.exception.RegistrationKeyInvalidException;
import teammates.exception.RegistrationKeyTakenException;
import teammates.jdo.CourseSummaryForCoordinator;
import teammates.jdo.EnrollmentReport;
import teammates.jdo.EnrollmentStatus;
import teammates.persistent.Course;
import teammates.persistent.Student;

import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;

/**
 * Courses handles all operations related to a Teammates course. This is a
 * static class (singleton).
 * 
 * @author Gerald GOH
 * @see Course
 * @see TeamAllocation
 * 
 */
public class Courses {
	private static Courses instance = null;
	private static final Logger log = Logger.getLogger(Courses.class
			.getSimpleName());

	/**
	 * Constructs a Courses object. Obtains an instance of PersistenceManager
	 * class to handle datastore transactions.
	 */
	private Courses() {
	}

	public PersistenceManager getPM() {
		return Datastore.getPersistenceManager();
	}

	/**
	 * Retrieve singleton instance of Courses
	 * 
	 * @return
	 */
	public static Courses inst() {
		if (instance == null)
			instance = new Courses();
		return instance;
	}

	/**
	 * Adds a Course under a specific Coordinator.
	 * 
	 * @param courseId
	 *            the course ID (Precondition: Must not be null)
	 * 
	 * @param courseName
	 *            the course name (Precondition: Must not be null)
	 * 
	 * @param coordId
	 *            the Google ID of the coordinator (Precondition: Must not be
	 *            null)
	 * @throws InvalidParametersException 
	 * 
	 * @throws EntityAlreadyExistsException
	 *             if a course with the specified ID already exists
	 */
	public void addCourse(String courseId, String courseName, String coordId) throws InvalidParametersException, EntityAlreadyExistsException {
		
		Course course = new Course(courseId, courseName, coordId);

		if (getCourse(courseId) != null) {
			throw new EntityAlreadyExistsException("Course already exists : "+courseId);
		}


		try {
			getPM().makePersistent(course);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Adds new Student objects. New Student objects are based on their e-mails,
	 * which serves as the Primary Key for Student objects.
	 * 
	 * @param studentList
	 *            the list of students to be added (Precondition: Must not be
	 *            null)
	 * 
	 * @return List<EnrollmentReport> a list of reports that confirm which
	 *         students have been added
	 */
	private List<EnrollmentReport> addStudents(List<Student> studentList, String courseID) {
		List<EnrollmentReport> enrollmentReportList = new ArrayList<EnrollmentReport>();
		List<Student> studentListToAdd = new ArrayList<Student>();

		for (Student s : studentList) {

			if (getStudentWithEmail(courseID, s.getEmail()) == null) {
				studentListToAdd.add(s);

				enrollmentReportList.add(new EnrollmentReport(s.getName(), s.getEmail(), EnrollmentStatus.ADDED, false, false, false));
			}
		}

		try {
			getPM().makePersistentAll(studentListToAdd);
		}

		finally {

		}

		return enrollmentReportList;
	}

	/**
	 * Archives a Course for a Coordinator.
	 * 
	 * @param ID
	 *            the course ID (Precondition: Must not be null)
	 */
	public void archiveCoordinatorCourse(String ID) {
		getCourse(ID).setArchived(true);

	}

	/**
	 * Archives a Course for a Student.
	 * 
	 * @param courseID
	 *            the course ID (Precondition: Must not be null)
	 * 
	 * @param googleID
	 *            the Google ID of the student (Precondition: Must not be null)
	 */
	public void archiveStudentCourse(String courseID, String googleID) {
		Student student = getStudentWithID(courseID, googleID);
		student.setCourseArchived(true);
	}

	/**
	 * Deletes a Course object, along with all the Student objects that belong
	 * to the course.
	 * 
	 * @param courseID
	 *            the course ID (Precondition: Must not be null)
	 * 
	 * @throws CourseDoesNotExistException
	 *             if the course with the specified ID cannot be found
	 */
	public void cleanUpCourse(String courseID) throws CourseDoesNotExistException {
		Course course = getCourse(courseID);

		// Check that the course exists
		if (course == null)
			throw new CourseDoesNotExistException();

		try {
			getPM().deletePersistent(course);
			deleteAllStudents(courseID);

		}

		finally {

		}

	}
	
	//TODO: check for existing student and throw exception
	public void createStudent(Student student) throws EntityAlreadyExistsException {
		String courseID = student.getCourseID();
		String email = student.getEmail();
		if(getStudentWithEmail(courseID, email)!=null){
			throw new EntityAlreadyExistsException("This student already existis :"+ courseID + "/" + email);
		}
		getPM().makePersistent(student);
	}


	/**
	 * Clean up courses, evaluations, submissions related to a course
	 * 
	 * @param coordinatorID
	 * @author wangsha
	 * @throws EntityDoesNotExistException 
	 * @throws CourseDoesNotExistException 
	 * @date Sep 8, 2011
	 */
	public void deleteCoordinatorCourses(String coordinatorID){
		List<Course> courses = getCoordinatorCourseList(coordinatorID);
		Iterator<Course> it = courses.iterator();

		while (it.hasNext()) {
			deleteCoordinatorCourse(it.next().getID());

		}

	}

	/**
	 * Deletes a Course object, along with all the Student objects that belong
	 * to the course.
	 * 
	 * @param courseID
	 *            the course ID (Precondition: Must not be null)
	 * 
	 * @throws CourseDoesNotExistException
	 *             if the course with the specified ID cannot be found
	 */
	//TODO: does this actually delete students, as the comment say?
	@Deprecated 
	public void deleteCoordinatorCourse(String courseID){
		Course course = getCourse(courseID);

		if (course == null) {
			String errorMessage = "Trying to delete non-existent course : "
					+ courseID;
			Log.info(errorMessage);
		}

		getPM().deletePersistent(course);

	}
	
	public void deleteCourse(String courseId) {
		Course course = getCourse(courseId);

		if (course == null) {
			String errorMessage = "Trying to delete non-existent course : "
					+ courseId;
			Log.warn(errorMessage);
		}
		
		deleteAllStudents(courseId);
		getPM().deletePersistent(course);
		
	}

	/**
	 * Deletes the Student objects in a particular Course.
	 * 
	 * @param courseID
	 *            the course ID (Precondition: Must not be null)
	 */
	public void deleteAllStudents(String courseID) {
		List<Student> studentList = getStudentList(courseID);
		Log.info("Deleting "+studentList.size()+" students from the course "+courseID);
		getPM().deletePersistentAll(studentList);
	}

	/**
	 * Deletes a Student object from a specific Course.
	 * 
	 * @param courseID
	 *            the course ID (Precondition: Must not be null)
	 * 
	 * @param email
	 *            the email of the student (Precondition: Must not be null)
	 * @throws EntityDoesNotExistException
	 */
	public void deleteStudent(String courseID, String email){
		Student s = getStudentWithEmail(courseID, email);
		if (s == null) {
			String errorMessage = "Trying to delete non-existent student : "
					+ courseID + "/" + email;
			Log.warn(errorMessage);
		} else {
			getPM().deletePersistent(s);
		}
	}

	/**
	 * Deletes a Course from a Student by setting the ID of the Student to an
	 * empty String.
	 * 
	 * @param courseID
	 *            the course ID (Precondition: Must not be null)
	 * 
	 * @param googleID
	 *            the Google ID of the student (Precondition: Must not be null)
	 */
	public void deleteStudentCourse(String courseID, String googleID) {
		Student student = getStudentWithID(courseID, googleID);
		student.setID("");

	}

	/**
	 * Edits a Student object of a specific Course.
	 * 
	 * @param courseID
	 *            the course ID (Precondition: The courseID and email pair must
	 *            be valid)
	 * 
	 * @param email
	 *            the email of the student (Precondition: The courseID and email
	 *            pair must be valid)
	 * 
	 * @param newName
	 *            the new name of the student (Precondition: Must not be null)
	 * 
	 * @param newEmail
	 *            the new email of the student (Precondition: Must not be null)
	 * 
	 * @param newGoogleID
	 *            the new Google ID of the student (Precondition: Must not be
	 *            null)
	 * 
	 * @param newComments
	 *            the new comments of the student (Precondition: Must not be
	 *            null)
	 */
	@Deprecated
	public void editStudent(String courseID, String email, String newName, String newEmail, String newGoogleID, String newComments) {
		Student student = getStudentWithEmail(courseID, email);

		student.setComments((newComments));
		student.setEmail(newEmail);
		student.setID(newGoogleID);
		student.setName(newName);
		
		getPM().close();
	}

	/**
	 * Edits a Student object of a specific Course.
	 * 
	 * @param courseID
	 *            the course ID (Precondition: The courseID and email pair must
	 *            be valid)
	 * 
	 * @param email
	 *            the email of the student (Precondition: The courseID and email
	 *            pair must be valid)
	 * 
	 * @param newName
	 *            the new name of the student (Precondition: Must not be null)
	 * 
	 * @param newTeamName
	 *            the new team name of the student (Precondition: Must not be
	 *            null)
	 * 
	 * @param newEmail
	 *            the new email of the student (Precondition: Must not be null)
	 * 
	 * @param newGoogleID
	 *            the new Google ID of the student (Precondition: Must not be
	 *            null)
	 * 
	 * @param newComments
	 *            the new comments of the student (Precondition: Must not be
	 *            null)
	 */ 
	@Deprecated
	public void editStudent(String courseID, String email, String newName, String newTeamName, String newEmail, String newGoogleID, String newComments) {
		Student student = getStudentWithEmail(courseID, email);

		student.setComments((newComments));
		student.setEmail(newEmail);
		student.setID(newGoogleID);
		student.setName(newName);
		student.setTeamName(newTeamName);
		
		getPM().close();
	}
	
	public void editStudent(String courseID, String email, String newName,
			String newTeamName, String newEmail, String newGoogleID,
			String newComments, Text newProfile)
			throws EntityDoesNotExistException {
		Student student = getStudentWithEmail(courseID, email);
		if (student == null)
			throw new EntityDoesNotExistException("Student " + email
					+ " does not exist in course " + courseID);
		student.setEmail(newEmail);
		student.setName(newName);
		
		if(newComments!=null){
			student.setComments(newComments);
		}
		if (newGoogleID != null) {
			student.setID(newGoogleID);
		}
		if (newTeamName != null) {
			student.setTeamName(newTeamName);
		}
		if(newProfile != null) {
			student.setProfileDetail(newProfile);
		}

		getPM().close();
	}

	/**
	 * Edits existing Student objects based on the list of Student objects
	 * given.
	 * 
	 * @param studentList
	 *            a list of students (Precondition: Must not be null)
	 * 
	 * @return List<EnrollmentReport> reports on which students have been edited
	 */
	private List<EnrollmentReport> editStudents(List<Student> studentList, String courseID) {
		// Acquire Student objects from the datastore
		List<Student> studentListToEdit = new ArrayList<Student>();
		List<Student> studentListToCompareWith = new ArrayList<Student>();
		List<EnrollmentReport> enrollmentReportList = new ArrayList<EnrollmentReport>();

		for (Student s1 : studentList) {
			Student s2 = getStudentWithEmail(courseID, s1.getEmail());

			if (s2 != null) {
				studentListToCompareWith.add(s1);
				studentListToEdit.add(s2);
				enrollmentReportList.add(new EnrollmentReport(s2.getName(), s2.getEmail(), EnrollmentStatus.REMAINED, false, false, false));
			}
		}

		// Edit the acquired Student objects and update the corresponding
		// EnrollmentReport objects
		for (int x = 0; x < studentListToEdit.size(); x++) {
			EnrollmentReport er = enrollmentReportList.get(x);
			Student s = studentListToCompareWith.get(x);
			Student se = studentListToEdit.get(x);

			if (!s.getName().equals(se.getName())) {
				se.setName(s.getName());
				er.setNameEdited(true);
				er.setStatus(EnrollmentStatus.EDITED);
			}

			if (!s.getTeamName().equals(se.getTeamName())) {
				se.setTeamName(s.getTeamName());
				er.setTeamNameEdited(true);
				er.setStatus(EnrollmentStatus.EDITED);
			}

			if (!s.getComments().equals(se.getComments())) {
				se.setComments(studentList.get(x).getComments());
				er.setCommentsEdited(true);
				er.setStatus(EnrollmentStatus.EDITED);
			}
		}

		return enrollmentReportList;
	}

	/**
	 * Performs addition and editing of Student objects. Addition must be done
	 * before editing and then the PersistenceManager should be closed.
	 * 
	 * @param studentList
	 *            a list of students (Precondition: Must not be null)
	 * 
	 * @return List<EnrollmentReport> reports on which students have been added
	 *         or edited
	 */
	public List<EnrollmentReport> enrollStudents(List<Student> studentList, String courseID) {
		List<EnrollmentReport> enrollmentReportList = new ArrayList<EnrollmentReport>();

		enrollmentReportList.addAll(addStudents(studentList, courseID));
		enrollmentReportList.addAll(editStudents(studentList, courseID));

		return enrollmentReportList;
	}

	/**
	 * Returns the list of Course objects of a Coordinator
	 * 
	 * @param coordinatorID
	 *            the Google ID of the coordinator (Precondition: Must not be
	 *            null)
	 * 
	 * @return List<Course> the list of courses of the coordinator
	 */
	public List<Course> getCoordinatorCourseList(String coordinatorID) {
		String query = "select from " + Course.class.getName() + " where coordinatorID == '" + coordinatorID + "'";

		@SuppressWarnings("unchecked")
		List<Course> courseList = (List<Course>) getPM().newQuery(query).execute();
		return courseList;
	}
	
	public HashMap<String, CourseSummaryForCoordinator> getCourseSummaryListForCoord(String coordId){
		List<Course> courseList = getCoordinatorCourseList(coordId);
		HashMap<String, CourseSummaryForCoordinator> courseSummaryList = new HashMap<String, CourseSummaryForCoordinator>();

		for (Course c : courseList) {
			CourseSummaryForCoordinator courseSummary = 
					new CourseSummaryForCoordinator(
							c.getID(), c.getName(), c.isArchived(), 
							getNumberOfTeams(c.getID()), 
							getTotalStudents(c.getID()),
							getUnregistered(c.getID())
						);
			courseSummaryList.put(c.getID(),courseSummary);
		}
		return courseSummaryList;
	}
	
	/**
	 * Returns the Student objects of the specified googleID.
	 * 
	 * @param googleID
	 *            the Google ID of the student (Precondition: Must not be null)
	 * 
	 * @return List<Student> the list of students that have the specified Google
	 *         ID
	 */
	//TODO: The method name does not match intent -damith
	/*
	 * FIXME: This method is to return list of courses for a specific student.
	 * But it returns it as list of Students object (which contains the Course).
	 * It's not very intuitive, probably convert to list of Course? ~Aldrian~
	 */
	public List<Student> getStudentCourseList(String googleID) {
		String query = "select from " + Student.class.getName() + " where ID == \"" + googleID + "\"";

		@SuppressWarnings("unchecked")
		List<Student> studentList = (List<Student>) getPM().newQuery(query).execute();

		return studentList;
	}
	/**
	 * Returns a course.
	 * 
	 * @param courseId
	 *            the course ID (Precondition: Must not be null)
	 * 
	 * @return Course the course that has the specified ID
	 */
	public Course getCourse(String courseId) {
		String query = "select from " + Course.class.getName() + " where ID == \"" + courseId + "\"";

		@SuppressWarnings("unchecked")
		List<Course> courseList = (List<Course>) getPM().newQuery(query).execute();

		if (courseList.isEmpty()){
			String errorMessage = "Trying to get non-existent Course : " + courseId;
			log.warning(errorMessage);
			return null;
		}

		return courseList.get(0);
	}

	/**
	 * Returns the number of teams in a Course.
	 * 
	 * @param courseID
	 *            the course ID (Precondition: Must be valid)
	 * 
	 * @return the number of teams in the course
	 */
	public int getNumberOfTeams(String courseID) {
		List<Student> studentList = getStudentList(courseID);
		List<String> teamNameList = new ArrayList<String>();

		for (Student s : studentList) {
			if (!teamNameList.contains(s.getTeamName())) {
				teamNameList.add(s.getTeamName());
			}
		}

		return teamNameList.size();
	}

	/**
	 * Returns a Student object of the specified courseID and email.
	 * 
	 * @param courseID
	 *            the course ID (Precondition: Must not be null)
	 * 
	 * @param email
	 *            the email of the student (Precondition: Must not be null)
	 * 
	 * @return the student who has the specified email in the specified course
	 */
	public Student getStudentWithEmail(String courseID, String email) {
		String query = "select from " + Student.class.getName() + " where courseID == \"" + courseID + "\" && email == \"" + email + "\"";

		@SuppressWarnings("unchecked")
		List<Student> studentList = (List<Student>) getPM().newQuery(query).execute();

		if (studentList.isEmpty()) {
			return null;
		}

		return studentList.get(0);
	}

	/**
	 * Returns a Student object of the specified courseID and googleID.
	 * 
	 * @param courseID
	 *            the course ID (Precondition: Must not be null)
	 * 
	 * @param googleID
	 *            the Google ID of the student (Precondition: Must not be null)
	 * 
	 * @return Student the student who has the specified Google ID in the
	 *         specified course
	 */
	public Student getStudentWithID(String courseID, String googleID) {
		String query = "select from " + Student.class.getName() + " where courseID == \"" + courseID + "\" && ID == \"" + googleID + "\"";

		@SuppressWarnings("unchecked")
		List<Student> studentList = (List<Student>) getPM().newQuery(query).execute();

		if (studentList.isEmpty()) {
			return null;
		}

		return studentList.get(0);
	}
	


	/**
	 * Returns a list of Student objects that matches the specified courseID.
	 * 
	 * @param courseID
	 *            the course ID (Precondition: Must not be null)
	 * 
	 * @return List<Student> the list of students that are in the course
	 */
	public List<Student> getStudentList(String courseID) {
		String query = "select from " + Student.class.getName() + " where courseID == \'" + courseID + "\'";

		@SuppressWarnings("unchecked")
		List<Student> studentList = (List<Student>) getPM().newQuery(query).execute();

		return studentList;
	}

	/**
	 * Returns the team name of a Student in a particular Course.
	 * 
	 * @param courseID
	 *            the course ID (Precondition: Must not be null)
	 * 
	 * @param email
	 *            the email of the student (Precondition: Must not be null)
	 * 
	 * @return the team name of the student in the course
	 */
	public String getTeamName(String courseID, String email) {
		String query = "select from " + Student.class.getName() + " where courseID == \"" + courseID + "\" && email == \"" + email + "\"";

		@SuppressWarnings("unchecked")
		List<Student> studentList = (List<Student>) getPM().newQuery(query).execute();

		if (studentList.isEmpty()) {
			return null;
		}

		return studentList.get(0).getTeamName();
	}

	/**
	 * Returns the number of students in a Course.
	 * 
	 * @param courseID
	 *            the course ID (Precondition: Must be valid)
	 * 
	 * @return the number of students in the course
	 */
	public int getTotalStudents(String courseID) {
		List<Student> studentList = getStudentList(courseID);
		return studentList.size();
	}

	/**
	 * Returns the number of unregistered students in a Course.
	 * 
	 * @param courseID
	 *            the course ID (Precondition: Must be valid)
	 * 
	 * @return the number of unregistered students in the course
	 */
	public int getUnregistered(String courseID) {
		List<Student> unregistered = getUnregisteredStudentList(courseID);
		return unregistered.size();
	}

	/**
	 * Returns a list of Student objects that matches the specified courseID and
	 * which do not have a Google ID associated with it
	 * 
	 * @param courseID
	 *            the course ID (Precondition: Must not be null)
	 * 
	 * @return List<Student> the list of unregistered students that are in the
	 *         course
	 */
	public List<Student> getUnregisteredStudentList(String courseID) {
		String query = "select from " + Student.class.getName() + " where courseID == \"" + courseID + "\"" + " && ID == \"\"";

		@SuppressWarnings("unchecked")
		List<Student> studentList = (List<Student>) getPM().newQuery(query).execute();

		return studentList;
	}

	/**
	 * Sets the ID of a particular Student object having the specified
	 * registration key.
	 * 
	 * @param registrationKey
	 *            the registration key of the student (Precondition: Must not be
	 *            null)
	 * 
	 * @param googleID
	 *            the Google ID of the student (Precondition: Must not be null)
	 * 
	 * @throws RegistrationKeyInvalidException
	 *             if the registration key does not exist
	 * 
	 * @throws GoogleIDExistsInCourseException
	 *             if the student has already registered in the course
	 * 
	 * @throws RegistrationKeyTakenException
	 *             if the registration key has been used by another student
	 */
	public void joinCourse(String registrationKey, String googleID) throws RegistrationKeyInvalidException, GoogleIDExistsInCourseException, RegistrationKeyTakenException {
		Student student = null;

		try {
			student = getPM().getObjectById(Student.class, KeyFactory.stringToKey(registrationKey));
		}

		catch (Exception e) {
			throw new RegistrationKeyInvalidException();
		}

		List<Student> studentList = getStudentCourseList(googleID);

		for (Student s : studentList) {
			if (s.getCourseID().equals(student.getCourseID())) {
				throw new GoogleIDExistsInCourseException();
			}
		}

		if (!student.getID().equals("")) {
			throw new RegistrationKeyTakenException();
		}

		student.setID(googleID);
	}

	/**
	 * Sends registration keys to Students.
	 * 
	 * @param studentList
	 *            the list of students to send registration keys to
	 *            (Precondition: Must not be null)
	 * 
	 * @param courseID
	 *            the course ID (Precondition: Must not be null)
	 * 
	 * @param courseName
	 *            the course name (Precondition: Must not be null)
	 * 
	 * @param coordinatorName
	 *            the name of the coordinator (Precondition: Must not be null)
	 */
	public void sendRegistrationKeys(List<Student> studentList, String courseID, String courseName, String coordinatorName, String coordinatorEmail) {
		Queue queue = QueueFactory.getQueue("email-queue");
		List<TaskOptions> taskOptionsList = new ArrayList<TaskOptions>();

		for (Student s : studentList) {
			// There is a limit of 100 tasks per batch addition to Queue in
			// Google App
			// Engine
			if (taskOptionsList.size() == 100) {
				queue.add(taskOptionsList);
				taskOptionsList = new ArrayList<TaskOptions>();
			}

			TaskOptions emailTask = TaskOptions.Builder.withUrl("/email").param("operation", "sendregistrationkey").param("email", s.getEmail())
					.param("regkey", KeyFactory.createKeyString(Student.class.getSimpleName(), s.getRegistrationKey())).param("courseid", courseID).param("coursename", courseName)
					.param("name", s.getName()).param("coordinatorname", coordinatorName).param("coordinatoremail", coordinatorEmail);
			taskOptionsList.add(emailTask);
		}

		if (!taskOptionsList.isEmpty()) {
			queue.add(taskOptionsList);
		}

	}

	/**
	 * Unarchives a Course belonging to a Coordinator.
	 * 
	 * @param ID
	 *            the course ID (Precondition: Must be valid)
	 */
	public void unarchiveCoordinatorCourse(String ID) {
		getCourse(ID).setArchived(false);
	}

	/**
	 * Unarchives a Course belonging to a Student.
	 * 
	 * @param courseID
	 *            the course ID (Precondition: The courseID and email pair must
	 *            be valid)
	 * 
	 * @param email
	 *            the email of the student (Precondition: The courseID and email
	 *            pair must be valid)
	 */
	public void unarchiveStudentCourse(String courseID, String email) {
		Student student = getStudentWithEmail(courseID, email);
		student.setCourseArchived(false);

	}

	/**
	 * Retrieve all courses
	 * 
	 * @return
	 * @author huy
	 */
	@SuppressWarnings("unchecked")
	public List<Course> getAllCourses() {
		return (List<Course>) getPM().newQuery(Course.class).execute();
	}
	
	public static void sortByTeamName(List<StudentData> students) {
		Collections.sort(students, new Comparator<StudentData>() {
			public int compare(StudentData s1, StudentData s2) {
				String t1 = s1.team;
				String t2 = s2.team;
				if ((t1 == null) && (t2==null)){
					return 0;
				}else if (t1==null){
					return 1;
				}else if (t2==null){
					return -1;
				}
				return t1.compareTo(t2);
			}
		});
	}

}