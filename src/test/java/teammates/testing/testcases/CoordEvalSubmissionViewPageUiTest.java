package teammates.testing.testcases;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import teammates.api.Common;
import teammates.datatransfer.DataBundle;
import teammates.jsp.Helper;
import teammates.testing.config.Config;
import teammates.testing.lib.BackDoor;
import teammates.testing.lib.BrowserInstance;
import teammates.testing.lib.BrowserInstancePool;

/**
 * Tests coordEvalSubmissionView.jsp from functionality and UI
 * @author Aldrian Obaja
 *
 */
public class CoordEvalSubmissionViewPageUiTest extends BaseTestCase {
	private static BrowserInstance bi;
	private static DataBundle scn;
	
	private static String appUrl = Config.inst().TEAMMATES_URL.replaceAll("/(?=$)","");
	
	@BeforeClass
	public static void classSetup() throws Exception {
		printTestClassHeader("CoordEvalSubmissionViewUITest");
		String jsonString = Common.readFile(Common.TEST_DATA_FOLDER+"/CoordEvalSubmissionViewUiTest.json");
		scn = Common.getTeammatesGson().fromJson(jsonString, DataBundle.class);

		System.out.println("Importing test data...");
		long start = System.currentTimeMillis();
		BackDoor.deleteCoordinators(jsonString);
		System.out.println(BackDoor.persistNewDataBundle(jsonString));
		System.out.println("The test data was imported in "+(System.currentTimeMillis()-start)+" ms");
		
		bi = BrowserInstancePool.getBrowserInstance();
		
		bi.loginCoord(scn.coords.get("teammates.demo.coord").id, Config.inst().TEAMMATES_APP_PASSWD);
		String link = appUrl+Common.PAGE_COORD_EVAL_SUBMISSION_VIEW;
		link = Helper.addParam(link, Common.PARAM_COURSE_ID, scn.evaluations.get("First Eval").course);
		link = Helper.addParam(link, Common.PARAM_EVALUATION_NAME, scn.evaluations.get("First Eval").name);
		link = Helper.addParam(link, Common.PARAM_STUDENT_EMAIL, scn.students.get("emily.tmms@CESubViewUiT.CS1101").email);
		bi.goToUrl(link);
	}
	
	@AfterClass
	public static void classTearDown() throws Exception {
		BrowserInstancePool.release(bi);
		printTestClassFooter("CoordEvalSubmissionViewUITest");
	}

	@Test
	public void testCoordEvalSubmissionViewPage() throws Exception{
		testCoordEvalSubmissionViewHTML();
		testCoordEvalSubmissionViewUiPaths();
	}

	public void testCoordEvalSubmissionViewHTML() throws Exception{
//		bi.printCurrentPage(Common.TEST_PAGES_FOLDER+"/coordEvalSubmissionViewNew.html");
		bi.verifyCurrentPageHTML(Common.TEST_PAGES_FOLDER+"/coordEvalSubmissionViewNew.html");
	}

	// TODO: Finish Evaluation Submission View UI Path test
	public void testCoordEvalSubmissionViewUiPaths() throws Exception{
		
	}
}