package teammates.test.cases.ui.browsertests;

import static org.testng.AssertJUnit.assertEquals;
import teammates.common.Common;
import teammates.common.Url;
import teammates.common.datatransfer.DataBundle;
import teammates.test.cases.BaseTestCase;
import teammates.test.driver.BackDoor;
import teammates.test.driver.TestProperties;
import teammates.test.pageobjects.AdminHomePage;
import teammates.test.pageobjects.AppPage;
import teammates.test.pageobjects.Browser;
import teammates.test.pageobjects.DevServerLoginPage;
import teammates.test.pageobjects.GoogleLoginPage;

public class BaseUiTestCase extends BaseTestCase {

	protected static final String appUrl = TestProperties.inst().TEAMMATES_URL;

	/**
	 * Logs in a page using admin credentials (i.e. in masquerade mode).
	 */
	protected static <T extends AppPage> T loginAdminToPage(Browser browser, Url url, Class<T> typeOfPage) {
		
		String adminUsername = TestProperties.inst().TEST_ADMIN_ACCOUNT; 
		String adminPassword = TestProperties.inst().TEST_ADMIN_PASSWORD;
		
		String instructorId = url.get(Common.PARAM_USER_ID);
		
		if(instructorId==null){ //admin using system as admin
			instructorId = "defaultAdmin";
		}
		
		if(browser.isAdminLoggedIn){
			browser.driver.get(url.toString());
		} else {
			//logout and attempt to load the requested URL. This will be 
			//  redirected to a dev-server/google login page
			AppPage.logout(browser);
			browser.driver.get(url.toString());
			String pageSource = browser.driver.getPageSource();
			
			//login based on the login page type
			if(DevServerLoginPage.containsExpectedPageContents(pageSource)){
				DevServerLoginPage loginPage = AppPage.getNewPageInstance(browser, DevServerLoginPage.class);
				loginPage.loginAdminAsInstructor(adminUsername, adminPassword, instructorId);
	
			} else if(GoogleLoginPage.containsExpectedPageContents(pageSource)){
				GoogleLoginPage loginPage = AppPage.getNewPageInstance(browser, GoogleLoginPage.class);
				loginPage.loginAdminAsInstructor(adminUsername, adminPassword, instructorId);
			
			} else {
				throw new IllegalStateException("Not a valid login page :" + pageSource);
			}
		}
		
		//After login, the browser should be redirected to the page requested originally.
		//  No need to reload. In fact, reloading might results in duplicate request to the server.
		return AppPage.getNewPageInstance(browser, typeOfPage);
	}
	
	/**
	 * Deletes are recreates the given data on the datastore.
	 */
	protected static void restoreTestDataOnServer(DataBundle testData) {
		String backDoorOperationStatus = BackDoor.restoreDataBundle(testData);
		assertEquals(Common.BACKEND_STATUS_SUCCESS, backDoorOperationStatus);
	}

	protected static AdminHomePage loginAdmin(Browser currentBrowser) {
		return loginAdminToPage(currentBrowser, new Url(Common.PAGE_ADMIN_HOME), AdminHomePage.class);
	}


}
