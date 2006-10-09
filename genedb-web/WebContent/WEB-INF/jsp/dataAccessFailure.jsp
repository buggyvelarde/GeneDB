<%@ page import="org.springframework.dao.DataAccessException"%>
<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>

<format:header name="Data Access Failure">
	<st:init/>
</format:header>


<%
Exception ex = (Exception) request.getAttribute("exception");
%>

<H2>Data access failure: <%= ex.getMessage() %></H2>
<P>


<%
ex.printStackTrace(new java.io.PrintWriter(out));
%>

<P>
<BR>
<A href="<c:url value="/"/>">Home</A>


<format:footer />

