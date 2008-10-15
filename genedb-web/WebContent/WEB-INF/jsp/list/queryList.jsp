<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<format:headerRound title="Category List" bodyClass="genePage">
	<st:init />
	<link rel="stylesheet" type="text/css" href="<c:url value="/includes/style/genedb/genePage.css"/>" />
</format:headerRound>

<st:flashMessage />

<div id="queryList">
<table>
<c:forEach items="${queries}" var="query">
<tr><td><a href="<c:url value="/Query" />?q=${query.key}">${query.key}</a></td><td>${query.value.queryDescription}</td></tr>
</c:forEach>
</table>
</div>

<st:flashMessage />

<format:footer />