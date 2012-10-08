package teammates.test.cases;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import teammates.common.Common;
import teammates.common.datatransfer.CourseData;

public class CourseDataTest extends BaseTestCase {

	@BeforeClass
	public static void setupClass() throws Exception {
		printTestClassHeader();
		turnLoggingUp(CourseData.class);
	}

	@Test
	public void testValidate() {
		CourseData c = new CourseData();
		
		// SUCCESS: Basic Success Case
		c.id = "valid-id-$_abc";
		c.coord = "valid-coord";
		c.name = "valid-name";
		
		assertTrue(c.isValid());
		
		// FAIL: ID null
		c.id = null;
		assertFalse(c.isValid());
		assertEquals(c.getInvalidStateInfo(), CourseData.ERROR_FIELD_ID);
		
		// SUCCESS: ID at max length
		String veryLongId = Common.generateStringOfLength(Common.COURSE_ID_MAX_LENGTH);
		c.id = veryLongId;
		assertTrue(c.isValid());
		
		// FAIL: ID too long
		c.id += "a";
		assertFalse(c.isValid());
		assertEquals(c.getInvalidStateInfo(), CourseData.ERROR_ID_TOOLONG);
		
		// FAIL : ID with invalid chars
		c.id = "my-uber-id!";
		assertFalse(c.isValid());
		assertEquals(c.getInvalidStateInfo(), CourseData.ERROR_ID_INVALIDCHARS);
		
		// FAIL : Coord null
		c.id = "valid-id";
		c.coord = null;
		assertFalse(c.isValid());
		assertEquals(c.getInvalidStateInfo(), CourseData.ERROR_FIELD_COORD);
		
		// FAIL : Name null
		c.coord = "valid-coord";
		c.name = null;
		assertFalse(c.isValid());
		assertEquals(c.getInvalidStateInfo(), CourseData.ERROR_FIELD_NAME);
		
		// SUCCESS: Name at max length
		String veryLongName = Common.generateStringOfLength(Common.COURSE_NAME_MAX_LENGTH);
		c.name = veryLongName;
		assertTrue(c.isValid());
		
		// FAIL : Name too long
		c.name += "e";
		assertFalse(c.isValid());
		assertEquals(c.getInvalidStateInfo(), CourseData.ERROR_NAME_TOOLONG);
	}
	
	@Test
	public void testGetInvalidStateInfo(){
	    //already tested in testValidate() above
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		turnLoggingDown(CourseData.class);
	}
}
