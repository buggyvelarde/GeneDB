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
<ul>
<c:forEach items="${queries}" var="query">
<li><a href="<c:url value="/Query" />?q=${query.key}">${query.key}</a></li>
</c:forEach>
</ul>
</div>

<st:flashMessage />

<format:footer />