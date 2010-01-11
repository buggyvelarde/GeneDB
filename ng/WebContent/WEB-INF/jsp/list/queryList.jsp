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

<h2>GeneDB queries</h2>

<P>The following queries are available from the GeneDB website.</P>



<table cellpadding="0" cellspacing="4" border="0" class="sequence-table">
<c:forEach items="${queries}" var="query">
<tr><th><a href="<misc:url value="/Query/${query.realName}" />?taxons=">${query.queryName}</a></th><td>${query.queryDescription}</td></tr>
</c:forEach>
</table>
</format:genePageSection>
</div>
</format:page>
