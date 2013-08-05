package teammates.test.cases.ui.browsertests;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import teammates.common.datatransfer.DataBundle;
import teammates.common.util.Const;
import teammates.common.util.Url;
import teammates.test.pageobjects.Browser;
import teammates.test.pageobjects.BrowserPool;
import teammates.test.pageobjects.StudentFeedbackResultsPage;

/**
 * Tests 'Feedback Results' view of students.
 * SUT: {@link StudentFeedbackResultsPage}.
 */
public class StudentFeedbackResultsPageUiTest extends BaseUiTestCase {

	private static DataBundle testData;
	private static Browser browser;
	private StudentFeedbackResultsPage resultsPage;
	
	
	@BeforeClass
	public static void classSetup() throws Exception {
		printTestClassHeader();
		testData = loadDataBundle("/StudentFeedbackResultsPageUiTest.json");
		restoreTestDataOnServer(testData);

		browser = BrowserPool.getBrowser();		
	}
	
	@Test
	public void testAll() throws Exception {
		
		______TS("no responses");
		
		resultsPage = loginToStudentFeedbackSubmitPage("Alice", "Empty Session");
		resultsPage.verifyHtml("/studentFeedbackResultsPageEmpty.html");
			
		______TS("standard session results");
		
		resultsPage = loginToStudentFeedbackSubmitPage("Alice", "Open Session");
		resultsPage.verifyHtml("/studentFeedbackResultsPageOpen.html");
	}

	@AfterClass
	public static void classTearDown() throws Exception {
		BrowserPool.release(browser);
	}
	
	private StudentFeedbackResultsPage loginToStudentFeedbackSubmitPage(
			String studentName, String fsName) {
		Url editUrl = createUrl(Const.ActionURIs.STUDENT_FEEDBACK_RESULTS_PAGE)
				.withUserId(testData.students.get(studentName).googleId)
				.withCourseId(testData.feedbackSessions.get(fsName).courseId)
				.withSessionName(testData.feedbackSessions.get(fsName).feedbackSessionName);
		return loginAdminToPage(browser, editUrl,
				StudentFeedbackResultsPage.class);
	}

}