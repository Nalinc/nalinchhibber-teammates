package teammates.ui.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import teammates.common.Common;
import teammates.common.datatransfer.AccountAttributes;

/**
 * A result that shows a page in the Browser. These are usually implemented as 
 * JSP pages.
 */
public class ShowPageResult extends ActionResult{
	
	/** The data that will be used to render the page*/
	public PageData data;
	
	public ShowPageResult(
			String destination, 
			AccountAttributes account,
			Map<String, String[]> parametersFromPreviousRequest,
			List<String> status) {
		super(destination, account, parametersFromPreviousRequest, status);
	}
	
	public ShowPageResult(
			String destination, 
			AccountAttributes account,
			Map<String, String[]> parametersFromPreviousRequest,
			PageData data,
			List<String> status) {
		super(destination, account, parametersFromPreviousRequest, status);
		this.data = data;
	}


	@Override
	public void send(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {
		
		req.setAttribute("data", data); 
		
		/* These two are required for the 'status message' section of the page
		 * Although these two are also sent as parameters in the URL,
		 *  they should be set as attributes too, because the status message
		 *  section is a {@code jsp:inclde} and cannot see parameters encoded 
		 *  in the URL
		 */ 
		req.setAttribute(Common.PARAM_ERROR, ""+isError); 
		req.setAttribute(Common.PARAM_STATUS_MESSAGE, ""+getStatusMessage()); 
		
		req.getRequestDispatcher(getDestinationWithParams()).forward(req, resp);
	}


}
