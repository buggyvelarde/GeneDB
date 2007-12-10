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
	<table align="center" width="100%" height="138px" style="background-image: url('includes/images/a2.gif'); background-repeat: repeat-x;">
		<tr>
			<td align="right" valign="bottom">
				<table align="left">
					<tr>
						<td width="55px"><img src="<c:url value="/"/>includes/images/fg.jpg" style="border: 3px solid #FFFFFF;"/></td>
						<td width="55px"><img src="<c:url value="/"/>includes/images/he.jpg" style="border: 3px solid #FFFFFF;"/></td>
						<td width="55px"><img src="<c:url value="/"/>includes/images/pl.jpg" style="border: 3px solid #FFFFFF;"/></td>
						<td width="55px"><img src="<c:url value="/"/>includes/images/ve.gif" style="border: 3px solid #FFFFFF;"/></td>
					</tr>
				</table>
			</td>
			<td align="center">
				<table align="center">
					<tr align="center">
						<td><h2 style="padding-top: 30px;">${name}</h2></td>
					</tr>
				</table>
			</td>
			<td align="left" valign="top">
				<table align="right">
					<tr align="left">
						<td width="55px"><img src="<c:url value="/"/>includes/images/vi.jpg" style="border: 3px solid #FFFFFF;"/></td>
						<td width="55px"><img src="<c:url value="/"/>includes/images/ba.jpg" style="border: 3px solid #FFFFFF;"/></td>
						<td width="55px"><img src="<c:url value="/"/>includes/images/nm.gif" style="border: 3px solid #FFFFFF;"/></td>
						<td width="55px"><img src="<c:url value="/"/>includes/images/bg.jpg" style="border: 3px solid #FFFFFF;"/></td>
					</tr>
				</table>
			</td>
		</tr>
	</table>
<ul><c:forEach var="line" items="${ERROR_MSG}">
<li>${line}
</c:forEach></ul>