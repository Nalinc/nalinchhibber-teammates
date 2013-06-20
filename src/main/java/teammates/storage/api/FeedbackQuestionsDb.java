package teammates.storage.api;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.JDOHelper;
import javax.jdo.Query;

import teammates.common.Assumption;
import teammates.common.Common;
import teammates.common.FeedbackParticipantType;
import teammates.common.datatransfer.EntityAttributes;
import teammates.common.datatransfer.FeedbackQuestionAttributes;
import teammates.common.exception.EntityDoesNotExistException;
import teammates.common.exception.InvalidParametersException;
import teammates.storage.entity.FeedbackQuestion;

public class FeedbackQuestionsDb extends EntitiesDb {
	
	public static final String ERROR_UPDATE_NON_EXISTENT = "Trying to update non-existent Feedback Question : ";
	private static final Logger log = Common.getLogger();

	/**
	 * Preconditions: <br>
	 * * All parameters are non-null. 
	 * @return Null if not found.
	 */
	public FeedbackQuestionAttributes getFeedbackQuestion (String feedbackQuestionId) {
		
		Assumption.assertNotNull(Common.ERROR_DBLEVEL_NULL_INPUT, feedbackQuestionId);

		FeedbackQuestion fq = getFeedbackQuestionEntity(feedbackQuestionId);
		
		if(fq == null) {
			log.info("Trying to get non-existent Question: " + feedbackQuestionId);
			return null;
		}
		return new FeedbackQuestionAttributes(fq);		
	}
	
	/**
	 * Preconditions: <br>
	 * * All parameters are non-null. 
	 * @return Null if not found.
	 */
	public FeedbackQuestionAttributes getFeedbackQuestion(
			String feedbackSessionName,
			String courseId,
			int questionNumber){
		
		Assumption.assertNotNull(Common.ERROR_DBLEVEL_NULL_INPUT, feedbackSessionName);
		Assumption.assertNotNull(Common.ERROR_DBLEVEL_NULL_INPUT, courseId);
		Assumption.assertNotNull(Common.ERROR_DBLEVEL_NULL_INPUT, questionNumber);

		FeedbackQuestion fq = getFeedbackQuestionEntity(feedbackSessionName,
				courseId, questionNumber);
		
		if(fq == null) {
			log.info("Trying to get non-existent Question: "
						+ questionNumber + "." + feedbackSessionName + "/" + courseId);
			return null;
		}
		return new FeedbackQuestionAttributes(fq);		
	}
	
	/**
	 * Preconditions: <br>
	 * * All parameters are non-null. 
	 * @return An empty list if no such questions are found.
	 */
	public List<FeedbackQuestionAttributes> getFeedbackQuestionsForSession(
			String feedbackSessionName, String courseId) {

		Assumption.assertNotNull(Common.ERROR_DBLEVEL_NULL_INPUT, feedbackSessionName);
		Assumption.assertNotNull(Common.ERROR_DBLEVEL_NULL_INPUT, courseId);

		List<FeedbackQuestion> questions = getFeedbackQuestionEntitiesForSession(
				feedbackSessionName, courseId);
		List<FeedbackQuestionAttributes> fqList = new ArrayList<FeedbackQuestionAttributes>();

		for (FeedbackQuestion question : questions) {
			fqList.add(new FeedbackQuestionAttributes(question));
		}
		return fqList;
	}
	
	/**
	 * Preconditions: <br>
	 * * All parameters are non-null. 
	 * @return An empty list if no such questions are found.
	 */
	public List<FeedbackQuestionAttributes> getFeedbackQuestionsForGiverType(
			String feedbackSessionName, String courseId, FeedbackParticipantType giverType) {

		Assumption.assertNotNull(Common.ERROR_DBLEVEL_NULL_INPUT, feedbackSessionName);
		Assumption.assertNotNull(Common.ERROR_DBLEVEL_NULL_INPUT, courseId);
		Assumption.assertNotNull(Common.ERROR_DBLEVEL_NULL_INPUT, giverType);

		List<FeedbackQuestion> questions = getFeedbackQuestionEntitiesForGiverType(
				feedbackSessionName, courseId, giverType);
		List<FeedbackQuestionAttributes> fqList = new ArrayList<FeedbackQuestionAttributes>();

		for (FeedbackQuestion question : questions) {
			fqList.add(new FeedbackQuestionAttributes(question));
		}
		return fqList;
	}
	
