package teammates.test.automated;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.internet.MimeMessage;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.appengine.api.taskqueue.dev.LocalTaskQueueCallback;
import com.google.appengine.api.urlfetch.URLFetchServicePb.URLFetchRequest;

import teammates.common.datatransfer.DataBundle;
import teammates.common.datatransfer.EvaluationAttributes;
import teammates.common.datatransfer.EvaluationAttributes.EvalStatus;
import teammates.common.util.Const;
import teammates.common.util.HttpRequestHelper;
import teammates.common.util.TimeHelper;
import teammates.common.util.Const.ParamsNames;
import teammates.logic.automated.EmailAction;
import teammates.logic.automated.EvaluationOpeningMailAction;
import teammates.logic.core.Emails;
import teammates.logic.core.EvaluationsLogic;
import teammates.logic.core.InstructorsLogic;
import teammates.logic.core.StudentsLogic;
import teammates.logic.core.Emails.EmailType;
import teammates.test.cases.BaseComponentUsingEmailQueueTestCase;

public class EvaluationOpeningReminderTest extends BaseComponentUsingEmailQueueTestCase {
	
	private static final EvaluationsLogic evaluationsLogic = EvaluationsLogic.inst();
	private static final StudentsLogic studentsLogic = new StudentsLogic();
	private static final InstructorsLogic instructorsLogic = new InstructorsLogic();
	
	@SuppressWarnings("serial")
	public static class EvaluationOpeningCallback implements LocalTaskQueueCallback {
		private static int taskCount;
		
		@Override
		public int execute(URLFetchRequest request) {
			
			HashMap<String, String> paramMap = HttpRequestHelper.getParamMap(request);
			
			assertTrue(paramMap.containsKey(ParamsNames.EMAIL_TYPE));
			
			EmailType typeOfMail = EmailType.valueOf((String) paramMap.get(ParamsNames.EMAIL_TYPE));
			assertEquals(EmailType.EVAL_OPENING, typeOfMail);
			
			assertTrue(paramMap.containsKey(ParamsNames.EMAIL_EVAL));
			String evaluation = (String) paramMap.get(ParamsNames.EMAIL_EVAL); 
			assertTrue(evaluation.equals("new-eval-in-course-1-tARE") || 
					   evaluation.equals("new-evaluation-in-course-2-tARE"));
			
			assertTrue(paramMap.containsKey(ParamsNames.EMAIL_COURSE));
			String courseId = (String) paramMap.get(ParamsNames.EMAIL_COURSE);
			assertTrue(courseId.equals("idOfTypicalCourse1") || 
					   courseId.equals("idOfTypicalCourse2"));
			
			EvaluationOpeningCallback.taskCount++;
			return Const.StatusCodes.TASK_QUEUE_RESPONSE_OK;
		}

		@Override
		public void initialize(Map<String, String> arg0) {
			taskCount = 0;
		}
		
		public static void resetTaskCount() {
			taskCount = 0;
		}
	}
	
	@BeforeClass
	public static void classSetUp() throws Exception {
		printTestClassHeader();
		gaeSimulation.setupWithTaskQueueCallbackClass(EvaluationOpeningCallback.class);
		gaeSimulation.resetDatastore();
	}
	
	@Test
	public void testAll() throws Exception {
		testAdditionOfTaskToTaskQueue();
		testEvaluationOpeningMailAction();
	}
	
	@AfterClass
	public static void classTearDown() throws Exception {
		printTestClassFooter();
	}
	
