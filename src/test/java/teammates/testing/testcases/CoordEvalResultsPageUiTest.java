package teammates.testing.testcases;

import static org.junit.Assert.fail;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;

import teammates.api.Common;
import teammates.datatransfer.DataBundle;
import teammates.exception.NoAlertAppearException;
import teammates.jsp.Helper;
import teammates.testing.config.Config;
import teammates.testing.lib.BackDoor;
import teammates.testing.lib.BrowserInstance;
import teammates.testing.lib.BrowserInstancePool;

/**
 * Tests coordEvalResults.jsp from UI functionality and HTML test
 */
public class CoordEvalResultsPageUiTest extends BaseTestCase {
	private static BrowserInstance bi;
	private static DataBundle scn;
	
	private static String appUrl = Config.inst().TEAMMATES_URL;
	
	@BeforeClass
	public static void classSetup() throws Exception {
		printTestClassHeader();

		String jsonString = Common.readFile(Common.TEST_DATA_FOLDER+"/CoordEvalResultsUiTest.json");
		scn = Common.getTeammatesGson().fromJson(jsonString, DataBundle.class);
		
		BackDoor.deleteCoordinators(jsonString);
		print("Importing test data...");
		long start = System.currentTimeMillis();
		String backDoorOperationStatus = BackDoor.persistNewDataBundle(jsonString);
		print(backDoorOperationStatus);
		print("The test data was imported in "+(System.currentTimeMillis()-start)+" ms");

		bi = BrowserInstancePool.getBrowserInstance();

		bi.loginAdmin(Config.inst().TEAMMATES_ADMIN_ACCOUNT, Config.inst().TEAMMATES_ADMIN_PASSWORD);
	}
	
	@AfterClass
	public static void classTearDown() throws Exception {
		BrowserInstancePool.release(bi);
		printTestClassFooter();
	}

	@Test
	public void testCoordEvalResultsOpenEval() throws Exception{
		printTestCaseHeader();

		______TS("summary view");
		
		String link = appUrl+Common.PAGE_COORD_EVAL_RESULTS;
		link = Helper.addParam(link,Common.PARAM_COURSE_ID,scn.courses.get("CEvalRUiT.CS1101").id);
		link = Helper.addParam(link,Common.PARAM_EVALUATION_NAME,scn.evaluations.get("First Eval").name);
		link = Helper.addParam(link,Common.PARAM_USER_ID,scn.coords.get("teammates.demo.coord").id);
		bi.goToUrl(link);
		bi.verifyCurrentPageHTML(Common.TEST_PAGES_FOLDER+"/coordEvalResultsOpenEval.html");
		
		//TODO: check for sorting?

		______TS("details by reviewer");
		
		bi.getSelenium().check("id=radio_reviewer");
		bi.verifyCurrentPageHTML(Common.TEST_PAGES_FOLDER+"/coordEvalResultsOpenEvalByReviewer.html");
		
		______TS("details by reviewee");
		
		bi.getSelenium().check("id=radio_reviewee");
		bi.verifyCurrentPageHTML(Common.TEST_PAGES_FOLDER+"/coordEvalResultsOpenEvalByReviewee.html");
	}
	
	@Test
	public void testCoordEvalResultsPublishedEval() throws Exception{
		printTestCaseHeader();
		
		______TS("summary view");
		
		String link = appUrl + Common.PAGE_COORD_EVAL_RESULTS;
		link = Helper.addParam(link,Common.PARAM_COURSE_ID,scn.courses.get("CEvalRUiT.CS1101").id);
		link = Helper.addParam(link,Common.PARAM_EVALUATION_NAME,scn.evaluations.get("Second Eval").name);
		link = Helper.addParam(link,Common.PARAM_USER_ID,scn.coords.get("teammates.demo.coord").id);
		bi.goToUrl(link);
		bi.verifyCurrentPageHTML(Common.TEST_PAGES_FOLDER+"/coordEvalResultsPublishedEval.html");

		______TS("unpublishing: click and cancel");
		
		By unpublishButton = By.id("button_unpublish");
		try{
			bi.clickAndCancel(unpublishButton);
			bi.verifyCurrentPageHTML(Common.TEST_PAGES_FOLDER+"/coordEvalResultsPublishedEval.html");
		} catch (NoAlertAppearException e){
			fail("No confirmation box when clicking unpublish button");
		}
		
		______TS("unpublishing: click and confirm");
		
		try{
			bi.clickAndConfirm(unpublishButton);
			bi.waitForStatusMessage(Common.MESSAGE_EVALUATION_UNPUBLISHED);
		} catch (NoAlertAppearException e){
			fail("No confirmation box when clicking unpublish button");
		}
		//TODO: check for the full html?
	}
	
	@Test
	public void testCoordEvalResultsClosedEval() throws Exception{
		printTestCaseHeader();
		
		______TS("summary view");
		
		String link = appUrl + Common.PAGE_COORD_EVAL_RESULTS;
		link = Helper.addParam(link,Common.PARAM_COURSE_ID,scn.courses.get("CEvalRUiT.CS1101").id);
		link = Helper.addParam(link,Common.PARAM_EVALUATION_NAME,scn.evaluations.get("Third Eval").name);
		link = Helper.addParam(link,Common.PARAM_USER_ID,scn.coords.get("teammates.demo.coord").id);
		bi.goToUrl(link);
		bi.verifyCurrentPageHTML(Common.TEST_PAGES_FOLDER+"/coordEvalResultsClosedEval.html");

		______TS("publishing: click and cancel");
		
		By publishButton = By.id("button_publish");
		try{
			bi.clickAndCancel(publishButton);
			bi.verifyCurrentPageHTML(Common.TEST_PAGES_FOLDER+"/coordEvalResultsClosedEval.html");
		} catch (NoAlertAppearException e){
			fail("No confirmation box when clicking publish button");
		}
		
		______TS("publishing: click and confirm");
		
		try{
			bi.clickAndConfirm(publishButton);
			bi.waitForStatusMessage(Common.MESSAGE_EVALUATION_PUBLISHED);
		} catch (NoAlertAppearException e){
			fail("No confirmation box when clicking publish button");
		}
	}
}