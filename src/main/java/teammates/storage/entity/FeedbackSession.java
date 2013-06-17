package teammates.storage.entity;

import java.util.Date;

import javax.jdo.annotations.Extension;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Text;

/**
 * Represents an instructor-created Feedback Session. 
 */
@PersistenceCapable
public class FeedbackSession {

	public enum FeedbackSessionType {
		STANDARD, TEAM, PRIVATE;
	}
	
	// Format is feedbackSessionName%courseId
	@PrimaryKey
	@Persistent
	private transient String feedbackSessionId;
	
	@Persistent
	private String feedbackSessionName;
	
	@Persistent
	private String courseId;
	
	@Persistent
	private String creatorEmail;
	
	@Persistent
	@Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
	private Text instructions;
	
	@Persistent
	@Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
	private Date createdTime;
	
	@Persistent
	@Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
	private Date startTime;
	
	@Persistent
	@Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
	private Date endTime;
	
	@Persistent
	@Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
	private Date sessionVisibleFromTime;
	
	@Persistent
	@Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
	private Date resultsVisibleFromTime;
	
	@Persistent
	@Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
	private int timeZone;
	
	@Persistent
	@Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
	private int gracePeriod;
	
	@Persistent
	private FeedbackSessionType feedbackSessionType;
	
	@Persistent
	private boolean sentOpenEmail;
	
	@Persistent
	private boolean sentPublishedEmail;
	
	
	public FeedbackSession(String feedbackSessionName, String courseId,
			String creatorEmail, Text instructions, Date createdTime, Date startTime, Date endTime,
			Date sessionVisibleFromTime, Date resultsVisibleFromTime, int timeZone, int gracePeriod,
			FeedbackSessionType feedbackSessionType, boolean sentOpenEmail, boolean sentPublishedEmail) {
		
		this.feedbackSessionName = feedbackSessionName;
		this.courseId = courseId;		
		this.creatorEmail = creatorEmail;
		this.instructions = instructions;
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
		this.feedbackSessionId = this.feedbackSessionName + "%" + this.courseId;
	}
		
	public String getFeedbackSessionName() {
		return feedbackSessionName;
	}

	public void setFeedbackSessionName(String feedbackSessionName) {
		this.feedbackSessionName = feedbackSessionName;
	}

	public String getCourseId() {
		return courseId;
	}

	public void setCourseId(String courseId) {
		this.courseId = courseId;
	}

	public String getCreatorEmail() {
		return creatorEmail;
	}

	public void setCreatorEmail(String creatorId) {
		this.creatorEmail = creatorId;
	}

	public Text getInstructions() {
		return instructions;
	}

	public void setInstructions(Text instructions) {
		this.instructions = instructions;
	}

	public Date getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(Date createdTime) {
		this.createdTime = createdTime;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public Date getSessionVisibleFromTime() {
		return sessionVisibleFromTime;
	}

	public void setSessionVisibleFromTime(Date sessionVisibleFromTime) {
		this.sessionVisibleFromTime = sessionVisibleFromTime;
	}

	public Date getResultsVisibleFromTime() {
		return resultsVisibleFromTime;
	}

	public void setResultsVisibleFromTime(Date resultsVisibleFromTime) {
		this.resultsVisibleFromTime = resultsVisibleFromTime;
	}
	
	public int getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(int timeZone) {
		this.timeZone = timeZone;
	}

	public int getGracePeriod() {
		return gracePeriod;
	}

	public void setGracePeriod(int gracePeriod) {
		this.gracePeriod = gracePeriod;
	}

	public FeedbackSessionType getFeedbackSessionType() {
		return feedbackSessionType;
	}

	public void setFeedbackSessionType(FeedbackSessionType feedbackSessionType) {
		this.feedbackSessionType = feedbackSessionType;
	}

	public boolean isSentOpenEmail() {
		return sentOpenEmail;
	}

	public void setSentOpenEmail(boolean activated) {
		this.sentOpenEmail = activated;
	}

	public boolean isSentPublishedEmail() {
		return sentPublishedEmail;
	}

	public void setSentPublishedEmail(boolean sentPublishedEmail) {
		this.sentPublishedEmail = sentPublishedEmail;
	}

	@Override
	public String toString() {
		return "FeedbackSession [feedbackSessionName=" + feedbackSessionName
				+ ", courseId=" + courseId + ", creatorId=" + creatorEmail
				+ ", instructions=" + instructions + ", createdTime="
				+ createdTime + ", startTime=" + startTime + ", endTime="
				+ endTime + ", sessionVisibleFromTime="
				+ sessionVisibleFromTime + ", resultsVisibleFromTime="
				+ resultsVisibleFromTime + ", timeZone=" + timeZone
				+ ", gracePeriod=" + gracePeriod + ", feedbackSessionType="
				+ feedbackSessionType + ", sentOpenEmail=" + sentOpenEmail
				+ ", sentPublishedEmail=" + sentPublishedEmail + "]";
	}

}
