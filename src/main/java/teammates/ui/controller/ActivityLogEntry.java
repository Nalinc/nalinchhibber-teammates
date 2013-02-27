package teammates.ui.controller;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import teammates.common.datatransfer.AccountData;

import com.google.appengine.api.log.AppLogLine;

public class ActivityLogEntry {
	private long time;
	private String servletName;
	private String action;
	private String role;
	private String name;
	private String googleId;
	private String email;
	private boolean toShow;
	private String message;
	
	
	/**
	 * Constructor that creates a empty ActivityLog
	 */
	public ActivityLogEntry(String servlet, String params){
		time = System.currentTimeMillis();
		servletName = servlet;
		action = "Unknown";
		role = "Unknown";
		name = "Unknown";
		googleId = "Unknown";
		email = "Unknown";
		toShow = true;
		message = "<span class=\"color_red\">Error. ActivityLog not created for this servlet action.</span><br>"
				+ params;
	}
	
	
	/**
	 * Constructor that creates an ActivityLog object from a app log on the server.
	 * Used in AdminActivityLogServlet.
	 * @param appLog
	 */
	public ActivityLogEntry(AppLogLine appLog){
		time = appLog.getTimeUsec();
		String[] tokens = appLog.getLogMessage().split("\\|\\|\\|", -1);
		
		//TEAMMATESLOG|||SERVLET_NAME|||ACTION|||TO_SHOW|||ROLE|||NAME|||GOOGLE_ID|||EMAIL|||MESSAGE(IN HTML)
		try{
			servletName = tokens[1];
			action = tokens[2];
			toShow = (tokens[3] == "true" ? true : false);
			role = tokens[4];
			name = tokens[5];
			googleId = tokens[6];
			email = tokens[7];
			message = tokens[8];
		} catch (ArrayIndexOutOfBoundsException e){
			
			servletName = "Unknown";
			action = "Unknown";
			role = "Unknown";
			name = "Unknown";
			googleId = "Unknown";
			email = "Unknown";
			toShow = true;
			message = "<span class=\"color_red\">Error. Problem parsing log message from the server.</span><br>"
					+ "System Error: " + e.getMessage() + "<br>" + appLog.getLogMessage();
		}
	}
	
	
	/**
	 * Constructor that creates an ActivityLog object from scratch
	 * Used in the various servlets in the application
	 * @param servlet
	 * @param action
	 * @param params
	 * @param toShow
	 */
	public ActivityLogEntry(String servlet, String act, boolean show, AccountData acc,  String params){
		time = System.currentTimeMillis();
		servletName = servlet;
		action = act;
		toShow = show;
		message = params;
		
		if (acc == null){
			role = "Unknown";
			name = "Unknown";
			googleId = "Unknown";
			email = "Unknown";
		} else {
			role = acc.isInstructor ? "Instructor" : "Student"; 
			name = acc.name;
			googleId = acc.googleId;
			email = acc.email;
		}
	}
	
	/**
	 * Generates a log message that will be logged in the server
	 * @return
	 */
	public String generateLogMessage(){
		//TEAMMATESLOG|||SERVLET_NAME|||ACTION|||TO_SHOW|||ROLE|||NAME|||GOOGLE_ID|||EMAIL|||MESSAGE(IN HTML)
		return "TEAMMATESLOG|||" + servletName + "|||" + action + "|||" + (toShow == true ? "true" : "false") + "|||" 
				+ role + "|||" + name + "|||" + googleId + "|||" + email + "|||" + message;
	}
	
	
	public String getDateInfo(){
		Calendar appCal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        appCal.setTimeInMillis((time / 1000) + 8*3600*1000);

		return sdf.format(appCal.getTime());
	}
	
	public String getRoleInfo(){
		return "<span class=\"bold\">" + role + "</span>";
	}
	
	public String getPersonInfo(){
		return "<span class=\"bold\">Id: </span>" + googleId + "<br>"
				+ "<span class=\"bold\">Name: </span>" + name + "<br>"
				+ email + "<br>";
	}
	
	public String getActionInfo(){
		return "<span class=\"color_green bold\">" + servletName + "</span><br>" + action;
	}
	
	public String getMessageInfo(){
		return message;
	}
}
