package teammates.common.datatransfer;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Text;

import teammates.common.util.Const;
import teammates.common.util.FieldValidator;
import teammates.common.util.Sanitizer;
import teammates.common.util.TimeHelper;
import teammates.common.util.FieldValidator.FieldType;
import teammates.common.util.Utils;
import teammates.storage.entity.FeedbackSession;

public class FeedbackSessionAttributes extends EntityAttributes {
	
	public String feedbackSessionName;
	public String courseId;
	public String creatorEmail;
	public Text instructions;
	public Date createdTime;
	public Date startTime;
	public Date endTime;
	public Date sessionVisibleFromTime;
	public Date resultsVisibleFromTime;
	public int timeZone;
	public int gracePeriod;
	public FeedbackSessionType feedbackSessionType;
	public boolean sentOpenEmail;
	public boolean sentPublishedEmail;
	
	@SuppressWarnings("unused")
	private static Logger log = Utils.getLogger();

	public FeedbackSessionAttributes() {
		
	}
	
	public FeedbackSessionAttributes(FeedbackSession fs) {
		this.feedbackSessionName = fs.getFeedbackSessionName();
		this.courseId = fs.getCourseId();
		this.creatorEmail = fs.getCreatorEmail();
		this.instructions = fs.getInstructions();
		this.createdTime = fs.getCreatedTime();
		this.startTime = fs.getStartTime();
		this.endTime = fs.getEndTime();
		this.sessionVisibleFromTime = fs.getSessionVisibleFromTime();
		this.resultsVisibleFromTime = fs.getResultsVisibleFromTime();
		this.timeZone = fs.getTimeZone();
		this.gracePeriod = fs.getGracePeriod();
		this.feedbackSessionType = fs.getFeedbackSessionType();
		this.sentOpenEmail = fs.isSentOpenEmail();
		this.sentPublishedEmail = fs.isSentPublishedEmail();
	}
		
	public FeedbackSessionAttributes(String feedbackSessionName,
			String courseId, String creatorId, Text instructions,
			Date createdTime, Date startTime, Date endTime,
			Date sessionVisibleFromTime, Date resultsVisibleFromTime,
			int timeZone, int gracePeriod, FeedbackSessionType feedbackSessionType,
			boolean sentOpenEmail, boolean sentPublishedEmail) {
		this.feedbackSessionName = Sanitizer.sanitizeTitle(feedbackSessionName);
		this.courseId = Sanitizer.sanitizeTitle(courseId);
		this.creatorEmail = Sanitizer.sanitizeGoogleId(creatorId);
		this.instructions = Sanitizer.sanitizeTextField(instructions);
		this.createdTime = createdTime;
		this.startTime = startTime;
		this.endTime = endTime;
		this.sessionVisibleFromTime = sessionVisibleFromTime;
		this.resultsVisibleFromTime = resultsVisibleFromTime;
		this.timeZone = timeZone;
		this.gracePeriod = gracePeriod;
		this.feedbackSessionType = feedbackSessionType;
		this.sentOpenEmail = sentOpenEmail;
		this.sentPublishedEmail = sentPublishedEmail;
	}
	
	

	@Override
	public FeedbackSession toEntity() {
		return new FeedbackSession(feedbackSessionName,
				courseId, creatorEmail, instructions,
				createdTime, startTime, endTime,
				sessionVisibleFromTime, resultsVisibleFromTime,
				timeZone, gracePeriod,
				feedbackSessionType, sentOpenEmail, sentPublishedEmail);
	}
	
	@Override
	public String getIdentificationString() {
		return this.feedbackSessionName + "/" + this.courseId;
	}

	@Override
	public String getEntityTypeAsString() {
		return "Feedback Session";
	}
	
