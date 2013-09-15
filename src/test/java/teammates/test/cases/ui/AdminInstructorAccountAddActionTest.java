package teammates.test.cases.ui;


import static org.testng.AssertJUnit.assertEquals;
import java.lang.reflect.Method;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import teammates.common.datatransfer.DataBundle;
import teammates.common.util.Const;
import teammates.common.util.StringHelper;
import teammates.ui.controller.AdminInstructorAccountAddAction;
import teammates.ui.controller.Action;
import teammates.ui.controller.AdminHomePageData;
import teammates.ui.controller.RedirectResult;
import teammates.ui.controller.ShowPageResult; 


public class AdminInstructorAccountAddActionTest extends BaseActionTest {

	DataBundle dataBundle;
	
	
	@BeforeClass
	public static void classSetUp() throws Exception {
		printTestClassHeader();
		uri = Const.ActionURIs.ADMIN_INSTRUCTORACCOUNT_ADD;
	}

	@BeforeMethod
	public void caseSetUp() throws Exception {
		dataBundle = getTypicalDataBundle();
		restoreTypicalDataInDatastore();
	}
	
	@Test
	public void testAccessControl() throws Exception{
		String[] submissionParams = new String[]{};
		verifyOnlyAdminsCanAccess(submissionParams);
	}

	@Test
	public void testGenerateNextDemoCourseId() throws Exception{
		testGenerateNextDemoCourseIdForLengthLimit(40);
		testGenerateNextDemoCourseIdForLengthLimit(20);
	}
	
	@Test
	public void testExecuteAndPostProcess() throws Exception{

		final String newInstructorId = "JamesBond89";
		final String name = "JamesBond";
		final String email = "jamesbond89@gmail.com";
		final String institute = "National University of Singapore";
		final String adminUserId = "admin.user";
		
		______TS("Not enough parameters");
		
		gaeSimulation.loginAsAdmin(adminUserId);
		verifyAssumptionFailure();
		verifyAssumptionFailure(Const.ParamsNames.INSTRUCTOR_ID, newInstructorId);
		verifyAssumptionFailure(Const.ParamsNames.INSTRUCTOR_ID, newInstructorId,
				Const.ParamsNames.INSTRUCTOR_NAME, name);
		verifyAssumptionFailure(Const.ParamsNames.INSTRUCTOR_ID, newInstructorId,
				Const.ParamsNames.INSTRUCTOR_NAME, name,
				Const.ParamsNames.INSTRUCTOR_EMAIL, email);
		verifyAssumptionFailure(Const.ParamsNames.INSTRUCTOR_ID, newInstructorId,
				Const.ParamsNames.INSTRUCTOR_NAME, name,
				Const.ParamsNames.INSTRUCTOR_INSTITUTION, institute);
		verifyAssumptionFailure(Const.ParamsNames.INSTRUCTOR_ID, newInstructorId,
				Const.ParamsNames.INSTRUCTOR_EMAIL, email,
				Const.ParamsNames.INSTRUCTOR_INSTITUTION, institute);
		verifyAssumptionFailure(Const.ParamsNames.INSTRUCTOR_NAME, name,
				Const.ParamsNames.INSTRUCTOR_EMAIL, email,
				Const.ParamsNames.INSTRUCTOR_INSTITUTION, institute);
		
		
		______TS("Normal case: not importing demo couse");
		
		Action a = getAction(
				Const.ParamsNames.INSTRUCTOR_ID, newInstructorId,
				Const.ParamsNames.INSTRUCTOR_NAME, name,
				Const.ParamsNames.INSTRUCTOR_EMAIL, email,
				Const.ParamsNames.INSTRUCTOR_INSTITUTION, institute);
		
		RedirectResult r = (RedirectResult) a.executeAndPostProcess();
		
		assertEquals(false, r.isError);
		assertEquals("Instructor " + name + " has been successfully created", r.getStatusMessage());
		assertEquals(Const.ActionURIs.ADMIN_HOME_PAGE, r.destination);
		assertEquals(Const.ActionURIs.ADMIN_HOME_PAGE + "?message=Instructor+JamesBond+has+been+successfully+created&error=false&user=" + adminUserId, r.getDestinationWithParams());
		
		______TS("Error: already an instructor");
		
		ShowPageResult rForAlreadyExistingInstructor = (ShowPageResult) getAction(
				Const.ParamsNames.INSTRUCTOR_ID, newInstructorId,
				Const.ParamsNames.INSTRUCTOR_NAME, name,
				Const.ParamsNames.INSTRUCTOR_EMAIL, email,
				Const.ParamsNames.INSTRUCTOR_INSTITUTION, institute).executeAndPostProcess();
		
		assertEquals(true, rForAlreadyExistingInstructor.isError);
		assertEquals("The Google ID " + newInstructorId + " is already registered as an instructor", rForAlreadyExistingInstructor.getStatusMessage());
		assertEquals(Const.ViewURIs.ADMIN_HOME, rForAlreadyExistingInstructor.destination);
		assertEquals(Const.ViewURIs.ADMIN_HOME + "?message=The+Google+ID+" + newInstructorId + "+is+already+registered+as+an+instructor&error=true&user=" + adminUserId, rForAlreadyExistingInstructor.getDestinationWithParams());
		AdminHomePageData pageData = (AdminHomePageData)rForAlreadyExistingInstructor.data;
		assertEquals(email, pageData.instructorEmail);
		assertEquals(newInstructorId, pageData.instructorId);
		assertEquals(institute, pageData.instructorInstitution);
		assertEquals(name, pageData.instructorName);
		
		______TS("Normal case: importing demo couse");
		
		final String anotherNewInstructorId = "JamesBond99";
		a = getAction(
				Const.ParamsNames.INSTRUCTOR_ID, anotherNewInstructorId,
				Const.ParamsNames.INSTRUCTOR_NAME, name,
				Const.ParamsNames.INSTRUCTOR_EMAIL, email,
				Const.ParamsNames.INSTRUCTOR_INSTITUTION, institute,
				Const.ParamsNames.INSTRUCTOR_IMPORT_SAMPLE, "SELECTED");
		
		r = (RedirectResult) a.executeAndPostProcess();
		assertEquals(false, r.isError);
		assertEquals("Instructor " + name + " has been successfully created", r.getStatusMessage());
		assertEquals(Const.ActionURIs.ADMIN_HOME_PAGE, r.destination);
		assertEquals(Const.ActionURIs.ADMIN_HOME_PAGE + "?message=Instructor+JamesBond+has+been+successfully+created&error=false&user=" + adminUserId, r.getDestinationWithParams());

	}
	

