package teammates.servlet;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import teammates.api.Common;
import teammates.jsp.EvalSubmissionEditHelper;
import teammates.jsp.Helper;

@SuppressWarnings("serial")
public class StudentEvalEditHandlerServlet extends EvalSubmissionEditHandlerServlet {

	@Override
	protected String getDisplayURL(){
		return Common.PAGE_STUDENT_HOME;
	}

	@Override
	protected String getEditSubmissionLink(){
		return Common.PAGE_STUDENT_EVAL_SUBMISSION_EDIT;
	}
	
	@Override
	protected String getSuccessMessage(HttpServletRequest req, Helper helper){
		String evalName = req.getParameter(Common.PARAM_EVALUATION_NAME);
		String courseID = req.getParameter(Common.PARAM_COURSE_ID);
		return String.format(Common.MESSAGE_EVALUATION_SUBMISSION_RECEIVED,EvalSubmissionEditHelper.escapeHTML(evalName), courseID);
	}

	@Override
	protected boolean doAuthenticateUser(HttpServletRequest req,
			HttpServletResponse resp, Helper helper)
			throws IOException {
		if(!helper.user.isStudent && !helper.user.isAdmin){
			resp.sendRedirect(Common.JSP_UNAUTHORIZED);
			return false;
		}
		return true;
	}

}
