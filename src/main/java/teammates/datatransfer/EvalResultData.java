package teammates.datatransfer;

import java.util.ArrayList;

import teammates.api.Common;

public class EvalResultData {
	
	public SubmissionData own;
	public ArrayList<SubmissionData> incoming = new ArrayList<SubmissionData>();
	public ArrayList<SubmissionData> outgoing = new ArrayList<SubmissionData>();
	
	public int claimedActual = Common.UNINITIALIZED_INT;
	public int claimedToStudent = Common.UNINITIALIZED_INT;
	public int claimedToCoord = Common.UNINITIALIZED_INT;
	public int perceivedToCoord = Common.UNINITIALIZED_INT;
	public int perceivedToStudent = Common.UNINITIALIZED_INT;
	

}
