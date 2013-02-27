package teammates.test.scripts;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import com.google.gson.Gson;

import teammates.common.Common;
import teammates.common.datatransfer.InstructorData;
import teammates.common.datatransfer.CourseData;
import teammates.common.datatransfer.DataBundle;
import teammates.common.datatransfer.EvaluationData;
import teammates.common.datatransfer.StudentData;
import teammates.common.datatransfer.SubmissionData;
import teammates.test.driver.BackDoor;

/**
 * Usage: This script imports a large data bundle to the appengine. The target of the script is the app with
 * appID in the test.properties file.Can use DataGenerator.java to generate random data.
 * 
 * Notes:
 * -Edit SOURCE_FILE_NAME before use
 * -Should not have any limit on the size of the databundle. However, the number of entities per request
 * should not be set to too large as it may cause Deadline Exception (especially for evaluations)
 * 
 */
public class ImportData {
	//  
	// Data source file name (under src/test/resources/data folder) to import
	private static final String SOURCE_FILE_NAME = "DataSourceFileName.json";
	
	private static final int MAX_NUMBER_OF_ENTITY_PER_REQUEST = 100;
	private static final int MAX_NUMBER_OF_EVALUATION_PER_REQUEST = 1;
	private static final int WAIT_TIME_BETWEEN_REQUEST =1000 ;//ms
	
	private static DataBundle data;
	private static Gson gson = Common.getTeammatesGson();
	private static String jsonString;
	
	public static void main(String args[]) throws Exception {
		jsonString = Common.readFile(Common.TEST_DATA_FOLDER+ "/" + SOURCE_FILE_NAME);
		data = gson.fromJson(jsonString, DataBundle.class);
		
		String status = "";
		do
		{
			long start = System.currentTimeMillis();
			
			if(!data.instructors.isEmpty()) {			//Instructors
				status = persist(data.instructors);
			} else if (!data.courses.isEmpty()){	//Courses
				status = persist(data.courses);
			} else if (!data.students.isEmpty()){	//Students
				status = persist(data.students);
			} else if (!data.evaluations.isEmpty()){//Evaluations
				status = persist(data.evaluations);
			} else if (!data.submissions.isEmpty()){//Submissions
				status = persist(data.submissions);
			} else {	
				// No more data, break the loop
				System.out.print("\n Finish!");
				break;
			}
			long elapsedTimeMillis = System.currentTimeMillis()-start;

			// Get elapsed time in seconds of the current request
			float elapsedTimeSec = elapsedTimeMillis/1000F;
			System.out.print(status + " in "+elapsedTimeSec +" s\n");

		}while (true);
		
	}
	
	/**
	 * This method will persist a number of entity and remove them from the source, return the 
	 * status of the operation.
	 *  
	 * @param map - HashMap which has data to persist
	 * @return status of the Backdoor operation
	 */
	private static String persist(@SuppressWarnings("rawtypes") HashMap map)
	{
		DataBundle bundle = new DataBundle();
		int count =0;
		@SuppressWarnings("unchecked")
		Set<String> set = map.keySet();
	    @SuppressWarnings("rawtypes")
		Iterator itr = set.iterator();
	    
	    String type = "";
	    while (itr.hasNext())
	    {
	    	String key = (String) itr.next();
	    	Object obj = map.get(key);
	    	
	    	if(obj instanceof InstructorData)
			{
	    		type = "InstructorData";
				InstructorData instructorData = (InstructorData)obj;
				bundle.instructors.put(key, instructorData);
			} else if(obj instanceof CourseData)
			{
				type = "CourseData";
				CourseData courseData = (CourseData)obj;
				bundle.courses.put(key, courseData);
			} else if(obj instanceof StudentData)
			{
				type = "StudentData";
				StudentData studentData = (StudentData)obj;
				bundle.students.put(key, studentData);
			} else if (obj instanceof EvaluationData)
			{
				type = "EvaluationData";
				EvaluationData evaluationData = (EvaluationData)obj;
				bundle.evaluations.put(key, evaluationData);
			} else if (obj instanceof SubmissionData)
			{
				type = "SubmissionData";
				SubmissionData submissionData = (SubmissionData)obj;
				bundle.submissions.put(key, submissionData);
			}
			count ++;
			itr.remove();
			System.out.print(key + "\n");
			if(type.equals("EvaluationData")&& count >= MAX_NUMBER_OF_EVALUATION_PER_REQUEST)
				break;
			if(count >= MAX_NUMBER_OF_ENTITY_PER_REQUEST)
				break;
	    }
	    System.out.print(count+ " entities of type "+ type + " left " + map.size() +" \n" );
	    
	    String status = BackDoor.persistNewDataBundle(gson.toJson(bundle));
	    
	    // wait a few seconds to allow data to persist completedly
	    try {
			Thread.sleep(WAIT_TIME_BETWEEN_REQUEST);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return status;
	}
}
