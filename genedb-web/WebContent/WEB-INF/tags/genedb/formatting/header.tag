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
    <jsp:doBody />
</head>    
<body>
<TABLE border="0" width="100%" cellpadding="0">
  <TR>
    <TD align="left" width="187px">
      <a href="<c:url value="/"/>"><IMG alt="GeneDB Homepage" border="0" src="<c:url value="/includes/images/genedb.gif"/>"></a>
    
    </TD>
    <TD align="center" width="90%">
      <h1>${name}</h1>
  </TD>
  <TD align="right" width="178px">
    <a href="<c:url value="/search.jsp"/>"><img alt="PSU logo" border="0" src="<c:url value="/includes/images/psu.gif"/>"></a>
  </TD>  
  </TR>
</TABLE>

<ul><c:forEach var="line" items="${ERROR_MSG}">
<li>${line}
</c:forEach></ul>