	@Override
	public List<String> getInvalidityInfo() {
		
		FieldValidator validator = new FieldValidator();
		List<String> errors = new ArrayList<String>();
		String error;
		
		// Check for null fields.

		error= validator.getValidityInfoForNonNullField("feedback session name", feedbackSessionName);
		if(!error.isEmpty()) { errors.add(error); }
		
		error= validator.getValidityInfoForNonNullField("course ID", courseId);
		if(!error.isEmpty()) { errors.add(error); }
		
		error= validator.getValidityInfoForNonNullField("instructions to students", instructions);
		if(!error.isEmpty()) { errors.add(error); }
		
		error= validator.getValidityInfoForNonNullField("time for the session to become visible", sessionVisibleFromTime);
		if(!error.isEmpty()) { errors.add(error); }
		
		error= validator.getValidityInfoForNonNullField("creator's email", creatorEmail);
		if(!error.isEmpty()) { errors.add(error); }
			
		error= validator.getValidityInfoForNonNullField("session creation time", createdTime);
		if(!error.isEmpty()) { errors.add(error); }
		
		// Early return if any null fields
		if (!errors.isEmpty()) {
			return errors;
		}
		
		error= validator.getInvalidityInfo(FieldType.FEEDBACK_SESSION_NAME, feedbackSessionName);
		if(!error.isEmpty()) { errors.add(error); }
		
		error= validator.getInvalidityInfo(FieldType.COURSE_ID, courseId);
		if(!error.isEmpty()) { errors.add(error); }
		
		error= validator.getInvalidityInfo(FieldType.EMAIL, "creator's email", creatorEmail);
		if(!error.isEmpty()) { errors.add(error); }
		
		// Skip time frame checks if session type is private.
		if (this.isPrivateSession()) {
			return errors;
		}
		
		error= validator.getValidityInfoForNonNullField("submission opening time", startTime);
		if(!error.isEmpty()) { errors.add(error); }
		
		error= validator.getValidityInfoForNonNullField("submission closing time", endTime);
		if(!error.isEmpty()) { errors.add(error); }
		
		error= validator.getValidityInfoForNonNullField("time for the responses to become visible", resultsVisibleFromTime);
		if(!error.isEmpty()) { errors.add(error); }
		
		// Early return if any null fields
		if (!errors.isEmpty()) {
			return errors;
		}		
		
		error= validator.getValidityInfoForTimeFrame(FieldType.FEEDBACK_SESSION_TIME_FRAME,
				FieldType.START_TIME, FieldType.END_TIME, startTime, endTime);
		if(!error.isEmpty()) { errors.add(error); }	
				
		error= validator.getValidityInfoForTimeFrame(FieldType.FEEDBACK_SESSION_TIME_FRAME,
				FieldType.SESSION_VISIBLE_TIME, FieldType.START_TIME, sessionVisibleFromTime, startTime);
		if(!error.isEmpty()) { errors.add(error); }	
		
		Date actualSessionVisibleFromTime = sessionVisibleFromTime;
		if (actualSessionVisibleFromTime.equals(Const.TIME_REPRESENTS_FOLLOW_OPENING)) {
			actualSessionVisibleFromTime = startTime;
		}
		error= validator.getValidityInfoForTimeFrame(FieldType.FEEDBACK_SESSION_TIME_FRAME,
				FieldType.SESSION_VISIBLE_TIME, FieldType.RESULTS_VISIBLE_TIME, actualSessionVisibleFromTime, resultsVisibleFromTime);
		if(!error.isEmpty()) { errors.add(error); }	
		
		return errors;
	}

	@Override
	public boolean isValid() {
		return getInvalidityInfo().isEmpty();
	}
	
	//TODO: if both methods are similar, we can extract it to TimeHelper class.
	// Copied from EvaluationsAttributes. To Unit Test.
	public boolean isClosingWithinTimeLimit(int hours) {
		
		Calendar now = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		// Fix the time zone accordingly
		now.add(Calendar.MILLISECOND,
				(int) (60 * 60 * 1000 * timeZone));
		
		Calendar start = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		start.setTime(startTime);
		
		Calendar deadline = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		deadline.setTime(endTime);

		long nowMillis = now.getTimeInMillis();
		long deadlineMillis = deadline.getTimeInMillis();
		long differenceBetweenDeadlineAndNow = (deadlineMillis - nowMillis)
				/ (60 * 60 * 1000);

		// If now and start are almost similar, it means the evaluation 
		// is open for only 24 hours.
		// Hence we do not send a reminder e-mail for the evaluation.
		return now.after(start)
				&& (differenceBetweenDeadlineAndNow >= hours - 1 
				&& differenceBetweenDeadlineAndNow < hours);
	}
	
