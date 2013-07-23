package teammates.ui.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import teammates.common.datatransfer.AccountAttributes;
import teammates.common.datatransfer.FeedbackSessionQuestionsBundle;

public class InstructorFeedbackSubmissionEditPageData extends PageData {

	public FeedbackSessionQuestionsBundle bundle = null;
	
	public InstructorFeedbackSubmissionEditPageData(AccountAttributes account) {
		super(account);
	}

	public List<String> getRecipientOptionsForQuestion(String feedbackQuestionId, String currentlySelectedOption) {
		ArrayList<String> result = new ArrayList<String>();		
		if(this.bundle == null) {
			return null;
		}
		
		Map<String, String> emailNamePair = this.bundle.getSortedRecipientList(feedbackQuestionId);
				
		for(Map.Entry<String, String> pair : emailNamePair.entrySet()) {
			result.add("<option value=\""+pair.getKey()+"\"" +
					(pair.getKey().equals(currentlySelectedOption) 
						? " selected=\"selected\"" : "") +
					">"+pair.getValue()+"</option>");			
		}

		return result;
	}
}
