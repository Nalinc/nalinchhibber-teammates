package teammates.testing.lib;

import java.io.IOException;
import java.util.Calendar;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.FlagTerm;

import teammates.testing.config.Config;

public class SharedLib {

	/**
	 * Retrieve registration key sent to Gmail inbox. After retrieve, mark the
	 * email as read.
	 * 
	 * Can be easily modified to support other mail providers
	 * 
	 * @param gmail
	 * @param password
	 * @return
	 * @throws Exception
	 */
	public static String getRegistrationKeyFromGmail(String gmail, String password, String courseId) {
		Session sessioned = Session.getDefaultInstance(System.getProperties(), null);
		Store store = null;
		try {
			store = sessioned.getStore("imaps");
		} catch (NoSuchProviderException e) {
			e.printStackTrace();
		}
		try {
			store.connect("imap.gmail.com", gmail, password);
		} catch (MessagingException e) {
			e.printStackTrace();
		}

		try {
			// Retrieve the "Inbox"
			Folder inbox = store.getFolder("inbox");
			// Reading the Email Index in Read / Write Mode
			inbox.open(Folder.READ_WRITE);
			FlagTerm ft = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
			Message messages[] = inbox.search(ft);

			// Loop over all of the messages
			for (int i = messages.length - 1; i >= 0; i--) {
				Message message = messages[i];
				// If this is the right message (by matching header)

				// Pattern pattern = Pattern
				// .compile("^TEAMMATES: Registration Invitation: Register in the course (\\w+)$");
				// Matcher m = pattern.matcher(message.getSubject());
				if (!message.getSubject().equals(String.format("TEAMMATES: Registration Invitation: Register in the course %s", courseId)))
					continue;

				System.out.println(message.getSubject());

				String body = "";

				if (message.getContent() instanceof String) { // if message is a
					// string
					body = message.getContent().toString();
				} else if (message.getContent() instanceof Multipart) { // if
																		// its a
					// multipart
					// message
					Multipart multipart = (Multipart) message.getContent();
					BodyPart bodypart = multipart.getBodyPart(0);
					body = bodypart.getContent().toString();
				}

				String key = body.split(courseId + ":")[1];
				key = key.split("\\*")[0].trim();

				// Mark the message as read
				message.setFlag(Flags.Flag.SEEN, true);

				return key;
			}
		} catch (IOException e1) {
			e1.printStackTrace();

		} catch (MessagingException e) {
			e.printStackTrace();
			return "";
		}

		return "";
	}

	/**
	 * Retrieve evaluation reminder sent to Gmail inbox. After retrieve, mark
	 * the email as read.
	 * 
	 * Can be easily modified to support other mail providers
	 * 
	 * @param gmail
	 * @param password
	 * @return
	 * @throws Exception
	 */
	public static String getEvaluationReminderFromGmail(String gmail, String password, String courseId, String evalId) throws Exception {
		Session sessioned = Session.getDefaultInstance(System.getProperties(), null);
		Store store = sessioned.getStore("imaps");
		store.connect("imap.gmail.com", gmail, password);

		// Retrieve the "Inbox"
		Folder inbox = store.getFolder("inbox");
		// Reading the Email Index in Read / Write Mode
		inbox.open(Folder.READ_WRITE);
		FlagTerm ft = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
		Message messages[] = inbox.search(ft);

		// Loop over all of the messages
		for (int i = messages.length - 1; i >= 0; i--) {
			Message message = messages[i];

			if (!message.getSubject().equals(String.format("TEAMMATES: Evaluation Reminder: %s %s", courseId, evalId)))
				continue;

			// Mark the message as read
			message.setFlag(Flags.Flag.SEEN, true);

			// Return the courseId
			return courseId;
		}

		return "";
	}

	/**
	 * Helper function - Mark all emails of an account as read.
	 * 
	 */
	public static void markAllEmailsSeen(String username, String password) throws Exception {
		Session sessioned = Session.getDefaultInstance(System.getProperties(), null);
		Store store = sessioned.getStore("imaps");
		store.connect("imap.gmail.com", username, password);

		// Retrieve the "Inbox"
		Folder inbox = store.getFolder("inbox");
		
		// Reading the Email Index in Read / Write Mode
		inbox.open(Folder.READ_WRITE);

		FlagTerm ft = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
		Message messages[] = inbox.search(ft);
		// Message messages[] = inbox.getMessages();

		// Loop over all of the messages
		for (Message message : messages) {
			message.setFlag(Flags.Flag.SEEN, true);
		}
		inbox.close(true);
	}

	/**
	 * Count the number of stress test emails
	 * 
	 * @author wangsha
	 */
	public static int mailStressTestCount(String username, String password) throws Exception {
		Session sessioned = Session.getDefaultInstance(System.getProperties(), null);
		Store store = sessioned.getStore("imaps");
		store.connect("imap.gmail.com", username, password);

		// Retrieve the "Inbox"
		Folder inbox = store.getFolder("inbox");
		// Reading the Email Index in Read / Write Mode
		inbox.open(Folder.READ_WRITE);

		FlagTerm ft = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
		Message messages[] = inbox.search(ft);
		// Message messages[] = inbox.getMessages();
		int count = 0;
		// Loop over all of the messages
		Pattern pattern = Pattern.compile("^Teammates Mail Stree Testing ");
		for (Message message : messages) {
			System.out.println(message.getSubject());
			Matcher m = pattern.matcher(message.getSubject());

			if (!m.find())
				continue;
			count++;

		}
		inbox.close(true);

		return count;
	}

	public static void main(String[] args) {
		try {
//			SharedLib.getRegistrationKeyFromGmail("alice.tmms@gmail.com", Config.inst().TEAMMATES_APP_PASSWD, "CS2103-TESTING");
			SharedLib.getEvaluationReminderFromGmail("alice.tmms@gmail.com", Config.inst().TEAMMATES_APP_PASSWD, "CS2103-TESTING", "First Eval");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Thread println
	 */
	public static void tprintln(String message) {
		System.out.println("[" + Thread.currentThread().getName() + "]" + message);
	}

}
