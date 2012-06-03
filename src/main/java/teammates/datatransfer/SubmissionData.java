package teammates.datatransfer;

import teammates.persistent.Submission;

import com.google.appengine.api.datastore.Text;

public class SubmissionData {
	/** course ID */
	public String course;
	/** evaluation name */
	public String evaluation;
	/** team name */
	public String team;
	/** reviewer email */
	public String reviewer;
	/** reviewee email */
	public String reviewee;
	public int points;
	public Text justification;
	public Text p2pFeedback;

	public SubmissionData() {

	}

	public SubmissionData(Submission s) {
		this.course = s.getCourseID();
		this.evaluation = s.getEvaluationName();
		this.reviewer = s.getFromStudent();
		this.reviewee = s.getToStudent();
		this.team = s.getTeamName();
		this.points = s.getPoints();
		this.justification = s.getJustification();
		this.p2pFeedback = s.getCommentsToStudent();
	}

	public Submission toSubmission() {
		return new Submission(reviewer, reviewee, course, evaluation, team,
				points, justification, p2pFeedback);
	}

}
