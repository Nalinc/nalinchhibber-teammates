package teammates.testing.testcases;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;

import teammates.api.Common;
import teammates.datatransfer.CoordData;
import teammates.datatransfer.CourseData;
import teammates.datatransfer.DataBundle;
import teammates.datatransfer.EvaluationData;
import teammates.datatransfer.StudentData;
import teammates.jsp.Helper;
import teammates.testing.config.Config;
import teammates.testing.lib.BackDoor;
import teammates.testing.lib.BrowserInstance;
import teammates.testing.lib.BrowserInstancePool;
import teammates.testing.testcases.BaseTestCase;

public class AccessControlUiTest extends BaseTestCase {
	
	private static BrowserInstance bi;
	private static String appUrl = Config.inst().TEAMMATES_URL;
	private static DataBundle dataBundle;
	
	@BeforeClass
	public static void classSetup(){
		printTestClassHeader();
		bi = BrowserInstancePool.getBrowserInstance();
		
		startRecordingTimeForDataImport();
		dataBundle = getTypicalDataBundle();
		
		//assign test user IDs to existing student and coord
		StudentData student = dataBundle.students.get("student1InCourse1");
		student.id = Config.inst().TEST_STUDENT_ACCOUNT;
		
		CoordData coord = dataBundle.coords.get("typicalCoord1");
		
		//remove existing data under the coord before changing his id
		BackDoor.deleteCoord(coord.id);
		
		//reassign courses to new coord id
		String idOfTestCoord = Config.inst().TEST_COORD_ACCOUNT;
		
		for(CourseData cd: dataBundle.courses.values()){
			if(cd.coord.equals(coord.id)){
				cd.coord = idOfTestCoord;
			}
		}
		
		coord.id = idOfTestCoord;
		
		String backDoorOperationStatus = BackDoor.restoreNewDataBundle(Common.getTeammatesGson().toJson(dataBundle));
		assertEquals(Common.BACKEND_STATUS_SUCCESS, backDoorOperationStatus);
		reportTimeForDataImport();
		
		bi.logout();
	}
	
	@AfterClass
	public static void classTearDown() throws Exception {
		BrowserInstancePool.release(bi);
		printTestClassFooter();
	}
	
	@Test
	public void testUserNotLoggedIn() throws Exception{
		
		bi.logout();
		bi.verifyCurrentPageHTML(Common.TEST_PAGES_FOLDER+"/login.html");
		
		______TS("student");
		
		verifyRedirectToLogin(Common.PAGE_STUDENT_HOME);
		verifyRedirectToLogin(Common.PAGE_STUDENT_JOIN_COURSE);
		verifyRedirectToLogin(Common.PAGE_STUDENT_COURSE_DETAILS);
		verifyRedirectToLogin(Common.PAGE_STUDENT_EVAL_SUBMISSION_EDIT);
		verifyRedirectToLogin(Common.PAGE_STUDENT_EVAL_RESULTS);
		
		______TS("coord");
		
		verifyRedirectToLogin(Common.PAGE_COORD_HOME);
		verifyRedirectToLogin(Common.PAGE_COORD_COURSE);
		verifyRedirectToLogin(Common.PAGE_COORD_COURSE_DELETE);
		verifyRedirectToLogin(Common.PAGE_COORD_COURSE_DETAILS);
		verifyRedirectToLogin(Common.PAGE_COORD_COURSE_ENROLL);
		verifyRedirectToLogin(Common.PAGE_COORD_COURSE_REMIND);
		verifyRedirectToLogin(Common.PAGE_COORD_COURSE_STUDENT_DELETE);
		verifyRedirectToLogin(Common.PAGE_COORD_COURSE_STUDENT_DETAILS);
		verifyRedirectToLogin(Common.PAGE_COORD_COURSE_STUDENT_EDIT);
		verifyRedirectToLogin(Common.PAGE_COORD_EVAL);
		verifyRedirectToLogin(Common.PAGE_COORD_EVAL_EDIT);
		verifyRedirectToLogin(Common.PAGE_COORD_EVAL_DELETE);
		verifyRedirectToLogin(Common.PAGE_COORD_EVAL_REMIND);
		verifyRedirectToLogin(Common.PAGE_COORD_EVAL_RESULTS);
		verifyRedirectToLogin(Common.PAGE_COORD_EVAL_PUBLISH);
		verifyRedirectToLogin(Common.PAGE_COORD_EVAL_UNPUBLISH);
		verifyRedirectToLogin(Common.PAGE_COORD_EVAL_SUBMISSION_VIEW);
		verifyRedirectToLogin(Common.PAGE_COORD_EVAL_SUBMISSION_EDIT);
		
	}
	
