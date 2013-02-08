package teammates.test.cases;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.appengine.api.datastore.Text;

import teammates.common.datatransfer.SubmissionData;

public class SubmissionDataTest extends BaseTestCase {

	@BeforeClass
	public static void setupClass() throws Exception {
		printTestClassHeader();
		turnLoggingUp(SubmissionData.class);
	}

	@Test
	public void testValidate() {
		SubmissionData s = new SubmissionData();
		
		s.course = "valid-course";
		s.evaluation = "valid-evaluation";
		s.reviewer = "valid.reviewer@gmail.com";
		s.reviewee = "valid.reviewee@gmail.com";
		s.team = "valid-team";

		// SUCCESS : minimal properties, still valid
		assertTrue(s.getInvalidStateInfo(), s.isValid());

		s.points = 10;
		s.justification = new Text("valid-justification");
		s.p2pFeedback = new Text("valid-feedback");

		// SUCCESS : other properties added, still valid
		assertTrue(s.getInvalidStateInfo(), s.isValid());

		// FAIL : no course
		s.course = null;
		assertFalse(s.isValid());
		assertEquals(s.getInvalidStateInfo(), SubmissionData.ERROR_FIELD_COURSE);
		
		// FAIL : no evaluation
		s.course = "valid-course";
		s.evaluation = null;
		assertFalse(s.isValid());
		assertEquals(s.getInvalidStateInfo(), SubmissionData.ERROR_FIELD_EVALUATION);
		
		// FAIL : no reviewee
		s.evaluation = "valid-evaluation";
		s.reviewee = null;
		assertFalse(s.isValid());
		assertEquals(s.getInvalidStateInfo(), SubmissionData.ERROR_FIELD_REVIEWEE);
		
		// FAIL : no reviewer
		s.reviewee = "validreviewee@gmail.com";
		s.reviewer = null;
		assertFalse(s.isValid());
		assertEquals(s.getInvalidStateInfo(), SubmissionData.ERROR_FIELD_REVIEWER);
	}
	
	@Test
	public void testGetInvalidStateInfo(){
	    //already tested in testValidate() above
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		turnLoggingDown(SubmissionData.class);
	}
}
