package teammates.logic.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import javax.mail.internet.MimeMessage;

import teammates.common.datatransfer.*;
import teammates.common.exception.*;
import teammates.common.util.Assumption;
import teammates.common.util.Utils;

import teammates.logic.core.AccountsLogic;
import teammates.logic.core.CoursesLogic;
import teammates.logic.core.Emails;
import teammates.logic.core.EvaluationsLogic;
import teammates.logic.core.FeedbackQuestionsLogic;
import teammates.logic.core.FeedbackResponsesLogic;
import teammates.logic.core.FeedbackSessionsLogic;
import teammates.logic.core.InstructorsLogic;
import teammates.logic.core.StudentsLogic;
import teammates.logic.core.SubmissionsLogic;

/**
 * This class represents the API to the business logic of the system. Please
 * refer to DevMan for general policies followed by Logic. As those policies
 * cover most of the behavior of the API, we use very short comments to describe
 * operations here.
 * Logic class is a Facade class. It simply forwards the method to internal classes.
 */
public class Logic {
	
	//TODO: sanitizes values received from outside.

	@SuppressWarnings("unused")
	private static Logger log = Utils.getLogger();

	//TODO: remove this constant
	public static final String ERROR_NULL_PARAMETER = "The supplied parameter was null\n";
	
	protected static GateKeeper gateKeeper = GateKeeper.inst();
	protected static Emails emailManager = new Emails();
	protected static AccountsLogic accountsLogic = AccountsLogic.inst();
	protected static StudentsLogic studentsLogic = StudentsLogic.inst();
	protected static InstructorsLogic instructorsLogic = InstructorsLogic.inst();
	protected static CoursesLogic coursesLogic = CoursesLogic.inst();
	protected static EvaluationsLogic evaluationsLogic = EvaluationsLogic.inst();
	protected static SubmissionsLogic submissionsLogic = SubmissionsLogic.inst();
	protected static FeedbackSessionsLogic feedbackSessionsLogic = FeedbackSessionsLogic.inst();
	protected static FeedbackQuestionsLogic feedbackQuestionsLogic = FeedbackQuestionsLogic.inst();
	protected static FeedbackResponsesLogic feedbackResponsesLogic = FeedbackResponsesLogic.inst();

	@SuppressWarnings("unused")
	private void ____USER_level_methods__________________________________() {
	}

	/**
	 * Produces the URL the user should use to login to the system
	 * 
	 * @param redirectUrl
	 *            This is the URL the user will be directed to after login.
	 */
	public static String getLoginUrl(String redirectUrl) {
		return gateKeeper.getLoginUrl(redirectUrl);
	}

	/**
	 * Produces the URL used to logout the user
	 * 
	 * @param redirectUrl
	 *            This is the URL the user will be directed to after logout.
	 */
	public static String getLogoutUrl(String redirectUrl) {
		return gateKeeper.getLogoutUrl(redirectUrl);
	}

	/**
	 * Verifies if the user is logged into his/her Google account
	 */
	public static boolean isUserLoggedIn() {
		return gateKeeper.isUserLoggedOn();
	}

	/**
	 * @return Returns null if the user is not logged in.
	 */
	public UserType getCurrentUser() {
		return gateKeeper.getCurrentUser();
	}
	
	@SuppressWarnings("unused")
	private void ____ACCOUNT_level_methods____________________________________() {
	}

