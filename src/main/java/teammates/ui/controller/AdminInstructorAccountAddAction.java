package teammates.ui.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import teammates.common.datatransfer.DataBundle;
import teammates.common.exception.EntityAlreadyExistsException;
import teammates.common.exception.EntityDoesNotExistException;
import teammates.common.exception.InvalidParametersException;
import teammates.common.util.Assumption;
import teammates.common.util.Config;
import teammates.common.util.Const;
import teammates.common.util.FileHelper;
import teammates.common.util.Utils;
import teammates.logic.api.GateKeeper;
import teammates.logic.backdoor.BackDoorLogic;

import com.google.gson.Gson;

public class AdminInstructorAccountAddAction extends Action {

	@Override
	protected ActionResult execute() throws EntityDoesNotExistException {
		
		new GateKeeper().verifyAdminPrivileges(account);
		
		AdminHomePageData data = new AdminHomePageData(account);
		
		data.instructorId = getRequestParam(Const.ParamsNames.INSTRUCTOR_ID);
		Assumption.assertNotNull(data.instructorId);
		data.instructorName = getRequestParam(Const.ParamsNames.INSTRUCTOR_NAME);
		Assumption.assertNotNull(data.instructorName);
		data.instructorEmail = getRequestParam(Const.ParamsNames.INSTRUCTOR_EMAIL);
		Assumption.assertNotNull(data.instructorEmail);
		data.instructorInstitution = getRequestParam(Const.ParamsNames.INSTRUCTOR_INSTITUTION);
		Assumption.assertNotNull(data.instructorInstitution);
		
		String importSampleData = getRequestParam(Const.ParamsNames.INSTRUCTOR_IMPORT_SAMPLE);
		
		data.instructorId = data.instructorId.trim();
		data.instructorName = data.instructorName.trim();
		data.instructorEmail = data.instructorEmail.trim();
		data.instructorInstitution = data.instructorInstitution.trim();

		if (logic.isInstructor(data.instructorId)) {
			isError = true;
			String errorMessage = "The Google ID " + data.instructorId + " is already registered as an instructor";
			statusToUser.add(errorMessage);
			statusToAdmin = Const.ACTION_RESULT_FAILURE + " : " + errorMessage;
			return createShowPageResult(Const.ViewURIs.ADMIN_HOME, data);
		}
		
		try {
			
			logic.createAccount(data.instructorId,
					data.instructorName, true,
					data.instructorEmail,
					data.instructorInstitution);
			
			if (importSampleData != null) {
				importDemoData(data);
			}

		} catch (Exception e) {
			isError = true;
			statusToUser.add(e.getMessage());
			statusToAdmin = Const.ACTION_RESULT_FAILURE + " : " + e.getMessage();
			return createShowPageResult(Const.ViewURIs.ADMIN_HOME, data);
		}

		statusToUser.add("Instructor " + data.instructorName + " has been successfully created");
		statusToAdmin = "A New Instructor <span class=\"bold\">" + data.instructorName + "</span> has been created.<br>"
				+ "<span class=\"bold\">Id: </span>" + data.instructorId + "<br>"
				+ "<span class=\"bold\">Email: </span>" + data.instructorEmail 
				+ "<span class=\"bold\">Institution: </span>" + data.instructorInstitution;
		
		return createRedirectResult(Const.ActionURIs.ADMIN_HOME_PAGE);
	}
	
	private void importDemoData(AdminHomePageData helper)
			throws EntityAlreadyExistsException,
			InvalidParametersException, EntityDoesNotExistException {
		
		String jsonString;
		String courseId = helper.instructorId.concat("-demo").replace("@", "-at-");
		if (courseId.length() > 20) {
			courseId = courseId.substring(courseId.length() - 20);
		}
		
		jsonString = FileHelper.readStream(Config.class.getClassLoader()
				.getResourceAsStream("InstructorSampleData.json"));

		// replace email
		jsonString = jsonString.replaceAll(
				"teammates.demo.instructor@demo.course",
				helper.instructorEmail);
		// replace name
		jsonString = jsonString.replaceAll("Demo_Instructor",
				helper.instructorName);
		// replace id
		jsonString = jsonString.replaceAll("teammates.demo.instructor",
				helper.instructorId);
		// replace course
		jsonString = jsonString.replaceAll("demo.course", courseId);

		// update evaluation time
		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		c.set(Calendar.AM_PM, Calendar.PM);
		c.set(Calendar.HOUR, 11);
		c.set(Calendar.MINUTE, 59);
		c.set(Calendar.YEAR, c.get(Calendar.YEAR) + 1);
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm a Z");

		jsonString = jsonString.replace("2013-04-01 11:59 PM UTC",
				formatter.format(c.getTime()));

		Gson gson = Utils.getTeammatesGson();
		DataBundle data = gson.fromJson(jsonString, DataBundle.class);
		
		new BackDoorLogic().persistDataBundle(data);
		
	}
	

}
