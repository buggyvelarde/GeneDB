<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<format:header title="Query List" />
<format:page>
<br>


<st:flashMessage />

<div id="queryList">
<format:genePageSection>
<table>
<c:forEach items="${queries}" var="query">
<tr><td><a href="<c:url value="/Query" />?q=${query.key}">${query.key}</a></td><td>${query.value.queryDescription}</td></tr>
</c:forEach>
</table>
</format:genePageSection>
</div>
</format:page>