	@Test
	public void testUserNotRegistered() throws Exception{
		
		______TS("student");
		
		String unregUsername = Config.inst().TEST_UNREG_ACCOUNT;
		String unregPassword = Config.inst().TEST_UNREG_PASSWORD;
		bi.logout();
		bi.loginStudent(unregUsername, unregPassword);
		
		verifyRedirectToWelcomeStrangerPage(Common.PAGE_STUDENT_HOME, unregUsername);
		verifyRedirectToWelcomeStrangerPage(Common.PAGE_STUDENT_JOIN_COURSE, unregUsername);
		
		verifyRedirectToNotAuthorized(Common.PAGE_STUDENT_COURSE_DETAILS);
		verifyRedirectToNotAuthorized(Common.PAGE_STUDENT_EVAL_SUBMISSION_EDIT);
		verifyRedirectToNotAuthorized(Common.PAGE_STUDENT_EVAL_RESULTS);
		
		______TS("coord");
		
		bi.logout();
		bi.loginCoord(unregUsername, unregPassword);
		
		verifyRedirectToNotAuthorized(Common.PAGE_COORD_HOME);
		verifyRedirectToNotAuthorized(Common.PAGE_COORD_COURSE);
		verifyRedirectToNotAuthorized(Common.PAGE_COORD_COURSE_DELETE);
		verifyRedirectToNotAuthorized(Common.PAGE_COORD_COURSE_DETAILS);
		verifyRedirectToNotAuthorized(Common.PAGE_COORD_COURSE_ENROLL);
		verifyRedirectToNotAuthorized(Common.PAGE_COORD_COURSE_REMIND);
		verifyRedirectToNotAuthorized(Common.PAGE_COORD_COURSE_STUDENT_DELETE);
		verifyRedirectToNotAuthorized(Common.PAGE_COORD_COURSE_STUDENT_DETAILS);
		verifyRedirectToNotAuthorized(Common.PAGE_COORD_COURSE_STUDENT_EDIT);
		verifyRedirectToNotAuthorized(Common.PAGE_COORD_EVAL);
		verifyRedirectToNotAuthorized(Common.PAGE_COORD_EVAL_EDIT);
		verifyRedirectToNotAuthorized(Common.PAGE_COORD_EVAL_DELETE);
		verifyRedirectToNotAuthorized(Common.PAGE_COORD_EVAL_REMIND);
		verifyRedirectToNotAuthorized(Common.PAGE_COORD_EVAL_RESULTS);
		verifyRedirectToNotAuthorized(Common.PAGE_COORD_EVAL_PUBLISH);
		verifyRedirectToNotAuthorized(Common.PAGE_COORD_EVAL_UNPUBLISH);
		verifyRedirectToNotAuthorized(Common.PAGE_COORD_EVAL_SUBMISSION_VIEW);
		verifyRedirectToNotAuthorized(Common.PAGE_COORD_EVAL_SUBMISSION_EDIT);
		
	}
	
