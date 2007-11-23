<%@ tag display-name="header"
        body-content="scriptless" %>
<%@ attribute name="name" required="true" %>
<%@ attribute name="title" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<head>
	<c:set var="pageTitle" value="${name} - GeneDB"/>
	<c:if test="!empty title">
		<c:set var="pageTitle" value="${title}" />
	</c:if>
	<title>${pageTitle}</title>
	<link rel="stylesheet" type="text/css" href="<c:url value="/includes/style/site.css"/>" />
	<link type="text/css" rel="stylesheet" href="<c:url value="/"/>includes/style/emerald.css">
    <jsp:doBody />
</head>    
<body>
<div id="header" align="right">
			<div id="floating-right">
				<img src="<c:url value="/"/>includes/images/fg.jpg"/>
				<img src="<c:url value="/"/>includes/images/he.jpg"/>
				<img src="<c:url value="/"/>includes/images/pl.jpg"/>
				<img src="<c:url value="/"/>includes/images/ve.gif"/>
			</div>
			<h2>${name}</h2>
			<div id="floating-left">
				<img src="<c:url value="/"/>includes/images/vi.jpg"/>
				<img src="<c:url value="/"/>includes/images/ba.jpg"/>
				<img src="<c:url value="/"/>includes/images/nm.gif"/>
				<img src="<c:url value="/"/>includes/images/bg.jpg"/>
			</div>
		</div>
<ul><c:forEach var="line" items="${ERROR_MSG}">
<li>${line}
</c:forEach></ul>