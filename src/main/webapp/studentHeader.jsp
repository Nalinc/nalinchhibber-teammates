<%@page import="teammates.api.Common"%>
<%@ page import="teammates.jsp.Helper" %>
<% Helper helper = (Helper)request.getAttribute("helper"); %>
		<div id="frameTopWrapper">
			<div id="logo">
				<img alt="Teammates" height="47px"
					src="/images/teammateslogo.jpg"
					width="150px" />
			</div>			
			<div id="contentLinks">
				<ul id="navbar">
					<li><a class='t_home' href="<%= Common.JSP_STUDENT_HOME %>">Home</a></li>
					<li><a class='t_help' href="http://www.comp.nus.edu.sg/~teams/studenthelp.html" target="_blank">Help</a></li>
					<li><a class='t_logout' href="<%= Common.JSP_LOGOUT %>">Logout</a>
					 (<%=Helper.truncate(helper.userId.toLowerCase())%>)</li>
				</ul>
			</div>
		</div>