	@Test
	public void testStudentAccessControl() throws Exception{
		
		String studentUsername = Config.inst().TEST_STUDENT_ACCOUNT;
		String studentPassword = Config.inst().TEST_STUDENT_PASSWORD;
		
		
		bi.loginStudent(studentUsername, studentPassword);
		
		verifyPageContains(Common.PAGE_STUDENT_HOME, studentUsername+"{*}Student Home{*}View Team");
		verifyPageContains(Common.PAGE_STUDENT_JOIN_COURSE, studentUsername+"{*}Student Home{*}View Team");
		
		______TS("student can view details of a student's own course");
		
		String link = Common.PAGE_STUDENT_COURSE_DETAILS;
		String idOfOwnCourse = "idOfCourse1OfCoord1";
		link = Helper.addParam(link, Common.PARAM_COURSE_ID, idOfOwnCourse);
		verifyPageContains(link, studentUsername+"{*}Team Details for "+idOfOwnCourse);
		
		______TS("student cannot view details of a course she is not registered for");

		link = Common.PAGE_STUDENT_COURSE_DETAILS;
		CourseData otherCourse = dataBundle.courses.get("course1OfCoord2");
		link = Helper.addParam(link, Common.PARAM_COURSE_ID, otherCourse.id);
		verifyRedirectToNotAuthorized(link);
		
		______TS("student cannot view course details while masquerading as a student in that course");
		
		StudentData otherStudent = dataBundle.students.get("student1InCourse2");
		//ensure other student belong to other course
		assertEquals(otherStudent.course, otherCourse.id); 
		link = Helper.addParam(link, Common.PARAM_USER_ID , otherStudent.id);
		verifyRedirectToNotAuthorized(link);
		
		______TS("student can view own evaluation submission page");
		
		link = Common.PAGE_STUDENT_EVAL_SUBMISSION_EDIT;
		link = Helper.addParam(link, Common.PARAM_COURSE_ID, idOfOwnCourse);
		EvaluationData ownEvaluation = dataBundle.evaluations.get("evaluation1InCourse1OfCoord1");
		link = Helper.addParam(link, Common.PARAM_EVALUATION_NAME, ownEvaluation.name);
		verifyPageContains(link, studentUsername+"{*}Evaluation Submission{*}"+idOfOwnCourse+"{*}"+ownEvaluation.name);
		
		______TS("student cannot submit evaluation after closing");
		
		ownEvaluation.endTime = Common.getDateOffsetToCurrentTime(-1);
		String backDoorOperationStatus = BackDoor.editEvaluation(ownEvaluation);
		assertEquals(Common.BACKEND_STATUS_SUCCESS, backDoorOperationStatus);
		bi.goToUrl(link);
		bi.click(By.id("button_submit"));
		verifyRedirectToNotAuthorized();
		
		______TS("student cannot view own evaluation result before publishing");
		
		link = Common.PAGE_STUDENT_EVAL_RESULTS;
		link = Helper.addParam(link, Common.PARAM_COURSE_ID, idOfOwnCourse);
		link = Helper.addParam(link, Common.PARAM_EVALUATION_NAME, ownEvaluation.name);
		verifyRedirectToNotAuthorized(link);
		
		______TS("student can view own evaluation result after publishing");
		
		ownEvaluation.published = true;
		backDoorOperationStatus = BackDoor.editEvaluation(ownEvaluation);
		assertEquals(Common.BACKEND_STATUS_SUCCESS, backDoorOperationStatus);
		verifyPageContains(link, studentUsername+"{*}Evaluation Results{*}"+ownEvaluation.name+"{*}"+idOfOwnCourse);
		
	}

