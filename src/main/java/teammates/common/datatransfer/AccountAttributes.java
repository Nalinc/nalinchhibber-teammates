package teammates.common.datatransfer;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import teammates.common.Common;
import teammates.common.FieldValidator;
import teammates.common.Sanitizer;
import teammates.storage.entity.Account;

/**
 * A data transfer object for Account entities.
 */
public class AccountAttributes extends EntityAttributes {
	
	//Note: be careful when changing these variables as their names are used in *.json files.
	
	public String googleId;
	public String name;
	public boolean isInstructor;
	public String email;
	public String institute;
	public Date createdAt;
	
	
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
		this.googleId = Sanitizer.sanitizeGoogleId(googleId);
		this.name = Sanitizer.sanitizeName(name);
		this.isInstructor = isInstructor;
		this.email = Sanitizer.sanitizeEmail(email);
		this.institute = Sanitizer.sanitizeTitle(institute);
	}
	
	public List<String> getInvalidityInfo() {
		
		FieldValidator validator = new FieldValidator();
		List<String> errors = new ArrayList<String>();
		String error;
		
		error= validator.getInvalidityInfo(FieldValidator.FieldType.PERSON_NAME, name);
		if(!error.isEmpty()) { errors.add(error); }
		
		error= validator.getInvalidityInfo(FieldValidator.FieldType.GOOGLE_ID, googleId);
		if(!error.isEmpty()) { errors.add(error); }
		
		error= validator.getInvalidityInfo(FieldValidator.FieldType.EMAIL, email);
		if(!error.isEmpty()) { errors.add(error); }
		
		error= validator.getInvalidityInfo(FieldValidator.FieldType.INSTITUTE_NAME, institute);
		if(!error.isEmpty()) { errors.add(error); }
		
		//No validation for isInstructor and createdAt fields.
		return errors;
	}

	public Account toEntity() {
		return new Account(googleId, name, isInstructor, email, institute);
	}
	
	public String toString(){
		return Common.getTeammatesGson().toJson(this, AccountAttributes.class);
	}

	@Override
	public String getIdentificationString() {
		return this.googleId;
	}

	@Override
	public String getEntityTypeAsString() {
		return "Account";
	}
	
}
