<%@ page import="teammates.common.Common"%>
<!DOCTYPE html>

<html>
<head>
<link rel="shortcut icon" href="/favicon.png">
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Teammates</title>
<link rel="stylesheet" href="/stylesheets/common.css" type="text/css">
</head>
<body>
	<div id="frameTop">
		<div id="frameTopWrapper">
			<div id="logo">
				<a href="/index.html">
				<img alt="Teammates" src="/images/teammateslogo.jpg"
					height="47px" width="150px">
				</a>
			</div>
		</div>
	</div>

	<div id="frameBody">
		<div id="frameBodyWrapper">
			<div style="margin: 30px auto; border: 1px solid #333; padding: 10px; text-align: left; width: 550px; background: #FFFFCC; height: 100px;">
				<img src="/images/angry.png"
					style="float: left; height: 90px; margin: 0 10px 10px 0;">
				<p>
					You are not authorized to view this page. <br> <br>
					<a href="/logout.jsp">Logout and return to main page.</a>
				</p>
			</div>
		</div>
	</div>

	<div id="frameBottom">
		<jsp:include page="<%=Common.JSP_FOOTER%>" />
	</div>
</body>
</html>