	@Test
	public void testCoordAccessControl() throws Exception{
		
		String coordUsername = Config.inst().TEST_COORD_ACCOUNT;
		String coordPassword = Config.inst().TEST_COORD_PASSWORD;
		
		CoordData otherCoord = dataBundle.coords.get("typicalCoord2");
		
		CourseData ownCourse = dataBundle.courses.get("course1OfCoord1");
		CourseData otherCourse = dataBundle.courses.get("course1OfCoord2");
		
		bi.loginCoord(coordUsername, coordPassword);
		
		______TS("can view own homepage");
		
		String link = Common.PAGE_COORD_HOME;
		verifyPageContains(link, coordUsername+"{*}Coordinator Home{*}"+ownCourse.id);
		
		______TS("cannot view other homepage");
		
		link = Helper.addParam(link, Common.PARAM_USER_ID , otherCoord.id);
		verifyRedirectToNotAuthorized(link);
		
		______TS("can view own course page");
		
		link = Common.PAGE_COORD_COURSE;
		verifyPageContains(link, coordUsername+"{*}Add New Course{*}"+ownCourse.id);
		
		______TS("cannot view others course page");
		
		link = Helper.addParam(link, Common.PARAM_USER_ID , otherCoord.id);
		verifyRedirectToNotAuthorized(link);
		
		______TS("can view own course details");
		
		link = Common.PAGE_COORD_COURSE_DETAILS;
		link = Helper.addParam(link,Common.PARAM_COURSE_ID, ownCourse.id);
		
		verifyPageContains(link, coordUsername+"{*}Course Details{*}"+ownCourse.id);
	
		______TS("cannot view others course details");
		
		link = Common.PAGE_COORD_COURSE_DETAILS;
		link = Helper.addParam(link,Common.PARAM_COURSE_ID, otherCourse.id);
		verifyRedirectToNotAuthorized(link);
		
		______TS("cannot view others course details by masquerading");
		
		link = Helper.addParam(link, Common.PARAM_USER_ID , otherCoord.id);
		verifyRedirectToNotAuthorized(link);
		
		______TS("can view own ourse enroll page");
		
		link = Common.PAGE_COORD_COURSE_ENROLL;
		link = Helper.addParam(link,Common.PARAM_COURSE_ID, ownCourse.id);
		
		verifyPageContains(link, coordUsername+"{*}Enroll Students for{*}"+ownCourse.id);
	
		______TS("cannot view others course enroll page");
		
		link = Common.PAGE_COORD_COURSE_ENROLL;
		link = Helper.addParam(link,Common.PARAM_COURSE_ID, otherCourse.id);
		bi.goToUrl(link);
		bi.fillString(By.id("enrollstudents"), "t|n|e@g.com|c"); 
		bi.click(By.id("button_enroll"));
		verifyRedirectToNotAuthorized();
		
		______TS("cannot view others course enroll page by masquerading");
		
		link = Helper.addParam(link, Common.PARAM_USER_ID , otherCoord.id);
		bi.goToUrl(link);
		bi.fillString(By.id("enrollstudents"), "t|n|e@g.com|c"); 
		bi.click(By.id("button_enroll"));
		verifyRedirectToNotAuthorized();
		
		//http://localhost:8080/page/coordCourseDelete?courseid=idOfCourse1OfCoord1&next=%2Fpage%2FcoordCourse
//		verifyRedirectToNotAuthorized(Common.PAGE_COORD_COURSE_DELETE);
//		verifyRedirectToNotAuthorized(Common.PAGE_COORD_COURSE_REMIND);
//		verifyRedirectToNotAuthorized(Common.PAGE_COORD_COURSE_STUDENT_DELETE);
//		verifyRedirectToNotAuthorized(Common.PAGE_COORD_COURSE_STUDENT_DETAILS);
//		verifyRedirectToNotAuthorized(Common.PAGE_COORD_COURSE_STUDENT_EDIT);
//		verifyRedirectToNotAuthorized(Common.PAGE_COORD_EVAL);
//		verifyRedirectToNotAuthorized(Common.PAGE_COORD_EVAL_EDIT);
//		verifyRedirectToNotAuthorized(Common.PAGE_COORD_EVAL_DELETE);
//		verifyRedirectToNotAuthorized(Common.PAGE_COORD_EVAL_REMIND);
//		verifyRedirectToNotAuthorized(Common.PAGE_COORD_EVAL_RESULTS);
//		verifyRedirectToNotAuthorized(Common.PAGE_COORD_EVAL_PUBLISH);
//		verifyRedirectToNotAuthorized(Common.PAGE_COORD_EVAL_UNPUBLISH);
//		verifyRedirectToNotAuthorized(Common.PAGE_COORD_EVAL_SUBMISSION_VIEW);
//		verifyRedirectToNotAuthorized(Common.PAGE_COORD_EVAL_SUBMISSION_EDIT);
		
	}

	private void verifyRedirectToWelcomeStrangerPage(String path, String unregUsername) {
		printUrl(appUrl+path);
		bi.goToUrl(appUrl+path);
		//A simple regex check is enough because we do full HTML tests elsewhere
		assertContainsRegex("{*}"+unregUsername+"{*}Welcome stranger{*}", bi.getCurrentPageSource());
	}

	private void verifyRedirectToNotAuthorized() {
		assertContains("You are not authorized to view this page.", bi.getCurrentPageSource());
	}

	private void verifyRedirectToNotAuthorized(String path) {
		printUrl(appUrl+path);
		bi.goToUrl(appUrl+path);
		verifyRedirectToNotAuthorized();
	}
	
	private void verifyPageContains(String path, String targetText) {
		printUrl(appUrl+path);
		bi.goToUrl(appUrl+path);
		assertContainsRegex(targetText, bi.getCurrentPageSource());
	}

	private void verifyRedirectToLogin(String path) {
		printUrl(appUrl+path);
		bi.goToUrl(appUrl+path);
		assertTrue(bi.isLocalLoginPage()||bi.isGoogleLoginPage());
	}

	private void printUrl(String url) {
		print("   "+url);
	}

}
