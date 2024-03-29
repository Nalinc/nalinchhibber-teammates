package teammates.test.cases.ui.browsertests;

import static org.testng.AssertJUnit.assertNull;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import teammates.common.datatransfer.DataBundle;
import teammates.common.util.Const;
import teammates.common.util.Url;
import teammates.test.driver.BackDoor;
import teammates.test.pageobjects.AdminAccountDetailsPage;
import teammates.test.pageobjects.Browser;
import teammates.test.pageobjects.BrowserPool;

/**
 * Covers the 'accounts management' view for admins.
 * SUT: {@link AdminAccountDetailsPage}
 */
public class AdminAccountDetailsPageUiTest extends BaseUiTestCase{
	private static Browser browser;
	private static AdminAccountDetailsPage detailsPage;
	private static DataBundle testData;
	
	@BeforeClass
	public static void classSetup() throws Exception {
		printTestClassHeader();
		testData = loadDataBundle("/AdminAccountDetailsPageUiTest.json");
		restoreTestDataOnServer(testData);
		browser = BrowserPool.getBrowser();
	}
	
	@Test
	public void testAll(){
		testContent();
		//no links or input validation to check
		testRemoveFromCourseAction();
	}
	
	public void testContent() {
		
		______TS("content: typical page");
		
		Url detailsPageUrl = createUrl(Const.ActionURIs.ADMIN_ACCOUNT_DETAILS_PAGE)
			.withInstructorId("AAMgtUiT.instr2");
		detailsPage = loginAdminToPage(browser, detailsPageUrl, AdminAccountDetailsPage.class);
		
		//a full content checking is omitted because this is for internal users only.
	}



	public void testRemoveFromCourseAction(){
		
		______TS("action: remove instructor from course");
		
		String googleId = "AAMgtUiT.instr2";
		String courseId = "AAMgtUiT.CS2104";
		
		detailsPage.clickRemoveInstructorFromCourse(courseId)
			.verifyStatus(Const.StatusMessages.INSTRUCTOR_REMOVED_FROM_COURSE);
		assertNull(BackDoor.getInstructor(googleId, courseId));
	
		______TS("action: remove student from course");
		
		courseId = "AAMgtUiT.CS1101";
		detailsPage.clickRemoveStudentFromCourse(courseId)
			.verifyStatus(Const.StatusMessages.INSTRUCTOR_REMOVED_FROM_COURSE);
		assertNull(BackDoor.getStudent(courseId, "AAMgtUiT.instr2@gmail.com"));
	
	}
	
	@AfterClass
	public static void classTearDown() throws Exception {
		BrowserPool.release(browser);
	}
	
}
