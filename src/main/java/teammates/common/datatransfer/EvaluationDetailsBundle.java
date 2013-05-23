package teammates.common.datatransfer;

import teammates.common.Common;

/**
 * Represents details of an evaluation. 
 * Contains:
 * <br> * The basic info of the evaluation (as a {@link EvaluationAttributes} object).
 * <br> * Submission statistics (as a {@link EvaluationStats} object).
 */
public class EvaluationDetailsBundle {

	public EvaluationStats stats;
	public EvaluationAttributes evaluation;

	public EvaluationDetailsBundle(EvaluationAttributes evaluation) {
		this.evaluation = evaluation;
		this.stats = new EvaluationStats();
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("course:" + evaluation.courseId + ", name:" + evaluation.name
				+ Common.EOL);
		sb.append("submitted/total: " + stats.submittedTotal + "/" + stats.expectedTotal);
		return sb.toString();
	}

}
