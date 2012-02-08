package teammates.testing.concurrent;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import teammates.testing.BaseTest2;
import teammates.testing.lib.BrowserInstance;
import teammates.testing.lib.BrowserInstancePool;
import teammates.testing.lib.TMAPI;
import teammates.testing.object.Scenario;

public class CoordEvaluationAddTest extends BaseTest2 {

	static BrowserInstance bi;
	static Scenario scn = setupScenarioInstance("scenario");
	static Scenario scn2 = setupScenarioInstance("scenario");// different course name, same evaluation name

	@BeforeClass
	public static void classSetup() throws Exception {
		System.out.println("========== CoordEvaluation");
		bi = BrowserInstancePool.request();

		TMAPI.cleanupCourse(scn.course.courseId);
		TMAPI.cleanupCourse(scn2.course.courseId);

		TMAPI.createCourse(scn.course);
		TMAPI.createCourse(scn2.course);
		TMAPI.enrollStudents(scn.course.courseId, scn.students);
		TMAPI.enrollStudents(scn2.course.courseId, scn2.students);

		bi.coordinatorLogin(scn.coordinator.username, scn.coordinator.password);
	}

	@AfterClass
	public static void classTearDown() throws Exception {
		bi.logout();
		
		TMAPI.cleanupCourse(scn.course.courseId);
		TMAPI.cleanupCourse(scn2.course.courseId);

		BrowserInstancePool.release(bi);
		System.out.println("CoordEvaluation ==========//");
	}

	@Test
	public void testCoordAddEvaluation() {

		testCoordAddEvaluationSuccessful();
		
		testCoordAddDuplicateEvaluationInDifferentCourseSuccessful();

		testCoordAddDuplicateEvaluationFailed();
		
//		testCoordAddEvaluationWithInvalidInputFailed();

	}

	public void testCoordAddEvaluationSuccessful() {
		
		bi.gotoEvaluations();
		bi.addEvaluation(scn.evaluation);
		bi.justWait();
//		assertEquals(bi.MESSAGE_EVALUATION_ADDED, bi.getElementText(bi.statusMessage));

		bi.clickEvaluationTab();
		bi.verifyEvaluationAdded(scn.evaluation.courseID, scn.evaluation.name, bi.EVAL_STATUS_AWAITING, "0 / " + scn.students.size());
		bi.justWait();
		
		System.out.println("========== testCoordAddEvaluationSuccessful ==========");
	}

	public void testCoordAddDuplicateEvaluationFailed() {
		bi.gotoEvaluations();
		bi.addEvaluation(scn.evaluation);
		bi.justWait();
//		assertEquals(bi.ERROR_MESSAGE_EVALUATION_EXISTS, bi.getElementText(bi.statusMessage));
		System.out.println("========== testCoordAddDuplicateEvaluationFailed ==========");
	}

	public void testCoordAddDuplicateEvaluationInDifferentCourseSuccessful() {
		bi.gotoEvaluations();
		bi.addEvaluation(scn2.evaluation);
		bi.justWait();
//		assertEquals(bi.MESSAGE_EVALUATION_ADDED, bi.getElementText(bi.statusMessage));
		bi.verifyEvaluationAdded(scn2.evaluation.courseID, scn2.evaluation.name, bi.EVAL_STATUS_AWAITING, "0 / " + scn2.students.size());
		System.out.println("========== testCoordAddDuplicateEvaluationInDifferentCourseSuccessful ==========");
	}

	// TODO:
	public void testCoordAddEvaluationWithInvalidInputFailed() {

	}

	// testCoordAddEvaluationWithMissingFieldsFailed

	// testCoordAddEvaluationWithInvalidNameFailed

	// testCoordAddEvaluationWithInvalidTimeFailed

}
