package teammates.servlet;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import teammates.api.Common;
import teammates.jsp.Helper;

@SuppressWarnings("serial")
public class StudentEvalEditHandlerServlet extends EvalSubmissionEditHandlerServlet {
	
	protected String getDisplayURL(){
		return Common.PAGE_STUDENT_HOME;
	}
	
	protected String getEditSubmissionLink(){
		return Common.PAGE_STUDENT_EVAL_SUBMISSION_EDIT;
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