	/**
	 * Updates the feedback question identified by `{@code newAttributes.getId()} 
	 * For the remaining parameters, the existing value is preserved 
	 *   if the parameter is null (due to 'keep existing' policy).<br> 
	 * Preconditions: <br>
	 * * {@code newAttributes.getId()} is non-null and
	 *  correspond to an existing feedback question. <br>
	 * @throws InvalidParametersException 
	 * @throws EntityDoesNotExistException 
	 */
	public void updateFeedbackQuestion (FeedbackQuestionAttributes newAttributes) throws InvalidParametersException, EntityDoesNotExistException {
		Assumption.assertNotNull(
				Common.ERROR_DBLEVEL_NULL_INPUT, 
				newAttributes);
		
		if (!newAttributes.isValid()) {
			throw new InvalidParametersException(newAttributes.getInvalidityInfo());
		}
		
		FeedbackQuestion fq = (FeedbackQuestion) getEntity(newAttributes);
		
		if (fq == null) {
			throw new EntityDoesNotExistException(
					ERROR_UPDATE_NON_EXISTENT + newAttributes.toString());
		}
		
		fq.setQuestionNumber(newAttributes.questionNumber);
		fq.setQuestionText(newAttributes.questionText);
		fq.setQuestionType(newAttributes.questionType);
		fq.setGiverType(newAttributes.giverType);
		fq.setRecipientType(newAttributes.recipientType);
		fq.setShowResponsesTo(newAttributes.showResponsesTo);
		fq.setShowGiverNameTo(newAttributes.showGiverNameTo);
		fq.setShowRecipientNameTo(newAttributes.showRecipientNameTo);
		fq.setNumberOfEntitiesToGiveFeedbackTo(newAttributes.numberOfEntitiesToGiveFeedbackTo);
		
		getPM().close();
		
	}
	
	// Gets a question entity if it's Key (feedbackQuestionId) is known.
	private FeedbackQuestion getFeedbackQuestionEntity (String feedbackQuestionId) {
		
		Assumption.assertNotNull(Common.ERROR_DBLEVEL_NULL_INPUT, feedbackQuestionId);

		Query q = getPM().newQuery(FeedbackQuestion.class);
		q.declareParameters("String feedbackQuestionIdParam");
		q.setFilter("feedbackQuestionId == feedbackQuestionIdParam");
		
		@SuppressWarnings("unchecked")
		List<FeedbackQuestion> feedbackQuestionList =
				(List<FeedbackQuestion>) q.execute(feedbackQuestionId);
		
		if (feedbackQuestionList.isEmpty() || JDOHelper.isDeleted(feedbackQuestionList.get(0))) {
			return null;
		}
		return feedbackQuestionList.get(0);
	}
	
	// Gets a feedbackQuestion based on feedbackSessionName and questionNumber.
	private FeedbackQuestion getFeedbackQuestionEntity (
			String feedbackSessionName, String courseId, int questionNumber) {
		
		Query q = getPM().newQuery(FeedbackQuestion.class);
		q.declareParameters("String feedbackSessionNameParam, String courseIdParam, int questionNumberParam");
		q.setFilter("feedbackSessionName == feedbackSessionNameParam && " +
				"courseId == courseIdParam && " +
				"questionNumber == questionNumberParam");
		
		@SuppressWarnings("unchecked")
		List<FeedbackQuestion> feedbackQuestionList =
			(List<FeedbackQuestion>) q.execute(feedbackSessionName, courseId, questionNumber);
		
		if (feedbackQuestionList.isEmpty() || JDOHelper.isDeleted(feedbackQuestionList.get(0))) {
			return null;
		}
		return feedbackQuestionList.get(0);
	}
	
	private List<FeedbackQuestion> getFeedbackQuestionEntitiesForSession (String feedbackSessionName, String courseId) {
		Query q = getPM().newQuery(FeedbackQuestion.class);
		q.declareParameters("String feedbackSessionNameParam, String courseIdParam");
		q.setFilter("feedbackSessionName == feedbackSessionNameParam && courseId == courseIdParam");
		
		@SuppressWarnings("unchecked")
		List<FeedbackQuestion> feedbackQuestionList = 
			(List<FeedbackQuestion>) q.execute(feedbackSessionName, courseId);
		
		return feedbackQuestionList;
	}
	
	private List<FeedbackQuestion> getFeedbackQuestionEntitiesForGiverType(
			String feedbackSessionName, String courseId, FeedbackParticipantType giverType) {
		Query q = getPM().newQuery(FeedbackQuestion.class);
		q.declareParameters("String feedbackSessionNameParam, " +
				"String courseIdParam, " +
				"FeedbackParticipantType giverTypeParam");
		q.declareImports("import teammates.common.FeedbackParticipantType");
		q.setFilter("feedbackSessionName == feedbackSessionNameParam && " +
				"courseId == courseIdParam && " +
				"giverType == giverTypeParam ");
		
		@SuppressWarnings("unchecked")
		List<FeedbackQuestion> feedbackQuestionList = 
			(List<FeedbackQuestion>) q.execute(feedbackSessionName, courseId, giverType);
		
		return feedbackQuestionList;
	}
	
	@Override
	protected Object getEntity(EntityAttributes attributes) {
		FeedbackQuestionAttributes feedbackQuestionToGet = (FeedbackQuestionAttributes) attributes;
		
		if (feedbackQuestionToGet.getId() != null) {
			return getFeedbackQuestionEntity(feedbackQuestionToGet.getId());
		} else {
			return getFeedbackQuestionEntity(
					feedbackQuestionToGet.feedbackSessionName,
					feedbackQuestionToGet.courseId,
					feedbackQuestionToGet.questionNumber);
		}
	}

}