	/**
	 * Preconditions: <br>
	 * * All parameters are non-null.
	 * 
	 */
	public void createAccount(String googleId, String name, boolean isInstructor,
								String email, String institute) throws InvalidParametersException, EntityAlreadyExistsException {
		
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, googleId);
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, name);
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, isInstructor);
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, email);
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, institute);
		
		AccountAttributes accountToAdd = new AccountAttributes(googleId, name, isInstructor, email, institute);
		
		accountsLogic.createAccount(accountToAdd);
	}
	
	/**
	 * Preconditions: <br>
	 * * All parameters are non-null.
	 */
	public AccountAttributes getAccount(String googleId) {
		
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, googleId);
		
		return accountsLogic.getAccount(googleId);
	}
	
	/**
	 * Preconditions: <br>
	 * * All parameters are non-null.
	 * 
	 * @return Details of accounts with instruction privileges. Returns empty
	 *         list if no such accounts are found.
	 */
	@Deprecated //Not scalable.
	public List<AccountAttributes> getInstructorAccounts() {
		
		return accountsLogic.getInstructorAccounts();
	}
	
	/**
	 * Preconditions: <br>
	 * * All parameters are non-null.<br>
	 * * {@code newAccountAttributes} represents an existing account.
	 */
	public void updateAccount(AccountAttributes newAccountAttributes) throws InvalidParametersException {
		
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, newAccountAttributes);
		
		accountsLogic.updateAccount(newAccountAttributes);
	}
	
	/**
	 * Deletes both instructor and student privileges.
	 * Does not delete courses. Can result in orphan courses 
	 * (to be rectified in future).
	 * Fails silently if no such account. <br>
	 * Preconditions: <br>
	 * * All parameters are non-null.
	 */
	public void deleteAccount(String googleId) {
		
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, googleId);
		
		accountsLogic.deleteAccountCascade(googleId);
	}

	@SuppressWarnings("unused")
	private void ____INSTRUCTOR_level_methods____________________________________() {
	}

	/**
	 * Creates an account and an instructor. <br>
	 * Preconditions: <br>
	 * * All parameters are non-null.
	 */
	public void createInstructorAccount(String googleId, String courseId, String name, String email, String institute)
			throws EntityAlreadyExistsException, InvalidParametersException {
		
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, googleId);
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, courseId);
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, name);
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, email);
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, institute);

		accountsLogic.createInstructorAccount(googleId, courseId, name, email, institute);
	}

	/**
	 * Preconditions: <br>
	 * * All parameters are non-null.
	 * @return null if not found.
	 */
	public InstructorAttributes getInstructorForGoogleId(String courseId, String googleId) {
		
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, googleId);
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, courseId);

		return instructorsLogic.getInstructorForGoogleId(courseId, googleId);
	}
	
	/**
	 * Preconditions: <br>
	 * * All parameters are non-null. 
	 * @return Empty list if none found.
	 */
	public List<InstructorAttributes> getInstructorsForGoogleId(String googleId) {
		
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, googleId);
		
		return instructorsLogic.getInstructorsForGoogleId(googleId);
	}

	/**
	 * Preconditions: <br>
	 * * All parameters are non-null. 
	 * @return Empty list if none found.
	 */
	public List<InstructorAttributes> getInstructorsForCourse(String courseId) {
		
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, courseId);
		
		return instructorsLogic.getInstructorsForCourse(courseId);
	}

	/**
	 * @deprecated Not scalable. Don't use unless in admin features.
	 */
	@Deprecated
	public List<InstructorAttributes> getAllInstructors() {
		
		return instructorsLogic.getAllInstructors();
	}
	
	/**
	 * @return true if this user has instructor privileges.
	 */
	public boolean isInstructor(String googleId) {
		return accountsLogic.isAccountAnInstructor(googleId);
	}

	public boolean isInstructorOfCourse(String googleId, String courseId) {
		return instructorsLogic.isInstructorOfCourse(googleId, courseId);
	}

	/**
	 * Preconditions: <br>
	 * * All parameters are non-null. 
	 */
	public void updateInstructor(InstructorAttributes instructor) 
			throws InvalidParametersException {
		
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, instructor);
		
		instructorsLogic.updateInstructor(instructor);
	}

	/**
	 * Preconditions: <br>
	 * * All parameters are non-null.
	 * @throws EntityDoesNotExistException 
	 */
	public void updateCourseInstructors(String courseId, String instructorLines, String courseInstitute) 
			throws InvalidParametersException, EntityDoesNotExistException {
		
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, courseId);
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, instructorLines);
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, courseInstitute);
	
		coursesLogic.verifyCourseIsPresent(courseId);
		
		instructorsLogic.updateCourseInstructors(courseId, instructorLines, courseInstitute); 
		
	}

	/**
	 * Removes instructor access but does not delete the account. 
	 * The account will continue to have student access. <br>
	 * Fails silently if no match found.<br>
	 * Preconditions: <br>
	 * * All parameters are non-null. 
	 */
	public void downgradeInstructorToStudentCascade(String googleId) {
	
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, googleId);
		
		accountsLogic.downgradeInstructorToStudentCascade(googleId);
	}

	/**
	 * Fails silently if no match found.
	 * Preconditions: <br>
	 * * All parameters are non-null. 
	 */
	public void deleteInstructor(String courseId, String googleId) {
		
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, googleId);
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, courseId);

		instructorsLogic.deleteInstructor(courseId, googleId);
	}

	@SuppressWarnings("unused")
	private void ____COURSE_level_methods__________________________________() {
	}

	/**
	 * Creates a course and an instructor for it. <br>
	 * Preconditions: <br>
	 * * All parameters are non-null. <br>
	 * * {@code instructorGoogleId} already has instructor privileges.
	 */
	public void createCourseAndInstructor(String instructorGoogleId, String courseId, String courseName) 
			throws EntityAlreadyExistsException, InvalidParametersException {
		
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, instructorGoogleId);
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, courseId);
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, courseName);

		coursesLogic.createCourseAndInstructor(instructorGoogleId, courseId, courseName);
	}
	
	/**
	 * Preconditions: <br>
	 * * All parameters are non-null.
	 * @return null if not found.
	 */
	public CourseAttributes getCourse(String courseId) {
		
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, courseId);
		
		return coursesLogic.getCourse(courseId);
	}
	
	
	/**
	 * Returns a detailed version of course data, including evaluation data. <br>
	 * Preconditions: <br>
	 * * All parameters are non-null.
	 */
	public CourseDetailsBundle getCourseDetails(String courseId)
			throws EntityDoesNotExistException {
		
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, courseId);

		return coursesLogic.getCourseDetails(courseId);

	}

	/**
	 * Preconditions: <br>
	 * * All parameters are non-null.
	 */
	public List<CourseAttributes> getCoursesForStudentAccount(String googleId)
			throws EntityDoesNotExistException, InvalidParametersException {
		
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, googleId);
	
		return coursesLogic.getCoursesForStudentAccount(googleId);
	}

	/**
	 * Preconditions: <br>
	 * * All parameters are non-null. 
	 * @return A less deatailed version of courses for this instructor. 
	 *   Returns an empty list if none found.
	 */
	public HashMap<String, CourseDetailsBundle> getCourseSummariesForInstructor(String googleId) 
			throws EntityDoesNotExistException {
		
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, googleId);
	
		instructorsLogic.verifyInstructorExists(googleId);
	
		return coursesLogic.getCourseSummariesForInstructor(googleId);
	}

	/**
	 * Preconditions: <br>
	 * * All parameters are non-null. 
	 * @return A more deatailed version of courses for this instructor. 
	 *   Returns an empty list if none found.
	 */
	public HashMap<String, CourseDetailsBundle> getCourseDetailsListForInstructor(
			String instructorId) throws EntityDoesNotExistException {
		
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, instructorId);
		
		instructorsLogic.verifyInstructorExists(instructorId);
		
		return coursesLogic.getCoursesDetailsForInstructor(instructorId);
	
	}

	/**
	 * Preconditions: <br>
	 * * All parameters are non-null.
	 * 
	 * @return Details of courses the student is in. CourseData objects
	 *         returned contain details of evaluations too (except the ones
	 *         still AWAITING).
	 */
	public List<CourseDetailsBundle> getCourseDetailsListForStudent(String googleId)
			throws EntityDoesNotExistException {
		
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, googleId);
	
		return coursesLogic.getCourseDetailsListForStudent(googleId);
	
	}

	public void updateCourse(CourseAttributes course) throws NotImplementedException {
		throw new NotImplementedException("Not implemented because we do "
				+ "not allow editing courses");
	}
	
	
	/**
	 * Deletes the course and all data related to the course 
	 * (instructors, students, evaluations).
	 * Fails silently if no such account. <br>
	 * Preconditions: <br>
	 * * All parameters are non-null.
	 */
	public void deleteCourse(String courseId) {
		
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, courseId);

		coursesLogic.deleteCourseCascade(courseId);
	}

	@SuppressWarnings("unused")
	private void ____STUDENT_level_methods__________________________________() {
	}

	/**
	 * Creates a student and adjust existing evaluations to accommodate the new student. <br> 
	 * Preconditions: <br>
	 * * All parameters are non-null.
	 */
	public void createStudent(StudentAttributes student)
			throws EntityAlreadyExistsException, InvalidParametersException {
		
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, student);

		//TODO: push the cascade logic to studentLogic.createStudent
		studentsLogic.createStudentCascade(student);

	}

	/**
	 * Preconditions: <br>
	 * * All parameters are non-null.
	 * 
	 * @return Null if no match found.
	 */
	public StudentAttributes getStudentForEmail(String courseId, String email) {
		
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, courseId);
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, email);

		return studentsLogic.getStudentForEmail(courseId, email);
	}

	
	/**
	 * Preconditions: <br>
	 * * All parameters are non-null.
	 * 
	 * @return Null if no match found.
	 */
	public StudentAttributes getStudentForGoogleId(String courseId, String googleId) {
		
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, courseId);
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, googleId);
	
		return studentsLogic.getStudentForGoogleId(courseId, googleId);
	}

	/**
	 * Preconditions: <br>
	 * * All parameters are non-null.
	 * 
	 * @return Empty list if no match found.
	 */
	public List<StudentAttributes> getStudentsForGoogleId(String googleId) {
		
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, googleId);
		
		return studentsLogic.getStudentsForGoogleId(googleId);
	}

	
	/**
	 * Preconditions: <br>
	 * * All parameters are non-null.
	 * @return Empty list if none found.
	 */
	public List<StudentAttributes> getStudentsForCourse(String courseId)
			throws EntityDoesNotExistException {
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, courseId);
	
		return studentsLogic.getStudentsForCourse(courseId);
	
	}

	/**
	 * Preconditions: <br>
	 * * All parameters are non-null.
	 */
	public CourseDetailsBundle getTeamsForCourse(String courseId)
			throws EntityDoesNotExistException {
		
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, courseId);
	
		return coursesLogic.getTeamsForCourse(courseId);
		
	}

	/**
	 * Preconditions: <br>
	 * * All parameters are non-null.
	 * 
	 * @return null if no match found.
	 */
	public String getKeyForStudent(String courseId, String email) {
		
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, courseId);
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, email);
	
		return studentsLogic.getKeyForStudent(courseId, email);
	}

	/**
	 * All attributes except courseId be changed. Trying to change courseId will
	 * be treated as trying to edit a student in a different course.<br>
	 * Changing team name will not delete existing submissions under that team <br>
	 * Cascade logic: Email changed-> changes email in all existing submissions <br>
	 * Team changed-> creates new submissions for the new team, deletes
	 * submissions for previous team structure. <br>
	 * Preconditions: <br>
	 * * All parameters are non-null.
	 */
	public void updateStudent(String originalEmail, StudentAttributes student)
			throws InvalidParametersException, EntityDoesNotExistException {
		
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, originalEmail);
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, student);

		studentsLogic.updateStudentCascade(originalEmail, student);
	}

	/**
	 * Preconditions: <br>
	 * * All parameters are non-null.
	 */
	public void joinCourse(String googleId, String key)
			throws JoinCourseException, InvalidParametersException, EntityAlreadyExistsException {
		
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, googleId);
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, key);
	
		accountsLogic.joinCourse(key, googleId);
	
	}

	/**
	 * Enrolls new students in the course or modifies existing students. But it
	 * will not delete any students. It will not edit email address either. If
	 * an existing student was enrolled with a different email address, that
	 * student will be treated as a new student.<br>
	 * If there is an error in the enrollLines, there will be no changes to the
	 * datastore <br>
	 * Preconditions: <br>
	 * * All parameters are non-null.
	 * 
	 * @return StudentData objects in the return value contains the status of
	 *         enrollment. It also includes data for other students in the
	 *         course that were not touched by the operation.
	 */
	public List<StudentAttributes> enrollStudents(String enrollLines, String courseId)
			throws EnrollException, EntityDoesNotExistException {
		
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, courseId);
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, enrollLines);
	
		return studentsLogic.enrollStudents(enrollLines, courseId);
	
	}

	/**
	 * Sends the registration invite to unregistered students in the course.
	 * Preconditions: <br>
	 * * All parameters are non-null.
	 * 
	 * @return The list of emails sent. These can be used for
	 *         verification.
	 */
	public List<MimeMessage> sendRegistrationInviteForCourse(String courseId)
			throws InvalidParametersException {
		
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, courseId);
	
		return studentsLogic.sendRegistrationInviteForCourse(courseId);
	}

	/**
	 * Preconditions: <br>
	 * * All parameters are non-null.
	 */
	public MimeMessage sendRegistrationInviteToStudent(String courseId,	String studentEmail) 
			throws EntityDoesNotExistException,	InvalidParametersException {
		
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, courseId);
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, studentEmail);
	
		return studentsLogic.sendRegistrationInviteToStudent(courseId, studentEmail);
	}

	/**
	 * Sends reminders to students who haven't submitted yet. <br>
	 * Preconditions: <br>
	 * * All parameters are non-null. <br>
	 */
	public List<MimeMessage> sendReminderForEvaluation(String courseId,
			String evaluationName) throws EntityDoesNotExistException {
		
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, courseId);
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, evaluationName);
	
		return evaluationsLogic.sendReminderForEvaluation(courseId, evaluationName);
	}

	/**
	 * Deletes the student from the course including any submissions to/from
	 * for this student in this course.
	 * Fails silently if no match found. <br>
	 * Preconditions: <br>
	 * * All parameters are non-null.
	 */
	public void deleteStudent(String courseId, String studentEmail) {
		
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, courseId);
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, studentEmail);

		studentsLogic.deleteStudentCascade(courseId, studentEmail);
	}

	@SuppressWarnings("unused")
	private void ____EVALUATION_level_methods______________________________() {
	}

	/**
	 * Preconditions: <br>
	 * * All parameters are non-null.
	 */
	public void createEvaluation(EvaluationAttributes evaluation)
			throws EntityAlreadyExistsException, InvalidParametersException, EntityDoesNotExistException {
		
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, evaluation);

		evaluationsLogic.createEvaluationCascade(evaluation);
	}

	/**
	 * Preconditions: <br>
	 * * All parameters are non-null.
	 */
	public EvaluationAttributes getEvaluation(String courseId, String evaluationName) {
		
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, courseId);
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, evaluationName);

		return evaluationsLogic.getEvaluation(courseId, evaluationName);
	}

	/**
	 * Preconditions: <br>
	 * * All parameters are non-null. <br>
	 * 
	 * @return Details of Instructor's evaluations. <br>
	 * Returns an empty list if none found.
	 */
	public ArrayList<EvaluationDetailsBundle> getEvaluationsDetailsForInstructor(String instructorId) 
			throws EntityDoesNotExistException {
		
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, instructorId);
	
		instructorsLogic.verifyInstructorExists(instructorId);
	
		return evaluationsLogic.getEvaluationsDetailsForInstructor(instructorId);
	}

	/**
	 * Preconditions: <br>
	 * * All parameters are non-null. 
	 */
	public EvaluationResultsBundle getEvaluationResult(
			String courseId, String evaluationName) 
					throws EntityDoesNotExistException {
		
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, courseId);
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, evaluationName);

		return evaluationsLogic.getEvaluationResult(courseId, evaluationName);

	}

	/**
	 * Generates summary results (without comments) in CSV format. <br>
	 * Preconditions: <br>
	 * * All parameters are non-null. <br>
	 */
	public String getEvaluationResultSummaryAsCsv(String courseId, String evalName) 
			throws EntityDoesNotExistException {
		
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, courseId);
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, evalName);
		
		return evaluationsLogic.getEvaluationResultSummaryAsCsv(courseId, evalName);
	}

	/**
	 * Preconditions: <br>
	 * * All parameters are non-null. <br>
	 * 
	 * @return Returns details of the course's evaluations. <br>
	 */
	public ArrayList<EvaluationDetailsBundle> getEvaluationDetailsForCourse(String courseId)
			throws EntityDoesNotExistException {
		
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, courseId);
	
		return evaluationsLogic.getEvaluationsDetailsForCourse(courseId);
	}

	/**
	 * Preconditions: <br>
	 * * All parameters are non-null.
	 */
	public StudentResultBundle getEvaluationResultForStudent(
			String courseId, String evaluationName, String studentEmail)
			throws EntityDoesNotExistException, InvalidParametersException {
		
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, courseId);
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, evaluationName);
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, studentEmail);
	
		return evaluationsLogic.getEvaluationResultForStudent(courseId, evaluationName, studentEmail);
	}

	/**
	 * Can be used to change instructions, p2pEnabled, start/end times, grace period and time zone. <br>
	 * Preconditions: <br>
	 * * All parameters are non-null.
	 * 
	 */
	public void updateEvaluation(String courseId, String evaluationName,
			String instructions, Date start, Date end, double timeZone,
			int gracePeriod, boolean p2pEnabled)
			throws EntityDoesNotExistException, InvalidParametersException {
		
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, courseId);
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, evaluationName);
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, instructions);
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, start);
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, end);
	
		if (!evaluationsLogic.isEvaluationExists(courseId, evaluationName)) {
			throw new EntityDoesNotExistException("Trying to edit non-existent evaluation " + courseId + "/" + evaluationName);
		}
	
		EvaluationAttributes evaluation = new EvaluationAttributes();
		evaluation.courseId = courseId;
		evaluation.name = evaluationName;
		evaluation.instructions = instructions;
		evaluation.p2pEnabled = p2pEnabled;
		evaluation.startTime = start;
		evaluation.endTime = end;
		evaluation.gracePeriod = gracePeriod;
		evaluation.timeZone = timeZone;
	
		evaluationsLogic.updateEvaluation(evaluation);
	}

	/**
	 * Publishes the evaluation and send email alerts to students.
	 * Preconditions: <br>
	 * * All parameters are non-null. <br>
	 * @throws InvalidParametersException
	 *             if the evaluation is not ready to be published.
	 * @throws EntityDoesNotExistException
	 */
	public void publishEvaluation(String courseId, String evaluationName)
			throws EntityDoesNotExistException, InvalidParametersException {
		
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, courseId);
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, evaluationName);
	
		evaluationsLogic.publishEvaluation(courseId, evaluationName);
		
	}

	/**
	 * Preconditions: <br>
	 * * All parameters are non-null. <br>
	 * @throws InvalidParametersException
	 *             if the evaluation is not ready to be unpublished.
	 */
	public void unpublishEvaluation(String courseId, String evaluationName)
			throws EntityDoesNotExistException, InvalidParametersException {
		
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, courseId);
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, evaluationName);
	
		evaluationsLogic.unpublishEvaluation(courseId, evaluationName);
	}

	/**
	 * Deletes the evaluation and all its submissions.
	 * Preconditions: <br>
	 * * All parameters are non-null.
	 */
	public void deleteEvaluation(String courseId, String evaluationName) {
		
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, courseId);
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, evaluationName);
	
		evaluationsLogic.deleteEvaluationCascade(courseId, evaluationName);
	}

	@SuppressWarnings("unused")
	private void ____SUBMISSION_level_methods_____________________________() {
	}

	public void createSubmission(SubmissionAttributes submission)
			throws NotImplementedException {
		throw new NotImplementedException(
				"Not implemented because submissions "
						+ "are created automatically");
	}

	/**
	 * Preconditions: <br>
	 * * All parameters are non-null. <br>
	 */
	public List<SubmissionAttributes> getSubmissionsForEvaluationFromStudent(String courseId,
			String evaluationName, String reviewerEmail)
			throws EntityDoesNotExistException, InvalidParametersException {
		
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, courseId);
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, evaluationName);
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, reviewerEmail);
	
		return submissionsLogic.getSubmissionsForEvaluationFromStudent(courseId, evaluationName, reviewerEmail);
	
	}

	/**
	 * Preconditions: <br>
	 * * All parameters are non-null.
	 */
	public boolean hasStudentSubmittedEvaluation(
			String courseId, String evaluationName, String studentEmail)
			throws InvalidParametersException {
		
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, courseId);
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, evaluationName);
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, studentEmail);
	
		return submissionsLogic.hasStudentSubmittedEvaluation(
				courseId, evaluationName, studentEmail);
	}

	/**
	 * Preconditions: <br>
	 * * All parameters are non-null. <br>
	 */
	public void updateSubmissions(List<SubmissionAttributes> submissionsList)
			throws EntityDoesNotExistException, InvalidParametersException {
		
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, submissionsList);

		submissionsLogic.updateSubmissions(submissionsList);
	}

	public void deleteSubmission(SubmissionAttributes submission)
			throws NotImplementedException {
		throw new NotImplementedException(
				"Not implemented because submissions "
						+ "are deleted automatically");
	}
	
	@SuppressWarnings("unused")
	private void ____FEEDBACK_SESSION_level_methods_____________________________() {
	}
	
	/**
	 * Preconditions: <br>
	 * * All parameters are non-null.
	 */
	public void createFeedbackSession(FeedbackSessionAttributes feedbackSession)
			throws EntityAlreadyExistsException, InvalidParametersException, EntityDoesNotExistException {
		
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, feedbackSession);

		feedbackSessionsLogic.createFeedbackSession(feedbackSession);
	}
	
	/**
	 * Updates the details of a feedback session <br>
	 * Does not affect the questions and responses associated with it.
	 * Preconditions: <br>
	 * * All parameters are non-null.
	 */
	public void updateFeedbackSession(FeedbackSessionAttributes updatedSession) 
			throws InvalidParametersException, EntityDoesNotExistException {
		
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, updatedSession);

		feedbackSessionsLogic.updateFeedbackSession(updatedSession);
	}
	
	/**
	 * Deletes the feedback session but not the questions and
	 * responses associated to it.
	 * Fails silently if no such feedback session. <br>
	 * Preconditions: <br>
	 * * All parameters are non-null.
	 */
	public void deleteFeedbackSession(String feedbackSessionName, String courseId) {
		
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, feedbackSessionName);
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, courseId);

		feedbackSessionsLogic.deleteFeedbackSessionCascade(feedbackSessionName, courseId);
	}
	
	/**
	 * Preconditions: <br>
	 * * All parameters are non-null.
	 */
	public FeedbackSessionAttributes getFeedbackSession(String feedbackSessionName, String courseId) {
		
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, feedbackSessionName);
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, courseId);
		
		return feedbackSessionsLogic.getFeedbackSession(feedbackSessionName, courseId);
	}
		
	/**
	 * Preconditions: <br>
	 * * All parameters are non-null. <br>
	 * 
	 * @return Details of Instructor's feedback sessions. <br>
	 * Returns an empty list if none found.
	 */
	public List<FeedbackSessionDetailsBundle>
		getFeedbackSessionDetailsForInstructor(String googleId) throws EntityDoesNotExistException{
		
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, googleId);
		
		return feedbackSessionsLogic.getFeedbackSessionDetailsForInstructor(googleId);
	}
	
	/**
	 * Preconditions: <br>
	 * * All parameters are non-null. <br>
	 * 
	 * @return Feedback session information, question + responses bundle for user <br>
	 * Returns an empty list if none found.
	 */
	public FeedbackSessionQuestionsBundle
		getFeedbackSessionQuestionsBundle(String feedbackSessionName, String courseId, String userEmail)
				throws EntityDoesNotExistException{
		
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, feedbackSessionName);
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, courseId);
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, userEmail);
		
		return feedbackSessionsLogic.getFeedbackSessionQuestionsForUser(feedbackSessionName, courseId, userEmail);
	}
	
	/**
	 * Preconditions: <br>
	 * * All parameters are non-null.
	 */
	public boolean hasStudentSubmittedFeedback(
			String courseId, String feedbackSessionName, String studentEmail)
			throws InvalidParametersException, EntityDoesNotExistException {
		
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, courseId);
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, feedbackSessionName);
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, studentEmail);
	
		return feedbackSessionsLogic.isFeedbackSessionCompletedByUser(
				feedbackSessionName, courseId, studentEmail);
	}
	
	@SuppressWarnings("unused")
	private void ____FEEDBACK_QUESTION_level_methods_____________________________() {
	}
	
	/**
	 * Preconditions: <br>
	 * * All parameters are non-null.
	 */
	public void createFeedbackQuestion(FeedbackQuestionAttributes feedbackQuestion)
			throws EntityAlreadyExistsException, InvalidParametersException, EntityDoesNotExistException {
		
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, feedbackQuestion);

		feedbackQuestionsLogic.createFeedbackQuestion(feedbackQuestion);
	}
	
	/**
	 * Updates the details of a Feedback Question.<br>
	 * The FeedbackQuestionAttributes should have the updated attributes
	 * together with the original ID of the question. Preserves null
	 * attributes.
	 * Preconditions: <br>
	 * * All parameters are non-null.
	 */
	public void updateFeedbackQuestion(FeedbackQuestionAttributes updatedQuestion)
			throws InvalidParametersException, EntityDoesNotExistException {

		Assumption.assertNotNull(ERROR_NULL_PARAMETER, updatedQuestion);

		feedbackQuestionsLogic.updateFeedbackQuestion(updatedQuestion);
	}
	
	/**
	 * Deletes the feedback session but not the questions and
	 * responses associated to it.
	 * Fails silently if no such feedback session. <br>
	 * Preconditions: <br>
	 * * All parameters are non-null.
	 */
	public void deleteFeedbackQuestion(String feedbackSessionName, String courseId, int questionNumber) {
		
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, feedbackSessionName);
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, courseId);
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, questionNumber);

		feedbackQuestionsLogic.deleteFeedbackQuestionCascade(feedbackSessionName, courseId, questionNumber);
	}
	
	/**
	 * Gets all questions for a feedback session.<br>
	 * Returns an empty list if they are no questions
	 * for the session.
	 * Preconditions: <br>
	 * * All parameters are non-null.
	 */
	public List<FeedbackQuestionAttributes> getFeedbackQuestionsForSession(
			String feedbackSessionName, String courseId) throws EntityDoesNotExistException {
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, feedbackSessionName);
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, courseId);
		
		return feedbackQuestionsLogic.getFeedbackQuestionsForSession(feedbackSessionName, courseId);
	}
	
	/**
	 * Gets all questions for a feedback session that a user can respond to.<br>
	 * Returns an empty list if they are no such questions for the session.
	 * Preconditions: <br>
	 * * All parameters are non-null.
	 */
	public List<FeedbackQuestionAttributes> getFeedbackQuestionsForUser(
			String feedbackSessionName, String courseId, String userEmail) throws EntityDoesNotExistException {
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, feedbackSessionName);
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, courseId);
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, userEmail);
		
		return feedbackQuestionsLogic.getFeedbackQuestionsForUser(feedbackSessionName, courseId, userEmail);
	}
	
	/**
	 * Gets a question+response bundle for questions with responses that
	 * is visible to the user for a feedback session.
	 * Preconditions: <br>
	 * * All parameters are non-null.
	 */
	public FeedbackSessionResultsBundle getFeedbackSessionResultsForUser(
			String feedbackSessionName, String courseId, String userEmail)
					throws UnauthorizedAccessException, EntityDoesNotExistException {
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, feedbackSessionName);
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, courseId);
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, userEmail);
		
		return feedbackSessionsLogic.getFeedbackSessionResultsForUser(feedbackSessionName, courseId, userEmail);
	}
	
	@SuppressWarnings("unused")
	private void ____FEEDBACK_RESPONSE_level_methods_____________________________() {
	}
	
	/**
	 * Preconditions: <br>
	 * * All parameters are non-null.
	 */
	public void createFeedbackResponse(FeedbackResponseAttributes feedbackResponse)
			throws EntityAlreadyExistsException, InvalidParametersException {
		
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, feedbackResponse);

		feedbackResponsesLogic.createFeedbackResponse(feedbackResponse);
	}
	
	/**
	 * Preconditions: <br>
	 * * All parameters are non-null.
	 */
	public void updateFeedbackResponse(FeedbackResponseAttributes feedbackResponse)
			throws InvalidParametersException, EntityDoesNotExistException {
		
		Assumption.assertNotNull(ERROR_NULL_PARAMETER, feedbackResponse);

		feedbackResponsesLogic.updateFeedbackResponse(feedbackResponse);
	}
	
	@SuppressWarnings("unused")
	private void ____MISC_methods__________________________________________() {
	}

	public MimeMessage emailErrorReport(String path, String params,	Throwable error) {
		return emailManager.sendErrorReport(path, params, error);
	}

	@SuppressWarnings("unused")
	private void ____helper_methods________________________________________() {
	}
}
