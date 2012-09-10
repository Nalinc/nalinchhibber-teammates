package teammates.logic.automated;

import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import teammates.common.Common;
import teammates.logic.backdoor.BackDoorLogic;

@SuppressWarnings("serial")
public class EvaluationOpeningRemindersServlet extends HttpServlet {
	
	@SuppressWarnings("unused")
	private static Logger log = Common.getLogger();
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp) {
		doGet(req, resp);
	}	
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp) {
		try {
			new BackDoorLogic().activateReadyEvaluations();
		} catch (Exception e) {
			throw new RuntimeException("Unexpected exception during evaluation activation",e);
		} 
	}

}
