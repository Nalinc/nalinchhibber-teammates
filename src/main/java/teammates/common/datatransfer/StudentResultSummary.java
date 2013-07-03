package teammates.common.datatransfer;

import teammates.common.util.Constants;

/**
 * Represents the contribution ratings for the student for a given evaluation.
 * <br> Contains claimed and perceived values to be shown to the student and the instructor.
 */
public class StudentResultSummary {
	/** The original contribution value claimed by the student  */
	public int claimedFromStudent = Constants.INT_UNINITIALIZED;
	
	/** The normalized 'claimed contribution' value to be shown to the instructor  */
	public int claimedToInstructor = Constants.INT_UNINITIALIZED;
	
	/** The 'de-normalized' perceived contribution, to be shown to the student */
	public int perceivedToStudent = Constants.INT_UNINITIALIZED;
	
	/** The normalized 'perceived contribution' value to be shown to the instructor  */
	public int perceivedToInstructor = Constants.INT_UNINITIALIZED;
	
}
