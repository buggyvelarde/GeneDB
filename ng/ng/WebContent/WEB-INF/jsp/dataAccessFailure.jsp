<%@ page import="org.springframework.dao.DataAccessException"%>
<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>

<format:header title="Data Access Failure" />
<format:page>
<br>
<format:header title="Data Access Failure" name="Data Access Failure" />


<%
Exception ex = (Exception) request.getAttribute("exception");
org.apache.log4j.Logger.getLogger("org.genedb.web.DataAccessFailure").error(ex);
%>

<H2>Data access failure: <%= ex.getMessage() %></H2>
<P>



<%
ex.printStackTrace(new java.io.PrintWriter(out));
%>

<P>
<BR>
<A href="<c:url value="/"/>">Home</A>


</format:page>