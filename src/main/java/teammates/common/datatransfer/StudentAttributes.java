package teammates.common.datatransfer;

import static teammates.common.util.Const.EOL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import teammates.common.exception.EnrollException;
import teammates.common.util.Assumption;
import teammates.common.util.FieldValidator;
import teammates.common.util.Sanitizer;
import teammates.common.util.StringHelper;
import teammates.common.util.FieldValidator.FieldType;
import teammates.storage.entity.Student;

public class StudentAttributes extends EntityAttributes {
	
	//=========================================================================
	public enum UpdateStatus {
		// @formatter:off
		ERROR(0), 
		NEW(1), 
		MODIFIED(2), 
		UNMODIFIED(3), 
		NOT_IN_ENROLL_LIST(4), 
		UNKNOWN(5);
		// @formatter:on

		public final int numericRepresentation;

		private UpdateStatus(int numericRepresentation) {
			this.numericRepresentation = numericRepresentation;
		}

		public static UpdateStatus enumRepresentation(int numericRepresentation) {
			switch (numericRepresentation) {
			case 0:
				return ERROR;
			case 1:
				return NEW;
			case 2:
				return MODIFIED;
			case 3:
				return UNMODIFIED;
			case 4:
				return NOT_IN_ENROLL_LIST;
			case 5:
				return UNKNOWN;
			default:
				return UNKNOWN;
			}
		}
	}

	//=========================================================================
	
	//Note: be careful when changing these variables as their names are used in *.json files.
	public String googleId;
	public String name;
	public String email;
	public String course = null;
	public String comments = null;
	public String team = null;
	public String key = null;

	public UpdateStatus updateStatus = UpdateStatus.UNKNOWN;

	public static final String ERROR_COURSE_ID_NULL = "Course ID was null\n";
	public static final String ERROR_ENROLL_LINE_NULL = "Enroll line was null\n";
	public static final String ERROR_ENROLL_LINE_EMPTY = "Enroll line was empty\n";
	public static final String ERROR_ENROLL_LINE_TOOFEWPARTS = "Enroll line had too few parts\n";
	public static final String ERROR_ENROLL_LINE_TOOMANYPARTS = "Enroll line had too many parts\n";
		
	public StudentAttributes(String id, String email, String name, String comments,
			String courseId, String team) {
		this();
		this.googleId = Sanitizer.sanitizeGoogleId(id);
		this.email = Sanitizer.sanitizeEmail(email);
		this.course = Sanitizer.sanitizeTitle(courseId);
		this.name = Sanitizer.sanitizeName(name);
		this.comments = Sanitizer.sanitizeTextField(comments);
		this.team = Sanitizer.sanitizeTitle(team);
	}

	public StudentAttributes() {
		
	}

	public StudentAttributes(String enrollLine, String courseId)
			throws EnrollException {

		this();
		int TEAM_POS = 0;
		int NAME_POS = 1;
		int EMAIL_POS = 2;
		int COMMENT_POS = 3;

		Assumption.assertNotNull(ERROR_ENROLL_LINE_NULL, enrollLine);
		Assumption.assertNotNull(ERROR_COURSE_ID_NULL, courseId);
			
		if (enrollLine.equals("")) {
			throw new EnrollException(ERROR_ENROLL_LINE_EMPTY);
		}

		String[] parts = enrollLine.replace("|", "\t").split("\t");

		if (parts.length < 3) {
			throw new EnrollException(ERROR_ENROLL_LINE_TOOFEWPARTS);
		} else if (parts.length > 4) {
			throw new EnrollException(ERROR_ENROLL_LINE_TOOMANYPARTS);
		}

		String paramCourseId = courseId;
		String paramTeam = parts[TEAM_POS];
		String paramName = parts[NAME_POS];
		String paramEmail = parts[EMAIL_POS];
		String paramComment = ((parts.length == 4) ? parts[COMMENT_POS] : "");

		this.name = Sanitizer.sanitizeName(paramName);
		this.email = Sanitizer.sanitizeEmail(paramEmail);
		this.course = Sanitizer.sanitizeTitle(paramCourseId);
		this.team = Sanitizer.sanitizeTitle(paramTeam);
		this.comments = Sanitizer.sanitizeTextField(paramComment);
	}

