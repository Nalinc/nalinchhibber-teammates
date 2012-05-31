package teammates.testing.testcases;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import teammates.testing.junit.TMAPITest;


@RunWith(Suite.class)
@Suite.SuiteClasses({
	CommonTest.class,
	StudentTest.class,
	AllJsUnitTests.class,
	TMAPITest.class,
	APIServletTest.class,
	CoordCourseAddPageUiTest.class,
	CoordCourseAddApiTest.class,
	CoordHomePageUiTest.class
})

public class AllCasesTestSuite {	
	
}
