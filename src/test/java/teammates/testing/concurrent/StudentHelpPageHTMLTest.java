package teammates.testing.concurrent;

import static org.junit.Assert.assertEquals;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import teammates.testing.config.Config;
import teammates.testing.lib.BrowserInstance;
import teammates.testing.lib.BrowserInstancePool;
import teammates.testing.object.Scenario;

public class StudentHelpPageHTMLTest extends TestCase {
	static Scenario scn = setupScenarioInstance("scenario");
	static BrowserInstance bi;
	
	private static String TEST_STUDENT = scn.students.get(2).email;

	@BeforeClass
	public static void classSetup() throws Exception {
		bi = BrowserInstancePool.getBrowserInstance();
		bi.studentLogin(TEST_STUDENT, Config.inst().TEAMMATES_APP_PASSWD);
	}

	@AfterClass
	public static void classTearDown() throws Exception {
		if (bi.isElementPresent(bi.logoutTab))
			bi.logout();

		BrowserInstancePool.release(bi);
	}

	@Test
	public void clickHelpTabOpenNewWindowSuccessful() throws Exception {
		String helpWindow = "Teammates Online Peer Feedback System for Student Team Projects";
		bi.clickAndOpenNewWindow(bi.helpTab, helpWindow);

		assertEquals(true, bi.isTextPresent("Table of Contents"));
		assertEquals(true, bi.isTextPresent("Getting Started"));
		assertEquals(true, bi.isTextPresent("Frequently Asked Questions"));

		bi.closeSelectedWindow();
	}

	@Test
	public void verifyHelpPageSuccessful() throws Exception {
		bi.verifyPageHTML("http://www.comp.nus.edu.sg/~teams/studenthelp.html", 
				"target/test-classes/pages/studentHelp.html");
	}
}