	/**
	 * @return {@code true} if it is after the closing time of this feedback session; {@code false} if not.
	 */
	public boolean isClosed() {
		Calendar now = TimeHelper.now(timeZone);
		Calendar end = TimeHelper.dateToCalendar(endTime);
		end.add(Calendar.MINUTE, gracePeriod);
		return (now.after(end));
	}
	
	/**
	 * @return {@code true} is currently open and accepting responses.
	 */
	public boolean isOpened() {		
		Calendar now = TimeHelper.now(timeZone);		
		Calendar start = TimeHelper.dateToCalendar(startTime);
		Calendar end = TimeHelper.dateToCalendar(endTime);
		end.add(Calendar.MINUTE, gracePeriod);
		return (now.after(start) && now.before(end));
	}
	
	/**
	 * @return {@code true} has not opened before and is waiting to open.<br> 
	 * {@code false} if session has opened before.
	 */
	public boolean isWaitingToOpen() {
		Calendar now = TimeHelper.now(timeZone);		
		Calendar start = TimeHelper.dateToCalendar(startTime);
		return (now.before(start));
	}
	
	/**
	 * @return {@code true} if the session is visible; {@code false} if not.
	 * Does not care if the session has started or not.
	 */
	public boolean isVisible() {
		Date now = TimeHelper.now(timeZone).getTime();
		Date visibleTime = this.sessionVisibleFromTime;
		
		if (visibleTime.equals(Const.TIME_REPRESENTS_FOLLOW_OPENING)) {
			visibleTime = this.startTime;
		} else if (visibleTime.equals(Const.TIME_REPRESENTS_NEVER)) {
			return false;
		} 
		return (visibleTime.before(now));
	}
	
	/**
	 * @return {@code true} if the results of the feedback session is visible; {@code false} if not.
	 * Does not care if the session has ended or not.
	 */
	public boolean isPublished() {
		Date now = TimeHelper.now(timeZone).getTime();
		Date publishTime = this.resultsVisibleFromTime;
		
		if (publishTime.equals(Const.TIME_REPRESENTS_FOLLOW_VISIBLE)) {
			return isVisible();
		} else if (publishTime.equals(Const.TIME_REPRESENTS_LATER)) {
			return false;
		} else if (publishTime.equals(Const.TIME_REPRESENTS_NEVER)) {
			return false;
		} else if (publishTime.equals(Const.TIME_REPRESENTS_NOW)) {
			return true;
		} else {
			return (publishTime.before(now));
		}
	}
	
	/**
	 * @return {@code true} if the session has been set by the creator to be manually published. 
	 */
	public boolean isManuallyPublished(){
		return resultsVisibleFromTime.equals(Const.TIME_REPRESENTS_LATER) || 
				 resultsVisibleFromTime.equals(Const.TIME_REPRESENTS_NOW);
	}
	
	/** 
	 * @return {@code true} if session is a private session (only open to the session creator),
	 *  {@code false} if not.
	 */
	public boolean isPrivateSession() {
		return sessionVisibleFromTime.equals(Const.TIME_REPRESENTS_NEVER) || 
				feedbackSessionType.equals(FeedbackSessionType.PRIVATE);
	}
	
	public boolean isCreator(String instructorEmail) {
		return creatorEmail.equals(instructorEmail);
	}
	
	@Override
	public void sanitizeForSaving() {
		// TODO implement this
	}

	@Override
	public String toString() {
		return "FeedbackSessionAttributes [feedbackSessionName="
				+ feedbackSessionName + ", courseId=" + courseId
				+ ", creatorEmail=" + creatorEmail + ", instructions=" + instructions
				+ ", startTime=" + startTime
				+ ", endTime=" + endTime + ", sessionVisibleFromTime="
				+ sessionVisibleFromTime + ", resultsVisibleFromTime="
				+ resultsVisibleFromTime + ", timeZone=" + timeZone
				+ ", gracePeriod=" + gracePeriod + ", feedbackSessionType="
				+ feedbackSessionType + ", sentOpenEmail=" + sentOpenEmail
				+ ", sentPublishedEmail=" + sentPublishedEmail + "]";
	}
}
