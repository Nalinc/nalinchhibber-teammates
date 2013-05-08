package teammates.common.datatransfer;

import java.util.Date;

import teammates.common.Common;
import teammates.common.FieldValidator;
import teammates.storage.entity.Account;

public class AccountAttributes extends EntityAttributes {
	public String googleId;
	public String name;
	public boolean isInstructor;
	
	// Other Information
	public String email;
	public String institute;
	
	public Date createdAt;
	
	public static final String ERROR_FIELD_ID = "GoogleID Field is invalid\n";
	public static final String ERROR_FIELD_NAME = "Name field cannot be null or empty\n";
	public static final String ERROR_FIELD_EMAIL = "Email Field is invalid\n";
	public static final String ERROR_FIELD_INSTITUTE = "Institute field cannot be null or empty\n";
	
	public AccountAttributes(Account a) {
		googleId = a.getGoogleId();
		name = a.getName();
		isInstructor = a.isInstructor();
		email = a.getEmail();
		institute = a.getInstitute();
		createdAt = a.getCreatedAt();
	}
	
	public AccountAttributes() {
		
	}
	
	public AccountAttributes(String googleId, String name, boolean isInstructor,
				String email, String institute) {
		this.googleId = Common.trimIfNotNull(googleId);
		this.name = Common.trimIfNotNull(name);
		this.isInstructor = isInstructor;
		this.email = Common.trimIfNotNull(email);
		this.institute = Common.trimIfNotNull(institute);
	}
	
	public Account toEntity() {
		return new Account(googleId, name, isInstructor, email, institute);
	}
	
	public String getInvalidStateInfo() {
		FieldValidator validator = new FieldValidator();
		String errorMessage = 
				validator.getInvalidStateInfo(FieldValidator.FieldType.PERSON_NAME, name) +
				validator.getInvalidStateInfo(FieldValidator.FieldType.INSTITUTE_NAME, institute);

		if (!Common.isValidGoogleId(googleId)) {
			errorMessage += ERROR_FIELD_ID;
		}
		
		if (!Common.isValidEmail(email)) {
			errorMessage += ERROR_FIELD_EMAIL;
		}
		
		return errorMessage;

	}
}