	public StudentAttributes(Student student) {
		this();
		this.email = student.getEmail();
		this.course = student.getCourseId();
		this.name = student.getName();
		this.comments = Sanitizer.sanitizeTextField(student.getComments());
		this.team = Sanitizer.sanitizeTitle(student.getTeamName());
		// TODO: Is this supposed to be null or "" ?? Find out and standardize.
		this.googleId = ((student.getGoogleId() == null) ? "" : student.getGoogleId());
		Long keyAsLong = student.getRegistrationKey();
		this.key = (keyAsLong == null ? null : Student
				.getStringKeyForLongKey(keyAsLong));

		// TODO: this is for backward compatibility with old system. Old system
		// considers "" as unregistered. It should be changed to consider
		// null as unregistered.
	}

	public boolean isRegistered() {
		return googleId != null && !googleId.equals("");
	}

	public boolean isEnrollInfoSameAs(StudentAttributes otherStudent) {
		return (otherStudent != null) && otherStudent.email.equals(this.email)
				&& otherStudent.course.equals(this.course)
				&& otherStudent.name.equals(this.name)
				&& otherStudent.comments.equals(this.comments)
				&& otherStudent.team.equals(this.team);
	}

	public List<String> getInvalidityInfo() {
		
		//id is allowed to be null when the student is not registered
		Assumption.assertTrue(team!=null);
		Assumption.assertTrue(comments!=null);
		
		FieldValidator validator = new FieldValidator();
		List<String> errors = new ArrayList<String>();
		String error;
		
		if (googleId != null && !googleId.isEmpty()) {
			error = validator.getInvalidityInfo(FieldType.GOOGLE_ID, googleId);
			if (!error.isEmpty()) {	errors.add(error);}
		}
		
		error= validator.getInvalidityInfo(FieldType.COURSE_ID, course);
		if(!error.isEmpty()) { errors.add(error); }
		
		error= validator.getInvalidityInfo(FieldType.EMAIL, email);
		if(!error.isEmpty()) { errors.add(error); }
		
		error= validator.getInvalidityInfo(FieldType.TEAM_NAME, team);
		if(!error.isEmpty()) { errors.add(error); }
		
		error= validator.getInvalidityInfo(FieldType.STUDENT_ROLE_COMMENTS, comments);
		if(!error.isEmpty()) { errors.add(error); }
		
		error= validator.getInvalidityInfo(FieldType.PERSON_NAME, name);
		if(!error.isEmpty()) { errors.add(error); }
		
		return errors;
	}
	
	public static void sortByTeamName(List<StudentAttributes> students) {
		Collections.sort(students, new Comparator<StudentAttributes>() {
			public int compare(StudentAttributes s1, StudentAttributes s2) {
				String t1 = s1.team;
				String t2 = s2.team;
				if ((t1 == null) && (t2 == null)) {
					return 0;
				} else if (t1 == null) {
					return 1;
				} else if (t2 == null) {
					return -1;
				}
				return t1.compareTo(t2);
			}
		});
	}
	
	public static void sortByNameAndThenByEmail(List<StudentAttributes> students) {
		Collections.sort(students, new Comparator<StudentAttributes>() {
			public int compare(StudentAttributes s1, StudentAttributes s2) {
				int result = s1.name.compareTo(s2.name);
				if (result == 0)
					result = s1.email.compareTo(s2.email);
				return result;
			}
		});
	}

	public Student toEntity() {
		return new Student(email, name, googleId, comments, course, team);
	}

	public String toString() {
		return toString(0);
	}

	public String toString(int indent) {
		String indentString = StringHelper.getIndent(indent);
		StringBuilder sb = new StringBuilder();
		sb.append(indentString + "Student:" + name + "[" + email + "]" + EOL);
		return sb.toString();
	}

	@Override
	public String getIdentificationString() {
		return this.course + "/" + this.email;
	}

	@Override
	public String getEntityTypeAsString() {
		return "Student";
	}
}
