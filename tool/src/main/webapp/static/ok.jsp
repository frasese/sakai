<%@ page import="org.sakaiproject.tool.cover.ToolManager"%>
<%@ page
	import="org.sakaiproject.component.cover.ServerConfigurationService"%>
<%@ page import="org.sakaiproject.user.cover.UserDirectoryService"%>
<%@ page session="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<!-- Make the page render as IE8 for wrapping in jstree -->
<meta http-equiv="X-UA-Compatible" content="IE=8">

<title>Module Signup</title>

<link href='<c:out value="${skinRepo}" />/tool_base.css' type="text/css" rel="stylesheet" media="all" />
<link href="<c:out value="${skinRepo}" />/<c:out value="${skinDefault}" />/tool.css" type="text/css" rel="stylesheet" media="all" />

<link rel="stylesheet" type="text/css" href="lib/jqmodal-r14/jqModal.css" />
<link rel="stylesheet" type="text/css" href="lib/dataTables-1.7/css/demo_table_jui.css" />
<link rel="stylesheet" type="text/css" href="lib/jquery-ui-1.8.4.custom/css/smoothness/jquery-ui-1.8.4.custom.css" />
<link rel="stylesheet" type="text/css" href="lib/tool.css" />

<script type="text/javascript" src="lib/jquery/jquery-1.4.2.min.js"></script>
<script type="text/javascript"
	src="lib/jstree-1.0rc2/_lib/jquery.cookie.js"></script>
<script type="text/javascript" src="lib/jstree-1.0rc2/jquery.jstree.js"></script>
<script type="text/javascript" src="lib/jqmodal-r14/jqModal.js"></script>
<script type="text/javascript"
	src="lib/jquery-ui-1.8.4.custom/js/jquery-ui-1.8.4.custom.min.js"></script>
<script type="text/javascript"
	src="lib/trimpath-template-1.0.38/trimpath-template.js"></script>
<script type="text/javascript"
	src="lib/dataTables-1.7/js/jquery.dataTables.js"></script>
<script type="text/javascript" src="lib/dataTables.reloadAjax.js"></script>
<script type="text/javascript" src="lib/signup.js"></script>
<script type="text/javascript" src="lib/Text.js"></script>
<script type="text/javascript" src="lib/serverDate.js"></script>
<script type="text/javascript" src="lib/QueryData.js"></script>

</head>
<body>
	<div id="toolbar">
		<ul class="navIntraTool actionToolBar">
			<li><span>Home</span></li>
			<li><span>Search Modules</span></li>
			<li><span>Browse by Department</span></li>
			<li><span>Browse by Calendar</span></li>
			<c:if test="${!externalUser}">
				<li><span>My Modules</span></li>
				<c:if test="${isPending}">
					<li><span><a href="pending.jsp">Pending Acceptances</a></span></li>
				</c:if>
				<c:if test="${isApprover}">
					<li><span><a href="approve.jsp">Pending
								Confirmations</a></span></li>
				</c:if>
				<c:if test="${isAdministrator}">
					<li><span><a href="admin.jsp">Module Administration</a></span></li>
				</c:if>
			</c:if>
		</ul>
	</div>
	<div id="messages"></div>

	<div id="browse">
		<p>
			<c:out value="${it.signup.user.name}" />
			has signed up to
			<c:out value="${it.signup.group.title}" />
			<c:choose>
				<c:when test="${it.status=='accept'}">
						Your acceptance for the following modules was successful:
					</c:when>
				<c:when test="${it.status=='approve'}">
						Your approval for the following modules was successful:
					</c:when>
				<c:when test="${it.status=='confirm'}">
						Your acceptance for the following modules was successful:
					</c:when>
			</c:choose>

			<br />
			<br />
			<c:out value="${it.signup.group.title}" />
			(
			<c:out value="${it.signup.group.department}" />
			) <br />
			<c:forEach var="component" items="${it.signup.components}">
					&nbsp;- <c:out value="${component.title}" />
					: <c:out value="${component.slot}" /> 
					for <c:out value="${component.sessions}" /> 
					starts in <c:out value="${component.when}" />
				<c:if test="${not empty component.presenter.name}">
						with <c:out value="${component.presenter.name}" />
				</c:if>
				<br />
			</c:forEach>
			<br />
		</p>
	</div>
	<br clear="all">
</body>
</html>