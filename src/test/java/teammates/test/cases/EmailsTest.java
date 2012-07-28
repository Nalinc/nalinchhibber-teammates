package teammates.test.cases;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import teammates.common.Common;
import teammates.common.datatransfer.CourseData;
import teammates.common.datatransfer.EvaluationData;
import teammates.common.datatransfer.StudentData;
import teammates.logic.BuildProperties;
import teammates.logic.Emails;

import com.google.appengine.tools.development.testing.LocalMailServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

public class EmailsTest extends BaseTestCase {
	private LocalServiceTestHelper helper;
	private LocalMailServiceTestConfig localMailService;

	@BeforeClass
	public static void classSetUp() throws Exception {
		printTestClassHeader();
		setGeneralLoggingLevel(Level.WARNING);
		setLogLevelOfClass(Emails.class, Level.FINE);
		setConsoleLoggingLevel(Level.FINE);
	}

	@Before
	public void caseSetUp() throws ServletException, IOException {
		localMailService = new LocalMailServiceTestConfig();
		helper = new LocalServiceTestHelper(localMailService);
		helper.setUp();
	}

	@Test
	public void testGetEmailInfo() throws MessagingException {

		Session session = Session.getDefaultInstance(new Properties(), null);
		MimeMessage message = new MimeMessage(session);

		String email = "reciever@gmail.com";
		String from = "sender@gmail.com";

		message.addRecipient(Message.RecipientType.TO, new InternetAddress(
				email));

		message.setFrom(new InternetAddress(from));
		String subject = "email subject";
		message.setSubject(subject);
		message.setContent("<h1>email body</h1>", "text/html");

		assertEquals(
				"[Email sent]to=reciever@gmail.com|from=sender@gmail.com|subject=email subject",
				Emails.getEmailInfo(message));
	}

	@Test
	public void testGenerateEvaluationEmailBase() throws IOException,
			MessagingException {

		EvaluationData e = new EvaluationData();
		e.name = "Evaluation Name";
		e.endTime = Common.getDateOffsetToCurrentTime(0);

		CourseData c = new CourseData();
		c.id = "course-id";
		c.name = "Course Name";

		StudentData s = new StudentData();
		s.name = "Student Name";
		s.key = "skxxxxxxxxxks";
		s.email = "student@email.com";

		______TS("generic template, student yet to join");

		String template = BuildProperties.inst().STUDENT_EMAIL_TEMPLATE_EVALUATION_;
		MimeMessage email = new Emails().generateEvaluationEmailBase(c, e, s,
				template);

		// check receiver
		assertEquals(s.email, email.getAllRecipients()[0].toString());

		// check sender
		assertEquals(BuildProperties.inst().TEAMMATES_APP_ADMIN_EMAIL,
				email.getFrom()[0].toString());

		// check subject
		assertEquals(
				"${subjectPrefix} [Course: Course Name][Evaluation: Evaluation Name]",
				email.getSubject());

		// check email body
		String joinUrl = BuildProperties.inst().TEAMMATES_APP_URL
				+ Common.PAGE_STUDENT_JOIN_COURSE;
		joinUrl = Common.addParamToUrl(joinUrl, Common.PARAM_REGKEY, s.key);

		String submitUrl = BuildProperties.inst().TEAMMATES_APP_URL
				+ Common.PAGE_STUDENT_EVAL_SUBMISSION_EDIT;
		submitUrl = Common.addParamToUrl(submitUrl, Common.PARAM_COURSE_ID, c.id);
		submitUrl = Common.addParamToUrl(submitUrl, Common.PARAM_EVALUATION_NAME,
				e.name);

		String deadline = Common.formatTime(e.endTime);

		String emailBody = email.getContent().toString();

		assertContainsRegex("Hello " + s.name + "{*}course <i>" + c.name
				+ "{*}" + joinUrl + "{*}" + joinUrl + "{*}" + c.name + "{*}"
				+ s.key + "{*}${status}{*}" + c.id + "{*}" + c.name + "{*}"
				+ e.name + "{*}" + deadline + "{*}" + submitUrl + "{*}"
				+ submitUrl, emailBody);

		printEmail(email);

		______TS("published template, student yet to join");

		template = BuildProperties.inst().STUDENT_EMAIL_TEMPLATE_EVALUATION_PUBLISHED;
		email = new Emails().generateEvaluationEmailBase(c, e, s, template);

		emailBody = email.getContent().toString();

		assertTrue(emailBody.contains(s.key));
		assertTrue(!emailBody.contains(submitUrl));

		String reportUrl = BuildProperties.inst().TEAMMATES_APP_URL
				+ Common.PAGE_STUDENT_EVAL_RESULTS;
		reportUrl = Common.addParamToUrl(reportUrl, Common.PARAM_COURSE_ID, c.id);
		reportUrl = Common.addParamToUrl(reportUrl, Common.PARAM_EVALUATION_NAME,
				e.name);

		assertContainsRegex("Hello " + s.name + "{*}course <i>" + c.name
				+ "{*}" + joinUrl + "{*}" + joinUrl + "{*}" + c.name + "{*}"
				+ s.key + "{*}is now ready for viewing{*}" + c.id + "{*}"
				+ c.name + "{*}" + e.name + "{*}" + reportUrl + "{*}"
				+ reportUrl, emailBody);

		printEmail(email);

		______TS("generic template, student joined");

		s.id = "student1id"; // set student id to make him "joined"
		template = BuildProperties.inst().STUDENT_EMAIL_TEMPLATE_EVALUATION_;

		email = new Emails().generateEvaluationEmailBase(c, e, s, template);

		emailBody = email.getContent().toString();

		assertTrue(!emailBody.contains(s.key));
		assertContainsRegex("Hello " + s.name + "{*}" + c.id + "{*}" + c.name
				+ "{*}" + e.name + "{*}" + deadline + "{*}" + submitUrl + "{*}"
				+ submitUrl, emailBody);

		printEmail(email);

		______TS("published template, student joined");

		template = BuildProperties.inst().STUDENT_EMAIL_TEMPLATE_EVALUATION_PUBLISHED;
		email = new Emails().generateEvaluationEmailBase(c, e, s, template);

		emailBody = email.getContent().toString();

		assertTrue(!emailBody.contains(s.key));

		assertContainsRegex("Hello " + s.name
				+ "{*}is now ready for viewing{*}" + c.id + "{*}" + c.name
				+ "{*}" + e.name + "{*}" + reportUrl + "{*}" + reportUrl,
				emailBody);

		printEmail(email);

	}

