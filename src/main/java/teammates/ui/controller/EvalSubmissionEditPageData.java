package teammates.ui.controller;

import java.util.List;

import teammates.common.Common;
import teammates.common.datatransfer.AccountAttributes;
import teammates.common.datatransfer.EvaluationAttributes;
import teammates.common.datatransfer.StudentAttributes;
import teammates.common.datatransfer.SubmissionAttributes;

public class EvalSubmissionEditPageData extends PageData {
	
	public EvaluationAttributes eval;
	public StudentAttributes student;
	public List<SubmissionAttributes> submissions;
	public String disableAttribute = "";
	
	
	public EvalSubmissionEditPageData(AccountAttributes account) {
		super(account);
	}
	
	/**
	 * @return The p2p comments in the submission, if one exists. If it is empty, inserts sub headings.
	 */
	public String getP2PComments(SubmissionAttributes sub) {
		String commentsString = PageData.escapeForHTML(sub.p2pFeedback.getValue());
		if (commentsString.trim().equals("")){
			if(sub.reviewee.equals(sub.reviewer)) {
				return "";
			} else {
				return "<<What I appreciate about you as a team member>>:\n\n<<Areas you can improve further>>:\n\n<<Other comments>>:";
			}
		} else {
			return commentsString;
		}
	 		
	}
	
	/**
	 * Returns the section title of evaluation depending on whether it is
	 * an evaluation submission for self or for others.
	 */
	public String getEvaluationSectionTitle(SubmissionAttributes sub){
		if(sub.reviewee.equals(sub.reviewer)){
			return "Self evaluation in Team [" + student.team + "]";
		} else {
			return "Evaluation for " + sub.details.revieweeName;
		}
	}
	
	/**
	 * Returns the descriptor for the justification field depending on whether it is
	 * an evaluation submission for self or for others.
	 */
	public String getJustificationInstr(SubmissionAttributes sub){
		if(sub.reviewee.equals(sub.reviewer)){
			return "Comments about my contribution:<br />" +
					"(shown to other teammates)";
		} else {
			return "My comments about this teammate:<br />" + 
					"(confidential and only shown to instructor)";
		}
	}
	
	/**
	 * Returns the comments (p2pfeedback) descriptor depending on whether
	 * it is an evaluation submission for self or for others.
	 */
	public String getCommentsInstr(SubmissionAttributes sub){
		if(sub.reviewee.equals(sub.reviewer)){
			return "Comments about team dynamics:<br />" +
					"(confidential and only shown to instructor)";
		} else {
			return "My feedback to this teammate:<br />" + 
					"(shown anonymously to the teammate)";
		}
	}
	
	/**
	 * Returns the options for contribution share in a team.
	 * This will also select the one which is already selected as shown in the
	 * given SubmissionData. If the submission data is a new data, then by
	 * default "Not Sure" is chosen.
	 */
	public String getEvaluationOptions(SubmissionAttributes sub){
		String result = "";
		if(sub.points==Common.POINTS_NOT_SUBMITTED ||
				sub.points==Common.UNINITIALIZED_INT){
			sub.points=Common.POINTS_NOT_SURE;
		}
		for(int i=200; i>=0; i-=10){
			result += "<option value=\"" + i + "\"" +
						(i==sub.points
						? "selected=\"selected\""
						: "") +
						">" + convertToEqualShareFormat(i) +
						"</option>\r\n";
		}
		result+="<option value=\"" + Common.POINTS_NOT_SURE + "\""
				+ (sub.points==-101 ? " selected=\"selected\"" : "") + ">" +
				"Not Sure</option>";
		return result;
	}
	
	private String convertToEqualShareFormat(int i) {
		if (i > 100)
			return "Equal share + " + (i - 100) + "%"; // Do more
		else if (i == 100)
			return "Equal share"; // Do same
		else if (i > 0)
			return "Equal share - " + (100 - i) + "%"; // Do less
		else
			return "0%"; // Do none
	}
	
}
