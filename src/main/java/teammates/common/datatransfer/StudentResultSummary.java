package teammates.common.datatransfer;

import teammates.common.util.Config;

/**
 * Represents the contribution ratings for the student for a given evaluation.
 * <br> Contains claimed and perceived values to be shown to the student and the instructor.
 */
public class StudentResultSummary {
	/** The original contribution value claimed by the student  */
	public int claimedFromStudent = Config.UNINITIALIZED_INT;
	
	/** The normalized 'claimed contribution' value to be shown to the instructor  */
	public int claimedToInstructor = Config.UNINITIALIZED_INT;
	
	/** The 'de-normalized' perceived contribution, to be shown to the student */
	public int perceivedToStudent = Config.UNINITIALIZED_INT;
	
	/** The normalized 'perceived contribution' value to be shown to the instructor  */
	public int perceivedToInstructor = Config.UNINITIALIZED_INT;
	
}
