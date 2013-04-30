package teammates.common.datatransfer;

import java.util.HashMap;

import teammates.common.Assumption;
import teammates.common.Common;
import teammates.common.exception.InvalidParametersException;
import teammates.storage.entity.Instructor;

public class InstructorData extends BaseData {
	public HashMap<String, CourseData> courses;
	public String googleId;
	public String name;
	public String email;
	
	// New
	public String courseId;
	
	public static final String ERROR_FIELD_ID = "Instructor ID is invalid\n";
	public static final String ERROR_FIELD_NAME = "Instructor name cannot be null or empty\n";
	public static final String ERROR_FIELD_EMAIL = "Instructor email is invalid\n";
	public static final String ERROR_FIELD_COURSEID = "Course ID is invalid\n";
	public static final String ERROR_INSTRUCTOR_LINE_NULL = "Instructor line was null";
	public static final String ERROR_INFORMATION_INCORRECT = "Please enter information in the format: GoogleID | Name | Email\n";

	public InstructorData(String id, String courseId, String name, String email) {
		this.googleId = id;
		this.courseId = courseId;
		this.name = name;
		this.email = email;
	}
	
	public InstructorData(Instructor instructor) {
		this.googleId = instructor.getGoogleId();
		this.courseId = instructor.getCourseId();
		this.name = instructor.getName();
		this.email = instructor.getEmail();
	}

	public InstructorData() {
		
	}
	
	public InstructorData(String courseId, String information) throws InvalidParametersException {
		Assumption.assertNotNull(ERROR_INSTRUCTOR_LINE_NULL, information);
		
		String[] parts = information.replace("|", "\t").split("\t");
		
		if (parts.length != 3) {
			throw new InvalidParametersException(ERROR_INFORMATION_INCORRECT);
		}
		
		this.googleId = Common.sanitizeGoogleId(parts[0]);
		Assumption.assertEquals(true, Common.isValidGoogleId(googleId));

		this.courseId = courseId;
		this.name = parts[1].trim();
		this.email = parts[2].trim();
	}

	public Instructor toEntity() {
		return new Instructor(googleId, courseId, name, email);
	}

	public String getInvalidStateInfo() {
		String errorMessage = "";

		if (!Common.isValidGoogleId(googleId)) {
			errorMessage += ERROR_FIELD_ID;
		}
		
		if (!Common.isValidCourseId(courseId)) {
			errorMessage += ERROR_FIELD_COURSEID;
		}
		
		if (!Common.isValidName(name)) {
			errorMessage += ERROR_FIELD_NAME;
		}
		
		if (!Common.isValidEmail(email)) {
			errorMessage += ERROR_FIELD_EMAIL;
		}

		return errorMessage;
	}
}