	@Test
	public void testGenerateStudentCourseJoinEmail() throws IOException,
			MessagingException {

		CourseData c = new CourseData();
		c.id = "course-id";
		c.name = "Course Name";

		StudentData s = new StudentData();
		s.name = "Student Name";
		s.key = "skxxxxxxxxxks";
		s.email = "student@email.com";

		MimeMessage email = new Emails().generateStudentCourseJoinEmail(c, s);

		// check receiver
		assertEquals(s.email, email.getAllRecipients()[0].toString());

		// check sender
		assertEquals(BuildProperties.inst().TEAMMATES_APP_ADMIN_EMAIL,
				email.getFrom()[0].toString());

		// check subject
		assertEquals(
				"TEAMMATES: Invitation to join course [Course Name][Course ID: course-id]",
				email.getSubject());

		// check email body
		String joinUrl = BuildProperties.inst().TEAMMATES_APP_URL
				+ Common.PAGE_STUDENT_JOIN_COURSE;
		joinUrl = Common.addParamToUrl(joinUrl, Common.PARAM_REGKEY, s.key);


		String emailBody = email.getContent().toString();

		assertContainsRegex("Hello " + s.name + "{*}course <i>" + c.name
				+ "{*}" + joinUrl + "{*}" + joinUrl + "{*}" + c.name + "{*}"
				+ s.key, emailBody);
		
		assertTrue(!emailBody.contains("$"));

		printEmail(email);
	}

	private void printEmail(MimeMessage email) throws MessagingException,
			IOException {
		print("Here's the generated email (for your eyeballing pleasure):");
		print(".............[Start of email]..............");
		print("Subject: " + email.getSubject());
		print("Body:");
		print(email.getContent().toString());
		print(".............[End of email]................");
	}

	@Test
	public void testGenerateEvaluationEmails() throws MessagingException,
			IOException {
		List<StudentData> students = new ArrayList<StudentData>();

		EvaluationData e = new EvaluationData();
		e.name = "Evaluation Name";
		e.endTime = Common.getDateOffsetToCurrentTime(0);

		CourseData c = new CourseData();
		c.id = "course-id";
		c.name = "Course Name";

		StudentData s1 = new StudentData();
		s1.name = "Student1 Name";
		s1.key = "skxxxxxxxxxks1";
		s1.email = "student1@email.com";
		students.add(s1);

		StudentData s2 = new StudentData();
		s2.name = "Student2 Name";
		s2.key = "skxxxxxxxxxks2";
		s2.email = "student2@email.com";
		students.add(s2);

		______TS("evaluation opening emails");

		List<MimeMessage> emails = new Emails()
				.generateEvaluationOpeningEmails(c, e, students);
		assertEquals(2, emails.size());

		String prefix = Emails.SUBJECT_PREFIX_STUDENT_EVALUATION_OPENING;
		String status = "is now open";
		verifyEvaluationEmail(s1, emails.get(0), prefix, status);
		verifyEvaluationEmail(s2, emails.get(1), prefix, status);

		______TS("evaluation reminders");

		emails = new Emails().generateEvaluationReminderEmails(c, e, students);
		assertEquals(2, emails.size());

		prefix = Emails.SUBJECT_PREFIX_STUDENT_EVALUATION_REMINDER;
		status = "is still open for submissions";
		verifyEvaluationEmail(s1, emails.get(0), prefix, status);
		verifyEvaluationEmail(s2, emails.get(1), prefix, status);

		______TS("evaluation closing alerts");

		emails = new Emails().generateEvaluationClosingEmails(c, e, students);
		assertEquals(2, emails.size());

		prefix = Emails.SUBJECT_PREFIX_STUDENT_EVALUATION_CLOSING;
		status = "is closing soon";
		verifyEvaluationEmail(s1, emails.get(0), prefix, status);
		verifyEvaluationEmail(s2, emails.get(1), prefix, status);

	}

	private void verifyEvaluationEmail(StudentData s, MimeMessage email,
			String prefix, String status) throws MessagingException,
			IOException {
		assertEquals(s.email, email.getAllRecipients()[0].toString());
		assertTrue(email.getSubject().contains(prefix));
		String emailBody = email.getContent().toString();
		assertTrue(emailBody.contains(status));
		assertTrue(!emailBody.contains("$"));
	}

	@After
	public void caseTearDown() {
		helper.tearDown();
	}

	@AfterClass()
	public static void classTearDown() throws Exception {
		printTestClassFooter();
		setLogLevelOfClass(Emails.class, Level.WARNING);
		setConsoleLoggingLevel(Level.WARNING);
	}
}