	private void testAdditionOfTaskToTaskQueue() throws Exception {
		DataBundle dataBundle = getTypicalDataBundle();
		restoreTypicalDataInDatastore();
		
		
		______TS("all evaluations activated");
		// ensure all existing evaluations are already activated.
		activateAllEvaluations(dataBundle);
		
		evaluationsLogic.activateReadyEvaluations();
		waitForTaskQueueExecution(0);
		assertEquals(0, EvaluationOpeningCallback.taskCount);
		
		______TS("typical case, two ready evaluations");
		// Reuse an existing evaluation to create a new one that is ready to
		// activate. Put this evaluation in a negative time zone.
		EvaluationAttributes evaluation1 = dataBundle.evaluations
				.get("evaluation1InCourse1");
		String nameOfEvalInCourse1 = "new-eval-in-course-1-tARE";
		evaluation1.name = nameOfEvalInCourse1;

		evaluation1.activated = false;

		double timeZone = -1.0;
		evaluation1.timeZone = timeZone;

		evaluation1.startTime = TimeHelper.getMsOffsetToCurrentTimeInUserTimeZone(0, timeZone);
		evaluation1.endTime = TimeHelper.getDateOffsetToCurrentTime(2);

		evaluationsLogic.createEvaluationCascade(evaluation1);
		assertEquals("This evaluation is not ready to activate as expected "+ evaluation1.toString(),
				true, 
				evaluationsLogic.getEvaluation(evaluation1.courseId, evaluation1.name).isReadyToActivate());

		// Create another evaluation in another course in similar fashion.
		// Put this evaluation in a positive time zone.
		// This one too is ready to activate.
		EvaluationAttributes evaluation2 = dataBundle.evaluations
				.get("evaluation1InCourse2");
		evaluation2.activated = false;
		String nameOfEvalInCourse2 = "new-evaluation-in-course-2-tARE";
		evaluation2.name = nameOfEvalInCourse2;

		timeZone = 2.0;
		evaluation2.timeZone = timeZone;

		evaluation2.startTime = TimeHelper.getMsOffsetToCurrentTimeInUserTimeZone(
				0, timeZone);
		evaluation2.endTime = TimeHelper.getDateOffsetToCurrentTime(2);

		evaluationsLogic.createEvaluationCascade(evaluation2);
		assertEquals("This evaluation is not ready to activate as expected "+ evaluation2.toString(),
				true, 
				evaluationsLogic.getEvaluation(evaluation2.courseId, evaluation2.name).isReadyToActivate());
		
		evaluationsLogic.activateReadyEvaluations();
		waitForTaskQueueExecution(2);
		
		//Expected is 2 because there are only 2 ready evaluations
		assertEquals(2, EvaluationOpeningCallback.taskCount);
		
	}

	private void testEvaluationOpeningMailAction() throws Exception{
		
		DataBundle dataBundle = getTypicalDataBundle();
		restoreTypicalDataInDatastore();
		
		activateAllEvaluations(dataBundle);
		
		// Reuse an existing evaluation to create a new one that is
		// closing in 24 hours.
		EvaluationAttributes evaluation1 = dataBundle.evaluations
				.get("evaluation1InCourse1");
		String nameOfEvalInCourse1 = "new-eval-in-course-1-tARE";
		evaluation1.name = nameOfEvalInCourse1;

		evaluation1.activated = false;

		double timeZone = -1.0;
		evaluation1.timeZone = timeZone;

		evaluation1.startTime = TimeHelper.getMsOffsetToCurrentTimeInUserTimeZone(0, timeZone);
		evaluation1.endTime = TimeHelper.getDateOffsetToCurrentTime(2);

		evaluationsLogic.createEvaluationCascade(evaluation1);
		assertEquals("This evaluation is not ready to activate as expected "+ evaluation1.toString(),
				true, 
				evaluationsLogic.getEvaluation(evaluation1.courseId, evaluation1.name).isReadyToActivate());
		
		//Prepare parameter map to be used with EvaluationClosingMailAction
		HashMap<String, String> paramMap = new HashMap<String, String>();
		
		paramMap.put(ParamsNames.EMAIL_TYPE, EmailType.EVAL_OPENING.toString());
		paramMap.put(ParamsNames.EMAIL_EVAL, evaluation1.name);
		paramMap.put(ParamsNames.EMAIL_COURSE, evaluation1.courseId);
		
		EmailAction evalOpeningAction = new EvaluationOpeningMailAction(paramMap);
		List<MimeMessage> preparedEmails = evalOpeningAction.getPreparedEmailsAndPerformSuccessOperations();
		
		int course1StudentCount = studentsLogic.getStudentsForCourse(
				evaluation1.courseId).size();
		int course1InstructorCount = instructorsLogic.getInstructorsForCourse(
				evaluation1.courseId).size();
		assertEquals(course1StudentCount + course1InstructorCount,
				preparedEmails.size());
		
		for (MimeMessage m : preparedEmails) {
			String subject = m.getSubject();
			assertTrue(subject.contains(evaluation1.name));
			assertTrue(subject.contains(Emails.SUBJECT_PREFIX_STUDENT_EVALUATION_OPENING));
		}
		
		______TS("Ensure no mails are sent anymore as all tasks are active");
		EvaluationOpeningCallback.resetTaskCount();
		evaluationsLogic.activateReadyEvaluations();
		waitForTaskQueueExecution(0);
		assertEquals(0, EvaluationOpeningCallback.taskCount);
	}

	private void activateAllEvaluations(DataBundle dataBundle) throws Exception{
		for (EvaluationAttributes e : dataBundle.evaluations.values()) {
			e.activated = true;
			evaluationsLogic.updateEvaluation(e);
			assertTrue(evaluationsLogic.getEvaluation(e.courseId, e.name).getStatus() != EvalStatus.AWAITING);
		}
	}
}