	private void testGenerateNextDemoCourseIdForLengthLimit(int maximumIdLength) throws Exception{
		AdminInstructorAccountAddAction a = new AdminInstructorAccountAddAction();
		final Method generateNextDemoCourseId;
		generateNextDemoCourseId = a.getClass().getDeclaredMethod("generateNextDemoCourseId", String.class, int.class);
		generateNextDemoCourseId.setAccessible(true);
		final String normalIdSuffix = ".gma-demo";
		final String atEmail = "@gmail.com";
		final int normalIdSuffixLength = normalIdSuffix.length();  //9
		final String strShortWithWordDemo = StringHelper.generateStringOfLength((maximumIdLength - normalIdSuffixLength)/2) + "-demo";
		final String strWayShorterThanMaxium = StringHelper.generateStringOfLength((maximumIdLength - normalIdSuffixLength)/2);
		final String strOneCharShorterThanMaximum = StringHelper.generateStringOfLength(maximumIdLength - normalIdSuffixLength);
		final String strOneCharLongerThanMaximum = StringHelper.generateStringOfLength(maximumIdLength - normalIdSuffixLength + 1); 
		assertEquals("Case email input: normal short email with word 'demo' with maximumIdLength:" + maximumIdLength,strShortWithWordDemo + normalIdSuffix, generateNextDemoCourseId.invoke(a, strShortWithWordDemo + atEmail, maximumIdLength));
		assertEquals("Case courseId input: normal short email with word 'demo', no index with maximumIdLength:" + maximumIdLength,strShortWithWordDemo + normalIdSuffix + "0", generateNextDemoCourseId.invoke(a, strShortWithWordDemo + normalIdSuffix, maximumIdLength));
		assertEquals("Case courseId input: normal short email with word 'demo', index is '0' with maximumIdLength:" + maximumIdLength,strShortWithWordDemo + normalIdSuffix + "1", generateNextDemoCourseId.invoke(a, strShortWithWordDemo + normalIdSuffix + "0", maximumIdLength));
		assertEquals("Case email input: normal short email with maximumIdLength:" + maximumIdLength,strWayShorterThanMaxium + normalIdSuffix, generateNextDemoCourseId.invoke(a, strWayShorterThanMaxium + atEmail, maximumIdLength));
		assertEquals("Case email input: one char shorter than maximumIdLength:" + maximumIdLength,strOneCharShorterThanMaximum + normalIdSuffix,generateNextDemoCourseId.invoke(a, strOneCharShorterThanMaximum + atEmail, maximumIdLength));
		assertEquals("Case email input: one char longer than maximumIdLength:" + maximumIdLength,strOneCharLongerThanMaximum.substring(1) + normalIdSuffix,generateNextDemoCourseId.invoke(a, strOneCharLongerThanMaximum + atEmail, maximumIdLength));
		assertEquals("Case courseId input: no index with maximumIdLength:" + maximumIdLength,strWayShorterThanMaxium + normalIdSuffix + "0",generateNextDemoCourseId.invoke(a, strWayShorterThanMaxium + normalIdSuffix, maximumIdLength));
		assertEquals("Case courseId input: index is '0' with maximumIdLength:" + maximumIdLength,strWayShorterThanMaxium + normalIdSuffix + "1",generateNextDemoCourseId.invoke(a, strWayShorterThanMaxium + normalIdSuffix + "0", maximumIdLength));
		assertEquals("Case courseId input: index is '9', short ID with maximumIdLength:" + maximumIdLength,strWayShorterThanMaxium + normalIdSuffix + "10",generateNextDemoCourseId.invoke(a, strWayShorterThanMaxium + normalIdSuffix + "9", maximumIdLength));
		assertEquals("Case courseId input: index is '9', short ID boundary with maximumIdLength:" + maximumIdLength,strOneCharShorterThanMaximum.substring(2) + normalIdSuffix + "10",generateNextDemoCourseId.invoke(a, strOneCharShorterThanMaximum.substring(1) + normalIdSuffix + "9", maximumIdLength));
	}

	private Action getAction(String... parameters) throws Exception {
		return (Action)gaeSimulation.getActionObject(uri, parameters);
	}


}
