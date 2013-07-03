<%@ page import="teammates.common.util.Constants" %>
<%@ page import="teammates.ui.controller.AdminHomePageData"%>

<%
	AdminHomePageData data = (AdminHomePageData)request.getAttribute("data");
%>
<!DOCTYPE html>
<html>
<head>
	<link rel="shortcut icon" href="/favicon.png">
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>Teammates - Administrator</title>
	<link rel="stylesheet" href="/stylesheets/adminHome.css" type="text/css">
	<link rel="stylesheet" href="/stylesheets/common.css" type="text/css">

	<script type="text/javascript" src="/js/googleAnalytics.js"></script>
	<script type="text/javascript" src="/js/jquery-minified.js"></script>
	<script type="text/javascript" src="/js/tooltip.js"></script>
	<script type="text/javascript" src="/js/common.js"></script>
	<script type="text/javascript" src="/js/administrator.js"></script>
	<jsp:include page="../enableJS.jsp"></jsp:include>
</head>

<body>
	<div id="dhtmltooltip"></div>
	<div id="frameTop">
	<jsp:include page="<%=Constants.VIEW_ADMIN_HEADER%>" />
	</div>
	<div id="frameBody">
		<div id="frameBodyWrapper">
			<div id="topOfPage"></div>
			<div id="headerOperation">
			<h1>Add New Instructor</h1>
			</div>
			<div id="adminManagement">
				<form method="post" action="<%=Constants.ACTION_ADMIN_INSTRUCTORACCOUNT_ADD%>" name="form_addinstructoraccount">
					<table id="addform" class="inputTable">
					<tr>
						<td class="label bold">Google ID:</td>
					</tr>
					<tr>
					   <td><input class="addinput" type="text" name="<%=Constants.PARAM_INSTRUCTOR_ID%>"></td>
					</tr>
					<tr>
						<td class="label bold">Name:</td>
					</tr>
					<tr>
						<td><input class="addinput" type="text" name="<%=Constants.PARAM_INSTRUCTOR_NAME%>"></td>
				    </tr>
				    <tr>
					    <td class="label bold">Email: </td>
					</tr>
					<tr>
						<td><input class="addinput" type="text" name="<%=Constants.PARAM_INSTRUCTOR_EMAIL%>"></td>
				    </tr>
   				    <tr>
					    <td class="label bold">Institution: </td>
					</tr>
					<tr>
						<td><input class="addinput" type="text" name="<%=Constants.PARAM_INSTRUCTOR_INSTITUTION%>"></td>
				    </tr>
				    <tr>
						<td class="centeralign"><input type="checkbox" name="<%=Constants.PARAM_INSTRUCTOR_IMPORT_SAMPLE%>" value="importsample">Import sample data</td>
				    </tr>
				    <tr>
						<td class="centeralign"><input id="btnAddInstructor" class="button" type="submit" value="Add Instructor" onclick="return verifyInstructorData();"></td>
				    </tr>
				    </table>
				</form>
			</div>
			<jsp:include page="<%=Constants.VIEW_STATUS_MESSAGE%>" />
			<br>
			<br>
		</div>
	</div>

	<div id="frameBottom">
		<jsp:include page="<%=Constants.VIEW_FOOTER%>" />
	</div>
</body>
</html>