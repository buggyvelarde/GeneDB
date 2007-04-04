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
    <jsp:doBody />
</head>    
<body>
<TABLE border="0" width="100%" cellpadding="0" >
  <TR>
    <TD align="left" width="187px">
      <a href="<c:url value="/"/>"><IMG alt="GeneDB Homepage" border="0" src="<c:url value="/includes/images/genedb.gif"/>"></a>
    
    </TD>
    <TD align="center" width="90%" background="<c:url value="/includes/images/header-bkgd.gif"/>">
      <h1>${name}</h1>
  </TD>
  <TD align="right" width="178px">
    <a href="<c:url value="/search.jsp"/>"><img alt="PSU logo" border="0" src="<c:url value="/includes/images/psu.gif"/>"></a>
  </TD>  
  </TR>
  <tr>
  	<td colspan="3">
  		<img src="<c:url value="/includes/images/purpleDot.gif"/>" width="100%" height="2" alt="----------------------">
  	</td>
  </tr>
</TABLE>
<ul><c:forEach var="line" items="${ERROR_MSG}">
<li>${line}
</c:forEach></ul>